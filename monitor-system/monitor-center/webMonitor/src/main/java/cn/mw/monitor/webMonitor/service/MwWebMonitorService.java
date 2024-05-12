package cn.mw.monitor.webMonitor.service;

import cn.mw.monitor.webMonitor.exception.TransformException;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.bean.ExcelExportParam;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.webMonitor.api.param.webMonitor.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;


/**
 * Created by baochengbin on 2020/3/12.
 */
public interface MwWebMonitorService {

    Reply selectById(Integer id);

    Reply selectList(QueryWebMonitorParam qParam);

    Reply updateWebMonitor(BatchUpdateParam uParam);

    Reply deleteWebMonitor(DeleteWebMonitorParam deleteWebMonitorParam);

    Reply insertWebMonitor(AddUpdateWebMonitorParam aParam);

    Reply updateState(UpdateWebMonitorStateParam updateWebMonitorStateParam);

    Reply selectWebInfo(QueryWebHistoryParam qParam);

    Reply getAssetsListByAssetsTypeId(MwCommonAssetsDto mwCommonAssetsDto);

    void excelImport(MultipartFile file, HttpServletResponse response);

    void excelTemplateExport(ExcelExportParam excelExportParam, HttpServletResponse response);

    AddUpdateWebMonitorParam transform(ImportWebMonitorParam iParam) throws TransformException;

    Reply fuzzSearchAllFiledData(QueryWebMonitorParam param);
}
