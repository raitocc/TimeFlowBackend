package org.whu.timeflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("todo")
public class Todo {
    @TableId
    private String id;
    private String userId;
    private String content;
    private Integer isCompleted;
    private Long deadline;
    private Long createTime;
    private Long updateTime;
    private Integer isDeleted;
}