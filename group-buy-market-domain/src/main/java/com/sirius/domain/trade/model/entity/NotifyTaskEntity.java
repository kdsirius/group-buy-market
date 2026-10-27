package com.sirius.domain.trade.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyTaskEntity {
    /** 发送的是整个团完成的信息，一团一份任务。 */
    private String teamId;
    private String notifyUrl;
    /** 已尝试次数；本次发送前读取这个数。 */
    private Integer notifyCount;
    /** 已经生成好的 JSON，不在 HTTP 网关里重新拼订单。 */
    private String parameterJson;

    public String lockKey() {
        return "notify_job_lock_key_" + teamId;
    }
}
