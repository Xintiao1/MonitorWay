package cn.mw.monitor.assetsTemplate.service;

import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.QueryTemplateExportParam;
import cn.mwpaas.common.model.Reply;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @author qzg
 * @date 2021/7/5
 */
public interface MwAssetsTemplateExportService {
    Reply templateInfoExport(QueryTemplateExportParam qParam, HttpServletResponse response);

    Reply templateInfoImport(MultipartFile file, HttpServletResponse response);
}
