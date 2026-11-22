package com.sirius.mapper;

import com.sirius.model.entity.CrowdTagsJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Sirius
 * @description 人群标签任务
 * @date 2026年09月19日 20:14
 */
@Mapper
public interface ICrowdTagsJobDao {
    CrowdTagsJob queryCrowdTagsJob(CrowdTagsJob crowdTagsJobReq);
}
