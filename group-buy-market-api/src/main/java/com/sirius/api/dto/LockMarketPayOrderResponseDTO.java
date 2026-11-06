package com.sirius.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 营销锁单响应；orderId 是拼团订单号，三个金额均由拼团服务确定。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LockMarketPayOrderResponseDTO {

    /**
     * 预购订单ID
     */
    private String orderId;
    /**
     * 原价。
     */
    private BigDecimal originalPrice;
    /**
     * 优惠额。
     */
    private BigDecimal deductionPrice;
    /**
     * 实付额。
     */
    private BigDecimal payPrice;
    /**
     * 0 表示初始锁单；不是支付成功。
     */
    private Integer tradeOrderStatus;

}
