package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.service.assets.param.AssetsSearchTermFuzzyParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.TPServer.dto.AddOrUpdateTPServerParam;
import cn.mw.monitor.TPServer.dto.DeleteTPServerParam;
import cn.mw.monitor.TPServer.dto.QueryTPServerParam;
import cn.mw.monitor.TPServer.service.MwTPServerService;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.tpserver.api.MWTPServerProxyListener;
import cn.mw.monitor.service.tpserver.model.ProxyServerInfo;
import cn.mw.monitor.service.tpserver.model.TPResult;
import cn.mwpaas.common.utils.StringUtils;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

/**
 * @author syt
 * @Date 2020/10/30 17:22
 * @Version 1.0
 */
@RequestMapping("/mwapi")
@Controller
@Slf4j
public class MwTPServerController extends BaseApiService {

    @Autowired
    MwTPServerService mwTPServerService;

    @Autowired
    MWTPServerProxyListener mwtpServerProxyListener;

    /**
     * 新增TPServer
     */
    @MwPermit(moduleName = "sys_manage")
    @PostMapping("/TPServer/create")
    @ResponseBody
    public ResponseBase addEngineMange(@RequestBody AddOrUpdateTPServerParam aParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            if (Strings.isNullOrEmpty(aParam.getMonitoringServerUrl())) {
                StringBuffer sUrl = new StringBuffer();
                sUrl.append(aParam.getProtocol()).append("://")
                        .append(aParam.getMonitoringServerIp())
                        .append(":")
                        .append(aParam.getPort())
                        .append("/api_jsonrpc.php");
                aParam.setMonitoringServerUrl(sUrl.toString());
            }
            ProxyServerInfo proxyServerInfo = new ProxyServerInfo(aParam.getMonitoringServerType()
                    , aParam.getMonitoringServerVersion(), aParam.getMonitoringServerUser()
                    , aParam.getMonitoringServerPassword(), aParam.getMonitoringServerUrl());

            boolean isOk = mwtpServerProxyListener.check(proxyServerInfo);
            if (!isOk) {
                reply = Reply.fail(ErrorConstant.ZABBIX_SERVER_INSERT_MSG_311003 + proxyServerInfo.toString(), proxyServerInfo);
                return setResultFail(reply.getMsg(), reply.getData());
            }

            reply = mwTPServerService.insert(aParam);

            TPResult ret = mwtpServerProxyListener.refreshServers();
            if (!ret.isSuccess()) {
                reply = Reply.fail(ErrorConstant.ZABBIX_SERVER_UPDATE_CODE_311002, ret.getMessage());
                return setResultFail(reply.getMsg(), reply.getData());
            }

        } catch (Throwable e) {
            log.error("addEngineMange", e);
            reply = Reply.fail(ErrorConstant.ZABBIX_SERVER_INSERT_CODE_311003, ErrorConstant.ZABBIX_SERVER_INSERT_MSG_311003);
            return setResultFail(reply.getMsg(), reply.getData());
        }
        return setResultSuccess(reply);
    }

    /**
     * 删除TPServer
     */
    @MwPermit(moduleName = "sys_manage")
    @PostMapping("/TPServer/delete")
    @ResponseBody
    public ResponseBase deleteEnigneManage(@RequestBody DeleteTPServerParam dParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwTPServerService.delete(dParam.getIds());

            TPResult ret = mwtpServerProxyListener.refreshServers();
            if (!ret.isSuccess()) {
                reply = Reply.fail(ErrorConstant.ZABBIX_SERVER_UPDATE_CODE_311002, ret.getMessage());
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteEnigneManage", e);
            reply = Reply.fail(ErrorConstant.ZABBIX_SERVER_DELETE_CODE_311004, ErrorConstant.ZABBIX_SERVER_DELETE_MSG_311004 + e.getMessage());
            return setResultFail(reply.getMsg(), reply.getData());
        }
        return setResultSuccess(reply);
    }

    /**
     * 修改TPServer
     */
    @MwPermit(moduleName = "sys_manage")
    @PostMapping("/TPServer/editor")
    @ResponseBody
    public ResponseBase updateEngineMange(@RequestBody AddOrUpdateTPServerParam auParam,
                                          HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            if (Strings.isNullOrEmpty(auParam.getMonitoringServerUrl())) {
                StringBuffer sUrl = new StringBuffer();
                sUrl.append(auParam.getProtocol()).append("://")
                        .append(auParam.getMonitoringServerIp())
                        .append(":")
                        .append(auParam.getPort())
                        .append("/api_jsonrpc.php");
                auParam.setMonitoringServerUrl(sUrl.toString());
            }
            ProxyServerInfo proxyServerInfo = new ProxyServerInfo(auParam.getMonitoringServerType()
                    , auParam.getMonitoringServerVersion(), auParam.getMonitoringServerUser()
                    , auParam.getMonitoringServerPassword(), auParam.getMonitoringServerUrl());

            boolean isOk = mwtpServerProxyListener.check(proxyServerInfo);
            if (!isOk) {
                reply = Reply.fail(ErrorConstant.ZABBIX_SERVER_UPDATE_MSG_311002 + proxyServerInfo.toString(), proxyServerInfo);
                return setResultFail(reply.getMsg(), reply.getData());
            }

            reply = mwTPServerService.update(auParam);

            TPResult ret = mwtpServerProxyListener.refreshServers();
            if (!ret.isSuccess()) {
                reply = Reply.fail(ErrorConstant.ZABBIX_SERVER_UPDATE_CODE_311002, ret.getMessage());
                return setResultFail(reply.getMsg(), reply.getData());
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateEngineMange", e);
            reply = Reply.fail(ErrorConstant.ZABBIX_SERVER_UPDATE_CODE_311002, ErrorConstant.ZABBIX_SERVER_UPDATE_MSG_311002);
            return setResultFail(reply.getMsg(), reply.getData());
        }
        return setResultSuccess(reply);
    }

    /**
     * 分页查询TPServer
     */
    @MwPermit(moduleName = "sys_manage")
    @PostMapping("/TPServer/browse")
    @ResponseBody
    public ResponseBase browseEngineMange(@RequestBody QueryTPServerParam qParam,
                                          HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTPServerService.selectList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), qParam);
        }
        return setResultSuccess(reply);
    }

    /**
     * TPServer下拉框查询
     */
    @MwPermit(moduleName = "sys_manage")
    @GetMapping("/TPServer/dropdown/browse")
    @ResponseBody
    public ResponseBase engineManageDropdownBrowse(@PathParam("selectFlag") boolean selectFlag, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTPServerService.selectDropdownListByType(selectFlag);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * TPServer子页面查询
     */
    @MwPermit(moduleName = "sys_manage")
    @PostMapping("/TPServer/popup/browse")
    @ResponseBody
    public ResponseBase engineManagePopupBrowse(@RequestBody QueryTPServerParam qParam,
                                                HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTPServerService.selectById(qParam.getId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * TPServer子页面查询
     */
    @MwPermit(moduleName = "sys_manage")
    @GetMapping("/TPServer/getZabbixTemplateSession")
    @ResponseBody
    public ResponseBase getZabbixTemplateSession(@RequestParam("hostIp") String hostIp,HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTPServerService.getZabbixTemplateSession(hostIp);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    /**
     * TPServer刷新
     */
    @MwPermit(moduleName = "sys_manage")
    @PostMapping("/TPServer/refresh")
    @ResponseBody
    public ResponseBase tpServerRefresh(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            TPResult ret = mwtpServerProxyListener.refreshServers();
            if (ret.isSuccess()) {
                reply = Reply.ok("刷新成功");
            } else {
                reply = Reply.fail(ErrorConstant.ZABBIX_SERVER_UPDATE_MSG_311002, ret.getMessage());
            }
        } catch (Throwable e) {
            log.error("tpServerRefresh", e);
            reply = Reply.fail(ErrorConstant.ZABBIX_SERVER_UPDATE_MSG_311002, "tpServerRefresh error");
            return setResultFail(reply.getMsg(), reply.getData());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "sys_manage")
    @PostMapping("/TPServer/fuzzSearchAllFiled/browse")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData(@RequestBody AssetsSearchTermFuzzyParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTPServerService.fuzzSearchAllFiledData(param.getValue());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), "模糊查询所有字段资数据失败");
        }

        return setResultSuccess(reply);
    }
}
