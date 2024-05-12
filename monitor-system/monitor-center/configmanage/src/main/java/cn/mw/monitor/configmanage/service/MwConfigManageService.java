package cn.mw.monitor.configmanage.service;


import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.configmanage.entity.*;
import cn.mw.monitor.timetask.entity.MwTimeTaskTable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

public interface MwConfigManageService {
    @Deprecated
    public List<Future<PerformResultEntity>> runScript(MwTimeTaskTable p);

    //修改置管理路径
    Reply updateOrAddPath(MwNcmPath param);

    //查看配置管理路径设置
    Reply getPath();

    //下发
    Reply batchPerform(MwDownloadParam param);

    //下发
    Reply configPerform(QueryTangAssetsParam param);

    //配置对比
    Reply configCompare(List<MwNcmDownloadConfig> param);

    //删除部分配置
    Reply deleteConfigs(List<MwNcmDownloadConfig> param);

    //下载具体配置
    void getDownload(MwNcmDownloadConfig param, HttpServletResponse response);

    //查看具体配置
    Reply selectDownload(MwNcmDownloadConfig param);

    //查看配置
    Reply selectDownloads(MwNcmDownloadConfig param);

    //批量下载配置
    Reply batchDownload(MwDownloadParam param);

    //下载配置
    Reply download(QueryTangAssetsParam param);

    //编辑
    Reply editor(MwConfigMapper param);

    /**
     * 批量更新
     * @param param 配置参数
     * @return
     */
    Reply batchEditor(MwConfigMapper param);

    //编辑前查询
    Reply editorSelect(MwConfigMapper assetsId);


    Reply selectList(QueryTangAssetsParam qParam);


    //查询执行结果
    Reply selectPerforms(MwNcmDownloadConfig qParam);

    Reply showPerform(MwNcmDownloadConfig qParam);

    Reply deletePerforms(List<MwNcmDownloadConfig> qParam);

    void getPerform(MwNcmDownloadConfig qParam, HttpServletResponse response);

    Reply delete(List<MwTangibleassetsDTO> qParam);

    Reply getTreeGroup(String treeName);

    Reply addTreeGroup(MwConfigManageTreeGroup mwConfigManageTreeGroup);

    Reply updateTreeGroup(MwConfigManageTreeGroup mwConfigManageTreeGroup);

    Reply deleteTreeGroup(MwConfigManageTreeGroup mwConfigManageTreeGroup);



    /**
     * 增加检测报告信息
     *
     * @param detectReport 检测报告
     * @return
     */
    Reply addDetectReport(DetectReportDTO detectReport);

    /**
     * 更新检测报告数据
     *
     * @param detectReport 检测报告
     * @return
     */
    Reply updateDetectReport(DetectReportDTO detectReport);

    /**
     * 更新检测报告数据
     *
     * @param detectReport 检测报告
     * @return
     */
    Reply updateDetectReportState(DetectReportDTO detectReport);

    /**
     * 删除检测报告
     *
     * @param detectReport 检测报告
     * @return
     */
    Reply deleteDetectReport(DetectReportDTO detectReport);

    /**
     * 获取报告列表
     *
     * @param detectReport 检测报告
     * @return 报告详情列表
     */
    Reply getReportList(DetectReportDTO detectReport);

    /**
     * 获取报告详情
     *
     * @param detectReport 检测报告
     * @return 报告详情
     */
    Reply getReportDetail(DetectReportDTO detectReport);

    /**
     * 获取报告详情
     *
     * @param detectReport 检测报告
     * @return 报告详情
     */
    Reply getReportDetectDetail(DetectReportDTO detectReport);



    /**
     * 增加策略管理信息
     *
     * @param policyManageDTO 策略管理
     * @return
     */
    Reply addPolicyManage(PolicyManageDTO policyManageDTO);

    /**
     * 更新策略管理数据
     *
     * @param policyManageDTO 策略管理
     * @return
     */
    Reply updatePolicyManage(PolicyManageDTO policyManageDTO);

    /**
     * 删除策略管理
     *
     * @param policyManageDTO 策略管理
     * @return
     */
    Reply deletePolicyManage(PolicyManageDTO policyManageDTO);

