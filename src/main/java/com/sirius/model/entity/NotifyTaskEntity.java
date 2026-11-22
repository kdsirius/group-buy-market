package com.sirius.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一次拼团完成通知任务；一团一条任务，负责承载发送所需的快照。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyTaskEntity {
    private String teamId;
    private String notifyType;
    private String notifyMQ;
    private String notifyUrl;
    /** 已尝试次数；本次发送前读取这个数。 */
    private Integer notifyCount;
    /** 成团消息 JSON，不在发送端重新查询订单。 */
    private String parameterJson;

    public String lockKey() {
        return "notify_job_lock_key_" + teamId;
    }
}
