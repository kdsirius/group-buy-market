package com.sirius.infrastructure.gateway;

import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

/** 本地成团通知发送器；只允许两个已知接收口，拒绝任意回调URL。 */
@Service
public class GroupBuyNotifyService {
    @Resource
    private OkHttpClient okHttpClient;

    public String groupBuyNotify(String apiUrl, String parameterJson) throws IOException {
        if (!"http://127.0.0.1:8091/api/v1/test/group_buy_notify".equals(apiUrl)
                && !"http://127.0.0.1:8080/api/v1/alipay/group_buy_notify".equals(apiUrl)) {
            return "error";
        }
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(parameterJson, MediaType.get("application/json; charset=utf-8")))
                .build();
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return "error";
            return "success".equals(response.body().string()) ? "success" : "error";
        }
    }
}
