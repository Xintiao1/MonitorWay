package cn.mw.monitor.assets.dao;


import cn.mw.monitor.assets.api.param.assets.QueryLabelParam;
import cn.mw.monitor.assets.api.param.assets.UpdateMonStateParam;
import cn.mw.monitor.assets.api.param.assets.UpdateSetStateParam;
import cn.mw.monitor.assets.dto.DeviceCountDTO;
import cn.mw.monitor.assets.dto.MwAssetsGroupMapper;
import cn.mw.monitor.assets.dto.MwAssetsOrgMapper;
import cn.mw.monitor.assets.dto.MwAssetsUserMapper;
import cn.mw.monitor.manager.dto.MwAssetsIdsDTO;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.assets.param.*;
import cn.mw.monitor.service.dropdown.param.DropdownDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Created by baochengbin on 2020/3/12.
 */
public interface MwTangibleAssetsTableDao {

    //根据hostid查询资产信息
    MwTangibleassetsDTO selectByHostId(String hostid);

    MwTangibleassetsDTO selectByHostIdandIP(@Param("hostid") String hostid,
                                            @Param("hostip") String hostip);

    List<MwTangibleassetsDTO> selectByHostIdandIPs();

    LabelDTOModel selectBylabelName();

    List<OrgDTO> selectOrg(String id);

    LabelMapperDTO selectBylabelId(LabelIDParam labelIDParam);

    LabelMapperDTO selectDropdownTable(LabelIDParam labelIDParam);
    /**
     * 检查ip是否存在
     */
    List<MwTangibleassetsDTO> checkIpAddress(String checkParam);
    List<IpAssetsCount> checkIpAddresses(List<String> list);

    /**
     * 新增资产
     *
     * @param record 资产信息
     * @return
     */
    int insert(AddUpdateTangAssetsParam record);

    /**
     * 新增设备特征信息
     *
     * @param record 资产信息
     * @return
     */
    int insertDeviceInfo(AddUpdateTangAssetsParam record);

    /**
     * 批量新增资产
     *
     * @param assetsList 资产信息
     * @return
     */
    int insertBatch(List<AddUpdateTangAssetsParam> assetsList);

    /**
     * 根据ID删除资产信息
     *
     * @param idList 资产id
     * @return
     */
    int delete(List<String> idList);

    /**
     * 根据ID删除设备特征信息
     *
     * @param list 资产id
     * @return
     */
    void deleteDeviceInfo(List<String> list);

    /**
     * 修改资产信息
     *
     * @param record
     * @return
     */
    int update(AddUpdateTangAssetsParam record);
    /**
     * 批量修改资产信息
     *
     * @param record
     * @return
     */
    int updateBatch(UpdateTangAssetsParam record);

    int updateBatchPolling(@Param("pollingEngine") String pollingEngine,@Param("monitorServerId") int monitorServerId, @Param("assetsIds") List<String> assetsIds);

    /**
     * 根据ID查询资产信息
     *
     * @param id 资产信息
     * @return
     */
    MwTangibleassetsDTO selectById(String id);

   List<MwTangibleassetsTable> selectTangibleAssetsByIds(List<String> list);

    List<UserMapperDTO> selectAssetsUserInfo();

    List<OrgMapperDTO> selectAssetsOrgInfo();

    List<IdAndNameInfoDTO> getMgDeptInfo();

    List<IdAndNameInfoDTO> getMgAreaInfo();

    List<IdAndNameInfoDTO> getMgDeviceTypeInfo();


    /**
     *私有角色查询资产列表
     * @param criteria 查询条件
     * @return
     */
    List<MwTangibleassetsTable> selectPriList(Map criteria);
    /**
     *public 角色查询资产列表
     * @param criteria 查询条件
     * @return
     */
    List<MwTangibleassetsTable> selectPubList(Map criteria);

    /**
     * 查询所有资产信息及相关信息
     *
     * @param map
     * @return
     */
    List<MwTangibleassetsDTO> selectListWithExtend(Map map);

    /**
     * 查询所有资产信息
     *
     * @return List<MwTangibleassetsTable>
     */
    List<MwTangibleassetsTable> selectTopoAssetsList();
    List<MwTangibleassetsTable> selectTopoAssetsList(Map map);

    List<MwAssetsIdsDTO> getAllAssetsIds();

    List<MwAssetsIdsDTO> getAllMonitorServerIds();
    /**
     * 新增重复检查
     *
     * @param  checkParam 查询条件
     * @return
     */
    List<MwTangibleassetsDTO> check(QueryTangAssetsParam checkParam);

    List<MwTangibleassetsDTO> check1(AddUpdateTangAssetsParam checkParam);

    List<MwTangibleassetsDTO> selectLabelList(QueryTangAssetsParam qParam);

    List<MwTangibleassetsDTO> selectAssestsTableList(QueryTangAssetsParam qParam);

    List<MwAllLabelDTO> selectAllLabel(QueryLabelParam labelParam);

    int createAssetsOrgMapper(List<MwAssetsOrgMapper> mapper);

    int deleteAssetsOrgMapperByAssetsId(String assetsId);

    int createAssetsGroupMapper(List<MwAssetsGroupMapper> mapper);

    int deleteAssetsGroupMapperByAssetsId(String assetsId);

    int createAssetsUserMapper(List<MwAssetsUserMapper> mapper);

