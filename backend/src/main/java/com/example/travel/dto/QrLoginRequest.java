package com.example.travel.dto;

import javax.validation.constraints.NotBlank;

public class QrLoginRequest {
    @NotBlank(message = "二维码凭证不能为空")
    private String qrCode;

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}
