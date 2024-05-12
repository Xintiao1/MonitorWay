package cn.mw.monitor.logManage.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.param.LogAnalysisParam;
import cn.mw.monitor.logManage.service.MwLogManageService;
import cn.mw.monitor.logManage.vo.TableNameInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping("/mwapi")
@RestController
@Slf4j
@Api(value = "日志分析", tags = "日志分析")
public class MwLogManageController extends BaseApiService {

    @Autowired
    private MwLogManageService logManageService;

    @GetMapping("/logManage/getTables")
    @ApiOperation("获取所有表名")
    public ResponseBase getTables() {
        try {
            List<TableNameInfo> tables = logManageService.getTables();
            return this.setResultSuccess(tables);
        } catch (Exception e) {
            return this.setResultFail("获取所有表名异常", null);
        }

    }


    @ApiOperation("获取表名字段")
    @GetMapping("/logManage/getColumnByTable")
    public ResponseBase getColumnByTable(@ApiParam("表名") @RequestParam("tableName") String tableName) {
        if (StringUtils.isEmpty(tableName)) {
            return this.setResultFail("查询表名不能为空", null);
        }
        try {
            List<Map<String, Object>> columnByTable = logManageService.getColumnByTable(tableName);
            return this.setResultSuccess(columnByTable);
        } catch (Exception e) {
            return this.setResultFail("获取字段异常", null);
        }
    }

    @ApiOperation(value = "获取日志信息")
    @PostMapping("/logManage/list")
    public ResponseBase list(@RequestBody LogAnalysisParam logAnalysisParam) {
        try {
            Object object = logManageService.list(logAnalysisParam);
            return this.setResultSuccess(object);
        } catch (Exception e) {
            return this.setResultFail("获取日志信息异常", null);
        }
    }

    @ApiOperation(value = "获取直方图信息")
    @PostMapping("/logManage/analysisChar")
    public ResponseBase logAnalysisChar(@RequestBody LogAnalysisParam logAnalysisParam) {
        try {
            Object object = logManageService.logAnalysisChar(logAnalysisParam);
            return this.setResultSuccess(object);
        } catch (Exception e) {
            return this.setResultFail("获取直方图信息异常", null);
        }
    }

    @ApiOperation("保存选定字段")
    @PostMapping("/logManage/saveSelectedColumns")
    public ResponseBase saveSelectedColumns(@RequestBody String columnsParam) {
        try {
            String result = logManageService.saveSelectedColumns(columnsParam);
            if ("success".equals(result)) {
                return this.setResultSuccess();
            }
        } catch (Exception e) {
            return this.setResultFail("保存选定字段异常", null);
        }
        return this.setResultFail("保存选定字段异常", null);
    }

    @ApiOperation("获取页面缓存数据")
    @PostMapping("/logManage/getLogAnalysisCacheInfo")
    public ResponseBase getLogAnalysisCacheInfo() {
        try {
            Object logAnalysisCacheInfo = logManageService.getLogAnalysisCacheInfo();
            return this.setResultSuccess(logAnalysisCacheInfo);
        } catch (Exception e) {
            return this.setResultFail("获取页面缓存数据", null);
        }
    }

}
