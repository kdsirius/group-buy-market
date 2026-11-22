package com.sirius.model.vo;

import com.sirius.model.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 拼团完成通知配置；路由键由拼团服务从配置中补齐，调用方只选择通知类型。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyConfigVO {
    /** HTTP 或 MQ。 */
    private NotifyType notifyType;
    /** 服务端固定的 MQ 路由键。 */
    private String notifyMQ;
    /** HTTP 回调地址。 */
    private String notifyUrl;
}
