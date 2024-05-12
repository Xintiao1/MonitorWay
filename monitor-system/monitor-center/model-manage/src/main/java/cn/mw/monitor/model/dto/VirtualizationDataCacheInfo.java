package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.virtual.dto.VirtualizationDataInfo;

import java.util.Objects;

public class VirtualizationDataCacheInfo {
    private Integer intanceId;
    private String modelIndex;
    private VirtualizationDataInfo virtualizationDataInfo;
    private String key;

    public VirtualizationDataCacheInfo(Integer intanceId ,VirtualizationDataInfo virtualizationDataInfo
    ,String modelIndex){
        this.intanceId = intanceId;
        this.virtualizationDataInfo = virtualizationDataInfo;
        this.modelIndex = modelIndex;
        initKey(virtualizationDataInfo);
    }

    public VirtualizationDataCacheInfo(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualizationDataCacheInfo cacheInfo = (VirtualizationDataCacheInfo) o;
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

    public VirtualizationDataInfo getVirtualizationDataInfo() {
        return virtualizationDataInfo;
    }

    public void setVirtualizationDataInfo(VirtualizationDataInfo virtualizationDataInfo) {
        this.virtualizationDataInfo = virtualizationDataInfo;
    }

    public void initKey(){
        String id = (null == virtualizationDataInfo.getId()?"null":virtualizationDataInfo.getId());
        String uuId = (null == virtualizationDataInfo.getUUID()?"null":virtualizationDataInfo.getUUID());
        this.key = id + uuId;
    }

    private void initKey(VirtualizationDataInfo virtualizationDataInfo){
        String id = (null == virtualizationDataInfo.getId()?"null":virtualizationDataInfo.getId());
        String uuId = (null == virtualizationDataInfo.getUUID()?"null":virtualizationDataInfo.getUUID());
        this.key = id + uuId;
    }

    public String getModelIndex() {
        return modelIndex;
    }

    public void setModelIndex(String modelIndex) {
        this.modelIndex = modelIndex;
    }
}
