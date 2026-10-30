package com.sirius.trigger.http;

import com.sirius.api.IMarketIndexService;
import com.sirius.api.dto.GoodsMarketRequestDTO;
import com.sirius.api.dto.GoodsMarketResponseDTO;
import com.sirius.api.response.Response;
import com.sirius.domain.activity.model.entity.MarketProductEntity;
import com.sirius.domain.activity.model.entity.TrialBalanceEntity;
import com.sirius.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import com.sirius.domain.activity.model.valobj.TeamStatisticVO;
import com.sirius.domain.activity.service.IIndexGroupBuyMarketService;
import com.sirius.types.enums.ResponseCode;
import com.sirius.types.exception.AppException;
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
public class MarketIndexController implements IMarketIndexService {

    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @PostMapping("query_group_buy_market_config")
    @Override
    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(@RequestBody GoodsMarketRequestDTO request) {
        // 校验参数
        if (StringUtils.isAnyBlank(request.getUserId(), request.getSource(),
                request.getChannel(), request.getGoodsId())) {

            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(ResponseCode.ILLEGAL_PARAMETER.getInfo()).build();
        }

        try {
            TrialBalanceEntity trial = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
                    .userId(request.getUserId())
                    .goodsId(request.getGoodsId())
                    .source(request.getSource())
                    .channel(request.getChannel()).build());

            Long activityId = trial.getGroupBuyActivityDiscountVO().getActivityId();
            List<UserGroupBuyOrderDetailEntity> details = indexGroupBuyMarketService
                    .queryInProgressUserGroupBuyOrderDetailList(activityId, request.getUserId(), 1, 2);
            TeamStatisticVO statistic =
                    indexGroupBuyMarketService.queryTeamStatisticByActivityId(activityId);

            // 同一次响应用同一个now算倒计时，DTO不泄露完整活动/折扣配置。
            Date now = new Date();
            List<GoodsMarketResponseDTO.Team> teams = new ArrayList<>();
            for (UserGroupBuyOrderDetailEntity detail : details) {
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
