package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.ipaddressmanage.param.*;
import cn.mw.monitor.ipaddressmanage.paramv6.AddUpdateIpv6ManageParam;
import cn.mw.monitor.ipaddressmanage.paramv6.QueryIpv6ManageParam;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManagePowerService;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManageService;
import cn.mw.monitor.ipaddressmanage.service.MwIpv6ManageService;
import cn.mw.monitor.labelManage.service.MwLabelManageService;
import cn.mw.monitor.server.param.MwOpenIPaddress;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.weixin.util.SendUnifiedInterFace;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.UUIDUtils;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "IP地址加强版", tags = "IP地址加强版")
@ConditionalOnProperty(prefix = "mwModule", name = "ipPrower", havingValue = "true")
public class MWIpAddressPowerController extends BaseApiService {

    @Value("${file.url}")
    private String imgPath;
    @Autowired
    private MwIpAddressManagePowerService mwIpAddressManagePowerService;

    @Autowired
    private MwLabelManageService mwLabelMangeService;

    @Autowired
    private MwIpAddressManageService mwIpAddressManageService;

    @Autowired
    private MwIpv6ManageService mwIpv6ManageService;

    @Autowired
    private MWMessageService mwMessageService;

    @Autowired
    private SendUnifiedInterFace sendUnifiedInterFace;

