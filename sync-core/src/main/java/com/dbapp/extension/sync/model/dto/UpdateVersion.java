package com.dbapp.extension.sync.model.dto;

import com.dbapp.extension.sync.enums.SyncStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.sql.Timestamp;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateVersion implements Cloneable {

    /**
     * 版本号
     */
    private long version;
    /**
     * 上次版本号
     */
    private long lastVersion;
    /**
     * 是否强制更新版本范围内错所有数据
     */
    private boolean force;
    /**
     * 描述
     */
    private String description;
    /**
     * 同步状态
     */
    private SyncStatus status = SyncStatus.New;
    /**
     * 次数
     */
    private long times = 0;
    /**
     * 本次同步耗时
     */
    private Long took;
    /**
     * 当次同步总条数
     */
    private int total;
    /**
     * 同步时间
     */
    private Timestamp syncTime;
    /**
     * 结束时间
     */
    private Timestamp finishTime;

    @Override
    public UpdateVersion clone() {
        try {
            UpdateVersion clone = (UpdateVersion) super.clone();
            BeanUtils.copyProperties(this, clone);
            return clone;
        } catch (CloneNotSupportedException ignore) {
        }
        return this;
    }
}
