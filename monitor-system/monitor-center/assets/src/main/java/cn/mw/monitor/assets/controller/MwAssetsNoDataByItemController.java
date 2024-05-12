package cn.mw.monitor.assets.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assets.service.MwAssetsNoDataByItemService;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author qzg
 * @date 2021/10/14
 */
@RequestMapping("/mwapi/assetsNoData")
@Controller
@Slf4j
@Api(value = "查询资产无数据的监控项接口", tags = "查询资产无数据的监控项接口")
public class MwAssetsNoDataByItemController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("MwAssetsNoDataByItem");
    @Autowired
    MwAssetsNoDataByItemService mwAssetsNoDataByItemService;


    @PostMapping("/getItemInfo")
    @ResponseBody
    @ApiOperation("查询资产无数据的监控项接口")
    public ResponseBase getNoDataAssets(HttpServletRequest request, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwAssetsNoDataByItemService.getNoDataAssets(request, response);
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("数据同步失败", e);
            return setResultFail("数据同步失败", "");
        }
    }

}
