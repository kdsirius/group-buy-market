package com.sirius.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 拼团锁单内部活动上下文参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PayActivity {

    /** 拼单组队ID */
    private String teamId;
    /** 活动ID */
    private Long activityId;
    /** 活动名称 */
    private String activityName;
    /** 拼团开始时间 */
    private Date startTime;
    /** 拼团结束时间 */
    private Date endTime;
    /** 目标数量 */
    private Integer targetCount;
    /** 有效时间（分钟） */
    private Integer validTime;

}
