package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lumingming
 * @createTime 2023511 17:06
 * @description
 */
@ApiModel("返回函数")

public class ResIpStatusDesc {
    @ApiModelProperty("是否带名称下拉")
    String ipAddress;
    @ApiModelProperty("最新状态描述")
    String nowDes;
    @ApiModelProperty("最新操作")
    String nowStatus;
    @ApiModelProperty("最新操作")
    List<ResIpStatusDesc> resIpOldStatusDescs;
    @ApiModelProperty("对应的修改当时所带属性")
    Map<String,Object>mapList = new HashMap<>();

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getNowDes() {
        return nowDes;
    }

    public void setNowDes(String nowDes) {
        if (nowDes.equals("-1")){
            this.nowDes ="";
        }else {
            this.nowDes = nowDes;
        }
    }

    public String getNowStatus() {
        return nowStatus;
    }

    public void setNowStatus(Integer nowStatus) {
        switch (nowStatus){
            case 1:
                this.nowStatus = "分配";
                break;
            case 2:
                this.nowStatus = "回收";
                break;
            case 3:
                this.nowStatus = "变更";
                break;
            case 4:
                this.nowStatus =  "回溯";
                break;
            default:
                this.nowStatus = "未存在系统内操作";
        }
    }

    public List<ResIpStatusDesc> getResIpOldStatusDescs() {
        return resIpOldStatusDescs;
    }

    public void setResIpOldStatusDescs(List<ResIpStatusDesc> resIpOldStatusDescs) {
        this.resIpOldStatusDescs = resIpOldStatusDescs;
    }

    public Map<String, Object> getMapList() {
        return mapList;
    }

    public void setMapList(Map<String, Object> mapList) {
        this.mapList = mapList;
    }
}
