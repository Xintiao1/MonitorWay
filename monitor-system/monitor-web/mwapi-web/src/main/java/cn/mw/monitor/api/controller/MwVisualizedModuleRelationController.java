package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleRelationDto;
import cn.mw.monitor.visualized.service.MwVisualizedModuleRelationService;
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

/**
 * @ClassName MwVisualizedModuleRelationController
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/12/5 11:39
 * @Version 1.0
 **/
@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "可视化生成页面关联模块")
public class MwVisualizedModuleRelationController extends BaseApiService {

    @Autowired
    private MwVisualizedModuleRelationService visualizedModuleRelationService;


    /**
     * 可视化关联模块新增
     */
    @PostMapping("/visualized/relation/module/create")
    @ResponseBody
    @ApiOperation("可视化关联模块新增")
    public ResponseBase addVisualizedModuleRelation(@RequestBody MwVisualizedModuleRelationDto visualizedModuleRelationDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = visualizedModuleRelationService.addVisualizedModuleRelation(visualizedModuleRelationDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化关联模块新增失败", null);
            }
        } catch (Throwable e) {
            log.error("MwVisualizedModuleRelationController{addVisualizedModuleRelation}",e);
            return setResultFail("可视化关联模块新增失败", null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 可视化关联模块修改
     */
    @PostMapping("/visualized/relation/module/update")
    @ResponseBody
    @ApiOperation("可视化关联模块修改")
    public ResponseBase updateVisualizedModuleRelation(@RequestBody MwVisualizedModuleRelationDto visualizedModuleRelationDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = visualizedModuleRelationService.editorVisualizedModuleRelation(visualizedModuleRelationDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化关联模块修改失败", null);
            }
        } catch (Throwable e) {
            log.error("MwVisualizedModuleRelationController{updateVisualizedModuleRelation}",e);
            return setResultFail("可视化关联模块修改失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化关联模块删除
     */
    @PostMapping("/visualized/relation/module/delete")
    @ResponseBody
    @ApiOperation("可视化关联模块删除")
    public ResponseBase deleteVisualizedModuleRelation(@RequestBody MwVisualizedModuleRelationDto visualizedModuleRelationDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = visualizedModuleRelationService.deleteVisualizedModuleRelation(visualizedModuleRelationDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化关联模块删除失败", null);
            }
        } catch (Throwable e) {
            log.error("MwVisualizedModuleRelationController{deleteVisualizedModuleRelation}",e);
            return setResultFail("可视化关联模块删除失败", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 可视化关联模块删除
     */
    @PostMapping("/visualized/relation/module/select")
    @ResponseBody
    @ApiOperation("可视化关联模块查询")
    public ResponseBase selectVisualizedModuleRelation(@RequestBody MwVisualizedModuleRelationDto visualizedModuleRelationDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = visualizedModuleRelationService.selectVisualizedModuleRelation(visualizedModuleRelationDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("可视化关联模块查询失败", null);
            }
        } catch (Throwable e) {
            log.error("MwVisualizedModuleRelationController{selectVisualizedModuleRelation}",e);
            return setResultFail("可视化关联模块查询失败", null);
        }
        return setResultSuccess(reply);
    }

}
