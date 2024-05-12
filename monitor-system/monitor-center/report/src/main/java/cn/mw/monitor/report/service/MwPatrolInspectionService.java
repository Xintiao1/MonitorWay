package cn.mw.monitor.report.service;

import cn.mw.monitor.report.param.PatrolInspectionParam;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName MwPatrolInspectionService
 * @Author gengjb
 * @Date 2022/11/1 10:22
 * @Version 1.0
 **/
public interface MwPatrolInspectionService {

    //查询巡检报告显示数据
    Reply selectPatrolInspection(PatrolInspectionParam param);

    //导出巡检报告word
    void exportWord(HttpServletRequest request, HttpServletResponse response,PatrolInspectionParam patrolInspectionParam);

    //导出巡检报告excel
    void exportExcel(PatrolInspectionParam patrolInspectionParam,HttpServletRequest request, HttpServletResponse response);

    //导出接口利用率明细EXCEL
    void exportInterfaceExcel(PatrolInspectionParam patrolInspectionParam,HttpServletRequest request, HttpServletResponse response);

    String getExportWordPath(PatrolInspectionParam patrolInspectionParam);

    String getExportExcelPath(PatrolInspectionParam patrolInspectionParam);
}
