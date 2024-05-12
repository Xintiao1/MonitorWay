package cn.mw.monitor.screen.service;

import cn.mw.monitor.screen.dto.MWNewScreenAlertDevOpsEventDto;
import cn.mw.monitor.screen.dto.MWNewScreenAssetsFilterDto;
import cn.mw.monitor.screen.dto.MWNewScreenModuleDto;
import cn.mw.monitor.screen.param.MWAlertCountParam;
import cn.mw.monitor.screen.param.MWNewScreenAssetsCensusParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @ClassName MWNewScreenManage
 * @Description 猫维新大屏
 * @Author gengjb
 * @Date 2021/11/29 10:33
 * @Version 1.0
 **/
public interface MWNewScreenManage {

    //查询资产信息
    Reply getNewScreenAssets();

    //查询资产统计数据
    Reply getNewScreenAssetsCensusData(MWNewScreenAssetsCensusParam param);

    //查询告警运维事件
    Reply getNewScreenAlertDevOpsEvent(List<MWNewScreenAlertDevOpsEventDto> param);

    //查询告警数据
    Reply getActivityAlertData(MWNewScreenAssetsCensusParam param);

    //查询告警分类数据
    Reply getNewScreenActivityAlertCount(MWNewScreenAssetsCensusParam param);

    //查询CPU利用率
    Reply getNewScreenAssetsTopN(MWNewScreenAlertDevOpsEventDto param);

    //获取线路topN数据
    Reply getNewScreenLinkTopN(MWNewScreenAlertDevOpsEventDto param);

    //获取首页流量错误包数据
    Reply getHomePageFlowErrorCountTopN(MWNewScreenAlertDevOpsEventDto param);

    //获取首页流量带宽数据
    Reply getHomePageFlowBandWidthTopN(MWNewScreenAlertDevOpsEventDto param) throws Exception;

    //获取大屏模块
    Reply getNewScreenModule();

    //新大屏下拉数据
    Reply getNewScreenModuleDropDown();

    //创建新大屏模块
    Reply createNewScreenUserModule(MWNewScreenModuleDto screenModuleDto);

    //修改大屏模块数据
    Reply updateNewScreenUserModule(MWNewScreenAssetsFilterDto assetsFilterDto);

    //查询大屏模块资产过滤数据
    Reply selectNewScreenUserModule(MWNewScreenAssetsFilterDto assetsFilterDto);

    //删除用户模块
    Reply deleteNewScreenUserModule(MWNewScreenAssetsFilterDto assetsFilterDto);

    //新首页模块排序
    Reply newScreenModuleSort(List<MWNewScreenModuleDto> screenModuleDto);

    Reply newScreenLabelDrop();

    //告警统计次数
    Reply getAlertCount(MWAlertCountParam param);

    //修改首页模块
    Reply updateNewHomeModule(MWNewScreenModuleDto moduleDto);

    //同步首页卡片信息
    Reply syncNewScreenCardInfo();

    //获取接口丢包率数据
    Reply getInterfaceRate(MWNewScreenAlertDevOpsEventDto param);

}
