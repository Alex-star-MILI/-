package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.EnergyRecord;
import com.gym.service.impl.EnergyServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/energy")
public class EnergyController {

    private final EnergyServiceImpl energyService;
    public EnergyController(EnergyServiceImpl energyService) {
        this.energyService = energyService;
    }

    @GetMapping
    public Result<List<EnergyRecord>> list(@RequestParam(required = false) Integer year) {
        return Result.ok(energyService.search(year));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(energyService.stats());
    }

    @GetMapping("/{id}")
    public Result<EnergyRecord> get(@PathVariable Long id) {
        return Result.ok(energyService.getById(id));
    }

    @PostMapping
    public Result<EnergyRecord> add(@RequestBody EnergyRecord r) {
        energyService.calcCosts(r);
        energyService.save(r);
        return Result.ok(r);
    }

    @PutMapping("/{id}")
    public Result<EnergyRecord> update(@PathVariable Long id, @RequestBody EnergyRecord r) {
        r.setId(id);
        energyService.calcCosts(r);
        energyService.updateById(r);
        return Result.ok(r);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        energyService.removeById(id);
        return Result.ok();
    }
}
