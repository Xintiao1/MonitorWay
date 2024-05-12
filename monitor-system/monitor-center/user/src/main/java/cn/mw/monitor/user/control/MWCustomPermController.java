package cn.mw.monitor.user.control;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.param.user.BindUserGroupParam;
import cn.mw.monitor.user.service.MWCustomPermService;
import cn.mw.monitor.user.service.MWGroupService;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * Created by zy.quaee on 2021/6/21 9:42.
 **/

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "自定义权限接口",tags = "自定义权限接口")
public class MWCustomPermController extends BaseApiService {

    @Autowired
    private MWCustomPermService mwCustomPermService;

    @ApiOperation(value = "自定义模块权限")
    @PostMapping("/custom/moduletoredis/perform")
    @ResponseBody
    public ResponseBase moduleToRedis() {
        try {
            Reply reply = mwCustomPermService.customModuleToRedis();
            return setResultSuccess(reply);
        } catch (Exception e) {
            return setResultFail(e.getMessage(), "");
        }
    }

    @ApiOperation(value = "自定义NotCheckUrl权限")
    @PostMapping("/custom/notchecktoredis/perform")
    @ResponseBody
    public ResponseBase notCheckToRedis() {
        try {
            Reply reply = mwCustomPermService.customNotCheckToRedis();
            return setResultSuccess(reply);
        } catch (Exception e) {
            return setResultFail(e.getMessage(), "");
        }
    }
}

