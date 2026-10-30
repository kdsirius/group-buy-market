package com.sirius.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsMarketResponseDTO {
    private Goods goods;
    private List<Team> teamList;
    private TeamStatistic teamStatistic;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Goods {
        private String goodsId;
        private BigDecimal originalPrice;
        private BigDecimal deductionPrice;
        private BigDecimal payPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Team {
        private String userId;
        private String teamId;
        private Long activityId;
        private Integer targetCount;
        private Integer completeCount;
        private Integer lockCount;
        private Date validStartTime;
        private Date validEndTime;
        private String validTimeCountdown;
        private String outTradeNo;

        public static String differenceDateTime2Str(Date now, Date end) {
            if (now == null || end == null) return "无效的时间";
            // 截止后展示0，不给前端负倒计时；保留总小时数，不在24小时处绕回。
            long seconds = Math.max(0L, (end.getTime() - now.getTime()) / 1000L);
            return String.format("%02d:%02d:%02d", seconds / 3600,
                    seconds / 60 % 60, seconds % 60);
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamStatistic {
        private Integer allTeamCount;
        private Integer allTeamCompleteCount;
        private Integer allTeamUserCount;
    }
}
