package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.visualized.dto.MwVisualizedClassifyDto;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleBusinSatusDto;
import cn.mw.monitor.visualized.dto.MwVisualizedQueryValueDTO;
import cn.mw.monitor.visualized.dto.MwVisualizedViewDto;
import cn.mw.monitor.visualized.param.MwVisualizedIndexQueryParam;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.param.MwVisualizedZkSoftWareParam;
import cn.mw.monitor.visualized.service.MwVisualizedExportService;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedMenuService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @ClassName MwVisualizedManageController
 * @Author gengjb
 * @Date 2022/4/21 15:04
 * @Version 1.0
 **/
@RequestMapping("/mwapi/visualized")
@Controller
@Slf4j
@Api(value = "可视化基础接口")
public class MwVisualizedManageController extends BaseApiService {

    @Autowired
    private MwVisualizedManageService manageService;

    @Autowired
    private MwVisualizedMenuService menuService;

    @Autowired
    private MwVisualizedExportService visualizedExportService;

    /**
     * 可视化视图分类添加
     */
    @PostMapping("/classift/create")
    @ResponseBody
    @ApiOperation("可视化视图分类添加")
    public ResponseBase addVisualizedClassify(@RequestBody MwVisualizedClassifyDto visualizedClassifyDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.addVisualizedClassify(visualizedClassifyDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化视图分类添加失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化视图分类添加失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化视图分类修改
     */
    @PostMapping("/classift/update")
    @ResponseBody
    @ApiOperation("可视化视图分类修改")
    public ResponseBase updateVisualizedClassify(@RequestBody MwVisualizedClassifyDto visualizedClassifyDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.updateVisualizedClassify(visualizedClassifyDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化视图分类修改失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化视图分类修改失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化视图分类删除
     */
    @PostMapping("/classift/delete")
    @ResponseBody
    @ApiOperation("可视化视图分类删除")
    public ResponseBase deleteVisualizedClassify(@RequestBody MwVisualizedClassifyDto visualizedClassifyDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.deleteVisualizedClassify(visualizedClassifyDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化视图分类删除失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化视图分类删除失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化视图分类查询
     */
    @PostMapping("/classift/browse")
    @ResponseBody
    @ApiOperation("可视化视图分类查询")
    public ResponseBase selectVisualizedClassify() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.selectVisualizedClassify();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化视图分类查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化视图分类查询失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化视图查询
     */
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation("可视化视图新增")
    public ResponseBase addVisualizedClassify(@RequestBody MwVisualizedViewDto viewDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.addVisualizedView(viewDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化视图新增失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化视图新增失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化视图修改
     */
    @PostMapping("/editor")
    @ResponseBody
    @ApiOperation("可视化视图修改")
    public ResponseBase updateVisualizedView(@RequestBody MwVisualizedViewDto viewDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.updateVisualizedView(viewDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化视图修改失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化视图修改失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化视图修改
     */
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation("可视化视图删除")
    public ResponseBase deleteVisualizedView(@RequestBody MwVisualizedViewDto viewDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.deleteVisualizedView(viewDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化视图删除失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化视图删除失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化视图查询
     */
    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation("可视化视图查询")
    public ResponseBase selectVisualizedView(@RequestBody MwVisualizedViewDto viewDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.selectVisualizedView(viewDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化视图查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化视图查询失败", null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 可视化根据指标查询数据
     */
    @PostMapping("/getIndexData")
    @ResponseBody
    @ApiOperation("可视化指标查询数据")
    public ResponseBase queryVisualizedItem(@RequestBody MwVisualizedIndexQueryParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.queryVisualizedItem(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化指标查询数据失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化指标查询数据失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化根据指标查询数据
     */
    @PostMapping("/editor/perform")
    @ResponseBody
    @ApiOperation("可视化编辑查询")
    public ResponseBase visualizedUpdateQuery(@RequestBody MwVisualizedViewDto viewDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.visualizedUpdateQuery(viewDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化编辑查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化编辑查询失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询基线的监控项数据
     */
    @PostMapping("/image/upload")
    @ResponseBody
    @ApiOperation("可视化背景图片上传")
    public ResponseBase visualizedBackDropImageUpload(@RequestBody MultipartFile file,@RequestParam("visualizedId") Integer id) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = menuService.addVisualizedImageUpload(file,id);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化背景图片上传失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化背景图片上传失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化根据指标查询数据
     */
    @PostMapping("/export")
    @ResponseBody
    @ApiOperation("可视化导出")
    public ResponseBase visualizedExport(@RequestBody MwVisualizedViewDto viewDto,HttpServletResponse response) {
        try {
            // 验证内容正确性
           visualizedExportService.export(response,viewDto);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化导出失败", null);
        }
        return setResultSuccess(Reply.ok("导出成功"));
    }

    /**
     * 可视化中控大屏数据
     */
    @PostMapping("/getZkSoftWareData")
    @ResponseBody
    @ApiOperation("可视化中控大屏数据")
    public ResponseBase queryVisualizedZkSoftWare(@RequestBody MwVisualizedZkSoftWareParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.selectVisualizedZkSoftWare(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化中控大屏数据失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化中控大屏数据失败", null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 可视化中控大屏告警趋势
     */
    @PostMapping("/zkSoftWareAlert/getTrend")
    @ResponseBody
    @ApiOperation("可视化中控大屏告警趋势")
    public ResponseBase getVisualizedZkSoftWareAlertTrend(@RequestBody MwVisualizedZkSoftWareParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.selectVisualizedZkSoftWareAlertTrend(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化中控大屏告警趋势失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化中控大屏告警趋势失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化中控大屏告警趋势
     */
    @PostMapping("/module/browse")
    @ResponseBody
    @ApiOperation("可视化组件区数据查询")
    public ResponseBase getModelInfo(@RequestBody MwVisualizedModuleParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.selectVisualizedModule(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化组件区数据查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化组件区数据查询失败", null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 可视化组件区选择业务数据
     */
    @PostMapping("/module/business/browse")
    @ResponseBody
    @ApiOperation("可视化组件区绑定业务查询")
    public ResponseBase getModuleBusinessTreeInfo() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.getBusinessTreeInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化组件区绑定业务查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化组件区绑定业务查询失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化资产类型分组
     */
    @PostMapping("/assetsTypeGroup/browse")
    @ResponseBody
    @ApiOperation("可视化资产类型分组")
    public ResponseBase getAssetsTypeGroup(@RequestBody MwVisualizedModuleParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.getAssetsTypeGroup(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化资产类型分组失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化资产类型分组失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化下拉数据查询
     */
    @PostMapping("/dropdown/browse")
    @ResponseBody
    @ApiOperation("可视化下拉数据查询")
    public ResponseBase getDropDownInfo(@RequestBody MwVisualizedModuleParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.gettVisualizedDropDownInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化下拉数据查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化下拉数据查询失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化下拉数据查询
     */
    @PostMapping("/containe/dropdown/browse")
    @ResponseBody
    @ApiOperation("可视化下拉数据查询")
    public ResponseBase getContaineDropDownInfo(@RequestBody MwVisualizedModuleParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.getVisualizedContaineDropDown(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化下拉数据查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化下拉数据查询失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 创建可视化业务状态标题分区信息
     */
    @PostMapping("/businstatus/create")
    @ResponseBody
    @ApiOperation("可视化业务状态标题创建")
    public ResponseBase createBusinStatusTilte(@RequestBody List<MwVisualizedModuleBusinSatusDto> businSatusDtos) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.createVisualizedBusinStatusTitle(businSatusDtos);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化业务状态标题创建失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化业务状态标题创建失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询可视化业务状态标题分区信息
     */
    @PostMapping("/businstatus/browse")
    @ResponseBody
    @ApiOperation("可视化业务状态标题查询")
    public ResponseBase selectBusinStatusTilte(@RequestBody MwVisualizedModuleBusinSatusDto businSatusDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.selectVisualizedBusinStatusTitle(businSatusDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化业务状态标题查询失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化业务状态标题查询失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询可视化业务状态标题分区信息
     */
    @PostMapping("/businstatus/dropDown")
    @ResponseBody
    @ApiOperation("可视化业务状态下拉")
    public ResponseBase selectBusinStatusDropDown() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.selectVisualizedBusinStatusDropDown();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化业务状态下拉失败", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("可视化业务状态下拉失败", null);
        }
        return setResultSuccess(reply);
    }



    /**
     * 保存前端数据
     */
    @PostMapping("/saveQueryValue/insert")
    @ResponseBody
    @ApiOperation("可视化编辑查询")
    public ResponseBase saveVisualizedQueryValue(@RequestBody MwVisualizedIndexQueryParam viewDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.saveVisualizedQueryValue(viewDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("saveVisualizedQueryValue() error", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("saveVisualizedQueryValue() error", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询前端保存的数据
     */
    @PostMapping("/getQueryValue/browse")
    @ResponseBody
    @ApiOperation("可视化编辑查询")
    public ResponseBase getVisualizedQueryValue(@RequestBody MwVisualizedQueryValueDTO viewDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = manageService.getVisualizedQueryValue(viewDto.getId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("getVisualizedQueryValue() error", null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("getVisualizedQueryValue() error", null);
        }
        return setResultSuccess(reply);
    }
}
