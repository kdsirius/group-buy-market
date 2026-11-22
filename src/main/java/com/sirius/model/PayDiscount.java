package com.sirius.model;

import com.sirius.model.vo.NotifyConfigVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 拼团锁单内部优惠金额快照参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayDiscount {

    /** 来源 */
    private String source;
    /** 渠道 */
    private String channel;
    /** 商品ID */
    private String goodsId;
    /** 商品名称 */
    private String goodsName;
    /** 原始价格 */
    private BigDecimal originalPrice;
    /** 折扣金额 */
    private BigDecimal deductionPrice;
    /** 支付金额 */
    private BigDecimal payPrice;
    /** 外部交易单号-确保外部调用唯一幂等 */
    private String outTradeNo;
    /** 开团时约定的通知配置 */
    private NotifyConfigVO notifyConfigVO;

}
