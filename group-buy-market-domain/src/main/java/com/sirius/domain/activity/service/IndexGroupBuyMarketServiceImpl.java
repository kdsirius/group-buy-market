package com.sirius.domain.activity.service;

import com.sirius.domain.activity.adapter.repository.IActivityRepository;
import com.sirius.domain.activity.model.entity.MarketProductEntity;
import com.sirius.domain.activity.model.entity.TrialBalanceEntity;
import com.sirius.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import com.sirius.domain.activity.model.valobj.TeamStatisticVO;
import com.sirius.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.sirius.types.design.framework.tree.StrategyHandler;
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
public class IndexGroupBuyMarketServiceImpl implements IIndexGroupBuyMarketService {
    @Resource
    private DefaultActivityStrategyFactory defaultActivityStrategyFactory;

    @Resource
    private IActivityRepository repository;

    @Override
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

    @Override
    public TeamStatisticVO queryTeamStatisticByActivityId(Long activityId) {
        return repository.queryTeamStatisticByActivityId(activityId);
    }

    @Override
    public TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception {
        // 获取执行策略
        StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> strategyHandler = defaultActivityStrategyFactory.strategyHandler();
        // 受理试算操作
        return strategyHandler.apply(marketProductEntity, DefaultActivityStrategyFactory.DynamicContext.builder().build());
    }
}
