package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.MwModelIdListParam;
import cn.mw.monitor.model.param.MwModelJudgeParam;
import cn.mw.monitor.model.service.MwModelJudgeService;
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

/**
 * @author qzg
 * @date 2023/9/1
 */
@RequestMapping("/mwapi/judgeMessage")
@Controller
@Slf4j
@Api(value = "模型评价接口", tags = "模型评价接口")
public class MwModelJudgeMessageController extends BaseApiService {
    @Autowired
    private MwModelJudgeService mwModelJudgeService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/selectInfoById/browse")
    @ResponseBody
    @ApiOperation(value = "查询评价信息")
    public ResponseBase selectJudgeMessage(@RequestBody MwModelJudgeParam param) {
        Reply reply;
        try {
            reply = mwModelJudgeService.selectJudgeMessage(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectJudgeMessage{}", e);
            return setResultFail("查询评价信息", "param");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/insertJudgeInfo/create")
    @ResponseBody
    @ApiOperation(value = "新增评价信息")
    public ResponseBase addJudgeMessage(@RequestBody MwModelJudgeParam param) {
        Reply reply;
        try {
            reply = mwModelJudgeService.insertJudgeMessage(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("insertJudgeMessage{}", e);
            return setResultFail("新增评价信息失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/checkJudgeCycle/browse")
    @ResponseBody
    @ApiOperation(value = "评价信息新增校验")
    public ResponseBase checkJudgeCycle(@RequestBody MwModelJudgeParam param) {
        Reply reply;
        try {
            reply = mwModelJudgeService.checkJudgeCycle(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("checkJudgeCycle{}", e);
            return setResultFail("评价信息新增校验失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/JudgeInfo/delete")
    @ResponseBody
    @ApiOperation(value = "删除评价信息")
    public ResponseBase deleteJudgeMessage(@RequestBody MwModelIdListParam param) {
        Reply reply;
        try {
            reply = mwModelJudgeService.deleteJudgeMessage(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("deleteJudgeMessage{}", e);
            return setResultFail("删除评价信息失败", "");
        }
    }

}
