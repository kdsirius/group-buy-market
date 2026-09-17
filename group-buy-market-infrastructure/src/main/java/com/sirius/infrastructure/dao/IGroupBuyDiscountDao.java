package com.sirius.infrastructure.dao;

import com.sirius.infrastructure.dao.po.GroupBuyDiscount;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Sirius
 * @description 折扣配置Dao
 * @date 2026年09月17日 21:15
 */
@Mapper
public interface IGroupBuyDiscountDao {
    List<GroupBuyDiscount> queryGroupBuyDiscountList();
}
