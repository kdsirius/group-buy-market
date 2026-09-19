package com.sirius.domain.tag.adapter.repository;

import com.sirius.domain.tag.model.entity.CrowdTagsJobEntity;

/**
 * @author Sirius
 * @description 人群标签仓储接口
 * @date 2026年09月19日 20:39
 */
public interface ITagRepository {
    CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId);

    void addCrowTagsUserId(String tagId, String userId);

    void updateCrowdTagsStatistics(String tagId, int count);
}
