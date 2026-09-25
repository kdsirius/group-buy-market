package com.sirius.types.enums;

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
public enum ActivityStatusEnumVO {
    CREATE(0, "创建"),
    EFFECTIVE(1, "生效"),
    OVERDUE(2, "过期"),
    ABANDONED(2, "废弃"),
    ;
    private Integer code;
    private String info;

    public static ActivityStatusEnumVO valueOf(Integer code) {
        for (ActivityStatusEnumVO activityStatusEnumVO : ActivityStatusEnumVO.values()) {
            if (activityStatusEnumVO.getCode().equals(code)) {
                return activityStatusEnumVO;
            }
        }
        throw new IllegalArgumentException("err code not exist：" + code);
    }
}
