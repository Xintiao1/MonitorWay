package cn.mw.monitor.visualized.service;

import cn.mw.monitor.visualized.dto.MwVisualizedViewDto;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName MwVisualizedExportService
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/8/24 16:30
 * @Version 1.0
 **/
public interface MwVisualizedExportService {

    void export(HttpServletResponse response, MwVisualizedViewDto viewDto);
}
