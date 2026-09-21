package com.sirius.infrastructure.dcc;

import com.sirius.types.annotations.DCCValue;
import org.springframework.stereotype.Service;

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

    public boolean isDowngradeSwitch() {
        return "1".equals(downgradeSwitch);
    }

    public boolean isCutRange(String userId) {
        int hashCode = Math.abs(userId.hashCode());

        int lastTwoDigits = hashCode % 100;

        return lastTwoDigits <= Integer.parseInt(cutRange);
    }
}
