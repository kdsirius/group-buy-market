package com.sirius.domain.trade.adapter.port;

import com.sirius.domain.trade.model.entity.NotifyTaskEntity;


public interface ITradePort {
    /**
     * 只返回本次发送结果；是否再试由领域服务决定。
     */
    String groupBuyNotify(NotifyTaskEntity task) throws Exception;
}
