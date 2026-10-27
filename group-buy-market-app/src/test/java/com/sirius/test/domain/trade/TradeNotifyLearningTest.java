package com.sirius.test.domain.trade;


import com.sirius.domain.trade.adapter.port.ITradePort;
import com.sirius.domain.trade.adapter.repository.ITradeRepository;
import com.sirius.domain.trade.model.entity.NotifyTaskEntity;
import com.sirius.domain.trade.service.settlement.TradeSettlementOrderService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TradeNotifyLearningTest {
    private ITradeRepository repository;
    private ITradePort port;
    private TradeSettlementOrderService service;
    private NotifyTaskEntity task;

    @Before
    public void setUp() {
        repository = mock(ITradeRepository.class);
        port = mock(ITradePort.class);
        service = new TradeSettlementOrderService();
        ReflectionTestUtils.setField(service, "repository", repository);
        ReflectionTestUtils.setField(service, "port", port);
        task = NotifyTaskEntity.builder().teamId("team-001").notifyCount(0)
                .notifyUrl("http://127.0.0.1:8091/api/v1/test/group_buy_notify")
                .parameterJson("{\"teamId\":\"team-001\",\"outTradeNoList\":[\"pay-001\"]}")
                .build();
        // 固定只返回这一张待办单，测试不会扫描你的真实数据库。
        when(repository.queryUnExecutedNotifyTaskList()).thenReturn(Collections.singletonList(task));
    }

    @Test
    public void test_01_success_shouldFinish() throws Exception {
        when(port.groupBuyNotify(task)).thenReturn("success");
        when(repository.updateNotifyTaskStatusSuccess("team-001")).thenReturn(1);
        Map<String, Integer> result = service.execSettlementNotifyJob();
        assertEquals(Integer.valueOf(1), result.get("successCount"));
        verify(port).groupBuyNotify(task);
        verify(repository).queryUnExecutedNotifyTaskList();
        verify(repository).updateNotifyTaskStatusSuccess("team-001");
        verifyNoMoreInteractions(repository, port);
    }

    @Test
    public void test_02_firstFailure_shouldRetry() throws Exception {
        // 之前0次，本次第1次失败，应保留自动重试资格。
        when(port.groupBuyNotify(task)).thenReturn("error");
        when(repository.updateNotifyTaskStatusRetry("team-001")).thenReturn(1);
        Map<String, Integer> result = service.execSettlementNotifyJob();
        assertEquals(Integer.valueOf(1), result.get("retryCount"));
        verify(repository).queryUnExecutedNotifyTaskList();
        verify(repository).updateNotifyTaskStatusRetry("team-001");
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_03_fifthFailure_shouldStop() throws Exception {
        // 之前4次，本次第5次也失败；不能再写状态2无限重试。
        task.setNotifyCount(4);
        when(port.groupBuyNotify(task)).thenReturn("error");
        when(repository.updateNotifyTaskStatusError("team-001")).thenReturn(1);
        Map<String, Integer> result = service.execSettlementNotifyJob();
        assertEquals(Integer.valueOf(1), result.get("errorCount"));
        verify(repository).queryUnExecutedNotifyTaskList();
        verify(repository).updateNotifyTaskStatusError("team-001");
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_04_withoutLock_shouldNotCountAttempt() throws Exception {
        when(port.groupBuyNotify(task)).thenReturn("null");
        Map<String, Integer> result = service.execSettlementNotifyJob();
        assertEquals(Integer.valueOf(0), result.get("successCount"));
        assertEquals(Integer.valueOf(0), result.get("retryCount"));
        assertEquals(Integer.valueOf(0), result.get("errorCount"));
        verify(repository).queryUnExecutedNotifyTaskList();
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_05_specificTeam_shouldNotScanAllTeams() throws Exception {
        when(repository.queryUnExecutedNotifyTaskList("team-001"))
                .thenReturn(Collections.singletonList(task));
        when(port.groupBuyNotify(task)).thenReturn("success");
        when(repository.updateNotifyTaskStatusSuccess("team-001")).thenReturn(1);
        service.execSettlementNotifyJob("team-001");
        verify(repository).queryUnExecutedNotifyTaskList("team-001");
        verify(repository).updateNotifyTaskStatusSuccess("team-001");
        verifyNoMoreInteractions(repository);
    }

    @Test
    public void test_06_emptyTasks_shouldNotSend() throws Exception {
        when(repository.queryUnExecutedNotifyTaskList()).thenReturn(Collections.emptyList());
        Map<String, Integer> result = service.execSettlementNotifyJob();
        assertEquals(Integer.valueOf(0), result.get("waitCount"));
        verifyNoInteractions(port);
    }
}
