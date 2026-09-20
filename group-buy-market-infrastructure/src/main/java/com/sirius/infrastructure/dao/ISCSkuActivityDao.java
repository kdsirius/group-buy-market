package com.sirius.infrastructure.dao;

import com.sirius.infrastructure.dao.po.SCSkuActivity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Sirius
 * @description 渠道商品活动配置关联表 Dao
 * @date 2026年09月20日 10:44
 */
@Mapper
public interface ISCSkuActivityDao {

    SCSkuActivity querySCSkuActivityBySCGoodsId(SCSkuActivity scSkuActivityReq);
}
