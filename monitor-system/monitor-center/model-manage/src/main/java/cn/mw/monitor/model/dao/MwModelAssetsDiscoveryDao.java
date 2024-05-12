package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.param.AddAndUpdateModelParam;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.param.MacrosDTO;
import cn.mw.monitor.service.assetsTemplate.dto.MwAssetsTemplateDTO;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import cn.mw.monitor.service.model.param.MwModelTemplateDTO;
import cn.mw.monitor.service.scan.model.ScanResultFail;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import cn.mw.monitor.service.scan.param.QueryScanResultParam;
import cn.mw.monitor.service.webmonitor.model.HttpParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.UpdateWebMonitorStateParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/7/22
 */
public interface MwModelAssetsDiscoveryDao {
    AddAndUpdateModelParam getModelInfoById(Integer assetsSubTypeId);

    List<MwModelTemplateDTO> getTemplateByServerIdAndMonitorMode(AddUpdateTangAssetsParam record);

    //查询对应zabbix服务器所有模板
    List<MwModelTemplateDTO> getByServerIdAllTemplate(AddUpdateTangAssetsParam record);

    List<Integer> getInstanceByModelIndex(@Param("modelIndexs") List modelIndexs);

    List<Map> getAssetsSubTypeByMode();

    List<ScanResultSuccess> selectScanSuccessList(QueryScanResultParam param);

    List<ScanResultFail> selectFailList(Map criteria);

    List<ScanResultSuccess> selectSuccessListByIds(List<Integer> list);

    List<MacrosDTO> selectMacros();

    List<HttpParam> selectHttpIds(List<String> ids);

    void updateUserState(@Param("enable") String enable,@Param("id") Integer id);

    void batchInsertDeviceInfo(List<AddUpdateTangAssetsParam> list);

    String getMonitorServerName(Integer monitorServerId);

    String getPollingEngineName(String pollingEngine);
}
