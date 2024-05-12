package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.user.dto.SettingDTO;
import cn.mw.monitor.user.service.MWUserService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@RequestMapping("/mwapi")
@Slf4j
@Controller
@Api(value = "系统配置", tags = "")
public class MWSystemConfigController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWSystemConfigController.class.getName());
    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    @Autowired
    private MWUserService mwUserService;
    @PostMapping("/systemSetting/performs")
    @ResponseBody
    @ApiOperation(value = "系统设置存储")
    public ResponseBase updateLogoUrl(@RequestBody SettingDTO settingDTO){
        Reply reply=null;
        try {
            settingDTO.setHttpHeader(filePath);
            reply=mwUserService.insertSettings(settingDTO);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(),settingDTO);
        }

    }


}
