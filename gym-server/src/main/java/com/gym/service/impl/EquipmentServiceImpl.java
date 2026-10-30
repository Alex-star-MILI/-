package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Equipment;
import com.gym.mapper.EquipmentMapper;
import com.gym.service.EquipmentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    @Override
    public List<Equipment> search(String keyword, String status, String location) {
        LambdaQueryWrapper<Equipment> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Equipment::getName, keyword)
                    .or().like(Equipment::getModel, keyword)
                    .or().like(Equipment::getId, keyword));
        }
        if (StringUtils.hasText(status)) {
            w.eq(Equipment::getStatus, status);
        }
        if (StringUtils.hasText(location)) {
            w.like(Equipment::getLocation, location);
        }
        w.orderByDesc(Equipment::getId);
        return list(w);
    }

    @Override
    public Map<String, Long> statusStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("normal", baseMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getStatus, "normal")));
        stats.put("repair", baseMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getStatus, "repair")));
        stats.put("scrapped", baseMapper.selectCount(new LambdaQueryWrapper<Equipment>().eq(Equipment::getStatus, "scrapped")));
        return stats;
    }

    @Override
    public List<Object[]> statusCount() { return null; }
}
