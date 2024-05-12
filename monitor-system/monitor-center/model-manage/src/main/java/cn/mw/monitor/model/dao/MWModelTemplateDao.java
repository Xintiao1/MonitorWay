package cn.mw.monitor.model.dao;

import cn.mw.monitor.service.model.dto.MwModelAssetsGroupTable;
import cn.mw.monitor.service.model.param.MwModelTemplateDTO;
import cn.mw.monitor.model.dto.MwModelTemplateNamesDto;
import cn.mw.monitor.service.model.param.MwModelTemplateTable;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.service.model.param.MwModelZabbixTemplateParam;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/5/05
 */
public interface MWModelTemplateDao {
    int insert(AddAndUpdateModelTemplateParam record);

    List<MwModelTemplateTable> check(QueryModelTemplateParam checkParam);

    MwModelTPServerTable selectTPServerById(int serverId);

    void insertBatchTemplateServerMap(List<MwModelZabbixTemplateParam> list);

    int deleteBatch(List<Integer> templateIds);

    List<Map<String,String>> fuzzSearchAllFiled(String value);

    void deleteBatchTemplateServerMap(List<Integer> templateIds);

    int update(AddAndUpdateModelTemplateParam record);

    List<MwModelTemplateDTO> selectTepmplateTableList(Map criteria);

    List<MwModelTemplateInfo> getAssetsTemplateId();

    List<MwModelTemplateDTO> selectList(Map criteria);

    List<MwModelTemplateDTO> selectPortList(Map criteria);

    MwModelTemplateDTO selectTemplateById(Integer id);

    List<MwModelTemplateDTO> selectListByModel(Map criteria);

    void cleanGroupServerMap();

    void cleanTemplateServerMap();

    void updateTemplateStatus(List<UpdateModelTemplateStatusParam> list);

    List<MwModelTemplateNamesDto> selectTemplateNames();

    List<ModelGroupAsSubDeviceType> selectModelGroupNames();

    Integer insertBatchGroupServerMap(List<MwModelAssetsGroupTable> list);

    Integer getMonitorModeId(String name);

    Integer getAssetsType(String name);

    Integer getAssetsSubType(String typeSubName);

}
