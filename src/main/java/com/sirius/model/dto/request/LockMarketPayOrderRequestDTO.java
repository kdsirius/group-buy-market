package com.sirius.model.dto.request;

import lombok.Data;

/**
 * 营销支付锁单请求对象。
 */
@Data
public class LockMarketPayOrderRequestDTO {

    // 用户ID
    private String userId;
    // 拼单组队ID - 可为空，为空则创建新组队ID
    private String teamId;
    // 活动ID
    private Long activityId;
    // 商品ID
    private String goodsId;
    // 渠道
    private String source;
    // 来源
    private String channel;
    // 外部交易单号
    private String outTradeNo;
    // 开团时约定的通知地址
    private NotifyConfigVO notifyConfigVO;

    /** 兼容旧 HTTP 调用：收到 notifyUrl 时自动构造 HTTP 配置。 */
    public void setNotifyUrl(String url){
        NotifyConfigVO config = new NotifyConfigVO();
        config.setNotifyType("HTTP");
        config.setNotifyUrl(url);
        this.notifyConfigVO = config;
    }
    /** MQ 不接受外部路由键，统一使用服务端 topic.team_success。 */
    public void setNotifyMQ(){
        NotifyConfigVO config = new NotifyConfigVO();
        config.setNotifyType("MQ");
        this.notifyConfigVO = config;
    }
    @Data
    public static class NotifyConfigVO {
        /** HTTP 或 MQ。 */
        private String notifyType;
        /** 仅 HTTP 使用。 */
        private String notifyUrl;
    }
}
