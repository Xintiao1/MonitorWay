package cn.mw.monitor.service.alert.api;

import cn.mw.monitor.service.alert.dto.AlertReasonEditorParam;
import cn.mw.monitor.service.alert.dto.MWItemDto;
import cn.mw.monitor.service.alert.dto.RecordParam;
import cn.mw.monitor.service.alert.dto.ZbxAlertDto;
import cn.mw.monitor.service.alert.param.AssetsStatusQueryParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.zbx.model.AlertDTO;
import cn.mw.monitor.service.zbx.model.HostProblem;
import cn.mw.monitor.service.zbx.param.*;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.List;

/**
 * @author xhy
 * @date 2020/3/27 14:10
 */
public interface MWAlertService {

    Reply getCurrAlertPage(AlertParam alertParam);

    /**
     * 从redis获取当前压缩告警信息
     *
     * @return List<AlertDTO>
     */
    List<AlertDTO> getZipCurrAlertPageFromRedis(AlertParam dto);

    /**
     * 从redis获取存在问题主机
     *
     * @return List<HostProblem>
     */
    List<HostProblem> getZipCurrHostProblemFromRedis();

    Reply getHistAlertPage(AlertParam mwAlertDto);

    Reply nowFuzzSeachAllFiledData(AlertParam alertParam);

    Reply histFuzzSeachAllFiledData(AlertParam alertParam);

    void export(AlertParam alertParam, HttpServletResponse response) throws ParseException;

    Reply getAlertHistory(Integer monitorServerId, String objectid);

    Reply getNoticeList(Integer monitorServerId, String eventid);


    /**
     * 确认事件
     *
     * @param eventid
     * @return
     */
    Reply confirm(Integer monitorServerId, Integer userId, String eventid, String type);

    Reply confirmList(List<ConfirmDto> param, String type);

    Reply getItemByTriggerId(Integer monitorServerId, String objectid);

    Reply getHistoryByItemId(MWItemDto mwItemDto);

    Reply getLuceneByTitle(String title);

    Reply getSendInfo(RecordParam param);

    /**
     * 查询关联告警
     * @param mwTangibleassetsDTO
     * @return
     */
    String getAssetsMonitor(MwTangibleassetsDTO mwTangibleassetsDTO);

    List<ZbxAlertDto> getCurrentAltertList(AlertParam dto);

    List<ZbxAlertDto> getFilterSelect(List<ZbxAlertDto> mwAlertDtos, AlertParam alertParam) throws ParseException ;

    Reply getAlertLevel();

    Reply reasonEditor(AlertReasonEditorParam param);

    Reply closeEventId(List<CloseDto> param);

    List<String> getAssetsStatusByHisAlert(AssetsStatusQueryParam alertParam);

    Reply getTiegger();

    Reply ignoreAlert(List<IgnoreAlertDto> params);

    Reply getignoreAlert(IgnoreAlertDto params);

    void triggerExport(HttpServletResponse response) throws ParseException;

    Reply getAlertCount(AlertCountParam param);

    Reply getAlert(AlertCountParam param);

    Reply getEventFlowByEventId(Integer monitorServerId, String eventid);

    Reply getAlertMessage(AlertCountParam param);

    Reply getIsAlert(List<QueryAlertStateParam> params);
}
