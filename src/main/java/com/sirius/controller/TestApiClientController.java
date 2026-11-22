package com.sirius.controller;

import com.sirius.model.dto.NotifyRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestApiClientController {

    @PostMapping("group_buy_notify")
    public String groupBuyNotify(@RequestBody NotifyRequestDTO request) {
        // 本机模拟商城：只能证明收到内容，不代表真实发货成功。
        log.info("模拟商城收到拼团完成通知 teamId:{} orders:{}",
                request.getTeamId(), request.getOutTradeNoList());
        return "success";
    }
}
