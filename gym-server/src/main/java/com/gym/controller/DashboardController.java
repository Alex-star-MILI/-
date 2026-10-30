package com.gym.controller;

import com.gym.common.Result;
import com.gym.service.EquipmentService;
import com.gym.service.impl.EnergyServiceImpl;
import com.gym.service.impl.MemberServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/** 首页 Dashboard 聚合统计 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final EquipmentService equipmentService;
    private final MemberServiceImpl memberService;
    private final EnergyServiceImpl energyService;

    public DashboardController(EquipmentService equipmentService,
                               MemberServiceImpl memberService,
                               EnergyServiceImpl energyService) {
        this.equipmentService = equipmentService;
        this.memberService = memberService;
        this.energyService = energyService;
    }

    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Map<String, Object> data = new HashMap<>();
        data.put("equipment", equipmentService.statusStats());
        Map<String, Object> memberStats = memberService.dashboardStats();
        data.put("member", memberStats);
        Map<String, Object> energyStats = energyService.stats();
        data.put("energy", energyStats);
        return Result.ok(data);
    }
}
