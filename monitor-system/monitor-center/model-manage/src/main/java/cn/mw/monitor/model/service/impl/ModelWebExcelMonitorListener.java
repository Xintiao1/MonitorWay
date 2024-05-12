package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.MwModelImportWebListParam;
import cn.mw.monitor.model.param.MwModelImportWebMonitorParam;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelWebMonitorService;
import cn.mw.monitor.util.ExcelUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author xhy
 * @date 2021/1/5 17:34
 */
@Slf4j
public class ModelWebExcelMonitorListener extends AnalysisEventListener<MwModelImportWebMonitorParam> {
    private MwModelInstanceService mwModelInstanceService;
    private MwModelWebMonitorService mwModelWebMonitorService;
    private HttpServletResponse response;
    private static Integer webMonitorModeId = 72;
    private String fileName;

    public ModelWebExcelMonitorListener(MwModelInstanceService mwModelInstanceService, HttpServletResponse response, String fileName, MwModelWebMonitorService mwModelWebMonitorService) {
        this.mwModelInstanceService = mwModelInstanceService;
        this.mwModelWebMonitorService = mwModelWebMonitorService;
        this.response = response;
        this.fileName = fileName;
    }

    /**
     * 每隔100条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 500;

    List<MwModelImportWebMonitorParam> list = new ArrayList<MwModelImportWebMonitorParam>();

    List<MwModelImportWebMonitorParam> failList = new ArrayList<MwModelImportWebMonitorParam>();

    @Override
    public void invoke(MwModelImportWebMonitorParam data, AnalysisContext context) {
        list.add(data);
        if (list.size() >= BATCH_COUNT) {
            saveData();
            list.clear();
        }
        if (failList.size() >= BATCH_COUNT) {
            excelFailData(failList);
            failList.clear();
        }
    }


    /**
     * 所有数据解析完成了 都会来调用
     *
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        if (list.size() > 0) {
            saveData();
            log.info("所有数据解析完成！");
        }
        if (failList.size() > 0) {
            excelFailData(failList);
            log.info("解析失败的所数据导出完成");
        }

    }


    private void excelFailData(List<MwModelImportWebMonitorParam> failList) {
        Set<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("instanceName");
        includeColumnFiledNames.add("webUrl");
        includeColumnFiledNames.add("inBandIp");
        includeColumnFiledNames.add("updateInterval");
        includeColumnFiledNames.add("attempts");

        includeColumnFiledNames.add("enable");
        includeColumnFiledNames.add("timeOut");
        includeColumnFiledNames.add("statusCode");

        includeColumnFiledNames.add("principalName");
        includeColumnFiledNames.add("orgs");
        includeColumnFiledNames.add("groups");
        includeColumnFiledNames.add("modelSystem");
        includeColumnFiledNames.add("modelClassify");
        includeColumnFiledNames.add("errorMsg");
        ExcelWriter excelWriter = null;
        String failFileName = fileName.indexOf(".") != -1 ? fileName.substring(0, fileName.indexOf(".")) : fileName;
        failFileName = "error_" + failFileName;
        try {
            excelWriter = ExcelUtils.getExcelWriter(failFileName, response, MwModelImportWebMonitorParam.class);
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0)
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(failList, sheet);
            log.info("导出成功");
        } catch (IOException e) {
            log.error("导出失败{}", e);
        } finally {
            if (null != excelWriter) {
                excelWriter.finish();
            }
        }
    }

    private void saveData() {
        log.info("{}条数据，开始添加web数据！", list.size());
        List<MwModelImportWebMonitorParam> successList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            try {
                //数据校验;
                MwModelImportWebListParam importWebList = mwModelWebMonitorService.transform(list);
                if (importWebList != null && CollectionUtils.isNotEmpty(importWebList.getSuccessList())) {
                    successList = importWebList.getSuccessList();
                    List<AddAndUpdateModelInstanceParam> addAndUpdateModelInstanceParams = new ArrayList<>();
                    //数据转换为List<AddAndUpdateModelInstanceParam>，进行批量新增
                    addAndUpdateModelInstanceParams = mwModelInstanceService.convertInstanceList(successList, webMonitorModeId);
                    List<AddAndUpdateModelInstanceParam> batchParamList = null;

                    batchParamList = mwModelInstanceService.batchInsertWebMonitorInstance(addAndUpdateModelInstanceParams);
                    //批量插入数据
                    if (batchParamList != null && batchParamList.size() > 0) {
                        mwModelInstanceService.saveData(batchParamList, true, true);
                    }
                }
                if (importWebList != null && CollectionUtils.isNotEmpty(importWebList.getErrorList())) {
                    failList.addAll(importWebList.getErrorList());
                }
            } catch (Exception e) {
                log.error("导入web数据保存失败", e);
            }
        }

        log.info("{}条数据，导入失败数据！", failList.size());
        log.info("{}条数据，添加web数据结束！", successList.size());
    }


}
