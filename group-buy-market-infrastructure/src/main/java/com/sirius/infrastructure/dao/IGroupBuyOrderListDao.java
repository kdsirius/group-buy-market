package com.sirius.infrastructure.dao;

import com.sirius.infrastructure.dao.po.GroupBuyOrderList;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Sirius
 * @description 用户拼单明细
 * @date 2026年09月22日 15:25
 */
@Mapper
public interface IGroupBuyOrderListDao {

    void insert(GroupBuyOrderList groupBuyOrderListReq);

    GroupBuyOrderList queryGroupBuyOrderRecordByOutTradeNo(GroupBuyOrderList groupBuyOrderListReq);


    Integer queryOrderCountByActivityId(GroupBuyOrderList groupBuyOrderListReq);
}
