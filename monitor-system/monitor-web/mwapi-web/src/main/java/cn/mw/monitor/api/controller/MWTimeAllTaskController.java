package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.timetask.component.CronUtil;
import cn.mw.monitor.timetask.entity.*;
import cn.mw.monitor.timetask.service.MwNcmTimetaskTimePlanService;
import cn.mw.monitor.timetask.service.MwTimeTaskService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
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
@Api(value = "任务中心",tags = "任务中心")
public class MWTimeAllTaskController extends BaseApiService {

    @Autowired
    private MwTimeTaskService mwTimeTaskService;

    @Autowired
    private MwNcmTimetaskTimePlanService mwTimeTaskTimePlanService;


    @PostMapping("/timeAllTask/getTypeList")
    @ResponseBody
    @ApiOperation(value = "定时任务查询任务类型")
    public ResponseBase getTypeList(@RequestBody TimeTaskBase param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.getTypeList(param.getType());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("定时任务查询报错",null);
            }
        } catch (Throwable e) {
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timeAllTask/browseHis")
    @ResponseBody
    @ApiOperation(value = "定时任务查询历史")
    public ResponseBase browseHis(@RequestBody QueryTimeTaskParam param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.selectListHis(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("定时任务查询报错",null);
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timeAllTask/deleteHis")
    @ResponseBody
    @ApiOperation(value = "定时任务删除历史")
    public ResponseBase deleteHis(@RequestBody List<MwTimeTaskDownloadHis> param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.deleteHis(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("定时任务查询报错",null);
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timeAllTask/browseHisFile")
    @ResponseBody
    @ApiOperation(value = "定时任务查询历文件")
    public ResponseBase browseHisFile(@RequestBody MwTimeTaskDownloadHis param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.browseHisFile(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("定时任务查询报错",null);
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timeAllTask/downHisFile")
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

    @PostMapping("/timeAllTask/editorBrowse")
    @ResponseBody
    @ApiOperation(value = "定时任务编辑前查询")
    public ResponseBase editorBrowse(@RequestBody AddTimeTaskParam param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.editorBrowse(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("定时任务查询报错",null);
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timeAllTask/browse")
    @ResponseBody
    @ApiOperation(value = "定时任务查询")
    public ResponseBase browse(@RequestBody QueryTimeTaskParam param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.selectList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("定时任务查询报错",null);
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    /*    @PostMapping("/timeAllTask/delete")*/
  /*  @ResponseBody
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
    }*/
/*
    @PostMapping("/timeAllTask/editor")
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
    }*/

   /* @ApiOperation(value = "新增定时任务")
    @PostMapping("/timeAllTask/create")
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
    }*/

   /* @ApiOperation(value = "新增定时任务")
    @PostMapping("/timeAllTask/create")
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
    }*/


    @ApiOperation(value = "查询规定时间任务")
    @PostMapping("/timeAllTask/time/browse")
    @ResponseBody
    public ResponseBase timeBrowse(@Validated @RequestBody MwNcmTimetaskTimePlan param,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        PageHelper.startPage(param.getPageNumber(), param.getPageSize());
        List<MwNcmTimetaskTimePlan> plans = mwTimeTaskTimePlanService.listAll();
        PageInfo pageInfo = new PageInfo<>(plans);
        pageInfo.setList(plans);
        //刷新定时器
        reply =Reply.ok(pageInfo);
        if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
            return setResultFail("定时任务查询报错",null);
        }

        return setResultSuccess(reply);
    }


    @ApiOperation(value = "新增规定时间")
    @PostMapping("/timeAllTask/time/create")
    @ResponseBody
    public ResponseBase timeCreate(@Validated @RequestBody MwNcmTimetaskTimePlan param,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        String hms = param.getTimeHms();
        String cron = "";
        if("H".equals(param.getTimeType())){
            String[] hmss = hms.split(":");
            cron = reduceCap(hmss[2])+" "+reduceCap(hmss[1])+" "+reduceCap(hmss[0])+" * * ?";
        }else if("W".equals(param.getTimeType())){
            String[] hmss = hms.split(":");
            cron = reduceCap(hmss[2])+" "+reduceCap(hmss[1])+" "+reduceCap(hmss[0])+" ? * "+resolveWeek(param.getTimeChoice());
        }else if("M".equals(param.getTimeType())){
            String[] hmss = hms.split(":");
            cron = reduceCap(hmss[2])+" "+reduceCap(hmss[1])+" "+reduceCap(hmss[0])+" "+param.getTimeChoice()+" * ?";
        }
        if("S".equals(param.getTimeType())){
            cron = param.getTimeCron();
        }
        param.setTimeCron(cron);
        boolean isTrue = CronSequenceGenerator.isValidExpression(cron);
        if(!isTrue){
            return setResultFail("请输入正确的定时器时间规则"+cron,param);
        }
        param.setTimeCronChinese(CronUtil.translateToChinese(param.getTimeCron()));
        mwTimeTaskTimePlanService.insertTaskTime(param);
        return setResultSuccess(Reply.ok());
    }


    @ApiOperation(value = "删除规定时间")
    @PostMapping("/timeAllTask/time/delete")
    @ResponseBody
    public ResponseBase timeDelete(@Validated @RequestBody DeleteTimeTask param,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        List<Integer> ids  = param.getId();
        for (Integer id:ids) {
            mwTimeTaskService.action(id,"delete");
            mwTimeTaskTimePlanService.deleteById(id);
        }


        return setResultSuccess(Reply.ok());
    }

    @ApiOperation(value = "修改规定时间")
    @PostMapping("/timeAllTask/time/update")
    @ResponseBody
    public ResponseBase timeUpdate(@Validated @RequestBody MwNcmTimetaskTimePlan param,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        String hms = param.getTimeHms();
        String cron = "";
        if("H".equals(param.getTimeType())){
            String[] hmss = hms.split(":");
            cron = reduceCap(hmss[2])+" "+reduceCap(hmss[1])+" "+reduceCap(hmss[0])+" * * ?";
        }else if("W".equals(param.getTimeType())){
            String[] hmss = hms.split(":");
            cron = reduceCap(hmss[2])+" "+reduceCap(hmss[1])+" "+reduceCap(hmss[0])+" ? * "+resolveWeek(param.getTimeChoice());
        }else if("M".equals(param.getTimeType())){
            String[] hmss = hms.split(":");
            cron = reduceCap(hmss[2])+" "+reduceCap(hmss[1])+" "+reduceCap(hmss[0])+" "+param.getTimeChoice()+" * ?";
        }
        if("S".equals(param.getTimeType())){
            cron = param.getTimeCron();
        }
        param.setTimeCron(cron);
        boolean isTrue = CronSequenceGenerator.isValidExpression(cron);
        if(!isTrue){
            return setResultFail("请输入正确的定时器时间规则"+cron,param);
        }
        param.setTimeCronChinese(CronUtil.translateToChinese(param.getTimeCron()));
        mwTimeTaskTimePlanService.updateByIdMy(param);
        mwTimeTaskService.action(param.getId(),"update");
        return setResultSuccess(Reply.ok());
    }

    public String resolveWeek(String s){
        Integer week = Integer.parseInt(s);
        Integer trueWeek = week + 1;
        if(trueWeek==8){
            trueWeek = 1;
            return String.valueOf(trueWeek);
        }else {
            return String.valueOf(trueWeek);
        }
    }

    /* *//**
     * TPServer刷新
     *//*
    @ApiOperation(value = "定时任务刷新")
    @PostMapping("/timeAllTask/refresh")
    @ResponseBody
    public ResponseBase timeServerRefresh(@RequestBody AddTimeTaskParam param, HttpServletRequest request, RedirectAttributesModelMap model) {
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
    }*/


    @PostMapping("/timeAllTask/getTree")
    @ResponseBody
    @ApiOperation(value = "获取树状结构数据")
    public ResponseBase getTree(@RequestBody TimeTaskBase param) {
        Reply reply;
        try {
            reply = mwTimeTaskService.getTree(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("定时任务查询报错",null);
            }
        } catch (Throwable e) {
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }




    @PostMapping("/timeAllTask/transferStation")
    @ResponseBody
    @ApiOperation(value = "定时任务中转站（绑定模块与对象）")
    public ResponseBase transferStation(@RequestBody Transfer transfer) {
        Reply reply;
        try {
            reply = mwTimeTaskService.getTransferStationTree(transfer);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/timeAllTask/getHistory")
    @ResponseBody
    @ApiOperation(value = "获取历史执行")
    public ResponseBase<Reply<PageInfo<TimeTaskRresult>>> getHistory(@RequestBody Transfer transfer) {
        Reply reply;
        try {
            reply = mwTimeTaskService.getHistory(transfer);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timeAllTask/getTimeOrderId")
    @ResponseBody
    @ApiOperation(value = "获取定时任务绑定Id")
    public ResponseBase getTimeOrderId() {
        Reply reply;
        try {

        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(null);
    }


    @PostMapping("/timeAllTask/runNewTime")
    @ResponseBody
    @ApiOperation(value = "立即执行定时任务")
    public ResponseBase runNewTime(@RequestBody Transfer transfer) {
        Reply reply;
        try {
            transfer.setActionId(-1);
            mwTimeTaskService.runNewTime(transfer);
            reply = Reply.ok("已经执行了");
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("定时任务查询报错",null);
        }
        return setResultSuccess(null);
    }

    @PostMapping("/timeAllTask/timeAllTaskCreate")
    @ResponseBody
    @ApiOperation(value = "最新系统定时任务新增或编辑")
    public ResponseBase timeAllTaskCreate(@RequestBody  NewTimeTask newTimeTask) {
        Reply reply;
        try {
            reply = mwTimeTaskService.createTimeAllTask(newTimeTask);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("定时任务报错",null);
        }
        return setResultSuccess(null);
    }

    @PostMapping("/timeAllTask/timeAllTaskBrows")
    @ResponseBody
    @ApiOperation(value = "最新定时任务查询")
    public ResponseBase timeAllTaskBrows(@RequestBody NewTimeTask newTimeTask) {
        Reply reply;
        try {
            reply = mwTimeTaskService.timeAllTaskBrows(newTimeTask);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("定时任务报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/timeAllTask/timeAllTaskDelete")
    @ResponseBody
    @ApiOperation(value = "最新定时任务删除")
    public ResponseBase timeAllTaskDelete(@RequestBody DeleteTimeTask newTimeTask) {
        Reply reply;
        try {
            reply = mwTimeTaskService.timeAllTaskDelete(newTimeTask);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("定时任务报错",null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/timeAllTask/timeAllTaskHisDelete")
    @ResponseBody
    @ApiOperation(value = "定时任务历史删除")
    public ResponseBase timeAllTaskHisDelete(@RequestBody DeleteTimeTask newTimeTask) {
        Reply reply;
        try {
            reply = mwTimeTaskService.timeAllTaskHisDelete(newTimeTask);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("定时任务报错",null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/timeAllTask/timeAllTaskObjectBrows")
    @ResponseBody
    @ApiOperation(value = "最新定时任务对象查询")
    public ResponseBase timeAllTaskObjectBrows(@RequestBody NewTimeTask newTimeTask) {
        Reply reply;
        try {
            reply = mwTimeTaskService.timeAllTaskObjectBrows(newTimeTask);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("定时任务报错",null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/timeAllTask/timeAllTaskObjectEditorBrow")
    @ResponseBody
    @ApiOperation(value = "最新定时任务编辑查询")
    public ResponseBase timeAllTaskObjectEditorBrow(@RequestBody NewTimeTask newTimeTask) {
        Reply reply;
        try {
            reply = mwTimeTaskService.timeAllTaskObjectEditorBrow(newTimeTask);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("定时任务报错",null);
        }
        return setResultSuccess(reply);
    }

    /*@PostMapping("/timeAllTask/timeAllTaskObjectBrows")
    @ResponseBody
    @ApiOperation(value = "最新定时任务查询")
    public ResponseBase timeAllTaskObjectBrows(@RequestBody NewTimeTask newTimeTask) {
        Reply reply;
        try {
            reply = mwTimeTaskService.timeAllTaskObjectBrows(newTimeTask);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }*/


    public String reduceCap(String s){
        char[] c = s.toCharArray();
        if(c[0]=='0'){
            return String.valueOf(c[1]);
        }else {
            return String.valueOf(c);
        }
    }
}
