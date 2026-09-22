package com.sirius.api;

import com.sirius.api.dto.LockMarketPayOrderRequestDTO;
import com.sirius.api.dto.LockMarketPayOrderResponseDTO;
import com.sirius.api.response.Response;

/**
 * @author Sirius
 * @description 营销交易服务接口
 * @date 2026年09月22日 16:15
 */
public interface IMarketTradeService {

    Response<LockMarketPayOrderResponseDTO> lockMarketPayOrder(LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO);

}
