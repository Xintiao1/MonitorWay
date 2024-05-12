package cn.mw.monitor.netflow.service;

import cn.mw.monitor.agent.param.NetFlowConfigParam;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.netflow.entity.NetflowDetailCacheInfo;
import cn.mw.monitor.netflow.param.*;
import cn.mwpaas.common.model.Reply;

import java.util.List;
import java.util.Map;

public interface MWNetflowService {
    static final String NETFLOW_SAVE_DAYS = "save_days";

    //添加,删除监控接口
    Reply doInterfaces(NetflowParam netflowParam , OperationType type) throws Exception;

    //启用,停止监控接口
    Reply performInterfaces(List<AssetParam> paramList , OperationType type) throws Exception;

    //数据清理
    TimeTaskRresult cleanData();

    //配置保存
    Reply netflowConfig(NetFlowConfigParam netFlowConfigParam);

    //获取配置
    Reply netflowConfigList(NetflowAgentParam netflowAgentParam);

    /**
     * 获取流量监控树状图
     *
     * @return 树状图
     */
    Reply browseTree();

    /**
     * 批量开启/关闭
     *
     * @param paramList 参数列表
     * @param type      状态
     */
    void switchNetFlow(List<AssetParam> paramList, OperationType type);

    /**
     * 获取流量监控结果
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply browseResult(NetFlowRequestParam requestParam);

    /**
     * 获取IP地址组数据
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply browseIpGroup(IpGroupRequestParam requestParam);

    /**
     * 获取IP地址组数据下拉数据
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply dropBrowseIpGroup(IpGroupRequestParam requestParam);

    /**
     * 获取单个IP地址组数据
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply browseOneIpGroup(IpGroupRequestParam requestParam);

    /**
     * 添加IP地址组数据
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply addIpGroup(IpGroupRequestParam requestParam);

    /**
     * 编辑IP地址组数据
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply editIpGroup(IpGroupRequestParam requestParam);

    /**
     * 删除IP地址组数据
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply deleteIpGroup(IpGroupRequestParam requestParam);

    /**
     * 更新IP地址组数据状态
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply updateIpGroupState(IpGroupRequestParam requestParam);

    /**
     * 查看应用
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply browseApp(ApplicationRequestParam requestParam);

    /**
     * 查看单个应用
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply browseOneApp(ApplicationRequestParam requestParam);

    /**
     * 添加应用
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply addApp(ApplicationRequestParam requestParam);

    /**
     * 编辑应用
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply editApp(ApplicationRequestParam requestParam);

    /**
     * 删除应用
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply deleteApp(ApplicationRequestParam requestParam);

    /**
     * 更新应用数据状态
     *
     * @param requestParam 请求参数
     * @return
     */
    Reply updateAppState(ApplicationRequestParam requestParam);

    /**
     * 缓存数据到REDIS
     *
     * @return
     */
    TimeTaskRresult cacheResultToRedis();

    /**
     * 获取资产及端口数据
     *
     * @param assetParam
     * @return
     */
    Reply popupInterfaces(AssetParam assetParam);

    /**
     * 更新资产的端口数据
     *
     * @param assetParam
     * @return
     */
    Reply editorInterfaces(AssetParam assetParam);

    /**
     * 获取流量明细
     * @return
     */
    Reply getNetFlowDetail(NetFlowDetailParam param);

    /**
     * 获取流量明细图表
     * @return
     */
    Reply getNetFlowDetailChart(NetFlowDetailParam param);

    /**
     * 获取ES中索引的字段
     * @return
     */
    Reply getNetFlowColumns();

    /**
     * 获取流量明细缓存数据
     *
     * @return
     */
    Reply getNetFlowDetailCacheInfo();

    /**
     * 保存流量分析已选择的数据
     * @param cacheInfo
     * @return
     */
    Reply saveNetFlowDetailSelectedColumns(NetflowDetailCacheInfo cacheInfo);
}
