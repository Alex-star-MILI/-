package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Equipment;
import java.util.List;

public interface EquipmentService extends IService<Equipment> {
    List<Equipment> search(String keyword, String status, String location);
    java.util.Map<String, Long> statusStats();
    java.util.List<Object[]> statusCount();
}
