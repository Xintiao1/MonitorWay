package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.service.assets.param.AssetsSearchTermFuzzyParam;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.scanrule.api.param.scanrule.AddScanruleParam;
import cn.mw.monitor.scanrule.api.param.scanrule.DeleteScanruleParam;
import cn.mw.monitor.scanrule.api.param.scanrule.QueryScanruleParam;
import cn.mw.monitor.scanrule.dto.MwScanruleDTO;
import cn.mw.monitor.scanrule.model.IpRangeView;
import cn.mw.monitor.scanrule.model.IpsubnetsView;
import cn.mw.monitor.scanrule.model.MwScanRuleView;
import cn.mw.monitor.scanrule.model.RuleView;
import cn.mw.monitor.scanrule.service.MwScanruleService;
import cn.mw.monitor.snmp.utils.ResovlerUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/17
 */

@RequestMapping("/mwapi")
@Controller
@Slf4j
public class MWScanruleController extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("MWDBLogger");

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    MwScanruleService mwScanruleService;

    /**
     * 扫描规则新增
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/scanrule/create")
    @ResponseBody
    public ResponseBase addScanrule(@RequestBody AddScanruleParam auParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model){
        try{
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("资产发现")
                    .objName(auParam.getScanruleName()).operateDes("资产发现扫描规则新增").build();
            logger.info(JSON.toJSONString(builder));
            // 验证内容正确性
            Reply reply = mwScanruleService.insert(auParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            log.error("error:",e);
            return setResultFail("扫描规则新增失败", auParam);
        }

        return setResultSuccess(auParam);
    }

    /**
     * 扫描规则删除
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/scanrule/delete")
    @ResponseBody
    public ResponseBase deleteScanRule(@RequestBody DeleteScanruleParam dParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model){
        try{
            List<Integer> idList = dParam.getIdList();
            // 验证内容正确性
            Reply reply = mwScanruleService.delete(idList);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            log.error("error:",e);
            return setResultFail("扫描规则删除失败", dParam);
        }

        return setResultSuccess(dParam);
    }

    /**
     * 扫描规则查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/scanrule/browse")
    @ResponseBody
    public ResponseBase browseScanrule(@RequestBody QueryScanruleParam qParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model){
        try{
            // 验证内容正确性
            Reply reply = mwScanruleService.selectList(qParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            log.error("error:",e);
            return setResultFail(e.getMessage(), qParam);
        }

        return setResultSuccess(qParam);
    }

    /**
     * 根据id查询扫描规则
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/scanrule/getById/browse")
    @ResponseBody
    public ResponseBase getScanruleById(@RequestBody QueryScanruleParam qParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model){
        try{
            // 验证内容正确性
            Reply reply = mwScanruleService.selectById(qParam.getScanruleId());
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                log.error("error res:" + reply.getRes());
                return setResultFail(reply.getMsg(), reply.getData());
            }
            MwScanruleDTO msDto = (MwScanruleDTO)reply.getData();
            MwScanRuleView view = transform(msDto);
            return setResultSuccess(view);
        }catch (Throwable e){
            log.error("error:",e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    private MwScanRuleView transform(MwScanruleDTO src){
        MwScanRuleView dest = new MwScanRuleView();
        dest.setName(src.getScanruleName());
        dest.setRuleId(src.getScanruleId());
        dest.setMonitorServerId(src.getMonitorServerId());
        dest.setEngineId(src.getEngineId());
        if(null != src.getIpRangDTO() && src.getIpRangDTO().size() > 0){
            List<IpRangeView> ipRangeViews = new ArrayList<IpRangeView>();
            src.getIpRangDTO().forEach(value ->{
                IpRangeView ipRangeView = new IpRangeView();
                ipRangeView.setStartip(value.getIpRangStart());
                ipRangeView.setEndip(value.getIpRangEnd());
                ipRangeView.setIpv6checked(value.getIpType());
                ipRangeViews.add(ipRangeView);
            });
            dest.setIpRange(ipRangeViews);
        }

        if(null != src.getIpAddressesDTO() && src.getIpAddressesDTO().size() > 0) {
            List<IpsubnetsView> ipsubnetsViews = new ArrayList<IpsubnetsView>();
            src.getIpAddressesDTO().forEach(value -> {
                IpsubnetsView ipsubnetsView = new IpsubnetsView();
                ipsubnetsView.setSubnet(value.getIpAddresses());
                ipsubnetsView.setIpv6checked(value.getIpType());
                ipsubnetsViews.add(ipsubnetsView);
            });
            dest.setIpsubnets(ipsubnetsViews);
        }

        if(null != src.getIpAddressListDTO() && src.getIpAddressListDTO().size() > 0) {
            StringBuffer sb = new StringBuffer();
            src.getIpAddressListDTO().forEach(value ->{
                if(null != value.getIpAddress()){
                    sb.append(ResovlerUtil.IP_SEPERATOR).append(value.getIpAddress());
                }
            });
            if(sb.length() > ResovlerUtil.IP_SEPERATOR.length()){
                dest.setIplist(sb.toString().substring(ResovlerUtil.IP_SEPERATOR.length()));
                dest.setIpv6checked(src.getIpAddressListDTO().get(0).getIpType());
            }
        }

        List<RuleView> ruleViews = new ArrayList<RuleView>();
        if(null != src.getPortruleDTOs() && src.getPortruleDTOs().size() > 0) {
            src.getPortruleDTOs().forEach(value -> {
                RuleView ruleView = new RuleView();
                ruleView.setPort((value.getPort() != null) ? value.getPort().toString() : "");
                ruleView.setProtoType(RuleType.Port.getName());
                ruleViews.add(ruleView);
            });
        }

        if(null != src.getAgentruleDTOs() && src.getAgentruleDTOs().size() > 0) {
            src.getAgentruleDTOs().forEach(value -> {
                RuleView ruleView = new RuleView();
                ruleView.setPort(value.getPort().toString());
                ruleView.setProtoType(RuleType.ZabbixAgent.getName());
                ruleViews.add(ruleView);
            });
        }

        if(null != src.getRulesnmpv1DTOs() && src.getRulesnmpv1DTOs().size() > 0) {
            src.getRulesnmpv1DTOs().forEach(value -> {
                RuleView ruleView = new RuleView();
                ruleView.setPort(value.getPort().toString());
                ruleView.setProtoType(RuleType.SNMP);
                ruleView.setVersion(RuleType.SNMPv1v2.getName());
                ruleView.setCommunity(value.getCommunity());
                ruleViews.add(ruleView);
            });
        }

        if(null != src.getRulesnmpDTOs() && src.getRulesnmpDTOs().size() > 0) {
            src.getRulesnmpDTOs().forEach(value -> {
                RuleView ruleView = new RuleView();
                ruleView.setPort(value.getPort().toString());
                ruleView.setProtoType(RuleType.SNMP);
                ruleView.setSecurityName(value.getSecName());
                ruleView.setContextName(value.getContextName());
                ruleView.setVersion(RuleType.SNMPv3.getName());
                ruleView.setSecurityLevel(value.getSecLevel());
                ruleView.setAuthProtocol(value.getAuthAlg());
                ruleView.setAuthToken(value.getAuthValue());
                ruleView.setPrivProtocol(value.getPrivAlg());
                ruleView.setPrivToken(value.getPriValue());
                ruleViews.add(ruleView);
            });
        }

        if(null != src.getIcmpruleDTOList() && src.getIcmpruleDTOList().size() > 0) {
            src.getIcmpruleDTOList().forEach(value -> {
                RuleView ruleView = new RuleView();
                ruleView.setPort((value.getPort() != null) ? value.getPort().toString() : "");
                ruleView.setProtoType(RuleType.ICMP.getName());
                ruleViews.add(ruleView);
            });
        }
        dest.setScanrules(ruleViews);
        return dest;
    }

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/scanrule/fuzzSearchAllFiled/browse")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData(@RequestBody AssetsSearchTermFuzzyParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwScanruleService.fuzzSearchAllFiledData(param.getValue());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("模糊查询所有字段资数据失败", param);
        }

        return setResultSuccess(reply);
    }
}
