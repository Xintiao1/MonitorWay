package cn.mw.monitor.api.controller;

import cn.mw.monitor.accountmanage.service.MwAccountManageService;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.service.impl.ProxySearch;
import cn.mw.monitor.script.entity.MwHomeworkAlert;
import cn.mw.monitor.script.param.*;
import cn.mw.monitor.script.service.ScriptManageService;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.QiZhiApiUtil;
import cn.mw.monitor.util.entity.Progress;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author gui.quanwang
 * @className MwScriptManageController
 * @description 脚本管理
 * @date 2022/4/8
 */
@RequestMapping("/mwapi/script-manage")
@Controller
@Api(value = "脚本管理", tags = "脚本管理")
public class MwScriptManageController extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("control-" + MwScriptManageController.class.getName());


    @Autowired
    private ProxySearch proxySearch;
    @Autowired
    private ScriptManageService scriptManageService;

    @Autowired
    private MWUserService userService;

    @Autowired
    private MwAccountManageService accountManageService;

    @Value("${script-manage.spider.python.url}")
    private String spiderPythonUrl;

    @Value("${script-manage.account.show}")
    private boolean SHOW;


    private final static String DEVICE = "device";
    private final static Progress progress = new Progress();

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation(value = "创建脚本数据")
    public ResponseBase addScript(@RequestBody ScriptManageParam param,
                                  HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.addScript(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/editor")
    @ResponseBody
    @ApiOperation(value = "修改脚本数据")
    public ResponseBase updateScript(@RequestBody ScriptManageParam param,
                                     HttpServletRequest request,
                                     RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.updateScript(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除脚本数据")
    public ResponseBase deleteScript(@RequestBody ScriptManageParam param,
                                     HttpServletRequest request,
                                     RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.deleteScript(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation(value = "查看脚本数据")
    public ResponseBase browseScript(@RequestBody ScriptManageParam param,
                                     HttpServletRequest request,
                                     RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            if (param.getId() != null && param.getId() > 0) {
                reply = scriptManageService.getScriptInfo(param);
            } else {
                reply = scriptManageService.getScriptList(param);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/context")
    @ResponseBody
    @ApiOperation(value = "查看脚本数据")
    public ResponseBase context(@RequestBody ScriptManageParam param,
                                     HttpServletRequest request,
                                     RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.getConText(param);

        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }



    /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/perform")
    @ResponseBody
    @ApiOperation(value = "执行脚本")
    public ResponseBase perform(@RequestBody ScriptManageParam param,
                                HttpServletRequest request,
                                RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.execScript(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }



    @PostMapping("/showPassWord")
    @ResponseBody
    @ApiOperation(value = "执行脚本")
    public ResponseBase showPassWord(HttpServletRequest request,
                                RedirectAttributesModelMap model) {

        return setResultSuccess(Reply.ok(SHOW));
    }

    /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/getAnsibleIP")
    @ResponseBody
    @ApiOperation(value = "获取注册主机ansible")
    public ResponseBase getAnsibleIP(HttpServletRequest request,
                                RedirectAttributesModelMap model) {
        String []ipaddress =spiderPythonUrl.split(",");
        List<String> strings = Arrays.asList(ipaddress);

        return setResultSuccess(Reply.ok(strings));
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/getExecHistoryList")
    @ResponseBody
    @ApiOperation(value = "获取历史执行列表")
    public ResponseBase getExecHisToryList(@RequestBody ScriptManageParam param,
                                           HttpServletRequest request,
                                           RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.getExecHistoryList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/deleteExecHistoryList")
    @ResponseBody
    @ApiOperation(value = "获取历史执行列表")
    public ResponseBase deleteExecHistoryList(@RequestBody List<ScriptManageParam> param,
                                           HttpServletRequest request,
                                           RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.deleteExecHistoryList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }


    /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/getExecList")
    @ResponseBody
    @ApiOperation(value = "获取执行列表")
    public ResponseBase getExecList(@RequestBody ScriptManageParam param,
                                    HttpServletRequest request,
                                    RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.getExecList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/getExecDetail")
    @ResponseBody
    @ApiOperation(value = "获取执行详情")
    public ResponseBase getExecDetail(@RequestBody ScriptManageParam param,
                                      HttpServletRequest request,
                                      RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.getExecDetail(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/open/execReturn")
    @ResponseBody
    @ApiOperation(value = "执行脚本返回数据")
    public ResponseBase execReturn(@RequestBody HashMap json,
                                   HttpServletRequest request,
                                   RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.updateExecScript(json);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/getFuzzList")
    @ResponseBody
    @ApiOperation(value = "获取模糊查询数据")
    public ResponseBase getFuzzList(@RequestBody HashMap map) {
        Reply reply = null;
        try {
            reply = scriptManageService.getFuzzList(String.valueOf(map.get("type")));
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/account-manage/create")
    @ResponseBody
    @ApiOperation(value = "创建账户数据")
    public ResponseBase addAccount(@RequestBody ScriptAccountParam param,
                                   HttpServletRequest request,
                                   RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            if (DEVICE.equals(param.getSystemType())) {
                /*AddAccountManageParam accountParam = CopyUtils.copy(AddAccountManageParam.class, param);
                reply = accountManageService.insert(accountParam);*/
                param.setSystemType("device");
                param.setDevice(1);
                param.setAccountAlias(param.getAccountAlias());
                param.setAccount(param.getAccount());
                reply = scriptManageService.addAccount(param);
            } else {
                reply = scriptManageService.addAccount(param);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }


   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/account-manage/syncAccount")
    @ResponseBody
    @ApiOperation(value = "同步账号数据")
    public ResponseBase syncAccount(@RequestBody SynAccount param,
                                    HttpServletRequest request,
                                    RedirectAttributesModelMap model) {
        if (!progress.getIsOver()) {
            return setResultFail("同步已经执行", "同步已经执行");
        }
        //初始化
        List<ProxyInfo> proxyInfos = new ArrayList<>();
        GlobalUserInfo userInfo = userService.getGlobalUser();
        //验证登录
        if (param.getInstanceId().equals("") && !QiZhiApiUtil.getInstance().loginQiZhi(param.getAccount(), param.getPassword(), param.getUrl())) {
            return setResultFail("账号密码错误", "账号密码错误");
        }
        if (!param.getInstanceId().equals("") && !proxySearch.doProxySearch(Boolean.class, proxyInfos, param.getInstanceId(), "syncAdminAndPwdService", "login", param, null)) {
            return setResultFail("账号密码错误", "账号密码错误");
        }
        CompletableFuture f = CompletableFuture.runAsync(() -> {
            JSONArray jsonArray = null;
            try {
                if (param.getInstanceId().equals("")) {
                    jsonArray = QiZhiApiUtil.getInstance().getAllRootAndpwdAnd(param.getAccount(), param.getUrl(), param.getPassword(), progress);
                } else {
                    //初始化引擎id
                    progress.setInstanceId(param.getInstanceId());

                    String json = proxySearch.doProxySearch(String.class, proxyInfos, param.getInstanceId()
                            , "syncAdminAndPwdService", "syncAccount", param, null);
                    jsonArray = JSONArray.parseArray(json);
                }
            } catch (Exception e) {
                progress.init();
                throw e;
            }
            if (jsonArray.size() > 0) {
                //进行账号同步
                logger.info("可同步资产" + jsonArray.size());
                scriptManageService.syncAccount(jsonArray, userInfo,param);

                progress.init();
            } else {
                progress.init();
            }
        });
        return setResultSuccess(Reply.ok("成功"));
    }

    @PostMapping("/account-manage/exeportModel")
    @ResponseBody
    @ApiOperation(value = "导出模板")
    public void exeportModel(HttpServletResponse response) throws IOException {

        scriptManageService.exeportModel(response);

    }



    @PostMapping("/account-manage/exeportAccount")
    @ResponseBody
    @ApiOperation(value = "导入账号密码资产")
    public ResponseBase exeportAccount(@RequestBody MultipartFile file,
                             HttpServletResponse response,
                             RedirectAttributesModelMap model) throws IOException {
        GlobalUserInfo userInfo = userService.getGlobalUser();
        scriptManageService.exeportAccount(file,response,userInfo);
        return setResultSuccess(Reply.ok("导入成功"));
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/account-manage/getProgress")
    @ResponseBody
    @ApiOperation(value = "获取进度条")
    public ResponseBase getProgress() {
        if (progress.getInstanceId().equals("0")) {
            return setResultSuccess(progress);
        } else {
            try {
                List<ProxyInfo> proxyInfos = new ArrayList<>();
                String progressString = proxySearch.doProxySearch(String.class, proxyInfos, progress.getInstanceId()
                        , "syncAdminAndPwdService", "getProgress", null, null);
                JSONObject jsonObject = JSONObject.parseObject(progressString);
                progress.setPercentage(jsonObject.getDouble("percentage"));
                progress.setIsOver(jsonObject.getBoolean("isOver"));
                if (jsonObject.getBoolean("isOver")) {
                    progress.init();
                }
            } catch (Exception e) {
                setResultFail("对不起！链接引擎失效", null);
                progress.init();
            }

        }
        return setResultSuccess(progress);
    }


   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/account-manage/editor")
    @ResponseBody
    @ApiOperation(value = "修改账户数据")
    public ResponseBase updateAccount(@RequestBody ScriptAccountParam param,
                                      HttpServletRequest request,
                                      RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            if (DEVICE.equals(param.getSystemType())) {
                /*AddAccountManageParam accountParam = CopyUtils.copy(AddAccountManageParam.class, param);
                reply = accountManageService.update(accountParam);*/
                param.setSystemType("device");
                param.setDevice(1);
                param.setAccountAlias(param.getAccount());
                param.setAccount(param.getUsername());
                reply = scriptManageService.updateAccount(param);
            } else {
                reply = scriptManageService.updateAccount(param);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/account-manage/delete")
    @ResponseBody
    @ApiOperation(value = "删除账户数据")
    public ResponseBase deleteScript(@RequestBody List<ScriptAccountParam> list,
                                     HttpServletRequest request,
                                     RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            for (ScriptAccountParam param : list) {
                if (DEVICE.equals(param.getSystemType())) {
                   /* reply = accountManageService.delete(Arrays.asList(param.getId()));*/
                    param.setIds(Arrays.asList(param.getId()));
                    reply = scriptManageService.deleteAccount(param);
                } else {
                    param.setIds(Arrays.asList(param.getId()));
                    reply = scriptManageService.deleteAccount(param);
                }
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/account-manage/browse")
    @ResponseBody
    @ApiOperation(value = "查看账户数据")
    public ResponseBase browseAccount(@RequestBody ScriptAccountParam param,
                                      HttpServletRequest request,
                                      RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            if (param.getId() != null && param.getId() > 0) {
                if (DEVICE.equals(param.getSystemType())) {
                    reply = scriptManageService.browseAccount(param);
                } else {
                    reply = scriptManageService.browseAccount(param);
                }
            } else {
                reply = scriptManageService.getAccountList(param);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/account-manage/drop/browse")
    @ResponseBody
    @ApiOperation(value = "查看下拉账户数据")
    public ResponseBase browseDropAccount(@RequestBody ScriptAccountParam param,
                                          HttpServletRequest request,
                                          RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.getAccountDropList(param); 
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/auto-manage/distributeFile")
    @ResponseBody
    @ApiOperation(value = "分发文件")
    public ResponseBase distributeFile(@RequestBody FileTransParam param,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.distributeFile(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/homework/create")
    @ResponseBody
    @ApiOperation(value = "创建作业")
    public ResponseBase homeworkCreate(@RequestBody HomeworkParam param,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.addHomework(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/homework/delete")
    @ResponseBody
    @ApiOperation(value = "删除作业")
    public ResponseBase homeworkDelete(@RequestBody HomeworkParam param,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.deleteHomework(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/homework/editor")
    @ResponseBody
    @ApiOperation(value = "更改作业")
    public ResponseBase homeworkEditor(@RequestBody HomeworkParam param,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.updateHomework(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/homework/browse")
    @ResponseBody
    @ApiOperation(value = "查询作业")
    public ResponseBase homeworkBrowse(@RequestBody HomeworkParam param,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            if (param.getId() != null && param.getId() > 0) {
                reply = scriptManageService.browseHomework(param);
            } else {
                reply = scriptManageService.browseHomeworkList(param);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/homework/step/browse")
    @ResponseBody
    @ApiOperation(value = "查询作业步骤详情")
    public ResponseBase homeworkStepBrowse(@RequestBody HomeworkParam param,
                                           HttpServletRequest request,
                                           RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.browseHomeworkStep(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

    /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/accountBase/account/create")
    @ResponseBody
    @ApiOperation(value = "创建自动化资产绑定账号")
    public ResponseBase accountCreate(@RequestBody CreateAssets param,
                                           HttpServletRequest request,
                                           RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.accountCreate(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }


    /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/accountBase/account/editor")
    @ResponseBody
    @ApiOperation(value = "修改自动化账号")
    public ResponseBase editorAccount(@RequestBody CreateAssets param,
                                      HttpServletRequest request,
                                      RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.editorAccount(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/homework/perform")
    @ResponseBody
    @ApiOperation(value = "执行作业")
    public ResponseBase homeworkPerform(@RequestBody HomeworkParam param,
                                        HttpServletRequest request,
                                        RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.performHomework(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/homework/his/browse")
    @ResponseBody
    @ApiOperation(value = "查看执行结果")
    public ResponseBase HisBrowse(@RequestBody HomeworkHis param,
                                        HttpServletRequest request,
                                        RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.HisBrowse(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/homework/his/browse/check")
    @ResponseBody
    @ApiOperation(value = "查看执行结果选项")
    public ResponseBase HisBrowseCheck(@RequestBody HomeworkHis param,
                                  HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.HisBrowseCheck(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/homework/his/browse/downById")
    @ResponseBody
    @ApiOperation(value = "下载配置单个传id")
    public ResponseBase downById(@RequestBody HomeworkHis param,
                                       HttpServletRequest request,HttpServletResponse response,
                                 RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.downById(param,response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/homework/his/browse/downZip")
    @ResponseBody
    @ApiOperation(value = "下载配置批量文件")
    public void downZip(@RequestBody HomeworkHis param,
                                 HttpServletRequest request,HttpServletResponse response,
                                 RedirectAttributesModelMap model) {
         scriptManageService.downZip(param,response);

    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/homework/rePerform")
    @ResponseBody
    @ApiOperation(value = "重新执行作业")
    public ResponseBase homeworkRePerform(@RequestBody HomeworkParam param,
                                          HttpServletRequest request,
                                          RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.rePerformHomework(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/homework/getExecResult")
    @ResponseBody
    @ApiOperation(value = "获取执行作业进度")
    public ResponseBase getExecResult(@RequestBody HomeworkParam param,
                                      HttpServletRequest request,
                                      RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.getExecResult(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/open/homework/execReturn")
    @ResponseBody
    @ApiOperation(value = "执行脚本返回数据")
    public ResponseBase homeworkExecReturn(@RequestBody HashMap json,
                                           HttpServletRequest request,
                                           RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = scriptManageService.updateHomeworkExecScript(json);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("脚本数据失败", "");
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail("脚本数据失败", "");
        }
        return setResultSuccess(reply);
    }

   /* @MwPermit(moduleName = "auto-manage")*/
    @PostMapping("/account-manage/removeAssets")
    @ResponseBody
    @ApiOperation(value = "资产移除账号")
    public ResponseBase removeAssets(@RequestBody List<Integer> ids) {
        scriptManageService.removAssets(ids);

        return setResultSuccess("ok");
    }

    @PostMapping("/accountManage/alert/browse")
    @ResponseBody
    @ApiOperation(value = "告警触发查询")
    public Reply alertBrowse(@RequestBody MwHomeworkAlert mwHomeworkAlert) {
        Reply reply =  scriptManageService.getAllAlertBrowse(mwHomeworkAlert);

        return reply;
    }

    @PostMapping("/accountManage/alert/create")
    @ResponseBody
    @ApiOperation(value = "告警触发新增")
    public Reply alertCreate(@RequestBody MwHomeworkAlert mwHomeworkAlert) {
        Reply reply =  scriptManageService.alertCreate(mwHomeworkAlert);

        return reply;
    }


    @PostMapping("/accountManage/alert/editor")
    @ResponseBody
    @ApiOperation(value = "告警触发编辑")
    public Reply alertEditor(@RequestBody MwHomeworkAlert mwHomeworkAlert) {
        Reply reply =  scriptManageService.alertEditor(mwHomeworkAlert);
        return reply;
    }

    @PostMapping("/accountManage/alert/delete")
    @ResponseBody
    @ApiOperation(value = "删除告警触发")
    public Reply alertDelete(@RequestBody List<MwHomeworkAlert> mwHomeworkAlerts) {
        Reply reply =  scriptManageService.alertDelete(mwHomeworkAlerts);
        return reply;
    }

    @PostMapping("/accountManage/alert/homework")
    @ResponseBody
    @ApiOperation(value = "查询告警触发作业")
    public Reply alertHomework(@RequestBody ListParamString listParamString) {
        Reply reply =  scriptManageService.alertHomework(listParamString.getAlertExeHomework());
        return reply;
    }

    @PostMapping("/alert/homeworkList")
    @ResponseBody
    @ApiOperation(value = "触发结果")
    public Reply homeworkList(@RequestBody ListParamString listParamString) {
        Reply reply =  scriptManageService.homeworkList(listParamString);
        return reply;
    }

}
