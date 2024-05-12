package cn.mw.monitor.user.control;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.user.service.MWVersionService;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "版本接口",tags = "版本接口")
public class MWVersionController extends BaseApiService {
    @Resource
    private MWVersionService mwVersionService;

    @ApiOperation(value="mw版本查询")
    @PostMapping("/mwVersion/browse")
    @ResponseBody
    public ResponseBase mwVersionBrowse(){
        try {
            Reply reply=mwVersionService.selectVersion();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw e;
        }
    }
}