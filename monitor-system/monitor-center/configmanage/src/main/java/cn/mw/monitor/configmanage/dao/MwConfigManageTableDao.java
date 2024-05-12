package cn.mw.monitor.configmanage.dao;


import cn.mw.monitor.accountmanage.entity.QueryAccountManageParam;
import cn.mw.monitor.configmanage.entity.*;
import cn.mw.monitor.templatemanage.entity.QueryTemplateManageParam;
import cn.mw.monitor.timetask.entity.MwTimeTaskDownloadHis;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface MwConfigManageTableDao {
    //保存定时任务历史
    int saveTimeHis(MwTimeTaskDownloadHis mwTimeTaskDownloadHis);

    //查询出定时任务下所属配置
    List<MwTangibleassetsTable> selectTimeConfig(@Param("timeId") String timeId);

    //修改置管理路径
    int updatePath(MwNcmPath param);

    int addPath(MwNcmPath param);

    //查看配置管理路径设置
    MwNcmPath getPath();

    //保存下发的信息
    int saveConfigPerfrom(MwNcmDownloadConfig mwNcmDownloadConfig);

    //删除某些配置
    int deleteDownloads(List<MwNcmDownloadConfig> list);

    //查询下载的配置信息
    List<MwNcmDownloadConfig> selectDownloads(Map priCriteria);

    //查询下载的配置信息
    List<MwNcmDownloadConfig> selectDownloads2(MwTangibleassetsDTO param);

    //保存下载的配置信息
    int saveDownloadConfig(MwNcmDownloadConfig mwNcmDownloadConfig);

    //查询具体的账号信息
    QueryAccountManageParam selectOneAccount(@Param("accountId") Integer accountId);

    //查询出资产对于账号关系表
    MwAccountMapper selectAccountMapper(@Param("assetsId") String assetsId);

    //查询具体的模板信息
    QueryTemplateManageParam selectOneTemplate(@Param("templateId") Integer templateId);

    //查询出资产对于模板关系表
    MwTemplateMapper selectTemplateMapper(@Param("assetsId") String assetsId);

    //保存账号信息
    int saveAccountMapper(MwAccountMapper mwAccountMapper);

    //删除账号信息
    int deleteAccountMapper(@Param("assetsId") String assetsId);

    //保存模板信息
    int saveTemplateMapper(MwTemplateMapper mwTemplateMapper);

    //删除模板信息
    int deleteTemplateMapper(@Param("assetsId") String assetsId);

    //编辑前查询
    MwConfigMapper editorSelect(@Param("assetsId") String assetsId);

    /**
     * 根据资产ID获取账户信息     * @param assetsId 资产ID
     *
     * @return
     */
    MwAccountMapper selectAccount(@Param("assetsId") String assetsId);

    /**
     * 根据资产ID获取模板信息
     *
     * @param assetsId 资产ID
     * @return
     */
    MwTemplateMapper selectTemplate(@Param("assetsId") String assetsId);


    List<MwAllLabelDTO> selectAllLabel(QueryLabelParam labelParam);

    List<MwTangibleassetsDTO> selectLabelList(QueryTangAssetsParam qParam);

    /**
     * 私有角色查询资产列表
     *
     * @param criteria 查询条件
     * @return
     */
    List<MwTangibleassetsTable> selectList(Map criteria);

    List<MwNcmDownloadConfig> selectPerforms(Map priCriteria);

    int deletePerforms(List<MwNcmDownloadConfig> qParam);

    //编辑定时间隔
    void updateTiming(MwConfigMapper param);

    void updateConfig(@Param("assetsId") String assetsId);


    List<MwConfigManageTreeGroup> getTreeGroup(@Param("treeName") String treeName);

    void addTreeGroup(MwConfigManageTreeGroup mwConfigManageTreeGroup);

    void updateTreeGroup(MwConfigManageTreeGroup mwConfigManageTreeGroup);

    void deleteTreeGroup(@Param("id") Integer id);

    List<MwConfigManageRuleManage> getRuleList(Map pubCriteria);

    /**
     * 增加检测报告信息
     *
     * @param detectReport 检测报告
     */
    void addDetectReport(DetectReport detectReport);

    /**
     * 更新检测报告信息
     *
     * @param detectReport 检测报告
     */
    void updateDetectReport(DetectReport detectReport);


    /**
     * 更新检测报告信息
     *
     * @param detectReport 检测报告
     */
    void updateDetectReportState(DetectReport detectReport);

    /**
     * 更新检测报告UUID
     *
     * @param detectReport 检测报告
     */
    void updateDetectReportUUID(DetectReport detectReport);

    /**
     * 批量删除检测报告
     *
     * @param operator 删除人
     * @param ids      检测报告ID列表
     */
    void batchDeleteDetectReport(@Param(value = "operator") String operator,
                                 @Param(value = "list") List<Integer> ids);

    /**
     * 增加对应关系
     *
     * @param id         报表或者策略的主键
     * @param typeIdList 策略或者规则主键集合
     * @param detectType 分类
     */
    void addDetectRelation(@Param(value = "id") int id,
                           @Param(value = "list") List<Integer> typeIdList,
                           @Param(value = "detectType") String detectType);

    /**
     * 删除对应关系
     *
     * @param id         报表或者策略的主键
     * @param detectType 分类
     */
    void deleteDetectRelation(@Param(value = "id") int id,
                              @Param(value = "detectType") String detectType);

    /**
     * 批量删除对应关系
     *
     * @param ids        报表或者策略的主键列表
     * @param detectType 分类
     */
    void batchDeleteDetectRelation(@Param(value = "list") List<Integer> ids,
                                   @Param(value = "detectType") String detectType);

    /**
     * 获取对应关系
     *
     * @param id         报表或者策略的主键
     * @param detectType 分类
     */
    List<Integer> listDetectRelation(@Param(value = "id") int id,
                                     @Param(value = "detectType") String detectType);

    /**
     * 获取报告数据
     *
     * @param detectReport 检测报告
     * @return 报告数据列表
     */
    List<DetectReport> listDetectReport(DetectReportDTO detectReport);

    /**
     * 根据主键ID获取检测报告
     *
     * @param id 主键ID
     * @return 检测报告
     */
    DetectReport getDetectReportById(@Param(value = "id") int id);


    /**
     * 增加策略管理信息
     *
     * @param policyManage 策略管理
     */
    void addPolicyManage(PolicyManage policyManage);

    /**
     * 更新策略管理信息
     *
     * @param policyManage 策略管理
     */
    void updatePolicyManage(PolicyManage policyManage);

    /**
     * @param operator
     * @param ids
     */
    void batchDeletePolicyManage(@Param(value = "operator") String operator,
                                 @Param(value = "list") List<Integer> ids);

    /**
     * 获取报告数据
     *
     * @param policyManageDTO 策略管理
     * @return 报告数据列表
     */
    List<PolicyManage> listPolicyManage(PolicyManageDTO policyManageDTO);

    /**
     * 根据主键ID获取策略管理
     *
     * @param id 主键ID
     * @return 策略管理
     */
    PolicyManage getPolicyManageById(@Param(value = "id") int id);

    /**
     * 添加策略和资产的对应关系
     *
     * @param policyId     策略ID
     * @param assetsIdList 资产ID列表
     */
    void addPolicyAssetsRelation(@Param(value = "id") int policyId,
                                 @Param(value = "list") List<String> assetsIdList);

    /**
     * 删除策略和资产的对应关系
     *
     * @param id 策略ID
     */
    void deletePolicyAssetsRelation(@Param(value = "id") int id);

    /**
     * 批量删除策略和资产的对应关系
     *
     * @param ids 策略ID列表
     */
    void batchDeletePolicyAssetsRelation(@Param(value = "list") List<Integer> ids);

    /**
     * 获取策略和资产的对应关系
     *
     * @param id 策略ID
     */
    List<String> listPolicyAssetsRelation(@Param(value = "id") int id);

    /**
     * 获取策略和资产的对应关系
     *
     * @param vendorId  厂商ID
     * @param condition 判断条件（0：等于  1：不等于）
     * @return
     */
    List<String> listPolicyAssetsByVendor(@Param(value = "vendorId") int vendorId,
                                          @Param(value = "condition") int condition);


    /**
     * 获取资产数据
     *
     * @param assetsIdList 资产ID列表
     */
    List<HashMap<String, String>> listAssets(@Param(value = "list") List<String> assetsIdList);

    /**
     * 增加规则管理数据信息
     *
     * @param ruleManage 规则管理数据
     */
    void addRuleManage(RuleManage ruleManage);

    /**
     * 更新规则管理数据信息
     *
     * @param ruleManage 规则管理数据
     */
    void updateRuleManage(RuleManage ruleManage);

    /**
     * 批量删除
     *
     * @param operator 操作人姓名
     * @param ids      id列表
     */
    void batchDeleteRuleManage(@Param(value = "operator") String operator,
                               @Param(value = "list") List<Integer> ids);

    /**
     * 插入规则高级匹配数据
     *
     * @param param 规则匹配数据
     * @return
     */
    Integer insertMwAlertRuleSelect(List<MwRuleSelectParam> param);

    /**
     * 删除规则高级匹配数据
     *
     * @param uuid 主键ID
     * @return
     */
    int deleteMwAlertRuleSelect(String uuid);

    /**
     * 批量删除规则高级匹配数据
     *
     * @param uuidList 列表数据
     */
    void batchDeleteMwAlertRuleSelect(@Param(value = "list") List<String> uuidList);

    /**
     * 根据规则主键ID获取规则匹配数据
     *
     * @param uuid 规则ID
     * @return
     */
    List<MwRuleSelectParam> selectMwAlertRuleSelect(String uuid);

    /**
     * 根据主键ID获取规则数据
     *
     * @param id 规则ID
     * @return 规则数据
     */
    RuleManage getRuleManageById(@Param(value = "id") int id);

    /**
     * 获取报告的模糊查询数据
     *
     * @return
     */
    List<Map<String, String>> fuzzSearchReportData(@Param(value = "systemUser") boolean systemUser,
                                                   @Param(value = "listSet") String listSet);

    /**
     * 获取策略的模糊查询数据
     *
     * @return
     */
    List<Map<String, String>> fuzzSearchPolicyData(@Param(value = "systemUser") boolean systemUser,
                                                   @Param(value = "listSet") String listSet);

    /**
     * 获取规则的模糊查询数据
     *
     * @return
     */
    List<Map<String, String>> fuzzSearchRuleData(@Param(value = "systemUser") boolean systemUser,
                                                 @Param(value = "listSet") String listSet);

    /**
     * 根据策略ID获取关联的报告列表
     *
     * @param policyId   策略ID
     * @param systemUser 是否为系统管理员
     * @param listSet    ID集合
     * @return
     */
    List<Map<String, String>> getReportRelationList(@Param(value = "id") int policyId,
                                                    @Param(value = "systemUser") boolean systemUser,
                                                    @Param(value = "listSet") String listSet);

    /**
     * 根据策略ID获取关联的规则列表
     *
     * @param policyId   策略ID
     * @param systemUser 是否为系统管理员
     * @param listSet    ID集合
     * @return
     */
    List<Map<String, String>> getRuleRelationList(@Param(value = "id") int policyId,
                                                  @Param(value = "systemUser") boolean systemUser,
                                                  @Param(value = "listSet") String listSet);

    /**
     * 根据ID获取规则数据
     *
     * @param list 列表ID
     * @return
     */
    List<RuleManage> listRuleManage(@Param(value = "list") List<Integer> list);

    /**
     * 增加执行检测日志记录
     *
     * @param execLog 检测记录
     */
    void insertDetectExecLog(DetectExecLog execLog);

    /**
     * 根据资产ID获取对应的配置数据
     *
     * @param assetsId   资产ID
     * @param configType 类别
     * @return
     */
    String getLastFileName(@Param(value = "assetsId") String assetsId,
                           @Param(value = "configType") String configType);

    /**
     * 更新执行结果
     *
     * @param logId       记录ID
     * @param execStatus  执行状态（2：处理结束  3：处理失败）
     * @param matchStatus 匹配结果（0：匹配失败，1：匹配成功）
     */
    void updateExecLogStatus(@Param(value = "logId") int logId,
                             @Param(value = "execStatus") int execStatus,
                             @Param(value = "matchStatus") int matchStatus);

    /**
     * 获取总数
     *
     * @param reportUUID UUID
     * @return
     */
    int countReportExec(@Param(value = "reportUUID") String reportUUID);

    /**
     * 获取已完成的数量
     *
     * @param reportUUID UUID
     * @return
     */
    int countFinishedReportExec(@Param(value = "reportUUID") String reportUUID);

    /**
     * 获取执行记录
     *
     * @param reportUUID UUID
     * @return
     */
    List<DetectExecLog> listLogByUUID(@Param(value = "reportUUID") String reportUUID);

    /**
     * 根据报告UUID获取所有未删除的资产数据信息
     *
     * @param reportUUID UUID
     * @return
     */
    List<Map<String, Object>> listAssetsByReportUUID(@Param(value = "reportUUID") String reportUUID);

    /**
     * 获取未处理数量
     *
     * @param reportUUID UUID
     * @return
     */
    int countUnHandle(@Param(value = "reportUUID") String reportUUID);

    /**
     * 获取规则检测出的问题数量
     *
     * @param reportUUID UUID
     * @param ruleId     规则ID
     * @return
     */
    int countRuleDetect(@Param(value = "reportUUID") String reportUUID,
                        @Param(value = "ruleId") int ruleId);

    /**
     * 获取策略检测出的问题数量
     *
     * @param reportUUID UUID
     * @param policyId   策略ID
     * @return
     */
    int countPolicyDetect(@Param(value = "reportUUID") String reportUUID,
                          @Param(value = "policyId") int policyId);

    /**
     * 获取当前报告检测出的问题总数
     *
     * @param reportUUID UUID
     * @param level      检测等级
     * @return
     */
    int countReportDetect(@Param(value = "reportUUID") String reportUUID,
                          @Param(value = "level") int level);

    /**
     * 根据主键ID获取资产数据
     *
     * @param id 资产的ID
     * @return 资产数据
     */
    MwTangibleassetsTable getAssetsById(@Param(value = "id") String id);

    /**
     * 插入配置变更记录
     *
     * @param id          资产ID
     * @param oldFilePath 旧文件地址
     * @param newFilePath 新文件地址
     */
    void insertConfigChange(@Param(value = "id") String id,
                            @Param(value = "oldFilePath") String oldFilePath,
                            @Param(value = "newFilePath") String newFilePath);

    /**
     * 根据ID获取资产变更数据
     *
     * @param id 资产ID
     * @return
     */
    List<Map> selectChangedConfigs(@Param(value = "id") String id);

    /**
     * 根据时间戳获取下载信息
     *
     * @param createTimeStart 开始时间
     * @param createTimeEnd   结束时间
     * @return
     */
    List<MwNcmDownloadConfig> selectDownloadInfos(@Param(value = "createTimeStart") Date createTimeStart,
                                                  @Param(value = "createTimeEnd") Date createTimeEnd);

    /**
     * 获取资产的模糊查询数据
     *
     * @return
     */
    List<Map<String, String>> fuzzSearchAssetsData();

    /**
     * 获取模板管理的模糊查询数据
     *
     * @return
     */
    List<Map<String, String>> fuzzSearchTemplateData();

    /**
     * 获取账户管理的模糊查询数据
     *
     * @return
     */
    List<Map<String, String>> fuzzSearchAccountData();

    /**
     * 获取所有配置资产数据（包含关联的账户和模板信息）
     *
     * @return
     */
    List<ExportAssetsParam> listAssetsExport();

    List<MwTangibleassetsTable>  selectOutAccount(@Param("accountId") String accountId,@Param("sreach") String sreach);

    Integer insertVariable(@Param("mwScriptVariable") MwScriptVariable mwScriptVariable);

    List<MwScriptVariable> getVariable(@Param("ids") List<Integer> ids);

    List<MwScriptVariable> getVariableById(@Param("integer")  Integer integer);


    Map<String, String> getAsstetByid(@Param("accountId") String assetsId);

    List<MwTangibleassetsTable> selectOutAssets(@Param("ids") List<String> ids);


    List<MwTangibleassetsTable> selectOutAccountList(@Param("s")String s,@Param("p") String p);
}
