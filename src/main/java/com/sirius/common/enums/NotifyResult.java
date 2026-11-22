package com.sirius.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotifyResult {
    SUCCESS("success"),
    ERROR("error"),
    NULL("null"),
    ;

    /** NULL 表示本次没拿到锁，不等于通知成功。 */
    private final String code;
}
