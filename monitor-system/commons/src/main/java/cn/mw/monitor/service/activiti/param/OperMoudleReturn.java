package cn.mw.monitor.service.activiti.param;

/**
 * @author lumingming
 * @createTime 29 15:30
 * @description
 */
public enum OperMoudleReturn {
    UNBAND("未绑定流程",0) //未绑定流程
    ,submitMore("流程提交次数过多",2)
    ,BAND_PROCESS_MORE("绑定流程过多 请检查流程设置",1)    //绑定流程过多 请检查流程设置
    ,BAND_PROCESS_UNUSER("绑定此流程，但用户无权提交审批",2) //绑定此流程，但用户无权提交审批
    ,BAND_PROCESS_SUCCESS("绑定流程并提交审批",3); //绑定流程并提交审批


    private int type;
    private String reason;

    OperMoudleReturn(String reason,int type){
        this.reason = reason;
        this.type = type;
    }


    public String getReason() {
        return reason;
    }

    public int getType() {
        return type;
    }
}
