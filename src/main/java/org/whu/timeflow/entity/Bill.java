package org.whu.timeflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("bill")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Bill {
    @TableId
    private String id;
    private String userId;
    private BigDecimal amount;
    private String type;
    private String remark;
    private Long createTime;
    private Long updateTime;
    private Integer isDeleted;
}