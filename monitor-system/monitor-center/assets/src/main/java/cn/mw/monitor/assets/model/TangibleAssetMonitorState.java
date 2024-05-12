package cn.mw.monitor.assets.model;

/*****
 * 资产监控状态
 */
public enum  TangibleAssetMonitorState {
    TRUE("ACTIVE",true, 0)
    ,FALSE("DISACTIVE",false, 1);

    private String name;
    private boolean isActive;
    private Integer zabbixStatus;

    public Integer getZabbixStatus(){
        return this.zabbixStatus;
    }

    public boolean isActive(){
        return this.isActive;
    }

    TangibleAssetMonitorState(String name, boolean isActive, Integer zabbixStatus){
        this.name = name;
        this.isActive = isActive;
        this.zabbixStatus = zabbixStatus;
    }
}
