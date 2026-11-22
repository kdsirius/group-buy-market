package com.sirius.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class OKHttpClientConfig {
    @Bean
    public OkHttpClient httpClient() {
        return new OkHttpClient.Builder()
                // 一次尝试不能无限等；失败让待办机制稍后处理。
                .callTimeout(5, TimeUnit.SECONDS)
                // 本地教学只请求明确指定的地址，不跟随重定向到其他服务。
                .followRedirects(false).followSslRedirects(false)
                .build();
    }
}
