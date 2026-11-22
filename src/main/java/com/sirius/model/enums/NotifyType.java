package com.sirius.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 拼团完成后的通知方式。
 */
@Getter
@AllArgsConstructor
public enum NotifyType {
    HTTP("HTTP", "HTTP 回调"),
    MQ("MQ", "MQ 消息通知");

    private final String code;
    private final String info;
}
