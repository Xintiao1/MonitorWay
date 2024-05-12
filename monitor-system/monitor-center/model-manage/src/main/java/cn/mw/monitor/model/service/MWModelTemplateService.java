package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.*;
import cn.mwpaas.common.model.Reply;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author qzg
 * @date 2022/05/05
 */
public interface MWModelTemplateService {
    Reply addModelTemplate(AddAndUpdateModelTemplateParam param);

    Reply updateModelTemplate(AddAndUpdateModelTemplateParam param);

    Reply checkModelFirmByName(AddAndUpdateModelFirmParam param);

    Reply queryModelTemplate(QueryModelTemplateParam param);

    Reply popupBrowseModelTemplate(QueryModelTemplateParam param);

    Reply selectList(QueryModelTemplateParam param);

    Reply deleteModelTemplate(AddAndUpdateModelTemplateParam param);

    Reply fuzzSearchAllFiledData(String value);

    Reply templateGet();

    Reply updateModelTemplateByMore();

    Reply templateInfoExport(QueryModelTemplateExportParam qParam, HttpServletResponse response);

    Reply templateInfoImport(MultipartFile file, HttpServletResponse response);

    Reply exportByFormwork(HttpServletRequest request, HttpServletResponse response);

    Reply updateTemplateStatus(List<UpdateModelTemplateStatusParam> list);
}
