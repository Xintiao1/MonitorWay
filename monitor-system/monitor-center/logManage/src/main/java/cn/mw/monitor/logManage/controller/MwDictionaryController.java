package cn.mw.monitor.logManage.controller;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.constant.LogManageConstant;
import cn.mw.monitor.logManage.param.DictionaryAddParam;
import cn.mw.monitor.logManage.param.DictionarySearchParam;
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

@Api(value = "解析字典", tags = "解析字典")
@Slf4j
@RestController
@RequestMapping("/mwapi/logManage/dictionary")
public class MwDictionaryController {

    @Autowired
    private AbstractApiTemplate apiTemplate;

    /**
     * 添加转发配置
     */
    @ApiOperation("添加或更新数据模型")
    @PostMapping("/addOrUpdate")
    public ResponseBase add(@RequestBody DictionaryAddParam param) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_DICTIONARY_URL_PREFIX + "addOrUpdate", param);
    }

    /**
     * 根据ID删除转发配置 逻辑删除
     */
    @ApiOperation("根据ID删除数据模型")
    @PostMapping("/delete")
    public ResponseBase deleteByIds(@RequestBody List<Integer> ids) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_DICTIONARY_URL_PREFIX + "delete", ids);
    }

    /**
     * 条件查询转发配置
     */
    @ApiOperation("条件查询数据模型")
    @PostMapping("/list")
    public ResponseBase list(@RequestBody DictionarySearchParam searchParam) {
        return apiTemplate.executeApi(LogManageConstant.VECTOR_DICTIONARY_URL_PREFIX + "list", searchParam);
    }

}
