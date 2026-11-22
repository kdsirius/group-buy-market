package com.sirius.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author Sirius
 * @description 拼团订单枚举
 * @date 2026年09月25日 11:22
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GroupBuyOrderStatus {

    progress(0, "拼团中"),
    complete(1, "完成"),
    fail(2, "失败"),
    ;
    private Integer code;
    private String info;

    public static GroupBuyOrderStatus valueOf(Integer code) {
        for (GroupBuyOrderStatus enumVO : GroupBuyOrderStatus.values()) {
            if (enumVO.getCode().equals(code)) {
                return enumVO;
            }
        }
        throw new RuntimeException("err code not exist! code:" + code);
    }
}
