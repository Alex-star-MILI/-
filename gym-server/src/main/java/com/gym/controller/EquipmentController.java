package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.Equipment;
import com.gym.service.EquipmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;
    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    /** 列表 + 搜索 */
    @GetMapping
    public Result<List<Equipment>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) String location) {
        return Result.ok(equipmentService.search(keyword, status, location));
    }

    /** 状态统计 */
    @GetMapping("/stats")
    public Result<Map<String, Long>> stats() {
        return Result.ok(equipmentService.statusStats());
    }

    /** 单个 */
    @GetMapping("/{id}")
    public Result<Equipment> get(@PathVariable Long id) {
        return Result.ok(equipmentService.getById(id));
    }

    /** 新增 */
    @PostMapping
    public Result<Equipment> add(@RequestBody Equipment eq) {
        equipmentService.save(eq);
        return Result.ok(eq);
    }

    /** 修改 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Equipment eq) {
        eq.setId(id);
        equipmentService.updateById(eq);
        return Result.ok();
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        equipmentService.removeById(id);
        return Result.ok();
    }
}
