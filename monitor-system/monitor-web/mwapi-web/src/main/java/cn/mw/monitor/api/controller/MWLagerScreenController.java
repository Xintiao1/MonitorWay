package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.screen.model.MapParam;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.constant.PaasConstant;
import com.alibaba.fastjson.JSON;
import org.apache.ibatis.annotations.Param;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.screen.dto.PermDto;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.screen.param.*;
import cn.mw.monitor.screen.service.MWLagerScreenService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;


/**
 * @author xhy
 * @date 2020/4/9 10:35
 */
@RequestMapping("/mwapi")
@Controller
@Api(value = "大屏", tags = "大屏")
@Slf4j
public class MWLagerScreenController extends BaseApiService {
    private static final Logger dbLogger = LoggerFactory.getLogger("MWDBLogger");
    @Autowired
    private MWLagerScreenService mwLagerScreenService;
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/getLayoutBase/browse")
    @ResponseBody
    @ApiOperation(value = "显示布局基础,如九宮格")
    public ResponseBase getLayoutBase() {
        Reply reply;
        try {
            reply = mwLagerScreenService.getLayoutBase();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(ErrorConstant.SCREEN_LAYOUT_MSG_304001, "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/getModelType/browse")
    @ResponseBody
    @ApiOperation(value = "显示组件类型")
    public ResponseBase getModelType() {
        Reply reply;
        try {
            reply = mwLagerScreenService.getModelType();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(ErrorConstant.SCREEN_MODEL_TYPE_MSG_304004, "");
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/getModelList/browse")
    @ResponseBody
    @ApiOperation(value = "显示组件基础")
    public ResponseBase getModelList() {
        Reply reply;
        try {
            reply = mwLagerScreenService.getModelList();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(ErrorConstant.SCREEN_MODEL_MSG_304002, "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/browse")
    @ResponseBody
    @ApiOperation(value = "查询所有的投屏")
    public ResponseBase getLagerScreenList(@RequestBody PermDto permDto) {
        Reply reply;
        try {
            reply = mwLagerScreenService.getLagerScreenList(permDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("查询所有的投屏失败", "");
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/browseById")
    @ResponseBody
    @ApiOperation(value = "修改前查询投屏")
    public ResponseBase getLagerScreenById(@RequestBody PermDto permDto) {
        Reply reply;
        try {
            reply = mwLagerScreenService.getLagerScreenById(permDto.getScreenId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("修改前查询投屏失败", "");
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @GetMapping("/ws/screen/popup/browse")
    @ResponseBody
    @ApiOperation(value = "查询组件对应的数据")
    public ResponseBase getDataListByModelDataId(@Param("modelDataId") String modelDataId,@Param("userId") Integer userId) {
        Reply reply;
        try {
            reply = mwLagerScreenService.getDataListByModelDataId1(modelDataId,userId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(ErrorConstant.SCREEN_MODEL_TYPE_MSG_304004, "");
        }
        return setResultSuccess(reply);
    }


//    /**
//     * 由于webSocket性能开销问题，一个大屏每个组件都开启一个webSocket,对于网络性能要求很高
//     * 优化方法：
//     * 根据大屏id,查询所有组件数据，只开启一个webSocket推送到前端
//     * @param screenId
//     * @param userId
//     * @return
//     */
//    @GetMapping("/ws/screen/popup/browse1")
//    @ResponseBody
//    @ApiOperation(value = "查询组件对应的数据")
//    public ResponseBase getDataListByScreenId(@Param("screenId") String screenId,@Param("userId") Integer userId) {
//        Reply reply;
//        try {
//            reply = mwLagerScreenService.getDataListByScreenId(screenId,userId);
//            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
//                return setResultFail(reply.getMsg(), reply.getData());
//            }
//        } catch (Throwable e) {
//            logger.error(e.getMessage());
//            return setResultFail(e.getMessage(), ErrorConstant.SCREEN_MODEL_TYPE_MSG_304004);
//        }
//        return setResultSuccess(reply);
//    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/getRolePermission/browse")
    @ResponseBody
    @ApiOperation(value = "查询用户登录角色")
    public ResponseBase getRolePermission() {
        Reply reply;
        try {
            reply = mwLagerScreenService.getRolePermission();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("查询用户登录角色失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/create")
    @ResponseBody
    @ApiOperation(value = "创建大屏")
    public ResponseBase addLagerScreen(@RequestBody MwLagerScreenParam mwLagerScreenParam) {
        Reply reply;
        try {
            SystemLogDTO systemLogDTO = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("监控大屏")
                    .objName(mwLagerScreenParam.getScreenName()).operateDes("创建大屏" + mwLagerScreenParam.getScreenName()).build();
            dbLogger.info(JSON.toJSONString(systemLogDTO));
            reply = mwLagerScreenService.addLagerScreen(mwLagerScreenParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("创建大屏失败", "");
        }
        return setResultSuccess(reply);
    }

    //修改大屏
    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/editor")
    @ResponseBody
    @ApiOperation(value = "修改大屏")
    public ResponseBase updateLagerScreen(@RequestBody MwLagerScreenParam mwLagerScreenParam) {
        Reply reply;
        try {
            reply = mwLagerScreenService.updateLagerScreen(mwLagerScreenParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("修改大屏失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/delete")
    @ResponseBody
    @ApiOperation(value = "删除大屏")
    public ResponseBase deleteLagerScreen(@RequestParam String screenId) {

        Reply reply;
        try {
            reply =  mwLagerScreenService.deleteLagerScreen(screenId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("删除大屏失败", "");
        }
        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/model/popup/create")
    @ResponseBody
    @ApiOperation(value = "添加大屏对应的组件,块，布局数据")
    public ResponseBase addLagerScreenData(@RequestBody ModelDataParam modelDataParam) {
        Reply reply;
        try {
            reply = mwLagerScreenService.addLagerScreenData(modelDataParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("添加大屏对应的组件失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/model/popup/editor")
    @ResponseBody
    @ApiOperation(value = "修改组件数据")
    public ResponseBase updateModelData(@RequestBody UpdateModelDataParam updateModelDataParam) {
        Reply reply;
        try {
            reply = mwLagerScreenService.updateModelData(updateModelDataParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("修改组件数据失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/popup/delete")
    @ResponseBody
    @ApiOperation(value = "删除组件数据")
    public ResponseBase deleteModelData(@RequestParam  String modelDataId) {
        Reply reply;
        try {
            reply = mwLagerScreenService.deleteModelData(modelDataId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("删除组件数据失败", "");
        }
        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/popup/perform")
    @ResponseBody
    @ApiOperation(value = "保存大屏截图")
    public ResponseBase saveScreenImg(@RequestBody ImgParam imgParam) {
        Reply reply;
        try {
            reply = mwLagerScreenService.saveScreenImg(imgParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("保存大屏截图失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/popup/editor")
    @ResponseBody
    @ApiOperation(value = "修改大屏名称")
    public ResponseBase updateScreenName(@RequestBody ScreenNameParam screenNameParam) {
        Reply reply;
        try {
            reply = mwLagerScreenService.updateScreenName(screenNameParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("修改大屏名称失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/perform")
    @ResponseBody
    @ApiOperation(value = "是否启用大屏")
    public ResponseBase updateEnableLagerScreen(@RequestBody  EnableParam enableParam) {
        Reply reply;
        try {
            reply =mwLagerScreenService.updateEnable(enableParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("是否启用大屏失败", "");
        }
        return setResultSuccess(reply);

    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/filterAssets/editor")
    @ResponseBody
    @ApiOperation(value = "添加首页/大屏不同的资产过滤")
    public ResponseBase editorFilterAssets(@RequestBody FilterAssetsParam param) {
        Reply reply;
        try {
            reply = mwLagerScreenService.editorFilterAssets(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("资产过滤失败", reply.getData());
            }
        } catch (Throwable e) {
            log.error("editorFilterAssets",e);
            return setResultFail("添加首页/大屏不同的资产过滤失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/filterAssets/browse")
    @ResponseBody
    @ApiOperation(value = "查询首页/大屏不同的资产过滤")
    public ResponseBase getFilterAssets(@RequestBody FilterAssetsParam param) {
        Reply reply;
        try {
            reply = mwLagerScreenService.getFilterAssets(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return setResultFail("查询首页/大屏不同的资产过滤失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/linkRank/editor")
    @ResponseBody
    @ApiOperation(value = "大屏线路组件编辑")
    public ResponseBase saveLinkRankCustomal(@RequestBody FilterAssetsParam param) {
        Reply reply;
        try {
            reply = mwLagerScreenService.editLinkRank(param.getModelDataId(),param.getInterfaceIds(),param.getLinkType());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("大屏线路组件编辑失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/linkOption/browse")
    @ResponseBody
    @ApiOperation(value = "大屏线路回显")
    public ResponseBase getLinkEdit(@PathParam("modelDataId") String modelDataId){
        Reply reply;
        try {
            reply = mwLagerScreenService.getLinkOption(modelDataId);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("大屏线路回显失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/coordinate/browse")
    @ResponseBody
    @ApiOperation(value = "大屏地图经纬度查询")
    public ResponseBase selectCoordinate(){
        Reply reply;
        try {
            reply = mwLagerScreenService.getCoordinate();
            if(null!=reply && reply.getRes()!=PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(),reply.getData());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return setResultFail("大屏地图经纬度查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "monitor_screen")
    @PostMapping("/screen/linkLine/browse")
    @ResponseBody
    @ApiOperation(value = "大屏地图线路连线")
    public ResponseBase selectScreenLinkLine(@RequestBody MapParam mapParam){
        Reply reply;
        try {
            reply = mwLagerScreenService.getIcmpLink(mapParam.isFirst());
            if(null!=reply && reply.getRes()!=PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(),reply.getData());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return setResultFail("大屏地图线路连线失败", "");
        }
        return setResultSuccess(reply);
    }

}
