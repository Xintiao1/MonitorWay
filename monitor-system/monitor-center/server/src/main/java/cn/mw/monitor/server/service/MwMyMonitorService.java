package cn.mw.monitor.server.service;

import cn.mw.monitor.server.param.QueryAdvanceTableParam;
import cn.mw.monitor.server.param.QueryArgumentsParam;
import cn.mw.monitor.server.param.QueryComLayoutParam;
import cn.mw.monitor.server.param.QueryComLayoutVersionParam;
import cn.mw.monitor.server.serverdto.ComponentLayoutDTO;
import cn.mw.monitor.service.server.api.dto.AdvanceTableDTO;
import cn.mw.monitor.service.server.api.dto.ItemBaseDTO;
import cn.mw.monitor.service.server.api.dto.LineChartDTO;
import cn.mwpaas.common.model.Reply;

/**
 * @author syt
 * @Date 2021/2/3 11:11
 * @Version 1.0
 */
public interface MwMyMonitorService {
    /**
     * 根据筛选条件查询组件所需参数选项
     *
     * @param queryArgumentsParam
     * @return
     */
//    Reply getItemInfoByFilterTest(QueryArgumentsParam queryArgumentsParam);
    Reply getItemInfoByFilter(QueryArgumentsParam queryArgumentsParam);

    /**
     * 根据筛选条件查询展示组件
     *
     * @param
     * @return
     */
    Reply getComponentList();

    /**
     * 查询折线图数据
     *
     * @param lineChartDTO
     * @return
     */
    Reply getLineChartsData(LineChartDTO lineChartDTO);

    /**
     * 保存详情页布局
     *
     * @param aParam
     * @return
     */
    Reply saveComponentLayout(ComponentLayoutDTO aParam);

    /**
     * 根据用户id和资产子类型进行查询，若查出则该用户有个性化页面，否者查询资产子类型的默认页面，若也没有默认页面则页面为空
     *
     * @param qParam
     * @return
     */
    Reply selectComponentLayout(QueryComLayoutParam qParam);

    Reply selectComLayoutByVersion(QueryComLayoutVersionParam qParam);

    /**
     * 取了前五的监控项排行榜，从大到小
     *
     * @param param
     * @return
     */
    Reply getItemRank(ItemBaseDTO param);

    /**
     * 根据用户所选监控项名称，获得当前资产的某些有关联的基本信息，制成表格数据
     *
     * @param param
     * @return
     */
    Reply getItemsTableInfo(ItemBaseDTO param);

    /**
     * 根据用户所选监控项名称，获得当前一小时数据制成不超过12条的柱状图
     *
     * @param param
     * @return
     */
    Reply getBarGraphInfo(LineChartDTO param);

    /**
     * 根据用户所选监控项名称,获取磁盘饼状图
     *
     * @param param
     * @return
     */
    Reply getPieChartInfo(LineChartDTO param);

    /**
     * 根据用户所选所有监控项名称,获取所有最新数据，绘制成饼状图
     *
     * @param param
     * @return
     */
    Reply getPieChartData(ItemBaseDTO param);

    /**
     * 高级表格查询所有应用集list
     *
     * @param param
     * @return
     */
    Reply getApplicationList(QueryAdvanceTableParam param);

    /**
     * 高级表格根据应用集查询监控项和监控设备list
     *
     * @param param
     * @return
     */
    Reply getItemListByApplication(QueryAdvanceTableParam param);

    Reply getAdvanceTableInfo(AdvanceTableDTO param);

    /**
     * 根据信息获取完整的折线图参数
     * @param param
     * @return
     */
    LineChartDTO getLineChartDTO(LineChartDTO param);
}
