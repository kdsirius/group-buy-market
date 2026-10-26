package com.sirius.test.domain.trade;

import com.sirius.domain.trade.adapter.repository.ITradeRepository;
import com.sirius.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.sirius.domain.trade.model.entity.GroupBuyTeamEntity;
import com.sirius.domain.trade.model.entity.MarketPayOrderEntity;
import com.sirius.domain.trade.model.entity.TradePaySettlementEntity;
import com.sirius.domain.trade.model.entity.TradePaySuccessEntity;
import com.sirius.domain.trade.service.settlement.TradeSettlementOrderService;
import com.sirius.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import com.sirius.domain.trade.service.settlement.filter.EndRuleFilter;
import com.sirius.domain.trade.service.settlement.filter.OutTradeNoRuleFilter;
import com.sirius.domain.trade.service.settlement.filter.SCRuleFilter;
import com.sirius.domain.trade.service.settlement.filter.SettableRuleFilter;
import com.sirius.types.exception.AppException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TradeSettlementRuleLearningTest {
    private ITradeRepository repository;
    private TradeSettlementOrderService service;
    private Date deadline;

    @Before
    public void setUp() throws Exception {
        // 每个测试开始前重建：上一题的“黑名单”不会污染下一题。
        repository = mock(ITradeRepository.class);
        deadline = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .parse("2026-01-01 14:30:00");

        // 普通题的默认场景：渠道允许，当前用户有一笔未结算锁单。
        when(repository.isSCBlackIntercept("s01", "c01")).thenReturn(false);
        when(repository.queryMarketPayOrderEntityByOutTradeNo("learner", "pay-001"))
                .thenReturn(MarketPayOrderEntity.builder().teamId("team-001").build());
        when(repository.queryGroupBuyTeamByTeamId("team-001"))
                .thenReturn(GroupBuyTeamEntity.builder()
                        .teamId("team-001").activityId(100123L)
                        .targetCount(3).completeCount(1).lockCount(3)
                        .validStartTime(new Date(deadline.getTime() - 30 * 60_000L))
                        .validEndTime(deadline).build());

        // 这些是你的真实节点，不是 mock；不启动 Spring，就手动放入仓储依赖。
        SCRuleFilter sc = new SCRuleFilter();
        OutTradeNoRuleFilter order = new OutTradeNoRuleFilter();
        SettableRuleFilter time = new SettableRuleFilter();
        EndRuleFilter end = new EndRuleFilter();
        ReflectionTestUtils.setField(sc, "repository", repository);
        ReflectionTestUtils.setField(order, "repository", repository);
        ReflectionTestUtils.setField(time, "repository", repository);

        // 调用你自己的工厂，验证的就是项目中定义的那条链及其顺序。
        TradeSettlementRuleFilterFactory factory = new TradeSettlementRuleFilterFactory();
        service = new TradeSettlementOrderService();
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "tradeSettlementRuleFilter",
                factory.tradeSettlementRuleFilter(sc, order, time, end));
    }

    // 这里统一组装支付入参；各测试分别调整支付时间或仓储回答，模拟不同场景。
    private TradePaySuccessEntity payment(Date paidAt) {
        return TradePaySuccessEntity.builder()
                .source("s01").channel("c01").userId("learner")
                .outTradeNo("pay-001").outTradeTime(paidAt).build();
    }

    @Test
    public void test_01_beforeDeadline_shouldSettle() throws Exception {
        // 准备：比截止早一分钟。调用真实服务，四个节点都应放行。
        Date paidAt = new Date(deadline.getTime() - 60_000L);
        TradePaySettlementEntity result = service.settlementMarketPayOrder(payment(paidAt));

        // 检查返回值来自这个订单所属的团，不是某个固定空对象。
        assertEquals("team-001", result.getTeamId());
        assertEquals(Long.valueOf(100123L), result.getActivityId());

        // 捕获服务交给仓储的“记账申请”，检查支付时间和团号没有丢。
        ArgumentCaptor<GroupBuyTeamSettlementAggregate> captor =
                ArgumentCaptor.forClass(GroupBuyTeamSettlementAggregate.class);
        verify(repository).settlementMarketPayOrder(captor.capture());
        assertEquals("team-001", captor.getValue().getGroupBuyTeamEntity().getTeamId());
        assertEquals(paidAt, captor.getValue().getTradePaySuccessEntity().getOutTradeTime());
        // 这里只证明申请记账一次；假仓储不会更新真实数据库。
    }

    @Test
    public void test_02_blacklisted_shouldStopImmediately() {
        // 只改变一个条件：这次渠道被禁用。
        when(repository.isSCBlackIntercept("s01", "c01")).thenReturn(true);
        AppException error = assertThrows(AppException.class,
                () -> service.settlementMarketPayOrder(payment(deadline)));
        assertEquals("E0105", error.getCode());

        // 只能查一次渠道；若继续查订单、查团或申请记账，下面就会报红。
        verify(repository).isSCBlackIntercept("s01", "c01");
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_03_missingOrder_shouldStopBeforeQueryingTeam() {
        // 渠道仍允许，但仓储说：没有这笔用户的未结算订单。
        when(repository.queryMarketPayOrderEntityByOutTradeNo("learner", "pay-001"))
                .thenReturn(null);
        AppException error = assertThrows(AppException.class,
                () -> service.settlementMarketPayOrder(payment(deadline)));
        assertEquals("E0104", error.getCode());

        verify(repository).isSCBlackIntercept("s01", "c01");
        verify(repository).queryMarketPayOrderEntityByOutTradeNo("learner", "pay-001");
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_04_equalDeadline_shouldReject() {
        // “不晚于”与“早于”不同：当前规则要求严格早于，等于也拒绝。
        AppException error = assertThrows(AppException.class,
                () -> service.settlementMarketPayOrder(payment(deadline)));
        assertEquals("E0106", error.getCode());

        verify(repository).isSCBlackIntercept("s01", "c01");
        verify(repository).queryMarketPayOrderEntityByOutTradeNo("learner", "pay-001");
        verify(repository).queryGroupBuyTeamByTeamId("team-001");
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_05_afterDeadline_shouldReject() {
        // 比截止晚一分钟。已经查到团，但不能再申请记账。
        Date paidAt = new Date(deadline.getTime() + 60_000L);
        AppException error = assertThrows(AppException.class,
                () -> service.settlementMarketPayOrder(payment(paidAt)));
        assertEquals("E0106", error.getCode());

        verify(repository).isSCBlackIntercept("s01", "c01");
        verify(repository).queryMarketPayOrderEntityByOutTradeNo("learner", "pay-001");
        verify(repository).queryGroupBuyTeamByTeamId("team-001");
        verifyNoMoreInteractions(repository);
    }
}