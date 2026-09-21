package com.sirius.api;

import com.sirius.api.response.Response;

/**
 * @author Sirius
 * @description 动态配置中心
 * @date 2026年09月21日 13:23
 */
public interface IDCCService {

    Response<Boolean> updateConfig(String key, String value);

}
