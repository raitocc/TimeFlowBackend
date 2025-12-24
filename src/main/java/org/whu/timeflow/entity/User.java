package org.whu.timeflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User {
    @TableId // 默认不仅是主键，MyBatis-Plus 插入时如果没有值，会自动根据策略生成。
    // 但因为我们要用 UUID，通常手动生成或者配置 IdType.ASSIGN_UUID
    private String id;

    private String email;
    private String password; // 真正开发时不返回密码，这里为了省事，DTO处理
    private String nickname;
    private LocalDateTime createTime;
}