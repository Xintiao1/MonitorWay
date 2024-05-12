package cn.mw.monitor.model.control;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.assets.param.MWMainTainHostParam;
import cn.mw.monitor.service.assets.param.MwAssetsMainTainDelParam;
import cn.mw.monitor.service.assets.param.MwAssetsMainTainParam;
import cn.mw.monitor.service.assets.param.MwAssetsMainTainParamV1;
import cn.mw.monitor.service.assets.service.MwAssetsMainTainService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName MwAssetsMainTainController
 * @Description 资源中心维护计划界面
 * @Author gengjb
 * @Date 2021/7/26 15:40
 * @Version 1.0
 **/
@RequestMapping("/mwapi/modelAssets")
@Controller
@Slf4j
@Api(value = "资产维护", tags = "资源中心维护页面")
public class MwModelMainTainController extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("MwAssetsMainTainController");

    @Autowired
    private MwAssetsMainTainService mainTainService;

    @PostMapping("/mainTain/create")
    @ResponseBody
    @ApiOperation("资产维护新增")
    public ResponseBase saveMainTain(@RequestBody MwAssetsMainTainParamV1 param) {
        Reply reply;
        try {
            reply = mainTainService.addAssetsMainTain(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("资产维护新增失败", e);
            return setResultFail("资产维护新增失败", "");
        }
    }

    @PostMapping("/mainTain/browse")
    @ResponseBody
    @ApiOperation("资产维护查询")
    public ResponseBase selectMainTain(@RequestBody MwAssetsMainTainParam param) {
        Reply reply;
        try {
            reply = mainTainService.selectAssetsMainTain(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("资产维护查询失败", e);
            return setResultFail("资产维护查询失败", "");
        }
    }

    @PostMapping("/mainTain/editor")
    @ResponseBody
    @ApiOperation("资产维护修改")
    public ResponseBase UpdateMainTain(@RequestBody MwAssetsMainTainParamV1 param) {
        Reply reply;
        try {
            reply = mainTainService.updateAssetsMainTain(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            reply.setMsg("数据编辑成功");
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("资产维护修改失败", e);
            return setResultFail("资产维护修改失败", "");
        }
    }

    @PostMapping("/mainTain/delete")
    @ResponseBody
    @ApiOperation("资产维护删除")
    public ResponseBase deleteMainTain(@RequestBody MwAssetsMainTainDelParam delParam) {
        Reply reply;
        try {
            reply = mainTainService.deleteAssetsMainTain(delParam.getIdList());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("资产维护删除失败", e);
            return setResultFail("资产维护删除失败", "");
        }
    }


    @PostMapping("/mainTain/groupdropdown/browse")
    @ResponseBody
    @ApiOperation("资产维护主机组下拉")
    public ResponseBase selectMainTainGroupDropDown(@RequestBody MwAssetsMainTainParam param) {
        Reply reply;
        try {
            reply = mainTainService.selectAssetsMainTainGroupDropDown(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("资产维护主机组下拉查询失败", e);
            return setResultFail("资产维护主机组下拉查询失败", "");
        }
    }

    @PostMapping("/mainTain/hostdropdown/browse")
    @ResponseBody
    @ApiOperation("资产维护主机下拉")
    public ResponseBase selectMainTainHostDropDown(@RequestBody MwAssetsMainTainParam param) {
        Reply reply;
        try {
            reply = mainTainService.selectAssetsMainTainHostDropDown(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("资产维护主机下拉查询失败", e);
            return setResultFail("资产维护主机下拉查询失败", "");
        }
    }

    @PostMapping("/mainTain/getAssetsDifficulty")
    @ResponseBody
    @ApiOperation("资产维护查询分类数据")
    public ResponseBase selectMainTainAssetsDifficulty() {
        Reply reply;
        try {
            reply = mainTainService.selectMainTainAssetsDifficulty();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("资产维护分类数据查询失败", e);
            return setResultFail("资产维护分类数据查询失败", "");
        }
    }

    @PostMapping("/mainTain/fuzzQuery")
    @ResponseBody
    @ApiOperation("资产维护计划模糊查询")
    public ResponseBase selectMainTainFuzzQuey() {
        Reply reply;
        try {
            reply = mainTainService.getAssetsMainTainPlanFuzzQuery();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("资产维护计划模糊查询失败", e);
            return setResultFail("资产维护计划模糊查询失败", "");
        }
    }

}
