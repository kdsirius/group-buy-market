package com.sirius.domain.trade.model.entity;

import com.sirius.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 一笔营销锁单的结果；金额属于该订单快照，而不是当前页面报价。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketPayOrderEntity {

    /** 拼单组队ID */
    private String teamId;
    /** 预购订单ID */
    private String orderId;
    /** 原价。 */
    private BigDecimal originalPrice;
    /** 优惠额。 */
    private BigDecimal deductionPrice;
    /** 实付额。 */
    private BigDecimal payPrice;
    /** 交易订单状态枚举 */
    private TradeOrderStatusEnumVO tradeOrderStatusEnumVO;

}
