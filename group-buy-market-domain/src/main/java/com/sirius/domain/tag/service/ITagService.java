package com.sirius.domain.tag.service;

/**
 * @author Sirius
 * @description 人群标签服务接口
 * @date 2026年09月19日 20:40
 */
public interface ITagService {
    /**
     * 执行人群标签批次任务
     *
     * @param tagId   人群ID
     * @param batchId 批次ID
     */
    void execTagBatchJob(String tagId, String batchId);
}
