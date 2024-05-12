package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.link.dto.MwLinkTreeDto;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.link.param.DeleteLinkParam;
import cn.mw.monitor.link.param.DropDownParam;
import cn.mw.monitor.link.param.LinkDropDownParam;
import cn.mw.monitor.service.MWNetWorkLinkService;
import cn.mw.monitor.service.license.service.CheckCountService;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * @author xhy
 * @date 2020/7/20 11:13
 */
@RequestMapping("/mwapi/link")
@Controller
@Api(value = "网络链路", tags = "网络链路")
@Slf4j
public class MWNetWorkLinkController extends BaseApiService {

    @Autowired
    MWNetWorkLinkService mwNetWorkLinkService;

    @Autowired
    LicenseManagementService licenseManagement;

    @Autowired
    CheckCountService checkCountService;

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getLinkList")
    @ResponseBody
    @ApiOperation(value = "查询全部线路")
    public ResponseBase getLinkList() {
        Reply reply;
        try {
            reply = mwNetWorkLinkService.getLinkList();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("getAssetsList", e);
            return setResultFail("查询全部线路失败", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询资产的名称list 从数据库中查询
     *
     * @param dropDownParam
     * @return
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getAssetsList")
    @ResponseBody
    @ApiOperation(value = "查询资产权限url")
    public ResponseBase getAssetsList(@RequestBody DropDownParam dropDownParam) {
        Reply reply;
        try {
            reply = mwNetWorkLinkService.getAssetsList(dropDownParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("getAssetsList", e);
            return setResultFail("查询资产的名称失败", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询ip地址或端口的list 从zabbix中查询
     *
     * @param dropDownParam
     * @return
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getIpList")
    @ResponseBody
    @ApiOperation(value = "查询资产ip地址权限url")
    public ResponseBase getIpAddressList(@RequestBody DropDownParam dropDownParam) {
        Reply reply;
        try {
            reply = mwNetWorkLinkService.getIpAddressList(dropDownParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("getIpAddressList", e);
            return setResultFail("查询ip地址或端口失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation(value = "查询链路table")
    public ResponseBase selectList(@RequestBody LinkDropDownParam param) {
        Reply reply;
        try {
            reply = mwNetWorkLinkService.selectList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("selectList", e);
            return setResultFail("查询链路table失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/perform")
    @ResponseBody
    @ApiOperation(value = "链路修改前查询")
    public ResponseBase selectLink(@RequestBody LinkDropDownParam param) {
        Reply reply;
        try {
            reply = mwNetWorkLinkService.selectLink(param.getLinkId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("perform_selectLink", e);
            return setResultFail("链路修改前查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/popup/create")
    @ResponseBody
    @ApiOperation(value = "添加链路")
    public ResponseBase insertNetWorkLink(@RequestBody AddAndUpdateParam addAndUpdateParam) {
        Reply reply;
        try {

            //许可校验
            //数量获取
            int aCount = checkCountService.selectAssetsCount(Arrays.asList(2, 3, 9), Arrays.asList(2, 4));
            int lCount = checkCountService.selectTableCount("mw_network_link", true);
            ResponseBase responseBase = licenseManagement.getLicenseManagemengtAssets(9, aCount + lCount, 1);
            if (responseBase.getRtnCode() != 200) {
                return  setResultFail(responseBase.getMsg(), responseBase.getData());
            }

            reply = mwNetWorkLinkService.insertNetWorkLink(addAndUpdateParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("insertNetWorkLink", e);
            return setResultFail("添加链路失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/popup/editor")
    @ResponseBody
    @ApiOperation(value = "编辑链路")
    public ResponseBase editorNetWorkLink(@RequestBody AddAndUpdateParam addAndUpdateParam) {
        Reply reply;
        try {
            reply = mwNetWorkLinkService.editorNetWorkLink(addAndUpdateParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("editorNetWorkLink", e);
            return setResultFail("编辑链路失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "批量删除链路")
    public ResponseBase deleteNetWorkLink(@RequestBody DeleteLinkParam dParam) {
        Reply reply;
        try {
            List<AddAndUpdateParam> addAndUpdateParams = dParam.getAddAndUpdateParams();
            //许可校验
            //数量获取
            int aCount = checkCountService.selectAssetsCount(Arrays.asList(2, 3, 9), Arrays.asList(2, 4));
            int lCount = checkCountService.selectTableCount("mw_network_link", true);
            lCount = lCount - addAndUpdateParams.size();
            ResponseBase responseBase = licenseManagement.getLicenseManagemengtAssets(9, aCount + lCount, 0);
            if (responseBase.getRtnCode() != 200) {
                return  setResultFail(responseBase.getMsg(), responseBase.getData());
            }
            reply = mwNetWorkLinkService.deleteNetWorkLink(dParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("deleteNetWorkLink", e);
            return setResultFail("批量删除链路失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/enable")
    @ResponseBody
    @ApiOperation(value = "启用或关闭探测")
    public ResponseBase enableActive(@RequestBody AddAndUpdateParam addAndUpdateParams) {
        Reply reply;
        try {
            reply = mwNetWorkLinkService.enableActive(addAndUpdateParams.getEnable(), addAndUpdateParams.getLinkId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("enableActive", e);
            return setResultFail("启用或关闭探测失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getBandwidth")
    @ResponseBody
    @ApiOperation(value = "获取对应端口的上下行带宽")
    public ResponseBase getBandwidth(@RequestBody DropDownParam param) {
        Reply reply;
        try {
            reply = mwNetWorkLinkService.getBandwidth(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("getBandwidth", e);
            return setResultFail("获取对应端口的上下行带宽失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/fuzzSearchAllFiled/browse")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData(@RequestBody LinkDropDownParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwNetWorkLinkService.fuzzSearchAllFiledData(param.getFuzzyQuery());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("模糊查询线路失败", "");
        }

        return setResultSuccess(reply);
    }


    /**
     * 查询线路树状结构数据
     * @return
     */
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getTree")
    @ResponseBody
    public ResponseBase selectLinkTree() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwNetWorkLinkService.selectLinkTree();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("查询线路树状结构数据失败", "");
            }
        } catch (Throwable e) {
            log.error("查询线路树状结构数据"+e.getMessage());
            return setResultFail("查询线路树状结构数据失败", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 增加线路树状结构数据
     * @return
     */
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/createContents")
    @ResponseBody
    public ResponseBase createLinkContents(@RequestBody MwLinkTreeDto mwLinkTreeDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwNetWorkLinkService.createLinkContents(mwLinkTreeDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("增加线路树状结构数据失败", "");
            }
        } catch (Throwable e) {
            log.error("查询线路树状结构数据"+e.getMessage());
            return setResultFail("增加线路树状结构数据失败", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改线路树状结构数据
     * @return
     */
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/updateContents")
    @ResponseBody
    public ResponseBase updateLinkContents(@RequestBody List<MwLinkTreeDto> mwLinkTreeDtos) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwNetWorkLinkService.updateLinkContents(mwLinkTreeDtos);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("修改线路树状结构数据失败", "");
            }
        } catch (Throwable e) {
            log.error("查询线路树状结构数据"+e.getMessage());
            return setResultFail("修改线路树状结构数据失败", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改线路树状结构数据
     * @return
     */
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/deleteContents")
    @ResponseBody
    public ResponseBase deleteLinkContents(@RequestBody MwLinkTreeDto mwLinkTreeDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwNetWorkLinkService.deleteLinkContents(mwLinkTreeDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("删除线路树状结构数据失败", "");
            }
        } catch (Throwable e) {
            log.error("查询线路树状结构数据"+e.getMessage());
            return setResultFail("删除线路树状结构数据失败", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改线路树状结构数据
     * @return
     */
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getLinkDropDown")
    @ResponseBody
    public ResponseBase selectLinkDropDown() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwNetWorkLinkService.selectLinkDropDown();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("查询线路下拉数据失败", "");
            }
        } catch (Throwable e) {
            log.error("查询线路树状结构数据"+e.getMessage());
            return setResultFail("查询线路下拉数据失败", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 获取线路目录下拉数据
     * @return
     */
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getLinkTreeDropDown")
    @ResponseBody
    public ResponseBase getLinkTreeDropDown() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwNetWorkLinkService.getLinkTreeDropDown();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("查询线路下拉数据失败", "");
            }
        } catch (Throwable e) {
            log.error("查询线路树状结构数据"+e.getMessage());
            return setResultFail("查询线路下拉数据失败", "");

        }

        return setResultSuccess(reply);
    }

    /**
     * 修改线路树状结构数据
     * @return
     */
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/Contents")
    @ResponseBody
    public ResponseBase dragLinkContents(@RequestBody MwLinkTreeDto mwLinkTreeDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwNetWorkLinkService.dragLinkContents(mwLinkTreeDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("拖动线路目录失败", "");
            }
        } catch (Throwable e) {
            log.error("拖动线路目录失败"+e.getMessage());
            return setResultFail("拖动线路目录失败", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改线路树状结构数据
     * @return
     */
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getTrendData")
    @ResponseBody
    public ResponseBase getLinkTrendData(@RequestBody NetWorkLinkDto linkDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwNetWorkLinkService.getLinkTrendData(linkDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("获取线路趋势信息失败", "");
            }
        } catch (Throwable e) {
            log.error("获取线路趋势信息失败",e);
            return setResultFail("获取线路趋势信息失败", "");
        }

        return setResultSuccess(reply);
    }

    @ApiOperation(value = "查询线路有状态下拉")
    @GetMapping("/status/dropDowm")
    @ResponseBody
    public ResponseBase getLinkStatusDropDown() {
        Reply reply;
        try {
            reply = mwNetWorkLinkService.getLinkStatusDropDown();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("查询线路状态下拉失败", "");
            }
        } catch (Throwable e) {
            log.error("getLinkStatusDropDown{}",e);
            return setResultFail("查询线路状态下拉失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getLinkDirectory/detail")
    @ResponseBody
    @ApiOperation(value = "获取目录分类信息")
    public ResponseBase getLinkDirectoryDetail() {
        Reply reply;
        try {
            reply = mwNetWorkLinkService.getLinkDirectoryDetail();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("获取目录分类信息失败", "");
            }
        } catch (Exception e) {
            log.error("getAssetsList", e);
            return setResultFail("获取目录分类信息失败", "");
        }
        return setResultSuccess(reply);
    }
}
