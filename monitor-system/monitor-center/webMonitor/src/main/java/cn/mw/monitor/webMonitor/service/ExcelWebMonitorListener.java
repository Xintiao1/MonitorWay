package cn.mw.monitor.webMonitor.service;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.license.service.CheckCountService;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.util.ExcelUtils;
import cn.mw.monitor.webMonitor.api.param.webMonitor.AddUpdateWebMonitorParam;
import cn.mw.monitor.webMonitor.api.param.webMonitor.ImportWebMonitorParam;
import cn.mw.monitor.webMonitor.exception.TransformException;
import cn.mwpaas.common.model.Reply;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xhy
 * @date 2021/1/5 17:34
 */
@Slf4j
public class ExcelWebMonitorListener extends AnalysisEventListener<ImportWebMonitorParam> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger("ExcelWebMonitorListener");

    private MwWebMonitorService mwWebMonitorService;

    private HttpServletResponse response;

    private String fileName;

    private LicenseManagementService licenseManagement;

    private CheckCountService checkCountService;

//    public ExcelWebMonitorListener(MwWebMonitorService mwWebMonitorService, HttpServletResponse response, String fileName) {
//        this.mwWebMonitorService = mwWebMonitorService;
//        this.response = response;
//        this.fileName = fileName;
//    }

    public ExcelWebMonitorListener(MwWebMonitorService mwWebMonitorService, HttpServletResponse response, String fileName, LicenseManagementService licenseManagement, CheckCountService checkCountService) {
        this.mwWebMonitorService = mwWebMonitorService;
        this.response = response;
        this.fileName = fileName;
        this.licenseManagement = licenseManagement;
        this.checkCountService = checkCountService;
    }

    /**
     * 每隔100条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;

    List<ImportWebMonitorParam> list = new ArrayList<ImportWebMonitorParam>();

    List<ImportWebMonitorParam> failList = new ArrayList<ImportWebMonitorParam>();

    @Override
    public void invoke(ImportWebMonitorParam data, AnalysisContext context) {
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
            logger.info("所有数据解析完成！");
        }
        if (failList.size() > 0) {
            excelFailData(failList);
            logger.info("解析失败的所数据导出完成");
        }

    }


    private void excelFailData(List<ImportWebMonitorParam> failList) {
        Set<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("webName");
        includeColumnFiledNames.add("webUrl");
        includeColumnFiledNames.add("hostIp");
        includeColumnFiledNames.add("updateInterval");
        includeColumnFiledNames.add("attempts");

        includeColumnFiledNames.add("enable");
        includeColumnFiledNames.add("timeOut");
        includeColumnFiledNames.add("statusCode");

        includeColumnFiledNames.add("principalName");
        includeColumnFiledNames.add("orgs");
        includeColumnFiledNames.add("groups");
        includeColumnFiledNames.add("monitorServer");
        includeColumnFiledNames.add("errorMsg");
        ExcelWriter excelWriter = null;
        String failFileName = fileName.indexOf(".") != -1 ? fileName.substring(0,fileName.indexOf(".")) : fileName;
        failFileName = "error_" + failFileName;
        try {
            excelWriter = ExcelUtils.getExcelWriter(failFileName, response, ImportWebMonitorParam.class);
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet" + 0)
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(failList, sheet);
            logger.info("导出成功");
        } catch (IOException e) {
            logger.error("导出失败{}", e);
        } finally {
            if (null != excelWriter) {
                excelWriter.finish();
            }
        }
    }

    private void saveData() {
        logger.info("{}条数据，开始添加web数据！", list.size());

        if (list.size() > 0) {
            List<Future<Reply>> futureList = new ArrayList<>();
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(10, list.size() > 10 ? list.size() : 10, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            for (ImportWebMonitorParam importWebMonitorParam : list) {
                Callable<Reply> callable = new Callable<Reply>() {
                    @Override
                    public Reply call() throws Exception {
                        Reply reply;
                        try {
                            AddUpdateWebMonitorParam addUpdateWebMonitorParam = mwWebMonitorService.transform(importWebMonitorParam);
                            //许可校验
                            //数量获取
                            int count = checkCountService.selectTableCount("mw_webmonitor_table", false);
                            ResponseBase responseBase = licenseManagement.getLicenseManagemengt("assets_manage_web", count, 1);
                            if (responseBase.getRtnCode() != 200) {
                                importWebMonitorParam.setErrorMsg(responseBase.getMsg());
                                failList.add(importWebMonitorParam);
                                return Reply.fail(responseBase.getMsg());
                            }
                            reply = mwWebMonitorService.insertWebMonitor(addUpdateWebMonitorParam);
                            if (reply.getRes() != 0) {
                                importWebMonitorParam.setErrorMsg(reply.getMsg());
                                failList.add(importWebMonitorParam);
                                return Reply.fail(reply.getMsg());
                            } else {
                                return reply;
                            }
                        } catch (TransformException e) {
                            importWebMonitorParam.setErrorMsg(e.getMessage());
                            failList.add(importWebMonitorParam);
                            return Reply.fail(e.getMessage());
                        }
                    }
                };
                Future<Reply> submit = executorService.submit(callable);
                futureList.add(submit);
            }
            for (Future<Reply> future : futureList) {
                try {
                    future.get(10, TimeUnit.MINUTES);
                } catch (Exception e) {
                    logger.info("saveData{}", e);
                }
            }
        }

        logger.info("{}条数据，导入失败数据！", failList.size());
        logger.info("{}条数据，添加web数据结束！", list.size());
    }


}
