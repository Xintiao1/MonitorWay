package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.ipaddressmanage.param.Check;
import cn.mw.monitor.ipaddressmanage.param.QueryIpAddressDistributtionParam;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManagePowerService;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManageService;
import cn.mw.monitor.ipaddressmanage.service.MwIpv6ManageService;
import cn.mw.monitor.labelManage.service.MwLabelManageService;
import cn.mw.monitor.weixin.util.SendUnifiedInterFace;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "IP地址加强第二页接口", tags = "IP地址加强第二页接口")
@ConditionalOnProperty(prefix = "mwModule", name = "ipPrower", havingValue = "true")
public class MWIpAddressPowerTwoController extends BaseApiService {



    @Autowired
    private MwIpAddressManagePowerService mwIpAddressManagePowerService;

    @Autowired
    private MwLabelManageService mwLabelMangeService;

    @Autowired
    private MwIpAddressManageService mwIpAddressManageService;

    @Autowired
    private MwIpv6ManageService mwIpv6ManageService;


    @Autowired
    private SendUnifiedInterFace sendUnifiedInterFace;


    @PostMapping("/MWIpAddressPowerTwo/brow")
    @ResponseBody
    @ApiOperation(value = "分配新增可用地址查询", tags = "分配查询")
    public ResponseBase<Check> brow(@RequestBody QueryIpAddressDistributtionParam param){
        Reply reply = null;
        try {
            if (param.getId().size()==0){

            }
            else {
                reply = mwIpAddressManagePowerService.selcectCheck(param);
            }


        } catch (Throwable e) {
            //e.printStackTrace();
            log.error(e.getMessage());
            return setResultFail("IP地址报错",null);
        }
        return setResultSuccess(reply);
    }

}
