package com.sirius.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sirius
 * @description 拼团交易，过滤反馈实体
 * @date 2026年09月24日 19:09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeLockRuleFilterBackEntity {

    /** 用户参与的订单量 */
    private Integer userTakeOrderCount;

}
