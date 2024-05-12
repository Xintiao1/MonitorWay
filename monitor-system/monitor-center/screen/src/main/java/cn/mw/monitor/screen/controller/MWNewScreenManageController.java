package cn.mw.monitor.screen.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.screen.dto.MWNewScreenAlertDevOpsEventDto;
import cn.mw.monitor.screen.dto.MWNewScreenAssetsFilterDto;
import cn.mw.monitor.screen.dto.MWNewScreenModuleDto;
import cn.mw.monitor.screen.param.MWAlertCountParam;
import cn.mw.monitor.screen.param.MWNewScreenAssetsCensusParam;
import cn.mw.monitor.screen.service.MWNewScreenManage;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MWNewScreenManageController
 * @Description 猫维新大屏数据接口
 * @Author gengjb
 * @Date 2021/11/29 10:27
 * @Version 1.0
 **/
@RequestMapping("/mwapi/new/screen")
@Controller
@Slf4j
@Api(value = "猫维新大屏", tags = "猫维新大屏")
public class MWNewScreenManageController  extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("MWNewScreenManageController");

    @Autowired
    private MWNewScreenManage newScreenManage;

    @Value("${screen.version}")
    private String screenVersion;

    @PostMapping("/getAssetsNews")
    @ResponseBody
    @ApiOperation("大屏资产信息")
    public ResponseBase getNewScreenAssetsNews() {
        Reply reply;
        try {
            reply = newScreenManage.getNewScreenAssets();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏查询资产信息失败", e);
            return setResultFail("新大屏查询资产信息失败", "");
        }
    }

    @PostMapping("/getAssetsCensusData")
    @ResponseBody
    @ApiOperation("大屏资产统计数据")
    public ResponseBase getNewScreenAssetsCensusData(@RequestBody MWNewScreenAssetsCensusParam param) {
        Reply reply;
        try {
            reply = newScreenManage.getNewScreenAssetsCensusData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏查询资产统计数据失败", e);
            return setResultFail("新大屏查询资产统计数据失败", "");
        }
    }

    @PostMapping("/getAlertDevOpsEvent")
    @ResponseBody
    @ApiOperation("大屏告警运维事件信息数据")
    public ResponseBase getNewScreenAlertDevOpsEvent(@RequestBody List<MWNewScreenAlertDevOpsEventDto> param) {
        Reply reply;
        try {
            reply = newScreenManage.getNewScreenAlertDevOpsEvent(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏查询告警运维事件数据失败", e);
            return setResultFail("新大屏查询告警运维事件数据失败", "");
        }
    }

    @PostMapping("/getActivityAlert")
    @ResponseBody
    @ApiOperation("大屏告警活动告警信息数据")
    public ResponseBase getNewScreenActivityAlert(@RequestBody MWNewScreenAssetsCensusParam param) {
        Reply reply;
        try {
            reply = newScreenManage.getActivityAlertData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏查询告警运维事件数据失败", e);
            return setResultFail("新大屏查询告警运维事件数据失败", "");
        }
    }

    @PostMapping("/getAssetsTopN")
    @ResponseBody
    @ApiOperation("大屏资产相关TOPN数据")
    public ResponseBase getNewScreenAssetsTopN(@RequestBody MWNewScreenAlertDevOpsEventDto param) {
        Reply reply;
        try {
            reply = newScreenManage.getNewScreenAssetsTopN(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏查询告警运维事件数据失败", e);
            return setResultFail("新大屏查询告警运维事件数据失败", "");
        }
    }

    @PostMapping("/getLinkTopN")
    @ResponseBody
    @ApiOperation("大屏线路TOPN数据")
    public ResponseBase getNewScreenLinkTopN(@RequestBody MWNewScreenAlertDevOpsEventDto param) {
        Reply reply;
        try {
            reply = newScreenManage.getNewScreenLinkTopN(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏查询告警运维事件数据失败", e);
            return setResultFail("新大屏查询告警运维事件数据失败", "");
        }
    }





    @PostMapping("/getNewScreenModule")
    @ResponseBody
    @ApiOperation("大屏首页模块查询")
    public ResponseBase getNewScreenModule() {
        Reply reply;
        try {
            reply = newScreenManage.getNewScreenModule();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏模块查询失败", e);
            return setResultFail("新大屏模块查询失败", "");
        }
    }


    @PostMapping("/getNewScreenDropDown")
    @ResponseBody
    @ApiOperation("大屏首页模块下拉数据查询")
    public ResponseBase getNewScreenModuleDropDown() {
        Reply reply;
        try {
            reply = newScreenManage.getNewScreenModuleDropDown();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏模块下拉数据查询失败", e);
            return setResultFail("新大屏模块下拉数据查询失败", "");
        }
    }

    @PostMapping("/create")
    @ResponseBody
    @ApiOperation("大屏首页模块数据添加")
    public ResponseBase createNewScreenUserModule(@RequestBody MWNewScreenModuleDto screenModuleDto) {
        Reply reply;
        try {
            reply = newScreenManage.createNewScreenUserModule(screenModuleDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("新大屏模块数据添加失败", "");
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏模块数据添加失败", e);
            return setResultFail("新大屏模块数据添加失败", "");
        }
    }

    @PostMapping("/update")
    @ResponseBody
    @ApiOperation("大屏首页模块数据修改资产过滤")
    public ResponseBase updateNewScreenUserModule(@RequestBody MWNewScreenAssetsFilterDto assetsFilterDto) {
        Reply reply;
        try {
            reply = newScreenManage.updateNewScreenUserModule(assetsFilterDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("新大屏修改资产过滤数据失败", "");
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏修改资产过滤数据失败", e);
            return setResultFail("新大屏修改资产过滤数据失败", "");
        }
    }

    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation("大屏首页模块资产过滤数据查询")
    public ResponseBase selectNewScreenUserModule(@RequestBody MWNewScreenAssetsFilterDto assetsFilterDto) {
        Reply reply;
        try {
            reply = newScreenManage.selectNewScreenUserModule(assetsFilterDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("新大屏资产过滤数据查询数据失败", "");
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏资产过滤数据查询失败", e);
            return setResultFail("新大屏资产过滤数据查询数据失败", "");
        }
    }

    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation("大屏首页模块删除")
    public ResponseBase deleteNewScreenUserModule(@RequestBody MWNewScreenAssetsFilterDto assetsFilterDto) {
        Reply reply;
        try {
            reply = newScreenManage.deleteNewScreenUserModule(assetsFilterDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("新大屏模块删除失败", "");
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏模块删除失败", e);
            return setResultFail("新大屏模块删除失败", "");
        }
    }


    @PostMapping("/sort")
    @ResponseBody
    @ApiOperation("大屏首页模块排序")
    public ResponseBase newScreenModuleSort(@RequestBody Map<String,List<MWNewScreenModuleDto>> screenModuleDto) {
        Reply reply;
        try {
            List<MWNewScreenModuleDto> screenModuleDtos = screenModuleDto.get("componentList");
            reply = newScreenManage.newScreenModuleSort(screenModuleDtos);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("大屏首页模块排序失败", "");
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏模块删除失败", e);
            return setResultFail("大屏首页模块排序失败", "");
        }
    }

    @PostMapping("/labelDrop")
    @ResponseBody
    @ApiOperation("大屏首页标签数据")
    public ResponseBase newScreenLabelDrop() {
        Reply reply;
        try {
            reply = newScreenManage.newScreenLabelDrop();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏查询标签失败", e);
            return setResultFail("新大屏查询标签失败", "");
        }
    }

    @PostMapping("/getActivityAlertCount")
    @ResponseBody
    @ApiOperation("大屏告警活动告警信息数据")
    public ResponseBase getNewScreenActivityAlertCount(@RequestBody MWNewScreenAssetsCensusParam param) {
        Reply reply;
        try {
            reply = newScreenManage.getNewScreenActivityAlertCount(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新大屏查询告警运维事件数据失败", e);
            return setResultFail("新大屏查询告警运维事件数据失败", "");
        }
    }

    @PostMapping("/getAlertCount")
    @ResponseBody
    @ApiOperation("告警统计次数")
    public ResponseBase getAlertCount(@RequestBody MWAlertCountParam param) {
        Reply reply;
        try {
            reply = newScreenManage.getAlertCount(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("告警统计次数", e);
            return setResultFail("告警统计次数", "");
        }
    }

    @PostMapping("/module/update")
    @ResponseBody
    @ApiOperation("新首页模块修改")
    public ResponseBase updateNewHomeModule(@RequestBody MWNewScreenModuleDto moduleDto) {
        Reply reply;
        try {
            reply = newScreenManage.updateNewHomeModule(moduleDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新首页模块修改失败", e);
            return setResultFail("新首页模块修改失败", "");
        }
    }

    @PostMapping("/getFlowErrorCount")
    @ResponseBody
    @ApiOperation("首页流量错误包数据统计")
    public ResponseBase getHomePageFlowErrorCountTopN(@RequestBody MWNewScreenAlertDevOpsEventDto param) {
        Reply reply;
        try {
            reply = newScreenManage.getHomePageFlowErrorCountTopN(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新首页获取流量错误包排行失败", e);
            return setResultFail("新首页获取流量错误包排行失败", "");
        }
    }

    @PostMapping("/getFlowBandWidth")
    @ResponseBody
    @ApiOperation("首页流量带宽数据统计")
    public ResponseBase getHomePageFlowBandWidthTopN(@RequestBody MWNewScreenAlertDevOpsEventDto param) {
        Reply reply;
        try {
            reply = newScreenManage.getHomePageFlowBandWidthTopN(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("新首页获取流量错误包排行失败", e);
            return setResultFail("新首页获取流量错误包排行失败", "");
        }
    }

    @PostMapping("/getScreenVersion")
    @ResponseBody
    @ApiOperation("获取首页版本")
    public ResponseBase getScreenVersion() {
        Reply reply;
        try {
            reply = Reply.ok(screenVersion);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("获取首页版本失败", e);
            return setResultFail("获取首页版本失败", "");
        }
    }


    @PostMapping("/sync/cardInfo")
    @ResponseBody
    @ApiOperation("首页同步卡片")
    public ResponseBase syncNewScreenCard() {
        Reply reply;
        try {
            reply = newScreenManage.syncNewScreenCardInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("首页同步卡片失败", "");
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("获取首页版本失败", e);
            return setResultFail("首页同步卡片失败", "");
        }
    }

    @PostMapping("/getInterfaceRate")
    @ResponseBody
    @ApiOperation("首页获取接口丢包率排行")
    public ResponseBase getInterfaceRate(@RequestBody MWNewScreenAlertDevOpsEventDto param) {
        Reply reply;
        try {
            reply = newScreenManage.getInterfaceRate(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("首页获取接口丢包率排行失败", e);
            return setResultFail("首页获取接口丢包率排行失败", "");
        }
    }

}
