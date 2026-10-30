package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.EnergyRecord;
import com.gym.mapper.EnergyRecordMapper;
import com.gym.service.EnergyService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnergyServiceImpl extends ServiceImpl<EnergyRecordMapper, EnergyRecord> implements EnergyService {

    @Override
    public List<EnergyRecord> search(Integer year) {
        LambdaQueryWrapper<EnergyRecord> w = new LambdaQueryWrapper<>();
        if (year != null) {
            w.eq(EnergyRecord::getStatYear, year);
        }
        w.orderByAsc(EnergyRecord::getStatYear).orderByAsc(EnergyRecord::getStatMonth);
        return list(w);
    }

    @Override
    public EnergyRecord findByYearMonth(Integer year, Integer month) {
        return getOne(new LambdaQueryWrapper<EnergyRecord>()
                .eq(EnergyRecord::getStatYear, year)
                .eq(EnergyRecord::getStatMonth, month));
    }

    /**
     * 自动计算电费/水费/合计
     */
    public EnergyRecord calcCosts(EnergyRecord r) {
        if (r.getElecUsage() != null && r.getElecPrice() != null) {
            r.setElecCost(r.getElecUsage().multiply(r.getElecPrice()).setScale(2, RoundingMode.HALF_UP));
        }
        if (r.getWaterUsage() != null && r.getWaterPrice() != null) {
            r.setWaterCost(r.getWaterUsage().multiply(r.getWaterPrice()).setScale(2, RoundingMode.HALF_UP));
        }
        BigDecimal elec = r.getElecCost() != null ? r.getElecCost() : BigDecimal.ZERO;
        BigDecimal water = r.getWaterCost() != null ? r.getWaterCost() : BigDecimal.ZERO;
        r.setTotalCost(elec.add(water).setScale(2, RoundingMode.HALF_UP));
        return r;
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> map = new HashMap<>();
        List<EnergyRecord> all = list();
        BigDecimal totalCost = all.stream().map(EnergyRecord::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        map.put("totalCost", totalCost);
        map.put("count", all.size());
        return map;
    }
}
