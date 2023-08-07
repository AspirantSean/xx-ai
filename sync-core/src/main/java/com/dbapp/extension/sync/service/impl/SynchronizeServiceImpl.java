package com.dbapp.extension.sync.service.impl;

import com.dbapp.extension.sync.core.synchronize.ISynchronizer;
import com.dbapp.extension.sync.enums.SyncType;
import com.dbapp.extension.sync.model.dto.UpdateVersion;
import com.dbapp.extension.sync.restful.entity.ErrorCode;
import com.dbapp.extension.sync.restful.response.ApiResponse;
import com.dbapp.extension.sync.restful.response.DataResponse;
import com.dbapp.extension.sync.service.ISynchronizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SynchronizeServiceImpl implements ISynchronizeService {

    @Resource
    private ISynchronizer iSynchronizer;


    @Override
    public ApiResponse<?> fullSynchronization(boolean force) {
        try {
            DataResponse<UpdateVersion> response = ApiResponse.success(iSynchronizer.fullSynchronization(false, force))
                    .build();
            iSynchronizer.refreshIndex();
            return response;
        } catch (Exception e) {
            log.error("数据同步异常", e);
            return ErrorCode.InternalErrors
                    .message("数据同步异常")
                    .build();
        }
    }

    /**
     * 根据id列表增量同步到elasticsearch
     *
     * @param ids
     * @return
     */
    @Override
    public ApiResponse<?> incrementalSynchronizationById(SyncType syncType, List<String> ids) {
        try {
            log.info("synchronizing:: time: {}, request for {} data", new Date(), syncType);
            DataResponse<List<String>> response = ApiResponse.success(iSynchronizer.incrementalSynchronizationById(syncType, ids))
                    .prop("message", "同步结束")
                    .prop("info", "结果集为失败的id集合")
                    .build();
            iSynchronizer.refreshIndex();
            return response;
        } catch (Exception e) {
            log.error("数据同步异常", e);
            return ErrorCode.InternalErrors
                    .message("数据同步异常")
                    .build();
        }
    }

    /**
     * 增量同步实体对象到elasticsearch
     *
     * @param data
     * @return
     */
    @Override
    public ApiResponse<?> incrementalSynchronizationByObject(List<Map<String, Object>> data) {
        try {
            DataResponse<List<String>> response = ApiResponse.success(iSynchronizer.incrementalSynchronizationByObject(data))
                    .prop("message", "同步结束")
                    .prop("info", "结果集为失败的id集合")
                    .build();
            iSynchronizer.refreshIndex();
            return response;
        } catch (Exception e) {
            log.error("数据同步异常", e);
            return ErrorCode.InternalErrors
                    .message("数据同步异常")
                    .build();
        }
    }

}
