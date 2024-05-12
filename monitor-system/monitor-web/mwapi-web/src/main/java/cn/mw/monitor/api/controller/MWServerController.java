package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.server.param.MwOpenItemParam;
import cn.mw.monitor.server.service.MwDatabaseService;
import cn.mw.monitor.server.service.MwStorageService;
import cn.mw.monitor.service.link.param.MwLinkCommonParam;
import cn.mw.monitor.service.link.service.MWNetWorkLinkCommonService;
import cn.mw.monitor.service.server.api.MwServerService;
import cn.mw.monitor.service.server.api.dto.*;
import cn.mw.monitor.service.server.param.*;
import cn.mw.monitor.util.RSAUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.util.List;

/**
 * @author xhy
 * @date 2020/4/26 15:00
 */
@RequestMapping("/mwapi/server")
@Controller
@Api(value = "我的监测", tags = "服务器")
public class MWServerController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-MWServerController" + MWServerController.class.getName());

    @Autowired
    private MwServerService service;

    @Autowired
    private MwDatabaseService mwDatabaseService;

    @Autowired
    private MwStorageService mwStorageService;

    @Autowired
    private MWNetWorkLinkCommonService linkCommonService;


     @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getMemory/browse")
    @ResponseBody
    @ApiOperation(value = "查询某监控项的排行")
    public ResponseBase getMemory(@RequestBody RankServerDTO param) {
        Reply reply;
        try {
            reply = service.getItemRank(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    //通过itemid获得监控数据 1小时 1天 一周 一个月  临时版
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getHistoryByName/browse")
    @ResponseBody
    @ApiOperation(value = "通过itemNames获得监控数据")
    public ResponseBase getItemsLineChartData(@RequestBody ServerHistoryDto param) {
        Reply reply;
        try {
            logger.info("进入历史折线图数据接口！");
            // 验证内容正确性
            if (param.getDateType() == 5) {
                if ((param.getDateStart() == null || param.getDateEnd() == null) && (param.getStartTime() == null || param.getEndTime() == null) ) {
                    return setResultFail("自定义时间不能为空", Reply.fail(param));
                }
            }
            reply = service.getHistoryData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                //默认不提示错误信息
                return setResultSuccess();
            }
        } catch (Throwable e) {
            logger.error("历史折线图数据接口"+e.getMessage());
            //默认不提示错误信息
            return setResultSuccess();
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getDiskDataList/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid的所有磁盘的数据")
    public ResponseBase getDiskDataList(@RequestBody AssetsBaseDTO assetsBaseDTO) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getDiskDataList(assetsBaseDTO);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), assetsBaseDTO);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getSoftwareDataList/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid的所有软件的数据")
    public ResponseBase getSoftwareDataList(@RequestBody AssetsIdsPageInfoParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getSoftwareDataList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getDiskDetail/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid的磁盘详情和磁盘IO详情")
    public ResponseBase getDiskDetail(@RequestBody DiskTypeDto diskTypeDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getDiskDetail(diskTypeDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), diskTypeDto);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getNetDataList/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid的所有网络接口列表的数据")
    public ResponseBase getNetDataList(@RequestBody AssetsIdsPageInfoParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getNetDataList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getNetDetail/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid的网络接口详情")
    public ResponseBase getNetDetail(@RequestBody DiskTypeDto diskTypeDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getNetDetail(diskTypeDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), diskTypeDto);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getApplication/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid应用集数据")
    public ResponseBase getApplication(@RequestBody AssetsBaseDTO aParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getApplication(aParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), aParam);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getItemApplication/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid应用集数据的item数据")
    public ResponseBase getItemApplication(@RequestBody ApplicationParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getItemApplication(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getAlarmByhostId/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid告警条数")
    public ResponseBase getAlarmByHostId(@RequestBody AssetsIdsPageInfoParam alarmParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getAlarmByHostId(alarmParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), alarmParam);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @GetMapping("/getSysByhostId/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前资产主键Id档案")
    public ResponseBase getSysByhostId(@PathParam("id") String id, @PathParam("moduleType") String moduleType) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getRecordByAssetsId(id, moduleType);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), id);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getNameList/browse")
    @ResponseBody
    @ApiOperation(value = "根据名字类型获得当前hostid的名字集合(如磁盘名称，接口名称)")
    public ResponseBase getNetNameList(@RequestBody TypeFilterDTO aParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getNameListByNameType(aParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.SERVER_TYPENAME_LIST_INFO_MSG_302015, reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.SERVER_TYPENAME_LIST_INFO_MSG_302015, aParam);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getApplicationName/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid应用集是否有磁盘、硬件和网络（接口）,展示相应应用集（固定的为概览，指标详情）")
    public ResponseBase getApplicationName(@RequestBody QueryNavigationBarParam aParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getNavigationBarByApplication(aParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), aParam);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @GetMapping("/getDurationAndStatusByHostId/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostid的持续运行时间和状态")
    public ResponseBase getDurationAndStatusByHostId(@PathParam("monitorServerId") int monitorServerId, @PathParam("assetsId") String assetsId) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getDurationAndStatusByHostId(monitorServerId, assetsId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), assetsId);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getHistoryByItemId/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前itemId一个小时内的历史数据")
    public ResponseBase getHistoryByItemId(@RequestBody ItemLineParam iParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getHistoryByItemId(iParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), iParam);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getHardwareByHostId/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostId的硬件数据")
    public ResponseBase getHardwareByHostId(@RequestBody QueryApplicationTableParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getHardwareByHostId(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @GetMapping("/getMonitoringItems/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostId的所有监控项数据")
    public ResponseBase getMonitoringItems(@PathParam("monitorServerId") int monitorServerId, @PathParam("assetsId") String assetsId, @PathParam("itemName") String itemName) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getMonitoringItems(monitorServerId, assetsId, itemName);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), assetsId);
        }

        return setResultSuccess(reply);
    }

    //通过itemid获得监控数据 1小时 1天 一周 一个月
    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getHistory/browse")
    @ResponseBody
    @ApiOperation(value = "通过serverHistoryDto获得监控数据")
    public ResponseBase getHistoryData(@RequestBody ServerHistoryDto param) {
        Reply reply;
        try {
            // 验证内容正确性
            if (param.getDateType() == 5) {
                if (param.getDateStart() == null || param.getDateEnd() == null) {
                    return setResultFail("自定义时间不能为空", Reply.fail(param));
                }
            }
            reply = service.getHistoryDataInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getRunServiceObject/browse")
    @ResponseBody
    @ApiOperation(value = "通过ip获得有相同IP的资产信息")
    public ResponseBase getAssetsDataByIp(@RequestBody RunServiceObjectParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getRunServiceObjectByIp(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @GetMapping("/getDiskInfoByDiskName/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前资产的某个磁盘的基本信息")
    public ResponseBase getDiskInfoByDiskName(@PathParam("monitorServerId") int monitorServerId, @PathParam("assetsId") String assetsId, @PathParam("diskName") String diskName) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getDiskInfo(monitorServerId, diskName, assetsId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), diskName);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getRelevantInfoByItemName/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前资产的某些有关联的基本信息，制成表格数据")
    public ResponseBase getRelevantInfoByItemName(@RequestBody ItemLineParam param) {
        Reply reply;
        try {
            if (param.getItemNames() != null && param.getItemNames().size() > 0) {
                reply = mwDatabaseService.getRelevantInfoForTable(param.getMonitorServerId(), param.getAssetsId(), param.getItemNames());
            } else {
                reply = mwDatabaseService.getRelevantInfoForTable(param.getMonitorServerId(), param.getAssetsId(), param.getItemName());
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.SERVER_INNODB_INFO_MSG_302013, reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.SERVER_INNODB_INFO_MSG_302013, param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @GetMapping("/getSelectInfoByItemName/browse")
    @ResponseBody
    @ApiOperation(value = "获取mysql 中每秒查询、每秒问题、每秒慢查询的监控项，并获取它们的最新数据")
    public ResponseBase getSelectInfoByItemName(@PathParam("monitorServerId") int monitorServerId, @PathParam("assetsId") String assetsId) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwDatabaseService.getSelectInfoForTable(monitorServerId, assetsId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.SERVER_INNODB_INFO_MSG_302014, reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.SERVER_INNODB_INFO_MSG_302014, assetsId);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getStorageVolInfoByTypeName/browse")
    @ResponseBody
    @ApiOperation(value = "获取存储卷的基础信息")
    public ResponseBase getStorageVolInfoByTypeName(@RequestBody ItemLineParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwStorageService.getStorageVolInfo(param.getMonitorServerId(), param.getAssetsId(), param.getItemName());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.SERVER_STORAGE_VOL_INFO_MSG_302016, reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.SERVER_STORAGE_VOL_INFO_MSG_302016, param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getItemNameLikes/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有相似的监控项除去分区的名称信息")
    public ResponseBase getItemNameLikes(@RequestBody ItemLineParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwStorageService.getItemNameLikes(param.getMonitorServerId(), param.getAssetsId(), param.getItemName());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(ErrorConstant.SERVER_ITEMNAMELIKES_INFO_MSG_302017, reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.SERVER_ITEMNAMELIKES_INFO_MSG_302017, param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getAvailableByHostId/browse")
    @ResponseBody
    @ApiOperation(value = "获取一段时间的可用性数据")
    public ResponseBase getAvailableInfo(@RequestBody QueryAssetsAvailableParam param) {
        Reply reply;
        try {
            // 验证内容正确性
//            reply = service.getAvailableByHostId(param);

            reply = service.getAvailableByHostIdTest(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
//                if ("2021".equals(reply.getData().toString())) {
                return setResult(2021, reply.getMsg(), reply.getData());
//                } else {
//                    return setResultFail(reply.getMsg(), reply.getData());
//                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(ErrorConstant.MYMONITOR_SELECT_Available_INFO_MSG_302024, param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/itemCheckNow/browse")
    @ResponseBody
    @ApiOperation(value = "控制监控项立即刷新")
    public ResponseBase itemCheckNow(@RequestBody ApplicationParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.itemCheckNow(param.getMonitorServerId(), param.getItems());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("监控项立即刷新失败！", param);
        }
        return setResultSuccess(reply);
    }

    /**
     * 开放接口
     */
    @PostMapping("/open/getMonitoringItems/browse")
    @ResponseBody
    @ApiOperation(value = "获得当前hostId的所有监控项数据")
    public ResponseBase<List<ItemApplication>> getOpenMonitoringItems(@RequestBody MwOpenItemParam param) {
        Reply reply;
        try {
            String privateKey = RSAUtils.RSA_PRIVATE_KEY;
            String serverId = RSAUtils.decryptData(param.getMonitorServerId(), privateKey);
            String hostId = RSAUtils.decryptData(param.getAssetsId(), privateKey);
            String name = RSAUtils.decryptData(param.getItemName(), privateKey);
            if(!NumberUtils.isNumber(serverId) || StringUtils.isBlank(hostId) || StringUtils.isBlank(name)){
                return setResultFail("数据不符合规则,解密数据失败", "");
            }
            // 验证内容正确性
            reply = service.getMonitoringItems(Integer.parseInt(serverId), hostId, name);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getAssetsStatus/browse")
    @ResponseBody
    @ApiOperation(value = "根据serverId与hostId查询资产状态")
    public ResponseBase getAssetsStatusInfo(@RequestBody QueryAssetsStatusParam param) {
        Reply reply;
        try {
            reply = service.getAssetsStatusInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error("查询资产状态失败",e);
            return setResultFail("查询资产状态失败", param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @GetMapping("/getAssetsDetailsProcess/browse")
    @ResponseBody
    @ApiOperation(value = "查询资产进程信息")
    public ResponseBase getAssetsDetailsProcess(@PathParam("monitorServerId") int monitorServerId, @PathParam("assetsId") String assetsId, @PathParam("itemName") String itemName) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getAssetsDetailsProcess(monitorServerId, assetsId, itemName);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), assetsId);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/assetsDetailsProcess/download")
    @ResponseBody
    @ApiOperation(value = "资产详情页签进程top10下载txt")
    public void downloadAssetsDetailsProcess(@RequestBody AssetsBaseDTO assetsBaseDTO, HttpServletResponse response) {
        try {
            service.downloadAssetsDetailsProcess(assetsBaseDTO,response);
        } catch (Throwable e) {
            logger.error("资产详情页签进程top10下载失败", e);
        }
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getTrendInfo")
    @ResponseBody
    @ApiOperation(value = "获取趋势信息")
    public ResponseBase getTrendInfo(@RequestBody QueryItemTrendParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getHistoryTrend(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("获取趋势信息失败", param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getAssetsTempAndHumidity")
    @ResponseBody
    @ApiOperation(value = "获取资产的温度和湿度")
    public ResponseBase getAssetsTempAndHumidity(@RequestBody QueryItemTrendParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getAssetsTempAndHumidity(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("获取资产的温度和湿度失败", param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getLinkInfo")
    @ResponseBody
    @ApiOperation(value = "获取线路信息")
    public ResponseBase getLinkInfo(@RequestBody MwLinkCommonParam linkCommonParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = linkCommonService.getLinkInfo(linkCommonParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("获取线路信息失败", linkCommonParam);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getAssetsDetail")
    @ResponseBody
    @ApiOperation(value = "获取资产详情")
    public ResponseBase getLinkInfo(@RequestBody QueryItemTrendParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = service.getAssetsDetatils(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("获取资产详情失败", param);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "mw_monitor")
    @PostMapping("/getLinkDirectory")
    @ResponseBody
    @ApiOperation(value = "获取线路目录信息")
    public ResponseBase getLinkDirectoryDropDown() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = linkCommonService.getLinkDirectoryDropDown();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("获取线路目录信息失败",null);
        }
        return setResultSuccess(reply);
    }
}
