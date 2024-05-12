package cn.mw.monitor.assets.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assets.dto.AssetsGroupDTO;
import cn.mw.monitor.assets.dto.MwAssetsGroupMapper;
import cn.mw.monitor.assets.param.AssetsInfoSyncByNameParam;
import cn.mw.monitor.assets.service.MwAssetsInfoSyncService;
import cn.mw.zbx.MWTPServerAPI;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author qzg
 * @date 2021/5/27
 */
@RequestMapping("/mwapi/assetsInfoSync")
@Controller
@Slf4j
@Api(value = "资产管理数据同步接口", tags = "资产管理数据同步接口")
public class MwAssetsInfoSyncController extends BaseApiService {
    @Autowired
    private MwAssetsInfoSyncService mwAssetsInfoSyncService;

    @PostMapping("/getAssetsInfo")
    @ResponseBody
    @ApiOperation("同步Zabbix资产数据接口")
    public ResponseBase assetsInfoSync(@RequestBody AssetsGroupDTO assetsGroupDTO) {
        Reply reply;
        try {
            reply = mwAssetsInfoSyncService.assetsInfoSync(assetsGroupDTO.getGroupName());
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("数据同步失败", e);
            return setResultFail("数据同步失败", "");
        }
    }


    @PostMapping("/syncAssetsName")
    @ResponseBody
    @ApiOperation("同步资产名称到zabbix可见名称")
    public ResponseBase syncAssetsNameReachZabbix(@RequestBody AssetsGroupDTO assetsGroupDTO) {
        Reply reply;
        try {
            reply = mwAssetsInfoSyncService.syncAssetsNameReachZabbix(assetsGroupDTO.getType());
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("同步资产名称到zabbix可见名称失败", e);
            return setResultFail("同步资产名称到zabbix可见名称失败", "");
        }
    }

}
