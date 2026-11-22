package com.sirius.mapper;

import com.sirius.model.entity.Sku;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Sirius
 * @description 商品查询
 * @date 2026年09月19日 13:06
 */
@Mapper
public interface ISkuDao {

    Sku querySkuByGoodsId(String goodsId);
}
