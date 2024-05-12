package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.model.param.BatchUpdatePowerParam;
import cn.mw.monitor.model.param.ModelAddTangAssetsParam;
import cn.mw.monitor.service.model.exception.NotFindGroupException;
import cn.mw.monitor.service.model.exception.NotFindTemplateException;
import cn.mw.monitor.service.model.param.QueryInstanceModelParam;
import cn.mw.monitor.model.service.MwModelAssestDiscoveryService;
import cn.mw.monitor.service.assets.event.AddTangibleassetsEvent;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.common.ListenerService;
import cn.mw.monitor.service.license.service.CheckCountService;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.model.service.MwModelAssetsByESService;
import cn.mw.monitor.service.model.service.MwModelManageCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.snmp.service.MWScanService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 资产发现添加模型实例
 *
 * @author qzg
 * @date 2022/7/12
 */
@RequestMapping("/mwapi/modelDiscovery")
@Controller
@Slf4j
@Api(value = "资源中心", tags = "资产发现添加模型实例")
public class MwModelAssetsDiscoveryController extends BaseApiService {
    @Autowired
    private MwModelAssestDiscoveryService modelAssestDiscoveryService;
    @Autowired
    private CheckCountService checkCountService;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private LicenseManagementService licenseManagement;
    @Autowired
    private MWScanService mwScanService;
    @Autowired
    private MwModelAssetsByESService mwModelAssetsByESService;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;

    @Autowired
    private MwModelManageCommonService mwModelManageCommonService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/modelAssets/create")
    @ResponseBody
    @ApiOperation(value = "资产发现实例入库")
    public ResponseBase addModelAssetsByScanSuccess(@RequestBody ModelAddTangAssetsParam param) {
        Reply reply = new Reply();

        //设置为资源管理模式,则不能从资源中心添加
        if(!modelAssetEnable){
            return setResultFail(ErrorConstant.TANGASSETS_MSG_210122,param);
        }

        try {
            reply = modelAssestDiscoveryService.addModelAssetsByScanSuccess(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("addModelAssetsByScanSuccess{}", e);
            return setResultFail("资产发现实例入库失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/manageAssets/create")
    @ResponseBody
    @ApiOperation(value = "资产实例纳管")
    public ResponseBase modelAssetsToManage(@RequestBody QueryInstanceModelParam params) {
        Reply reply = new Reply();
        try {
            reply = modelAssestDiscoveryService.modelAssetsToManage(params);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (NotFindGroupException e) {
            log.error("NotFindGroupException{}", e);
            return setResultFail(e.getMessage(), "");
        }catch (NotFindTemplateException e) {
            log.error("NotFindTemplateException{}", e);
            return setResultFail(e.getMessage(), "");
        } catch (Throwable e) {
            log.error("modelAssetsToManage{}", e);
            return setResultFail("资产实例纳管失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceList/updateStatus")
    @ResponseBody
    @ApiOperation(value = "实例列表修改开关型状态")
    public ResponseBase updateListStatus(@RequestBody BatchUpdatePowerParam param) {
        Reply reply = new Reply();
        try {
            reply = modelAssestDiscoveryService.updateListStatus(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("updateListStatus{}", e);
            return setResultFail("实例列表修改状态失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/manageAssets/afresh")
    @ResponseBody
    @ApiOperation(value = "资产实例数据重新添加到扫描结果里")
    public ResponseBase afreshModelAssetsManage(@RequestBody QueryInstanceModelParam params) {
        Reply reply = new Reply();
        try {
            reply = modelAssestDiscoveryService.modelAssetsToManage(params);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (NotFindGroupException e) {
            return setResultFail(e.getMessage(), "");
        }catch (NotFindTemplateException e) {
            return setResultFail(e.getMessage(), "");
        } catch (Throwable e) {
            log.error("modelAssetsToManage{}", e);
            return setResultFail("资产实例纳管失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/modelAssetsByInsert/create")
    @ResponseBody
    @ApiOperation(value = "资产新增实例添加")
    public ResponseBase addModelAssetsByInsert(@RequestBody AddUpdateTangAssetsParam addUpdateTangAssetsParam) {
        Reply reply;
        try {
            //除了Snmp资产，其他监控方式资产启动配置为false
            //snmpv1v2和snmpv3的monitorMode相同,只用了snmpv1v2来判断
            if (addUpdateTangAssetsParam.getMonitorMode() != null
                    && addUpdateTangAssetsParam.getMonitorMode().intValue() != RuleType.SNMPv1v2.getMonitorMode()) {
                addUpdateTangAssetsParam.setSettingFlag(false);
            }
            //许可校验
            Integer typeId = addUpdateTangAssetsParam.getAssetsTypeId();
            Integer monitorMode = addUpdateTangAssetsParam.getMonitorMode();
            List<Integer> assetTypeIds = new ArrayList<>();
            int lCount = 0;

            //数量获取
            int aCount = checkCountService.selectAssetsCount(assetTypeIds, assetTypeIds);
            ResponseBase responseBase = licenseManagement.getLicenseManagemengtAssetsByMonitorMode(typeId, monitorMode, aCount + lCount, 1);
            if (responseBase.getRtnCode() != 200) {
                return setResultFail(responseBase.getMsg(), responseBase.getData());
            }

            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("有形资产")
                    .objName(addUpdateTangAssetsParam.getAssetsName()).operateDes("有形资产新增").build();
            log.info(JSON.toJSONString(builder));
            reply = mwModelAssetsByESService.doInsertAssetsByES(addUpdateTangAssetsParam, false);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }

            if (mwScanService instanceof ListenerService) {
                AddTangibleassetsEvent event = AddTangibleassetsEvent.builder().addTangAssetsParam(addUpdateTangAssetsParam).build();
                ((ListenerService) mwScanService).publishFinishEvent(event);
            }
        } catch (Throwable e) {
            log.error("addTangAssets", e);
            return setResultFail("资产新增实例添加失败", addUpdateTangAssetsParam);
        }

        return setResultSuccess(reply);
    }


    /**
     * 非扫描资产查询模板接口，根据监控方式
     */
    @MwPermit(moduleName = "model_manage")
    @PostMapping("/templateByModel/getList")
    @ResponseBody
    public ResponseBase getTemplateByModeList(@RequestBody AddUpdateTangAssetsParam addUpdateTangAssetsParam,
                                              HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = modelAssestDiscoveryService.getTemplateListByMode(addUpdateTangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.TANGASSETS_MSG_210120, reply.getData());
            }
        } catch (Throwable e) {
            log.error("查询模板接口异常",e);
            return setResultFail(ErrorConstant.TANGASSETS_MSG_210120, addUpdateTangAssetsParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 获取资产子类型（模型）
     *
     * @return
     */
    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getAssetsSubType/browse")
    @ResponseBody
    public ResponseBase getAssetsSubTypeByMode() {
        Reply reply;
        try {
            reply = mwModelManageCommonService.getAssetsSubTypeByMode();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("获取资产子类型失败", reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAssetsSubTypeByMode fail{}",e);
            return setResultFail("获取资产子类型失败", "");
        }

        return setResultSuccess(reply);
    }


}
