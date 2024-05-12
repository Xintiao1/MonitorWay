package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.DeleteModelMacrosParam;
import cn.mw.monitor.model.param.MwModelMacrosManageParam;
import cn.mw.monitor.model.param.ilosystem.ILOInstanceParam;
import cn.mw.monitor.model.service.MwModelILOSystemService;
import cn.mw.monitor.model.service.MwModelMacroAuthService;
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

import java.util.List;

/**
 * 刀片服务器
 *
 * @author qzg
 * @date 2023/04/18
 */
@RequestMapping("/mwapi/modelMacroAuth")
@Controller
@Slf4j
@Api(value = "资产新增宏值凭证管理", tags = "资产新增宏值凭证管理")
public class MwModelMacroAuthController extends BaseApiService {

    @Autowired
    private MwModelMacroAuthService mwModelMacroAuthService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getList/browse")
    @ResponseBody
    @ApiOperation(value = "宏值凭证列表查询")
    public ResponseBase getMacroAuthList(@RequestBody MwModelMacrosManageParam param) {
        Reply reply;
        try {
            reply = mwModelMacroAuthService.getMacroAuthList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getMacroAuthList{}", e);
            return setResultFail("宏值凭证列表查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getAllMacroField/browse")
    @ResponseBody
    @ApiOperation(value = "查询宏值字段下拉数据")
    public ResponseBase getAllMacroField() {
        Reply reply;
        try {
            reply = mwModelMacroAuthService.getAllMacroField();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllMacroField{}", e);
            return setResultFail("查询宏值字段下拉数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/queryMacroField/browse")
    @ResponseBody
    @ApiOperation(value = "根据模型查询宏值字段")
    public ResponseBase queryMacroFieldByModelId(@RequestBody MwModelMacrosManageParam param) {
        Reply reply;
        try {
            reply = mwModelMacroAuthService.queryMacroFieldByModelId(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("queryMacroFieldByModelId{}", e);
            return setResultFail("根据模型查询宏值字段失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/addMacroAuthInfo/insert")
    @ResponseBody
    @ApiOperation(value = "新增资产同步凭证数据")
    public ResponseBase addMacroAuthInfo(@RequestBody List<MwModelMacrosManageParam> paramList) {
        Reply reply;
        try {
            reply = mwModelMacroAuthService.addMacroAuthInfo(paramList);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("addMacroAuthInfo{}", e);
            return setResultFail("新增资产同步凭证数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/selectInfoPopup/browse")
    @ResponseBody
    @ApiOperation(value = "编辑查询资产同步凭证数据")
    public ResponseBase selectInfoPopup(@RequestBody MwModelMacrosManageParam param) {
        Reply reply;
        try {
            reply = mwModelMacroAuthService.selectInfoPopup(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateMacroAuthNameInfo{}", e);
            return setResultFail("编辑查询资产同步凭证数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/updateMacroAuthInfo/editor")
    @ResponseBody
    @ApiOperation(value = "修改资产同步凭证数据")
    public ResponseBase updateMacroAuthNameInfo(@RequestBody List<MwModelMacrosManageParam> paramList) {
        Reply reply;
        try {
            reply = mwModelMacroAuthService.updateMacroAuthNameInfo(paramList);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateMacroAuthNameInfo{}", e);
            return setResultFail("修改资产同步凭证数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/deleteMacroInfo/delete")
    @ResponseBody
    @ApiOperation(value = "删除宏值凭证数据")
    public ResponseBase deleteMarcoInfoByModel(@RequestBody List<DeleteModelMacrosParam> param) {
        Reply reply;
        try {
            reply = mwModelMacroAuthService.deleteMarcoInfoByModel(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteMarcoInfoByModel{}", e);
            return setResultFail("删除宏值凭证数据失败", "");
        }
        return setResultSuccess(reply);
    }

}
