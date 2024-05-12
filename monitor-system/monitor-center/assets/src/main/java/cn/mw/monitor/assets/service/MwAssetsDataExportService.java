package cn.mw.monitor.assets.service;

import cn.mw.monitor.assets.dto.MwAssetsDataExportDto;
import cn.mwpaas.common.model.Reply;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2021/6/24
 */
public interface MwAssetsDataExportService {
    Reply exportForExcel(MwAssetsDataExportDto param, HttpServletRequest request, HttpServletResponse response);

    Reply exportComponentLayoutForExcel(MwAssetsDataExportDto param, HttpServletRequest request, HttpServletResponse response);

    Reply importComponentLayoutForExcel(MultipartFile file, HttpServletResponse response);

    Reply missTypeExport(HttpServletResponse response);

    Reply isCoverImportData();

    /**
     * 导出资产监控项指标
     * @param response
     */
    void exportAssetsIndex(HttpServletResponse response);
}
