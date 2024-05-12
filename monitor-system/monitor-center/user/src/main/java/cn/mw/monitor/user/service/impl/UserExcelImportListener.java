package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.param.user.ExportUserParam;
import cn.mw.monitor.api.param.user.RegisterParam;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.ExcelUtils;
import cn.mwpaas.common.model.Reply;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.TransformException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @author gui.quanwang
 * @className UserExcelImportListener
 * @description 用户excel批量导入监听器
 * @date 2021/12/2
 */
@Slf4j
public class UserExcelImportListener extends AnalysisEventListener<ExportUserParam> {

    private MWUserService mwUserService;

    private HttpServletResponse response;

    private String fileName;

    /**
     * 每隔100条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 解析成功的数据
     */
    List<ExportUserParam> list = new ArrayList<>();

    /**
     * 解析失败的数据
     */
    List<ExportUserParam> failList = new ArrayList<>();

    public UserExcelImportListener(HttpServletResponse response, String fileName) {
        this.mwUserService = ApplicationContextProvider.getBean(MWUserService.class);
        this.response = response;
        this.fileName = fileName;
    }

    @Override
    public void invoke(ExportUserParam data, AnalysisContext context) {
        list.add(data);
        if (list.size() >= BATCH_COUNT) {
            saveData();
            list.clear();
        }
//        if (failList.size() >= BATCH_COUNT) {
//            excelFailData(failList);
//            failList.clear();
//        }
    }

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

    /**
     * 导出错误数据至excel文件里
     *
     * @param failList 错误数据
     */
    private void excelFailData(List<ExportUserParam> failList) {
        Set<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("loginName");
        includeColumnFiledNames.add("userName");
        includeColumnFiledNames.add("phoneNumber");
        includeColumnFiledNames.add("password");
        includeColumnFiledNames.add("orgs");
        includeColumnFiledNames.add("groups");
        includeColumnFiledNames.add("roleName");
        includeColumnFiledNames.add("errorMsg");
        ExcelWriter excelWriter = null;
        String failFileName = fileName.indexOf(".") != -1 ? fileName.substring(0, fileName.indexOf(".")) : fileName;
        failFileName = "error_" + failFileName;
        try {
            excelWriter = ExcelUtils.getExcelWriter(failFileName, response, ExportUserParam.class);
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

    /**
     * 保存数据
     */
    private void saveData() {
        log.info("{}条数据，开始添加web数据！", list.size());
        if (list.size() > 0) {
            List<Future<Reply>> futureList = new ArrayList<>();
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(10,
                    list.size() > 10 ? list.size() : 10, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            for (ExportUserParam importAssetsParam : list) {
                Callable<Reply> callable = new Callable<Reply>() {
                    @Override
                    public Reply call() throws Exception {
                        Reply reply;
                        try {
                            RegisterParam registerParam = mwUserService.transform(importAssetsParam);
                            UserDTO userDTO = CopyUtils.copy(UserDTO.class, registerParam);
                            mwUserService.addUser(userDTO);
                            return Reply.ok();
                        } catch (TransformException e) {
                            importAssetsParam.setErrorMsg(e.getMessage());
                            failList.add(importAssetsParam);
                            return Reply.fail(e.getMessage());
                        } catch (Throwable throwable) {
                            log.error("添加用户至数据库失败", throwable);
                            importAssetsParam.setErrorMsg("添加用户失败");
                            failList.add(importAssetsParam);
                            return Reply.fail("添加用户失败");
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
                    log.info("saveData{}", e);
                }
            }
        }
        log.info("{}条数据，导入失败数据！", failList.size());
        log.info("{}条数据，添加web数据结束！", list.size());
    }
}