package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.EnergyRecord;
import java.util.List;
import java.util.Map;

public interface EnergyService extends IService<EnergyRecord> {
    List<EnergyRecord> search(Integer year);
    EnergyRecord findByYearMonth(Integer year, Integer month);
    Map<String, Object> stats();
}
