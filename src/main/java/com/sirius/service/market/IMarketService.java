package com.sirius.service.market;

import com.sirius.model.vo.TrialBalanceVO;
import com.sirius.model.vo.UserGroupBuyOrderDetailVO;
import com.sirius.model.vo.TeamStatisticVO;

import java.util.List;

public interface IMarketService {

    List<UserGroupBuyOrderDetailVO> queryInProgressUserGroupBuyOrderDetailList(
            Long activityId, String userId, Integer ownerCount, Integer randomCount);

    TeamStatisticVO queryTeamStatisticByActivityId(Long activityId);

    TrialBalanceVO indexMarketTrial(com.sirius.model.dto.request.MarketProductRequest request) throws Exception;
}
