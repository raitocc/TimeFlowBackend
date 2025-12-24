package org.whu.timeflow.dto;
import lombok.Data;

@Data
public class UserDTO {
    private String email;
    private String password;    // 登录/注册用
    private String nickname;
    private String oldPassword; // 修改密码专用
    private String newPassword; // 修改密码专用
}