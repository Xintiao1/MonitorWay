package cn.mw.monitor.configmanage.service.impl;

import cn.mw.monitor.accountmanage.dao.MwAccountManageTableDao;
import cn.mw.monitor.accountmanage.entity.AddAccountManageParam;
import cn.mw.monitor.accountmanage.entity.MwQueryAccountManageTable;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.configmanage.dao.MwConfigManageTableDao;
import cn.mw.monitor.configmanage.entity.ExportAssetsParam;
import cn.mw.monitor.configmanage.entity.MwAccountMapper;
import cn.mw.monitor.configmanage.entity.MwTangibleassetsTable;
import cn.mw.monitor.configmanage.entity.MwTemplateMapper;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.templatemanage.dao.MwTemplateManageDao;
import cn.mw.monitor.templatemanage.entity.MwQueryTemplateManageTable;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mw.monitor.util.ExcelUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.write.metadata.WriteSheet;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author gui.quanwang
 * @className AssetsExcelImportListener
 * @description 配置管理资产数据导入监听器
 * @date 2022/7/5
 */
@Slf4j
public class AssetsExcelImportListener extends AnalysisEventListener<ExportAssetsParam> {

    //配置数据DAO
    private MwConfigManageTableDao configManageTableDao;


    private MwModelViewCommonService mwModelViewCommonService;

    //账户数据DAO
    private MwAccountManageTableDao mwAccountManageTableDao;

    //模板数据DAO
    private MwTemplateManageDao mwTemplateManageDao;

    private HttpServletResponse response;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 操作人员名称
     */
    private String operatorName;

    /**
     * 每隔100条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 100;

    /**
     * 解析成功的数据
     */
    List<ExportAssetsParam> list = new ArrayList<>();

    /**
     * 解析失败的数据
     */
    List<ExportAssetsParam> failList = new ArrayList<>();

    public AssetsExcelImportListener(HttpServletResponse response, String fileName, String operatorName) {
        this.configManageTableDao = ApplicationContextProvider.getBean(MwConfigManageTableDao.class);
        this.mwAccountManageTableDao = ApplicationContextProvider.getBean(MwAccountManageTableDao.class);
        this.mwTemplateManageDao = ApplicationContextProvider.getBean(MwTemplateManageDao.class);
        this.mwModelViewCommonService = ApplicationContextProvider.getBean(MwModelViewCommonService.class);
        this.response = response;
        this.fileName = fileName;
        this.operatorName = operatorName;
    }

