package com.sirius.infrastructure.dao;

import com.sirius.infrastructure.dao.po.CrowdTags;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Sirius
 * @description 人群标签
 * @date 2026年09月19日 20:13
 */
@Mapper
public interface ICrowdTagsDao {
    void updateCrowdTagsStatistics(CrowdTags crowdTagsReq);
}
