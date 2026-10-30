package com.gym.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Member;
import com.gym.mapper.MemberMapper;
import com.gym.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {

    @Override
    public List<Member> search(String keyword, String cardType, String status) {
        LambdaQueryWrapper<Member> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Member::getName, keyword)
                    .or().like(Member::getPhone, keyword)
                    .or().like(Member::getId, keyword));
        }
        if (StringUtils.hasText(cardType)) {
            w.eq(Member::getCardType, cardType);
        }
        if ("active".equals(status)) {
            w.ge(Member::getExpireDate, LocalDate.now());
        } else if ("expired".equals(status)) {
            w.lt(Member::getExpireDate, LocalDate.now());
        }
        w.orderByDesc(Member::getId);
        return list(w);
    }

    @Override
    public List<Member> expiringMembers(int days) {
        LocalDate future = LocalDate.now().plusDays(days);
        LambdaQueryWrapper<Member> w = new LambdaQueryWrapper<>();
        w.le(Member::getExpireDate, future);
        w.orderByAsc(Member::getExpireDate);
        return list(w);
    }

    @Override
    @Transactional
    public void renew(Long id, Integer addYears, Integer addMonths, Integer addDays,
                      LocalDate newExpireDate, BigDecimal amount) {
        Member m = getById(id);
        if (m == null) return;
        if (newExpireDate != null) {
            m.setExpireDate(newExpireDate);
        } else {
            LocalDate d = m.getExpireDate() != null ? m.getExpireDate() : LocalDate.now();
            if (addYears != null) d = d.plusYears(addYears);
            if (addMonths != null) d = d.plusMonths(addMonths);
            if (addDays != null) d = d.plusDays(addDays);
            m.setExpireDate(d);
        }
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal current = m.getAmount() != null ? m.getAmount() : BigDecimal.ZERO;
            m.setAmount(current.add(amount));
        }
        updateById(m);
    }

    @Override
    public Map<String, Object> dashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        long total = count();
        long active = baseMapper.selectCount(new LambdaQueryWrapper<Member>().ge(Member::getExpireDate, LocalDate.now()));
        long expired = total - active;
        // 卡类型统计
        long month = baseMapper.selectCount(new LambdaQueryWrapper<Member>().eq(Member::getCardType, "month"));
        long quarter = baseMapper.selectCount(new LambdaQueryWrapper<Member>().eq(Member::getCardType, "quarter"));
        long year = baseMapper.selectCount(new LambdaQueryWrapper<Member>().eq(Member::getCardType, "year"));
        // 累计办卡金额
        BigDecimal totalRevenue = baseMapper.selectList(null).stream()
                .map(Member::getAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("total", total);
        stats.put("active", active);
        stats.put("expired", expired);
        stats.put("cardType", Map.of("month", month, "quarter", quarter, "year", year));
        stats.put("totalRevenue", totalRevenue);
        return stats;
    }
}
