package com.dbapp.extension.sync.service;

import com.dbapp.extension.sync.enums.SyncType;
import com.dbapp.extension.sync.restful.response.ApiResponse;

import java.util.List;
import java.util.Map;

public interface ISynchronizeService {
    ApiResponse<?> fullSynchronization(boolean force);

    ApiResponse<?> incrementalSynchronizationById(SyncType syncType, List<String> ids);

    ApiResponse<?> incrementalSynchronizationByObject(List<Map<String, Object>> data);
}
