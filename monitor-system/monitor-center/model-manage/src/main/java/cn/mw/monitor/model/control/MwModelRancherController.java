package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.ModelRelationInstanceUserListParam;
import cn.mw.monitor.model.param.ModelRelationInstanceUserParam;
import cn.mw.monitor.model.param.rancher.QueryRancherInstanceParam;
import cn.mw.monitor.model.param.rancher.RancherInstanceParam;
import cn.mw.monitor.model.service.MwModelRancherService;
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

/**
 * @author qzg
 * @date 2023/04/18
 */
@RequestMapping("/mwapi/modelRancher")
@Controller
@Slf4j
@Api(value = "模型Rancher接口", tags = "Rancher接口")
public class MwModelRancherController extends BaseApiService {

    @Autowired
    private MwModelRancherService mwModelRancherService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/login/browse")
    @ResponseBody
    @ApiOperation(value = "Rancher登录，获取数据")
    public ResponseBase loginClientGetData(@RequestBody RancherInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelRancherService.getAllRancherDataInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("loginClientGetData{}", e);
            return setResultFail("Rancher同步数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getRancherDeviceTree/browse")
    @ResponseBody
    @ApiOperation(value = "Rancher树结构获取")
    public ResponseBase getRancherDeviceTree(@RequestBody QueryRancherInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelRancherService.getRancherDeviceTree(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getRancherDeviceTree{}", e);
            return setResultFail("Rancher树结构获取失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getRancherList/browse")
    @ResponseBody
    @ApiOperation(value = "获取Rancher列表数据")
    public ResponseBase getRancherList(@RequestBody QueryRancherInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelRancherService.getRancherList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getRancherDeviceTree{}", e);
            return setResultFail("获取Rancher列表数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/setRancherPerUser/create")
    @ResponseBody
    @ApiOperation(value = "设置Rancher权限数据")
    public ResponseBase setRancherPerUser(@RequestBody ModelRelationInstanceUserListParam param) {
        Reply reply;
        try {
            reply = mwModelRancherService.setRancherUser(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getRancherDeviceTree{}", e);
            return setResultFail("设置Rancher权限数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getRancherPerUser/browse")
    @ResponseBody
    @ApiOperation(value = "获取Rancher权限数据")
    public ResponseBase getRancherPerUser(@RequestBody ModelRelationInstanceUserParam param) {
        Reply reply;
        try {
            reply = mwModelRancherService.getRancherUser(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getRancherDeviceTree{}", e);
            return setResultFail("获取Rancher权限数据失败", "");
        }
        return setResultSuccess(reply);
    }
}
