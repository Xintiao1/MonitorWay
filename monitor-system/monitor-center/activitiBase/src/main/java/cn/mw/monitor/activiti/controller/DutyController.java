package cn.mw.monitor.activiti.controller;

import cn.mw.monitor.activiti.param.*;
import cn.mw.monitor.activiti.service.DutyManageService;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;


/**
 * @author
 * @Date
 * @Version
 */
@RequestMapping("/mwapi/duty")
@Controller
@Api(value = "排班管理", tags = "排班管理")
@Slf4j
public class DutyController extends BaseApiService {

    @Autowired
    private DutyManageService dutyManageService;

    @PostMapping("/create")
    @ResponseBody
    @ApiOperation(value = "添加值班人员")
    public ResponseBase  create(@RequestBody List<DutyManageParam> params){
        Reply reply = null;
        try {
            reply = dutyManageService.createDuty(params);;
        } catch (Exception e) {
            log.error("添加值班人员失败:{}", e);
            return setResultFail(reply.getMsg(), params);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation(value = "查询值班人员")
    public ResponseBase browse(@RequestBody DutyManageParam param){
        Reply reply = null;
        try {
            reply = dutyManageService.queryDuty(param);
        }catch (Exception e){
            log.error("查询值班人员失败:{}", e);
            return setResultFail(reply.getMsg(), param);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("show/browse")
    @ResponseBody
    @ApiOperation(value = "值班人员展示")
    public ResponseBase showBrowse(@RequestBody DutyManageParam param){
        Reply reply = null;
        try {
            reply = dutyManageService.showDuty(param);
        }catch (Exception e){
            log.error("查询值班人员失败:{}", e);
            return setResultFail(reply.getMsg(), param);
        }
        return setResultSuccess(reply);
    }

    @GetMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除值班人员")
    public ResponseBase delete(@RequestParam String id){
        Reply reply = null;
        try{
            reply = dutyManageService.deleteDuty(id);
        }catch (Exception e){
            log.error("删除值班人员失败:{}", e);
            return setResultFail(reply.getMsg(), id);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("shift/create")
    @ResponseBody
    @ApiOperation(value = "添加班次")
    public ResponseBase  shiftCreate(@RequestBody DutyShiftParam param){
        Reply reply = null;
        try{
            reply = dutyManageService.shiftCreate(param);;
        }catch (Exception e){
            log.error("添加班次失败:{}", e);
            return setResultFail(reply.getMsg(), param);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("shift/browse")
    @ResponseBody
    @ApiOperation(value = "查询班次信息")
    public ResponseBase shiftBrowse(@RequestBody DutyShiftParam param){
        Reply reply = null;
        try{
            reply = dutyManageService.shiftBrowse(param);
        }catch (Exception e){
            log.error("查询班次信息失败:{}", e);
            return setResultFail(reply.getMsg(), param);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("shift/delete")
    @ResponseBody
    @ApiOperation(value = "删除班次")
    public ResponseBase shiftDelete(@RequestBody DutyShiftParam param){
        Reply reply = null;
        try{
            reply = dutyManageService.shiftDelete(param.getIds());
        }catch (Exception e){
            log.error("删除班次失败:{}", e);
            return setResultFail(reply.getMsg(), param);
        }
        return setResultSuccess(reply);
    }

    @GetMapping("shift/editor/before")
    @ResponseBody
    @ApiOperation(value = "班次编辑前回显")
    public ResponseBase shiftEditorBefore(@RequestParam String id){
        Reply reply = null;
        try{
            reply = dutyManageService.shiftEditorBefore(id);
        }catch (Exception e){
            log.error("班次编辑前回显失败:{}", e);
            return setResultFail(reply.getMsg(), id);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("shift/editor")
    @ResponseBody
    @ApiOperation(value = "班次编辑")
    public ResponseBase shiftEditor(@RequestBody DutyShiftParam param){
        Reply reply = null;
        try{
            reply = dutyManageService.shiftEditor(param);
        }catch (Exception e){
            log.error("班次编辑失败:{}", e);
            return setResultFail(reply.getMsg(), param);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("shift/drop/browse")
    @ResponseBody
    @ApiOperation(value = "班次信息下拉选择")
    public ResponseBase dropBrowse(@RequestBody DutyShiftParam param){
        Reply reply = null;
        try{
            reply = dutyManageService.dropBrowse(param);
        }catch (Exception e){
            log.error("查询班次信息失败:{}", e);
            return setResultFail(reply.getMsg(), param);
        }
        return setResultSuccess(reply);
    }


}
