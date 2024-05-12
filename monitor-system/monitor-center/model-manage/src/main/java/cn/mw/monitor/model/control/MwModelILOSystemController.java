package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.ilosystem.ILOInstanceParam;
import cn.mw.monitor.model.param.rancher.RancherInstanceParam;
import cn.mw.monitor.model.service.MwModelILOSystemService;
import cn.mwpaas.common.constant.PaasConstant;
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

/**
 * 刀片服务器
 *
 * @author qzg
 * @date 2023/04/18
 */
@RequestMapping("/mwapi/modelILO")
@Controller
@Slf4j
@Api(value = "模型ILO服务器接口", tags = "模型ILO服务器接口")
public class MwModelILOSystemController extends BaseApiService {

    @Autowired
    private MwModelILOSystemService mwModelILOSystemService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/login/browse")
    @ResponseBody
    @ApiOperation(value = "HP刀片服务器登录，获取数据")
    public ResponseBase loginClientGetData(@RequestBody ILOInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelILOSystemService.getAllILODataInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("loginClientGetData{}", e);
            return setResultFail("获取数据失败", "");
        }
        return setResultSuccess(reply);
    }


}
