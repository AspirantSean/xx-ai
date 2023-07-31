package com.dbapp.extension.sync.controller;

import com.dbapp.extension.sync.enums.SyncType;
import com.dbapp.extension.sync.restful.response.ApiResponse;
import com.dbapp.extension.sync.service.ISynchronizeService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.annotation.security.PermitAll;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1.0")
public class SynchronizeController {

    @Resource
    private ISynchronizeService iSynchronizeService;

    @PermitAll
    @PostMapping("/all-data-synchronizations")
    @ResponseBody
    public ApiResponse<?> fullSynchronization(boolean force) {
        return iSynchronizeService.fullSynchronization(force);
    }

    @PermitAll
    @PostMapping("/ids-synchronizations")
    @ResponseBody
    public ApiResponse<?> incrementalSynchronizationById(@RequestParam("syncType") SyncType syncType, @RequestBody List<String> ids) {
        return iSynchronizeService.incrementalSynchronizationById(syncType, ids);
    }

    @PermitAll
    @PostMapping("/data-synchronizations")
    @ResponseBody
    ApiResponse<?> incrementalSynchronizationByObject(@RequestBody List<Map<String, Object>> data) {
        return iSynchronizeService.incrementalSynchronizationByObject(data);
    }
}
