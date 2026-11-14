package com.sirius.domain.trade.model.entity;

import com.sirius.domain.trade.model.valobj.NotifyConfigVO;
import com.sirius.types.enums.GroupBuyOrderEnumVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author Sirius
 * @description 拼团组队实体对象
 * @date 2026年09月25日 14:43
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyTeamEntity {

    /** 拼单组队ID */
    private String teamId;
    /** 活动ID */
    private Long activityId;
    /** 目标数量 */
    private Integer targetCount;
    /** 完成数量 */
    private Integer completeCount;
    /** 锁单数量 */
    private Integer lockCount;
    /** 状态（0-拼单中、1-完成、2-失败） */
    private GroupBuyOrderEnumVO status;
    /** 开团时保存的团有效开始时间 */
    private Date validStartTime;
    /** 开团时保存的团有效结束时间 */
    private Date validEndTime;
    /** 锁单时约定的完成通知配置。 */
    private NotifyConfigVO notifyConfigVO;

}
