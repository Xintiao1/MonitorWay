package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.util.QueryHostParam;
import cn.mw.monitor.service.assets.service.MwAssetsVirtualService;
import cn.mw.monitor.virtualization.dto.VirtualUser;
import cn.mw.monitor.virtualization.dto.VirtualUserListPerm;
import cn.mw.monitor.virtualization.service.MwVirtualService;
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

import javax.servlet.http.HttpServletResponse;

/**
 * @author syt
 * @Date 2020/6/29 15:34
 * @Version 1.0
 */
@RequestMapping("/mwapi/virtual")
@Controller
@Slf4j
@Api(value = "我的监测", tags = "虚拟化")
public class MWVirtualController extends BaseApiService {
    private static final String STORE = "store";
    private static final String VHOST = "vHost";
    @Autowired
    private MwVirtualService mwVirtualService;

    @Autowired
    private MwAssetsVirtualService mwAssetsVirtualService;

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getVCenterTree/browse")
    @ResponseBody
    @ApiOperation(value = "查询虚拟化树形数据")
    public ResponseBase getVCenterTree() {
        Reply reply;
        try {
            reply = mwVirtualService.getAllTree(VHOST);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getVCenterTree {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getStoreTree/browse")
    @ResponseBody
    @ApiOperation(value = "查询存储树形数据")
    public ResponseBase getStoreTree() {
        Reply reply;
        try {
            reply = mwVirtualService.getAllTree(STORE);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getStoreTree {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getTableTitleData/browse")
    @ResponseBody
    @ApiOperation(value = "查询数据表类型列表数据")
    public ResponseBase getTableTitleData(@RequestBody QueryHostParam qParam) {
        Reply reply;
        try {
            reply = mwVirtualService.getTableTitle(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getTableTitleData {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getHostTableData/browse")
    @ResponseBody
    @ApiOperation(value = "查询主机表数据")
    public ResponseBase getHostTableData(@RequestBody QueryHostParam qParam) {
        Reply reply;
        try {
            reply = mwVirtualService.getHostTable(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getHostTableData {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getVMsTableData/browse")
    @ResponseBody
    @ApiOperation(value = "查询虚拟机表数据")
    public ResponseBase getVMsTableData(@RequestBody QueryHostParam qParam) {
        Reply reply;
        try {
            reply = mwVirtualService.getVMsTable(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getVMsTableData {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/exportVMsTableData")
    @ResponseBody
    @ApiOperation(value = "虚拟机数据导出")
    public ResponseBase exportVMsTableData(@RequestBody QueryHostParam qParam, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwVirtualService.exportVMsTableData(qParam, response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("exportVMsTableData {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/exportHostTableData")
    @ResponseBody
    @ApiOperation(value = "主机数据导出")
    public ResponseBase exportHostTableData(@RequestBody QueryHostParam qParam, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwVirtualService.exportHostTableData(qParam, response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("exportHostTableData {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/exportStoreTableData")
    @ResponseBody
    @ApiOperation(value = "数据存储导出")
    public ResponseBase exportStoreTableData(@RequestBody QueryHostParam qParam, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwVirtualService.exportStoreTableData(qParam, response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("exportStoreTableData {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getStoreTableData/browse")
    @ResponseBody
    @ApiOperation(value = "查询数据存储表数据")
    public ResponseBase getStoreTableData(@RequestBody QueryHostParam qParam) {
        Reply reply;
        try {
            reply = mwVirtualService.getStoreTable(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getStoreTableData {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getBasicData/browse")
    @ResponseBody
    @ApiOperation(value = "查询基本信息数据")
    public ResponseBase getBasicData(@RequestBody QueryHostParam qParam) {
        Reply reply;
        try {
            reply = mwVirtualService.getBasic(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getBasicData {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getAssetsId/browse")
    @ResponseBody
    @ApiOperation(value = "根据Ip地址查询资产主机id和assetsId")
    public ResponseBase getAssetsIdByIp(@RequestBody QueryHostParam qParam) {
        Reply reply;
        try {
            reply = mwVirtualService.getAssetsIdByIp(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResult(reply.getRes(), reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAssetsIdByIp {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply.getData());
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/setVirtualUser/create")
    @ResponseBody
    @ApiOperation(value = "设置虚拟化资产负责人")
    public ResponseBase addVirtualUser(@RequestBody VirtualUserListPerm qParam) {
        Reply reply;
        try {
            reply = mwVirtualService.setVirtualUser(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResult(reply.getRes(), reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("addVirtualUser {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply.getData());
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/setVirtualUser/browse")
    @ResponseBody
    @ApiOperation(value = "查询虚拟化资产负责人")
    public ResponseBase getVirtualUser(@RequestBody VirtualUser qParam) {
        Reply reply;
        try {
            reply = mwVirtualService.getVirtualUser(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResult(reply.getRes(), reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getVirtualUser {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply.getData());
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getTestInface/browse")
    @ResponseBody
    @ApiOperation(value = "查询主机表数据")
    public ResponseBase getTestInface(@RequestBody QueryHostParam qParam) {
        Reply reply;
        try {
            String type = "vHost";
            String roleId = "";
            Integer userId = 1751;
            reply = mwAssetsVirtualService.getAllInventedAssets(type, roleId, userId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getHostTableData {}", e);
            return setResultWarn(e.getMessage());
        }
        return setResultSuccess(reply);
    }


}
