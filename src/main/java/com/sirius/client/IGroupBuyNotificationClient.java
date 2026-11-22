package com.sirius.client;

import com.sirius.model.vo.NotifyTaskVO;


public interface IGroupBuyNotificationClient {
    /**
     * 只返回本次发送结果；是否再试由领域服务决定。
     */
    String groupBuyNotify(NotifyTaskVO task) throws Exception;
}
