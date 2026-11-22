package com.sirius.model.dto;


import lombok.Data;

import java.util.Date;

@Data
public class SettlementMarketPayOrderRequestDTO {
    private String userId;
    private String source;
    private String channel;
    private String outTradeNo;
    // JSON演示用毫秒时间戳；生产必须来自可信支付系统，不相信浏览器任意值。
    private Date outTradeTime;
}