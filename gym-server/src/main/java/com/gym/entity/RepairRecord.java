package com.gym.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("repair_record")
public class RepairRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private String fault;
    private LocalDate repairDate;
    private BigDecimal cost;
    private String repairPerson;
    private String status; // repairing/done/scrapped
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
