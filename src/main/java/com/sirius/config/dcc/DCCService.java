package com.sirius.config.dcc;

import com.sirius.common.annotations.DCCValue;
import com.sirius.common.Constants;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @author Sirius
 * @description 动态配置中心
 * @date 2026年09月21日 12:17
 */
@Service
public class DCCService {

    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    @DCCValue("cutRange:100")
    private String cutRange;

    @DCCValue("scBlacklist:s02c02")
    private String scBlacklist;

    public boolean isSCBlackIntercept(String source, String channel) {
        return Arrays.asList(scBlacklist.split(Constants.SPLIT)).contains(source + channel);
    }

    public boolean isDowngradeSwitch() {
        return "1".equals(downgradeSwitch);
    }

    public boolean isCutRange(String userId) {
        int hashCode = Math.abs(userId.hashCode());

        int lastTwoDigits = hashCode % 100;

        return lastTwoDigits <= Integer.parseInt(cutRange);
    }
}
