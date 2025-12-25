package org.whu.timeflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@TableName("diary")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Diary {
    @TableId
    private String id;
    private String userId;
    private String content;
    private String mood;
    private String weather;
    private String images; // JSON 字符串
    private Long createTime;
    private Long updateTime;
    private Integer isDeleted;
}