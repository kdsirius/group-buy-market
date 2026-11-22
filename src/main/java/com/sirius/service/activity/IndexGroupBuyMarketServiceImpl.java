package com.sirius.service.activity;

import com.sirius.repository.ActivityRepository;
import com.sirius.model.entity.MarketProductEntity;
import com.sirius.model.entity.TrialBalanceEntity;
import com.sirius.model.entity.UserGroupBuyOrderDetailEntity;
import com.sirius.model.value.TeamStatisticVO;
import com.sirius.service.activity.trial.factory.DefaultActivityStrategyFactory;
import com.sirius.common.design.framework.tree.StrategyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Sirius
 * @description 首页营销服务
 * @date 2026年09月18日 17:04
 */
@Slf4j
@Service
public class IndexGroupBuyMarketServiceImpl {
    @Resource
    private DefaultActivityStrategyFactory defaultActivityStrategyFactory;

    @Resource
    private ActivityRepository repository;

    public List<UserGroupBuyOrderDetailEntity> queryInProgressUserGroupBuyOrderDetailList(Long activityId, String userId,
                                                                                          Integer ownerCount, Integer randomCount) {
        // 有序Map保留第一次出现的团：自己的优先，再加入其他候选。
        Map<String, UserGroupBuyOrderDetailEntity> teams = new LinkedHashMap<>();
        if (ownerCount > 0) {
            for (UserGroupBuyOrderDetailEntity team : repository
                    .queryInProgressUserGroupBuyOrderDetailListByOwner(activityId, userId, ownerCount)) {
                teams.putIfAbsent(team.getTeamId(), team);
            }
        }
        if (randomCount > 0) {
            for (UserGroupBuyOrderDetailEntity team : repository
                    .queryInProgressUserGroupBuyOrderDetailListByRandom(activityId, userId, randomCount)) {
                teams.putIfAbsent(team.getTeamId(), team);
            }
        }
        return new ArrayList<>(teams.values());
    }

    public TeamStatisticVO queryTeamStatisticByActivityId(Long activityId) {
        return repository.queryTeamStatisticByActivityId(activityId);
    }

    public TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception {
        // 获取执行策略
        StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> strategyHandler = defaultActivityStrategyFactory.strategyHandler();
        // 受理试算操作
        return strategyHandler.apply(marketProductEntity, DefaultActivityStrategyFactory.DynamicContext.builder().build());
    }
}
