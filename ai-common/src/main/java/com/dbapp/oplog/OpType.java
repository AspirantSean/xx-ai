package com.dbapp.oplog;

public interface OpType {
    String LOGIN = "登录";
    String LOGOUT = "登出";
    String FACTORY_RESET = "恢复出厂设置";
    String NEW = "新增";
    String DELETE = "删除";
    String MODIFY = "修改";
    String SAVE = "保存";
    String QUERY = "查询";
    String IMPORT = "导入";
    String EXPORT = "导出";
    String UPDATE = "升级";
    String OPEN = "开启";
    String CLOSE = "关闭";
    String ONOROFF= "打开/关闭";
    String SYNCHRONIZE = "同步";
    String ONLINEUPDATE = "在线更新";
    String OFFLINEUPDATE = "离线更新";
    String TEST = "测试";
    String RESETPASSWORD = "重置密码";
//    String ADDDATA = "添加数据";
    String OPENALARM = "开启告警";
    String CLOSEALARM = "关闭告警";
    String DEPLOY = "部署";
    String CONNECTEST="连接测试";
}
