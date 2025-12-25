package org.whu.timeflow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("bill")
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