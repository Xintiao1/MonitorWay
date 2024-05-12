package cn.mw.monitor.logManage.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.constant.LogManageConstant;
import cn.mw.monitor.logManage.param.ParsingRuleAddParam;
import cn.mw.monitor.logManage.param.ParsingRuleSearchParam;
import cn.mw.monitor.logManage.service.AbstractApiTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(value = "解析规则", tags = "解析规则")
@Slf4j
@RestController
@RequestMapping("/mwapi/logManage/parsingRule")
public class MwParsingRuleController extends BaseApiService {


    @Autowired
    private AbstractApiTemplate apiTemplate;

    @ApiOperation("添加或更新解析规则")
    @PostMapping("/addOrUpdate")
    public ResponseBase addOrUpdate(@RequestBody ParsingRuleAddParam param) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_PARSING_RULE_URL_PREFIX + "addOrUpdate", param);

    }

    @ApiOperation("删除解析规则")
    @PostMapping("/delete")
    public ResponseBase delete(@RequestBody List<Integer> ids) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_PARSING_RULE_URL_PREFIX + "delete", ids);

    }

    @ApiOperation("查询解析规则")
    @PostMapping("/list")
    public ResponseBase list(@RequestBody ParsingRuleSearchParam searchParam) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_PARSING_RULE_URL_PREFIX + "list", searchParam);

    }

    @ApiOperation("获取定义源动态字段")
    @PostMapping("/getSourcesExtend")
    public ResponseBase getSourcesExtend() {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_PARSING_RULE_URL_PREFIX + "getSourcesExtend", "");

    }


}
