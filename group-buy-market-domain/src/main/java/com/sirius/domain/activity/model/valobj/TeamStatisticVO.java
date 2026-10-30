package com.sirius.domain.activity.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamStatisticVO {
    private Integer allTeamCount;
    private Integer allTeamCompleteCount;
    // 本节口径是锁单名额总量，不是去重用户数。
    private Integer allTeamUserCount;
}
