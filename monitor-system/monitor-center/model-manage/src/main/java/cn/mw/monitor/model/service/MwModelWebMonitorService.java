package cn.mw.monitor.model.service;

import cn.mw.monitor.bean.ExcelExportParam;
import cn.mw.monitor.model.dto.MwModelWEBProxyDTO;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.service.webmonitor.model.HttpParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.BatchUpdateParam;
import cn.mw.monitor.webMonitor.exception.TransformException;
import cn.mwpaas.common.model.Reply;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface MwModelWebMonitorService {

    Reply selectWebSeverInfo(MwModelWEBProxyDTO param);

    List<AddAndUpdateModelWebMonitorParam> batchCreateWebSeverData(List<AddAndUpdateModelWebMonitorParam> list);

    MwModelImportWebListParam transform(List<MwModelImportWebMonitorParam> list);

    AddAndUpdateModelWebMonitorParam createWebSeverData(AddAndUpdateModelWebMonitorParam param);

    List<MwModelWebMonitorTable> queryWebSeverList(List<MwModelWebMonitorTable> params);

    Reply updateWebMonitor(List<AddAndUpdateModelWebMonitorParam> params);

    Reply deleteWebMonitor(List<HttpParam> params) throws Exception;

    Reply excelTemplateExport(ExcelExportParam excelExportParam, HttpServletResponse response);

   void excelImportWebMonitor(MultipartFile file,HttpServletResponse response);

    Reply updateState(MwModelUpdateWebMonitorStateParam updateWebMonitorStateParam);
}
