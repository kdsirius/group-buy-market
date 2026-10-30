package com.sirius.infrastructure.dao;

import com.sirius.infrastructure.dao.po.GroupBuyOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Sirius
 * @description 用户拼单Dao
 * @date 2026年09月22日 15:25
 */
@Mapper
public interface IGroupBuyOrderDao {

    void insert(GroupBuyOrder groupBuyOrderReq);

    int updateAddLockCount(String teamId);

    int updateSubtractionLockCount(String teamId);

    GroupBuyOrder queryGroupBuyProgress(String teamId);

    GroupBuyOrder queryGroupBuyTeamByTeamId(String teamId);

    int updateAddCompleteCount(String teamId);

    int updateOrderStatus2COMPLETE(String teamId);

    List<GroupBuyOrder> queryGroupBuyProgressByTeamIds(@Param("teamIds") Set<String> teamIds);

    Integer queryAllTeamCount(@Param("activityId") Long activityId);

    Integer queryAllTeamCompleteCount(@Param("activityId") Long activityId);

    Integer queryAllUserCount(@Param("activityId") Long activityId);
}
