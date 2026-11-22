package com.sirius.repository;

import com.sirius.model.entity.CrowdTagsJobEntity;
import com.sirius.mapper.ICrowdTagsDao;
import com.sirius.mapper.ICrowdTagsDetailDao;
import com.sirius.mapper.ICrowdTagsJobDao;
import com.sirius.model.entity.CrowdTags;
import com.sirius.model.entity.CrowdTagsDetail;
import com.sirius.model.entity.CrowdTagsJob;
import com.sirius.client.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @author Sirius
 * @description
 * @date 2026年09月19日 20:15
 */
@Repository
public class TagRepository {

    @Resource
    private ICrowdTagsDao crowdTagsDao;

    @Resource
    private ICrowdTagsDetailDao crowdTagsDetailDao;

    @Resource
    private ICrowdTagsJobDao crowdTagsJobDao;

    @Resource
    private IRedisService redisService;

    public CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId) {
        CrowdTagsJob crowdTagsJobReq = new CrowdTagsJob();
        crowdTagsJobReq.setTagId(tagId);
        crowdTagsJobReq.setBatchId(batchId);

        CrowdTagsJob crowdTagsJobRes = crowdTagsJobDao.queryCrowdTagsJob(crowdTagsJobReq);
        if (crowdTagsJobRes == null) return null;


        return CrowdTagsJobEntity.builder()
                .tagType(crowdTagsJobRes.getTagType())
                .tagRule(crowdTagsJobRes.getTagRule())
                .statStartTime(crowdTagsJobRes.getStatStartTime())
                .statEndTime(crowdTagsJobRes.getStatEndTime())
                .build();
    }

    public void addCrowTagsUserId(String tagId, String userId) {
        CrowdTagsDetail crowdTagsDetailReq = new CrowdTagsDetail();
        crowdTagsDetailReq.setTagId(tagId);
        crowdTagsDetailReq.setUserId(userId);

        try {
            crowdTagsDetailDao.addCrowdTagsUserId(crowdTagsDetailReq);

            // 获取BitSet
            RBitSet bitSet = redisService.getBitSet(tagId);
            bitSet.set(redisService.getIndexFromUserId(userId));
        } catch (DuplicateKeyException ignore) {
            // 忽略唯一索引冲突
        }
    }

    public void updateCrowdTagsStatistics(String tagId, int count) {
        CrowdTags crowdTagsReq = new CrowdTags();
        crowdTagsReq.setTagId(tagId);
        crowdTagsReq.setStatistics(count);
        crowdTagsDao.updateCrowdTagsStatistics(crowdTagsReq);
    }

}
