package com.sirius.mapper;

import com.sirius.model.entity.CrowdTagsDetail;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Sirius
 * @description 人群标签明细
 * @date 2026年09月19日 20:13
 */
@Mapper
public interface ICrowdTagsDetailDao {
    void addCrowdTagsUserId(CrowdTagsDetail crowdTagsDetailReq);
}
