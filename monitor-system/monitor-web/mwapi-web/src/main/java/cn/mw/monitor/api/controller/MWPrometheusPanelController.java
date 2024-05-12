package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.prometheus.dto.LayoutConfigDto;
import cn.mw.monitor.prometheus.dto.PanelConfigDto;
import cn.mw.monitor.prometheus.service.impl.PanelConfigServiceImpl;
import cn.mw.monitor.prometheus.utils.PrometheusApiConnectorFactory;
import cn.mw.monitor.prometheus.vo.PanelQueryParamVo;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/mwapi/prometheus")
@RestController
@Api(value = "prometheus容器面板", tags = "prometheus容器面板")
@Slf4j
public class MWPrometheusPanelController extends BaseApiService {

    @Autowired
    private PanelConfigServiceImpl panelConfigService;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getAllPanelConfigs")
    @ResponseBody
    @ApiOperation(value = "查询当前用户配置的面板信息")
    public ResponseBase getAllPanelConfigs(){
        Reply reply;
        try {
            reply = panelConfigService.getAllPanelConfigs(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllPanelConfigs error,{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.PROMETHEUS_QUERY_PANEL_ERROR_MSG_321001);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/getPanelData")
    @ResponseBody
    @ApiOperation(value = "查询面板指标信息")
    public ResponseBase getPanelData(@RequestBody PanelQueryParamVo panelQueryParamVo){
        Reply reply;
        try {
            reply = panelConfigService.getPanelData(panelQueryParamVo);
        } catch (Exception e) {
            log.error("getPanelData error,{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.PROMETHEUS_QUERY_PANEL_ERROR_MSG_322001);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/insertPanelConfig")
    @ResponseBody
    @ApiOperation(value = "新增查询面板")
    public ResponseBase insertPanelConfig(@RequestBody PanelConfigDto panelConfigDto){
        Reply reply;
        try {
            panelConfigDto.setCreator(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
            reply = panelConfigService.insertPanelData(panelConfigDto);
        } catch (Exception e) {
            log.error("insertPanelConfig error,{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.PROMETHEUS_QUERY_PANEL_ERROR_MSG_323001);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/updatePanelConfig")
    @ResponseBody
    @ApiOperation(value = "修改查询面板")
    public ResponseBase updatePanelConfig(@RequestBody PanelConfigDto panelConfigDto){
        Reply reply;
        try {
            reply = panelConfigService.updatePanelData(panelConfigDto);
        } catch (Exception e) {
            log.error("updatePanelConfig error,{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.PROMETHEUS_QUERY_PANEL_ERROR_MSG_324001);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @GetMapping("/deletePanelConfig")
    @ResponseBody
    @ApiOperation(value = "删除查询面板")
    public ResponseBase deletePanelConfig(Integer panelId) {
        Reply reply;
        try {
            reply = panelConfigService.deletePanelData(panelId);
        } catch (Exception e) {
            log.error("updatePanelConfig error,{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.PROMETHEUS_QUERY_PANEL_ERROR_MSG_325001);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/insertLayoutConfig")
    @ResponseBody
    @ApiOperation(value = "新增布局")
    public ResponseBase insertLayoutConfig(@RequestBody LayoutConfigDto layoutConfigDto) {
        Reply reply;
        try {
            layoutConfigDto.setCreator(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
            reply = panelConfigService.insertLayoutConfig(layoutConfigDto);
        } catch (Exception e) {
            log.error("insertLayoutConfig error,{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.PROMETHEUS_QUERY_PANEL_ERROR_MSG_326001);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @GetMapping("/getAllQuerySql")
    @ResponseBody
    @ApiOperation(value = "获取查询指标脚本")
    public ResponseBase getAllQuerySql() {
        Reply reply;
        try {
            reply = panelConfigService.getAllQuerySql();
        } catch (Exception e) {
            log.error("getAllQuerySql error,{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.PROMETHEUS_QUERY_PANEL_ERROR_MSG_327001);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @GetMapping("/getAllPrometheusProperties")
    @ResponseBody
    @ApiOperation(value = "获取所有字段映射关系")
    public ResponseBase getAllPrometheusProperties() {
        Reply reply;
        try {
            reply = panelConfigService.getAllPrometheusProperties();
        } catch (Exception e) {
            log.error("getAllPrometheusProperties error,{}", e);
            return setResultFail(e.getMessage(), ErrorConstant.PROMETHEUS_QUERY_PANEL_ERROR_MSG_328001);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "assets_manage")
    @GetMapping("/deleteLayoutConfig")
    @ResponseBody
    @ApiOperation(value = "删除布局")
    public ResponseBase deleteLayoutConfig(Integer id) {
        return setResultSuccess(panelConfigService.deleteLayoutConfig(id));
    }
}
