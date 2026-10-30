package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Equipment;
import com.gym.entity.RepairRecord;
import com.gym.mapper.EquipmentMapper;
import com.gym.mapper.RepairRecordMapper;
import com.gym.service.RepairService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RepairServiceImpl extends ServiceImpl<RepairRecordMapper, RepairRecord> implements RepairService {

    private final EquipmentMapper equipmentMapper;
    public RepairServiceImpl(EquipmentMapper equipmentMapper) {
        this.equipmentMapper = equipmentMapper;
    }

    @Override
    public List<RepairRecord> listByStatus(String status) {
        LambdaQueryWrapper<RepairRecord> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            w.eq(RepairRecord::getStatus, status);
        }
        w.orderByDesc(RepairRecord::getId);
        return list(w);
    }

    /**
     * 完成维修 - 同步更新器材状态
     */
    @Transactional
    public void finishRepair(Long repairId, boolean scrapped) {
        RepairRecord rp = getById(repairId);
        if (rp == null) return;
        rp.setStatus(scrapped ? "scrapped" : "done");
        updateById(rp);
        Equipment eq = equipmentMapper.selectById(rp.getEquipmentId());
        if (eq != null) {
            eq.setStatus(scrapped ? "scrapped" : "normal");
            equipmentMapper.updateById(eq);
        }
    }

    /**
     * 新增维修记录 - 自动把器材置为 repair
     */
    @Transactional
    public RepairRecord addRepair(RepairRecord rp) {
        Equipment eq = equipmentMapper.selectById(rp.getEquipmentId());
        if (eq != null) {
            rp.setEquipmentName(eq.getName());
            eq.setStatus("repair");
            equipmentMapper.updateById(eq);
        }
        rp.setStatus("repairing");
        save(rp);
        return rp;
    }
}
