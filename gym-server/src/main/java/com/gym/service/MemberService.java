package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Member;
import java.time.LocalDate;
import java.util.List;

public interface MemberService extends IService<Member> {
    List<Member> search(String keyword, String cardType, String status);
    List<Member> expiringMembers(int days);
    /**
     * 续费延期
     */
    void renew(Long id, Integer addYears, Integer addMonths, Integer addDays,
               LocalDate newExpireDate, java.math.BigDecimal amount);
    java.util.Map<String, Object> dashboardStats();
}
