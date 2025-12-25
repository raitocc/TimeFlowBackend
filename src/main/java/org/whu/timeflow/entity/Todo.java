package org.whu.timeflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@TableName("todo")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)

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