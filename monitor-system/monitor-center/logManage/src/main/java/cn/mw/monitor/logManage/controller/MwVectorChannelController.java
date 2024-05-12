package cn.mw.monitor.logManage.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.param.VectorChannelParam;
import cn.mw.monitor.logManage.param.VectorChannelSearchParam;
import cn.mw.monitor.logManage.service.MwVectorChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/mwapi/logManage/vectorChannel")
@RestController
@Slf4j
@Api(value = "通道管理", tags = "通道管理")
public class MwVectorChannelController extends BaseApiService {

    @Autowired
    private MwVectorChannelService mwVectorChannelService;

    @ApiOperation("添加通道")
    @PostMapping("/add")
    public ResponseBase addVectorChannel(@RequestBody VectorChannelParam param) {
        return this.mwVectorChannelService.addVectorChannel(param);
    }

    @ApiOperation("更新通道")
    @PostMapping("/update")
    public ResponseBase updateVectorChannel(@RequestBody VectorChannelParam param) {
        return this.mwVectorChannelService.updateVectorChannel(param);
    }

    /**
     * 条件查询vector通道
     *
     * @param searchParam
     * @return
     */
    @ApiOperation("条件查询vector通道")
    @PostMapping("/list")
    public ResponseBase list(@RequestBody VectorChannelSearchParam searchParam) {
        return mwVectorChannelService.selectList(searchParam);
    }

    /**
     * 根据ID删除vector通道逻辑删除
     *
     * @param vectorChannelIds
     * @return
     */
    @PostMapping("/delete")
    public ResponseBase deleteVectorChannelByIds(@RequestBody List<Integer> vectorChannelIds) {
        return this.mwVectorChannelService.deleteVectorChannel(vectorChannelIds);
    }

}
