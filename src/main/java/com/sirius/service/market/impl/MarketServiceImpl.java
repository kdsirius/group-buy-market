package com.sirius.service.market.impl;

import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import com.sirius.model.dto.request.MarketProductRequest;
import com.sirius.model.vo.TrialBalanceVO;
import com.sirius.model.vo.UserGroupBuyOrderDetailVO;
import com.sirius.model.vo.GroupBuyActivityDiscountVO;
import com.sirius.model.vo.SkuVO;
import com.sirius.model.enums.TagScope;
import com.sirius.model.vo.TeamStatisticVO;
import com.sirius.repository.ActivityRepository;
import com.sirius.service.market.IMarketService;
import com.sirius.service.market.discount.IDiscountCalculateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class MarketServiceImpl implements IMarketService {

    @Resource
    private ActivityRepository repository;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private Map<String, IDiscountCalculateService> discountCalculateServiceMap;

    @Override
    public List<UserGroupBuyOrderDetailVO> queryInProgressUserGroupBuyOrderDetailList(
            Long activityId, String userId, Integer ownerCount, Integer randomCount) {
        Map<String, UserGroupBuyOrderDetailVO> teams = new LinkedHashMap<>();
        if (ownerCount > 0) {
            for (UserGroupBuyOrderDetailVO team :
                    repository.queryInProgressUserGroupBuyOrderDetailListByOwner(activityId, userId, ownerCount)) {
                teams.putIfAbsent(team.getTeamId(), team);
            }
        }
        if (randomCount > 0) {
            for (UserGroupBuyOrderDetailVO team :
                    repository.queryInProgressUserGroupBuyOrderDetailListByRandom(activityId, userId, randomCount)) {
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
    public TrialBalanceVO indexMarketTrial(MarketProductRequest request) throws Exception {
        validateRequest(request);

        if (repository.downgradeSwitch()) {
            throw new AppException(ResponseCode.E0003.getCode(), ResponseCode.E0003.getInfo());
        }
        if (!repository.cutRange(request.getUserId())) {
            throw new AppException(ResponseCode.E0004.getCode(), ResponseCode.E0004.getInfo());
        }

        MarketData marketData = queryMarketData(request);
        GroupBuyActivityDiscountVO activityDiscount = marketData.activityDiscount;
        SkuVO sku = marketData.sku;
        if (activityDiscount == null || sku == null || activityDiscount.getGroupBuyDiscount() == null) {
            throw new AppException(ResponseCode.E0002.getCode(), ResponseCode.E0002.getInfo());
        }

        IDiscountCalculateService calculator =
                discountCalculateServiceMap.get(activityDiscount.getGroupBuyDiscount().getMarketPlan());
        if (calculator == null) {
            throw new AppException(ResponseCode.E0001.getCode(), ResponseCode.E0001.getInfo());
        }

        BigDecimal payPrice = calculator.calculate(
                request.getUserId(), sku.getOriginalPrice(), activityDiscount.getGroupBuyDiscount());
        BigDecimal deductionPrice = sku.getOriginalPrice().subtract(payPrice);

        boolean visible = activityDiscount.isVisible();
        boolean enable = activityDiscount.isEnable();
        String tagId = activityDiscount.getTagId();
        if (StringUtils.isBlank(tagId)) {
            visible = TagScope.VISIBLE.getAllow();
            enable = TagScope.ENABLE.getAllow();
        }

        boolean inTagRange = repository.isTagCrowdRange(tagId, request.getUserId());
        visible = visible || inTagRange;
        enable = enable || inTagRange;

        return TrialBalanceVO.builder()
                .goodsId(sku.getGoodsId())
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .deductionPrice(deductionPrice)
                .payPrice(payPrice)
                .targetCount(activityDiscount.getTarget())
                .startTime(activityDiscount.getStartTime())
                .endTime(activityDiscount.getEndTime())
                .isVisible(visible)
                .isEnable(enable)
                .groupBuyActivityDiscountVO(activityDiscount)
                .build();
    }

    private void validateRequest(MarketProductRequest request) {
        if (request == null
                || StringUtils.isBlank(request.getUserId())
                || StringUtils.isBlank(request.getGoodsId())
                || StringUtils.isBlank(request.getSource())
                || StringUtils.isBlank(request.getChannel())) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), ResponseCode.ILLEGAL_PARAMETER.getInfo());
        }
    }

    private MarketData queryMarketData(MarketProductRequest request) throws Exception {
        Future<GroupBuyActivityDiscountVO> activityFuture = threadPoolExecutor.submit(
                () -> queryActivityDiscount(request));
        Future<SkuVO> skuFuture = threadPoolExecutor.submit(
                () -> repository.querySkuByGoodsId(request.getGoodsId()));

        return new MarketData(
                activityFuture.get(500L, TimeUnit.MINUTES),
                skuFuture.get(500L, TimeUnit.MINUTES));
    }

    private GroupBuyActivityDiscountVO queryActivityDiscount(MarketProductRequest request) {
        Long activityId = request.getActivityId();
        if (activityId == null) {
            com.sirius.model.vo.SCSkuActivityVO skuActivity =
                    repository.querySCSkuActivityBySCGoodsId(
                            request.getSource(), request.getChannel(), request.getGoodsId());
            if (skuActivity == null) {
                return null;
            }
            activityId = skuActivity.getActivityId();
        }
        return repository.queryGroupBuyActivityDiscountVO(activityId);
    }

    private static class MarketData {
        private final GroupBuyActivityDiscountVO activityDiscount;
        private final SkuVO sku;

        private MarketData(GroupBuyActivityDiscountVO activityDiscount, SkuVO sku) {
            this.activityDiscount = activityDiscount;
            this.sku = sku;
        }
    }
}
