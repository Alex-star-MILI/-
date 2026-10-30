package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("energy_record")
public class EnergyRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("stat_year")
    private Integer statYear;
    @TableField("stat_month")
    private Integer statMonth;
    private BigDecimal elecUsage;
    private BigDecimal elecPrice;
    private BigDecimal elecCost;
    private BigDecimal waterUsage;
    private BigDecimal waterPrice;
    private BigDecimal waterCost;
    private BigDecimal totalCost;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
