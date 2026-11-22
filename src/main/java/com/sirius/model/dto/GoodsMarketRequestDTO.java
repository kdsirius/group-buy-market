package com.sirius.model.dto;


import lombok.Data;

@Data
public class GoodsMarketRequestDTO {
    private String userId;
    private String source;
    private String channel;
    private String goodsId;
}