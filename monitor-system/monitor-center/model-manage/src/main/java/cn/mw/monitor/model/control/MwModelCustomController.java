package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.model.param.QueryModelCustomPageParam;
import cn.mw.monitor.service.model.param.QueryModelGroupParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceParam;
import cn.mw.monitor.service.model.service.MwModelCustomService;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xhy
 * @date 2021/2/20 8:58
 */
@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "模型管理列接口", tags = "模型管理列接口")
public class MwModelCustomController extends BaseApiService {

    @Autowired
    private MwModelCustomService mwModelCustomService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/modelCustom/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型")
    public ResponseBase selectModelAllCustom(@RequestBody QueryModelCustomPageParam pageParam) {
        Reply reply;
        try {
            reply = mwModelCustomService.selectModelAllCustom(pageParam);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelAllCustom{}", e);
            return setResultFail("查询模型失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/modelPropertiesCustom/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型实例对应的属性名称")
    public ResponseBase selectModelPropertiesAllCustom(@RequestBody QueryModelInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelCustomService.selectModelPropertiesAllCustom(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelPropertiesAllCustom{}", e);
            return setResultFail("查询模型实例对应的属性名称失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/modelIcon/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型所有的图标名称")
    public ResponseBase selectModelAllIcon() {
        Reply reply;
        try {
            reply = mwModelCustomService.selectModelAllIcon();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelAllIcon{}", e);
            return setResultFail("查询模型所有的图标失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/selectModelGroupList")
    @ResponseBody
    @ApiOperation(value = "查询模型分组")
    public ResponseBase selectModelGroupList(@RequestBody QueryModelGroupParam param) {
        Reply reply;
        try {
            reply = mwModelCustomService.selectModelGroupList(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelList{}", e);
            return setResultFail("查询模型分组失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/selectPropertiesList")
    @ResponseBody
    @ApiOperation(value = "查询模型属性值类型")
    public ResponseBase selectPropertiesList() {
        Reply reply;
        try {
            reply = mwModelCustomService.selectPropertiesList();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelList{}", e);
            return setResultFail("查询模型属性值类型失败", "");
        }
    }
}
