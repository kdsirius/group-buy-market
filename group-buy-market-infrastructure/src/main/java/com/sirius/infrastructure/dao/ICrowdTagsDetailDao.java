package com.sirius.infrastructure.dao;

import com.sirius.infrastructure.dao.po.CrowdTagsDetail;
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
