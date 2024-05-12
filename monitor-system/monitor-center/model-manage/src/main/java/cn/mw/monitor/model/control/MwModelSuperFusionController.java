package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.ilosystem.ILOInstanceParam;
import cn.mw.monitor.model.param.superfusion.MwQuerySuperFusionParam;
import cn.mw.monitor.model.param.superfusion.QuerySuperFusionHistoryParam;
import cn.mw.monitor.model.service.MwModelSuperFusionService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * 超融合
 *
 * @author qzg
 * @date 2023/04/18
 */
@RequestMapping("/mwapi/modelSuperFusion")
@Controller
@Slf4j
@Api(value = "超融合模型接口", tags = "超融合模型接口")
public class MwModelSuperFusionController extends BaseApiService {

    @Autowired
    private MwModelSuperFusionService mwModelSuperFusionService;

    @MwPermit(moduleName = "model_manage")
    @GetMapping("/publicKey/browse")
    @ResponseBody
    @ApiOperation(value = "获取公钥")
    public ResponseBase getPublicKey() {
        Reply reply = null;
        try {
            mwModelSuperFusionService.getPublicKey();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getPublicKey{}", e);
            return setResultFail("获取公钥失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getStorageInfo/browse")
    @ResponseBody
    @ApiOperation(value = "根据存储id获取存储详情数据")
    public ResponseBase getSuperFusionStorageInfo(@RequestBody QuerySuperFusionHistoryParam param) {
        Reply reply;
        try {
            reply = mwModelSuperFusionService.getSuperFusionStorageInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSuperFusionStorageInfo{}", e);
            return setResultFail("根据存储id获取存储详情数据失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getVmInfo/browse")
    @ResponseBody
    @ApiOperation(value = "根据虚拟机Id获取详情信息")
    public ResponseBase getSuperFusionVmInfo(@RequestBody QuerySuperFusionHistoryParam param) {
        Reply reply;
        try {
            reply = mwModelSuperFusionService.getSuperFusionVmInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSuperFusionVmInfo{}", e);
            return setResultFail("根据虚拟机Id获取详情信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getHostInfo/browse")
    @ResponseBody
    @ApiOperation(value = "根据主机Id获取详情信息")
    public ResponseBase getSuperFusionHostInfo(@RequestBody QuerySuperFusionHistoryParam param) {
        Reply reply;
        try {
            reply = mwModelSuperFusionService.getSuperFusionHostInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSuperFusionHostInfo{}", e);
            return setResultFail("根据主机Id获取详情信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getBaseInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取基础信息")
    public ResponseBase getSuperFusionBaseInfo() {
        Reply reply;
        try {
            reply = mwModelSuperFusionService.getSuperFusionBaseInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSuperFusionBaseInfo{}", e);
            return setResultFail("获取基础信息失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getStorageList/browse")
    @ResponseBody
    @ApiOperation(value = "获取存储列表数据")
    public ResponseBase getAllStorageList(@RequestBody MwQuerySuperFusionParam mParam) {
        Reply reply;
        try {
            reply = mwModelSuperFusionService.getAllStorageList(mParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllStorageList{}", e);
            return setResultFail("获取存储列表数据失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getVmList/browse")
    @ResponseBody
    @ApiOperation(value = "获取虚拟机列表数据")
    public ResponseBase getAllVmList(@RequestBody MwQuerySuperFusionParam mParam) {
        Reply reply;
        try {
            reply = mwModelSuperFusionService.getAllVmList(mParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllVmList{}", e);
            return setResultFail("获取虚拟机列表数据失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getHostList/browse")
    @ResponseBody
    @ApiOperation(value = "获取主机列表数据")
    public ResponseBase getAllHostList(@RequestBody MwQuerySuperFusionParam mParam) {
        Reply reply;
        try {
            reply = mwModelSuperFusionService.getAllHostList(mParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllHostList{}", e);
            return setResultFail("获取主机列表数据失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getHostTree/browse")
    @ResponseBody
    @ApiOperation(value = "获取超融合树结构")
    public ResponseBase getSuperFusionTree(@RequestBody MwQuerySuperFusionParam mParam) {
        Reply reply;
        try {
            reply = mwModelSuperFusionService.getSuperFusionTree(mParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSuperFusionTree{}", e);
            return setResultFail("获取超融合树结构失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getHostHistory/browse")
    @ResponseBody
    @ApiOperation(value = "获取指定节点的历史数据")
    public ResponseBase getSuperFusionHostHistory(@RequestBody QuerySuperFusionHistoryParam mParam) {
        Reply reply;
        try {
            reply = mwModelSuperFusionService.getSuperFusionHostHistory(mParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSuperFusionHostHistory{}", e);
            return setResultFail("获取指定节点的历史数据失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/saveDataInfo/insert")
    @ResponseBody
    @ApiOperation(value = "数据同步")
    public ResponseBase saveSuperFusionDeviceData(@RequestBody MwQuerySuperFusionParam mParam) {
        Reply reply;
        try {
            reply = mwModelSuperFusionService.saveSuperFusionDeviceData(mParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("saveSuperFusionDeviceData{}", e);
            return setResultFail("数据同步失败","");
        }
        return setResultSuccess(reply);
    }
}
