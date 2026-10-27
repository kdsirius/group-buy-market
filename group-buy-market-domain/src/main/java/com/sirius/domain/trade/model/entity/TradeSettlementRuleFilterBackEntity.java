package com.sirius.domain.trade.model.entity;

import com.sirius.types.enums.GroupBuyOrderEnumVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSettlementRuleFilterBackEntity {
    private String teamId;
    private Long activityId;
    private Integer targetCount;
    private Integer completeCount;
    private Integer lockCount;
    private GroupBuyOrderEnumVO status;
    private Date validStartTime;
    private Date validEndTime;
    /** 开团时约定的通知地址。 */
    private String notifyUrl;
}
