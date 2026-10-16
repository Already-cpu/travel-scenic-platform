package com.example.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.travel.common.AuthUtils;
import com.example.travel.common.BusinessException;
import com.example.travel.common.PageResult;
import com.example.travel.dto.LoginRequest;
import com.example.travel.dto.QrLoginRequest;
import com.example.travel.dto.RegisterRequest;
import com.example.travel.dto.SmsLoginRequest;
import com.example.travel.dto.UserUpdateRequest;
import com.example.travel.dto.UserView;
import com.example.travel.entity.User;
import com.example.travel.mapper.UserMapper;
import com.example.travel.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void register(RegisterRequest request) {
        User existed = userMapper.selectOne(new QueryWrapper<User>().eq("phone", request.getPhone()));
        if (existed != null) {
            throw new BusinessException("该手机号已注册");
        }
        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRealName(request.getNickname());
        // The frontend shows the nickname initial until a local avatar is uploaded.
        user.setAvatar(null);
        user.setStatus(1);
        userMapper.insert(user);
    }

    public Object login(LoginRequest request) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", request.getPhone()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("手机号或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        return issueToken(user);
    }

    public Object smsLogin(SmsLoginRequest request) {
        if (!"123456".equals(request.getCode())) {
            throw new BusinessException("演示验证码错误，请输入 123456");
        }
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", request.getPhone()));
        if (user == null) throw new BusinessException("该手机号尚未注册，请先注册账号");
        return issueToken(user);
    }

    public Object qrLogin(QrLoginRequest request) {
        if (!"TRAVEL-DEMO-QR-2026".equals(request.getQrCode())) {
            throw new BusinessException("二维码已失效，请重新扫码");
        }
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("phone", "13800138000"));
        if (user == null) throw new BusinessException("演示用户不存在，请先导入数据库演示数据");
        return issueToken(user);
    }

    private Object issueToken(User user) {
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userMapper.updateById(user);
        return new com.example.travel.dto.TokenResponse<>(jwtService.generate("USER:" + user.getId(), "USER"), UserView.from(user));
    }

    public UserView current() {
        User user = userMapper.selectById(AuthUtils.currentId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return UserView.from(user);
    }

    public UserView update(UserUpdateRequest request) {
        User user = userMapper.selectById(AuthUtils.currentId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname().trim());
        }
        if (request.getRealName() != null) {
            user.setRealName(request.getRealName().trim());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar().trim());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        userMapper.updateById(user);
        return UserView.from(userMapper.selectById(user.getId()));
    }

    public PageResult<UserView> adminPage(long current, long size, String keyword) {
        Page<User> page = new Page<>(current, size);
        QueryWrapper<User> query = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            query.and(wrapper -> wrapper.like("phone", keyword).or().like("nickname", keyword).or().like("real_name", keyword));
        }
        query.orderByDesc("create_time");
        Page<User> result = userMapper.selectPage(page, query);
        return new PageResult<>(result.getRecords().stream().map(UserView::from).collect(Collectors.toList()),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        if (userMapper.deleteById(id) == 0) {
            throw new BusinessException("用户不存在");
        }
    }
}
