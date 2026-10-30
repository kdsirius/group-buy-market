package com.sirius.api;


import com.sirius.api.dto.GoodsMarketRequestDTO;
import com.sirius.api.dto.GoodsMarketResponseDTO;
import com.sirius.api.response.Response;

public interface IMarketIndexService {
    Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(GoodsMarketRequestDTO request);
}
