package com.sirius.controller;

import com.sirius.model.response.Response;
import com.sirius.common.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Sirius
 * @description 动态配置管理
 * @date 2026年09月21日 13:24
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/api/v1/gbm/dcc")
public class DCCController {

    @Resource
    private RTopic dccTopic;

    @RequestMapping(value = "update_config", method = RequestMethod.GET)
    public Response<Boolean> updateConfig(@RequestParam String key, @RequestParam String value) {
        try {

            dccTopic.publish(key + "," + value);

            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
