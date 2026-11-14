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
 * @description 用户拼单
 * @date 2026年09月22日 15:22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBuyOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 自增ID */
    private Long id;
    /** 拼单组队ID */
    private String teamId;
    /** 活动ID */
    private Long activityId;
    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 原始价格 */
    private BigDecimal originalPrice;
    /** 折扣金额 */
    private BigDecimal deductionPrice;
    /** 支付价格 */
    private BigDecimal payPrice;
    /** 目标数量 */
    private Integer targetCount;
    /** 完成数量 */
    private Integer completeCount;
    /** 锁单数量 */
    private Integer lockCount;
    /** 状态（0-拼单中、1-完成、2-失败） */
    private Integer status;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
    /** 开团时保存的团有效开始时间 */
    private Date validStartTime;
    /** 开团时保存的团有效结束时间 */
    private Date validEndTime;
    /** 回调类型：HTTP、MQ。 */
    private String notifyType;
    /** HTTP 回调地址；MQ 任务为空。 */
    private String notifyUrl;

}
