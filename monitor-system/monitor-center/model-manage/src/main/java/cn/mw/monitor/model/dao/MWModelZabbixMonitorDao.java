package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.param.MwModelTPServerParam;
import cn.mw.monitor.service.assets.model.AssetsInterfaceDTO;
import cn.mw.monitor.service.assets.model.ModelInterfaceDTO;
import cn.mw.monitor.service.model.param.MwModelAssetsInterfaceParam;
import cn.mw.monitor.service.model.param.MwModelInterfaceCommonParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2023/5/31
 */
public interface MWModelZabbixMonitorDao {
    //根据条件查询全表数据
    List<AssetsInterfaceDTO> getAllInterfaceByCriteria(Map map);

    List<ModelInterfaceDTO> getAllInterfaceNameAndHostId(@Param("hostIds")List<String> hostIds);

    List<ModelInterfaceDTO> getInterfaceInfoByAssetsId(String assetsId);

   void updateInterfaceDescById(MwModelAssetsInterfaceParam param);

    void batchUpdateInterfaceShow(MwModelAssetsInterfaceParam param);

    void batchUpdateInterfaceHide(MwModelAssetsInterfaceParam param);

    void updateAlertTag(MwModelAssetsInterfaceParam param);

    List<MwModelInterfaceCommonParam> queryInterfaceInfoAlertTag(MwModelInterfaceCommonParam param);

    List<MwModelTPServerParam> queryMonitorServerInfo();

    MwModelTPServerParam queryMonitorServerInfoById(Integer monitorServerId);

    void batchInsert(List<AssetsInterfaceDTO> list);

    void updateInterfaceStatus();
}
