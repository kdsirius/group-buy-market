package com.sirius.model.dto.request;


import lombok.Data;

@Data
public class GoodsMarketRequest {
    private String userId;
    private String source;
    private String channel;
    private String goodsId;
}
