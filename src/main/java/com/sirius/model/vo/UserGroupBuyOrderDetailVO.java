package com.sirius.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupBuyOrderDetailVO {
    private String userId;
    private String teamId;
    private Long activityId;
    private Integer targetCount;
    private Integer completeCount;
    private Integer lockCount;
    private Date validStartTime;
    private Date validEndTime;
    // 仅自己的候选保留此字段，不能向其他用户泄漏外部单号。
    private String outTradeNo;
}
