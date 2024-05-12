package cn.mw.monitor.service.server.api;

import cn.mw.monitor.service.server.api.dto.*;
import cn.mw.monitor.service.server.param.*;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author xhy
 * @date 2020/4/26 15:25
 */
public interface MwServerService {
    public final static String NUMERAL = "NUMERAL";

    Reply getHistoryData(ServerHistoryDto serverHistoryDto);

    Reply getItemRank(RankServerDTO param);

    Reply getSoftwareDataList(AssetsIdsPageInfoParam param);

    Reply getDiskDataList(AssetsBaseDTO assetsBaseDTO);

    Reply getDiskDetail(DiskTypeDto diskTypeDto);

    Reply getNetDataList(AssetsIdsPageInfoParam param);

    List<NetListDto> getNetDataAllList(AssetsIdsPageInfoParam param);

    Reply getNetNumCount(AssetsIdsPageInfoParam param);

    List<NetListDto> getNetDataListAll(AssetsIdsPageInfoParam param);

    Reply getAllNetDataListByZabbix();

    void getNetDataListByZabbix(List<AssetsIdsPageInfoParam> paramList, List<NetListDto> listDtos, List<String> netNameList);

//    Reply getNetDataListTest(AssetsIdsPageInfoParam param);

    Reply getNetDetail(DiskTypeDto diskTypeDto);

    Reply getApplication(AssetsBaseDTO param);

    Reply getItemApplication(ApplicationParam param);

    Reply getNameListByNameType(TypeFilterDTO param);

    Reply getNavigationBarByApplication(QueryNavigationBarParam param);

    Reply getHistoryByItemId(ItemLineParam iParam);

    Reply getHistoryByItemIds(List<ItemLineParam> itemLineParams, int monitorServerId);

    Reply getHardwareByHostId(QueryApplicationTableParam param);

    Reply getDiskInfo(int monitorServerId, String name, String hostId);

    Reply getMonitoringItems(int monitorServerId, String hostId, String itemName);

    Reply getHistoryDataInfo(ServerHistoryDto serverHistoryDto);

    Reply getDurationAndStatusByHostId(int monitorServerId, String hostId);

    //获取详情页table标签列表接口;数据库结构已修改，有需要的从这个接口获取数据
    Reply selectNavigationBar(String templateId, String assetsId);

    Reply getRunServiceObjectByIp(RunServiceObjectParam param);

    Reply getRecordByAssetsId(String id, String type);


//    Reply getRecordByTypeId(String id);

    Reply getAlarmByHostId(AssetsIdsPageInfoParam alarmParam);

    Reply getRunServiceObjectByIpNoPage(RunServiceObjectParam param);

//    Reply getAlarmByHostIdNoPage(AssetsIdsPageInfoParam alarmParam);

    Reply getAvailableByHostId(QueryAssetsAvailableParam param);

    Reply getAvailableByHostIdTest(QueryAssetsAvailableParam param);

    Reply getChannelInfoList(AssetsIdsPageInfoParam param);

    Reply itemCheckNow(int monitorServerId, List<String> itemIds);

    List<ItemApplication> itemsGet(int monitorServerId, String hostId, String itemName);

    List<ItemApplication> itemsGetByNames(int monitorServerId, List<String> hostIds, List<String> itemNames);

    //查询资产状态
    Reply getAssetsStatusInfo(QueryAssetsStatusParam param);

    /**
     * 查询进程信息
     *
     * @param monitorServerId 服务器ID
     * @param hostId          主机ID
     * @param itemName        监控项名称
     * @return
     */
    Reply getAssetsDetailsProcess(int monitorServerId, String hostId, String itemName);

    /**
     * 资产详情页签进程top10下载txt
     *
     * @param assetsBaseDTO 查询参数
     * @param response
     */
    void downloadAssetsDetailsProcess(AssetsBaseDTO assetsBaseDTO, HttpServletResponse response);

    /**
     * 获取历史趋势数据
     * @return
     */
    Reply getHistoryTrend(QueryItemTrendParam param);

    /**
     * 获取资产的温度与湿度
     * @param param
     * @return
     */
    Reply getAssetsTempAndHumidity(QueryItemTrendParam param);

    /**
     * 获取资产详情的清单信息
     * @param param
     * @return
     */
    Reply getAssetsDetatils(QueryItemTrendParam param);

}
