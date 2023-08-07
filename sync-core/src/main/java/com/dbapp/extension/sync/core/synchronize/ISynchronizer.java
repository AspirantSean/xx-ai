package com.dbapp.extension.sync.core.synchronize;

import com.dbapp.extension.sync.enums.SyncType;
import com.dbapp.extension.sync.model.dto.UpdateVersion;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public interface ISynchronizer {
    UpdateVersion fullSynchronization(boolean wait, boolean force);

    Future<UpdateVersion> doFullSynchronization(UpdateVersion currentVersion);

    void incrementalSynchronizationByVersion(UpdateVersion currentVersion);

    List<String> incrementalSynchronizationById(SyncType syncType, List<String> ids);

    List<String> deleteSynchronizeData(List<String> ids);

    List<String> incrementalSynchronizationByObject(List<Map<String, Object>> data);

    boolean refreshIndex();
}
