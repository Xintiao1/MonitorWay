package cn.mw.monitor.service.assets.service;

import cn.mw.monitor.service.assets.model.MWMainTainHostView;
import cn.mw.monitor.service.assets.param.MwAssetsMainTainDelParam;
import cn.mw.monitor.service.assets.param.MwAssetsMainTainParam;
import cn.mw.monitor.service.assets.param.MwAssetsMainTainParamV1;
import cn.mw.monitor.service.model.param.QueryInstanceModelParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @ClassName MwAssetsMainTainService
 * @Description 猫维资产管理维护的操作接口
 * @Author gengjb
 * @Date 2021/7/26 15:28
 * @Version 1.0
 **/
public interface MwAssetsMainTainService {

    /**
     * 新增zabbix中的维护期间
     * @param param 添加主机维护的参数
     * @return
     */
    Reply addAssetsMainTain(MwAssetsMainTainParamV1 param);

    /**
     * 查询资产管理下维护页面的数据
     * @param param 添加主机维护的参数
     * @return
     */
    Reply selectAssetsMainTain(MwAssetsMainTainParam param);


    /**
     * 修改资产管理下维护页面的数据
     * @param param 修改主机维护的参数
     * @return
     */
    Reply updateAssetsMainTain(MwAssetsMainTainParamV1 param);

    /**
     * 删除维护的数据
     * @return
     */
    Reply deleteAssetsMainTain(List<MwAssetsMainTainDelParam> delParam);


    /**
     * 查询维护主机组所需要的下拉数据
     * @param mainTainParam 对应的监控服务器
     * @return
     */
    Reply selectAssetsMainTainGroupDropDown(MwAssetsMainTainParam mainTainParam);

    /**
     * 查询维护主机所需要的下拉数据
     * @param mainTainParam 资产类型
     * @return
     */
    Reply selectAssetsMainTainHostDropDown(MwAssetsMainTainParam mainTainParam);


    Reply selectMainTainAssetsDifficulty();

    Reply getAssetsMainTainPlanFuzzQuery();

    /**
     * 查询主机数据
     * @param mainTainParam
     * @return
     */
    Reply selectMainTainHostInfo(MwAssetsMainTainParam mainTainParam) throws Exception;

    //查询实例信息
    Reply getModelListInfo(QueryInstanceModelParam param);

    /**
     * 获取维护中的资产
     * @return
     */
    List<MWMainTainHostView> getUnderMaintenanceHost();
}
