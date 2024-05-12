package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.timetask.entity.*;
import cn.mw.monitor.timetask.service.MwTimeTaskService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "定时任务")
public class MWTimeTaskController extends BaseApiService {

    @Autowired
    private MwTimeTaskService mwTimeTaskService;

    @PostMapping("/timetask/getTypeList")
    @ResponseBody
    @ApiOperation(value = "定时任务查询任务类型")
    public ResponseBase getTypeList() {
        Reply reply;
        try {
            reply = mwTimeTaskService.getTypeList(0);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("定时任务报错",null);
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timetask/browseHis")
    @ResponseBody
    @ApiOperation(value = "定时任务查询历史")
    public ResponseBase browseHis(@RequestBody QueryTimeTaskParam param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.selectListHis(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timetask/deleteHis")
    @ResponseBody
    @ApiOperation(value = "定时任务删除历史")
    public ResponseBase deleteHis(@RequestBody List<MwTimeTaskDownloadHis> param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.deleteHis(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timetask/browseHisFile")
    @ResponseBody
    @ApiOperation(value = "定时任务查询历文件")
    public ResponseBase browseHisFile(@RequestBody MwTimeTaskDownloadHis param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.browseHisFile(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timetask/downHisFile")
    @ResponseBody
    @ApiOperation(value = "定时任务下载历史文件")
    public ResponseBase downHisFile(@RequestBody MwTimeTaskDownloadHis param,HttpServletResponse response) {
        Reply reply = null;
        try {
            mwTimeTaskService.downHisFile(param,response);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timetask/editorBrowse")
    @ResponseBody
    @ApiOperation(value = "定时任务编辑前查询")
    public ResponseBase editorBrowse(@RequestBody AddTimeTaskParam param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.editorBrowse(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timetask/browse")
    @ResponseBody
    @ApiOperation(value = "定时任务查询")
    public ResponseBase browse(@RequestBody QueryTimeTaskParam param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.selectList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

  /*  @PostMapping("/timetask/delete")
    @ResponseBody
    @ApiOperation(value = "定时任务删除")
    public ResponseBase delete(@RequestBody List<AddTimeTaskParam> list) {
        Reply reply;
        try {
            reply = mwTimeTaskService.delete(list);

            //刷新定时器
            AddTimeTaskParam dels = new AddTimeTaskParam();
            dels.setDels(list);
            TimeProcessResult res = mwTimeTaskService.refreshServers(dels);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), list);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timetask/editor")
    @ResponseBody
    @ApiOperation(value = "定时任务修改")
    public ResponseBase editor(@Validated  @RequestBody AddTimeTaskParam param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.update(param);

            //刷新定时器
            TimeProcessResult res = mwTimeTaskService.refreshServers(param);
            log.info("res is:{}",res);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }
*/
  /*  @ApiOperation(value = "新增定时任务")
    @PostMapping("/timetask/create")
    @ResponseBody
    public ResponseBase create(@Validated @RequestBody AddTimeTaskParam param,
                            HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwTimeTaskService.insert(param);

            //刷新定时器
            TimeProcessResult res = mwTimeTaskService.refreshServers(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }
*/
/*    *
     * TPServer刷新*/

    @PostMapping("/TimeServer/refresh")
    @ResponseBody
    public ResponseBase timeServerRefresh(@RequestBody List<MwNcmTimetaskTimePlanRun> param, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            TimeProcessResult ret = mwTimeTaskService.refreshServers(param);
            if (ret.isSuccess()) {
                reply = Reply.ok("刷新成功");
            } else {
                reply = Reply.fail(ret.getMessage());
            }
        } catch (Throwable e) {
            log.error("timeServerRefresh", e);
            reply = Reply.fail("timeServerRefresh error");
            return setResultFail(reply.getMsg(), reply.getData());
        }
        return setResultSuccess(reply);
    }



}
