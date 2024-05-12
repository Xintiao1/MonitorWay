package cn.mw.monitor.api.controller;

import cn.mw.monitor.alert.dao.MwAlertActionDao;
import cn.mw.monitor.alert.param.*;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.alert.service.MWAlertActionService;
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
import java.util.List;

/**
 * @author xhy
 * @date 2020/8/26 9:09
 */
@Controller
@Slf4j
@RequestMapping("/mwapi/action")
@Api(value = "告警通知")
public class MWAlertActionController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWAlertActionController.class.getName());
    private static final Logger dblogger = LoggerFactory.getLogger("MWDBLogger");
    @Autowired
    private MWAlertActionService mwAlertActionService;

    @Resource
    private MwAlertActionDao mwAlertActionDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation(value = "查詢告警")
    public ResponseBase selectAction(@RequestBody AlertActionParam param) {
        // 查询分页
        Reply reply;
        try {
            reply = mwAlertActionService.selectAction(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("selectAction", e);
            return setResultFail("查詢告警", param);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/popup/enable")
    @ResponseBody
    @ApiOperation(value = "告警动作开闭")
    public ResponseBase editorActionEnable(@RequestBody AlertAndRuleEnableParam param) {
        // 查询分页
        try {
            mwAlertActionDao.updateActionEnable(param);
        } catch (Throwable e) {
            logger.error("editorAction", e);
            return setResultFail("接口报错!", param);
        }
        return setResultSuccess();
    }

    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "刪除告警动作")
    public ResponseBase deleteAction(@RequestBody List<AddAndUpdateAlertActionParam> list) {
        // 查询分页
        Reply reply;
        try {
            reply = mwAlertActionService.deleteAction(list);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            for (AddAndUpdateAlertActionParam param: list) {
                SystemLogDTO sbuild = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("告警管理")
                        .objName(param.getActionName()).operateDes("刪除告警动作").build();
                dblogger.info(JSON.toJSONString(sbuild));
            }
        } catch (Throwable e) {
            logger.error("deleteAction", e);
            return setResultFail("接口报错!", list);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/fielid")
    @ResponseBody
    @ApiOperation(value = "获取通知字段")
    public ResponseBase getFielid() {
        Reply reply;
        reply = mwAlertActionService.getFielid();
        return setResultSuccess(reply);
    }

    @PostMapping("/popup/add")
    @ResponseBody
    @ApiOperation(value = "添加告警动作")
    public ResponseBase addAction(@RequestBody MwRuleSelectListParam param) {
        // 查询分页
        Reply reply;
        try {
            reply = mwAlertActionService.addAction(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.ALERT_ACTION_INSERT_MAG_300010, reply.getData());
            }
            SystemLogDTO sbuild = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("告警管理")
                    .objName(param.getActionName()).operateDes("添加告警动作").build();
            dblogger.info(JSON.toJSONString(sbuild));
        } catch (Throwable e) {
            logger.error("insertAction", e);
            return setResultFail("接口报错!", param);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/popup/editor")
    @ResponseBody
    @ApiOperation(value = "编辑告警动作")
    public ResponseBase updateAction(@RequestBody MwRuleSelectListParam param) {
        // 查询分页
        Reply reply;
        try {
            reply = mwAlertActionService.updateAction(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            SystemLogDTO sbuild = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("告警管理")
                    .objName(param.getActionName()).operateDes("编辑告警动作").build();
            dblogger.info(JSON.toJSONString(sbuild));
        } catch (Throwable e) {
            logger.error("editorAction", e);
            return setResultFail("接口报错!", param);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/popup/select")
    @ResponseBody
    @ApiOperation(value = "编辑前查询告警动作")
    public ResponseBase selectAction(@RequestBody AlertActionTable param) {
        Reply reply;
        try {
            reply = mwAlertActionService.selectAction(param.getActionId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("selectPopupAction", e);
            return setResultFail("接口报错!", param);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/tagInfo/browse")
    @ResponseBody
    @ApiOperation(value = "")
    public ResponseBase getTag() {
        Reply reply;
        try {
            reply = mwAlertActionService.getTag();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("selectPopupAction", e);
            return setResultFail("接口报错!",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/getAlertLevel")
    @ResponseBody
    @ApiOperation(value = "获取告警等级")
    public ResponseBase getAlertLevel() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwAlertActionService.getAlertLevel();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("getAlertLevel{}",e);
            return setResultFail("接口报错!", mwAlertActionService);
        }

        return setResultSuccess(reply);
    }

}
