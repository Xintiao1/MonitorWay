package cn.mw.monitor.api.controller;

import cn.mw.monitor.alert.dao.MwAlertRuleDao;
import cn.mw.monitor.alert.param.AlertAndRuleEnableParam;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.alert.param.AddAndUpdateAlertRuleParam;
import cn.mw.monitor.alert.param.MwAlertRuleParam;
import cn.mw.monitor.alert.service.MWAlertRuleService;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author xhy
 * @date 2020/4/1 9:49
 */
@Controller
@Slf4j
@RequestMapping("/mwapi")
@Api(value = "告警规则")
public class MWAlertRuleController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWAlertController.class.getName());
    private static final Logger dblogger = LoggerFactory.getLogger("MWDBLogger");

    @Resource
    private MwAlertRuleDao mwAlertRuleDao;

    @Autowired
    private MWAlertRuleService mwAlertRuleService;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @PostMapping("/rule/getActionType")
    @ResponseBody
    @ApiOperation(value = "查询告警通知方式")
    public ResponseBase getActionType() {
        Reply reply;
        try {
            reply = mwAlertRuleService.getActionType();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getActionType",e);
            return setResultFail("接口报错!",  ErrorConstant.ALERT_ACTION_TYPE_MAG_300009);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/rule/getRuleListByType")
    @ResponseBody
    @ApiOperation(value = "查询告警通知方式")
    public ResponseBase getRuleListByActionTypeIds(@RequestBody MwAlertRuleParam param) {
        Reply reply;
        try {
            reply = mwAlertRuleService.getRuleListByActionTypeIds(param.getActionTypeIds());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getRuleListByActionTypeIds",e);
            return setResultFail("接口报错!",  ErrorConstant.ALERT_ACTION_TYPE_MAG_300009);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/rule/enable")
    @ResponseBody
    @ApiOperation(value = "规则开闭")
    public ResponseBase editorRuleEnable(@RequestBody AlertAndRuleEnableParam param) {

        // 查询分页
        try {
            int result = mwAlertRuleDao.updateRuleEnable(param);
        } catch (Throwable e) {
            logger.error("editorRule",e);
            return setResultFail("接口报错!", param);
        }
        return setResultSuccess();
    }

    @PostMapping("/rule/popup/create")
    @ResponseBody
    @ApiOperation(value = "添加规则动作")
    public ResponseBase insertRule(@Valid @RequestBody  AddAndUpdateAlertRuleParam param) {
        // 查询分页
        Reply reply;
        try {
            reply = mwAlertRuleService.insertRule(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            SystemLogDTO sbuild = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("通知规则")
                    .objName(param.getRuleName()).operateDes("添加规则动作").build();
            dblogger.info(JSON.toJSONString(sbuild));
        } catch (Throwable e) {
            logger.error("insertRule",e);
            return setResultFail("接口报错!", param);
        }

        return setResultSuccess(reply);
    }
    @PostMapping("/rule/editor")
    @ResponseBody
    @ApiOperation(value = "修改规则动作")
    public ResponseBase editorRule(@Valid @RequestBody AddAndUpdateAlertRuleParam param) {
        // 查询分页
        Reply reply;
        try {
            reply = mwAlertRuleService.editorRule(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            SystemLogDTO sbuild = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("通知规则")
                    .objName(param.getRuleName()).operateDes("修改规则动作").build();
            dblogger.info(JSON.toJSONString(sbuild));
        } catch (Throwable e) {
            logger.error("editorRule",e);
            return setResultFail("接口报错!", param);
        }

        return setResultSuccess(reply);
    }
    @PostMapping("/rule/delete")
    @ResponseBody
    @ApiOperation(value = "删除规则动作")
    public ResponseBase deleteRule(@RequestBody List<MwAlertRuleParam> param) {
        // 查询分页
        Reply reply;
        try {
            reply = mwAlertRuleService.deleteRule(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            for (MwAlertRuleParam ruleParam: param) {
                SystemLogDTO sbuild = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("通知规则")
                        .objName(ruleParam.getRuleName()).operateDes("删除规则动作").build();
                dblogger.info(JSON.toJSONString(sbuild));
            }
        } catch (Throwable e) {
            logger.error("deleteRule",e);
            return setResultFail("接口报错!", param);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/rule/fuzzSearch")
    @ResponseBody
    public ResponseBase fuzzSeach(@RequestBody MwAlertRuleParam mwAlertRuleParam){
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwAlertRuleService.fuzzSeach(mwAlertRuleParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("错误:{}",e);
            return setResultFail("接口报错!", "模糊查询所有字段资数据失败");
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/rule/browse")
    @ResponseBody
    @ApiOperation(value = "查询规则动作列表")
    public ResponseBase selectRuleList(@RequestBody MwAlertRuleParam mwAlertRuleParam) {
        // 查询分页
        Reply reply;
        try {
            reply = mwAlertRuleService.selectRuleList(mwAlertRuleParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("selectRuleList",e);
            return setResultFail("接口报错!", mwAlertRuleParam);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/rule/popup/browse")
    @ResponseBody
    @ApiOperation(value = "修改前查询规则动作列表")
    public ResponseBase selectRuleById(@RequestBody MwAlertRuleParam mwAlertRuleParam) {
        // 查询分页
        Reply reply;
        try {
            reply = mwAlertRuleService.selectRuleById(mwAlertRuleParam.getRuleId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            logger.error("selectRuleById",e);
            return setResultFail("接口报错!", mwAlertRuleParam);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/rule/sendTest/browse")
    @ResponseBody
    @ApiOperation(value = "信息发送测试")
    public ResponseBase sendTest(@RequestBody AddAndUpdateAlertRuleParam param) {
        Reply reply;
        try {
            reply = mwAlertRuleService.sendTest(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            logger.error("selectRuleById",e);
            return setResultFail("接口报错!", param);
        }

        return setResultSuccess(reply);
    }

}
