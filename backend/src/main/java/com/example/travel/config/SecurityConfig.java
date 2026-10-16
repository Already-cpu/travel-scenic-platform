package com.example.travel.config;

import com.example.travel.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/", "/index.html", "/assets/**", "/scenic-images/**", "/image-unavailable.svg",
                        "/photo-credits.html", "/photo-credits.json", "/login", "/register", "/scenic",
                        "/scenic/**", "/account", "/admin", "/admin/**", "/error").permitAll()
                .antMatchers("/api/user/register", "/api/user/login", "/api/user/sms-login", "/api/user/qr-login", "/api/admin/login", "/api/assistant/chat").permitAll()
                .antMatchers(HttpMethod.GET, "/api/scenic/list", "/api/scenic/hot", "/api/scenic/*").permitAll()
                .antMatchers(HttpMethod.GET, "/api/comment/scenic/**").permitAll()
                .antMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                .anyRequest().authenticated().and()
                .exceptionHandling().authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(200);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":401,\"msg\":\"请先登录\",\"data\":null}");
                }).and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
