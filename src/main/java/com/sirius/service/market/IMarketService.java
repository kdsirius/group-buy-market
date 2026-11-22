package com.sirius.service.market;

import com.sirius.model.vo.TrialBalanceEntity;
import com.sirius.model.vo.UserGroupBuyOrderDetailEntity;
import com.sirius.model.vo.TeamStatisticVO;

import java.util.List;

public interface IMarketService {

    List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailList(
            Long activityId, String userId, Integer ownerCount, Integer randomCount);

    TeamStatisticVO queryTeamStatisticByActivityId(Long activityId);

    TrialBalanceEntity indexMarketTrial(com.sirius.model.dto.request.MarketProductEntity request) throws Exception;
}
