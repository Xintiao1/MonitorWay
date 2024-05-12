package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.virtual.dto.VirtualizationDataInfo;
import cn.mw.monitor.service.virtual.dto.VirtualizationType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VirtualInstanceChangeContext {
    //es中是否存在虚拟化数据
    private boolean hasVirtualData = false;
    private List<VirtualizationDataCacheInfo> deleteDatas = new ArrayList<>();
    private List<VirtualizationDataCacheInfo> deleteRelateDatas = new ArrayList<>();

    private List<VirtualizationDataCacheInfo> addDatas = new ArrayList<>();
    private List<VirtualizationDataCacheInfo> addRelatedDatas = new ArrayList<>();

    private List<VirtualizationDataCacheInfo> updateDatas = new ArrayList<>();

    private List<VirtualizationDataCacheInfo> oriData;

    private VirtualizationDataCacheInfo transformCacheData(VirtualizationDataInfo data, Map<String ,VirtualizationDataCacheInfo> map){
        VirtualizationDataCacheInfo virtualizationDataCacheInfo = map.get(data.getId());
        if(null == virtualizationDataCacheInfo){
            virtualizationDataCacheInfo = map.get(data.getUUID());
        }
        return virtualizationDataCacheInfo;
    }

    public void addDelete(VirtualizationDataInfo delete ,Map<String ,VirtualizationDataCacheInfo> map){
        VirtualizationDataCacheInfo data = transformCacheData(delete ,map);
        deleteDatas.add(data);
        addRelatedDatas(data ,map ,deleteRelateDatas);
    }

    public void addAdd(VirtualizationDataInfo add ,Map<String ,VirtualizationDataCacheInfo> newMap){
        VirtualizationDataCacheInfo cacheInfo = new VirtualizationDataCacheInfo();
        cacheInfo.setVirtualizationDataInfo(add);
        addDatas.add(cacheInfo);
        addRelatedDatas(cacheInfo ,newMap ,addRelatedDatas);
    }

    private void addRelatedDatas(VirtualizationDataCacheInfo data
            ,Map<String ,VirtualizationDataCacheInfo> map ,List<VirtualizationDataCacheInfo> relateDatas){
        //加入父节点
        VirtualizationDataCacheInfo parent = map.get(data.getVirtualizationDataInfo().getPId());
        if(null != parent){
            relateDatas.add(parent);
        }

        //加入子节点
        for(VirtualizationDataCacheInfo pendingChild : map.values()){
            VirtualizationDataInfo virtualizationDataInfo = pendingChild.getVirtualizationDataInfo();
            if(!VirtualizationType.VCENTER.getType().equals(virtualizationDataInfo.getType())
            && virtualizationDataInfo.getPId().equals(data.getVirtualizationDataInfo().getId())){
                relateDatas.add(pendingChild);
            }
        }
    }

    public void addUpdate(VirtualizationDataInfo update ,Map<String ,VirtualizationDataCacheInfo> map){
        VirtualizationDataCacheInfo cacheInfo = transformCacheData(update ,map);
        updateDatas.add(cacheInfo);
    }

    public boolean isChange(){
        return 0 != deleteDatas.size() || 0 != addDatas.size() || 0 != updateDatas.size();
    }

    public boolean isAdd(){
        return 0 != addDatas.size();
    }

    public boolean isModify(){
        return 0 != updateDatas.size();
    }

    public boolean isDelete(){
        return 0 != deleteDatas.size();
    }

    public List<VirtualizationDataCacheInfo> getDeleteDatas() {
        return deleteDatas;
    }

    public List<VirtualizationDataCacheInfo> getDeleteRelateDatas() {
        return deleteRelateDatas;
    }

    public List<VirtualizationDataCacheInfo> getAddDatas() {
        return addDatas;
    }

    public List<VirtualizationDataCacheInfo> getAddRelatedDatas() {
        return addRelatedDatas;
    }

    public List<VirtualizationDataCacheInfo> getUpdateDatas() {
        return updateDatas;
    }

    public boolean isHasVirtualData() {
        return hasVirtualData;
    }

    public void setHasVirtualData(boolean hasVirtualData) {
        this.hasVirtualData = hasVirtualData;
    }

    public List<VirtualizationDataCacheInfo> getOriData() {
        return oriData;
    }

    public void setOriData(List<VirtualizationDataCacheInfo> oriData) {
        this.oriData = oriData;
    }

    public void mergeChange(){

    }
}
