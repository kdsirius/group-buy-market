package com.sirius.test.domain.tag;

import com.sirius.domain.tag.service.TagService;
import com.sirius.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBitSet;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Sirius
 * @description 人群标签服务测试
 * @date 2026年09月19日 22:16
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ITagServiceTest {

    @Resource
    private TagService tagService;
    @Resource
    private IRedisService redisService;

    @Test
    public void test_tag_job() {
        tagService.execTagBatchJob("RQ_KJHKL98UU78H66554GFDV", "10001");
    }

    @Test
    public void test_get_tag_bitmap() {
        RBitSet bitSet = redisService.getBitSet("RQ_KJHKL98UU78H66554GFDV");
        // 是否存在
        log.info("xfg01 存在，预期结果为 true，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("xfg01")));
        log.info("xfg02 存在，预期结果为 true，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("xfg02")));
        log.info("xfg03 存在，预期结果为 true，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("xfg03")));
        log.info("xfg04 存在，预期结果为 true，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("xfg04")));
        log.info("xfg05 存在，预期结果为 true，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("xfg05")));
        log.info("xfg06 存在，预期结果为 true，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("xfg06")));
        log.info("xfg07 存在，预期结果为 true，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("xfg07")));
        log.info("xfg08 存在，预期结果为 true，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("xfg08")));
        log.info("xfg09 存在，预期结果为 true，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("xfg09")));
        log.info("gudebai 不存在，预期结果为 false，测试结果:{}", bitSet.get(redisService.getIndexFromUserId("gudebai")));
    }

}

