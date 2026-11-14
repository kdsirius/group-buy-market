package com.sirius.domain.trade.model.entity;

import com.sirius.domain.trade.model.valobj.NotifyConfigVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author Sirius
 * @description 拼团，支付优惠实体对象
 * @date 2026年09月22日 16:38
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayDiscountEntity {

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
    /** 开团时约定的通知地址。 */
    private NotifyConfigVO notifyConfigVO;

}
