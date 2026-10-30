package com.gym.controller;

import com.gym.common.Result;
import com.gym.entity.Member;
import com.gym.service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public Result<List<Member>> list(@RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String cardType,
                                     @RequestParam(required = false) String status) {
        return Result.ok(memberService.search(keyword, cardType, status));
    }

    /** 到期提醒 */
    @GetMapping("/expiring")
    public Result<List<Member>> expiring(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(memberService.expiringMembers(days));
    }

    /** 统计 */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.ok(memberService.dashboardStats());
    }

    @GetMapping("/{id}")
    public Result<Member> get(@PathVariable Long id) {
        return Result.ok(memberService.getById(id));
    }

    @PostMapping
    public Result<Member> add(@RequestBody Member m) {
        if (m.getAmount() == null) m.setAmount(BigDecimal.ZERO);
        memberService.save(m);
        return Result.ok(m);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Member m) {
        m.setId(id);
        memberService.updateById(m);
        return Result.ok();
    }

    /**
     * 续费延期
     * 参数示例：{"addMonths":1,"amount":199} 或 {"addYears":1,"amount":1999} 或 {"newExpireDate":"2027-10-30","amount":200}
     */
    @PostMapping("/{id}/renew")
    public Result<Void> renew(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer addYears = toInt(body.get("addYears"));
        Integer addMonths = toInt(body.get("addMonths"));
        Integer addDays = toInt(body.get("addDays"));
        LocalDate newExpireDate = body.get("newExpireDate") != null ? LocalDate.parse(body.get("newExpireDate").toString()) : null;
        BigDecimal amount = body.get("amount") != null ? new BigDecimal(body.get("amount").toString()) : BigDecimal.ZERO;
        memberService.renew(id, addYears, addMonths, addDays, newExpireDate, amount);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        memberService.removeById(id);
        return Result.ok();
    }

    private Integer toInt(Object o) {
        if (o == null) return null;
        return Integer.parseInt(o.toString());
    }
}
