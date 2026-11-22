package com.sirius.model.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 渠道商品活动配置关联表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SCSkuActivity implements Serializable {
    private static final long serialVersionUID = 1L;

    // 自增ID
    private Integer id;

    // 渠道
    private String source;

    // 来源
    private String channel;

    // 活动ID
    private Long activityId;

    // 商品ID (varchar类型，映射为String)
    private String goodsId;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;
}