    @PostMapping("/MWIpAddressPower/brow")
    @ResponseBody
    @ApiOperation(value = "分配查询", tags = "分配查询")
    public ResponseBase<List<ResponIpDistributtionParam>> brow(@RequestBody QueryIpAddressDistributtionParam param) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.selectListbrow(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("brow", e);
            return setResultSuccess(reply);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/MWIpAddressPower/check/brow")
    @ResponseBody
    @ApiOperation(value = "下级选项", tags = "下级选项")
    public ResponseBase brow(@RequestBody IsInput param) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.checkBrow(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("brow", e);
            return setResultSuccess(reply);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/MWIpAddressPower/create")
    @ResponseBody
    @ApiOperation(value = "分配/修改地址", tags = "分配/修改地址")
    public ResponseBase create(@RequestBody RequestIpAddressDistributtionParam param, HttpServletRequest request) {
        Reply reply = null;
        try {
            if (!param.isSubmitStatus()) {
                reply = mwIpAddressManagePowerService.createDistributtion(param);
            } else {
                reply = mwIpAddressManagePowerService.updateDistributtion(param);
            }

        } catch (Throwable e) {
            log.error("create", e);
            reply = Reply.fail("提交数据非正常！请检查数据提交");
            return setResultSuccess(reply);
        }
        return setResultSuccess(reply);
    }



 /*   @PostMapping("/MWIpAddressPower/editor")
    @ResponseBody
    @ApiOperation(value = "回收查询", tags = "回收查询")
    public ResponseBase<List<RequestIpAddressDistributtionParam>> editor(@RequestBody QueryIpAddressDistributtionParam param){
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
        } catch (Throwable e) {
            log.error("editor" ,e);
            return setResultFail(e.toString(), e);
        }
        return setResultSuccess(reply);
    }*/

    /*@PostMapping("/MWIpAddressPower/DistributtionSenior/browse")
    @ResponseBody
    @ApiOperation(value = "查看基础属性", tags = "查看基础属性")
    public ResponseBase<RequestIpAddressDistributtionSeniorParam> distributtionSeniorParamBrowse(@RequestBody QueryIpAddressDistributtionParam param){
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
        } catch (Throwable e) {
            log.error("distributtionSeniorParamBrowse" ,e);
            return setResultFail(e.toString(), e);
        }
        return setResultSuccess(reply);
    }*/




   /* @PostMapping("/MWIpAddressPower/clean")
    @ResponseBody
    @ApiOperation(value = "回收接口", tags = "回收接口")
    public ResponseBase clean(@RequestBody List<QueryIpAddressDistributtionParam> param){
        Reply reply = null;
        try {
//            reply = mwIpAddressManagePowerService.cleanIP(param);

            reply = mwIpAddressManagePowerService.cleanSignleIP(param);
        } catch (Throwable e) {
            log.error("clean" ,e);
            return setResultFail(e.toString(), e);
        }
        return setResultSuccess(reply);
    }*/

    @PostMapping("/MWIpAddressPower/historyGroup/browse")
    @ResponseBody
    @ApiOperation(value = "历史分组查询", tags = "历史分组查询")
    public ResponseBase<List<CleanParam>> browse(@RequestBody QueryIpAddressDistributtionParam param) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.historyGroup(param);
        } catch (Throwable e) {
            log.error("historyGroup", e);
            return setResultFail("ip历史分组报错", null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/MWIpAddressPowerOperHistory/brow")
    @ResponseBody
    @ApiOperation(value = "查看分配历史", tags = "查细节历史")
    public ResponseBase<PageInfo<ResponseIpAddressOperHistoryParam>> MWIpAddressPowerHistorybrow(@RequestBody QueryIpAddressDistributtionParam param) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.MWIpAddressPowerHistorybrow(param);
        } catch (Throwable e) {
            log.error("MWIpAddressPowerHistorybrow", e);
            return setResultFail("ip历史分组报错", null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/MWIpAddressPowerOperHistory/createIpv6")
    @ResponseBody
    @ApiOperation(value = "生成IPv6", tags = "生成IPv6")
    public ResponseBase<String> createIpv6(@RequestBody QueryIpAddressDistributtionParam param) {
        Reply reply = null;
        Map<String, Object> map = new HashMap<>();
        if (param.getSignId() == null) {
            param.setSignId(1);
        }
        try {
            if (param.getIp() == null || param.getIp().trim().equals("")) {
                reply = Reply.fail("IPV6生成格式错误/当前分组没有此地址段");
                return setResultSuccess(reply);
            }
            String[] test = param.getIp().split("\\.");
            String ipv6 = "fc00::" + test[2];
            for (String s : test) {
                ipv6 = ipv6 + ":" + s;
            }
            boolean ip = mwIpAddressManagePowerService.createIpv6(ipv6, param.getKeyValue(), param.getSignId());
            Integer id = mwIpAddressManagePowerService.selectByIPaddress(ipv6, param.getSignId());
            map.put("id", id);
            map.put("ip", ipv6);
            map.put("idType", true);
            if (ip) {
                reply = Reply.ok(map);
            } else {
                reply = Reply.fail("IPV6生成格式错误/当前分组没有此地址段");
            }
        } catch (Throwable e) {
            log.error("createIpv6", e);
            return setResultFail("ip新建报错", null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/MWIpAddressPower/browlabel")
    @ResponseBody
    @ApiOperation(value = "查看属性", tags = "查看属性")
    public ResponseBase<List<Label>> browlabel(@RequestBody QueryIpAddressDistributtionParam param) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.selectLabel(param);

        } catch (Throwable e) {
            log.error("browlabel", e);
            return setResultFail("查看属性报错", null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/MWIpAddressPower/createLabel")
    @ResponseBody
    @ApiOperation(value = "创造属性选项", tags = "创造属性选项")
    public ResponseBase<Integer> createLabel(@RequestBody LabelCheck param) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.createLabel(param);
        } catch (Throwable e) {
            log.error("createLabel", e);
            return setResultFail("创造属性选项报错", null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/MWIpAddressPower/deleteLabel")
    @ResponseBody
    @ApiOperation(value = "删除属性选项", tags = "删除属性选项")
    public ResponseBase<Integer> deleteLabel(@RequestBody LabelCheck param) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.deleteLabel(param);
        } catch (Throwable e) {
            log.error("deleteLabel", e);
            return setResultFail("删除属性选项报错", null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/MWIpAddressPower/browDrop")
    @ResponseBody
    @ApiOperation(value = "下拉属性值", tags = "下拉属性值")
    public ResponseBase<List<LabelCheck>> browDrop(@RequestBody Map<String, String> name) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.browDrop(name.get("name"));
        } catch (Throwable e) {
            log.error("browDrop", e);
            return setResultFail("下拉属性值选项报错", null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/MWIpAddressPower/saveLabel")
    @ResponseBody
    @ApiOperation(value = "保存属性", tags = "保存属性")
    public ResponseBase<List<Label>> saveLabel(@RequestBody List<Label> name) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.saveLabel(name);
        } catch (Throwable e) {
            log.error("saveLabel", e);
            return setResultFail("保存属性值选项报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressPowerManageList/browse")
    @ResponseBody
    @ApiOperation(value = "ipv4列表查询")
    public ResponseBase querySonList(@RequestBody QueryIpAddressPowerManageListParam qParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {

            reply = mwIpAddressManagePowerService.selectSnoListTest(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("querySonList", e);
            return setResultFail("ipv4列表查询报错", null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6AddressPowerManageList/browse")
    @ResponseBody
    @ApiOperation(value = "IPv6地址管理查询")
    public ResponseBase ipv6QueryList(@RequestBody QueryIpAddressPowerManageListParam qParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManagePowerService.selectSonList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("ipv6QueryList", e);
            return setResultFail("IPv6地址管理查询报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPowerManage/browse")
    @ResponseBody
    @ApiOperation(value = "IP地址树状图下拉")
    public ResponseBase queryList(@RequestBody QueryIpAddressManageParam qParam,
                                  HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            if (qParam.getSignId() == null || qParam.getSignId() == 0) {
                qParam.setSignId(1);
            }
            reply = mwIpAddressManagePowerService.selectTree(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("queryList", e);
            return setResultFail("IP地址树状图下拉报错", null);
        }
        return setResultSuccess(reply);
    }


    @ApiOperation(value = "新增IP地址管理")
    @PostMapping("/MWIpAddressPowerManage/create")
    @ResponseBody
    public ResponseBase add(@RequestBody @Valid AddUpdateIpAddressManageParam param,
                            HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {

            if (param.getIpAddresses() == null || param.getIpAddresses().equals("")) {
                reply = mwIpAddressManageService.insert(param);
            } else {
                if (param.getSignId() == null || param.getSignId() == 0) {
                    param.setSignId(1);
                }
                String[] ipAddresses = param.getIpAddresses().split("/");
                String ip = ipAddresses[0];
                if (sendUnifiedInterFace.isIPv4Address(ip)) {
                    reply = mwIpAddressManageService.insert(param);
                } else {
                    AddUpdateIpv6ManageParam ipv6ManageParam = new AddUpdateIpv6ManageParam();
                    BeanUtils.copyProperties(param, ipv6ManageParam);
                    reply = mwIpv6ManageService.insert(ipv6ManageParam);
                }
            }

            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("add", e);
            return setResultFail("新增IP地址报错", null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPowerManage/editorBrowse")
    @ResponseBody
    @ApiOperation(value = "地址管理编辑查询")
    public ResponseBase ipv6QueryOne(@RequestBody AddUpdateIpAddressManageParam qParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {

            if (qParam.getIPv4() == 1 || qParam.getType().equals("grouping")) {

                QueryIpAddressManageParam ipv6ManageParam = new QueryIpAddressManageParam();
                BeanUtils.copyProperties(qParam, ipv6ManageParam);
                reply = mwIpAddressManageService.selectList1(ipv6ManageParam);
            } else {
                QueryIpv6ManageParam ipv6ManageParam = new QueryIpv6ManageParam();
                BeanUtils.copyProperties(qParam, ipv6ManageParam);
                reply = mwIpv6ManageService.editorSelect(ipv6ManageParam);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("ipv6QueryOne", e);
            return setResultFail("地址管理编辑查询报错", null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPowerManage/getIpAddressOrg")
    @ResponseBody
    @ApiOperation(value = "获取IP地址的用户机构组")
    public ResponseBase getIpAddressOrg(@RequestBody AddUpdateIpAddressManageParam qParam,
                                        HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.getIpAddressOrg(qParam);
        } catch (Throwable e) {
            log.error("ipv6QueryOne", e);
            return setResultFail("获取IP地址的用户机构组查询报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPowerManage/editor")
    @ResponseBody
    @ApiOperation(value = "IP地址管理修改")
    public ResponseBase update(@RequestBody @Valid AddUpdateIpAddressManageParam addUpdateIpAddressManageParam) {
        Reply reply;
        try {
            String[] ipAddresses = addUpdateIpAddressManageParam.getIpAddresses().split("/");
            String ip = ipAddresses[0];
            if (sendUnifiedInterFace.isIPv4Address(ip)) {
                reply = mwIpAddressManageService.update(addUpdateIpAddressManageParam);
            } else if (addUpdateIpAddressManageParam.getType().equals("grouping")) {
                reply = mwIpAddressManageService.update(addUpdateIpAddressManageParam);
            } else {
                AddUpdateIpv6ManageParam ipv6ManageParam = new AddUpdateIpv6ManageParam();
                BeanUtils.copyProperties(addUpdateIpAddressManageParam, ipv6ManageParam);
                reply = mwIpv6ManageService.update(ipv6ManageParam);
            }

            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("update", e);
            return setResultFail("IP地址管理修改报错", null);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "增加标签查询列")
    @PostMapping("/MWIpAddressPowerManage/customcol/browse")
    @ResponseBody
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", paramType = "query", required = true),
            @ApiImplicitParam(name = "pageId", value = "页面id", paramType = "query", required = true)
    })
    public ResponseBase browseCustomcol(@RequestBody QueryCustomPageParam qParam,
                                        HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManagePowerService.selectById(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("browseCustomcol", e);
            return setResultFail("增加标签查询列报错", null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPowerManage/getHisList")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页历史查询")
    public ResponseBase getHisList(@RequestBody AddUpdateIpAddressManageListParam parm,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManagePowerService.getHisList(parm);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("getHisList", e);
            return setResultFail("IP地址管理table页历史查询报错", null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 开放接口
     */
    @PostMapping("/open/getIpAddressListDes/browse")
    @ResponseBody
    @ApiOperation(value = "获取IP对应分配记录历史")
    public ResponseBase<ResIpStatusDesc> getIpAddressListDes(@RequestBody MwOpenIPaddress param) {
        Reply reply;
        try {
//            String privateKey = RSAUtils.RSA_PRIVATE_KEY;
//            String ipAddresses = RSAUtils.decryptData(param.getIpAddress(), privateKey);
//
//
//            if(!NumberUtils.isNumber(ipAddresses)){
//                return setResultFail("数据不符合规则,解密数据失败", "");
//            }
            List<String> ipAddressList = Arrays.asList(param.getIpAddress().split(","));
            // 验证内容正确性
            reply = mwIpAddressManagePowerService.getIpAddressListDes(ipAddressList);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            return setResultFail("获取IP对应分配记录历史报错", null);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/fuzzSeachAllFiled/browse")
    @ResponseBody
    @ApiOperation(value = "获取IP地址查询下拉选项")
    public ResponseBase fuzzSeachAllFiledData(@RequestBody AddUpdateIpAddressManageListParam parm) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwIpAddressManagePowerService.fuzzSeachAllFiledData(parm);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("fuzzSeachAllFiledData", e);
            return setResultFail("获取IP地址查询下拉选项报错", null);
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/getAllIpManage/browse")
    @ResponseBody
    @ApiOperation(value = "返回所有下拉结构")
    public ResponseBase getAllIpManage(@RequestBody IpAllRequestBody ipAllRequestBody) {
        Reply reply;
        try {
            if (ipAllRequestBody.getSignId() == null || ipAllRequestBody.getSignId() == 0) {
                ipAllRequestBody.setId(1);
            } else {
                ipAllRequestBody.setId(ipAllRequestBody.getSignId());
            }
            // 验证内容正确性
            reply = mwIpAddressManagePowerService.getAllIpManage(ipAllRequestBody);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("getAllIpManage", e);
            return setResultFail("返回所有下拉结构报错", null);
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/ipsign")
    @ResponseBody
    @ApiOperation(value = "获取IP地域")
    public ResponseBase getIpSign() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwIpAddressManagePowerService.getIpSign();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("getAllIpManage", e);
            return setResultFail("获取IP地域报错", null);
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/createIpsign")
    @ResponseBody
    @ApiOperation(value = "创建IP地域")
    public ResponseBase createIpsign(@RequestBody IpAllRequestBody ipAllRequestBody) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwIpAddressManagePowerService.createIpsign(ipAllRequestBody);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("getAllIpManage", e);
            return setResultFail("创建IP地域报错", null);
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/deleteIpsign")
    @ResponseBody
    @ApiOperation(value = "创建IP地域")
    public ResponseBase deleteIpsign(@RequestBody IpAllRequestBody ipAllRequestBody) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwIpAddressManagePowerService.deleteIpsign(ipAllRequestBody);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("getAllIpManage", e);
            return setResultFail("创建IP地域报错", null);
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/getAllIpGroupManage/browse")
    @ResponseBody
    @ApiOperation(value = "返回所有文件夹类型下拉结构(返回input里的Id(可选）)")
    public ResponseBase getAllIpGroupManage(@RequestBody IsInput isInput) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwIpAddressManagePowerService.getAllIpGroupManage(isInput);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("getAllIpGroupManage", e);
            return setResultFail("返回所有文件夹类型下拉结构(返回input里的Id(可选）)报错", null);
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/isInput/profrom")
    @ResponseBody
    @ApiOperation(value = "判断位置是否能插如")
    public ResponseBase<UserDTO> isInput(@RequestBody IsInput isInput) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwIpAddressManagePowerService.isInput(isInput);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("isInput", e);
            return setResultFail("判断位置是否能插如报错", null);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/changeIndex/browse")
    @ResponseBody
    @ApiOperation(value = "修改代码位置")
    public ResponseBase<UserDTO> changeIndex(@RequestBody IsInput isInput) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwIpAddressManagePowerService.changeIndex(isInput);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("changeIndex", e);
            return setResultFail("修改代码位置报错", null);
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/getLeader/browse")
    @ResponseBody
    @ApiOperation(value = "获取负责人的机构名称")
    public ResponseBase<UserDTO> getLeader() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwIpAddressManagePowerService.getLeader();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("getLeader", e);
            return setResultFail("获取负责人的机构名称报错", null);
        }

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/GroupByParent/browse")
    @ResponseBody
    @ApiOperation(value = "查看其它分组的地址")
    public ResponseBase<UserDTO> getOtherGroupByParent(@RequestBody IsInput isInput) {
        Reply reply = null;
        try {
            if (isInput.getSignId() == null) {
                isInput.setSignId(1);
            }
            QueryIpAddressManageParam qParam = new QueryIpAddressManageParam();
            qParam.setParentId(isInput.getParentId());
            qParam.setPageNumber(1);
            qParam.setPageSize(256);
            qParam.setSignId(isInput.getSignId());
            reply = mwIpAddressManagePowerService.selectList(qParam, isInput.getLevel());

            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("getOtherGroupByParent", e);
            return setResultFail("查看其它分组的地址报错", null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/seniorParam/browse")
    @ResponseBody
    @ApiOperation(value = "新回收总查询", tags = "新回收总查询")
    public ResponseBase<RequestIpAddressDistributtionNewParam> seniorParamBrowse(@RequestBody IsInput param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.selectListSeniorParam(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("新回收总查询报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/seniorParam/seeInfo")
    @ResponseBody
    @ApiOperation(value = "新回收总查询", tags = "新回收总查询")
    public ResponseBase<RequestIpAddressDistributtionNewParam> seeInfo(@RequestBody IsInput param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.selectListSeniorseeInfo(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("新回收总查询报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/comprehensive/list")
    @ResponseBody
    @ApiOperation(value = "IP概览列表接口", tags = "IP概览列表接口")
    public ResponseBase comprehensiveList(@RequestBody Map<String, Object> param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.selectListComprehensive(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("IP概览列表报错", null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/countNum/list")
    @ResponseBody
    @ApiOperation(value = "IP概览列表上层数据", tags = "IP概览列表上层数据")
    public ResponseBase<IPCountNum> countNum() {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.countNumList();
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("IP概览列表上层数据报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/comprehensive/sreachLabel")
    @ResponseBody
    @ApiOperation(value = "IP概览标签列表接口", tags = "IP概览标签列表接口")
    public ResponseBase sreachLabel(@RequestBody QueryCustomPageParam qParam) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.sreachLabel(qParam);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("IP概览列表报错", null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/seniorParam/list")
    @ResponseBody
    @ApiOperation(value = "新回收总查询一", tags = "新回收总查询一")
    public ResponseBase<RequestIpAddressDistributtionNewParamList> list(@RequestBody IsInput param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.selectIPDristi(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("IP概览列表报错", null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/changeOrRe/browse")
    @ResponseBody
    @ApiOperation(value = "新回收接口", tags = "新回收接口")
    public ResponseBase<RequestIpAddressDistributtionNewParam> changeOrRe(@RequestBody RequestIpAddressReciveParam param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);

            reply = mwIpAddressManagePowerService.changeOrRe(param);
        } catch (Throwable e) {
            log.error("changeOrRe", e);
            return setResultFail("IP概览列表报错", null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/MWIpAddressPower/new/create")
    @ResponseBody
    @ApiOperation(value = "新分配/修改地址", tags = "新分配/修改地址")
    public ResponseBase newCreate(@RequestBody RequestIpAddressDistributtionNewParam param) {
        Reply reply = null;

        try {
            if (!param.isSubmitStatus()) {
                String operNum = UUIDUtils.getUUID();
                reply = mwIpAddressManagePowerService.createNewDistributtion(param, operNum);
            } else {
//                reply = mwIpAddressManagePowerService.updateDistributtion(param);
            }

        } catch (Throwable e) {
            log.error("newCreate", e);
            reply = Reply.fail("提交数据非正常！请检查数据提交");
            return setResultSuccess(reply);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/MWIpAddressPower/change/history")
    @ResponseBody
    @ApiOperation(value = "临时转化操作关系", tags = "临时转化操作关系")
    public ResponseBase history() {
        Reply reply = null;
        reply = mwIpAddressManagePowerService.history();

        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/changeParam/browse")
    @ResponseBody
    @ApiOperation(value = "新变更接口", tags = "新变更接口")
    public ResponseBase<RequestIpAddressDistributtionNewParam> changeParam(@RequestBody List<RequestIpAddressDistributtionNewParam> params) {
        Reply reply = null;
        try {

            mwIpAddressManagePowerService.changeRes(params);
        } catch (Throwable e) {
            log.error("changeParam", e);
            return setResultFail("IP概览列表报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/changeParam/relation")
    @ResponseBody
    @ApiOperation(value = "IP地址关联", tags = "IP地址关联")
    public ResponseBase<RequestIpAddressDistributtionNewParam> relation(@RequestBody List<RequestIpAddressDistributtionNewParam> params) {
        Reply reply = null;
        try {
            for (RequestIpAddressDistributtionNewParam requestIpAddressDistributtionNewParam : params) {
                List<ResponIpDistributtionNewParentParam> responIpDistributtionNewParams = requestIpAddressDistributtionNewParam.getResponIpDistributtionParams();
                RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam = requestIpAddressDistributtionNewParam.getRequestIpAddressDistributtionSeniorParam();
                for (ResponIpDistributtionNewParentParam e : responIpDistributtionNewParams) {
                    List<ResponIpDistributtionNewParam> responIpDistributtionNewParamList = e.getTreeData().get(0).getChildren();
                    for (ResponIpDistributtionNewParam r : responIpDistributtionNewParamList) {
                        mwIpAddressManagePowerService.relation(requestIpAddressDistributtionSeniorParam, r);

                    }

                }
            }

        } catch (Throwable e) {
            log.error("changeParam", e);
            return setResultFail("变动主IP关系", "变动主IP关系");
        }
        return setResultSuccess(reply);
    }

    /**
     * d
     *
     * @param file
     * @param response
     */
    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressPowerManageList/ipImport")
    @ResponseBody
    @ApiOperation(value = "地址导入")
    public ResponseBase ipImport(@RequestBody MultipartFile file, @RequestParam Integer signId, HttpServletResponse response) {
        Reply reply;
        try {
          /*  String fileName = "12345.xlsx";
            XSSFWorkbook workbook = mwIpAddressManagePowerService.excelImport(file);
            FileOutputStream out = null;
            out = new FileOutputStream(imgPath+"/12345.xlsx");
            workbook.write(out);*/
            if (signId == null) {
                signId = 1;
            }
            List<String> workbook = mwIpAddressManagePowerService.ipImport(file, signId);
            if (workbook.size() > 0) {
                return setResultFail("ipv6地址未在当前地址段或者已存在：" + workbook.toString(), "ipv6地址未在当前地址段或者已存在：" + workbook.toString());
            }
        } catch (Exception e) {
            log.error("excelImport", e);
            return setResultFail("导入文件异常", null);
        }
        return setResultSuccess("导入成功");
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressPowerManageList/inversion")
    @ResponseBody
    @ApiOperation(value = "关系倒置")
    public ResponseBase ipImport(@RequestParam IsInput id, HttpServletResponse response) {
        Reply reply = null;
        try {
            reply = mwIpAddressManagePowerService.inversion(id);

        } catch (Exception e) {
            log.error("excelImport", e);
            return setResultFail("导入文件异常", null);
        }
        return setResultSuccess("导入成功");
    }


    /**
     * d
     *
     * @param file
     * @param response
     */
    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressPowerManageList/excelImport")
    @ResponseBody
    @ApiOperation(value = "导入分配信息")
    public ResponseBase excelImport(@RequestBody MultipartFile file, HttpServletResponse response) {
        Reply reply;
        HashMap<String, Object> data = null;
        InputStream inputStream = null;
        ServletOutputStream outputStream = null;

        try {
            String fileName = "12345.xlsx";
            XSSFWorkbook workbook = mwIpAddressManagePowerService.excelImport(file);
            FileOutputStream out = null;
            out = new FileOutputStream(imgPath + "/12345.xlsx");
            workbook.write(out);

        } catch (Exception e) {
            log.error("excelImport", e);
            return setResultSuccess(Reply.fail("导入失败"));
        }
        return setResultSuccess(Reply.ok());
    }


    /**
     * d
     *
     * @param file
     * @param response
     */
    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressPowerManageList/excelImportDesc")
    @ResponseBody
    @ApiOperation(value = "导入ip描述信息")
    public ResponseBase excelImportDesc(@RequestBody MultipartFile file, @RequestParam Integer signId, HttpServletResponse response) {
        Reply reply;
        HashMap<String, Object> data = null;
        InputStream inputStream = null;
        ServletOutputStream outputStream = null;

        try {
            reply = mwIpAddressManagePowerService.excelImportDesc(file, signId);
          /*  String fileName = "12345.xlsx";
            FileOutputStream out = null;
            out = new FileOutputStream(imgPath+"/678910.xlsx");
            workbook.write(out);*/

        } catch (Exception e) {
            log.error("excelImport", e);
            return setResultSuccess(Reply.fail("导入失败"));
        }
        return setResultSuccess(Reply.ok());
    }

    /**
     * d
     *
     * @param file
     * @param response
     */
    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressPowerManageList/excelAssets")
    @ResponseBody
    @ApiOperation(value = "资产暂时接口")
    public ResponseBase execlAssets(@RequestBody MultipartFile file, @RequestParam Integer signId, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwIpAddressManagePowerService.execlImport(file, signId);
        } catch (Exception e) {
            log.error("excelImport", e);
            return setResultFail("导入文件异常", null);
        }
        return setResultSuccess("导入成功");
    }

    @PostMapping("/MWIpAddressPower/change/list")
    @ResponseBody
    @ApiOperation(value = "所有分配页面", tags = "所有分配页面")
    public ResponseBase<RequestIpAddressDistributtionNewParam> changeList(@RequestBody IsInput isInput) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);

            reply = mwIpAddressManagePowerService.changeList(isInput);
        } catch (Throwable e) {
            log.error("editor", e);
            return setResultFail("所有分配页面错误", null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/MWIpAddressPower/message/editor")
    @ResponseBody
    @ApiOperation(value = "消息已读", tags = "消息已读")
    public ResponseBase<RequestIpAddressDistributtionNewParam> editor(@RequestParam Integer param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);

            reply = mwMessageService.chageEditor(param);
        } catch (Throwable e) {
            log.error("editor", e);
            return setResultFail("消息已读错误", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/comprehensive/countCreate")
    @ResponseBody
    @ApiOperation(value = "IP概览ip新增删除数据接口", tags = "IP概览ip新增删除数据接口")
    public ResponseBase countCreate(@RequestBody seachLabelList param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.countCreate(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("IP概览列表上层数据报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/comprehensive/countHaving")
    @ResponseBody
    @ApiOperation(value = "IP概览ip分配回收数据", tags = "IP概览ip分配回收数据")
    public ResponseBase countHaving(@RequestBody seachLabelList param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.countHaving(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("IP概览ip分配回收数据报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/comprehensive/getTree")
    @ResponseBody
    @ApiOperation(value = "IP概览ip分配回收再分配查询", tags = "IP概览ip分配回收再分配查询")
    public ResponseBase getTree(@RequestBody seachLabelList param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.getTree(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("IP概览ip分配回收再分配查询报错", null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/MWIpAddressPower/browagain")
    @ResponseBody
    @ApiOperation(value = "IP概览分配查询", tags = "IP概览分配查询")
    public ResponseBase<List<ResponIpDistributtionParam>> browagain(@RequestBody seachLabelList param) {
        Reply reply = null;
        try {

            reply = mwIpAddressManagePowerService.browagain(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("ip地址报错", null);
            }
        } catch (Throwable e) {
            log.error("brow", e);
            return setResultSuccess(reply);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/comprehensive/cancel")
    @ResponseBody
    @ApiOperation(value = "IP概览ip分配回收", tags = "IP概览ip分配回收")
    public ResponseBase cancel(@RequestBody List<DeleteIpList> param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.cancel(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("IP地址报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/comprehensive/change")
    @ResponseBody
    @ApiOperation(value = "IP概览在分配", tags = "IP概览在分配")
    public ResponseBase change(@RequestBody List<seachLabelList> param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.change(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("IP地址报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/changeParamList/change")
    @ResponseBody
    @ApiOperation(value = "批量新变更接口", tags = "批量新变更接口")
    public ResponseBase<RequestIpAddressDistributtionNewParam> changeParamList(@RequestBody List<RequestIpAddressDistributtionNewParam> param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            String operNum = UUIDUtils.getUUID();
            for (RequestIpAddressDistributtionNewParam p : param) {
                reply = mwIpAddressManagePowerService.changeParam(p, operNum);
            }
        } catch (Throwable e) {
            log.error("changeParam", e);
            return setResultFail("IP地址报错", null);
        }
        return setResultSuccess(reply);
    }

  /*  @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/comprehensive/change")
    @ResponseBody
    @ApiOperation(value = "IP导出, tags = "IP导出")
    public ResponseBase change(@RequestBody List<seachLabelList> param){
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.change(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse" ,e);
            return setResultFail(e.toString(), e);
        }
        return setResultSuccess(reply);
    }*/


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/comprehensive/getInfo")
    @ResponseBody
    @ApiOperation(value = "IP概览获取详情", tags = "IP概览获取详情")
    public ResponseBase getInfo(@RequestBody Map<String, Object> param) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            reply = mwIpAddressManagePowerService.getInfo(param);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
            return setResultFail("IP地址报错", null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/MWIpAddressPower/comprehensive/execlInfo")
    @ResponseBody
    @ApiOperation(value = "导出接口", tags = "IP概览获取详情")
    public void execlInfo(@RequestBody Map<String, String> param, HttpServletResponse response) {
        Reply reply = null;
        try {
//         reply = mwIpAddressManagePowerService.selectListDistributtionbrow(param);
//            reply = mwIpAddressManagePowerService.selectListDistributtionSeniorParamBrowse(param);
            mwIpAddressManagePowerService.execlInfo(param, response);
        } catch (Throwable e) {
            log.error("seniorParamBrowse", e);
        }
    }

}

