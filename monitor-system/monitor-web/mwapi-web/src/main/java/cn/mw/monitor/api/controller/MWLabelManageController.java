package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.labelManage.api.param.*;
import cn.mw.monitor.labelManage.service.MwLabelManageService;
import cn.mw.monitor.service.label.model.QueryLabelParam;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.List;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "资产标签接口", tags = "资产标签接口")
public class MWLabelManageController extends BaseApiService {

    @Autowired
    private MwLabelManageService mwLabelMangeService;

    private static final Logger logger = LoggerFactory.getLogger("MWDBLogger");

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "标签新增")
    @PostMapping("/labelManage/create")
    @ResponseBody
    public ResponseBase addLM(@RequestBody AddUpdateLabelManageParam param,
                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("标签管理")
                    .objName(param.getLabelName()).operateDes("标签管理新增").build();
            logger.info(JSON.toJSONString(builder));
            reply = mwLabelMangeService.insert(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("insert{}", e);
            return setResultFail("标签新增失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "标签删除")
    @PostMapping("/labelManage/delete")
    @ResponseBody
    public ResponseBase deleteLM(@RequestBody List<DeleteLabelManageParam> param,
                                 HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwLabelMangeService.delete(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteLM{}", e);
            return setResultFail("标签删除失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "标签状态更新")
    @PostMapping("/labelManage/perform")
    @ResponseBody
    public ResponseBase updateLMState(@RequestBody UpdateLabelStateParam param,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwLabelMangeService.updateState(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateLMState{}", e);
            return setResultFail("标签状态更新失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "标签更新")
    @PostMapping("/labelManage/editor")
    @ResponseBody
    public ResponseBase updateLM(@RequestBody AddUpdateLabelManageParam param,
                                 HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("标签管理")
                    .objName(param.getLabelName()).operateDes("标签管理修改").build();
            logger.info(JSON.toJSONString(builder));
            reply = mwLabelMangeService.update(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateLM{}", e);
            return setResultFail("标签更新失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "分页查询标签列表信息")
    @PostMapping("/labelManage/browse")
    @ResponseBody
    public ResponseBase browseLM(@RequestBody QueryLabelManageParam param,
                                 HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwLabelMangeService.selectList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseLM{}", e);
            return setResultFail("分页查询标签列表信息失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "根据标签ID查询标签信息")
    @PostMapping("/labelManage/popup/browse")
    @ResponseBody
    public ResponseBase browseLMPopup(@RequestBody QueryLabelManageParam param,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwLabelMangeService.selectById(param.getLabelId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseLMPopup{}", e);
            return setResultFail("根据标签ID查询标签信息失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "标签关联资产类型查询")
    @PostMapping("/labelManage/assetsType/browse")
    @ResponseBody
    public ResponseBase browseLabelAssetsType(@RequestBody QueryLabelManageParam param,
                                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwLabelMangeService.selectAsstsType(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseLabelAssetsType{}", e);
            return setResultFail("标签关联资产类型查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "标签关联模块类型查询")
    @PostMapping("/labelManage/moduleType/browse")
    @ResponseBody
    public ResponseBase browseLabelModuleType(@RequestBody QueryLabelManageParam param,
                                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwLabelMangeService.selectModuleType(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseLabelModuleType{}", e);
            return setResultFail("标签关联模块类型查询失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "查询不同模块和不同资产类型的标签")
    @PostMapping("/labelManage/getDropLabelList/browse")
    @ResponseBody
    public ResponseBase getDropLabelList(@RequestBody QueryLabelParam queryLabelParam) {
        Reply reply;
        try {
            reply = mwLabelMangeService.getDropLabelList(queryLabelParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("getDropLabelList{}", e);
            return setResultFail("查询不同模块和不同资产类型的标签失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "查询多个资产类型的标签")
    @PostMapping("/getDropLabelListByAssetsTypeList")
    @ResponseBody
    public ResponseBase getDropLabelListByAssetsTypeList(@RequestBody QueryLabelParam queryLabelParam) {
        Reply reply;
        try {
            reply = mwLabelMangeService.getDropLabelListByAssetsTypeList(queryLabelParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getDropLabelListByAssetsTypeList{}", e);
            return setResultFail("查询多个资产类型的标签失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "查询线路的标签")
    @GetMapping("/labelManage/getLabelListByAssetsTypeId")
    @ResponseBody
    public ResponseBase getLabelListByAssetsTypeId(@PathParam("assetsTypeId") Integer assetsTypeId) {
        Reply reply;
        try {
            reply = mwLabelMangeService.getLabelListByAssetsTypeId(assetsTypeId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("", e);
            return setResultFail("查询线路的标签失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "标签模糊查询")
    @PostMapping("/labelManage/getLabelFuzzyQuery")
    @ResponseBody
    public ResponseBase getLabelFuzzyQuery() {
        Reply reply;
        try {
            reply = mwLabelMangeService.getLabelAssociateFuzzyQuery();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("", e);
            return setResultFail("标签模糊查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @ApiOperation(value = "下拉类型标签分页查询值")
    @PostMapping("/labelManage/getDropDownLabelValue")
    @ResponseBody
    public ResponseBase selectDropDownLabelValue(@RequestBody QueryLabelManageParam param) {
        Reply reply;
        try {
            reply = mwLabelMangeService.selectDropDownLabelValue(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("", e);
            return setResultFail("下拉类型标签分页查询值失败", "");
        }
        return setResultSuccess(reply);
    }
}