    /**
     * When analysis one row trigger invoke function.
     *
     * @param data    one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(ExportAssetsParam data, AnalysisContext context) {
        list.add(data);
        if (list.size() >= BATCH_COUNT) {
            saveData();
            list.clear();
        }
    }

    /**
     * if have something to do after all analysis
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

    /**
     * 导出错误数据至excel文件里
     *
     * @param failList 错误数据
     */
    private void excelFailData(List<ExportAssetsParam> failList) {
        Set<String> includeColumnFiledNames = new HashSet<>();
        includeColumnFiledNames.add("assetsName");
        includeColumnFiledNames.add("templateName");
        includeColumnFiledNames.add("accountName");
        includeColumnFiledNames.add("userName");
        includeColumnFiledNames.add("password");
        includeColumnFiledNames.add("protocol");
        includeColumnFiledNames.add("port");
        includeColumnFiledNames.add("assetsId");
        includeColumnFiledNames.add("errorMsg");
        ExcelWriter excelWriter = null;
        String failFileName = fileName.indexOf(".") != -1 ? fileName.substring(0, fileName.indexOf(".")) : fileName;
        failFileName = "error_" + failFileName;
        try {
            excelWriter = ExcelUtils.getExcelWriter(failFileName, response, ExportAssetsParam.class);
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
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            for (ExportAssetsParam importAssetsParam : list) {
                Callable<Reply> callable = new Callable<Reply>() {
                    @Override
                    public Reply call() throws Exception {
                        Reply reply;
                        try {
                            MwTangibleassetsTable assets = null;
                            //第一步：先查询资产是否存在
                            Reply modelReply = mwModelViewCommonService.selectById(importAssetsParam.getAssetsId());
                            if(null != modelReply && PaasConstant.RES_SUCCESS == modelReply.getRes()){
                                assets = (MwTangibleassetsTable)modelReply.getData();
                            }

                            if (assets == null || !assets.getSettingFlag()) {
                                importAssetsParam.setErrorMsg("资产不存在或未开启配置状态");
                                failList.add(importAssetsParam);
                                return Reply.fail("资产不存在或未开启配置状态");
                            }
                            //第二步：查询账户信息是否存在,不存在则创建
                            MwQueryAccountManageTable accountInfo = mwAccountManageTableDao.getInfoByAccountName(importAssetsParam.getAccountName());
                            int accountId = 0;
                            if (accountInfo == null) {
                                String checkError = checkAccountParam(importAssetsParam);
                                if (StringUtils.isNotEmpty(checkError)) {
                                    importAssetsParam.setErrorMsg(checkError);
                                    failList.add(importAssetsParam);
                                    return Reply.fail(checkError);
                                }
                                accountId = addAccount(importAssetsParam);
                            } else {
                                accountId = accountInfo.getId();
                            }
                            //第三步：查询模板信息是否存在
                            MwQueryTemplateManageTable templateInfo = mwTemplateManageDao.getInfoByTemplateName(importAssetsParam.getTemplateName());
                            if (templateInfo == null) {
                                importAssetsParam.setErrorMsg("模板不存在");
                                failList.add(importAssetsParam);
                                return Reply.fail("模板不存在");
                            }
                            //第四步：关联资产和账户及模板数据
                            String errorMsg = updateAssets(importAssetsParam.getAssetsId(), accountId, templateInfo.getId());
                            if (StringUtils.isNotEmpty(errorMsg)) {
                                importAssetsParam.setErrorMsg(errorMsg);
                                failList.add(importAssetsParam);
                                return Reply.fail(errorMsg);
                            }
                            return Reply.ok();
                        } catch (Throwable throwable) {
                            log.error("添加失败", throwable);
                            importAssetsParam.setErrorMsg("添加失败");
                            failList.add(importAssetsParam);
                            return Reply.fail("添加失败");
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
                    log.error("saveData error ", e);
                }
            }
        }
        log.info("{}条数据，导入失败数据！", failList.size());
        log.info("{}条数据，添加web数据结束！", list.size());
    }

    /**
     * 更新资产数据信息
     *
     * @param assetsId   资产ID
     * @param accountId  账户ID
     * @param templateId 模板ID
     */
    private String updateAssets(String assetsId, int accountId, Integer templateId) {
        String errorMsg = null;
        try {
            if (accountId == 0) {
                errorMsg = "更新关联数据失败，无账户信息";
                return errorMsg;
            }
            if (templateId == 0) {
                errorMsg = "更新关联数据失败，无模板信息";
                return errorMsg;
            }
            MwAccountMapper accountMapper = new MwAccountMapper();
            accountMapper.setCreateDate(new Date());
            accountMapper.setCreator(operatorName);
            accountMapper.setModificationDate(new Date());
            accountMapper.setModifier(operatorName);
            accountMapper.setAssetsId(assetsId);
            accountMapper.setAccountId(accountId);

            //先删除资产和账户关联，再添加
            configManageTableDao.deleteAccountMapper(assetsId);
            configManageTableDao.saveAccountMapper(accountMapper);

            MwTemplateMapper templateMapper = new MwTemplateMapper();
            templateMapper.setAssetsId(assetsId);
            templateMapper.setCreateDate(new Date());
            templateMapper.setCreator(operatorName);
            templateMapper.setModificationDate(new Date());
            templateMapper.setModifier(operatorName);
            templateMapper.setTemplateId(templateId);
            //保存模板,先删除久的，再增加新的
            configManageTableDao.deleteTemplateMapper(assetsId);
            configManageTableDao.saveTemplateMapper(templateMapper);
        } catch (Exception e) {
            log.error("更新资产关联模板和账户数据信息", e);
            errorMsg = "更新关联数据失败";
        }
        return errorMsg;
    }

    /**
     * 创建账户信息
     *
     * @param importAssetsParam 导入数据
     * @return
     */
    private int addAccount(ExportAssetsParam importAssetsParam) {
        AddAccountManageParam accountParam = new AddAccountManageParam();
        //添加ip地址管理 主信息
        accountParam.setCreator(operatorName);
        accountParam.setCreateDate(new Date());
        accountParam.setModifier(operatorName);
        accountParam.setModificationDate(new Date());

        String pass = importAssetsParam.getPassword();
        if (StringUtils.isNotEmpty(pass)) {
            String str = EncryptsUtil.encrypt(pass);
            accountParam.setPassword(pass);
        }
        accountParam.setAccount(importAssetsParam.getAccountName());
        accountParam.setUsername(importAssetsParam.getUserName());
        accountParam.setPassword(importAssetsParam.getPassword());
        accountParam.setProtocol(importAssetsParam.getProtocol());
        accountParam.setPort(importAssetsParam.getPort());
        accountParam.setEnable(false);

        mwAccountManageTableDao.insert(accountParam);
        return accountParam.getId();
    }


    /**
     * 校验账户数据
     *
     * @param assetsParam 导入数据
     * @return
     */
    private String checkAccountParam(ExportAssetsParam assetsParam) {
        if (StringUtils.isEmpty(assetsParam.getAccountName())) {
            return "账户名称为空";
        }
        if (StringUtils.isEmpty(assetsParam.getUserName())) {
            return "用户名为空";
        }
        if (StringUtils.isEmpty(assetsParam.getPassword())) {
            return "密码为空";
        }
        if (assetsParam.getPassword().length() > 64) {
            return "密码最大长度为64位";
        }
        if (StringUtils.isEmpty(assetsParam.getProtocol())) {
            return "连接协议为空";
        }
        if (StringUtils.isEmpty(assetsParam.getPort())) {
            return "端口为空";
        }
        if (!NumberUtils.isNumber(assetsParam.getPort())) {
            return "端口必须为纯数字";
        }
        if (mwAccountManageTableDao.checkAccountNameRepeat(assetsParam.getAccountName())) {
            return "账户名称重复";
        }
        return null;
    }
}
