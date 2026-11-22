package com.sirius.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Sirius
 * @description 拼团活动状态枚举
 * @date 2026年09月24日 19:14
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ActivityStatus {
    CREATE(0, "创建"),
    EFFECTIVE(1, "生效"),
    OVERDUE(2, "过期"),
    ABANDONED(2, "废弃"),
    ;
    private Integer code;
    private String info;

    public static ActivityStatus valueOf(Integer code) {
        for (ActivityStatus status : ActivityStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("err code not exist：" + code);
    }
}
