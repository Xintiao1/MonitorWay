package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.model.dto.rancher.MwModelRancherDataInfoDTO;

import java.util.Objects;

public class RancherDataCacheInfo {
    private Integer intanceId;
    private String modelIndex;
    private MwModelRancherDataInfoDTO rancehrDataInfo;
    private String key;

    public RancherDataCacheInfo(Integer intanceId , MwModelRancherDataInfoDTO rancehrDataInfo
    , String modelIndex){
        this.intanceId = intanceId;
        this.rancehrDataInfo = rancehrDataInfo;
        this.modelIndex = modelIndex;
        initKey(rancehrDataInfo);
    }

    public RancherDataCacheInfo(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RancherDataCacheInfo cacheInfo = (RancherDataCacheInfo) o;
        return Objects.equals(key, cacheInfo.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    public Integer getIntanceId() {
        return intanceId;
    }

    public void setIntanceId(Integer intanceId) {
        this.intanceId = intanceId;
    }

    public MwModelRancherDataInfoDTO getRancehrDataInfo() {
        return rancehrDataInfo;
    }

    public void setRancehrDataInfo(MwModelRancherDataInfoDTO rancehrDataInfo) {
        this.rancehrDataInfo = rancehrDataInfo;
    }

    public void initKey(){
        String id = (null == rancehrDataInfo.getId()?"null":rancehrDataInfo.getId());
        String uuId = (null == rancehrDataInfo.getUuid()?"null":rancehrDataInfo.getUuid());
        this.key = id + uuId;
    }

    private void initKey(MwModelRancherDataInfoDTO rancehrDataInfo){
        String id = (null == rancehrDataInfo.getId()?"null":rancehrDataInfo.getId());
        String uuId = (null == rancehrDataInfo.getUuid()?"null":rancehrDataInfo.getUuid());
        this.key = id + uuId;
    }

    public String getModelIndex() {
        return modelIndex;
    }

    public void setModelIndex(String modelIndex) {
        this.modelIndex = modelIndex;
    }
}
