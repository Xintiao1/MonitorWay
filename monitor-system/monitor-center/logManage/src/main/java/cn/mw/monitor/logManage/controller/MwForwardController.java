package cn.mw.monitor.logManage.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.constant.LogManageConstant;
import cn.mw.monitor.logManage.param.ForWardAddParam;
import cn.mw.monitor.logManage.param.MwForwardSearchParam;
import cn.mw.monitor.logManage.param.ParsingLibSearchParam;
import cn.mw.monitor.logManage.service.MwForWardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(value = "转发配置", tags = "转发配置")
@Slf4j
@RestController
@RequestMapping("/mwapi/logManage/forward")
public class MwForwardController extends BaseApiService {

    @Autowired
    private MwForWardService mwForWardService;

    /**
     * 添加转发配置
     */
    @ApiOperation(value = "添加转发配置")
    @PostMapping("/add")
    public ResponseBase add(@RequestBody ForWardAddParam param) {
        return this.mwForWardService.addForWard(param);
    }

    /**
     * 更新转发配置
     */
    @ApiOperation(value = "更新转发配置")
    @PostMapping("/update")
    public ResponseBase update(@RequestBody ForWardAddParam param) {
        return this.mwForWardService.updateForWard(param);
    }

    /**
     * 条件查询转发配置
     */
    @ApiOperation(value = "条件查询转发配置")
    @PostMapping("/list")
    public ResponseBase list(@RequestBody MwForwardSearchParam searchParam) {
        return mwForWardService.pageList(searchParam);
    }

    /**
     * 根据ID删除转发配置 逻辑删除
     */
    @ApiOperation(value = "根据ID删除转发配置")
    @PostMapping("/delete")
    public ResponseBase deleteByIds(@RequestBody List<Integer> ids) {
        return this.mwForWardService.deleteByIds(ids);
    }

    @ApiOperation(value = "获取常用枚举值")
    @PostMapping("/getEnumValue")
    public ResponseBase geEnumValue() {
        return this.mwForWardService.getEnumValue();
    }

}
