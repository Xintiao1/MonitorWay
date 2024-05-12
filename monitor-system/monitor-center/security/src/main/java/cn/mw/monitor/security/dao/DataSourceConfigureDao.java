package cn.mw.monitor.security.dao;

import cn.mw.module.security.dto.DataSourceConfigureDTO;
import cn.mw.module.security.dto.FieldModelDTO;
import cn.mw.module.security.dto.TopicFieldDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2021/12/8
 */
public interface DataSourceConfigureDao {
    Integer creatDataSourceInfo(DataSourceConfigureDTO param);

    void creatTopicFieldInfo(TopicFieldDTO field);

    Integer editorDataSourceInfo(DataSourceConfigureDTO param);

    void editorTopicFieldInfo(TopicFieldDTO field);

    void editorDataSourceState(DataSourceConfigureDTO dataSource);

    void editorDataSourceStatus(DataSourceConfigureDTO dataSource);

    List<DataSourceConfigureDTO> getDataSourceInfo(DataSourceConfigureDTO param);

    void deleteDataSourceInfo(@Param("ids") List<Long> ids);

    List<Map> dataSourceDropDownByDataSourceType();

    List<Map> dataSourceDropDownByConnectionType();

    List<Map> dataSourceDropDownByAuthType();

    List<Map<String,Object>> fuzzSearchAllFiled();

    List<TopicFieldDTO> getTopicField();

    List<FieldModelDTO> getTimeModel();
    List<FieldModelDTO> getEventSources();

    List<TopicFieldDTO> getTopicByKafkaId(@Param("kafkaId") String kafkaId);

    void creatAlertLevelInfo(@Param("levleList")List<FieldModelDTO> levleList);
    void deleteAlertLevelInfo(@Param("fieldId")String fieldId);

    List<Map<String,String>> getLevelList(TopicFieldDTO param);
}
