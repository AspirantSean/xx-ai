package com.dbapp.extension.ai.baas.dto.entity;

import java.sql.Timestamp;

/**
 * @author delin
 * @date 2018/8/2
 */
public class UserStatus {
    private String id;
    private String type;
    private Integer enable;
    private Integer toAlarm;
    private Timestamp updateTime;

    public UserStatus(String id, String type, String statusType, Integer value) {
        this.id = id;
        this.type = type;
        if ("enable".equals(statusType)){
            this.enable = value;
        } else if ("toAlarm".equals(statusType)){
            this.toAlarm = value;
        }
    }

    public UserStatus(String id, String type, Integer enable, Integer toAlarm, Timestamp updateTime) {
        this.id = id;
        this.type = type;
        this.enable = enable;
        this.toAlarm = toAlarm;
        this.updateTime = updateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getEnable() {
        return enable;
    }

    public void setEnable(Integer enable) {
        this.enable = enable;
    }

    public Integer getToAlarm() {
        return toAlarm;
    }

    public void setToAlarm(Integer toAlarm) {
        this.toAlarm = toAlarm;
    }
}
