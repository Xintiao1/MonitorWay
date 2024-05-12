package cn.mw.monitor.assetsTemplate.dao;

import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.AddAssetsTemplateParam;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.QueryAssetsTemplateParam;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.TemplateNamesDto;
import cn.mw.monitor.assetsTemplate.model.MwTemplateServerTable;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assetsTemplate.dto.MwAssetsTemplateDTO;
import cn.mw.monitor.service.assetsTemplate.model.MwAssetsTemplateTable;
import cn.mw.monitor.service.model.param.MwModelTemplateDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwAseetstemplateTableDao {
    int deleteBatch(List<Integer> templateIds);

    int insertBatch(List<AddAssetsTemplateParam> record);

    int insert(AddAssetsTemplateParam record);

    MwAssetsTemplateDTO selectById(Integer id);

    int update(AddAssetsTemplateParam record);

    int updateBatch(List<AddAssetsTemplateParam> record);

    List<MwAssetsTemplateDTO> getTemplateByServerIdAndAssetsType(AddUpdateTangAssetsParam record);

    List<MwAssetsTemplateDTO> selectList(Map criteria);

    List<MwAssetsTemplateDTO> selectTepmplateTableList(Map criteria);

    List<MwAssetsTemplateDTO> selectPortList(Map criteria);

    List<MwModelTemplateDTO> selectListByModel(Map criteria);

    List<MwModelTemplateDTO> selectPortListByModel(Map criteria);

    String selectTypeName(Integer id);

    List<TemplateNamesDto> selectTemplateNames();

    void cleanTemplateServerMap();

    void insertBatchTemplateServerMap(List<MwTemplateServerTable> list);

    void deleteBatchTemplateServerMap(List<Integer> templateIds);

    List<MwAssetsTemplateDTO> getTemplateByServerIdAndMonitorMode(AddUpdateTangAssetsParam record);

    /**
     * 新增重复检查
     *
     * @param checkParam 查询条件
     * @return
     */
    List<MwAssetsTemplateTable> check(QueryAssetsTemplateParam checkParam);

    int getMonitorModeId(String name);

    int getAssetsType(@Param("name") String name, @Param("type") Integer type);

    Integer getAssetsSubType(@Param("typeSubName") String typeSubName, @Param("type") Integer type,@Param("typeName") String typeName);

    List<Map<String,String>> fuzzSearchAllFiled(String value);

    //查询对应zabbix服务器所有模板
    List<MwAssetsTemplateDTO> getByServerIdAllTemplate(AddUpdateTangAssetsParam record);

    //查询ICMP资产模板信息
    MwAssetsTemplateDTO selectIcmpTemplate(@Param("serverId") Integer serverId,@Param("isNewVersion") Boolean isNewVersion );

}
