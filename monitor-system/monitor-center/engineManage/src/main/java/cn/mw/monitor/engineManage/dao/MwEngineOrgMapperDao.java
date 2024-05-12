package cn.mw.monitor.engineManage.dao;

import cn.mw.monitor.engineManage.model.MwEngineOrgModel;

import java.util.List;

public interface MwEngineOrgMapperDao {
    int delete(List<Integer> engineIds);

    int insert(List<MwEngineOrgModel> models);

}
