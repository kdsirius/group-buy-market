package com.sirius.infrastructure.gateway;

import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class GroupBuyNotifyService {
    @Resource
    private OkHttpClient okHttpClient;

    public String groupBuyNotify(String apiUrl, String parameterJson) throws IOException {
        // 本地实验只允许这一个接收端，不能拿来对任意 URL 发请求。
        if (!"http://127.0.0.1:8091/api/v1/test/group_buy_notify".equals(apiUrl)) {
            return "error";
        }

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(RequestBody.create(parameterJson, MediaType.get("application/json; charset=utf-8")))
                .build();

        // response 持有连接资源，离开代码块即关闭，避免反复补偿造成泄漏。
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return "error";
            return "success".equals(response.body().string()) ? "success" : "error";
        }
    }
}
