package com.sirius.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Sirius
 * @description 用户拼单明细
 * @date 2026年09月22日 15:22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyOrderList implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 自增ID */
    private Long id;
    /** 用户ID */
    private String userId;
    /** 拼单组队ID */
    private String teamId;
    /** 订单ID */
    private String orderId;
    /** 活动ID */
    private Long activityId;
    /** 活动开始时间 */
    private Date startTime;
    /** 活动结束时间 */
    private Date endTime;
    /** 商品ID */
    private String goodsId;
    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 原始价格 */
    private BigDecimal originalPrice;
    /** 折扣金额 */
    private BigDecimal deductionPrice;
    /** 状态；0初始锁定、1消费完成 */
    private Integer status;
    /** 外部交易单号-确保外部调用唯一幂等 */
    private String outTradeNo;
    /* 业务唯一Id */
    private String bizId;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
    /** 外部支付完成时间 */
    private Date outTradeTime;

    // 只作为查询上限，不是表字段
    private Integer count;

}
