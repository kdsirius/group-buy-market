package com.sirius.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sirius
 * @description 拼团交易命令实体
 * @date 2026年09月24日 19:06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeLockRuleCommandEntity {

    /** 用户Id */
    private String userId;
    /** 活动Id */
    private Long activityId;

}