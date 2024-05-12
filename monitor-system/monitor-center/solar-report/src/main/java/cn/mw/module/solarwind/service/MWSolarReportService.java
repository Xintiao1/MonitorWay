package cn.mw.module.solarwind.service;


import cn.mwpaas.common.model.Reply;
import cn.mw.module.solarwind.param.ExportSolarParam;
import cn.mw.module.solarwind.param.InputParam;
import cn.mw.module.solarwind.param.QueryParam;

import javax.servlet.http.HttpServletResponse;

/**
 * @author xhy
 * @date 2020/6/22 16:13
 */
public interface MWSolarReportService {

    Reply selectSolarReportList(QueryParam param);

    Reply selectCarrierName();

    Reply selectCaption(String carrierName);

    Reply selectHistory(QueryParam queryParam);

    Reply groupSelect(QueryParam param);

    Reply getHistoryByList(QueryParam param);

    Reply inputSolarTime(InputParam inputParam);

    void export(ExportSolarParam uParam, HttpServletResponse response);

    //PDF导出
    void exportPdf(QueryParam param,HttpServletResponse response,String filePath);

    void selectAll();
}
