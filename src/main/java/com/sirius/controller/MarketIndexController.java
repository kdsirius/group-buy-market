package com.sirius.controller;

import com.sirius.model.dto.request.GoodsMarketRequest;
import com.sirius.model.dto.response.GoodsMarketResponseDTO;
import com.sirius.model.dto.response.Response;
import com.sirius.model.dto.request.MarketProductRequest;
import com.sirius.model.vo.TrialBalanceVO;
import com.sirius.model.vo.UserGroupBuyOrderDetailVO;
import com.sirius.model.vo.TeamStatisticVO;
import com.sirius.service.market.IMarketService;
import com.sirius.common.enums.ResponseCode;
import com.sirius.common.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *  首页试算服务
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/index/")
public class MarketIndexController {

    @Resource
    private IMarketService indexGroupBuyMarketService;

    @PostMapping("query_group_buy_market_config")
    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(@RequestBody GoodsMarketRequest request) {
        // 校验参数
        if (StringUtils.isAnyBlank(request.getUserId(), request.getSource(),
                request.getChannel(), request.getGoodsId())) {

            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo()).build();
        }

        try {
            TrialBalanceVO trial = indexGroupBuyMarketService.indexMarketTrial(MarketProductRequest.builder()
                    .userId(request.getUserId())
                    .goodsId(request.getGoodsId())
                    .source(request.getSource())
                    .channel(request.getChannel()).build());

            Long activityId = trial.getGroupBuyActivityDiscountVO().getActivityId();
            List<UserGroupBuyOrderDetailVO> details = indexGroupBuyMarketService
                    .queryInProgressUserGroupBuyOrderDetailList(activityId, request.getUserId(), 1, 2);
            TeamStatisticVO statistic =
                    indexGroupBuyMarketService.queryTeamStatisticByActivityId(activityId);

            // 同一次响应用同一个now算倒计时，DTO不泄露完整活动/折扣配置。
            Date now = new Date();
            List<GoodsMarketResponseDTO.Team> teams = new ArrayList<>();
            for (UserGroupBuyOrderDetailVO detail : details) {
                teams.add(GoodsMarketResponseDTO.Team.builder()
                        .userId(detail.getUserId()).teamId(detail.getTeamId()).activityId(detail.getActivityId())
                        .targetCount(detail.getTargetCount()).completeCount(detail.getCompleteCount()).lockCount(detail.getLockCount())
                        .validStartTime(detail.getValidStartTime()).validEndTime(detail.getValidEndTime()).
                        validTimeCountdown(GoodsMarketResponseDTO.Team.differenceDateTime2Str(now, detail.getValidEndTime()))
                        .outTradeNo(detail.getOutTradeNo()).build());
            }

            // 组装数据
            GoodsMarketResponseDTO.Goods goods = GoodsMarketResponseDTO.Goods.builder()
                    .goodsId(trial.getGoodsId())
                    .originalPrice(trial.getOriginalPrice()).deductionPrice(trial.getDeductionPrice())
                    .payPrice(trial.getPayPrice())
                    .build();

            GoodsMarketResponseDTO.TeamStatistic teamStatistic = GoodsMarketResponseDTO.TeamStatistic.builder()
                    .allTeamCount(statistic.getAllTeamCount())
                    .allTeamCompleteCount(statistic.getAllTeamCompleteCount())
                    .allTeamUserCount(statistic.getAllTeamUserCount())
                    .build();

            GoodsMarketResponseDTO response = GoodsMarketResponseDTO.builder()
                    .activityId(activityId)
                    .goods(goods)
                    .teamList(teams)
                    .teamStatistic(teamStatistic)
                    .build();

            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(response)
                    .build();

        } catch (AppException e) {
            return Response.<GoodsMarketResponseDTO>builder()
                    .code(e.getCode())
                    .info(e.getInfo())
                    .build();

        } catch (Exception e) {
            log.error("首页营销查询失败", e);
            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
