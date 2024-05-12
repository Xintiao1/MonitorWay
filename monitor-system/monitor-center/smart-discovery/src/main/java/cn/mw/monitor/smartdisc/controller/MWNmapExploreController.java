package cn.mw.monitor.smartdisc.controller;


import cn.mw.monitor.annotation.MwSysLog;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.exception.CheckInsertNmapTaskException;
import cn.mw.monitor.service.smartDiscovery.param.DeleteNmapTaskParam;
import cn.mw.monitor.service.smartDiscovery.param.QueryNmapTaskParam;
import cn.mw.monitor.service.smartDiscovery.api.MWNmapTaskService;
import cn.mw.monitor.service.smartDiscovery.param.AddUpdateNmapTaskParam;
import cn.mw.monitor.service.smartDiscovery.param.QueryNmapResultParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "Nmap扫描接口",tags = "Nmap扫描接口")
public class MWNmapExploreController extends BaseApiService {

    @Resource
    private MWNmapTaskService mwNmapTaskService;

    @ApiOperation(value="新增扫描任务")
    @MwSysLog("新增扫描任务")
    @PostMapping("/nmapTask/create")
    @ResponseBody
    public ResponseBase addNmapTask(@RequestBody AddUpdateNmapTaskParam param,
                                 HttpServletRequest request,
                                 RedirectAttributesModelMap model) {
        try {
            Reply reply = mwNmapTaskService.insert(param);
        }catch (CheckInsertNmapTaskException e) {
            return setResultFail(e.getMessage(),param);
        }catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(param);
    }

    /* *
     * NMAP扫描任务查询
     */
    @ApiOperation(value="NMAP扫描任务列表查询")
    @PostMapping(value = "/nmapTask/browse")
    @ResponseBody
    public ResponseBase browseNmapTaskList(@RequestBody QueryNmapTaskParam qParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwNmapTaskService.pageUser(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), qParam);
        }

        return setResultSuccess(reply);
    }

    @ApiOperation(value="查看扫描结果")
    @MwSysLog("查看扫描结果")
    @PostMapping("/nmapTask/nmapResultBrowse")
    @ResponseBody
    public ResponseBase browseNmapResult(@RequestBody QueryNmapResultParam param,
                                    HttpServletRequest request,
                                    RedirectAttributesModelMap model) {
        try {
            Reply reply = mwNmapTaskService.selectResult(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        }catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="查询任务详情")
    @MwSysLog("查询任务详情")
    @PostMapping("/nmapTask/nmapTaskDetails")
    @ResponseBody
    public ResponseBase browseNmapTaskDetails(@RequestBody QueryNmapResultParam param,
                                         HttpServletRequest request,
                                         RedirectAttributesModelMap model) {
        try {
            Reply reply = mwNmapTaskService.selectNmapTaskDetails(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        }catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
    }


    @ApiOperation(value="编辑扫描任务")
    @MwSysLog("编辑扫描任务")
    @PostMapping("/nmapTask/editor")
    @ResponseBody
    public ResponseBase updateNmapTask(@RequestBody AddUpdateNmapTaskParam param,
                                    HttpServletRequest request,
                                    RedirectAttributesModelMap model) {
        try {
            Reply reply = mwNmapTaskService.updateNmapTask(param);
        }catch (CheckInsertNmapTaskException e) {
            return setResultFail(e.getMessage(),param);
        }catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(param);
    }

    @ApiOperation(value="开启扫描任务")
    @MwSysLog("开启扫描任务")
    @PostMapping("/nmapTask/run")
    @ResponseBody
    public ResponseBase runNmapTask(@RequestBody QueryNmapResultParam param,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        try {
            Reply reply = mwNmapTaskService.runNmapTask(param);
        }catch (CheckInsertNmapTaskException e) {
            return setResultFail(e.getMessage(),param);
        }catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(param);
    }


    /**
     * NMAP任务删除
     */
    @ApiOperation(value="NMAP任务删除")
    @PostMapping("/nmapTask/delete")
    @ResponseBody
    public ResponseBase deleteUser(@RequestBody DeleteNmapTaskParam dParam,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        try {
            List<Integer> idList = dParam.getNmapTaskIdList();
            Reply reply = mwNmapTaskService.delete(idList);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), dParam);
        }

        return setResultSuccess(dParam);
    }

}
