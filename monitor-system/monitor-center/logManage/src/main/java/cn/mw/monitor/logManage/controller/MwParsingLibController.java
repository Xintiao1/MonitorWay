package cn.mw.monitor.logManage.controller;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.constant.LogManageConstant;
import cn.mw.monitor.logManage.param.ParsingLibAddParam;
import cn.mw.monitor.logManage.param.ParsingLibDeleteParam;
import cn.mw.monitor.logManage.param.ParsingLibProcessingParam;
import cn.mw.monitor.logManage.param.ParsingLibSearchParam;
import cn.mw.monitor.logManage.service.AbstractApiTemplate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "数据模型", tags = "解析库")
@Slf4j
@RestController
@RequestMapping("/mwapi/logManage/parsingLib")
public class MwParsingLibController {

    @Autowired
    private AbstractApiTemplate apiTemplate;

    @ApiOperation("样本解析")
    @PostMapping("/parse")
    public ResponseBase parse(@RequestBody String text) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_PARSING_LIB_URL_PREFIX + "parse", text);

    }

    @ApiOperation("新建解析库")
    @PostMapping("/addOrUpdate")
    public ResponseBase addOrUpdateParsingLib(@RequestBody ParsingLibAddParam param) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_PARSING_LIB_URL_PREFIX + "addOrUpdate", param);

    }

    @ApiOperation("数据处理")
    @PostMapping("/dataProcessing")
    public ResponseBase dataConversion(@RequestBody ParsingLibProcessingParam param) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_PARSING_LIB_URL_PREFIX + "dataProcessing", param);

    }

    @ApiOperation("删除解析库")
    @PostMapping("/delete")
    public ResponseBase delete(@RequestBody ParsingLibDeleteParam deleteParam) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_PARSING_LIB_URL_PREFIX + "delete", deleteParam);

    }

    @ApiOperation("查询解析库")
    @PostMapping("/list")
    public ResponseBase list(@RequestBody ParsingLibSearchParam searchParam) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_PARSING_LIB_URL_PREFIX + "list", searchParam);

    }



}
