package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.RepairRecord;
import com.gym.service.impl.RepairServiceImpl;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repair")
public class RepairController {

    private final RepairServiceImpl repairService;
    public RepairController(RepairServiceImpl repairService) {
        this.repairService = repairService;
    }

    @GetMapping
    public Result<List<RepairRecord>> list(@RequestParam(required = false) String status) {
        return Result.ok(repairService.listByStatus(status));
    }

    @GetMapping("/{id}")
    public Result<RepairRecord> get(@PathVariable Long id) {
        return Result.ok(repairService.getById(id));
    }

    /** 新增报修 - 自动把器材状态置为 repair */
    @PostMapping
    public Result<RepairRecord> add(@RequestBody RepairRecord rp) {
        return Result.ok(repairService.addRepair(rp));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RepairRecord rp) {
        rp.setId(id);
        repairService.updateById(rp);
        return Result.ok();
    }

    /** 维修完成 */
    @PostMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id,
                               @RequestParam(defaultValue = "false") boolean scrapped) {
        repairService.finishRepair(id, scrapped);
        return Result.ok();
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        repairService.removeById(id);
        return Result.ok();
    }
}
