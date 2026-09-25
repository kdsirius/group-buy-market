package com.sirius.infrastructure.dao;

import com.sirius.infrastructure.dao.po.NotifyTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Sirius
 * @description 回调任务
 * @date 2026年09月25日 15:16
 */
@Mapper
public interface INotifyTaskDao {

    void insert(NotifyTask notifyTask);

}
