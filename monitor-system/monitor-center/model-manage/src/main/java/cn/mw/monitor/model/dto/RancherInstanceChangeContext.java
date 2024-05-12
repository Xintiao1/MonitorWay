package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.model.dto.rancher.MwModelRancherDataInfoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.mw.monitor.model.param.MatchModelTypeEnum.RANCHER;

public class RancherInstanceChangeContext {
    //es中是否存在Rancher数据
    private boolean hasRancherData = false;
    private List<RancherDataCacheInfo> deleteDatas = new ArrayList<>();
    private List<RancherDataCacheInfo> deleteRelateDatas = new ArrayList<>();

    private List<RancherDataCacheInfo> addDatas = new ArrayList<>();
    private List<RancherDataCacheInfo> addRelatedDatas = new ArrayList<>();

    private List<RancherDataCacheInfo> updateDatas = new ArrayList<>();

    private List<RancherDataCacheInfo> oriData;

    private RancherDataCacheInfo transformCacheData(MwModelRancherDataInfoDTO data, Map<String, RancherDataCacheInfo> map) {
        RancherDataCacheInfo rancherDataCacheInfo = map.get(data.getId());
        if (null == rancherDataCacheInfo) {
            rancherDataCacheInfo = map.get(data.getUuid());
        }
        return rancherDataCacheInfo;
    }

    public void addDelete(MwModelRancherDataInfoDTO delete, Map<String, RancherDataCacheInfo> map) {
        RancherDataCacheInfo data = transformCacheData(delete, map);
        deleteDatas.add(data);
        addRelatedDatas(data, map, deleteRelateDatas);
    }

    public void addAdd(MwModelRancherDataInfoDTO add, Map<String, RancherDataCacheInfo> newMap) {
        RancherDataCacheInfo cacheInfo = new RancherDataCacheInfo();
        cacheInfo.setRancehrDataInfo(add);
        addDatas.add(cacheInfo);
        addRelatedDatas(cacheInfo, newMap, addRelatedDatas);
    }

    private void addRelatedDatas(RancherDataCacheInfo data
            , Map<String, RancherDataCacheInfo> map, List<RancherDataCacheInfo> relateDatas) {
        //加入父节点
        RancherDataCacheInfo parent = map.get(data.getRancehrDataInfo().getPId());
        if (null != parent) {
            relateDatas.add(parent);
        }

        //加入子节点
        for (RancherDataCacheInfo pendingChild : map.values()) {
            MwModelRancherDataInfoDTO rancherDataInfoDTO = pendingChild.getRancehrDataInfo();
            if (!RANCHER.getType().equals(rancherDataInfoDTO.getType())
                    && rancherDataInfoDTO.getPId().equals(data.getRancehrDataInfo().getId())) {
                relateDatas.add(pendingChild);
            }
        }
    }

    public void addUpdate(MwModelRancherDataInfoDTO update, Map<String, RancherDataCacheInfo> map) {
        RancherDataCacheInfo cacheInfo = transformCacheData(update, map);
        updateDatas.add(cacheInfo);
    }

    public boolean isChange() {
        return 0 != deleteDatas.size() || 0 != addDatas.size() || 0 != updateDatas.size();
    }

    public boolean isAdd() {
        return 0 != addDatas.size();
    }

    public boolean isModify() {
        return 0 != updateDatas.size();
    }

    public boolean isDelete() {
        return 0 != deleteDatas.size();
    }

    public List<RancherDataCacheInfo> getDeleteDatas() {
        return deleteDatas;
    }

    public List<RancherDataCacheInfo> getDeleteRelateDatas() {
        return deleteRelateDatas;
    }

    public List<RancherDataCacheInfo> getAddDatas() {
        return addDatas;
    }

    public List<RancherDataCacheInfo> getAddRelatedDatas() {
        return addRelatedDatas;
    }

    public List<RancherDataCacheInfo> getUpdateDatas() {
        return updateDatas;
    }

    public boolean isHasRancherData() {
        return hasRancherData;
    }

    public void setHasRancherData(boolean hasRancherData) {
        this.hasRancherData = hasRancherData;
    }

    public List<RancherDataCacheInfo> getOriData() {
        return oriData;
    }

    public void setOriData(List<RancherDataCacheInfo> oriData) {
        this.oriData = oriData;
    }

    public void mergeChange() {

    }
}
