package com.sirius.model.value;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Sirius
 * @description 活动人群标签作用域范围枚举
 * @date 2026年09月20日 20:48
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor

public enum TagScopeEnumVO {
    VISIBLE(true, false, "是否可看见拼团"),
    ENABLE(true, false, "是否可参与拼团"),
    ;

    private Boolean allow;
    private Boolean refuse;
    private String desc;

}
