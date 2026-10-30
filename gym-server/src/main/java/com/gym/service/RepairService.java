package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.RepairRecord;
import java.util.List;

public interface RepairService extends IService<RepairRecord> {
    List<RepairRecord> listByStatus(String status);
}
