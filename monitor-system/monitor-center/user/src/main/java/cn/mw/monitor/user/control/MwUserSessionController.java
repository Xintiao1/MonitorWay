package cn.mw.monitor.user.control;


import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.param.user.ExportUserOnlineParam;
import cn.mw.monitor.user.service.MwUserSessionService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Random;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author swy
 * @since 2023-08-08
 */
@RequestMapping("/mwapi/user/session")
@RestController
@Slf4j
@Api(value = "用户管理接口",tags = "用户管理接口")
public class MwUserSessionController extends BaseApiService {

    @Autowired
    private MwUserSessionService userSessionService;

    @PostMapping("/exportUserOnline")
    @ApiOperation("导出用户在线时长")
    public void exportUserOnline(@RequestBody ExportUserOnlineParam param, HttpServletResponse response){
        try {
            userSessionService.exportUserOnline(param,response);
        }catch (Exception e){
            log.error("excelTemplateExport{}", e);
        }
    }

    @PostMapping("/browse")
    @ApiOperation("用户在线时长分页查询")
    public ResponseBase queryPage(@RequestBody ExportUserOnlineParam param,
                                                        HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try {
            reply = userSessionService.queryPage(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Exception e){
            log.error("queryPage{}",e);
            return setResultFail(e.getMessage(),null);
        }
        return setResultSuccess(reply);
    }

}
