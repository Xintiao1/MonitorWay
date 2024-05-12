package cn.mw.monitor.model.service;

import cn.mw.monitor.model.dto.MwModelPowerDTO;
import cn.mw.monitor.model.exception.ModelManagerException;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.ModelExportDataInfoListParam;
import cn.mw.monitor.model.param.ModelExportDataInfoParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceParam;
import cn.mwpaas.common.model.Reply;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author qzg
 * @date 2021/12/06
 */
public interface MwModelExportService {
    Reply getFieldByFile(MultipartFile file, ModelExportDataInfoParam param);

    Reply exportDataInfo(MultipartFile file, ModelExportDataInfoListParam param);

    Reply getAllModelList();

    Reply  exportForExcel(QueryModelInstanceParam param, HttpServletRequest request, HttpServletResponse response);

    Reply exportTemplatel(QueryModelInstanceParam param, HttpServletRequest request, HttpServletResponse response);

    Reply batchInsertImportData(MultipartFile file);

    Reply importWebMonitorData(MultipartFile file);

    List<MwModelPowerDTO> selectGroupIdInfo();

    List<MwModelPowerDTO> selectOrgIdInfo();

    List<MwModelPowerDTO> selectUserIdInfo();

}