    /**
     * 获取策略列表
     *
     * @param policyManageDTO 策略管理
     * @return 策略详情列表
     */
    Reply getPolicyList(PolicyManageDTO policyManageDTO);

    /**
     * 获取策略详情
     *
     * @param policyManageDTO 策略管理
     * @return 策略详情
     */
    Reply getPolicyDetail(PolicyManageDTO policyManageDTO);


    /**
     * 增加规则管理信息
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    Reply addRuleManage(MwConfigManageRuleManage ruleManage);

    /**
     * 更新规则管理数据
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    Reply updateRuleManage(MwConfigManageRuleManage ruleManage);

    /**
     * 删除规则管理
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    Reply deleteRuleManage(MwConfigManageRuleManage ruleManage);

    /**
     * 获取规则管理数据列表
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    Reply getRuleList(MwConfigManageRuleManage ruleManage);

    /**
     * 获取规则详情
     *
     * @param ruleManage 规则管理数据
     * @return 规则详情
     */
    Reply getRuleDetail(MwConfigManageRuleManage ruleManage);

    /**
     * 根据类别获取文件夹及其内部数据
     *
     * @param type 类别
     * @return
     */
    Reply getTreeAndData(String type);

    /**
     * 根据类别获取模糊查询数据
     *
     * @param type 类别
     * @return
     */
    Reply getFuzzList(String type);

    /**
     * 执行合约检测
     *
     * @param reportId 报告ID
     */
    void execAssetsDetect(int reportId);

    /**
     * 根据策略ID获取关联的报告列表和规则列表
     *
     * @param map 数据  类别
     * @return
     */
    Reply getPolicyRelationList(HashMap map);

    /**
     * 下载检测报告
     *
     * @param detectReport 检测报告参数
     * @param response     返回数据
     */
    void downloadDetectReport(DetectReportDTO detectReport, HttpServletResponse response);

    /**
     * 定时任务————下载配置数据
     *
     * @param id 资产ID
     * @return
     */
    TimeTaskRresult downloadConfig(String id);

    /**
     * 定时任务————执行配置脚本
     *
     * @param id      资产ID
     * @param command 执行命令
     * @return
     */
    TimeTaskRresult execConfigScript(String id, String command);

    /**
     * 定时任务————比较当前文件的最近时间两个配置文件差异性
     *
     * @param id 资产ID
     * @return
     */
    TimeTaskRresult compareConfigContent(String id);

    /**
     * 执行合规检测报告
     *
     * @param id 报告ID
     * @return
     */
    TimeTaskRresult execReport(String id);

    /**
     * 获取配置信息变化的资产数据
     * @param param
     * @return
     */
    Reply selectChangedConfigs(MwNcmDownloadConfig param);

    /**
     * 定时清除资产配置数据
     *
     * @param assetsId 资产ID
     */
    void clearConfigSetting(String assetsId);

    /**
     * 批量下载执行文件
     *
     * @param map      参数
     * @param response 返回内容
     */
    void batchDownloadPerform(HashMap map, HttpServletResponse response);

    /**
     * 获取配置管理模糊查询字段
     *
     * @param type 查询类别
     * @return
     */
    Reply fuzzSearchAllFiledData(String type);

    /**
     * 导出excel模板
     *
     * @param response 导出数据
     */
    void excelTemplateExport(HttpServletResponse response);

    /**
     * 用户批量导入
     *
     * @param file     excel文件数据
     * @param response 失败数据返回
     */
    void excelImport(MultipartFile file, HttpServletResponse response);

    /**
     * 用户批量导入
     *
     * @param file     excel文件数据
     * @param response 失败数据返回
     */
    void excelImport(File file, HttpServletResponse response);

    Reply configManagebrowselist(QueryTangAssetsParam qParam);

    Reply createVariable(List<MwScriptVariable> qParam);

    Reply getVariable(List<MwScriptVariable> qParam);

    Reply selectAssets(QueryTangAssetsParam queryTangAssetsParam);

    Reply getVariableById(Integer integer);

    Reply browselistAssets(List<QueryTangAssetsParam> qParam);
}