    int deleteAssetsUserMapperByAssetsId(String assetsId);

    int createAssetsSnmpv12(MwSnmpv1AssetsDTO snmpv1AssetsDTO);

    int batchDeleteAssetsSnmpv12ByAssetsId(@Param("idList") List<String> idList);

    int createAssetsSnmpv3(MwSnmpAssetsDTO snmpAssetsDTO);

    int batchDeleteAssetsSnmpv3ByAssetsId(@Param("idList")List<String> idList);

    int createAssetsAgent(MwAgentAssetsDTO agentAssetsDTO);

    int batchDeleteAssetsAgentByAssetsId(@Param("idList")List<String> idList);

    int deleteAssetsSnmpv12ByAssetsId(String assetsId);
    int deleteAssetsSnmpv3ByAssetsId(String assetsId);
    int deleteAssetsAgentByAssetsId(String assetsId);
    int deleteAssetsPortByAssetsId(String assetsId);


    int createAssetsPort(MwPortAssetsDTO portAssetsDTO);

    int batchDeleteAssetsPortByAssetsId(@Param("idList")List<String> idList);

   // int createAssetsLabel(List<MwAssetsLabelDTO> assetsLabel);

    int deleteAssetsLabelByAssetsId(String assetsId);

    int updateAssetsState(UpdateAssetsStateParam updateAssetsStateParam);

    int updateAssetsMonState(UpdateMonStateParam updateMonStateParam);

    int updateAssetsSetState(UpdateSetStateParam updateSetStateParam);

    int updateTemplateId(@Param("templateId") String templateId, @Param("id") String id);

    void updateTemplateIdBatch(@Param("list") List<MwAssetsIdsDTO> list);

	List<MwAssetsIdsDTO> selectAllAssetsIds(Boolean deleteFlag);

    int createAssetsIOT(MwIOTAssetsDTO mwIOTAssetsDTO);

    int deleteAssetsIOTByAssetsId(String assetsId);

  //  List<MwAssetsLabelDTO> selectLabelForId(String id);

    int deleteAssetsActionMapper(@Param("assetsIds") List<String> assetsIds);

    MwAssetsIdsDTO selectAssetsByIp(String linkTargetIp);

    List<String> getAssetsNameByIp(String ipAddress);

    List<IpAssetsNameDTO> getAssetsNameByIps(List<String> list);

    List<MwTangibleassetsDTO> selectAssetsByAssetsTypeId( @Param("assetsTypeId") Integer assetsTypeId);

    List<MwTangibleassetsTable> selectBySrecah(@Param("search") String search,@Param("aTrue") String aTrue);

    List<MwTangibleassetsTable> selectAssetsListByTypeIds(@Param("assetsTypeIds")  List<Integer> assetsTypeIds);

    MwTangibleassetsDTO selectByAssetsIdAndServerId(@Param("assetsId") String assetsId,@Param("monitorServerId") int monitorServerId);

    List<Map<String,String>> selectAssetsTermFuzzyQuery();

    List<Map<String,String>> fuzzSeachAllFiled(@Param("value") String value, @Param("assetsIOTFlag") boolean assetsIOTFlag);

    List<DeviceCountDTO> deviceCount(List<String> list);

    /**
     *删除批量修改的资产的文本标签
     * @param labelMap 修改标签的数据
     */
    void deleteAssetsLabel(Map<String,Object> labelMap);

    /**
     * 根据标签ID查询标签编码
     * @param labelId 标签ID
     * @return
     */
    String getLabelCode(Integer labelId);

    /**
     *   查询是否有重读数据
     * @param labelCode  标签Code
     * @param dropId  标签ID
     * @param value  标签值
     * @return
     */
    Integer getDropLabelRepeatData(@Param("labelCode") String labelCode,@Param("dropId") Integer dropId,@Param("dropValue") String value);

    /**
     * 获取配置管理数量
     *
     * @param ids 资产ID列表
     * @return
     */
    int countSettingAssets(@Param(value = "ids") List<String> ids);

    /**
     * 查询删除资产的关联线路
     * @return
     */
    List<String> selectAssetsRelationLink(@Param("hostIds") List<String> hostIds);

    /**
     * 查询所有监控项
     * @return
     */
    List<Map<String,String>> selectAllMonitorItem();

    /**
     * 查询资产类型
     * @param pid
     * @param classify
     * @return
     */
    List<DropdownDTO> selectAssetsTypeList(@Param("pid") Integer pid, @Param("classify") Integer classify);

    String getIfMode(@Param("assetsId") String assetsId,
                     @Param("name") String name);

    /**
     * 查询vxlan设备
     * @return
     */
    List<MwTangibleassetsTable> selectVXLanAssetsList();

    /**
     * 跟据传入的查询字段查询资产数据
     * @param fields
     * @return
     */
    List<Map<String,Object>> getAssetsFieldData(@Param("fields") List<String> fields);


    /**
     * 获取发现资产时需要的端口, 以及snmp等配置信息
     */
    List<MwAgentAssetsDTO> selectAgentList();

    List<MwPortAssetsDTO> selectPortList();

    List<MwSnmpv1AssetsDTO> selectSnmpV1List();

    List<MwSnmpAssetsDTO> selectSnmpV3List();

    void updateAssetsPollingEngine(@Param("pollingEngine") String pollingEngine,@Param("monitorServerId") Integer monitorServerId,@Param("hostId") String hostid);
}
