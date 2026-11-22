package com.sirius.service.tag.impl;

import com.sirius.repository.TagRepository;
import com.sirius.model.vo.CrowdTagsJobEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;

/**
 * @author Sirius
 * @description 人群标签服务
 * @date 2026年09月19日 20:40
 */
@Slf4j
@Service
public class TagServiceImpl implements com.sirius.service.tag.ITagService {
    @Resource
    private TagRepository repository;

    public void execTagBatchJob(String tagId, String batchId) {
        log.info("人群标签批次任务 tagId:{} batchId:{}", tagId, batchId);

        //1. 查询批次任务
        CrowdTagsJobEntity crowdTagsJobEntity = repository.queryCrowdTagsJobEntity(tagId, batchId);

        //2. 采集用户数据 - 这部分需要采集用户的消费类数据，后续有用户发起拼单后再处理。

        //3. 数据写入记录
        ArrayList<String> userIdList = new ArrayList<String>() {{
            add("xiaofuge");
            add("liergou");
            add("xfg01");
            add("xfg02");
            add("xfg03");
            add("xfg04");
            add("xfg05");
            add("xfg06");
            add("xfg07");
            add("xfg08");
            add("xfg09");
        }};

        //4. 一般人群标签的处理在公司中，会有专门的数据数仓团队通过脚本方式写入到数据库，就不用这样一个个或者批次来写。
        for (String userId : userIdList) {
            repository.addCrowTagsUserId(tagId, userId);
        }

        //5. 更新人群标签统计量
        repository.updateCrowdTagsStatistics(tagId, userIdList.size());
    }
}
