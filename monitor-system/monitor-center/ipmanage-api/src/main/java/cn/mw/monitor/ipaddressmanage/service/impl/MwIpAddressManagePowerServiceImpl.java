package cn.mw.monitor.ipaddressmanage.service.impl;

import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.dto.MwCustomPageDTO;
import cn.mw.monitor.customPage.model.MwPageselectTable;
import cn.mw.monitor.customPage.service.MwCustomcolService;
import cn.mw.monitor.dropDown.model.MwDropdownTable;
import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManageListTableDao;
import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManagePowerTableDao;
import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManageTableDao;
import cn.mw.monitor.ipaddressmanage.dao.MwIpv6ManageTableDao;
import cn.mw.monitor.ipaddressmanage.dto.GroupDTO;
import cn.mw.monitor.ipaddressmanage.dto.IpamOperHistoryDTO;
import cn.mw.monitor.ipaddressmanage.dto.IpamProcessHistoryDTO;
import cn.mw.monitor.ipaddressmanage.dto.OrgDTO;
import cn.mw.monitor.ipaddressmanage.param.*;
import cn.mw.monitor.ipaddressmanage.paramv6.Ipv6ManageTable1Param;
import cn.mw.monitor.ipaddressmanage.paramv6.Ipv6ManageTableParam;
import cn.mw.monitor.ipaddressmanage.paramv6.QueryIpv6ManageParam;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManagePowerService;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManageScanService;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManageService;
import cn.mw.monitor.ipaddressmanage.util.ConverParam;
import cn.mw.monitor.ipaddressmanage.util.IPv6Judge;
import cn.mw.monitor.ipaddressmanage.util.MapUtils;
import cn.mw.monitor.labelManage.service.MwLabelManageService;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.service.ipmanage.model.IpManageTree;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.dto.LoginInfo;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.listener.LoginContext;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mw.monitor.user.dao.MwUserOrgMapperDao;
import cn.mw.monitor.user.dto.MwUserDTO;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.mw.monitor.ipaddressmanage.ipenum.Constant.*;

/**
 * bkc
 */
@Service
@Slf4j
@Transactional
@ConditionalOnProperty(prefix = "mwModule", name = "ipPrower", havingValue = "true")
public class MwIpAddressManagePowerServiceImpl implements MwIpAddressManagePowerService {

    @Value("${datasource.check}")
    private String DATACHECK;
    @Resource
    MwIpAddressManageTableDao mwIpAddressManageTableDao;

    @Resource
    MwIpAddressManageListTableDao mwIpAddressManageListTableDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;


    @Resource
    private MWUserDao mwuserDao;
    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;
    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MWMessageService mwMessageService;
    @Autowired
    private MwCustomcolService mwCustomcolService;

    @Autowired
    private MwIpAddressManageService mwIpAddressManageService;

    @Resource
    MwIpv6ManageTableDao mwIpv6ManageTableDao;
    @Resource
    private MwUserOrgMapperDao mwUserOrgMapperDao;
    @Resource
    MwIpAddressManagePowerTableDao ipAddressManagePowerTableDao;

    @Autowired
    private MwLabelManageService mwLabelManageService;

    @Autowired
    private MWOrgService mwOrgService;

    private static final Pattern IPV4_REGEX =
            Pattern.compile(
                    "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
    private static final String ORG_IDS = "orgIds";
    private static final String GROUP_IDS = "groupIds";
    private static final String PRINCIPAL = "principal";
// 原有分配逻辑
//    @Override
//    public Reply selectListbrow(QueryIpAddressDistributtionParam param) {
//        List<ResponIpDistributtionParam> responIpDistributtionParams = new ArrayList<>();
//        String iptype ="ipv4";
//        if (param.isIdType()){
//            iptype ="ipv6";
//        }
//
//        Integer countIp = ipAddressManagePowerTableDao.selectCountDistribution(param.getId(),iptype,1);
//        if (countIp>0){
//            return  Reply.fail("存在IP地址已分配");
//        }else {
//            List<Check> maps = ipAddressManagePowerTableDao.selectListradio(iptype);
//            for (Integer s: param.getId()) {
//                ResponIpDistributtionParam responIpDistributtionParam = ipAddressManagePowerTableDao.selectResponIpDistributtionParam(s,iptype);
//                responIpDistributtionParam.putradom(maps);
//                responIpDistributtionParam.setIdType(param.isIdType());
//                responIpDistributtionParams.add(responIpDistributtionParam);
//            }
//
//        }
//
//        return  Reply.ok(responIpDistributtionParams);
//    }


    @Override
    public Reply selectListbrow(QueryIpAddressDistributtionParam param) {
        List<ResponIpDistributtionParam> responIpDistributtionParams = new ArrayList<>();
        param.setIdType(!param.isIdType());
        String iptype =IPV4_JUDGE ;
        if (param.isIdType()) {
            iptype =IPV6_JUDGE;
        }

        Integer countIp = ipAddressManagePowerTableDao.selectCountDistribution(param.getId(), iptype, 1);
      /*  if (countIp > 0) {
            return Reply.fail("当前地址已被禁止分配");
        } else {*/
            List<IpAddressManageTableParam> tree1 = selectAllTree(param.getId(), iptype);
            List<String> tree = new ArrayList<>();
            for (IpAddressManageTableParam node:tree1) {
                tree.add(node.getLabel());
            }


//            List<Check> maps = ipAddressManagePowerTableDao.selectListradio(iptype);


            List<Integer> upTreeid = new ArrayList<>();
            List<Integer> downTreeid = new ArrayList<>();
            List<String> upTree = new ArrayList<>();
            List<String> downTree = new ArrayList<>();
            String upTreeText = "";
            Integer length = tree.size();

            for (int i = 0; i < tree1.size(); i++) {
                if (tree1.get(i).getParentId()==0){
                    upTreeid.add(tree1.get(i).getId());
                    upTree.add(tree.get(i));
                    upTreeText= tree1.get(i).getLabel();
                }else if (tree1.get(i).getType().equals(IP_GROUPING)){
                    downTreeid.add(tree1.get(i).getId());
                    downTree.add(tree.get(i));
                }
            }

            Integer parentId = 0;
            if (iptype ==IPV4_JUDGE ) {
                parentId = ipAddressManagePowerTableDao.selectIpParent(param.getId().get(0));
            } else {
                parentId = ipAddressManagePowerTableDao.selectIpvsixParent(param.getId().get(0));
                parentId = ipAddressManagePowerTableDao.selectIpv6AddressManageTableParam(parentId).getParentId();
            }
            IpAddressManageTableParam ipAddressManageTableParam = ipAddressManagePowerTableDao.selectIpAddressManageTableParam(parentId);
            if (ipAddressManageTableParam.getRadioStatus() == 0) {
                return Reply.fail(UN_DISTRI_IP);
            }
            Integer graParentId = ipAddressManageTableParam.getId();
            Integer graGraParentId = graParentId;
            if (length - 2 >= 0) {
                graParentId = ipAddressManagePowerTableDao.selectIpAddressManageTableParam(graGraParentId).getParentId();
                graGraParentId = graParentId;
            }

            for (Integer s : param.getId()) {
                ResponIpDistributtionParam responIpDistributtionParam = ipAddressManagePowerTableDao.selectResponIpDistributtionParam(s, iptype);
                responIpDistributtionParam.setKeyValue(graGraParentId.toString());
                responIpDistributtionParam.setKeyValueInt(graGraParentId);
                List<Check> maps = new ArrayList<>();
                Check check = new Check();
                if (downTree.size() > 0) {
                    check.setLabel(downTree.get(downTree.size() - 1));
                } else {
                    check.setLabel(upTree.get(upTree.size() - 1));
                }

                check.setParentId(graParentId);
                check.setKeyValue(graParentId.toString());
                check.setIdType(param.isIdType());
                check.setKeyTestValue(responIpDistributtionParam.getPrimaryIp());
                if (!param.isIdType()) {
                    check.setRadom(String.valueOf(graParentId * 100000));
                } else {
                    check.setRadom(String.valueOf(-graParentId));
                }
                maps.add(check);
                responIpDistributtionParam.setRadio(maps);
                responIpDistributtionParam.setUpTree(upTreeid);
                responIpDistributtionParam.setUpTreeText(upTreeText);
                responIpDistributtionParam.setDownTree(downTree.size() > 0 ? downTreeid : upTreeid);
                responIpDistributtionParam.setLabel(downTree.size() > 0 ? downTree.get(0) : upTree.get(0));
                responIpDistributtionParam.setIdType(param.isIdType());
                List<Label> labels = ipAddressManagePowerTableDao.selectLabel(s, Level_BASCIS, iptype ==IPV4_JUDGE  ? DataType.IP.getName() : DataType.IPV6.getName());
                List<Label> labelList = new ArrayList<>();
                for (Label label : labels) {
                    label.setLabelIpId(param.getId().get(0));
                    label.setLabelIpType(param.isIdType() == false ? 0 : 1);
                    List<LabelCheck> checks = ipAddressManagePowerTableDao.browDrop(label.getLabelDrop());
                    label.setLabelChecks(checks);
                    labelList.add(label);
                }
                responIpDistributtionParam.setAttrParam(labels);
                responIpDistributtionParams.add(responIpDistributtionParam);
            }


/*        }*/

        return Reply.ok(responIpDistributtionParams);
    }

    private List<IpAddressManageTableParam> selectAllTree(List<Integer> id, String iptype) {
        List<IpAddressManageTableParam> list = new ArrayList<>();
        Integer IPid = id.get(0);

        List<IpAddressManageTableParam> listname = selectIPvFourParentTree(IPid, list, iptype);

        return listname;
    }


    private List<IpAddressManageTableParam> selectIPvFourParentTree(Integer iPid, List<IpAddressManageTableParam> list, String iptype) {

        Integer parentId = 0;
        List<IpAddressManageTableParam> listname = new ArrayList<>();
        if (iptype ==IPV4_JUDGE ) {
            parentId = ipAddressManagePowerTableDao.selectIpParent(iPid);
            listname = selectIpAddressManageTableParam(parentId, list);
        } else {
            parentId = ipAddressManagePowerTableDao.selectIpvsixParent(iPid);

            IpAddressManageTableParam ipAddressManageTableParam = ipAddressManagePowerTableDao.selectIpv6AddressManageTableParam(parentId);
            listname.add(ipAddressManageTableParam);

            listname.addAll(selectIpAddressManageTableParam(ipAddressManageTableParam.getParentId(), list));
        }

        return listname;
    }


    private List<IpAddressManageTableParam> selectIpAddressManageTableParam(Integer parentId, List<IpAddressManageTableParam> list) {
        IpAddressManageTableParam ipAddressManageTableParam = ipAddressManagePowerTableDao.selectIpAddressManageTableParam(parentId);
        if (ipAddressManageTableParam == null) {
            IpAddressManageTableParam ipAddressManageTableParam1 = new IpAddressManageTableParam();
            ipAddressManageTableParam1.setLabel(REMOVE_TEXT);
            list.add(ipAddressManageTableParam1);
            return list;
        }
        if (ipAddressManageTableParam.getParentId() == 0) {
            list.add(ipAddressManageTableParam);
            return list;
        } else {
            list.add(ipAddressManageTableParam);
            return selectIpAddressManageTableParam(ipAddressManageTableParam.getParentId(), list);
        }
    }

    @Override
    @Transactional
    public Reply createDistributtion(RequestIpAddressDistributtionParam param) {
        Reply reply;
        List<ResponIpDistributtionParam> responIpDistributtionParams = param.getResponIpDistributtionParams();
        List<ResponIpDistributtionParam> responIpDistributtionParamsAdd = new ArrayList<>();
        String getDistri = null;

        if (param.getSignId()==null){
            param.setSignId(1);
        }
        Boolean tyle = false;
        for (int i = 0; i < responIpDistributtionParams.size(); i++) {
            ResponIpDistributtionParam responIpDistributtionParam = responIpDistributtionParams.get(i);
            responIpDistributtionParam.setPrimaryIp(responIpDistributtionParam.getPrimaryIp());
            Integer id = ipAddressManagePowerTableDao.selectIPaddressId(responIpDistributtionParam.getPrimaryIp(),param.getSignId());
            responIpDistributtionParam.setId(id);

            List<String> strings = createdAddIP(responIpDistributtionParam);
            List<Integer> integer = ipAddressManagePowerTableDao.selectIPaddress(strings, 0,param.getSignId());
            List<ResponIpDistributtionParam> responIpDistributtionParamList = new ArrayList<>();
            if (strings.size() == integer.size()) {
                tyle = isIPv4Address(responIpDistributtionParam.getPrimaryIp());
                List<ResponIpDistributtionParam> ipDistributtionParams = createdAddIPAddress(tyle, responIpDistributtionParam,getDistri,param.getSignId());
                responIpDistributtionParamList.addAll(ipDistributtionParams);
            } else {
                getDistri=UUIDUtils.getUUID();
                tyle = isIPv4Address(responIpDistributtionParam.getPrimaryIp());
                List<ResponIpDistributtionParam> ipDistributtionParams = createdAddIPAddress(tyle, responIpDistributtionParam,getDistri,param.getSignId());
                responIpDistributtionParamList.addAll(ipDistributtionParams);
            }


            responIpDistributtionParamsAdd.addAll(responIpDistributtionParamList);

            createdHis(responIpDistributtionParam, param.getRequestIpAddressDistributtionSeniorParam(), 0,getDistri,param.getSignId());
//            ipAddressManagePowerTableDao.updateStats();
        }
        String org = param.getRequestIpAddressDistributtionSeniorParam().getOrgIds().toString();
        String orgtest = param.getRequestIpAddressDistributtionSeniorParam().getOrgtext().toString();
        ipAddressManagePowerTableDao.inster(responIpDistributtionParamsAdd, param.getRequestIpAddressDistributtionSeniorParam(), !tyle, org, orgtest);

        return Reply.ok();
    }


/*    @Override
    @Transactional
    public Reply selectListDistributtionbrow(QueryIpAddressDistributtionParam param) throws ParseException {
        List<ResponIpDistributtionParam> responIpDistributtionParams = new ArrayList<>();
        String iptype =IPV4_JUDGE ;
        if (!param.isIdType()) {
            iptype =IPV6_JUDGE;
        }

        Integer countIp = ipAddressManagePowerTableDao.selectCountDistribution(param.getId(), iptype, 0);
        if (countIp > 0) {
            return Reply.fail("存在IP地址未分配");
        } else {

            for (Integer s : param.getId()) {
                Map<String, Integer> primary = ipAddressManagePowerTableDao.selectPrimary(s, iptype);
                if (primary == null) {
                    return Reply.fail("不存在分配地址ip");
                }
                iptype =IPV6_JUDGE;
                if (primary.get(PRIMARY_IP_TYPE) == 0) {
                    iptype =IPV4_JUDGE ;
                }
                List<String> tree = selectAllTree(param.getId(), iptype);
                Collections.reverse(tree);

                List<String> upTree = new ArrayList<>();
                List<String> downTree = new ArrayList<>();
                Integer length = tree.size();

                for (int i = 0; i < tree.size() - 1; i++) {
                    if (i >= length - 2 && length - 2 > 0) {
                        downTree.add(tree.get(i));
                    } else {
                        upTree.add(tree.get(i));
                    }
                }
                ResponIpDistributtionParam responIpDistributtionParam = ipAddressManagePowerTableDao.selectResponIpDistributtionParam(primary.get(PRIMARY_IP), iptype);
                responIpDistributtionParam.setIdType(param.isIdType());
                List<Check> maps = new ArrayList<>();

                maps.addAll(ipAddressManagePowerTableDao.selectCheck(responIpDistributtionParam.getId(), iptype));
                responIpDistributtionParam.putradom(maps);

                List<Label> labels = ipAddressManagePowerTableDao.selectLabel(s, 6, iptype ==IPV4_JUDGE  ? DataType.IP.getName() : DataType.IPV6.getName());
                List<Label> labelList = new ArrayList<>();
                for (Label label : labels) {
                    label.setLabelIpId(param.getId().get(0));
                    label.setLabelIpType(param.isIdType() == false ? 0 : 1);
                    if (label.getInputFormat().equals(INPUT_TYPE_TWO)) {
                        SimpleDateFormat df = new SimpleDateFormat(TIME_STATUS);
                        label.setDateTagboard(df.parse(label.getTestValue()));
                    } else if (label.getInputFormat().equals(INPUT_TYPE_THERE)) {
                        label.setDropTagboard(label.getLabelDropId());
                    } else if (label.getInputFormat().equals(INPUT_TYPE_ONE)) {
                        label.setTagboard(label.getTestValue());
                    }
                    List<LabelCheck> checks = ipAddressManagePowerTableDao.browDrop(label.getLabelDrop());
                    label.setLabelChecks(checks);
                    labelList.add(label);
                }
                responIpDistributtionParam.setAttrParam(labelList);
                responIpDistributtionParam.setUpTree(upTree);
                responIpDistributtionParam.setDownTree(downTree);
                responIpDistributtionParams.add(responIpDistributtionParam);
            }

        }
        return Reply.ok(responIpDistributtionParams);
    }*/

   /* @Override
    public Reply cleanIP(QueryIpAddressDistributtionParam param) {
        List<ResponIpDistributtionParam> responIpDistributtionParams = new ArrayList<>();

        String iptype =IPV4_JUDGE ;
        if (!param.isIdType()) {
            iptype =IPV6_JUDGE;
        }

        Integer countIp = ipAddressManagePowerTableDao.selectCountDistribution(param.getId(), iptype, 0);
        if (countIp > 0) {
            return Reply.fail("存在IP地址未分配");
        } else {

            for (Integer s : param.getId()) {
                List<Check> maps = new ArrayList<>();
                Map<String, Integer> primary = ipAddressManagePowerTableDao.selectPrimary(s, iptype);
                if (primary == null) {
                    return Reply.fail("不存在分配地址ip");
                }
                iptype =IPV6_JUDGE;
                if (primary.get(PRIMARY_IP_TYPE) == 0) {
                    iptype =IPV4_JUDGE ;
                }
                ResponIpDistributtionParam responIpDistributtionParam = ipAddressManagePowerTableDao.selectResponIpDistributtionParam(primary.get(PRIMARY_IP), iptype);
                maps.addAll(ipAddressManagePowerTableDao.selectCheck(responIpDistributtionParam.getId(), iptype));
                responIpDistributtionParam.putradom(maps);
                responIpDistributtionParam.setIdType(!param.isIdType());
                responIpDistributtionParams.add(responIpDistributtionParam);
            }
        }
        List<String> strings = new ArrayList<>();
        for (ResponIpDistributtionParam responIpDistributtionParam : responIpDistributtionParams) {
            strings.addAll(createdAddIP(responIpDistributtionParam));
            deleteDistributttion(responIpDistributtionParam, strings);
            createdHis(responIpDistributtionParam, null, 1,param.getBangDistri());
        }


        return Reply.ok();
    }*/

    @Override
    public Reply MWIpAddressPowerHistorybrow(QueryIpAddressDistributtionParam param) throws ParseException {
        String iptype =IPV4_JUDGE ;
        if (!param.isIdType()) {
            iptype =IPV6_JUDGE;
        }
        PageHelper.startPage(param.getPageNumber(), param.getPageSize());
        List<ResponseIpAddressOperHistoryParamOB> maps = ipAddressManagePowerTableDao.selectOperHistory(param.getId().get(0), !param.isIdType(), param.getKeytype() != null ? param.getKeytype() : 3, param.getUpdateDateStart() != null ? param.getUpdateDateStart() : null, param.getUpdateDateEnd() != null ? new Date(param.getUpdateDateEnd().getTime() + 24 * 60 * 60 * 1000) : null);
        PageInfo pageInfo = new PageInfo<>(maps);
        List<ResponseIpAddressOperHistoryParam> kill = new ArrayList<>();
        for (ResponseIpAddressOperHistoryParamOB ipamOperHistoryDTO : maps) {
            ResponseIpAddressOperHistoryParam killOne = new ResponseIpAddressOperHistoryParam();
            killOne.setParamOB(ipamOperHistoryDTO);
            killOne.setRecoveryId(ipamOperHistoryDTO.getRecoveryId());
            killOne.setRecoveryDate(ipamOperHistoryDTO.getRecoveryDate());
            killOne.setDistributtionDate(ipamOperHistoryDTO.getApplicantDate());
            killOne.setDistributtionId(ipamOperHistoryDTO.getDistributtionId());
            killOne.setCountTime(CalculateTime(ipamOperHistoryDTO.getRecoveryDate() == null ? new Date() : ipamOperHistoryDTO.getRecoveryDate(), ipamOperHistoryDTO.getApplicantDate()));
            List<Label> labels = ipAddressManagePowerTableDao.selectLabel(ipamOperHistoryDTO.getLabelLinkId(), Level_SENIOR, DataType.IPHIS.getName());
            List<Label> labelList = new ArrayList<>();
            for (Label label : labels) {
                label.setLabelIpId(param.getId().get(0));
                label.setLabelIpType(param.isIdType() == false ? 0: 1);
                if (label.getInputFormat().equals(INPUT_TYPE_TWO)) {
                    SimpleDateFormat df = new SimpleDateFormat(TIME_STATUS);
                    if (label.getTestValue()==null||label.getTestValue().equals("")){
                        label.setDateTagboard(null);
                    }else {
                        label.setDateTagboard(df.parse(label.getTestValue()));
                    }
                } else if (label.getInputFormat().equals(INPUT_TYPE_THERE)) {
                    label.setDropTagboard(label.getLabelDropId());
                } else if (label.getInputFormat().equals(INPUT_TYPE_ONE)) {
                    label.setTagboard(label.getTestValue());
                }
                List<LabelCheck> checks = ipAddressManagePowerTableDao.browDrop(label.getLabelDrop());
                label.setLabelChecks(checks);
                labelList.add(label);
            }
            killOne.setAttrParam(labelList);
            kill.add(killOne);
        }

        pageInfo.setList(kill);

        return Reply.ok(pageInfo);
    }

    @Override
    public Reply selectLabel(QueryIpAddressDistributtionParam param) {
        if (param.getId() != null && param.getId().size() > 1) {
            return Reply.fail(SCREACH_TOO_IP);
        } else {
            Integer integer = Level_BASCIS;
            if (param.isLabelLevel()) {
                integer = Level_SENIOR;
            }
            Integer id = 0;
            boolean type = true;
            if (param.getId() != null && param.getId().size() != 0) {
                id = param.getId().get(0);
                type = param.isIdType();
            }
            boolean isRequired = false;
            List<Label> labels = new ArrayList<>();
            labels.addAll(ipAddressManagePowerTableDao.selectLabel(id, integer, type == true ? DataType.IP.getName() : DataType.IPV6.getName()));


            List<Label> labelList = new ArrayList<>();
            for (Label label : labels) {
                label.setInitDropValue();
                label.setLabelIpId(id);
                label.setLabelIpType(type == false ? 0 : INT_STAUS_NOT);
                if (label.getInputFormat().equals(INPUT_TYPE_THERE)) {
                    List<LabelCheck> checks = ipAddressManagePowerTableDao.browDrop(label.getLabelDrop());
                    label.setLabelChecks(checks);
                }
                if (label.getIsRequired()) {
                    isRequired = true;
                }
                labelList.add(label);
            }
            Map<String, Object> map = new HashMap<>();
            map.put(LABEL_LIST, labelList);
            map.put(LABEL_IS_REQUIRE, isRequired);
            return Reply.ok(map);
        }
    }

    @Override
    public Reply createLabel(LabelCheck param) {
        ipAddressManagePowerTableDao.createLabel(param);
        return Reply.ok(param.getDropId());
    }

    @Override
    public Reply deleteLabel(LabelCheck param) {
        ipAddressManagePowerTableDao.deleteLabel(param.getDropId());
        return Reply.ok(SUCCESSFUL);
    }

    @Override
    public Reply browDrop(String name) {
        List<LabelCheck> checks = ipAddressManagePowerTableDao.browDrop(name);
        return Reply.ok(checks);
    }

    @Override
    public Reply selectSnoListTest(QueryIpAddressPowerManageListParam queryIpAddressManageParam) throws Exception {

        List<QueryIpAddressManageListParam> list = new ArrayList<>();
        Boolean isALL = false;
        if (queryIpAddressManageParam.getSearchId() == null) {

        } else {
            if (queryIpAddressManageParam.getSearchId().size() == 0) {
                return Reply.ok(UNFIND_USEFUL_IP);
            }
            for (Integer i : queryIpAddressManageParam.getSearchId()) {
                if (i == 0) {
                    isALL = true;
                }
            }
            if (!isALL) {
                List<String> strings = ipAddressManagePowerTableDao.selectCountIPDIsEnabel(queryIpAddressManageParam.getSearchId());
                if (strings.size() > 0) {
                    return Reply.ok(UN_DISTRI_IP_LIST+ strings.toString());
                }
            }

        }
        PageHelper.startPage(queryIpAddressManageParam.getPageNumber(), queryIpAddressManageParam.getPageSize());
        queryIpAddressManageParam.setIsALL(isALL);
        Map priCriteria = PropertyUtils.describe(queryIpAddressManageParam);
        if (queryIpAddressManageParam.getOrderName() != null && queryIpAddressManageParam.getOrderType() != null) {
            String s = ConverParam.HumpToLine(queryIpAddressManageParam.getOrderName());
            priCriteria.put(ORDER_NAME, s);
        }

        list = ipAddressManagePowerTableDao.selectSonList(priCriteria);
        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply selectSonList(QueryIpAddressPowerManageListParam qParam) throws Exception {
        PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
        Map priCriteria = PropertyUtils.describe(qParam);
        if (qParam.getOrderName() != null && qParam.getOrderType() != null) {
            String s = ConverParam.HumpToLine(qParam.getOrderName());
            priCriteria.put(ORDER_NAME, s);
        }
        List<QueryIpAddressManageListParam> list = ipAddressManagePowerTableDao.selectIpv6SonList(priCriteria);
        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply saveLabel(List<Label> name) {
        List<Label> check = new ArrayList<>();
        if (name.size() > 0) {
            Label label = name.get(0);
            Integer integer = Level_BASCIS;
            if (label.isLabelLevel()) {
                integer = Level_SENIOR;
            }
            check.addAll(ipAddressManagePowerTableDao.selectLabel(label.getLabelIpId(), integer, label.getLabelIpType() == 0 ? DataType.IP.getName() : DataType.IPV6.getName()));
        }
        if (check.size() > 0) {
            for (Label label : name) {
                Integer dropid = saveCheck(label);
                label.setLabelDropId(dropid);
                for (Label labels : check) {

                    if (labels.getLabelId() == label.getLabelId()) {
//                        if (labels.getLabelDropId() != null) {
//                            ipAddressManagePowerTableDao.updateIpaddressDrop(label);
//                        } else {
//                            ipAddressManagePowerTableDao.insterIpaddressDrop(label);
//                        }
                        //abcdefg
                    }
                }

            }
        } else {
            for (Label label : name) {
                Integer dropid = saveCheck(label);
                label.setLabelDropId(dropid == 0 ? null : dropid);
                ipAddressManagePowerTableDao.insterIpaddressDrop(label);
            }
        }
        return Reply.ok();
    }

    @Override
    public boolean createIpv6(String param, String keyValue,Integer signId) {
        String intrand = IPv6Judge.getInstance().Ipv6IptoBigInteger(param).toString();
        Integer llinkid = ipAddressManagePowerTableDao.selectipv6rand(intrand, keyValue);
        if (llinkid == null) {
            return false;
        } else {
            Integer kill = ipAddressManagePowerTableDao.selectIpv6(param,signId);
            if (kill == 0) {
                ipAddressManagePowerTableDao.insterIpv6list(llinkid, param, iLoginCacheInfo.getLoginName(),signId);
            }
            return true;
        }
    }

    @Override
    public Reply selcectCheck(QueryIpAddressDistributtionParam param) {
        List<Check> maps = ipAddressManagePowerTableDao.selectListradioTwo(param.getId());
        Integer graGraParentId = -1;
        Check check = new Check();
        if (maps.size() > 0) {
            check = maps.get(0);
            check.setIdType(param.isIdType());
            graGraParentId = ipAddressManagePowerTableDao.selectIpAddressManageTableParam(param.getId().get(0)).getId();
            check.setParentId(graGraParentId);
            check.setId(param.getId().get(0));
            ;
            if (!param.isIdType()) {
                Integer in = param.getId().get(0) * 10000;
                check.setRadom(in.toString());
            } else {
                check.setRadom("-" + param.getId().get(0).toString());
            }

        }
        return Reply.ok(check);
    }

    @Override
    public Reply selectTree(QueryIpAddressManageParam qParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        mwMessageService.createMessage(null, 2, 0,null);
        if (qParam.getParentId()==null){
            return Reply.ok();
        }
         else if (qParam.getParentId()>0){
            List<Object> hask = new ArrayList<>();
            List<IpAddressManageTableParam> list = queryIpv4(qParam);
            hask.addAll(list);
            if (!qParam.getPop() == true) {
                List<Ipv6ManageTableParam> listOne = queryipv6(qParam);
                hask.addAll(listOne);
            }
            Collections.sort(hask, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    IpAddressManageTableParam s1 = JSONObject.parseObject(JSONObject.toJSONString(o1), IpAddressManageTableParam.class);
                    IpAddressManageTableParam s2 = JSONObject.parseObject(JSONObject.toJSONString(o2), IpAddressManageTableParam.class);
                    return s1.getIndexSort().compareTo(s2.getIndexSort());
                }
            });
            List<IpAddressManageTableParam> list1 = new ArrayList<>();
            for (Object ipAddressManageTableParam:hask) {
                IpAddressManageTableParam s2 = JSONObject.parseObject(JSONObject.toJSONString(ipAddressManageTableParam), IpAddressManageTableParam.class);
                s2.setIssueNo(getAllIssue(0,s2.getParentId()));
                s2.setIssueDone(getAllIssue(1,s2.getParentId()));
                list1.add(s2);
            }

            PageList pageList = new PageList();
            PageInfo pageInfo = new PageInfo<>(list1);
            if (qParam.getPageNumber() == null) {
                pageInfo.setList(list1);
            } else {
                pageInfo.setTotal(list1.size());
                hask = pageList.getList(list1, qParam.getPageNumber(), qParam.getPageSize());
                pageInfo.setList(list1);
            }
            return Reply.ok(pageInfo);
        }
         else {
            QueryIpAddressManageParam qPara = new QueryIpAddressManageParam();
            qPara.setParentId(0);
            qPara.setType(IP_GROUPING);
            List<Object> hask = new ArrayList<>();
            List<IpAddressManageTableParam> listone = getAllIPv4(qPara, new ArrayList<>(),1,new ArrayList<>(),new ArrayList<>());
            for (IpAddressManageTableParam ip:listone) {
                if (ip.getId().equals(-qParam.getParentId())){
                    List<Object> ipAddressManageTableParamList = ip.getChildren();
                    for (Object o : ipAddressManageTableParamList){
                        IpAddressManageTableParam ipAddressManageTableParam = new IpAddressManageTableParam();
                        BeanUtils.copyProperties(o,ipAddressManageTableParam);
                        if (ipAddressManageTableParam.getId().equals(qParam.getParentId())){
                            hask.addAll(ipAddressManageTableParam.getChildren());
                        }
                    }
                }
            }
            PageList pageList = new PageList();
            PageInfo pageInfo = new PageInfo<>(hask);
            if (qParam.getPageNumber() == null) {
                pageInfo.setList(hask);
            } else {
                pageInfo.setTotal(hask.size());
                hask = pageList.getList(hask, qParam.getPageNumber(), qParam.getPageSize());
                pageInfo.setList(hask);
            }
            return Reply.ok(pageInfo);

    }

    }

    private Integer getAllIssue(int i, Integer id) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Integer count  = 0;
        List<Integer> linkids = getAlllinkids(id);
        if (linkids.size()>0){
            count  = ipAddressManagePowerTableDao.getCountIssue(i,linkids);
        }else {
            count=0;
        }
        return count;
    }



    private List<Integer> getAlllinkids(Integer id) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<Integer> linkids = new ArrayList<>();
        QueryIpAddressManageParam qPara = new QueryIpAddressManageParam();
        qPara.setParentId(id);
        qPara.setType(IP_GROUPING);
        List<Object> hask = new ArrayList<>();
        List<IpAddressManageTableParam> listone = getAllIPv4(qPara, new ArrayList<>(),1,new ArrayList<>(),new ArrayList<>());
        for (IpAddressManageTableParam ip:listone) {
            if (ip.getType().equals(IP_ADDRESSES)){
                if (ip.isIPv4()==true){
                    linkids.add(ip.getId());
                }else {
                    linkids.add(-ip.getId());
                }
            }
        }
        return linkids;
    }

    @Override
    public Reply historyGroup(QueryIpAddressDistributtionParam param) {
        List<CleanParam> maps = new ArrayList<>();
        String iptype =IPV4_JUDGE ;
        if (!param.isIdType()) {
            iptype =IPV6_JUDGE;
        }

        Integer countIp = ipAddressManagePowerTableDao.selectCountDistribution(param.getId(), iptype, 0);
        if (countIp > 0) {
            return Reply.fail(UN_DISTRI_IP);
        } else {

            for (Integer s : param.getId()) {
                Map<String, Integer> primary = ipAddressManagePowerTableDao.selectPrimary(s, iptype,param.getBangDistri());
                if (primary == null) {
                    return Reply.fail(UNFIND_USEFUL_IP);
                }
                iptype =IPV6_JUDGE;
                if (primary.get(PRIMARY_IP_TYPE) == 0) {
                    iptype =IPV4_JUDGE ;
                }
                ResponIpDistributtionParam responIpDistributtionParam = ipAddressManagePowerTableDao.selectResponIpDistributtionParam(primary.get(PRIMARY_IP), iptype);

                responIpDistributtionParam.setIdType(param.isIdType());
                List<CleanParam> list = ipAddressManagePowerTableDao.selectCheck(responIpDistributtionParam.getId(), iptype);

                List<Map<String, Object>> parentLabel = ipAddressManagePowerTableDao.slecetParentLabel(primary.get(PRIMARY_IP), primary.get(PRIMARY_IP_TYPE));
                /*父节点位置加对应iIPd*/
                for (Map<String, Object> map : parentLabel) {
                    CleanParam c = new CleanParam();
                    c.setGraparentId((Integer) map.get(MAP_PARENT_ID));
                    c.setGraparentName(map.get(LABEL).toString());
                    c.setIdType((Integer) map.get(IP_TYPE) == 0 ? false : true);
                    c.setKeyTestValue(map.get(IP_ADDRESS_MAP).toString());
                    c.setKeyValue(map.get(LIST_ID).toString());
                    c.setId((Integer) map.get(LIST_ID));
                    c.setParentId((Integer) map.get(MAP_PARENT_ID));
                    maps.add(c);
                }
            }

        }
        return Reply.ok(maps);
    }

   /* @Override
    public Reply selectListDistributtionSeniorParamBrowse(QueryIpAddressDistributtionParam param) throws ParseException {
        List<RequestIpAddressDistributtionSeniorParam> requestIpAddressDistributtionSeniorParams = new ArrayList<>();
        String iptype =IPV4_JUDGE ;
        if (!param.isIdType()) {
            iptype =IPV6_JUDGE;
        }

        Integer countIp = ipAddressManagePowerTableDao.selectCountDistribution(param.getId(), iptype, 0);
        if (countIp > 0) {
            return Reply.fail("存在IP地址未分配");
        } else {

            for (Integer s : param.getId()) {
                Map<String, Integer> primary = ipAddressManagePowerTableDao.selectPrimary(s, iptype);
                if (primary == null) {
                    return Reply.fail("不存在分配地址ip");
                }
                iptype =IPV6_JUDGE;
                if (primary.get(PRIMARY_IP_TYPE) == 0) {
                    iptype =IPV4_JUDGE ;
                }
                RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam = new RequestIpAddressDistributtionSeniorParam();
                IpAddressDistributtionSeniorParam ipAddressDistributtionSeniorParam = ipAddressManagePowerTableDao.selectDistributtionSenior(primary.get(PRIMARY_IP), iptype,param.getBangDistri());
                IpamProcessHistoryDTO ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicant(primary.get(PRIMARY_IP), iptype,param.getBangDistri());
                requestIpAddressDistributtionSeniorParam.setApplicant(ipamProcessHistoryDTO.getApplicant());
                requestIpAddressDistributtionSeniorParam.setApplicantionDate(ipamProcessHistoryDTO.getApplicantDate());
                List<Label> labels = ipAddressManagePowerTableDao.selectLabel(ipamProcessHistoryDTO.getId(), Level_SENIOR, DataType.IPHIS.getName());
                List<Label> labelList = new ArrayList<>();
                for (Label label : labels) {
                    label.setLabelIpId(param.getId().get(0));
                    label.setLabelIpType(param.isIdType() == false ? 0 : 1);
                    if (label.getInputFormat().equals(INPUT_TYPE_TWO)) {
                        SimpleDateFormat df = new SimpleDateFormat(TIME_STATUS);
                        label.setDateTagboard(df.parse(label.getTestValue()));
                    } else if (label.getInputFormat().equals(INPUT_TYPE_THERE)) {
                        label.setDropTagboard(label.getLabelDropId());
                    } else if (label.getInputFormat().equals(INPUT_TYPE_ONE)) {
                        label.setTagboard(label.getTestValue());
                    }
                    List<LabelCheck> checks = ipAddressManagePowerTableDao.browDrop(label.getLabelDrop());
                    label.setLabelChecks(checks);
                    labelList.add(label);
                }
                requestIpAddressDistributtionSeniorParam.setAttrParam(labelList);
                requestIpAddressDistributtionSeniorParam.setApplicanttext(ipAddressManagePowerTableDao.selectUserName(ipamProcessHistoryDTO.getApplicant()));
                if (ipAddressDistributtionSeniorParam.getOrgIds() == null) {
                    ipAddressDistributtionSeniorParam.setOrgIds(FALSE_JUDGE);
                }
                List<List<Integer>> orgids = StringChangeList(ipAddressDistributtionSeniorParam.getOrgIds());
                requestIpAddressDistributtionSeniorParam.setOa(ipAddressDistributtionSeniorParam.getOa()).setOatext(ipAddressDistributtionSeniorParam.getOatext())
                        .setOaurl(ipAddressDistributtionSeniorParam.getOaurl()).setOaurltext(ipAddressDistributtionSeniorParam.getOaurltext()).setOrgIds(orgids).setOrgtext(Arrays.asList(ipAddressDistributtionSeniorParam.getOrgtext().substring(1, ipAddressDistributtionSeniorParam.getOrgtext().length() - 1).split(",")));
                requestIpAddressDistributtionSeniorParams.add(requestIpAddressDistributtionSeniorParam);

            }

        }
        return Reply.ok(requestIpAddressDistributtionSeniorParams);
    }*/

  /*  @Transactional
    @Override
    public Reply cleanSignleIP(List<QueryIpAddressDistributtionParam> qaram) {
        List<ResponIpDistributtionParam> responIpDistributtionParams = new ArrayList<>();
        for (QueryIpAddressDistributtionParam param : qaram) {
            String iptype =IPV4_JUDGE ;
            if (!param.isIdType()) {
                iptype =IPV6_JUDGE;
            }
            if (param.getIpList().size() < 1) {
                return Reply.fail("未选择回收地址");
            }
            Integer IpId = ipAddressManagePowerTableDao.selectIPaddressId(param.getIpList().get(0));
            Boolean ipStatus = isIPv4Address(param.getIpList().get(0));

            if (param.isSourceCheck()) {
                List<Integer> i = new ArrayList<>();
                i.add(IpId);
                param.setId(i);
                param.setIdType(ipStatus);
                cleanIP(param);
            } else {
                Integer countIp = ipAddressManagePowerTableDao.selectCountDistributionByIp(param.getIpList(), iptype, 0);
                if (countIp > 0) {
                    return Reply.fail("存在IP地址未分配");
                } else {
                    Map<String, Integer> primary = ipAddressManagePowerTableDao.selectPrimary(IpId, ipStatus == true ?IPV4_JUDGE  :IPV6_JUDGE);
                    Integer primaryId = primary.get(PRIMARY_IP);
                    Integer primary_type = primary.get(PRIMARY_IP_TYPE);
                    String ip = ipAddressManagePowerTableDao.selectIPaddressById(primaryId, primary_type);
                    List<String> ipv4 = new ArrayList<>();
                    List<String> ipv6 = new ArrayList<>();
                    for (String s : param.getIpList()) {
                        if (!ip.equals(s)) {
                            if (isIPv4Address(s)) {
                                ipv4.add(s);
                            } else {
                                ipv6.add(s);
                            }
                        }

                    }

                    cleanDistribution(ipv4, ipv6,param.getBangDistri());
                    createdHisbyIpAddress(ipv4, ipv6, IpId, ipStatus, 1,param.getBangDistri());
                }

            }
        }
        return Reply.ok();
    }*/

    @Override
    public Reply updateDistributtion(RequestIpAddressDistributtionParam param) {
        if (param.getSignId()==null){
            param.setSignId(1);
        }
        List<ResponIpDistributtionParam> responIpDistributtionParams = param.getResponIpDistributtionParams();
        for (ResponIpDistributtionParam responIpDistributtionParam : responIpDistributtionParams) {
            QueryIpAddressDistributtionParam queryIpAddressDistributtionParam = new QueryIpAddressDistributtionParam();
            List<Integer> list = new ArrayList<>();
            list.add(responIpDistributtionParam.getId());
            queryIpAddressDistributtionParam.setIdType(responIpDistributtionParam.getIdType());
            queryIpAddressDistributtionParam.setId(list);
            List<Check> maplist = responIpDistributtionParam.getRadio();
            List<Check> maps = new ArrayList<>();
            maps.addAll(ipAddressManagePowerTableDao.selectCheck(responIpDistributtionParam.getId(), responIpDistributtionParam.getIdType() == true ?IPV4_JUDGE  :IPV6_JUDGE));
            List<String> cleanipv4 = new ArrayList<>();
            List<String> cleanipv6 = new ArrayList<>();
            List<Check> addipv4 = new ArrayList<>();
            List<Check> addipv6 = new ArrayList<>();
            List<String> addipv4his = new ArrayList<>();
            List<String> addipv6his = new ArrayList<>();

            for (Check v : maps) {
                boolean kill = false;
                for (Check vs : maplist) {
                    if (v.getKeyTestValue().equals(vs.getKeyTestValue())) {
                        kill = true;
                    }
                }
                if (!kill) {
                    if (isIPv4Address(v.getKeyTestValue())) {
                        cleanipv4.add(v.getKeyTestValue());
                    } else {
                        cleanipv4.add(v.getKeyTestValue());
                    }
                }
            }
            for (Check v : maplist) {
                boolean kill = false;
                for (Check vs : maps) {
                    if (v.getKeyTestValue().equals(vs.getKeyTestValue())) {
                        kill = true;
                    }
                }
                if (!kill) {
                    if (isIPv4Address(v.getKeyTestValue())) {
                        addipv4.add(v);
                        addipv4his.add(v.getKeyTestValue());
                    } else {
                        addipv6.add(v);
                        addipv6his.add(v.getKeyTestValue());
                    }
                }
            }
            List<MwAssetsLabelDTO> mwAssetsLabelDTOS = new ArrayList<>();

            mwAssetsLabelDTOS.addAll(responIpDistributtionParam.getAttrData());
            mwAssetsLabelDTOS.addAll(param.getRequestIpAddressDistributtionSeniorParam().getAttrData());

            addDistribution(maplist, mwAssetsLabelDTOS, addipv4, addipv6, responIpDistributtionParam.getId(), responIpDistributtionParam.getIdType(), responIpDistributtionParam.getBangDistri(),param.getSignId());
            createdHisbyIpAddress(addipv4his, addipv6his, responIpDistributtionParam.getId(), responIpDistributtionParam.getIdType(), 0,param.getBangDistri(),param.getSignId());
            cleanDistribution(cleanipv4, cleanipv6,param.getBangDistri(),param.getSignId());
            createdHisbyIpAddress(cleanipv4, cleanipv6, responIpDistributtionParam.getId(), responIpDistributtionParam.getIdType(), 1,param.getBangDistri(),param.getSignId());
        }

        return null;
    }

    @Override
    public Reply selectById(QueryCustomPageParam qParam) {
        MwCustomPageDTO pageDTO = (MwCustomPageDTO) mwCustomcolService.selectById(qParam).getData();
        List<MwPageselectTable> psTable = pageDTO.getMwPageselectTables();
        List<Label> labels = ipAddressManagePowerTableDao.selectLabel(-1, Level_SENIOR, DataType.IPHIS.getName());
        List<MwCustomColDTO> pageDTOs=pageDTO.getMwCustomColDTOS();
        List<MwCustomColDTO> mwCustomColDTOS= new ArrayList<>();
        for (Label l:labels) {
            if (l.getLabelName().equals(APPLICANTOR)||l.getLabelName().equals(APPLICANT_NAME)){
                MwCustomColDTO customColDTO = new MwCustomColDTO();
                customColDTO.setColId(l.getLabelId());
                customColDTO.setLabel(l.getLabelName());
                customColDTO.setProp(l.getLabelName());
                customColDTO.setUserId(qParam.getUserId());
                customColDTO.setSortable(false);
                mwCustomColDTOS.add(0,customColDTO);
            }
        }
        for (Label label : labels) {
            MwPageselectTable mwPageselectTable = new MwPageselectTable();
            mwPageselectTable.setId(0);
            mwPageselectTable.setInputFormat(label.getInputFormat());
            mwPageselectTable.setLabel(label.getLabelName());
            mwPageselectTable.setProp(label.getLabelName());
            mwPageselectTable.setPageId(qParam.getPageId());
            psTable.add(mwPageselectTable);
        }
        for (MwCustomColDTO mwCustomColDTO:mwCustomColDTOS) {
            mwCustomColDTO.setVisible(true);
        }
        mwCustomColDTOS.addAll(pageDTOs);
        pageDTO.setMwCustomColDTOS(mwCustomColDTOS);
        pageDTO.setMwPageselectTables(psTable);
        return Reply.ok(pageDTO);
    }

    @Override
    public Reply getHisList(AddUpdateIpAddressManageListParam parm) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PageHelper.startPage(parm.getPageNumber(), parm.getPageSize());
        Map priCriteria = PropertyUtils.describe(parm);
        List<AddUpdateIpAddressManageListHisParam> list = mwIpAddressManageListTableDao.getHisList(priCriteria);
        List<Map<String, Object>> set = new ArrayList<>();
        List<Label> labels = ipAddressManagePowerTableDao.selectLabel(parm.getLabelLinkId(), Level_SENIOR, DataType.IPHIS.getName());

        Map<String, Object> mapSet = new HashMap<>();
        for (Label label : labels) {
            switch (label.getInputFormat()) {
                case INPUT_TYPE_ONE:
                    mapSet.put(label.getLabelName(), label.getTestValue());
                    break;
                case INPUT_TYPE_TWO:
                    mapSet.put(label.getLabelName(), label.getDateTagboard());
                    break;
                case INPUT_TYPE_THERE:
                    mapSet.put(label.getLabelName(), label.getDropValue());
                    break;
            }

        }
        ResponseIpAddressOperHistoryParamOB responseIpAddressOperHistoryParamOB = ipAddressManagePowerTableDao.selectOperHistoryByApplicant(parm.getLabelLinkId(), parm.getLinkId());
        Map<String, Object> mapObject = getObjectToMap(responseIpAddressOperHistoryParamOB);
        mapSet.putAll(mapObject);
  /*      setMap.putAll(mapSet);*/
        Map<String, Object> setMap = new HashMap<>();
        setMap.putAll(mapSet);
        String ipaddress = "";
        if (parm.getIpType().equals(IPV4_JUDGE_CATIPAL)){
            ipaddress=ipAddressManagePowerTableDao.selectIPaddressById(parm.getLinkId(),0);
        }else{
            ipaddress=ipAddressManagePowerTableDao.selectIPaddressById(parm.getLinkId(),1);
        }
        Map<String,Object> stringObjectMap = new HashMap<>();
        String bang_distri = ipAddressManagePowerTableDao.selectBangString(responseIpAddressOperHistoryParamOB.getDistributtionId());
       Map<String, Object>  map = ipAddressManagePowerTableDao.selectOperCleanHis(ipaddress,bang_distri);
        setMap.put(IP_ADDRESS,ipaddress);
        if (map!=null&&map.size()>0){
            Map<String ,Object> label = JSONObjectToMap(map.get(MAP_KEY_VALUE));
            setMap.put(CLEAN_DISTRIBUTTION,label.get(APPLICANT_NAME));
        }

        set.add(setMap);
//        for (AddUpdateIpAddressManageListHisParam map : list) {
          /*  Map<String, Object> setMap = getObjectToMap(map);
            setMap.putAll(mapSet);
            set.add(setMap);*/
//        }

        PageInfo pageInfo = new PageInfo<>(set);
        pageInfo.setList(set);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply fuzzSeachAllFiledData(AddUpdateIpAddressManageListParam parm) {
        List<Map<String, String>> maps = new ArrayList<>();
        if (parm.getIpType().trim().equals(IPV4_JUDGE_CATIPAL)) {
            maps = ipAddressManagePowerTableDao.fuzzSeachAllFiledData(DATACHECK.equals(DATEBASEORACLE)?MW_IPADDRESSMANAGELIST_TABLE_ORACLE:MW_IPADDRESSMANAGELIST_TABLE, parm.getLinkId()<0?-parm.getLinkId():parm.getLinkId());
        } else {
            maps = ipAddressManagePowerTableDao.fuzzSeachAllFiledData(DATACHECK.equals(DATEBASEORACLE)?MW_IPADDRESSMANAGELIST_TABLE_ORACLE:MW_IPADDRESSMANAGELIST_TABLE, parm.getLinkId()<0?-parm.getLinkId():parm.getLinkId());
        }
        Map<String, List> listMap = new HashMap<>();
        for (Map<String, String> map : maps) {
            if (listMap.get(map.get(MAP_TYPE)) == null) {
                List<String> strings = new ArrayList<>();
                strings.add(map.get(MAP_KEY_NAME));
                listMap.put(map.get(MAP_TYPE), strings);
            } else {
                List<String> strings = listMap.get(map.get(MAP_TYPE));
                strings.add(map.get(MAP_KEY_NAME));
                listMap.put(map.get(MAP_TYPE), strings);
            }
        }

        return Reply.ok(listMap);
    }

    @Override
    public Reply getAllIpManage(IpAllRequestBody ipAllRequestBody) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        QueryIpAddressManageParam qParam = new QueryIpAddressManageParam();
        qParam.setParentId(0);
        qParam.setType(IP_GROUPING);
        qParam.setSignId(ipAllRequestBody.getId());
        List<Object> list = getIPv4(qParam);
//        List<Object> list = getIPv4(qParam);

        PageList pageList = new PageList();
        PageInfo pageInfo = new PageInfo<>(list);
        if (qParam.getPageNumber() == null) {

            pageInfo.setList(list);
        } else {
            pageInfo.setTotal(list.size());
            list = pageList.getList(list, qParam.getPageNumber(), qParam.getPageSize());
            pageInfo.setList(list);
        }


        return Reply.ok(pageInfo);
    }

    @Override
    public Reply isInput(IsInput isInput) {
        IpAddressManageTableParam va = new IpAddressManageTableParam();
        va.setId(isInput.getIndexId());
        IpAddressManageTableParam table1Param = mwIpAddressManageTableDao.selectIpAddressById1(va);
        if (!table1Param.getType().equals(IP_GROUPING)) {
            return Reply.ok(false);
        }
        return Reply.ok(true);
    }

    @Override
    public Reply getLeader() {
        String loginName = iLoginCacheInfo.getLoginName();
        LoginContext loginContext = iLoginCacheInfo.getCacheInfo(loginName);
        LoginInfo loginInfo = loginContext.getLoginInfo();
        UserDTO userDTO = loginInfo.getUser();
        List<Integer> orgIds = mwUserOrgMapperDao.getOrgIds(loginName);
//        userDTO.setDepartment(orgIds);
        return Reply.ok(userDTO);
    }

    @Override
    @Transactional
    public Reply changeIndex(IsInput isInput) {
        Boolean change = false;
        IpAddressManageTableParam va = new IpAddressManageTableParam();
        va.setId(isInput.getDragging());
        va.setIPv4(isInput.isDraggingType());
        IpAddressManageTableParam table1Param = mwIpAddressManageTableDao.selectAllIpAddressById(va);
        va.setIPv4(isInput.isDropNodeType());
        va.setId(isInput.getDropNode());
        IpAddressManageTableParam table1Paramte = mwIpAddressManageTableDao.selectAllIpAddressById(va);
        if (table1Param.getId()==null||table1Paramte.getParentId()==null){
            return Reply.fail(FALIE_NUMBER_FUL);
        }

        switch (isInput.getType()) {
            case INNER_JUDGE:
                mwIpAddressManageTableDao.changeParentId(table1Param.getId(), isInput.isDraggingType(), table1Paramte.getId());
                break;
            case INNER_BEFORE:
                mwIpAddressManageTableDao.changeParentId(table1Param.getId(), isInput.isDraggingType(), table1Paramte.getParentId());
                change = changeBeforeSort(table1Param, isInput.isDraggingType(), isInput.isDropNodeType(), table1Paramte);
                break;
            case INNER_AFTER:
                mwIpAddressManageTableDao.changeParentId(table1Param.getId(), isInput.isDraggingType(), table1Paramte.getParentId());
                change = changeAfterSort(table1Param, isInput.isDraggingType(), table1Paramte);
                break;
            default:
                break;
        }
        return Reply.ok(change);
    }

    @Override
    @Transactional
    public Reply createNewDistributtion(RequestIpAddressDistributtionNewParam param, String operNum) {
        if (param.getSignId()==null){
            param.setSignId(1);
        }
        List<Map<String, Object>> maps = new ArrayList<>();
        List<Map<String, Object>> descMap = new ArrayList<>();
        List<String> IpAddress = new ArrayList<>();
        RequestIpAddressDistributtionNewParam requestIpAddressDistributtionNewParam = new RequestIpAddressDistributtionNewParam();
        List<ResponIpDistributtionNewParentParam> responIpDistributtionNewParentParams = param.getResponIpDistributtionParams();
        for (ResponIpDistributtionNewParentParam responIpDistributtionNewParentParam : responIpDistributtionNewParentParams) {
            List<ResponIpDistributtionNewParam> responIpDistributtionNewParentParamList = responIpDistributtionNewParentParam.getTreeData();
            for (ResponIpDistributtionNewParam responIpDistributtionNewParam : responIpDistributtionNewParentParamList) {
                List<Map<String, Object>> map = OrganizeIpData(IpAddress, descMap, responIpDistributtionNewParam.getChildren(), -1, -1, false, false, 0);
                maps.addAll(map);
            }
        }
        if (IpAddress.size()==0){
            return Reply.fail(HAVE_REPEAT_IP);
        }
        List<Integer> intege = ipAddressManagePowerTableDao.selectIPaddress(IpAddress, 0,param.getSignId());
        String getDistri = null;
        if (IpAddress.contains(HAVE_REPEAT_IP)) {
            return Reply.fail(HAVE_REPEAT_IP);
        }

    /*   if (intege.size() != IpAddress.size()) {*/
            getDistri  =UUIDUtils.getUUID();
  /*      }*/

        //获取生成分配操作后的所有key（id）
        createNewDistributtionRecord(maps, param.getRequestIpAddressDistributtionSeniorParam(),getDistri);

        for (ResponIpDistributtionNewParentParam responIpDistributtionNewParentParam : responIpDistributtionNewParentParams) {
            List<ResponIpDistributtionNewParam> responIpDistributtionNewParentParamList = responIpDistributtionNewParentParam.getTreeData();
            for (ResponIpDistributtionNewParam responIpDistributtionNewParam : responIpDistributtionNewParentParamList) {
                List<String> ipaddress = new ArrayList<>();
                /*描述信息整合*/
                Map<String, Object> objectMap = new HashMap<>();
                for (Map<String, Object> map : descMap) {
                    String key = "";
                    if ((boolean) map.get(BAND_IP_TYPE)) {
                        key = DISTINGUISH_IPV6;
                    } else {
                        key = DISTINGUISH_IPV4;
                    }
                    key = key + map.get(BAND_IP_ID);
                    objectMap.put(key, map.get(DESCRIPTION));
                }

                OrganizeIpData(ipaddress, descMap, responIpDistributtionNewParam.getChildren(), -1, -1, false, false, 0);
                createdNewHis(ipaddress, responIpDistributtionNewParentParam.getAttrData(), param.getRequestIpAddressDistributtionSeniorParam(), objectMap, 0,getDistri,param.getSignId());

                try {
                    //放入测试
                    createdIPScanHIS(maps, ipaddress,param.getSignId());
                } catch (Exception e) {

                }

            }
        }
        createIPGenerl(maps,descMap,param.getRequestIpAddressDistributtionSeniorParam(),operNum,IpAddress,getDistri);
        return Reply.ok(SUCCESSFUL);
    }

    private void createIPGenerl(List<Map<String, Object>> maps, List<Map<String, Object>> descMap, RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam, String operNum, List<String> ipAddress,String getDistri) {
        Map<String,String> newLabel = new HashMap<>();
        Date date  = requestIpAddressDistributtionSeniorParam.getApplicantionDate();
        for (MwAssetsLabelDTO label:requestIpAddressDistributtionSeniorParam.getAttrData()) {
            newLabel.put(label.getLabelName(),label.getDropValue());
        }

        ipAddressManagePowerTableDao.insertOperHis(JSONObject.toJSONString(maps),null,JSONObject.toJSONString(newLabel),null,operNum,new Date(),1,date,null,0,JSONObject.toJSONString(descMap),null,ipAddress.toString(),iLoginCacheInfo.getLoginName(),getDistri);
    }



    //创建端口历史
    private void createdIPScanHIS(List<Map<String, Object>> maps, List<String> strings,Integer signId) {
        List<AddUpdateIpAddressManageListHisParam> listHisParams = new ArrayList<>();
        for (String map : strings) {
            if (isIPv4Address(map)) {
                AddUpdateIpAddressManageListHisParam addUpdateIpAddressManageListHisParam = new AddUpdateIpAddressManageListHisParam();
                addUpdateIpAddressManageListHisParam.setBatchId(MwIpAddressManageScanService.PREFIX + UUIDUtils.getUUID());
                addUpdateIpAddressManageListHisParam.setIpAddress(map);
                Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(map,signId);
                addUpdateIpAddressManageListHisParam.setLinkId(integer);
                addUpdateIpAddressManageListHisParam.setUpdateDate(new Date());
                addUpdateIpAddressManageListHisParam.setChangeIpStatus(2);
                listHisParams.add(addUpdateIpAddressManageListHisParam);
            }

        }


        mwIpAddressManageListTableDao.batchCreateHis(listHisParams);
    }

    @Override
    public Integer selectByIPaddress(String ip,Integer signId) {
        Integer IpId = ipAddressManagePowerTableDao.selectIPaddressId(ip,signId);
        return IpId;
    }

    @Override
    public Reply selectList(QueryIpAddressManageParam qParam, Integer level) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<IpAddressManageTableParam> list = new ArrayList<>();
        //分组
        List<IpAddressManageTableParam> ipAddressManageTableParams = new ArrayList<>();
        //地址段
        List<IpAddressManageTableParam> groupupBy = new ArrayList<>();
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
        DataPermission dataPermission = DataPermission.valueOf(perm);
//        int parenId = qParam.getParentId();
//
//        qParam.setParentId(ipAddressManagePowerTableDao.selectIpAddressManageTableParam(qParam.getParentId()).getParentId());


        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            qParam.setGroupIds(groupIds);
        }

        list = selectIpGrop(qParam, dataPermission, userId, loginName);
        //去空
        dealNull(list);
        for (IpAddressManageTableParam var : list) {
            var.setIPv4(true);
            if (IP_GROUPING.equals(var.getType())) {
                int i = mwIpAddressManageTableDao.checkIsLeaf(var.getId());
                if (i == 0) {
                    var.setLeaf(true);
                }

                ipAddressManageTableParams.add(var);
//                List<IpAddressManageTableParam> groupupByid = new ArrayList<>();
//                qParam.setParentId(var.getId());
//                groupupByid = selectIpGrop(qParam, dataPermission, userId, loginName);
//                if (groupupByid != null && groupupByid.size() > 0) {
//                    groupupBy.addAll(groupupByid);
//                }
            } else {
                groupupBy.add(var);
            }


        }
        Map<String, Object> map = new HashMap<>();
        map.put(IP_GROUP, ipAddressManageTableParams);
        map.put(IP_ADDRESS, groupupBy);
        return Reply.ok(map);
    }

    @Override
    public Reply  selectListSeniorParam(IsInput param) throws ParseException {
        List<RequestIpAddressDistributtionNewParam> requestIpAddressDistributtionNewParams = new ArrayList<>();
        for (Integer Ipid:param.getIds()) {
            param.setId(Ipid);
            List<Integer> integers = new ArrayList<>();
            integers.add(param.getId());
            String iptype =IPV4_JUDGE ;
            if (param.isIdType()) {
                iptype =IPV6_JUDGE;
            }
            Integer parendId = 0;
            if (iptype ==IPV4_JUDGE ) {
                parendId = ipAddressManagePowerTableDao.selectIpParent(param.getId());
                parendId = ipAddressManagePowerTableDao.selectIpAddressManageTableParam(parendId).getParentId();
            } else {
                parendId = ipAddressManagePowerTableDao.selectIpvsixParent(param.getId());
                parendId = ipAddressManagePowerTableDao.selectIpv6AddressManageTableParam(parendId).getParentId();
            }

            Integer countIp = ipAddressManagePowerTableDao.selectCountDistribution(integers, iptype, 0);
            if (countIp > 0) {
                return Reply.fail(UN_DISTRI_IP);
            } else {


                for (Integer s : integers) {
                    List<RequestIpAddressDistributtionNewParamList> list = ipAddressManagePowerTableDao.selectIPDristi(s,iptype);
                    for (RequestIpAddressDistributtionNewParamList requestIpAddressDistributtionNewParamList:list) {
                        param.setBangDistri(requestIpAddressDistributtionNewParamList.getBangDistri());
                        Map<String, Integer> primary = ipAddressManagePowerTableDao.selectPrimary(s, iptype,param.getBangDistri());
                        RequestIpAddressDistributtionNewParam requestIpAddressDistributtionNewParam = new RequestIpAddressDistributtionNewParam();
                        if (primary == null) {
                            return Reply.fail(UNFIND_USEFUL_IP);
                        }
                        iptype =IPV6_JUDGE;
                        if (primary.get(PRIMARY_IP_TYPE) == 0) {
                            iptype =IPV4_JUDGE ;
                        }
                        List<Integer> test = new ArrayList<>();
                        test.add(primary.get(PRIMARY_IP));
                        List<IpAddressManageTableParam> tree1 = selectAllTree(test, iptype);
                        List<String> tree = new ArrayList<>();
                        List<Integer> treeId = new ArrayList<>();
                        for (IpAddressManageTableParam node:tree1) {
                            tree.add(node.getLabel());
                            treeId.add(node.getId());
                        }
                        Collections.reverse(tree);
                        Collections.reverse(treeId);
                        List<String> upTree = new ArrayList<>();
                        List<String> downTree = new ArrayList<>();
                        List<Integer> upTreeId = new ArrayList<>();
                        List<Integer> downTreeId = new ArrayList<>();
                        Integer length = tree.size();

                        for (int i = 0; i < tree.size() - 1; i++) {
                            if (i >= length - 2 && length - 2 > 0) {
                                downTree.add(tree.get(i));
                                downTreeId.add(treeId.get(i));
                            } else {
                                upTree.add(tree.get(i));
                                upTreeId.add(treeId.get(i));
                            }
                        }
                        ResponIpDistributtionParam responIpDistributtionParam = ipAddressManagePowerTableDao.selectResponIpDistributtionParam(primary.get(PRIMARY_IP), iptype);
                        if (responIpDistributtionParam == null) {
                            responIpDistributtionParam = new ResponIpDistributtionParam();
                            responIpDistributtionParam.setId(s);
                            responIpDistributtionParam.setIdType(primary.get(PRIMARY_IP_TYPE) == 0);
                        }
                        responIpDistributtionParam.setIdType(param.isIdType());
                        List<Check> maps = new ArrayList<>();

                        maps.addAll(ipAddressManagePowerTableDao.selectCheck(responIpDistributtionParam.getId(), iptype));
                        responIpDistributtionParam.putradom(maps);

                        List<Label> labels = ipAddressManagePowerTableDao.selectLabel(s, Level_BASCIS, iptype ==IPV4_JUDGE  ? DataType.IP.getName() : DataType.IPV6.getName());
                        List<Label> labelList = new ArrayList<>();
                        for (Label label : labels) {
                            label.setLabelIpId(integers.get(0));
                            label.setLabelIpType(param.isIdType() == false ? 0 : 1);
                            if (label.getInputFormat().equals(INPUT_TYPE_TWO)) {
                                SimpleDateFormat df = new SimpleDateFormat(TIME_STATUS);
                                label.setDateTagboard(df.parse(label.getTestValue()));
                            } else if (label.getInputFormat().equals(INPUT_TYPE_THERE)) {
                                label.setDropTagboard(label.getLabelDropId());
                            } else if (label.getInputFormat().equals(INPUT_TYPE_ONE)) {
                                label.setTagboard(label.getTestValue());
                            }
                            List<LabelCheck> checks = ipAddressManagePowerTableDao.browDrop(label.getLabelDrop());
                            label.setLabelChecks(checks);
                            labelList.add(label);
                        }

                        /*获取基础信息*/
                        RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam = salectSenr(param.getId(), param.isIdType(), primary, iptype,param.getBangDistri());
                        /*获取树状节点*/
                        List<ResponIpDistributtionNewParentParam> responIpDistributtionNewParentParams = new ArrayList<>();
                        /*首届点树*/
                        ResponIpDistributtionNewParentParam responIpDistributtionNewParentParam = new ResponIpDistributtionNewParentParam();
                        responIpDistributtionNewParentParam.setAttrParam(labelList);
                        /*装载树形结构*/
                        List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = new ArrayList<>();
                        /*装载首届点*/
                        ResponIpDistributtionNewParam responIpDistributtionNewParam = new ResponIpDistributtionNewParam();
                        responIpDistributtionNewParam.setId(-1);
                        responIpDistributtionNewParam.setIsfz(0);
                        responIpDistributtionNewParam.setRedomId(UUIDUtils.getUUID());
                        if (downTree != null && downTree.size() > 0) {
                            responIpDistributtionNewParam.setLabel(downTree.size() > 0 ? downTree.get(0) : upTree.get(0));
                        }
                        List<ResponIpDistributtionNewParam> responIpDistributtionNewParamList = createChildren(primary, parendId, null, param);
                        if (responIpDistributtionNewParamList != null && responIpDistributtionNewParamList.size() > 0) {
                            responIpDistributtionNewParam.setChildren(responIpDistributtionNewParamList);
                        }
                        responIpDistributtionNewParams.add(responIpDistributtionNewParam);
                        Integer num = calucateNum(responIpDistributtionNewParam,0);
                        /*便利得下层节点*/
                        /*装载树形节点*/
                        if (param.getGaiId()!=null){
                            responIpDistributtionNewParentParam.setGaiId(param.getGaiId());
                        }

                        responIpDistributtionNewParentParam.setTreeData(responIpDistributtionNewParams);
                        responIpDistributtionNewParentParam.setUpTree(upTree);
                        responIpDistributtionNewParentParam.setUpTreeIds(upTreeId);
                        responIpDistributtionNewParentParam.setDownTreeIds(downTreeId);
                        responIpDistributtionNewParentParam.setDownTree(downTree);
                        responIpDistributtionNewParentParam.setNum(num);
                        responIpDistributtionNewParentParams.add(responIpDistributtionNewParentParam);
                        requestIpAddressDistributtionNewParam.setBangDistri(param.getBangDistri());
                        /*装载总结构*/
                        requestIpAddressDistributtionNewParam.setRequestIpAddressDistributtionSeniorParam(requestIpAddressDistributtionSeniorParam);
                        requestIpAddressDistributtionNewParam.setResponIpDistributtionParams(responIpDistributtionNewParentParams);
                        requestIpAddressDistributtionNewParam.setParentId(downTreeId.size()>0?downTreeId.get(0):0);
                        requestIpAddressDistributtionNewParams.add(requestIpAddressDistributtionNewParam);
                    }
                }
                Map<Integer,RequestIpAddressDistributtionNewParam> map = new HashMap<>();
                for (RequestIpAddressDistributtionNewParam requestIpAddressDistributtionNewParam:requestIpAddressDistributtionNewParams) {
                    if (map.get(requestIpAddressDistributtionNewParam.getParentId())==null){
                            map.put(requestIpAddressDistributtionNewParam.getParentId(),requestIpAddressDistributtionNewParam);
                    }else {
                        RequestIpAddressDistributtionNewParam r = map.get(requestIpAddressDistributtionNewParam.getParentId());
                        List<ResponIpDistributtionNewParam> rlist = r.getResponIpDistributtionParams().get(0).getTreeData().get(0).getChildren();
                        rlist.addAll(requestIpAddressDistributtionNewParam.getResponIpDistributtionParams().get(0).getTreeData().get(0).getChildren());
                        r.getResponIpDistributtionParams().get(0).getTreeData().get(0).setChildren(rlist);
                        map.put(requestIpAddressDistributtionNewParam.getParentId(),r);
                    }
                }
                requestIpAddressDistributtionNewParams.clear();
                for (Integer s:map.keySet()){
                    requestIpAddressDistributtionNewParams.add(map.get(s));
                }

            }
        }


        return Reply.ok(requestIpAddressDistributtionNewParams);
    }

    private Integer calucateNum(ResponIpDistributtionNewParam responIpDistributtionNewParam,Integer i) {
        if (responIpDistributtionNewParam.getChildren().size()>0){
            i=i+responIpDistributtionNewParam.getChildren().size();
            for (ResponIpDistributtionNewParam responIpDistributtionNewParamSign:responIpDistributtionNewParam.getChildren()) {
                i=calucateNum(responIpDistributtionNewParamSign,i);
            }
        }
        else {
            return i;
        }
        return i;
    }



    /*public Reply selectListNewSeniorParam(IsInput param) throws ParseException {
        List<RequestIpAddressDistributtionNewParam> requestIpAddressDistributtionNewParams = new ArrayList<>();
        RequestIpAddressDistributtionNewParam requestIpAddressDistributtionNewParam = new RequestIpAddressDistributtionNewParam();
        List<Integer> integers = new ArrayList<>();
        integers.add(param.getId());
        String iptype =IPV4_JUDGE ;
        if (param.isIdType()) {
            iptype =IPV6_JUDGE;
        }
        Integer parendId = 0;
        if (iptype ==IPV4_JUDGE ) {
            parendId = ipAddressManagePowerTableDao.selectIpParent(param.getId());
            parendId = ipAddressManagePowerTableDao.selectIpAddressManageTableParam(parendId).getParentId();
        } else {
            parendId = ipAddressManagePowerTableDao.selectIpvsixParent(param.getId());
            parendId = ipAddressManagePowerTableDao.selectIpv6AddressManageTableParam(parendId).getParentId();
        }

        Integer countIp = ipAddressManagePowerTableDao.selectCountDistribution(integers, iptype, 0);
        if (countIp > 0) {
            return Reply.fail("存在IP地址未分配");
        } else {

            for (Integer s : integers) {
                Map<String, Integer> primary = ipAddressManagePowerTableDao.selectPrimary(s, iptype);
                if (primary == null) {
                    return Reply.fail("不存在分配地址ip");
                }
                iptype =IPV6_JUDGE;
                if (primary.get(PRIMARY_IP_TYPE) == 0) {
                    iptype =IPV4_JUDGE ;
                }
                List<Integer> test = new ArrayList<>();
                test.add(primary.get(PRIMARY_IP));
                List<String> tree = selectAllTree(test, iptype);
                Collections.reverse(tree);

                List<String> upTree = new ArrayList<>();
                List<String> downTree = new ArrayList<>();
                Integer length = tree.size();

                for (int i = 0; i < tree.size() - 1; i++) {
                    if (i >= length - 2 && length - 2 > 0) {
                        downTree.add(tree.get(i));
                    } else {
                        upTree.add(tree.get(i));
                    }
                }
                ResponIpDistributtionParam responIpDistributtionParam = ipAddressManagePowerTableDao.selectResponIpDistributtionParam(primary.get(PRIMARY_IP), iptype);
                if (responIpDistributtionParam == null) {
                    responIpDistributtionParam = new ResponIpDistributtionParam();
                    responIpDistributtionParam.setId(s);
                    responIpDistributtionParam.setIdType(primary.get(PRIMARY_IP_TYPE) == 0);
                }
                responIpDistributtionParam.setIdType(param.isIdType());
                List<Check> maps = new ArrayList<>();

                maps.addAll(ipAddressManagePowerTableDao.selectCheck(responIpDistributtionParam.getId(), iptype));
                responIpDistributtionParam.putradom(maps);

                List<Label> labels = ipAddressManagePowerTableDao.selectLabel(s, 6, iptype ==IPV4_JUDGE  ? DataType.IP.getName() : DataType.IPV6.getName());
                List<Label> labelList = new ArrayList<>();
                for (Label label : labels) {
                    label.setLabelIpId(integers.get(0));
                    label.setLabelIpType(param.isIdType() == false ? 0 : 1);
                    if (label.getInputFormat().equals(INPUT_TYPE_TWO)) {
                        SimpleDateFormat df = new SimpleDateFormat(TIME_STATUS);
                        label.setDateTagboard(df.parse(label.getTestValue()));
                    } else if (label.getInputFormat().equals(INPUT_TYPE_THERE)) {
                        label.setDropTagboard(label.getLabelDropId());
                    } else if (label.getInputFormat().equals(INPUT_TYPE_ONE)) {
                        label.setTagboard(label.getTestValue());
                    }
                    List<LabelCheck> checks = ipAddressManagePowerTableDao.browDrop(label.getLabelDrop());
                    label.setLabelChecks(checks);
                    labelList.add(label);
                }

                *//*获取基础信息*//*
                RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam = salectSenr(param.getId(), param.isIdType(), primary, iptype,param.getBangDistri());
                *//*获取树状节点*//*
                List<ResponIpDistributtionNewParentParam> responIpDistributtionNewParentParams = new ArrayList<>();
                *//*首届点树*//*
                ResponIpDistributtionNewParentParam responIpDistributtionNewParentParam = new ResponIpDistributtionNewParentParam();
                responIpDistributtionNewParentParam.setAttrParam(labelList);
                *//*装载树形结构*//*
                List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = new ArrayList<>();
                *//*装载首届点*//*
                ResponIpDistributtionNewParam responIpDistributtionNewParam = new ResponIpDistributtionNewParam();
                responIpDistributtionNewParam.setId(-1);
                responIpDistributtionNewParam.setIsfz(0);
                if (downTree != null && downTree.size() > 0) {
                    responIpDistributtionNewParam.setLabel(downTree.size() > 0 ? downTree.get(0) : upTree.get(0));
                }
                List<ResponIpDistributtionNewParam> responIpDistributtionNewParamList = createChildren(primary, parendId, null, param);
                if (responIpDistributtionNewParamList != null && responIpDistributtionNewParamList.size() > 0) {
                    responIpDistributtionNewParam.setChildren(responIpDistributtionNewParamList);
                }
                responIpDistributtionNewParams.add(responIpDistributtionNewParam);

                *//*便利得下层节点*//*
                *//*装载树形节点*//*
                responIpDistributtionNewParentParam.setTreeData(responIpDistributtionNewParams);
                responIpDistributtionNewParentParam.setUpTree(upTree);
                responIpDistributtionNewParentParam.setDownTree(downTree);
                responIpDistributtionNewParentParams.add(responIpDistributtionNewParentParam);
                *//*装载总结构*//*
                requestIpAddressDistributtionNewParam.setRequestIpAddressDistributtionSeniorParam(requestIpAddressDistributtionSeniorParam);
                requestIpAddressDistributtionNewParam.setResponIpDistributtionParams(responIpDistributtionNewParentParams);

            }

        }
        return Reply.ok(requestIpAddressDistributtionNewParam);
    }*/

    @Transactional
    @Override
    public Reply changeOrRe(RequestIpAddressReciveParam param) throws ParseException {


        List<Map<String, Object>> maps = new ArrayList<>();
        Map<String, IpamProcessHistoryDTO> ipamProcessHistoryDTOMap = new HashMap<>();
        List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = param.getIpAndAddressList();
        String operNum = UUIDUtils.getUUID();
        String bangStr = "";
        for (ResponIpDistributtionNewParam d : responIpDistributtionNewParams) {
            Boolean clean = false;
            IpamProcessHistoryDTO ipamProcessHistoryDTO = new IpamProcessHistoryDTO();
            for (ResponIpDistributtionNewParam f:d.getIpAndAddressList()) {
                if (f.getIdType() != null) {
                    List<Map<String, Object>> cleans = new ArrayList<>();
                    clean = true;
                    bangStr= f.getBangDistri();
                    ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicant(f.getId(), f.getIdType() == false ? IPV4_JUDGE:IPV6_JUDGE,f.getBangDistri());
                    ipamProcessHistoryDTOMap.put(bangStr,ipamProcessHistoryDTO);
                    d.setBangDistri(f.getBangDistri());
                    Map<String, Object> map = new HashMap<>();
                    map.put(IP_ID, f.getId());
                    map.put(IP_TYPE_ID, f.getIdType());
                    maps.add(map);
                    cleans.add(map);
                    IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                    ipamOperHistoryDTO.setCreateDate(new Date()).setType(1).setCreator(iLoginCacheInfo.getLoginName()).setIpType(f.getIdType()).setRlistId(f.getId()).setApplicant(ipamProcessHistoryDTO.getId()).setDesc(null).setBangDistri(f.getBangDistri());
                    ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);
                    deelNewDistributtionRecord(cleans,f.getBangDistri());
                 }

            }
            if (clean==true){
                List<ResponIpDistributtionNewParam> treeData = d.getTreeData();
                List<String> ipaddress = new ArrayList<>();
                List<List<Map<String, Object>>> mapList  = handleTree(treeData,maps,ipaddress);
                List<Label> labels = ipAddressManagePowerTableDao.selectLabel(ipamProcessHistoryDTO.getId(), Level_SENIOR, DataType.IPHIS.getName());
                Map<String,String> newLabel = new HashMap<>();
                for (String key:param.getRequestIpAddressDistributtionSeniorParam().keySet()) {
                    for (Label label:labels) {
                        if (label.getLabelDrop().equals(key)){
                            newLabel.put(label.getLabelName(),param.getRequestIpAddressDistributtionSeniorParam().get(key).toString());
                        }
                    }

                }
                Map<String,String> oldLabel = new HashMap<>();
                for (Label label:labels) {
                    oldLabel.put(label.getLabelName(),label.getDropValue());

                }
                ipAddressManagePowerTableDao.insertOperHis(JSONObject.toJSONString(mapList.get(1)),JSONObject.toJSONString(mapList.get(0)),JSONObject.toJSONString(newLabel),JSONObject.toJSONString(oldLabel),operNum,new Date(),2,ipamProcessHistoryDTO.getApplicantDate(),null,0,JSONObject.toJSONString(mapList.get(2)),JSONObject.toJSONString(mapList.get(2)),ipaddress.toString(),iLoginCacheInfo.getLoginName(),bangStr);

            }
        }



        return Reply.ok(SUCCESSFUL);
    }

    //第一个是原ip地址，第二是先ip地址，第三个描述及信息 第四个是变动的IP地址
    private List<List<Map<String, Object>>> handleTree(List<ResponIpDistributtionNewParam> treeData, List<Map<String, Object>> maps,List<String> ipaddress) {
        List<List<Map<String, Object>>> mas = new ArrayList<>();
        List<Map<String, Object>> ipDiscription = new ArrayList<>();
        List<Map<String, Object>> mapList = getChildrenMap(treeData.get(0).getChildren(),0,false,0,false,ipDiscription,ipaddress);
        List<Map<String, Object>> mapnewList = new ArrayList<>();

        for (Map<String, Object> map:mapnewList) {
            boolean addTrue = true;
            for (Map<String, Object> map1:maps) {
                if (map1.get(IP_ID).equals(map.get(IP_ID))&&map1.get(IP_TYPE_ID).equals(map.get(IP_TYPE_ID))){
                    addTrue = false;
                }
            }
            if (addTrue){
                mapnewList.add(map);
            }
        }
        mas.add(mapList);
        mas.add(mapnewList);
        mas.add(ipDiscription);
        return  mas;
    }

    private  List<Map<String, Object>> getChildrenMap(List<ResponIpDistributtionNewParam> treeData, Integer sourceId, Boolean sourceIdType, Integer parenId, Boolean parenIdType, List<Map<String, Object>> ipDiscription,List<String> ipaddress) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (ResponIpDistributtionNewParam responIpDistributtionNewParam:treeData) {
            sourceId= 0;
            Map<String, Object> ipDiscriptionmMap = new HashMap<>();
            if (sourceId==0){
                sourceId = responIpDistributtionNewParam.getId();
                sourceIdType = responIpDistributtionNewParam.getIdType();
                parenId = responIpDistributtionNewParam.getId();
                parenIdType = responIpDistributtionNewParam.getIdType();
            }
            Map<String, Object> map = new HashMap<>();
            if (responIpDistributtionNewParam.getChildren().size()>0){
                maps.addAll(getChildrenMap(responIpDistributtionNewParam.getChildren(),sourceId,sourceIdType,responIpDistributtionNewParam.getId(),responIpDistributtionNewParam.getIdType(), ipDiscription,ipaddress));
            }
            ipaddress.add(responIpDistributtionNewParam.getKeyTestValue());
            map.put(SOURCR_ID,sourceId);
            map.put(SOURCE_IP_TYPE,sourceIdType);
            map.put(PARENT_ID,parenId);
            map.put(PARENT_IP_TYPE,parenIdType);
            map.put(IP_ID,responIpDistributtionNewParam.getId());
            map.put(IP_TYPE_ID,responIpDistributtionNewParam.getIdType());
            ipDiscriptionmMap.put(BAND_IP_ID,responIpDistributtionNewParam.getId());
            ipDiscriptionmMap.put(BAND_IP_TYPE,responIpDistributtionNewParam.getIdType());
            ipDiscriptionmMap.put(DESCRIPTION,responIpDistributtionNewParam.getDesc());
            ipDiscription.add(ipDiscriptionmMap);
            maps.add(map);
        }
            return  maps;
    }

    @Override
    @Transactional
    public Reply changeParam(RequestIpAddressDistributtionNewParam param, String operNum) throws ParseException {
        if (param.getSignId()==null){
            param.setSignId(1);
        }
        param.getIsInput().setBangDistri(param.getBangDistri());
        Reply reply = selectListSeniorParam(param.getIsInput());
        IpamProcessHistoryDTO ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicant(param.getIsInput().getId(), param.getIsInput().isIdType() == false ? IPV4_JUDGE:IPV6_JUDGE,param.getBangDistri());
        List<Label> labels = ipAddressManagePowerTableDao.selectLabel(ipamProcessHistoryDTO.getId(), Level_SENIOR, DataType.IPHIS.getName());
        List<String> IpAddress = new ArrayList<>();
        if (reply.getRes().equals(PaasConstant.RES_SUCCESS)) {
//            原来关系对应
            List<Map<String, Object>> mapList = cleanDistributionIP(reply,param.getBangDistri());
//           新关系对应
            List<Map<String, Object>> maps = new ArrayList<>();
            List<Map<String, Object>> descMap = new ArrayList<>();

            List<ResponIpDistributtionNewParentParam> responIpDistributtionNewParentParams = param.getResponIpDistributtionParams();
            for (ResponIpDistributtionNewParentParam responIpDistributtionNewParentParam : responIpDistributtionNewParentParams) {
                List<ResponIpDistributtionNewParam> responIpDistributtionNewParentParamList = responIpDistributtionNewParentParam.getTreeData();
                for (ResponIpDistributtionNewParam responIpDistributtionNewParam : responIpDistributtionNewParentParamList) {
                    List<Map<String, Object>> map = OrganizeIpData(IpAddress, descMap, responIpDistributtionNewParam.getChildren(), -1, -1, false, false, 0);
                    maps.addAll(map);
                }
            }
            List<Map<String, Object>> addMap = getCleanMap(maps, mapList);
            List<Map<String, Object>> cleanMap = getCleanMap(mapList, maps);
            //回收不用的
            for (Map<String, Object> oldMap : cleanMap) {
                IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                ipamOperHistoryDTO.setCreateDate(new Date()).setType(1).setCreator(iLoginCacheInfo.getLoginName()).setIpType((Boolean) oldMap.get(IP_TYPE_ID)).setRlistId((Integer) oldMap.get(IP_ID)).setApplicant(ipamProcessHistoryDTO.getId()).setBangDistri(param.getBangDistri());
                ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);
            }


            //获取生成分配操作后的所有key（id）
            createNewDistributtionRecord(maps, param.getRequestIpAddressDistributtionSeniorParam(),param.getBangDistri());


            for (ResponIpDistributtionNewParentParam responIpDistributtionNewParentParam : responIpDistributtionNewParentParams) {
                List<ResponIpDistributtionNewParam> responIpDistributtionNewParentParamList = responIpDistributtionNewParentParam.getTreeData();
                for (ResponIpDistributtionNewParam responIpDistributtionNewParam : responIpDistributtionNewParentParamList) {
                    List<String> ipaddress = new ArrayList<>();
                    /*描述信息整合*/
                    Map<String, Object> objectMap = new HashMap<>();
                    for (Map<String, Object> map : descMap) {
                        String key = "";
                        if ((boolean) map.get(BAND_IP_TYPE)) {
                            key = DISTINGUISH_IPV6;
                        } else {
                            key = DISTINGUISH_IPV4;
                        }
                        key = key + map.get(BAND_IP_ID);
                        objectMap.put(key, map.get(DESCRIPTION));
                    }
                    OrganizeIpData(ipaddress, descMap, responIpDistributtionNewParam.getChildren(), -1, -1, false, false, 0);
                    updateHis(ipaddress, responIpDistributtionNewParentParam.getAttrData(), addMap, ipamProcessHistoryDTO, param.getRequestIpAddressDistributtionSeniorParam(), objectMap,param.getBangDistri(),param.getSignId());
                    Map<String,String> newLabel = new HashMap<>();
                    for (MwAssetsLabelDTO label:param.getRequestIpAddressDistributtionSeniorParam().getAttrData()) {
                        newLabel.put(label.getLabelName(),label.getDropValue());
                    }
                    Map<String,String> oldLabel = new HashMap<>();
                    for (Label label:labels) {
                        oldLabel.put(label.getLabelName(),label.getDropValue());

                    }
                    ipAddressManagePowerTableDao.insertOperHis(JSONObject.toJSONString(mapList),JSONObject.toJSONString(maps),JSONObject.toJSONString(newLabel),JSONObject.toJSONString(oldLabel),operNum,new Date(),3,param.getRequestIpAddressDistributtionSeniorParam().getApplicantionDate(),ipamProcessHistoryDTO.getApplicantDate(),0,JSONObject.toJSONString(descMap),null,ipaddress.toString(),iLoginCacheInfo.getLoginName(),param.getBangDistri());
                    createdIPScanHIS(maps, ipaddress,param.getSignId());
                }

         }

            return Reply.ok(SUCCESSFUL);
        } else {
            return Reply.fail(UNFIND_USEFUL_IP);
        }
    }

    @Override
    public Reply getAllIpGroupManage(IsInput isInput) {
        Map<String, Object> maps = new HashMap<>();
        if (isInput.getId() != 0) {
            Integer parentId = 0;
            if (!isInput.isIdType()) {
                parentId = ipAddressManagePowerTableDao.selectIpParent(isInput.getId());
            } else {
                parentId = ipAddressManagePowerTableDao.selectIpvsixParent(isInput.getId());
                parentId = ipAddressManagePowerTableDao.selectIpv6AddressManageTableParam(parentId).getParentId();
            }
            IpAddressManageTableParam ipAddressManageTableParam = ipAddressManagePowerTableDao.selectIpAddressManageTableParam(parentId);
            maps.put(MAP_VALUE, ipAddressManageTableParam.getParentId());
        } else {
            maps.put(MAP_VALUE, 0);
        }

        List<Map<String, Object>> mapList = ipAddressManagePowerTableDao.selectAllManage(IP_GROUPING);
        List<TreeStructure> treeStructures = sortTree(mapList, isInput.getParentId());


        maps.put(TREE_STRUCTURES, treeStructures);
        return Reply.ok(maps);
    }

    @Override
    public XSSFWorkbook excelImport(MultipartFile file) {
        try {
            //组分配数据
            //总数据
            RequestIpAddressDistributtionNewParam requestIpAddressDistributtionNewParam = new RequestIpAddressDistributtionNewParam();

            //加载导入人信息
            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            MwUserDTO user = mwuserDao.selectById(userId);
            List<List<Integer>> orgNodes = new ArrayList<>();
            if (user.getDepartment() != null) {
                user.getDepartment().forEach(department -> {
                            List<Integer> orgIds = new ArrayList<>();
                            List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                            nodes.forEach(node -> {
                                if (!"".equals(node))
                                    orgIds.add(Integer.valueOf(node));
                            });
                            orgNodes.add(orgIds);
                        }
                );
            }
            List<String> chekIp = new ArrayList<>();
            byte[] byteArr = file.getBytes();
            InputStream inputStream = new ByteArrayInputStream(byteArr);

            //获取工作簿
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            //加载
            List<Map<String, Object>> maps = ExeclchangeMap(workbook, chekIp);
            int line = 0;
            List<String> unAbleIpAddress = new ArrayList<>();
            List<Integer> integers = new ArrayList<>();
            for (Map<String, Object> map : maps) {
                unAbleIpAddress.add(map.get(IP_LIST).toString());
                line++;

                //高级选项
                RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam = new RequestIpAddressDistributtionSeniorParam();
                //大标签
                List<ResponIpDistributtionNewParentParam> responIpDistributtionParams = new ArrayList<>();
                //组高级属性
                requestIpAddressDistributtionSeniorParam.setApplicant(userId);
                requestIpAddressDistributtionSeniorParam.setOrgIds(orgNodes);
                requestIpAddressDistributtionSeniorParam.setApplicantionDate(new Date());
                List<MwAssetsLabelDTO> labelList = new ArrayList<>();
                labelList.addAll(findLabel(Level_SENIOR, APPLICANT_NAME, map, APPLICANT_PROCES));
                labelList.addAll(findLabel(Level_SENIOR, APPLICANTOR, map, APPLICANT));

                requestIpAddressDistributtionSeniorParam.setAttrData(labelList);

                //组装下层结构
                ResponIpDistributtionNewParentParam responIpDistributtionNewParentParam = new ResponIpDistributtionNewParentParam();
                List<MwAssetsLabelDTO> labels = new ArrayList<>();
                List<MwAssetsLabelDTO> labelDTOS = new ArrayList<>();
                labelDTOS.addAll(findLabel(Level_BASCIS, DESCRIPTION_TEXT, map, RADIO_BUTTON));
                //组装首届点
                responIpDistributtionNewParentParam.setAttrData(labelDTOS);

                //设置首届点
                List<ResponIpDistributtionNewParam> treeData = new ArrayList<>();
                ResponIpDistributtionNewParam responIpDistributtionNewParam = new ResponIpDistributtionNewParam();
                responIpDistributtionNewParam.setId(666);
                responIpDistributtionNewParam.setIdType(false);
                responIpDistributtionNewParam.setChildDrop(IPV4_JUDGE_CATIPAL);
                responIpDistributtionNewParam.setIsDesc(false);
                responIpDistributtionNewParam.setLabel(INTRANT);
                //装载下层关系
                responIpDistributtionNewParam.setChildren(hanleMap(map));
                if (responIpDistributtionNewParam.getChildren() == null || responIpDistributtionNewParam.getChildren().size() == 0) {
//                    mwMessageService.createMessage("导入数据第" + line + "有问题,存在已分配或者数据结构不对", 1, 0);
                } else {
                    treeData.add(responIpDistributtionNewParam);
                    responIpDistributtionNewParentParam.setTreeData(treeData);
                    responIpDistributtionParams.add(responIpDistributtionNewParentParam);
                    requestIpAddressDistributtionNewParam.setResponIpDistributtionParams(responIpDistributtionParams);
                    requestIpAddressDistributtionNewParam.setRequestIpAddressDistributtionSeniorParam(requestIpAddressDistributtionSeniorParam);
                    try {
                        createNewDistributtion(requestIpAddressDistributtionNewParam, UUIDUtils.getUUID());
                        unAbleIpAddress.remove(map.get(IP_LIST).toString());
                    } catch (Exception e) {
                        integers.add(line);
                    }
                }
            }
            if (unAbleIpAddress.size() > 0) {
                log.error(UN_SUCCESS_FUL + unAbleIpAddress.toString());
            }
            if (integers.size() > 0) {
                mwMessageService.createMessage(FALIE_FUL+ integers.toString(), 1, 0, null);
            }

            try {
                deleteOldLabel();
            } catch (Exception e) {

            }

            try {
                List<String> ip = new ArrayList<>();
                for (String s : chekIp) {
                    List<String> ls = splotIpaddress(s);
                    if (ls.size() > 0) {
                        ip.addAll(ls);
                    }
                }
                List<Map<String, Object>> ipcheck = ipAddressManagePowerTableDao.checkIP(ip);
                String st = "";
                Map<String, Integer> integerMap = new HashMap<>();
                for (Map<String, Object> s : ipcheck) {
                    st = st + "\n" + s.get(IP_ADDRESS_MAP).toString();
                    integerMap.put(s.get(IP_ADDRESS_MAP).toString(), (Integer) s.get(DISTRIBUTION_STATUS));
                }
                XSSFSheet hs = workbook.getSheetAt(0);
                //获取Sheet的第一个行号和最后一个行号
                int last = hs.getLastRowNum();
                int first = hs.getFirstRowNum();

                for (int i = first + 1; i < last; i++) {
                    String ipMap = hs.getRow(i).getCell(2).getStringCellValue();
                    List<String> strings = new ArrayList<>();
                    List<String> ls = splotIpaddress(ipMap);
                    if (ls.size() > 0) {
                        strings.addAll(ls);
                    }
                    String sb = "";
                    for (String k : strings) {
                        try {
                            sb = sb + k + ":" + integerMap.get(k).toString() + ",";
                        } catch (Exception e) {
                            sb = sb + k + ":-2,";
                        }
                    }
                    hs.getRow(i).createCell(6).setCellValue(sb);
                }

                log.info(st);
            } catch (Exception e) {

            }


            return workbook;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error(FALIE_LOG_FUL,e);

        }


        return null;
    }

    @Override
    public List<String> ipImport(MultipartFile file, Integer id) throws IOException {
        byte[] byteArr = file.getBytes();
        List<String> execl = new ArrayList<>();
        InputStream inputStream = new ByteArrayInputStream(byteArr);
        //获取工作簿
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        //加载
        List<Map<String, Object>> maps = ExeclIP(workbook);
        QueryIpv6ManageParam qParam = new QueryIpv6ManageParam();
        qParam.setId(id);
        //获取地址段分组
        Ipv6ManageTable1Param s = mwIpv6ManageTableDao.selectIpv6ById(qParam);
        if (s.getSignId()==null){
            s.setSignId(1);
        }
        BigInteger Min = new BigInteger(s.getIpRandStart());
        BigInteger Max = new BigInteger(s.getIpRandEnd());
        for (Map<String, Object> map : maps) {
            String ip = map.get(IP_SIGN).toString();
            if (ip != null && !ip.trim().equals("")) {
                try {
                    BigInteger iprand = IPv6Judge.getInstance().Ipv6IptoBigInteger(ip);
                    String getip = IPv6Judge.getInstance().getFullIPv6(ip);
                    if (iprand.compareTo(Min) == 1 && iprand.compareTo(Max) == -1) {
                        if (ipAddressManagePowerTableDao.selectIpv6(getip,s.getSignId()) > 0) {
                            execl.add(ip);
                        } else {
                            ipAddressManagePowerTableDao.insterIpv6list(s.getId(), ip, iLoginCacheInfo.getLoginName(),s.getSignId());
                        }
                    }
                } catch (Exception e) {
                    execl.add(ip);
                }

            }
        }
        return execl;
    }

    @Override
    public Reply selectIPDristi(IsInput param) {
        String iptype =IPV4_JUDGE;
        if (param.isIdType()) {
            iptype =IPV6_JUDGE;
        }
        if(param.getId()==null){
            return Reply.ok();
        }

        List<RequestIpAddressDistributtionNewParamList> list = ipAddressManagePowerTableDao.selectIPDristi(param.getId(),iptype);
        for (RequestIpAddressDistributtionNewParamList requestIpAddressDistributtionNewParamList:list) {
            requestIpAddressDistributtionNewParamList.setPrimaryIdress(ipAddressManagePowerTableDao.selectIPaddressById(requestIpAddressDistributtionNewParamList.getPrimaryIp(),requestIpAddressDistributtionNewParamList.getPrimaryType()));
        }
        return Reply.ok(list);
    }

    @Override
    public Reply checkBrow(IsInput param) {
        Map<String, Object> maps = new HashMap<>();

        List<Map<String, Object>> mapList = ipAddressManagePowerTableDao.selectAllManage(IP_GROUPING);
        List<TreeStructure> treeStructures = sortTree(mapList, param.getParentId());


        maps.put(TREE_STRUCTURES, treeStructures);
        return Reply.ok(maps);
    }


    @Override
    public Reply selectListComprehensive(Map<String,Object> param) {
        Map<String,Object> srach =  new HashMap<>();
        String distributtioner = param.get(DISTRIBUTTIONER)==null?null:  param.get(DISTRIBUTTIONER).toString();
        String start = param.get(APPLICANT_DATE_START)==null?null:param.get(APPLICANT_DATE_START).toString();
        String end = param.get(APPLICANT_DATE_END)==null?null:param.get(APPLICANT_DATE_END).toString();
        String operStart = param.get(OPER_TIME_START)==null?null:param.get(OPER_TIME_START).toString();
        String operEnd = param.get(OPER_TIME_END)==null?null:param.get(OPER_TIME_END).toString();
        String ipAddresses = param.get(IP_ADDRESSES)==null?null:  param.get(IP_ADDRESSES).toString();
        String operInt = param.get(OPER_INT)==null?null:  param.get(OPER_INT).toString();
        String applicator = param.get(APPLICANTOR)==null?null:  param.get(APPLICANTOR).toString();
        String applicaturl = param.get(APPLICANT_NAME)==null?null:  param.get(APPLICANT_NAME).toString();
        srach.put(DISTRIBUTTIONER,distributtioner);
        srach.put(START,start);
        srach.put(END,end);
        srach.put(IP_ADDRESSES,ipAddresses);
        srach.put(OPER_INT,operInt);
        srach.put(APPLICANTOR_ENGLISH,applicator);
        srach.put(APPLICANT_URL,applicaturl);
        String orderName =  param.get(ORDER_NAME)==null?null:  param.get(ORDER_NAME).toString();
        String orderType =  param.get(ORDER_TYPE)==null?null:  param.get(ORDER_TYPE).toString();
        List<Map<String,Object>> mapres = new ArrayList<>();
        PageHelper.startPage((Integer) param.get(PAGE_NUMBER), (Integer) param.get(PAGE_SIZE));
        List<Map<String,Object>> mapList = ipAddressManagePowerTableDao.selectIPdisOperHis(-1,0,distributtioner,start,end,ipAddresses,operInt,applicaturl,applicator,orderName,orderType,operStart,operEnd,null);
        PageInfo pageInfo = new PageInfo(mapList);
        for (Map<String,Object> his:mapList) {
            Map<String,Object> stringObjectMap = new HashMap<>();
            List<Map<String,Object>> kill = ipAddressManagePowerTableDao.selectIPdisOperHis(0,0,distributtioner,start,end,ipAddresses,operInt,applicaturl,applicator,orderName,orderType,operStart,operEnd,his.get(DISTINGUISH_GROUP).toString());
            List<String> ipaddress_relation = new ArrayList<>();
            for (Map<String,Object> bill:kill) {
                String ip =  bill.get(IPADDRESS_RELATION).toString().replace("[","").replace("]","");
                ipaddress_relation.addAll(Arrays.asList(ip.split(",")));
            }
            stringObjectMap.put(IP_ADDRESSES,ipaddress_relation.toString());

            List<Integer> ipaddress  = new ArrayList<>();
            if (his.get(MAP_OPER_INT).toString().equals(INPUT_TYPE_THERE)||his.get(MAP_OPER_INT).toString().equals(INPUT_TYPE_ONE)){
                JSONArray objects = JSONArray.parseArray(his.get(DIS_IPADDRESS).toString());
                for (Object o:objects) {
                    Map<String ,Object> ip = JSONObjectToMap(o.toString());
                    if (!ipaddress.contains(Integer.parseInt(ip.get(SOURCR_ID).toString()))){
                        ipaddress.add(Integer.parseInt(ip.get(SOURCR_ID).toString()));
                    }
                }
            }

            Map<String ,Object> label = JSONObjectToMap(his.get(MAP_KEY_VALUE));
            List<Label>  labels = ipAddressManagePowerTableDao.selectLabelAll(Level_SENIOR, DataType.IPHIS.getName(),-1);
            for (Label l :labels) {
                boolean novalue = true;
                for (String s :label.keySet()){
                    if (s.equals(l.getLabelName())){
                        novalue = false;
                        stringObjectMap.put(s,label.get(s));
                    }
                }
                if (novalue){
                    stringObjectMap.put(l.getLabelName(),UN_WRITE_TXET);
                }
            }
            stringObjectMap.put(IP_IDS,ipaddress);
            stringObjectMap.put(OPER_TIME,his.get(MAP_OPER_TIME));
            stringObjectMap.put(APPLICANT_DATE,his.get(MAP_APPLICANT_DATE));
            stringObjectMap.put(DISTRIBUTTIONER,his.get(MAP_APPLICANTOR_ENGLISH));
            stringObjectMap.put(IP_ID,his.get(IP_ID));
            stringObjectMap.put(OPER_INT,his.get(MAP_OPER_INT));
            mapres.add(stringObjectMap);
        }

        pageInfo.setList(mapres);
        return Reply.ok(pageInfo);
    }

    private Map<String, Object> JSONObjectToMap(Object map_key_value) {
        Map<String,Object> label = new HashMap<>();
        if (map_key_value==null){
            return label;
        }else {
            JSONObject jsonObject = JSON.parseObject(map_key_value.toString());
            for (Map.Entry<String, Object> s:jsonObject.entrySet()) {
                label.put(s.getKey(),s.getValue());
            }
            return label;
        }
    }

  /*  @Override
    public Reply selectListComprehensive(Map<String,Object> param) {
        List<IPComprehensive> list = new ArrayList<>();
        List<Label> labels = new ArrayList<>();
        List<Label> checkLabel = new ArrayList<>();
       *//* if (param.getLabels()!=null&&param.getLabels().size()>0){
           labels = param.getLabels();
            checkLabel=ipAddressManagePowerTableDao.selectLabelAll(Level_SENIOR, DataType.IPHIS.getName());
        }else {*//*
            labels = ipAddressManagePowerTableDao.selectLabelAll(Level_SENIOR, DataType.IPHIS.getName(),-1);
            checkLabel = labels;
            String sreachname = "";
            String sreach ="";
            Integer searchid = 0;
            Integer srechid = 0;
            Integer srechidby = 0 ;
        for (String s:param.keySet()) {
            for (Label l:labels) {
              if (s.equals(l.getLabelName())){
                  sreachname = s;
                  sreach = param.get(s).toString();
                  srechidby = l.getLabelId();
              }
            }
        }

        if (srechid==0){
            for (Label l:labels) {
                if (APPLICANT_NAME.equals(l.getLabelName())) {
                    srechid = l.getLabelId();
                }
                if (APPLICANTOR.equals(l.getLabelName())){
                    searchid = l.getLabelId();
                }
            }

        }

        *//*}*//*
        PageHelper.startPage((Integer) param.get(PAGE_NUMBER), (Integer) param.get(PAGE_SIZE));
        //获取分组信息
        List<Map<String,Object>> label = ipAddressManagePowerTableDao.selectLabelString(srechid,searchid,sreach,srechidby);
        PageInfo pageInfo = new PageInfo<>(label);
        List<Map<String,Object>> res = new ArrayList<>();

        for (int i = 0; i <label.size() ; i++) {
            Map<String,Object> rest =  new HashMap<>();
            *//*String group = label.get(i).get(IP_GROUP_ID).toString();*//*
            String group = label.get(i).get(IP_GROUP_ID).toString();
            String drop_value = "";
            String drop_values = "";
            if (label.get(i).get("drop_value")!=null){
                drop_value =label.get(i).get("drop_value").toString();
            }
            if (label.get(i).get("drop_values")!=null){
                drop_values =label.get(i).get("drop_values").toString();
            }
            List<Map<String,Object>> groups = ipAddressManagePowerTableDao.selectGroups(srechid,searchid,sreach,srechidby,label.get(i).get("creator").toString(),drop_value,drop_values);
            IPComprehensive comprehensive = new IPComprehensive();
            List<Integer> grouplist = new ArrayList<>();
            String string = "";

            List<Map<String,Object>> text = ipAddressManagePowerTableDao.selectLabelText(label.get(i).get(APPLICANT).toString());
            text.addAll(ipAddressManagePowerTableDao.selectLabelTextNotDrop(label.get(i).get(APPLICANT).toString()));
            Map<String,Object> labeltext = new HashMap<>();
            for (Map<String,Object> b:text) {
                labeltext.put((String) b.get("label_name"),b.get("drop_value"));
            }
            for (Label l:checkLabel) {
                if ( labeltext.get(l.getLabelName())==null|| labeltext.get(l.getLabelName()).toString().equals("")){
                    labeltext.put(l.getLabelName(),"未填");
                }
            }
            List<String> applicantId = new ArrayList<>();
            for (int j = 0; j < groups.size(); j++) {
                grouplist.add((Integer) groups.get(j).get(IP_GROUP_ID));
                if (!applicantId.contains(groups.get(j).get(APPLICANT).toString())){
                    applicantId.add(groups.get(j).get(APPLICANT).toString());
                }
            }
            if (grouplist.size()!=0){
                List<String> ipAddress = ipAddressManagePowerTableDao.selectIPaddressByDtriGroup(grouplist);
                for (String s:ipAddress) {
                    string =string==""?s:string+","+s;
                }
                comprehensive.setIpAddresses(string);
                comprehensive.setApplicantDate((Date) label.get(i).get("create_date"));
                labeltext.put(IP_ADDRESSES, comprehensive.getIpAddresses());
                labeltext.put("applicantId", String.join(",",applicantId));
                labeltext.put(DISTRIBUTTIONER, label.get(i).get("creator").toString());
                labeltext.put(APPLICANT_DATE, (Date) label.get(i).get("create_date"));
                res.add(labeltext);
            }
        }

    *//*    for (Map<String,Object> s:label) {
            String group = s.get(IP_GROUP_ID).toString();
            IPComprehensive comprehensive = new IPComprehensive();
            String applicant = s.get(APPLICANT).toString();
            List<Map<String,Object>> text = ipAddressManagePowerTableDao.selectLabelText(applicant);
            text.addAll(ipAddressManagePowerTableDao.selectLabelTextNotDrop(applicant));
            Map<String,Object> labeltext = new HashMap<>();
            for (Map<String,Object> b:text) {
                labeltext.put((String) b.get("label_name"),b.get("drop_value"));
            }
            for (Label l:checkLabel) {
                if ( labeltext.get(l.getLabelName())==null|| labeltext.get(l.getLabelName()).toString().equals("")){
                    labeltext.put(l.getLabelName(),"未填");
                }
            }
            comprehensive.setText(labeltext);
            List<Map<String,Object>> iPdisByApplicant = ipAddressManagePowerTableDao.selectIPdisByApplicant(applicant);

            for (Map<String,Object> b:iPdisByApplicant) {
                if (b.get(IP_GROUP_ID).toString().equals(s.get(IP_GROUP_ID).toString())) {
                    comprehensive.setApplicantDate((Date) b.get("applicant_date"));
                    labeltext.put(DISTRIBUTTIONER, (String) b.get("creator"));
                    labeltext.put(APPLICANT_DATE, (Date) b.get("applicant_date"));
                    comprehensive.setDistributtioner((String) b.get("creator"));
                    if (comprehensive.getIpAddresses() == null || comprehensive.getIpAddresses().equals("")) {
                        comprehensive.setIpAddresses(b.get(IP_ADDRESS_MAP).toString());
                        labeltext.put(IP_ADDRESSES, comprehensive.getIpAddresses());
                        labeltext.put("applicantId", applicant);
                    } else {
                        comprehensive.setIpAddresses(comprehensive.getIpAddresses() + "," + b.get(IP_ADDRESS_MAP).toString());
                    }
                }
            }
            labeltext.put(IP_ADDRESSES,comprehensive.getIpAddresses());
            labeltext.put("applicantId",applicant);
            res.add(labeltext);
            list.add(comprehensive);
        }*//*



        *//*PageInfo pageInfo = new PageInfo(list);
        pageInfo.setList(list);*//*
        res.sort(new Comparator<Map<String,Object>>() {
            @Override
            public int compare(Map<String,Object> o1, Map<String,Object> o2) {
                String i1 = o1.get(APPLICANTOR).toString();
                String i2 = o1.get(APPLICANTOR).toString();
                return i1.compareTo(i2);
            }
        });
        pageInfo.setList(res);
        return Reply.ok(pageInfo);
    }*/

    @Override
    public Reply sreachLabel(QueryCustomPageParam qParam) {
        List<Label> labels = ipAddressManagePowerTableDao.selectLabelAll(Level_SENIOR, DataType.IPHIS.getName(),-1);
      /*  List<Label> labels2 = ipAddressManagePowerTableDao.selectLabelAll(6, DataType.IPHIS.getName());
        labels.addAll(labels2);*/

        Reply reply = mwCustomcolService.selectById(qParam);
        MwCustomPageDTO pageDTO = (MwCustomPageDTO) reply.getData();
        List<MwCustomColDTO> pageDTOs=pageDTO.getMwCustomColDTOS();
        List<MwCustomColDTO> mwCustomColDTOS= new ArrayList<>();
        for (MwCustomColDTO mwCustomColDTO:pageDTOs) {
            if (mwCustomColDTO.getProp().equals(IP_ADDRESSES)){
                mwCustomColDTO.setSortable(false);
            }
            mwCustomColDTO.setVisible(true);

            if(mwCustomColDTO.getLabel().contains(HAVE_WITH)){
                mwCustomColDTO.setWidth(250);
            }else if (mwCustomColDTO.getLabel().contains(HAVE_PERSON)){
                mwCustomColDTO.setWidth(150);
            }
            else if (mwCustomColDTO.getLabel().contains(HAVE_OPER)){
                mwCustomColDTO.setWidth(150);
            }
            else{
                mwCustomColDTO.setWidth(null);
            }
        }
        for (Label l:labels) {
            if (l.getLabelName().equals(APPLICANTOR)){
                MwCustomColDTO customColDTO = new MwCustomColDTO();
                customColDTO.setColId(l.getLabelId());
                customColDTO.setLabel(l.getLabelName());
                customColDTO.setProp(l.getLabelName());
                customColDTO.setUserId(qParam.getUserId());
                customColDTO.setSortable(false);
                customColDTO.setWidth(150);
                mwCustomColDTOS.add(0,customColDTO);
            }
            if (l.getLabelName().equals(APPLICANT_NAME)){
                MwCustomColDTO customColDTO = new MwCustomColDTO();
                customColDTO.setColId(l.getLabelId());
                customColDTO.setLabel(l.getLabelName());
                customColDTO.setProp(l.getLabelName());
                customColDTO.setUserId(qParam.getUserId());
                customColDTO.setSortable(false);
                customColDTO.setWidth(null);
                mwCustomColDTOS.add(0,customColDTO);
            }

        }
        List<MwPageselectTable> pageselectTables=pageDTO.getMwPageselectTables();
        List<MwPageselectTable> mwPageselectTables= new ArrayList<>();
        for (Label l:labels) {
            if (l.getLabelName().equals(APPLICANTOR)||l.getLabelName().equals(APPLICANT_NAME)){
                MwPageselectTable customColDTO = new MwPageselectTable();
                customColDTO.setId(l.getLabelId());
                customColDTO.setLabel(l.getLabelName());
                customColDTO.setProp(l.getLabelName());
                if (l.getInputFormat() == INPUT_TYPE_TWO) {
                    customColDTO.setInputFormat(INPUT_TYPE_TWO);
                } else {
                    customColDTO.setInputFormat(INPUT_TYPE_ONE);
                }
                mwPageselectTables.add(customColDTO);
            }
        }
        pageselectTables.addAll(mwPageselectTables);
        mwCustomColDTOS.addAll(pageDTOs);


        pageDTO.setMwCustomColDTOS(mwCustomColDTOS);
        pageDTO.setMwPageselectTables(pageselectTables);
        return Reply.ok(pageDTO);
    }

    @Override
    public Reply countNumList() {
        IPCountNum ipCountNum = ipAddressManagePowerTableDao.countNumList();
        return Reply.ok(ipCountNum);
    }

    @Override
    public Reply countCreate(seachLabelList param)  {
        List<IPCountPricture> ipCountPrictures = new ArrayList<>();
        String solarDataStart = "";
        String solarDataEnd = "";
        if (param.getType()==0){
            solarDataStart = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);
             solarDataEnd =MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, 0);
        }else if (param.getType()==1){
            solarDataStart = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, 0);
        }
        if (param.getType()>1){
            List <Integer> s = new ArrayList<>();
            if (param.getType()==2){
                solarDataStart = MWUtils.getStartOrEndDateByweek().get(0);
                solarDataEnd = MWUtils.getStartOrEndDateByweek().get(1);
                s = MWUtils.getStartOrEndDateByweekGetDay();
            }
            else if (param.getType()==3){
                solarDataStart = MWUtils.getLastDayOfMonth().get(0);
                solarDataEnd = MWUtils.getLastDayOfMonth().get(1);
                s = MWUtils.getStartOrEndDateByMonthGetDay();
            }
            IPCountPricture ipCountPricture = new IPCountPricture();
            List<IPCountPrictureDetails> prictures = ipAddressManagePowerTableDao.countCreateByDay(solarDataStart,solarDataEnd,s);
            ipCountPricture.setDetails(prictures);
            ipCountPricture.setType(0);
            ipCountPrictures.add(ipCountPricture);
            IPCountPricture ipCountPricturet = new IPCountPricture();
            List<IPCountPrictureDetails> pricturest = ipAddressManagePowerTableDao.countDeleteByDay(solarDataStart,solarDataEnd,s);
            ipCountPricturet.setDetails(pricturest);
            ipCountPricturet.setType(1);
            ipCountPrictures.add(ipCountPricturet);
        }else {
            IPCountPricture ipCountPricture = new IPCountPricture();
            List<IPCountPrictureDetails> prictures = ipAddressManagePowerTableDao.countCreateByhour(solarDataStart,solarDataEnd);
            ipCountPricture.setDetails(prictures);
            ipCountPricture.setType(0);
            ipCountPrictures.add(ipCountPricture);
            IPCountPricture ipCountPricturet = new IPCountPricture();
            List<IPCountPrictureDetails> pricturest = ipAddressManagePowerTableDao.countDeleteByhour(solarDataStart,solarDataEnd);
            ipCountPricturet.setDetails(pricturest);
            ipCountPricturet.setType(1);
            ipCountPrictures.add(ipCountPricturet);
        }

        return Reply.ok(ipCountPrictures);
    }

    @Override
    public Reply countHaving(seachLabelList param) {
        List<IPCountPricture> ipCountPrictures = new ArrayList<>();
        String solarDataStart = "";
        String solarDataEnd = "";
        if (param.getType()==0){
            solarDataStart = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, -1);
            solarDataEnd =MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, 0);
        }else if (param.getType()==1){
            solarDataStart = MWUtils.getSolarData(0, 0, 0, MWDateConstant.NORM_DATE, 0);
        }
        if (param.getType()>1){
            List <Integer> s = new ArrayList<>();
            if (param.getType()==2){
                solarDataStart = MWUtils.getStartOrEndDateByweek().get(0);
                solarDataEnd = MWUtils.getStartOrEndDateByweek().get(1);
                s = MWUtils.getStartOrEndDateByweekGetDay();
            }
            else if (param.getType()==3){
                solarDataStart = MWUtils.getLastDayOfMonth().get(0);
                solarDataEnd = MWUtils.getLastDayOfMonth().get(1);
                s = MWUtils.getStartOrEndDateByMonthGetDay();
            }
            IPCountPricture ipCountPricture = new IPCountPricture();
            List<IPCountPrictureDetails> prictures = ipAddressManagePowerTableDao.countHavingByDay(solarDataStart,solarDataEnd,0,s);
            ipCountPricture.setDetails(prictures);
            ipCountPricture.setType(0);
            ipCountPrictures.add(ipCountPricture);
            IPCountPricture ipCountPricturet = new IPCountPricture();
            List<IPCountPrictureDetails> pricturest = ipAddressManagePowerTableDao.countHavingByDay(solarDataStart,solarDataEnd,1,s);
            ipCountPricturet.setDetails(pricturest);
            ipCountPricturet.setType(1);
            ipCountPrictures.add(ipCountPricturet);
        }else {
            IPCountPricture ipCountPricture = new IPCountPricture();
            List<IPCountPrictureDetails> prictures = ipAddressManagePowerTableDao.countHavingByhour(solarDataStart,solarDataEnd,0);
            ipCountPricture.setDetails(prictures);
            ipCountPricture.setType(0);
            ipCountPrictures.add(ipCountPricture);
            IPCountPricture ipCountPricturet = new IPCountPricture();
            List<IPCountPrictureDetails> pricturest = ipAddressManagePowerTableDao.countHavingByhour(solarDataStart,solarDataEnd,1);
            ipCountPricturet.setDetails(pricturest);
            ipCountPricturet.setType(1);
            ipCountPrictures.add(ipCountPricturet);
   /*         IPCountPricture ipCountPrictureth = new IPCountPricture();
            List<IPCountPrictureDetails> pricturesth = ipAddressManagePowerTableDao.countHavingByhour(solarDataStart,solarDataEnd,2);
            ipCountPrictureth.setDetails(pricturesth);
            ipCountPrictureth.setType(2);
            ipCountPrictures.add(ipCountPrictureth);*/
        }

        return Reply.ok(ipCountPrictures);
    }

    @Override
    public Reply getTree(seachLabelList param) {
        List<Map<String,Object> > ipc = new ArrayList<>();
        for (String applicants:param.getApplicant()) {
            List<String> applicantes = Arrays.asList(applicants.split(","));
            for (String applicant:applicantes) {
                List<Map<String,Object>> iPdisByApplicant = ipAddressManagePowerTableDao.selectIPdisByApplicant(applicant);
                if (iPdisByApplicant!=null&&iPdisByApplicant.size()>0){
                    List<String>  Ipids = MapUtils.getList(iPdisByApplicant,IP_GROUP_ID);
                    String s = String.valueOf(iPdisByApplicant.get(0).get(BANG_DISTRI)==null?"":iPdisByApplicant.get(0).get(BANG_DISTRI));
                    List<Map<String,Object>> ipdistri = ipAddressManagePowerTableDao.selectTreeAllbyIPids(Ipids,s,applicant);
                    for (Map<String,Object> objectMap:ipdistri) {
                        Integer integer  = Integer.parseInt( objectMap.get(MAP_IP_LIST_TYPE).toString());
                        if (integer==0){
                            IpAddressManageTableParam ipAddressManageTableParam= ipAddressManagePowerTableDao.selectIpAddressManageTableParam( Integer.parseInt( objectMap.get(PAREN_IP).toString()));
                            objectMap.put(PAREN_IP,ipAddressManageTableParam.getParentId());
                        }else{
                            IpAddressManageTableParam ipAddressManageTableParam= ipAddressManagePowerTableDao.selectIpv6AddressManageTableParam( Integer.parseInt( objectMap.get(PAREN_IP).toString()));
                            objectMap.put(PAREN_IP,ipAddressManageTableParam.getParentId());
                        }
                    }
                    Map<String,List<Map<String,Object>>> maplist = MapUtils.groupBykey(IP_GROUP_ID,ipdistri);
                    Set<String> strings = maplist.keySet();
                    for (String str:strings) {
                        List<Map<String,Object>> iptree = MapUtils.getTree(maplist.get(str),str,MAP_IP_LIST_ID,PRIMARY_IP);

                        ipc.addAll(iptree);
                    }
            }
                else {
                    return  Reply.fail(FALIE_NUMBER_FUL);
                }
            }


        }
        return  Reply.ok(ipc);
    }

    @Override
    @Transactional
    public Reply cancel(List<DeleteIpList> param) {
        for (DeleteIpList params: param) {
                if (params.getId()!=0){
                    List<Integer> id = new ArrayList<>();
                    id.add(params.getId());
                    params.setIds(id);
                }
                List<IpamOperHistoryDTO> ipamOperHistoryDTOS = ipAddressManagePowerTableDao.getApplicantBydisttri(params.getIds(),params.getApplicantId());
                for (IpamOperHistoryDTO f : ipamOperHistoryDTOS){
                    IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                    ipamOperHistoryDTO.setCreateDate(new Date()).setType(1).setCreator(iLoginCacheInfo.getLoginName()).setIpType(f.getIpType()).setRlistId(f.getRlistId()).setApplicant(f.getApplicant()).setDesc(f.getDesc());
                    ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);
                    if (f.getIpType()==false){
                        List<Integer> ipv4 = new ArrayList<>();
                        ipv4.add(f.getRlistId());
                        ipAddressManagePowerTableDao.deleteDstributionByIpv4(ipv4,f.getDesc());
                        List<Integer> ip = ipAddressManagePowerTableDao.checkStauts(ipv4,IPV4_JUDGE);
                        List<Integer> delete = new ArrayList<>();
                        for (Integer i:ipv4) {
                            if (ip.contains(i)){

                            }else {
                                delete.add(i);
                            }
                        }
                        if (delete.size()>0) {
                            ipAddressManagePowerTableDao.UpdateIPListDistributionTrue(delete, false);
                        }
                    }else{
                        List<Integer> ipv6 = new ArrayList<>();
                        ipv6.add(f.getRlistId());
                        ipAddressManagePowerTableDao.deleteDstributionByIpv6(ipv6,f.getDesc());
                    }

                }
        }



        return Reply.ok(SUCCESSFUL);
    }

    @Override
    @Transactional
    public Reply change(List<seachLabelList> param) {
        for (seachLabelList s:param) {
            if (s.getDistriId()==null||s.getDistriId().equals("")){
                return Reply.fail(FALIE_NUMBER_FUL);
            }else if (s.getIds()!=null&&s.getIds().size()>0){
                return Reply.fail(FALIE_NUMBER_FUL);
            }
            else{
                List<IpDestribution> ipDestributions = new ArrayList<>();
                List<IpDestribution> ipDestributionOld = ipAddressManagePowerTableDao.selectrealdtri(s.getDistriId());
                ipDestributions = getFristChildren(s);
                List<IpDestribution> cleanNode = new ArrayList<>();
                List<IpDestribution> addNode = new ArrayList<>();
                List<Integer> ipv4s = new ArrayList<>();
                List<Integer> ipv6s = new ArrayList<>();
                IpDestribution ipDestribution = ipDestributionOld.get(0);
                for (IpDestribution l:ipDestributions) {
                    if (l.getIplistType()==0){
                        ipv4s.add(l.getIplistId());
                    }else {
                        ipv6s.add(l.getIplistId());
                    }

                    boolean add  = true;
                    for (IpDestribution o:ipDestributionOld){
                        if (o.getIplistId().equals(l.getIplistId())&&o.getPrimaryIp().equals(l.getPrimaryIp())){
                             add = false;
                        }
                    }
                    if (add){
                        l.setOa(ipDestribution.getOa());
                        l.setOatext(ipDestribution.getOatext());
                        l.setOaurl(ipDestribution.getOaurl());
                        l.setOaurltext(ipDestribution.getOaurltext());
                        l.setOrgIds(ipDestribution.getOrgIds());
                        l.setOrgtext(ipDestribution.getOrgtext());
                        addNode.add(l);
                    }
                }
                for (IpDestribution l:ipDestributionOld) {
                    boolean delete  = true;
                    for (IpDestribution o:ipDestributions){
                        if (o.getIplistId().equals(l.getIplistId())&&o.getPrimaryIp().equals(l.getPrimaryIp())){
                            delete = false;
                        }
                    }
                    if (delete){
                        cleanNode.add(l);
                    }
                }
                String  applicant= s.getApplicantId();
                Integer id = s.getIplist_id();
                IpamProcessHistoryDTO ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicantById(applicant);
                ipAddressManagePowerTableDao.insterApplicant(ipamProcessHistoryDTO);
                String applicantId = ipamProcessHistoryDTO.getId().toString();
                List<MwAssetsLabelDTO> list = mwLabelCommonServcie.getLabelBoard(applicant,DataType.IPHIS.getName());
                for (MwAssetsLabelDTO l:list) {
                    l.setLabelId(l.getId());
                }
                mwLabelCommonServcie.insertLabelboardMapper(list, applicantId.toString(), DataType.IPHIS.getName());

                if (addNode.size()>0){
                    List<Integer> ids = new ArrayList<>();
                    ids.add(s.getDistriId());
                    List<Integer> ipv4 = new ArrayList<>();
                    List<Integer> ipv6 = new ArrayList<>();
                    List<IpamOperHistoryDTO> responseIpAddressOperHistoryParamOB = ipAddressManagePowerTableDao.getApplicantBydisttri(ids,applicant);
                    for (IpDestribution o:addNode) {
                        IpamOperHistoryDTO ipAddressOperHistoryParamOB = responseIpAddressOperHistoryParamOB.get(0);
                        ipAddressOperHistoryParamOB.setRlistId(o.getIplistId());
                        ipAddressOperHistoryParamOB.setCreateDate(new Date());
                        ipAddressOperHistoryParamOB.setIpType(o.getIplistType()==1);
                        ipAddressOperHistoryParamOB.setType(0);
                        if (o.getIplistType()==1){
                            ipv6.add(o.getIplistId());
                        }else {
                            ipv4.add(o.getIplistId());
                        }
                        ipAddressManagePowerTableDao.insterOperHistoryDTO(ipAddressOperHistoryParamOB);
                        ipAddressManagePowerTableDao.insertDstr(o);
                    }
                    if (ipv6.size()>0){
                        ipAddressManagePowerTableDao.UpdateIPV6ListDistributionTrue(ipv6,true);
                    }
                    if (ipv4.size()>0){
                        ipAddressManagePowerTableDao.UpdateIPListDistributionTrue(ipv4,true);
                    }


                }

                if (cleanNode.size()>0){
                    seachLabelList b = new seachLabelList();
                    List<Integer> integers = new ArrayList<>();
                    for (IpDestribution o:cleanNode) {
                        integers.add(o.getId());
                    }
                    b.setIds(integers);
                    List<DeleteIpList> deleteIpLists = new ArrayList<>();
                    DeleteIpList deleteIpList =  new DeleteIpList();
                    deleteIpList.setIds(integers);
                    deleteIpList.setApplicantId(applicant);
                    deleteIpLists.add(deleteIpList);
                    cancel(deleteIpLists);
                }

                for (Integer i:ipv4s) {
                    ipAddressManagePowerTableDao.updateApplicant(Integer.parseInt(applicantId),Integer.parseInt(applicant),i,false,null);
                }
                for (Integer i :ipv6s){
                    ipAddressManagePowerTableDao.updateApplicant(Integer.parseInt(applicantId),Integer.parseInt(applicant),i,true,null);
                }

            }
        }


        return Reply.ok();
    }

    @Override
    public Reply browagain(seachLabelList param) {
        List<ResponIpDistributtionParam> responIpDistributtionParams = new ArrayList<>();
        for (String applicant:param.getApplicant()) {
            List<Map<String,Object>> iPdisByApplicant = ipAddressManagePowerTableDao.selectIPdisByApplicant(applicant);
            String ipgroup  = "";
            for (int i = 0; i <iPdisByApplicant.size() ; i++) {
                if (!ipgroup.equals(iPdisByApplicant.get(i).get(IP_GROUP_ID).toString())) {
                    ipgroup = iPdisByApplicant.get(i).get(IP_GROUP_ID).toString();
                    QueryIpAddressDistributtionParam queryIpAddressDistributtionParam = new QueryIpAddressDistributtionParam();
                    List<Integer> integers = new ArrayList<>();
                    integers.add(Integer.parseInt(iPdisByApplicant.get(i).get(IP_GROUP_ID).toString()));
                    queryIpAddressDistributtionParam.setId(integers);
                    queryIpAddressDistributtionParam.setIdType(true);
                    List<ResponIpDistributtionParam> responIpDistributtionParamList = (List<ResponIpDistributtionParam>) selectListbrow(queryIpAddressDistributtionParam).getData();
                    responIpDistributtionParams.addAll(responIpDistributtionParamList);
                }
            }
        }


        return Reply.ok(responIpDistributtionParams);
    }

    @Override
    public Reply getInfo(Map<String, Object> param) {
        Map<String,Object> objectMap = new HashMap<>();
        Integer integer = (Integer) param.get(IP_ID);
        List<Map<String, Object>> mapListGroup = ipAddressManagePowerTableDao.selectIPdisOperHis(-1,integer, null,null,null,null,null,null,null,null,null,null,null,null);
        List<Map<String, Object>> mapList = ipAddressManagePowerTableDao.selectIPdisOperHis(0,0, null,null,null,null,null,null,null,null,null,null,null,mapListGroup.get(0).get(DISTINGUISH_GROUP).toString());
        List<Map<String, Object>> listTree = new ArrayList<>();
        Map<String ,Object> label = JSONObjectToMap(mapList.get(0).get(MAP_KEY_VALUE));
        Map<String ,Object> bel = JSONObjectToMap(mapList.get(0).get(MAP_KEY_VALUE));
        List<Label> labels = ipAddressManagePowerTableDao.selectLabelAll(Level_SENIOR, DataType.IPHIS.getName(), -1);
        for (Label label1:labels) {
            boolean has = false;
            for(String kill:label.keySet()){
                if (label1.getLabelName().equals(kill)){
                    has = true;
                    bel.put(kill,label.get(kill));
                }
            }
            if (!has){
                bel.put(label1.getLabelName(),"");
            }
        }
        if (label==null||label.size()<1){

        }
        List<Map<String,Object>> mapList1 = JSONARRAYtoLISTMAP(mapList,MAP_IP_DISCRIPTION);
        List<Map<String,Object>> dis_ipaddress = JSONARRAYtoLISTMAP(mapList,DIS_IPADDRESS);
        List<Map<String,Object>> dis_old_ipaddress = JSONARRAYtoLISTMAP(mapList,MAP_DIS_OLD_IPADDRESS);
        List<Map<String,Object>> res = new ArrayList<>();
        Map<String, List<Map<String, Object>>> ip = MapUtils.groupBykey(SOURCR_ID,dis_ipaddress);
        Map<String, List<Map<String, Object>>> Oldip = MapUtils.groupBykey(SOURCR_ID,dis_old_ipaddress);
        //分配
        if (mapList.get(0).get(MAP_OPER_INT).toString().equals(INPUT_TYPE_ONE)){
            for (String s:ip.keySet()) {
                for (Map<String, Object> map:ip.get(s)) {
                    map.put(PRIMARY_IP,map.get(PARENT_ID));
                    map.put(PRIMARY_IP_TYPE,map.get(PARENT_IP_TYPE));
                    map.put(IP_GROUP_ID,map.get(SOURCR_ID));
                    map.put(IP_GROUP_TYPR,map.get(SOURCE_IP_TYPE));
                    map.put(MAP_IP_LIST_ID,map.get(IP_ID));
                    map.put(MAP_IP_LIST_TYPE,map.get(IP_TYPE_ID));

                    if (map.get(IP_TYPE_ID).toString().equals(FALSE_JUDGE)){
                        QueryIpAddressManageListParam queryIpAddressManageListParam = ipAddressManagePowerTableDao.selectIpAddressManageTableListParam((Integer) map.get(IP_ID));
                        IpAddressManageTableParam ipAddressManageTableParam= ipAddressManagePowerTableDao.selectIpAddressManageTableParam(queryIpAddressManageListParam.getLinkId());
                        map.put(IP_ADDRESS_MAP,queryIpAddressManageListParam.getIpAddress());
                        map.put(INDEX_SORT_IP,getLong(queryIpAddressManageListParam.getIpAddress()));
                        map.put(PAREN_IP,ipAddressManageTableParam.getParentId());
                        IpAddressManageTableParam ipAddressManageTableParam1= ipAddressManagePowerTableDao.selectIpAddressManageTableParam(ipAddressManageTableParam.getParentId());
                        map.put(FRIDST_NAME,ipAddressManageTableParam1.getLabel());
                    }else {
                        QueryIpAddressManageListParam queryIpAddressManageListParam = ipAddressManagePowerTableDao.selectIpv6ddressManageTableListParam((Integer) map.get(IP_ID));
                        IpAddressManageTableParam ipAddressManageTableParam= ipAddressManagePowerTableDao.selectIpv6AddressManageTableParam(queryIpAddressManageListParam.getLinkId());
                        map.put(IP_ADDRESS_MAP,queryIpAddressManageListParam.getIpAddress());
                        map.put(INDEX_SORT_IP,0);
                        map.put(PAREN_IP,ipAddressManageTableParam.getParentId());
                        IpAddressManageTableParam ipAddressManageTableParam1= ipAddressManagePowerTableDao.selectIpAddressManageTableParam(ipAddressManageTableParam.getParentId());
                        map.put(FRIDST_NAME,ipAddressManageTableParam1.getLabel());
                    }
                    map.put(IP_STATUS,1);
                    for (Map<String,Object> descr:mapList1) {
                        if (descr.get(BAND_IP_ID).toString().equals(map.get(IP_ID).toString())){
                            map.put(DESCRIPT,descr.get(DESCRIPTION));
                        }
                    }
                }
                MapUtils.sortList(ip.get(s),INDEX_SORT_IP);
                List<Map<String,Object>> iptree = MapUtils.getTree(ip.get(s),s,MAP_IP_LIST_ID,PRIMARY_IP);
                res.addAll(iptree);
            }
        }
        //回收
        if (mapList.get(0).get(MAP_OPER_INT).toString().equals(INPUT_TYPE_TWO)){
            for (String s:Oldip.keySet()) {
                for (Map<String, Object> map:Oldip.get(s)) {
                    map.put(PRIMARY_IP,map.get(PARENT_ID));
                    map.put(PRIMARY_IP_TYPE,map.get(PARENT_IP_TYPE));
                    map.put(IP_GROUP_ID,map.get(SOURCR_ID));
                    map.put(IP_GROUP_TYPR,map.get(SOURCE_IP_TYPE));
                    map.put(MAP_IP_LIST_ID,map.get(IP_ID));
                    map.put(MAP_IP_LIST_TYPE,map.get(IP_TYPE_ID));
                    if (map.get(IP_TYPE_ID).toString().equals(FALSE_JUDGE)){
                        QueryIpAddressManageListParam queryIpAddressManageListParam = ipAddressManagePowerTableDao.selectIpAddressManageTableListParam((Integer) map.get(IP_ID));
                        IpAddressManageTableParam ipAddressManageTableParam= ipAddressManagePowerTableDao.selectIpAddressManageTableParam(queryIpAddressManageListParam.getLinkId());
                        map.put(IP_ADDRESS_MAP,queryIpAddressManageListParam.getIpAddress());
                        map.put(INDEX_SORT_IP,getLong(queryIpAddressManageListParam.getIpAddress()));
                        map.put(PAREN_IP,ipAddressManageTableParam.getParentId());
                        IpAddressManageTableParam ipAddressManageTableParam1= ipAddressManagePowerTableDao.selectIpAddressManageTableParam(ipAddressManageTableParam.getParentId());
                        map.put(FRIDST_NAME,ipAddressManageTableParam1.getLabel());
                    }else {
                        QueryIpAddressManageListParam queryIpAddressManageListParam = ipAddressManagePowerTableDao.selectIpv6ddressManageTableListParam((Integer) map.get(IP_ID));
                        IpAddressManageTableParam ipAddressManageTableParam= ipAddressManagePowerTableDao.selectIpv6AddressManageTableParam(queryIpAddressManageListParam.getLinkId());
                        map.put(IP_ADDRESS_MAP,queryIpAddressManageListParam.getIpAddress());
                        map.put(INDEX_SORT_IP,0);
                        map.put(PAREN_IP,ipAddressManageTableParam.getParentId());
                        IpAddressManageTableParam ipAddressManageTableParam1= ipAddressManagePowerTableDao.selectIpAddressManageTableParam(ipAddressManageTableParam.getParentId());
                        map.put(FRIDST_NAME,ipAddressManageTableParam1.getLabel());
                    }
                    boolean has = false;
                    for (Map<String,Object> hashmap:dis_ipaddress) {
                       if (hashmap.get(IP_ID).toString().equals(map.get(MAP_IP_LIST_ID).toString())){
                           has =true;
                       }
                    }
                    if (has){
                        map.put(IP_STATUS,0);
                    }else {
                        map.put(IP_STATUS,2);
                    }
                    for (Map<String,Object> descr:mapList1) {
                        if (descr.get(BAND_IP_ID).toString().equals(map.get(IP_ID).toString())) {
                            map.put(DESCRIPT, descr.get(DESCRIPTION));
                        }
                    }
                    if (map.get(DESCRIPT)==null){
                        map.put(DESCRIPT, "");
                    }
                }
                MapUtils.sortList(Oldip.get(s),INDEX_SORT_IP);
                List<Map<String,Object>> iptree = MapUtils.getTree(Oldip.get(s),s,MAP_IP_LIST_ID,PRIMARY_IP);
                res.addAll(iptree);
            }
        }
        if (mapList.get(0).get(MAP_OPER_INT).toString().equals(INPUT_TYPE_THERE)){
            for (String s:Oldip.keySet()) {
                List<Map<String,Object>> addMap   =  MapCompare(Oldip.get(s),ip.get(s));
                List<Map<String,Object>> cleanMap   =  MapCompare(ip.get(s),Oldip.get(s));
                List<Map<String,Object>> count = countHave(Oldip.get(s),ip.get(s));
                for (Map<String, Object> map:count) {
                    map.put(PRIMARY_IP,map.get(PARENT_ID));
                    map.put(PRIMARY_IP_TYPE,map.get(PARENT_IP_TYPE));
                    map.put(IP_GROUP_ID,map.get(SOURCR_ID));
                    map.put(IP_GROUP_TYPR,map.get(SOURCE_IP_TYPE));
                    map.put(MAP_IP_LIST_ID,map.get(IP_ID));
                    map.put(MAP_IP_LIST_TYPE,map.get(IP_TYPE_ID));
                    if (map.get(IP_TYPE_ID).toString().equals(FALSE_JUDGE)){
                        QueryIpAddressManageListParam queryIpAddressManageListParam = ipAddressManagePowerTableDao.selectIpAddressManageTableListParam((Integer) map.get(IP_ID));
                        IpAddressManageTableParam ipAddressManageTableParam= ipAddressManagePowerTableDao.selectIpAddressManageTableParam(queryIpAddressManageListParam.getLinkId());
                        map.put(IP_ADDRESS_MAP,queryIpAddressManageListParam.getIpAddress());
                        map.put(INDEX_SORT_IP,getLong(queryIpAddressManageListParam.getIpAddress()));
                        map.put(PAREN_IP,ipAddressManageTableParam.getParentId());
                        IpAddressManageTableParam ipAddressManageTableParam1= ipAddressManagePowerTableDao.selectIpAddressManageTableParam(ipAddressManageTableParam.getParentId());
                        map.put(FRIDST_NAME,ipAddressManageTableParam1.getLabel());
                    }else {
                        QueryIpAddressManageListParam queryIpAddressManageListParam = ipAddressManagePowerTableDao.selectIpv6ddressManageTableListParam((Integer) map.get(IP_ID));
                        IpAddressManageTableParam ipAddressManageTableParam= ipAddressManagePowerTableDao.selectIpv6AddressManageTableParam(queryIpAddressManageListParam.getLinkId());
                        map.put(IP_ADDRESS_MAP,queryIpAddressManageListParam.getIpAddress());
                        map.put(INDEX_SORT_IP,0);
                        map.put(PAREN_IP,ipAddressManageTableParam.getParentId());
                        IpAddressManageTableParam ipAddressManageTableParam1= ipAddressManagePowerTableDao.selectIpAddressManageTableParam(ipAddressManageTableParam.getParentId());
                        map.put(FRIDST_NAME,ipAddressManageTableParam1.getLabel());
                    }
                    if (map.get(IP_STATUS)==null){
                        map.put(IP_STATUS,0);
                    }
                    for (Map<String,Object> descr:mapList1) {
                        if (descr.get(BAND_IP_ID).toString().equals(map.get(IP_ID).toString())){
                            map.put(DESCRIPT,descr.get(DESCRIPTION));
                        }
                    }
                }
                MapUtils.sortList(Oldip.get(s),INDEX_SORT_IP);
                List<Map<String,Object>> iptree = MapUtils.getTree(Oldip.get(s),s,MAP_IP_LIST_ID,PRIMARY_IP);
                res.addAll(iptree);
            }

        }
        MapUtils.sortList(res,INDEX_SORT_IP);
        List<Map<String,Object>> maps = new ArrayList<>();
        for (Map<String, Object> kill:res) {
            Map<String, Object> father = new HashMap<>();
            List<Map<String,Object>> child = new ArrayList<>();
            child.add(kill);
            boolean has  = false;
            for (Map<String, Object>  group:maps) {
                if (group.get(PAREN_IP).toString().equals(kill.get(PAREN_IP).toString())){
                    List<Map<String,Object>> bill  = (List<Map<String, Object>>) group.get(CHILDREN);
                    bill.addAll(child);
                    group.put(CHILDREN,bill);
                    has  = true;
                }
            }
            if (!has){
                father.put(CHILDREN,child);
                father.put(FRIDST_NAME,kill.get(FRIDST_NAME));
                father.put(PAREN_IP,kill.get(PAREN_IP));
                father.put(MAP_IP_LIST_TYPE,3);
                maps.add(father);
            }
        }
        List<Map<String,Object>> mapArrayList = new ArrayList<>();
        for (Map<String,Object> kill:maps) {
            Map<String, Object> father = new HashMap<>();
            List<Map<String,Object>> mapList2 = new ArrayList<>();
            Integer i = (Integer) kill.get(PAREN_IP);
            IpAddressManageTableParam ipAddressManageTableParam1= ipAddressManagePowerTableDao.selectIpAddressManageTableParam(ipAddressManagePowerTableDao.selectIpAddressManageTableParam(i).getParentId());
            mapList2.add(kill);
            if (ipAddressManageTableParam1==null){
                father.put(CHILDREN,mapList2);
                father.put(FRIDST_NAME,"");
                father.put(PAREN_IP,0);
                father.put(MAP_IP_LIST_TYPE,3);
            }else {
                father.put(CHILDREN,mapList2);
                father.put(FRIDST_NAME,ipAddressManageTableParam1.getLabel());
                father.put(PAREN_IP,ipAddressManageTableParam1.getId());
                father.put(MAP_IP_LIST_TYPE,3);
            }
            boolean hasFul = false;
            for (Map<String,Object> objectMap1:mapArrayList) {
                if (father.get(PAREN_IP).toString().equals(objectMap1.get(PAREN_IP).toString())){
                    List<Map<String,Object>> mapList3 = (List<Map<String, Object>>) objectMap1.get(CHILDREN);
                    mapList3.addAll((List<Map<String, Object>>) father.get(CHILDREN));
                    objectMap1.put(CHILDREN,mapList3);
                    hasFul = true;
                }
            }
            if (!hasFul){
                mapArrayList.add(father);
            }
        }

//        第一证卷特有
        mapArrayList =  sortResIndex(mapArrayList);
        objectMap.put(OPER_INT,mapList.get(0).get(MAP_OPER_INT));
        objectMap.put( LIST_TREE,mapArrayList);
        objectMap.put(APPLICANT_DATE,mapList.get(0).get(MAP_APPLICANT_DATE));
        objectMap.put(DISTRIBUTTIONER,mapList.get(0).get(MAP_APPLICANTOR_ENGLISH));
        objectMap.putAll(bel);
        objectMap.put(APPLICANT_DATE,mapList.get(0).get(MAP_APPLICANT_DATE));
        return Reply.ok(objectMap);
    }

    private List<Map<String, Object>> sortResIndex(List<Map<String, Object>> mapArrayList) {
        List<String> nameList= NAME_LIST;
        int i = 3;
        for (Map<String, Object> map:mapArrayList) {
            if (nameList.contains(map.get(FRIDST_NAME).toString())){
                int pull  = 0;
                for (String s:nameList) {
                    pull = pull +1;
                    if (s.equals(map.get(FRIDST_NAME).toString())){
                        map.put(SORT_INDEX_PUT,pull);
                    }
                }
            }else {
                map.put(SORT_INDEX_PUT,i);
                i=i+1;
            }
        }
        MapUtils.sortList(mapArrayList,SORT_INDEX_PUT);
        return mapArrayList;
    }

    private Long getLong(String ipAddress) {
        List<String> s = Arrays.asList(ipAddress.split("\\."));
        Long k = 0l;
        for (String b:s) {
            k = k*1000+Long.parseLong(b);
        }
        return k;
    }

    private List<Map<String, Object>> countHave(List<Map<String, Object>> oldip, List<Map<String, Object>> ip) {
        List<Map<String,Object>> qubieMap = oldip;

        for (Map<String, Object> map:oldip) {
            boolean has = false;
            for (Map<String, Object> objectMap:ip) {
                if (map.get(IP_ID).toString().equals(objectMap.get(IP_ID).toString())){
                    has = true;
                }
            }
            if (!has){
                map.put(IP_STATUS,2);
            }
        }
        for (Map<String, Object> map:ip) {
            boolean has = false;
            for (Map<String, Object> objectMap:oldip) {
                if (map.get(IP_ID).toString().equals(objectMap.get(IP_ID).toString())){
                    has = true;
                }
            }
            if (!has){
                map.put(IP_STATUS,1);
                qubieMap.add(map);
            }
        }
        return  qubieMap;
    }

    private List<Map<String, Object>> MapCompare(List<Map<String, Object>> oldip, List<Map<String, Object>> ip) {
          List<Map<String,Object>> qubieMap = new ArrayList<>();
          boolean has = false;
            for (Map<String, Object> map:oldip) {
                for (Map<String, Object> objectMap:ip) {
                    if (map.get(IP_ID).toString().equals(objectMap.get(IP_ID).toString())){
                        has = true;
                    }
                }
                if (!has){
                    qubieMap.add(map);
                }
            }
            return  qubieMap;
    }

    private List<Map<String, Object>> JSONARRAYtoLISTMAP(List<Map<String,Object>> objects,String key) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Map<String,Object> objectMap:objects) {
            Object ip_discription = objectMap.get(key);
            if (ip_discription==null){
                return mapList;
            }else {
                JSONArray jsonArray = JSON.parseArray(ip_discription.toString());
                for (Object o:jsonArray) {
                    Map<String ,Object> map = JSONObjectToMap(o.toString());
                    mapList.add(map);
                }
            }
        }
        return  mapList;
    }

    @Override
    public void execlInfo( Map<String,String> param, HttpServletResponse response)  throws IOException {

            /*}*/
            //获取分组信息
            List<Map<String, Object>> res = new ArrayList<>();
        List<Map<String,Object>> mapList = ipAddressManagePowerTableDao.selectIPdisOperHis(0,0,null,null, null,null,null,null,null,null,null,null,null,null);
        for (Map<String,Object> his:mapList) {
            Map<String,Object> stringObjectMap = new HashMap<>();
            stringObjectMap.put(IP_ADDRESSES,his.get(IPADDRESS_RELATION));
            Map<String ,Object> label = JSONObjectToMap(his.get(MAP_KEY_VALUE));
            List<Label>  labels = ipAddressManagePowerTableDao.selectLabelAll(Level_SENIOR, DataType.IPHIS.getName(),-1);
            for (Label l :labels) {
                boolean novalue = true;
                for (String s :label.keySet()){
                    if (s.equals(l.getLabelName())){
                        novalue = false;
                        stringObjectMap.put(s,label.get(s));
                    }
                }
                if (novalue){
                    stringObjectMap.put(l.getLabelName(),UN_WRITE_TXET);
                }
            }
            stringObjectMap.put(APPLICANT_DATE,his.get(MAP_APPLICANT_DATE).toString());
            stringObjectMap.put(DISTRIBUTTIONER,his.get(MAP_APPLICANTOR_ENGLISH));
            stringObjectMap.put(IP_ID,his.get(IP_ID));
            stringObjectMap.put(OPER_INT,his.get(MAP_OPER_INT));
            res.add(stringObjectMap);
        }

            ExcelWriter excelWriter = null;
            try {
                //需要导出的数据

                //将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)

                //初始化导出字段
                Set<String> includeColumnFiledNames = new HashSet<>();


                includeColumnFiledNames.add(APPLICANT_NAME);
                includeColumnFiledNames.add(APPLICANTOR);
                includeColumnFiledNames.add(DISTRIBUTTIONER);
                includeColumnFiledNames.add(IP_ADDRESSES);
                includeColumnFiledNames.add(APPLICANT_DATE);
                includeColumnFiledNames.add(OPER_INT);
                List<String> strings= new ArrayList<>();
                strings.add(APPLICANT_NAME);
                strings.add(APPLICANTOR);
                strings.add(DISTRIBUTTIONER);
                strings.add(IP_ADDRESSES);
                strings.add(APPLICANT_DATE);
                strings.add(OPER_INT);
                List<List<Object>> bill = new ArrayList<>();
                for (Map<String,Object> m:res) {
                    List<Object> data = new ArrayList<>();
                    for (String b:strings) {
                        data.add(m.get(b));
                    }
                    bill.add(data);
                }

                List<String> stringtille= new ArrayList<>();
                stringtille.add(APPLICANT_NAME);
                stringtille.add(APPLICANTOR);
                stringtille.add(APPLICANT_PEOPLE);
                stringtille.add(IP_ADDRESS_TEXT);
                stringtille.add(SUBMIT_TIME);
                stringtille.add(OPER_NUMBER);
                List<List<String>> lists = new ArrayList<>();
                for (String s:stringtille) {
                    List<String> data = new ArrayList<>();
                    data.add(s);
                    lists.add(data);
                }

                //设置回复头一些信息
                String fileName = null; //导出文件名

                fileName = System.currentTimeMillis() + "";

                response.setContentType(HTTP_CONTAXT);
                response.setCharacterEncoding(HTTP_UTF);
                response.setHeader(CONTENT_DISPOSITION, ATTACHMENT_FILENAME+ fileName + FLIENAME_FIX);

                //创建easyExcel写出对象

                //计算sheet分页

              /*  for (int i = 0; i < res.size(); i++) {
                    WriteSheet sheet = EasyExcel.writerSheet(i, "sheet" + i)
                            .includeColumnFiledNames(includeColumnFiledNames)
                            .build();*/

                EasyExcel.write( response.getOutputStream()).head(lists).sheet(SHEET_ONE)
                        .doWrite(bill);

                /*}*/
                System.err.println(SUCCESSFUL);
            } catch (Exception e) {
                log.error(FALIE_LOG_FUL, e);
            } finally {
                if (excelWriter != null) {
                    excelWriter.finish();
                }
            }

    }

    @SneakyThrows
    @Override
    public Reply selectListSeniorseeInfo(IsInput param) {
        List<RequestIpAddressDistributtionNewParam> requestIpAddressDistributtionNewParams = new ArrayList<>();
        for (Integer Ipid:param.getIds()) {
            param.setId(Ipid);
            List<Integer> integers = new ArrayList<>();
            integers.add(param.getId());
            String iptype = IPV4_JUDGE;
            if (param.isIdType()) {
                iptype = IPV6_JUDGE;
            }
            Integer parendId = 0;
            if (iptype == IPV4_JUDGE) {
                parendId = ipAddressManagePowerTableDao.selectIpParent(param.getId());
                parendId = ipAddressManagePowerTableDao.selectIpAddressManageTableParam(parendId).getParentId();
            } else {
                parendId = ipAddressManagePowerTableDao.selectIpvsixParent(param.getId());
                parendId = ipAddressManagePowerTableDao.selectIpv6AddressManageTableParam(parendId).getParentId();
            }

            Integer countIp = ipAddressManagePowerTableDao.selectCountDistribution(integers, iptype, 0);
            if (countIp > 0) {
                return Reply.fail(UN_DISTRI_IP);
            } else {


                for (Integer s : integers) {
                        Map<String, Integer> primary = ipAddressManagePowerTableDao.selectPrimary(s, iptype,param.getBangDistri());
                        RequestIpAddressDistributtionNewParam requestIpAddressDistributtionNewParam = new RequestIpAddressDistributtionNewParam();
                        if (primary == null) {
                            return Reply.fail(FALIE_NUMBER_FUL);
                        }
                        iptype =IPV6_JUDGE;
                        if (primary.get(PRIMARY_IP_TYPE) == 0) {
                            iptype =IPV4_JUDGE;
                        }
                        List<Integer> test = new ArrayList<>();
                        test.add(primary.get(PRIMARY_IP));
                        List<IpAddressManageTableParam> tree1 = selectAllTree(test, iptype);
                        List<String> tree = new ArrayList<>();
                        List<Integer> treeId = new ArrayList<>();
                        for (IpAddressManageTableParam node:tree1) {
                            tree.add(node.getLabel());
                            treeId.add(node.getId());
                        }
                        Collections.reverse(tree);
                        Collections.reverse(treeId);
                        List<String> upTree = new ArrayList<>();
                        List<String> downTree = new ArrayList<>();
                        List<Integer> upTreeId = new ArrayList<>();
                        List<Integer> downTreeId = new ArrayList<>();
                        Integer length = tree.size();

                        for (int i = 0; i < tree.size() - 1; i++) {
                            if (i >= length - 2 && length - 2 > 0) {
                                downTree.add(tree.get(i));
                                downTreeId.add(treeId.get(i));
                            } else {
                                upTree.add(tree.get(i));
                                upTreeId.add(treeId.get(i));
                            }
                        }
                        ResponIpDistributtionParam responIpDistributtionParam = ipAddressManagePowerTableDao.selectResponIpDistributtionParam(primary.get(PRIMARY_IP), iptype);
                        if (responIpDistributtionParam == null) {
                            responIpDistributtionParam = new ResponIpDistributtionParam();
                            responIpDistributtionParam.setId(s);
                            responIpDistributtionParam.setIdType(primary.get(PRIMARY_IP_TYPE) == 0);
                        }
                        responIpDistributtionParam.setIdType(param.isIdType());
                        List<Check> maps = new ArrayList<>();

                        maps.addAll(ipAddressManagePowerTableDao.selectCheck(responIpDistributtionParam.getId(), iptype));
                        responIpDistributtionParam.putradom(maps);

                        List<Label> labels = ipAddressManagePowerTableDao.selectLabel(s, Level_BASCIS, iptype ==IPV4_JUDGE ? DataType.IP.getName() : DataType.IPV6.getName());
                        List<Label> labelList = new ArrayList<>();
                        for (Label label : labels) {
                            label.setLabelIpId(integers.get(0));
                            label.setLabelIpType(param.isIdType() == false ? 0 : 1);
                            if (label.getInputFormat().equals(INPUT_TYPE_TWO)) {
                                SimpleDateFormat df = new SimpleDateFormat(TIME_STATUS);
                                label.setDateTagboard(df.parse(label.getTestValue()));
                            } else if (label.getInputFormat().equals(INPUT_TYPE_THERE)) {
                                label.setDropTagboard(label.getLabelDropId());
                            } else if (label.getInputFormat().equals(INPUT_TYPE_ONE)) {
                                label.setTagboard(label.getTestValue());
                            }
                            List<LabelCheck> checks = ipAddressManagePowerTableDao.browDrop(label.getLabelDrop());
                            label.setLabelChecks(checks);
                            labelList.add(label);
                        }

                        /*获取基础信息*/
                        RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam = salectSenr(param.getId(), param.isIdType(), primary, iptype,param.getBangDistri());
                        /*获取树状节点*/
                        List<ResponIpDistributtionNewParentParam> responIpDistributtionNewParentParams = new ArrayList<>();
                        /*首届点树*/
                        ResponIpDistributtionNewParentParam responIpDistributtionNewParentParam = new ResponIpDistributtionNewParentParam();
                        responIpDistributtionNewParentParam.setAttrParam(labelList);
                        /*装载树形结构*/
                        List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = new ArrayList<>();
                        /*装载首届点*/
                        ResponIpDistributtionNewParam responIpDistributtionNewParam = new ResponIpDistributtionNewParam();
                        responIpDistributtionNewParam.setId(-1);
                        responIpDistributtionNewParam.setIsfz(0);
                        if (downTree != null && downTree.size() > 0) {
                            responIpDistributtionNewParam.setLabel(downTree.size() > 0 ? downTree.get(0) : upTree.get(0));
                        }
                        List<ResponIpDistributtionNewParam> responIpDistributtionNewParamList = createChildren(primary, parendId, null, param);
                        if (responIpDistributtionNewParamList != null && responIpDistributtionNewParamList.size() > 0) {
                            responIpDistributtionNewParam.setChildren(responIpDistributtionNewParamList);
                        }
                        responIpDistributtionNewParams.add(responIpDistributtionNewParam);
                        Integer num = calucateNum(responIpDistributtionNewParam,0);
                        /*便利得下层节点*/
                        /*装载树形节点*/

                        responIpDistributtionNewParentParam.setTreeData(responIpDistributtionNewParams);
                        responIpDistributtionNewParentParam.setUpTree(upTree);
                        responIpDistributtionNewParentParam.setUpTreeIds(upTreeId);
                        responIpDistributtionNewParentParam.setDownTreeIds(downTreeId);
                        responIpDistributtionNewParentParam.setDownTree(downTree);
                        responIpDistributtionNewParentParam.setNum(num);
                        responIpDistributtionNewParentParams.add(responIpDistributtionNewParentParam);
                        requestIpAddressDistributtionNewParam.setBangDistri(param.getBangDistri());
                        /*装载总结构*/
                        requestIpAddressDistributtionNewParam.setRequestIpAddressDistributtionSeniorParam(requestIpAddressDistributtionSeniorParam);
                        requestIpAddressDistributtionNewParam.setResponIpDistributtionParams(responIpDistributtionNewParentParams);
                        requestIpAddressDistributtionNewParams.add(requestIpAddressDistributtionNewParam);
                    }



            }
        }


        return Reply.ok(requestIpAddressDistributtionNewParams);
    }

    @Override
    @Async
    public Reply history() {
        //转化分配数据

        List<IpamOperHistoryDTO>  ipamProcessHistoryDTOS =ipAddressManagePowerTableDao.selectAplicantGroupbyApplicant(0,-1,1);
        for (IpamOperHistoryDTO i :ipamProcessHistoryDTOS) {
            Integer sourceId = i.getRlistId();
            Integer sourceIdType = i.getType();
            Date date = i.getCreateDate();
            IpamProcessHistoryDTO ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicantById(i.getApplicant().toString());
            List<IpamOperHistoryDTO>  operHistoryDTOS =ipAddressManagePowerTableDao.selectAplicantGroupbyApplicant(0,i.getApplicant(),0);
            List<String> ipaddress  = new ArrayList<>();
            List<Map<String,Object>> map = new ArrayList<>();
            List<Map<String,Object>> descList = new ArrayList<>();
            String uuid = UUIDUtils.getUUID();
            List<Label> labels = ipAddressManagePowerTableDao.selectLabel(i.getApplicant(), Level_SENIOR, DataType.IPHIS.getName());
            Map<String,String> newLabel = new HashMap<>();
            String parenid = "";
            for (Label key:labels) {
                newLabel.put(key.getLabelName(),key.getDropValue());
            }
            for (IpamOperHistoryDTO ipamOperHistoryDTO:operHistoryDTOS) {
                try{
                    Map<String,Object> desc = new HashMap<>();
                    String string = ipAddressManagePowerTableDao.selectIPaddressById(ipamOperHistoryDTO.getRlistId(),ipamOperHistoryDTO.getIpType()==true?1:0);
                    ipaddress.add(string);
                    String kilparent = "";
                    if (!ipamOperHistoryDTO.getIpType()){
                        kilparent = ipAddressManagePowerTableDao.selectIpParent(ipamOperHistoryDTO.getRlistId()).toString();
                    }else {
                        kilparent = ipAddressManagePowerTableDao.selectIpvsixParent(ipamOperHistoryDTO.getRlistId()).toString();
                    }
                    Map<String,Object> mapObject = new HashMap<>();
                    if (kilparent.equals(parenid)){
                        sourceId = ipamOperHistoryDTO.getRlistId();
                        sourceIdType = ipamOperHistoryDTO.getIpType()==false?0:1;
                        mapObject = getHisToMap(ipamOperHistoryDTO,ipamOperHistoryDTO.getRlistId(),ipamOperHistoryDTO.getIpType()==false?0:1);
                    }else {
                        parenid=kilparent;
                        mapObject = getHisToMap(ipamOperHistoryDTO,sourceId,sourceIdType);
                    }

                    map.add(mapObject);
                    desc.put(BAND_IP_ID,ipamOperHistoryDTO.getRlistId());
                    desc.put(BAND_IP_TYPE,ipamOperHistoryDTO.getIpType()==true?1:0);
                    desc.put(DESCRIPTION,ipamOperHistoryDTO.getDescript());
                    descList.add(desc);
            }catch (Exception e){


                }
            }
            ipAddressManagePowerTableDao.insertOperHis(JSONObject.toJSONString(map),null,JSONObject.toJSONString(newLabel),null,uuid,date,1,ipamProcessHistoryDTO.getApplicantDate(),null,0,JSONObject.toJSONString(descList),null,ipaddress.toString(),i.getCreator(),i.getBangDistri());
        }


        List<IpamOperHistoryDTO>  ipamProcessHistoryDTOT =ipAddressManagePowerTableDao.selectAplicantGroupbyApplicant(1,-1,1);
        for (IpamOperHistoryDTO i :ipamProcessHistoryDTOT) {
            Integer sourceId = i.getRlistId();
            Integer sourceIdType = i.getType();
            Date date = i.getCreateDate();
            List<IpamOperHistoryDTO>  operHistoryDTOS =ipAddressManagePowerTableDao.selectAplicantGroupbyApplicant(1,i.getApplicant(),0);
            IpamProcessHistoryDTO ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicantById(i.getApplicant().toString());
            List<String> ipaddress  = new ArrayList<>();
            List<Map<String,Object>> map = new ArrayList<>();
            List<Map<String,Object>> descList = new ArrayList<>();
            String uuid = UUIDUtils.getUUID();
            String parenid = "";
            List<Label> labels = ipAddressManagePowerTableDao.selectLabel(i.getApplicant(), Level_SENIOR, DataType.IPHIS.getName());
            Map<String,String> newLabel = new HashMap<>();
            for (Label key:labels) {
                newLabel.put(key.getLabelName(),key.getDropValue());
            }
            for (IpamOperHistoryDTO ipamOperHistoryDTO:operHistoryDTOS) {
                try {
                    Map<String, Object> desc = new HashMap<>();
                    String string = ipAddressManagePowerTableDao.selectIPaddressById(ipamOperHistoryDTO.getRlistId(), ipamOperHistoryDTO.getIpType() == true ? 1 : 0);
                    ipaddress.add(string);
                    String kilparent = "";
                    if (!ipamOperHistoryDTO.getIpType()) {
                        kilparent = ipAddressManagePowerTableDao.selectIpParent(ipamOperHistoryDTO.getRlistId()).toString();
                    } else {
                        kilparent = ipAddressManagePowerTableDao.selectIpvsixParent(ipamOperHistoryDTO.getRlistId()).toString();
                    }
                    Map<String, Object> mapObject = new HashMap<>();
                    if (kilparent.equals(parenid)) {
                        sourceId = ipamOperHistoryDTO.getRlistId();
                        sourceIdType = ipamOperHistoryDTO.getIpType() == false ? 0 : 1;
                        mapObject = getHisToMap(ipamOperHistoryDTO, ipamOperHistoryDTO.getRlistId(), ipamOperHistoryDTO.getIpType() == false ? 0 : 1);
                    } else {
                        parenid = kilparent;
                        mapObject = getHisToMap(ipamOperHistoryDTO, sourceId, sourceIdType);
                    }
                    map.add(mapObject);
                    desc.put(BAND_IP_ID, ipamOperHistoryDTO.getRlistId());
                    desc.put(BAND_IP_TYPE, ipamOperHistoryDTO.getIpType() == true ? 1 : 0);
                    desc.put(DESCRIPTION, ipamOperHistoryDTO.getDesc());
                    descList.add(desc);
                }catch (Exception e){}
            }
            ipAddressManagePowerTableDao.insertOperHis(null,JSONObject.toJSONString(map),null,JSONObject.toJSONString(newLabel),uuid,date,2,ipamProcessHistoryDTO.getApplicantDate(),null,0,JSONObject.toJSONString(descList),null,ipaddress.toString(),i.getCreator(),i.getBangDistri());
        }

        List<Map<String,Object>> clean = ipAddressManagePowerTableDao.selectClean();
        for (Map<String,Object> map:clean) {

            List<Integer> id = new ArrayList<>();
            JSONArray dis_ipaddress = new JSONArray();
            String group_id = UUIDUtils.getUUID();
            Date oper_time =  (Date) map.get(MAP_OPER_TIME);
            List<Map<String,Object>> his = ipAddressManagePowerTableDao.selectIPdisOperHisByOperTimt(oper_time);
            Date applicant_time = new Date();
            JSONArray ip_discription = new JSONArray();
            List<String> ipaddress_relation = new ArrayList<>();
            String oper_user = "";
            String bang_distri = "";
            String disMap  = "";
            for (Map<String,Object> cleanMap: his) {
                try{
                    oper_user = (String) cleanMap.get(MAP_APPLICANTOR_ENGLISH);
                    bang_distri = (String) cleanMap.get(BANG_DISTRI);
                    applicant_time = (Date) cleanMap.get(MAP_APPLICANT_DATE);
                    Map<String,Object> map1 = new HashMap<>();
                    id.add(Integer.parseInt(cleanMap.get(IP_ID).toString()));
                    String bill =  cleanMap.get(IPADDRESS_RELATION).toString().replace("[","").replace("]","");
                    List<String> ipadress = Arrays.asList(bill.split(","));
                    ipaddress_relation.addAll(ipadress);
                    ip_discription.addAll(JSONArray.parseArray(cleanMap.get(MAP_IP_DISCRIPTION).toString()));
                    dis_ipaddress.addAll(JSONArray.parseArray(cleanMap.get(DIS_IPADDRESS).toString()));
                    disMap= cleanMap.get(MAP_KEY_VALUE).toString();
                }catch (Exception e){

                }
            }
            ipAddressManagePowerTableDao.insertOperHis(dis_ipaddress.toJSONString(),null,disMap,null,group_id,oper_time,1,applicant_time,null,0,ip_discription.toJSONString(),null,ipaddress_relation.toString(),oper_user,bang_distri);
            ipAddressManagePowerTableDao.deleteOperHis(id);
        }
        //转化回收数据

        return null;
    }

    @Override
    public Reply changeList(IsInput isInput) {
        List<QueryIpAddressManageListParam> list = new ArrayList<>();
        PageHelper.startPage(isInput.getPageNumber(), isInput.getPageSize());
        list = ipAddressManagePowerTableDao.selectDisHave();
        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);


        return Reply.ok(pageInfo);
    }

    @Override
    @Transactional
    public void changeRes(List<RequestIpAddressDistributtionNewParam> params) throws ParseException {
        Integer i = 0;
        String operNum = UUIDUtils.getUUID();

        for (RequestIpAddressDistributtionNewParam param:params) {
            List<ResponIpDistributtionNewParam>  list =param.getResponIpDistributtionParams().get(0).getTreeData().get(0).getChildren();
            List<Integer> integers = new ArrayList<>();
            if (param.getResponIpDistributtionParams().get(0).getTreeData().get(0).getChildren().size()==1){
                integers.add(param.getResponIpDistributtionParams().get(0).getTreeData().get(0).getChildren().get(0).getId());
                param.getIsInput().setIds(integers);
                param.getIsInput().setId(integers.get(0));
                changeParam(param,operNum);
            }else if(param.getResponIpDistributtionParams().get(0).getTreeData().get(0).getChildren().size()>1){
                for (ResponIpDistributtionNewParam r:list) {
                    integers = new ArrayList<>();
                    List<ResponIpDistributtionNewParam> k =new ArrayList<>();
                    k.add(r);
                    param.getResponIpDistributtionParams().get(0).getTreeData().get(0).setChildren(k);
                    integers.add(param.getResponIpDistributtionParams().get(0).getTreeData().get(0).getChildren().get(0).getId());
                    param.getIsInput().setIds(integers);
                    param.getIsInput().setId(integers.get(0));
                   changeParam(param,operNum);
                }
            }

        }
    }

    @Override
    public Reply inversion(IsInput id) {
        List<Integer> ids = id.getIds();

        List<Integer> fId = ipAddressManagePowerTableDao.selectFIPaddress(ids);

        //验证每一个倒置的 地址段
        Boolean having = false;
        if (having){

        }else {

        }

        return null;
    }

    @Override
    public void relation(RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam, ResponIpDistributtionNewParam r) {
        Integer mainId = r.getMainId();
        Integer mainIdType = r.getMainIdType();
        String bangDistri = r.getBangDistri();
        if (bangDistri==null||bangDistri.equals("")){
            bangDistri = "-1";
        }
        List<Map<String, Object>> maps = new ArrayList<>();
        List<Map<String, Object>> descMap = new ArrayList<>();
        List<String> IpAddress = new ArrayList<>();
        List<Map<String,Object>> mapList = ipAddressManagePowerTableDao.getRelation(mainId,mainIdType,bangDistri);
        deleteDataNum(mapList,requestIpAddressDistributtionSeniorParam,bangDistri,r);
        List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = new ArrayList<>();
        responIpDistributtionNewParams.add(r);
     /*   List<Map<String, Object>> map = splitIPRelation(IpAddress,r,bangDistri);*/
        List<Map<String, Object>> mapl = OrganizeIpData(IpAddress, descMap, responIpDistributtionNewParams, -1, -1, false, false, 0);
        //System.out.println(mapl);
       /* createNewDistributtionRecord(mapl, requestIpAddressDistributtionSeniorParam,bangDistri);
        IpamProcessHistoryDTO ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicant(r.getId(), r.getIdType() == false ? "ipv4" :IPV6_JUDGE,r.getBangDistri());
        List<Label> labels = ipAddressManagePowerTableDao.selectLabel(ipamProcessHistoryDTO.getId(), Level_SENIOR, DataType.IPHIS.getName());

        //删除原先对应关系和选线

        List<String> ipaddress = new ArrayList<>();
        *//*描述信息整合*//*
        Map<String, Object> objectMap = new HashMap<>();
        for (Map<String, Object> map : descMap) {
            String key = "";
            if ((boolean) map.get(BAND_IP_TYPE)) {
                key = DISTINGUISH_IPV6;
            } else {
                key = DISTINGUISH_IPV4;
            }
            key = key + map.get(BAND_IP_ID);
            objectMap.put(key, map.get(DESCRIPTION));
        }

        OrganizeIpData(ipaddress, descMap, responIpDistributtionNewParams, -1, -1, false, false, 0);
        createdNewHis(ipaddress, requestIpAddressDistributtionSeniorParam.getAttrData(),requestIpAddressDistributtionSeniorParam , objectMap, 0,r.getBangDistri());

        try {
            //放入测试
            createdIPScanHIS(maps, ipaddress);
        } catch (Exception e) {

        }*/
    }

    @Override
    public Reply getIpAddressListDes(List<String> ipAddressList) {
        Map<String,Integer> map = new HashMap<>();
        for (String s: ipAddressList) {
            map.put(s,ipAddressManagePowerTableDao.selectIPaddressId(s,1));
        }
        Map<String,Object> reslut = new HashMap<>();
        List<ResIpStatusDesc> resIpStatusDescs = new ArrayList<>();
        //查询des
        for (String s:map.keySet()) {
            ResIpStatusDesc resIpStatusDesc = new ResIpStatusDesc();
            Map<String,Object> kill = new HashMap<>();
            Integer id = map.get(s);
            List<Map<String,Object>> maps = ipAddressManagePowerTableDao.getDes(id);
            int oper = 0;
            String desc = "";
            Map<String ,Object> ipBelongNow = new HashMap<>();
            List<Map<String,Object>> mapList = new  ArrayList<>();
            List<ResIpStatusDesc> resIpStatusDescList = new ArrayList<>();
            for (Map<String,Object> map1:maps) {
                Map<String,Object> res = new HashMap<>();
                ResIpStatusDesc ipStatusDesc = new ResIpStatusDesc();
                String key = "";
                if (oper==0){
                    oper = (int) map1.get("oper_int");
                }
                Integer nowOper = (int) map1.get("oper_int");
                JSONArray jsonArray = JSONArray.parseArray(map1.get("ip_discription").toString());
                Map<String ,Object> ipBelong = JSONObjectToMap(map1.get("map_key_value").toString());
                for (Object o:jsonArray) {
                    Map<String ,Object> ip = JSONObjectToMap(o.toString());
                    Integer ids = (Integer) ip.get("bandIp");
                    if (ids.equals(id)){
                        String nowDes = (String) ip.get("desc");
                        if (desc.equals("")){
                            desc = nowDes;
                            ipBelongNow= ipBelong;
                            if (desc.equals("")){
                                desc = "-1";
                            }
                        }
                        ipStatusDesc.setIpAddress(s);
                        ipStatusDesc.setNowDes(nowDes);
                        ipStatusDesc.setNowStatus(nowOper);
                        ipStatusDesc.setMapList(ipBelong);
                        resIpStatusDescList.add(ipStatusDesc);
                        break;
                    }
                }
            }
            resIpStatusDesc.setIpAddress(s);
            resIpStatusDesc.setNowStatus(oper);
            resIpStatusDesc.setNowDes(desc);
            resIpStatusDesc.setMapList(ipBelongNow);
            resIpStatusDesc.setResIpOldStatusDescs(resIpStatusDescList);
            resIpStatusDescs.add(resIpStatusDesc);
            reslut.put(s,kill);
        }


        return Reply.ok(resIpStatusDescs);
    }

    @Override
    public Reply getIpSign() {

        //初始化本机函数
        checkNumber();


        List<IpAllRequestBody> ipAllRequestBodies = ipAddressManagePowerTableDao.getIpSign(null);
        return Reply.ok(ipAllRequestBodies);
    }

    @Override
    public Reply createIpsign(IpAllRequestBody ipAllRequestBody) {
        ipAddressManagePowerTableDao.insertIpSign(ipAllRequestBody);
        return Reply.ok("新增完成");
    }

    @Override
    public Reply deleteIpsign(IpAllRequestBody ipAllRequestBody) {
        ipAddressManagePowerTableDao.deleteIpsign(ipAllRequestBody.getSignId());
        return Reply.ok("新增完成");
    }

    @Override
    public   Reply excelImportDesc(MultipartFile file, Integer signId) throws IOException {

        List<String> chekIp = new ArrayList<>();
        byte[] byteArr = file.getBytes();
        InputStream inputStream = new ByteArrayInputStream(byteArr);

        if (signId==null){
            signId = 1;
        }
        //获取工作簿
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        //加载
        List<Map<String, Object>> maps = ExeclDesc(workbook, chekIp);
         ipAddressManagePowerTableDao.updateOperHistory(maps,signId);

        return Reply.ok("成功");
    }

    @Override
    public Reply execlImport(MultipartFile file, Integer signId) throws IOException {
        List<String> chekIp = new ArrayList<>();
        byte[] byteArr = file.getBytes();
        InputStream inputStream = new ByteArrayInputStream(byteArr);

        if (signId==null){
            signId = 1;
        }
        //获取工作簿
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
        //加载
        List<Map<String, Object>> maps = ExeclAll(workbook, chekIp);
        ipAddressManagePowerTableDao.updateOtherBelong(maps,signId);

        return Reply.ok("成功");
    }

    @Override
    public Reply getIpAddressOrg(AddUpdateIpAddressManageParam qParam) {
        if (qParam.getIpAddresses()==null||qParam.getIpAddresses().equals("")){
            return Reply.fail("对不起，IP地址不能为空");
        }
        String ipaddress =qParam.getIpAddresses();
        List<String> stringList = new ArrayList<>();
        stringList.add(ipaddress);
        List<IpManageTree> LinkId = ipAddressManagePowerTableDao.selectParenIds(stringList);
        IpAddressManageTableParam table1Param = new IpAddressManageTableParam();
        Map<String,Object> map = new HashMap<>();
        if (LinkId.size()>0){
            IpAddressManageTableParam va = new IpAddressManageTableParam();
            va.setId(LinkId.get(0).getId());
            table1Param = mwIpAddressManageTableDao.selectIpAddressById1(va);
            List<OrgDTO> orgDTOS = table1Param.getOrgIds();
            List<List<Integer>> orgDTOSId = new ArrayList<>();
            for (OrgDTO o:orgDTOS) {
                List<Integer> integers = new ArrayList<>();
                integers.add(o.getOrgId());
                orgDTOSId.add(integers);
            }
            map.put(ORG_IDS,orgDTOSId);

            List<GroupDTO> groupIds = table1Param.getGroupIds();
            List<Integer> groupIdsList = new ArrayList<>();
            for (GroupDTO g:groupIds) {
                groupIdsList.add(g.getGroupId());
            }
            map.put(GROUP_IDS,groupIdsList);


            List<cn.mw.monitor.ipaddressmanage.dto.UserDTO> principal = table1Param.getPrincipal();
            List<Integer> principalList = new ArrayList<>();
            for (cn.mw.monitor.ipaddressmanage.dto.UserDTO u:principal) {
                principalList.add(u.getUserId());
            }
            map.put(PRINCIPAL,principalList);

        }



        return Reply.ok(map);
    }

    private List<Map<String, Object>> ExeclAll(XSSFWorkbook workbook, List<String> chekIp) {

        List<Map<String, Object>> maps = new ArrayList<>();

        //获取第一个工作表

        XSSFSheet hs = workbook.getSheetAt(0);

        //获取Sheet的第一个行号和最后一个行号
        int last = hs.getLastRowNum();
        int first = hs.getFirstRowNum();
        //遍历获取单元格里的信息

        for (int i = first + 1; i < 100000; i++) {
            XSSFRow row = hs.getRow(i);
            if (row == null) {
                i = 1000001;
                continue;
            }
            boolean saveMap = true;
            int firstCellNum = 0;//获取所在行的第一个行号
            int lastCellNum = row.getLastCellNum();//获取所在行的最后一个行号
            Map<String, Object> map = new HashMap<>();
            for (int j = firstCellNum; j < firstCellNum + 9; j++) {
                XSSFCell cell = row.getCell(j);
                if (lastCellNum - firstCellNum < 1) {
                    saveMap = false;
                } else {
                    String value = " ";
                    try {
                        try {
                            cell.setCellType(CellType.STRING);
                            value = cell.getRichStringCellValue().getString();
                        } catch (Exception e) {

                        }
                        if (j == firstCellNum) {
                            map.put(IP_ADDRESS, value);
                            chekIp.add(value);
                        }
                        else if (j == firstCellNum + 2) {
                            map.put(REMARKS, value.trim());
                        }
                        else if (j == firstCellNum + 4) {
                            map.put(MAP_IP_MAC, value.trim());
                        }
                        else if (j == firstCellNum + 5) {
                            map.put(MAP_IP_VENDOR, value.trim());
                        }
                        else if (j == firstCellNum + 6 ) {
                            map.put(MAP_IP_ACCESS_EPUIP, value.trim());
                        }
                        else if (j == firstCellNum + 7) {
                            map.put(MAP_IP_ACCESS_PORT, value.trim());
                        }
                        else if (j == firstCellNum + 8) {
                            map.put(MAP_IP_ASSETS_NAME, value.trim());
                        }
                        else if (j == firstCellNum + 9) {
                            map.put(MAP_IP_ASSETS_TYPE, value.trim());
                        }

                    }catch (Exception e) {
                        j = 5;
                        saveMap = false;
                    }
                }
            }
            if (saveMap) {
                maps.add(map);
            }
        }


        return maps;
    }

    private List<Map<String, Object>> ExeclDesc(XSSFWorkbook workbook, List<String> chekIp) {
        List<Map<String, Object>> maps = new ArrayList<>();

        //获取第一个工作表

        XSSFSheet hs = workbook.getSheetAt(0);

        //获取Sheet的第一个行号和最后一个行号
        int last = hs.getLastRowNum();
        int first = hs.getFirstRowNum();
        //遍历获取单元格里的信息

        for (int i = first + 1; i < 100000; i++) {
            XSSFRow row = hs.getRow(i);
            if (row == null) {
                i = 1000001;
                continue;
            }
            boolean saveMap = true;
            int firstCellNum = 0;//获取所在行的第一个行号
            int lastCellNum = row.getLastCellNum();//获取所在行的最后一个行号
            Map<String, Object> map = new HashMap<>();
            for (int j = firstCellNum; j < firstCellNum + 2; j++) {
                XSSFCell cell = row.getCell(j);
                if (lastCellNum - firstCellNum < 1) {
                    saveMap = false;
                } else {
                    String value = " ";
                    try {
                        try {
                            cell.setCellType(CellType.STRING);
                            value = cell.getRichStringCellValue().getString();
                        } catch (Exception e) {

                        }
                        if (j == firstCellNum) {
                            map.put(IP_ADDRESS, value);
                            chekIp.add(value);
                        } else if (j == firstCellNum + 1) {
                            map.put(MAP_IP_DISCRIPTION, value);
                        }
                    }catch (Exception e) {
                        j = 5;
                        saveMap = false;
                    }
                }
            }
            if (saveMap) {
                maps.add(map);
            }
        }


        return maps;
    }

    private void checkNumber() {
        List<IpAllRequestBody> ipAllRequestBodies = ipAddressManagePowerTableDao.getIpSign(1);
        boolean init = false;
        if (ipAllRequestBodies.size()>0){
            if (!ipAllRequestBodies.get(0).getNamespace().equals(IP_ADDRESS_SIGN_TEXT)){
                insertInitIpSign();
            }
        }else {
            //初始化工具
           insertInitIpSign();
        }

    }

    private void insertInitIpSign() {
        ipAddressManagePowerTableDao.deleteIpsign(1);
        IpAllRequestBody ipAllRequestBody = new IpAllRequestBody();
        ipAllRequestBody.setId(1).setNamespace("本机");
    }



    private void deleteDataNum(List<Map<String, Object>> maps,RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam,String bangDistri,ResponIpDistributtionNewParam d) {
        /*List<ResponIpDistributtionNewParam> treeData = d.getTreeData();
        List<String> ipaddress = new ArrayList<>();
        List<List<Map<String, Object>>> mapList  = handleTree(treeData,maps,ipaddress);
        IpamProcessHistoryDTO ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicant(Integer.parseInt(maps.get(0).get(IP_ID).toString()), maps.get(0).get(IP_TYPE_ID).toString().equals("0") ? "ipv4" :IPV6_JUDGE,bangDistri);
        List<Label> labels = ipAddressManagePowerTableDao.selectLabel(ipamProcessHistoryDTO.getId(), Level_SENIOR, DataType.IPHIS.getName());
        Map<String,String> newLabel = new HashMap<>();
        for (String key:requestIpAddressDistributtionSeniorParam.keySet()) {
            for (Label label:labels) {
                if (label.getLabelDrop().equals(key)){
                    newLabel.put(label.getLabelName(),param.getRequestIpAddressDistributtionSeniorParam().get(key).toString());
                }
            }
        }



        Map<String,String> oldLabel = new HashMap<>();
        for (Label label:labels) {
            oldLabel.put(label.getLabelName(),label.getDropValue());

        }
        ipAddressManagePowerTableDao.insertOperHis(JSONObject.toJSONString(mapList.get(1)),JSONObject.toJSONString(mapList.get(0)),JSONObject.toJSONString(newLabel),JSONObject.toJSONString(oldLabel),operNum,new Date(),2,ipamProcessHistoryDTO.getApplicantDate(),null,0,JSONObject.toJSONString(mapList.get(2)),JSONObject.toJSONString(mapList.get(2)),ipaddress.toString(),iLoginCacheInfo.getLoginName(),bangStr);*/
    }



/*    private List<Map<String, Object>> splitIPRelation(List<String> ipAddress, ResponIpDistributtionNewParam responIpDistributtionNewParentParams, String bangDistri) {

        return null;
    }*/

    private Map<String, Object> getHisToMap(IpamOperHistoryDTO ipamOperHistoryDTO, Integer sourceId, Integer sourceIdType) {
        Map<String,Object> map = new HashMap<>();
        map.put(SOURCR_ID,sourceId);
        map.put(PARENT_ID,sourceId);
        map.put(SOURCE_IP_TYPE,sourceIdType==0?false:true);
        map.put(IP_TYPE_ID,ipamOperHistoryDTO.getIpType());
        map.put(PARENT_IP_TYPE,sourceIdType==0?false:true);
        map.put(IP_ID,ipamOperHistoryDTO.getRlistId());
        return map;
    }

    private List<IpDestribution> getFristChildren(seachLabelList s) {
        List<IpDestribution>  list =  new ArrayList<>();

            IpDestribution ipDestribution = new IpDestribution();
            ipDestribution.setIplistId(s.getIplist_id());
            ipDestribution.setIplistType(s.getIplist_type());
            ipDestribution.setPrimaryIp(s.getIplist_id());
            ipDestribution.setPrimaryType(s.getIplist_type());
            ipDestribution.setIpgroupId(s.getIplist_id());
            ipDestribution.setIpgroupType(s.getIplist_type());
            if (s.getChildrens().size()>0){
                List<IpDestribution>  ipDestributionList =  getChildren(s.getChildrens(),s.getIplist_id(),s.getIplist_type(),s.getIplist_id(),s.getIplist_type());
                list.addAll(ipDestributionList);
            }
            list.add(ipDestribution);

        return list;
    }
;
    private List<IpDestribution> getChildren(List<seachLabelList> l,Integer group ,Integer type,Integer pid,Integer ptype) {
        List<IpDestribution>  list =  new ArrayList<>();
        for (seachLabelList s:l) {
            IpDestribution ipDestribution = new IpDestribution();
            ipDestribution.setIpgroupId(s.getId());
            ipDestribution.setIplistId(s.getIplist_id());
            ipDestribution.setIplistType(s.getIplist_type());
            ipDestribution.setPrimaryIp(pid);
            ipDestribution.setPrimaryType(ptype);
            ipDestribution.setIpgroupId(group);
            ipDestribution.setIpgroupType(type);
            if (s.getChildrens()!=null&&s.getChildrens().size()>0){
                List<IpDestribution>  ipDestributionList =  getChildren(s.getChildrens(),group,type,s.getIplist_id(),s.getIplist_type());
                list.addAll(ipDestributionList);
            }
            list.add(ipDestribution);
        }

        return list;
    }

    private List<Map<String, Object>> ExeclIP(XSSFWorkbook workbook) {
        List<Map<String, Object>> maps = new ArrayList<>();

        //获取第一个工作表

        XSSFSheet hs = workbook.getSheetAt(0);


        //获取Sheet的第一个行号和最后一个行号
        int last = hs.getLastRowNum();
        int first = hs.getFirstRowNum();
        //遍历获取单元格里的信息

        String applicant = "";
        String applicantProces = "";

        for (int i = first + 1; i < 100000; i++) {
            XSSFRow row = hs.getRow(i);
            if (row == null) {
                i = 1000001;
                continue;
            }
            boolean saveMap = true;
            int firstCellNum = 0;//获取所在行的第一个行号
            int lastCellNum = row.getLastCellNum();//获取所在行的最后一个行号
            Map<String, Object> map = new HashMap<>();
            for (int j = firstCellNum; j < firstCellNum + 1; j++) {
                XSSFCell cell = row.getCell(j);
                String value = " ";
                try {
                    try {
                        cell.setCellType(CellType.STRING);
                        value = cell.getRichStringCellValue().getString();
                    } catch (Exception e) {
                    }
                    if (j == firstCellNum) {
                        map.put(IP_SIGN, value);
                    }
                } catch (Exception e) {
                }
            }
            if (saveMap) {
                maps.add(map);
            }
        }


        return maps;
    }

    private List<String> splotIpaddress(String ipMap) {
        List<String> ipls = new ArrayList<>();
        try {
            if (ipMap.contains("-")) {
                String[] ips = ipMap.split("-");
                String[] stratIp = ips[0].split("[.]");
                String[] endIp = ips[1].split("[.]");
                String primaryIp = stratIp[0] + "." + stratIp[1] + "." + stratIp[2];
                Integer start = Integer.parseInt(stratIp[3]);
                Integer end = Integer.parseInt(endIp[3]);
                for (int j = start; j < end + 1; j++) {
                    ipls.add(primaryIp + "." + String.valueOf(j));
                }
            } else if (ipMap.contains("、")) {
                ipls.addAll(Arrays.asList(ipMap.split("、")));
            } else if (!ipMap.trim().equals("")) {
                ipls.addAll(Arrays.asList(ipMap.split("、")));
            }
        } catch (Exception e) {

        }


        return ipls;
    }


    //全局拿ip


    private void deleteOldLabel() {
        //删除重复标签
        List<MwDropdownTable> dtos = mwLabelManageService.selectOldLabel();
        List<Integer> deleteLabelIds = new ArrayList<>();
        List<String> dropcode = new ArrayList<>();
        Map<String, List<Integer>> deleteMap = new HashMap<>();
        Map<String, Integer> olderSelect = new HashMap<>();
        for (MwDropdownTable dto : dtos) {
            if (deleteMap.get(dto.getDropValue() + dto.getDropCode()) == null) {
                List<Integer> strings = new ArrayList<>();
                strings.add(dto.getDropId());
                deleteMap.put(dto.getDropValue() + dto.getDropCode(), strings);
                olderSelect.put(dto.getDropValue() + dto.getDropCode(), dto.getDropId());
            } else {
                List<Integer> strings = deleteMap.get(dto.getDropValue() + dto.getDropCode());
                strings.add(dto.getDropId());
                deleteMap.put(dto.getDropValue() + dto.getDropCode(), strings);
            }
        }
        for (String s : deleteMap.keySet()) {
            Integer updateId = olderSelect.get(s);
            List<Integer> strings = deleteMap.get(s);
            List<Integer> delete = new ArrayList<>();
            for (int i = 0; i < strings.size(); i++) {
                if (!strings.get(i).equals(updateId)) {
                    delete.add(strings.get(i));
                }
            }
            mwLabelManageService.updateDeleteById(updateId);
            try {
                if (delete.size() > 0) {
                    mwLabelManageService.updateById(delete, updateId);
                    mwLabelManageService.deleteById(delete);
                }
            } catch (Exception e) {
                log.error(FALIE_FUL + e.toString());
            }


        }

    }

    private List<ResponIpDistributtionNewParam> hanleMap(Map<String, Object> map) {
        List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = new ArrayList<>();
        String ipMap = map.get(IP_LIST).toString();
        if (ipMap.contains("-")) {
            responIpDistributtionNewParams.addAll(changeStringtoTree(ipMap, map));
        } else {
            String[] ipss = ipMap.split("、");
            List<String> ips = new ArrayList<>();
            for (int i = 0; i < ipss.length; i++) {
                ips.add(i, ipss[i]);
            }
            if (ipss.length == 1) {
                responIpDistributtionNewParams.addAll(createResponIpDistributtionNewParam(ips, map));
            } else {
                String[] stratIp = ips.get(0).split("[.]");
                String[] endIp = ips.get(1).split("[.]");
                String primaryIp = stratIp[0] + "." + stratIp[1] + "." + stratIp[2];
                String belongIp = endIp[0] + "." + endIp[1] + "." + endIp[2];
                if (primaryIp.equals(belongIp)) {
                    responIpDistributtionNewParams.addAll(createResponIpDistributtionNewParam(ips, map));
                } else {
                    responIpDistributtionNewParams.addAll(changeRe(ipMap, map));
                }
            }


        }

        return responIpDistributtionNewParams;
    }


    private Collection<? extends ResponIpDistributtionNewParam> changeRe(String ipMap, Map<String, Object> map) {
        List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = new ArrayList<>();
        String[] ipss = ipMap.split("、");
        List<String> ips = new ArrayList<>();
        List<String> iplist = new ArrayList<>();
        for (int i = 0; i < ipss.length; i++) {
            ips.add(i, ipss[i]);
        }


        List<String> desc = new ArrayList<>();
        //去除描述信息
        int index = 0;
        for (String ip : ips) {
            if (ip.contains("（")) {
                String[] descr = ip.split("（");
                String[] descri = descr[1].split("）");
                desc.add(index, descri[0]);
                iplist.add(index, descr[0]);
            } else {
                iplist.add(index, ip);
                desc.add(index, ELEMENT);
            }
            index++;
        }

        List<Map<String, Object>> mapList = mwIpAddressManageListTableDao.selectMapString(iplist, 0);
        if (mapList.size() != ips.size()) {
            return new ArrayList<>();
        } else {
            ResponIpDistributtionNewParam responIpDistributtionNewParam = new ResponIpDistributtionNewParam();
            List<ResponIpDistributtionNewParam> responIpDistributtionNewParamschildren = new ArrayList<>();
            for (int i = 0; i < iplist.size(); i++) {
                Map<String, Object> maps = retrunMap(mapList, iplist.get(i));
                if (i == 0) {
                    responIpDistributtionNewParam.setId((Integer) maps.get(IP_ID));
                    responIpDistributtionNewParam.setIdType(false);
                    responIpDistributtionNewParam.setKeyTestValue((String) maps.get(IP_ADDRESS_MAP));
                    responIpDistributtionNewParam.setRedomId(UUIDUtils.getUUID());
                    responIpDistributtionNewParam.setIsfz(1);
                    if (!desc.get(i).equals(ELEMENT)) {
                        ResponIpDistributtionNewParam distributtionNewParam = new ResponIpDistributtionNewParam();
                        distributtionNewParam.setId((Integer) maps.get(IP_ID));
                        distributtionNewParam.setIdType(false);
                        distributtionNewParam.setIsfz(2);
                        distributtionNewParam.setDesc(desc.get(i));
                        responIpDistributtionNewParamschildren.add(distributtionNewParam);
                    }
                } else {
                    ResponIpDistributtionNewParam ipDistributtionNewParam = new ResponIpDistributtionNewParam();
                    ipDistributtionNewParam.setId((Integer) maps.get(IP_ID));
                    ipDistributtionNewParam.setIdType(false);
                    ipDistributtionNewParam.setIsfz(1);
                    ipDistributtionNewParam.setKeyTestValue((String) maps.get(IP_ADDRESS_MAP));
                    responIpDistributtionNewParam.setRedomId(UUIDUtils.getUUID());
                    if (!desc.get(i).equals(ELEMENT)) {
                        ResponIpDistributtionNewParam distributtionNewParam = new ResponIpDistributtionNewParam();
                        distributtionNewParam.setId((Integer) maps.get(IP_ID));
                        distributtionNewParam.setIdType(false);
                        distributtionNewParam.setIsfz(2);
                        distributtionNewParam.setDesc(desc.get(i));
                        List<ResponIpDistributtionNewParam> responIpDistributtionNewParamschildrendesc = new ArrayList<>();
                        responIpDistributtionNewParamschildrendesc.add(distributtionNewParam);
                        ipDistributtionNewParam.setChildren(responIpDistributtionNewParamschildrendesc);
                    }
                    responIpDistributtionNewParamschildren.add(ipDistributtionNewParam);
                }


            }
            responIpDistributtionNewParam.setChildren(responIpDistributtionNewParamschildren);
            responIpDistributtionNewParams.add(responIpDistributtionNewParam);
        }
        return responIpDistributtionNewParams;
    }

    private Map<String, Object> retrunMap(List<Map<String, Object>> mapList, String s) {
        for (Map<String, Object> map : mapList) {
            if (map.get(IP_ADDRESS_MAP).toString().equals(s)) {
                return map;
            }

        }
        return null;
    }


    private List<ResponIpDistributtionNewParam> changeStringtoTree(String ipMap, Map<String, Object> map) {

        String[] ips = ipMap.split("-");
        String[] stratIp = ips[0].split("[.]");
        String[] endIp = ips[1].split("[.]");
        String primaryIp = stratIp[0] + "." + stratIp[1] + "." + stratIp[2];
        String belongIp = endIp[0] + "." + endIp[1] + "." + endIp[2];
        Integer start = Integer.parseInt(stratIp[3]);
        Integer end = Integer.parseInt(endIp[3]);
        List<String> strings = new ArrayList<>();
        for (int i = start; i < end + 1; i++) {
            strings.add(primaryIp + "." + String.valueOf(i));
        }

        List<ResponIpDistributtionNewParam> ResponIpDistributtionNewParam = createResponIpDistributtionNewParam(strings, map);

        return ResponIpDistributtionNewParam;
    }

    private List<ResponIpDistributtionNewParam> createResponIpDistributtionNewParam(List<String> strings, Map<String, Object> map) {
        List<String> iplist = new ArrayList<>();
        List<String> desc = new ArrayList<>();
        //去除描述信息
        int index = 0;
        for (String ip : strings) {
            if (ip.contains("（")) {
                String[] descr = ip.split("（");
                String[] descri = descr[1].split("）");
                desc.add(index, descri[0]);
                iplist.add(index, descr[0]);
            } else {
                iplist.add(index, ip);
                desc.add(index, ELEMENT);
            }
            index++;
        }
        List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = new ArrayList<>();
        List<Map<String, Object>> mapList = mwIpAddressManageListTableDao.selectMapString(iplist, 0);
        if (mapList.size() != strings.size()) {

            return new ArrayList<>();
        } else {

            for (int i = 0; i < iplist.size(); i++) {
                Map<String, Object> maps = retrunMap(mapList, iplist.get(i));
                ResponIpDistributtionNewParam responIpDistributtionNewParam = new ResponIpDistributtionNewParam();
                responIpDistributtionNewParam.setId((Integer) maps.get(IP_ID));
                responIpDistributtionNewParam.setIdType(false);
                responIpDistributtionNewParam.setIsfz(1);
                responIpDistributtionNewParam.setKeyTestValue((String) maps.get(IP_ADDRESS_MAP));
                responIpDistributtionNewParam.setRedomId(UUIDUtils.getUUID());
                if (!desc.get(i).equals(ELEMENT)) {
                    ResponIpDistributtionNewParam ipDistributtionNewParam = new ResponIpDistributtionNewParam();
                    ipDistributtionNewParam.setId((Integer) maps.get(IP_ID));
                    ipDistributtionNewParam.setIdType(false);
                    ipDistributtionNewParam.setIsfz(2);
                    ipDistributtionNewParam.setDesc(desc.get(i));
                    List<ResponIpDistributtionNewParam> responIpDistributtionNewParamschildrendesc = new ArrayList<>();
                    responIpDistributtionNewParamschildrendesc.add(ipDistributtionNewParam);
                    responIpDistributtionNewParam.setChildren(responIpDistributtionNewParamschildrendesc);
                }
                responIpDistributtionNewParams.add(responIpDistributtionNewParam);
            }
        }
        return responIpDistributtionNewParams;
    }


    private List<MwAssetsLabelDTO> findLabel(int i, String s, Map<String, Object> map, String radio) {
        List<MwAssetsLabelDTO> labelList = new ArrayList<>();
        List<Label> labels = ipAddressManagePowerTableDao.selectLabel(0, i, DataType.IP.getName());
        for (Label label : labels) {
            if (label.getLabelName().equals(s)) {
                MwAssetsLabelDTO labelDTO = new MwAssetsLabelDTO();
                labelDTO.setInputFormat(INPUT_TYPE_THERE);
                labelDTO.setLabelName(s);
                labelDTO.setLabelId(label.getLabelId());
                labelDTO.setDropValue(map.get(radio).toString());
                labelDTO.setTagboard(map.get(radio).toString());
                labelList.add(labelDTO);
            }
        }
        return labelList;
    }

    private List<Map<String, Object>> ExeclchangeMap(XSSFWorkbook workbook, List<String> check) throws IOException {
        List<Map<String, Object>> maps = new ArrayList<>();

        //获取第一个工作表

        XSSFSheet hs = workbook.getSheetAt(0);


        //获取Sheet的第一个行号和最后一个行号
        int last = hs.getLastRowNum();
        int first = hs.getFirstRowNum();
        //遍历获取单元格里的信息

        String applicant = "";
        String applicantProces = "";

        for (int i = first + 1; i < 100000; i++) {
            XSSFRow row = hs.getRow(i);
            if (row == null) {
                i = 1000001;
                continue;
            }
            boolean saveMap = true;
            int firstCellNum = 0;//获取所在行的第一个行号
            int lastCellNum = row.getLastCellNum();//获取所在行的最后一个行号
            Map<String, Object> map = new HashMap<>();
            for (int j = firstCellNum; j < firstCellNum + 4; j++) {
                XSSFCell cell = row.getCell(j);
                if (lastCellNum - firstCellNum < 2) {
                    saveMap = false;
                } else {
                    String value = " ";
                    try {
                        try {
                            cell.setCellType(CellType.STRING);
                            value = cell.getRichStringCellValue().getString();
                        } catch (Exception e) {

                        }
                        if (j == firstCellNum) {
                            map.put(APPLICANT, value);
                            if (value == null || value.trim().equals("")) {
                                map.put(APPLICANT, applicant);
                            } else {
                                applicant = value;
                            }
                        } else if (j == firstCellNum + 1) {
                            map.put(APPLICANT_PROCES, value);
                            if (value == null || value.trim().equals("")) {
                                map.put(APPLICANT_PROCES, applicantProces);
                            } else {
                                applicantProces = value;
                            }
                        } else if (j == firstCellNum + 2) {
                            map.put(IP_LIST, value);
                            row.createCell(firstCellNum + 4).setCellValue(SUCCESSFUL_IMPORT);
                            check.add(value);
                            if (value == null || value.trim().equals("")) {
                                saveMap = false;
                            }
                        } else if (j == firstCellNum + 3) {
                            map.put(RADIO_BUTTON, value);
                        }
                    } catch (Exception e) {
                        check.add(row.getCell(firstCellNum + 2).getRichStringCellValue().getString());
                        row.createCell(firstCellNum + 4).setCellValue(HAVE_PROBLRM_LINE);
                        row.createCell(firstCellNum + 5).setCellValue(e.toString());
                        j = 5;
                        saveMap = false;
                    }
                }
            }
            if (saveMap) {
                maps.add(map);
            }
        }


        return maps;
    }


    private List<TreeStructure> sortTree(List<Map<String, Object>> mapList, int i) {
        List<TreeStructure> treeStructures = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            if (map.get(MAP_PARENT_ID).toString().equals(String.valueOf(i))) {
                TreeStructure treeStructure = new TreeStructure();
                treeStructure.setLabel(map.get(LABEL).toString());
                treeStructure.setValue(Integer.parseInt(map.get(IP_ID).toString()));
                List<TreeStructure> treeStructureList = sortTree(mapList, Integer.parseInt(map.get(IP_ID).toString()));
                treeStructure.setChildren(treeStructureList);
                treeStructures.add(treeStructure);
            }
        }

        return treeStructures;

    }

    private void updateHis(List<String> ipAddress, List<MwAssetsLabelDTO> attrData, List<Map<String, Object>> maps, IpamProcessHistoryDTO ipamProcessHistoryDTO, RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam, Map<String, Object> objectMap,String bangDistri,Integer signId) {
        List<MwAssetsLabelDTO> mwAssetsLabelDTOS = new ArrayList<>();
        Integer applicantId = 0;
        IpamProcessHistoryDTO processHistoryDTO = new IpamProcessHistoryDTO();
        processHistoryDTO.setApplicant(requestIpAddressDistributtionSeniorParam.getApplicant());
        processHistoryDTO.setApplicantDate(requestIpAddressDistributtionSeniorParam.getApplicantionDate());
        ipAddressManagePowerTableDao.insterApplicant(processHistoryDTO);
        applicantId = processHistoryDTO.getId();
        /*mwLabelCommonServcie.deleteLabelBoard(ipamProcessHistoryDTO.getId().toString(), DataType.IPHIS.getName());*/
        mwLabelCommonServcie.insertLabelboardMapper(requestIpAddressDistributtionSeniorParam.getAttrData(), applicantId.toString(), DataType.IPHIS.getName());
        mwAssetsLabelDTOS.addAll(attrData);

        List<String> strings = ipAddress;
        for (String string : strings) {
            Boolean ip = false;
            if (isIPv4Address(string)) {
                Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(string,signId);
                mwLabelCommonServcie.deleteLabelBoard(integer.toString(), DataType.IP.getName());
                mwLabelCommonServcie.insertLabelboardMapper(mwAssetsLabelDTOS, integer.toString(), DataType.IP.getName());
                for (Map<String, Object> map : maps) {
                    if (map.get(IP_ID).equals(integer) && map.get(IP_TYPE_ID).equals(false)) {
                        IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                        ipamOperHistoryDTO.setCreateDate(new Date()).setType(0).setCreator(iLoginCacheInfo.getLoginName()).setIpType(false).setRlistId(integer).setApplicant(applicantId).setDesc(objectMap.get(DISTINGUISH_IPV4 + integer.toString()) == null ? null : objectMap.get(DISTINGUISH_IPV4 + integer.toString()).toString()).setBangDistri(bangDistri);;
                        ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);
                        ip = true;
                    }
                }
                if (!ip) {
                    String s = objectMap.get(DISTINGUISH_IPV4 + integer.toString()) == null ? null : objectMap.get(DISTINGUISH_IPV4 + integer.toString()).toString();
                    //System.out.println(applicantId.toString());
                    ipAddressManagePowerTableDao.updateApplicant(applicantId, ipamProcessHistoryDTO.getId(), integer, false, s);
                }
            } else {
                Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(string,signId);
                mwLabelCommonServcie.deleteLabelBoard(integer.toString(), DataType.IPV6.getName());
                mwLabelCommonServcie.insertLabelboardMapper(mwAssetsLabelDTOS, integer.toString(), DataType.IPV6.getName());
                for (Map<String, Object> map : maps) {
                    if (map.get(IP_ID).equals(integer) && map.get(IP_TYPE_ID).equals(true)) {
                        IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                        ipamOperHistoryDTO.setCreateDate(new Date()).setType(0).setCreator(iLoginCacheInfo.getLoginName()).setIpType(true).setRlistId(integer).setApplicant(applicantId).setDesc(objectMap.get(DISTINGUISH_IPV6 + integer.toString()) == null ? null : objectMap.get(DISTINGUISH_IPV6 + integer.toString()).toString()).setBangDistri(bangDistri);;
                        ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);
                        ip = true;
                    }
                }
                if (ip) {
                    ipAddressManagePowerTableDao.updateApplicant(applicantId, ipamProcessHistoryDTO.getApplicant(), integer, true, objectMap.get(DISTINGUISH_IPV6 + integer.toString()) == null ? null : objectMap.get(DISTINGUISH_IPV6 + integer.toString()).toString());
                }
            }

        }


    }

    private List<Map<String, Object>> getCleanMap(List<Map<String, Object>> maps, List<Map<String, Object>> mapList) {
        List<Map<String, Object>> changeMap = new ArrayList<>();
        for (Map<String, Object> newMap : maps) {
            boolean have = true;
            for (Map<String, Object> oldMap : mapList) {
                if (newMap.get(IP_ID).equals(oldMap.get(IP_ID)) && (newMap.get(IP_TYPE_ID).equals(oldMap.get(IP_TYPE_ID)))) {
                    have = false;
                }
            }
            if (have) {
                changeMap.add(newMap);
            }
        }

        return changeMap;
    }

    private List<Map<String, Object>> cleanDistributionIP(Reply reply,String dies) {
        List<RequestIpAddressDistributtionNewParam> requestIpAddressDistributtionNewParamOlds = (List<RequestIpAddressDistributtionNewParam>) reply.getData();
        RequestIpAddressDistributtionNewParam requestIpAddressDistributtionNewParamOld = requestIpAddressDistributtionNewParamOlds.get(0);
        /*先把关系和地址类分配改为为分批*/
        List<Map<String, Object>> maps = new ArrayList<>();
        List<Map<String, Object>> descMap = new ArrayList<>();
        List<String> IpAddress = new ArrayList<>();
        List<ResponIpDistributtionNewParentParam> responIpDistributtionNewParentParams = requestIpAddressDistributtionNewParamOld.getResponIpDistributtionParams();
        for (ResponIpDistributtionNewParentParam responIpDistributtionNewParentParam : responIpDistributtionNewParentParams) {
            List<ResponIpDistributtionNewParam> responIpDistributtionNewParentParamList = responIpDistributtionNewParentParam.getTreeData();
            for (ResponIpDistributtionNewParam responIpDistributtionNewParam : responIpDistributtionNewParentParamList) {
                List<Map<String, Object>> map = OrganizeIpData(IpAddress, descMap, responIpDistributtionNewParam.getChildren(), -1, -1, false, false, 0);
                maps.addAll(map);
            }
        }
        /*删除原ip地址关系*/
        deelNewDistributtionRecord(maps,dies);

        return maps;
    }

    private void deelNewDistributtionRecord(List<Map<String, Object>> maps,String getDistri) {

        List<Integer> ipv4 = new ArrayList<>();
        List<Integer> ipv6 = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            if ((boolean) map.get(IP_TYPE_ID) == false) {
                ipv4.add((Integer) map.get(IP_ID));
            } else {
                ipv6.add((Integer) map.get(IP_ID));
            }
        }
        if (ipv4.size() > 0) {
            ipAddressManagePowerTableDao.deleteDstributionByIpv4(ipv4,getDistri);
            List<Integer> ip = ipAddressManagePowerTableDao.checkStauts(ipv4,IPV4_JUDGE);
            List<Integer> delete = new ArrayList<>();
            for (Integer i:ipv4) {
                if (ip.contains(i)){

                }else {
                    delete.add(i);
                }
            }
            if (delete.size()>0) {
                ipAddressManagePowerTableDao.UpdateIPListDistributionTrue(delete, false);
            }
        }

        if (ipv6.size() > 0) {
            ipAddressManagePowerTableDao.deleteDstributionByIpv6(ipv6,getDistri);
            List<Integer> ip = ipAddressManagePowerTableDao.checkStauts(ipv6,IPV6_JUDGE);
            List<Integer> delete = new ArrayList<>();
            for (Integer i:ipv6) {
                if (ip.contains(i)){

                }else {
                    delete.add(i);
                }
            }
            if (delete.size()>0){
                ipAddressManagePowerTableDao.UpdateIPV6ListDistributionTrue(delete, false);
            }

        }

    }

    private List<ResponIpDistributtionNewParam> createChildren(Map<String, Integer> primary, Integer parendId, String desc, IsInput param) {
        List<Map<String, Object>> maps = ipAddressManagePowerTableDao.selectDIs(primary.get(PRIMARY_IP), primary.get(PRIMARY_IP_TYPE),param.getBangDistri());
        List<Map<String, Object>> parentLabel = ipAddressManagePowerTableDao.slecetParentLabel(primary.get(PRIMARY_IP), primary.get(PRIMARY_IP_TYPE));
        /*父节点位置加对应iIPd*/
        Map<String, String> mapParent = new HashMap<>();
        for (Map<String, Object> map : parentLabel) {
            if (map.get(IP_TYPE).equals(0)) {
                mapParent.put(DISTINGUISH_IPV4 + map.get(LIST_ID).toString(), map.get(LABEL).toString());
            } else {
                mapParent.put(DISTINGUISH_IPV6 + map.get(LIST_ID).toString(), map.get(LABEL).toString());
            }
        }


        IpamProcessHistoryDTO ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicant(primary.get(PRIMARY_IP), primary.get(PRIMARY_IP_TYPE) == 0 ?IPV4_JUDGE :IPV6_JUDGE,param.getBangDistri());
//        List<Map<String,String>> IpMapList =  ipAddressManagePowerTableDao.selectIPaddressMapById(maps);
        List<Map<String, Object>> mapList = new ArrayList<>();
        List<ResponIpDistributtionNewParam> responIpDistributtionNewParamList = new ArrayList<>();
        Integer iplist_id = 0;
        Integer iplist_type = 0;
        for (int i = 0; i < maps.size(); i++) {
            Map<String, Object> map = maps.get(i);
            if (map.get(MAP_IP_LIST_ID).equals(primary.get(PRIMARY_IP)) && map.get(MAP_IP_LIST_TYPE).equals(primary.get(PRIMARY_IP_TYPE))) {
                ResponIpDistributtionNewParam responIpDistributtionNewParam = new ResponIpDistributtionNewParam();
                responIpDistributtionNewParam.setId((Integer) map.get(MAP_IP_LIST_ID));
                responIpDistributtionNewParam.setMainId(primary.get(PRIMARY_IP));
                responIpDistributtionNewParam.setMainIdType(primary.get(PRIMARY_IP_TYPE));
                responIpDistributtionNewParam.setIsfz(1);
                responIpDistributtionNewParam.setRedomId(UUIDUtils.getUUID());
                responIpDistributtionNewParam.setLabel(mapParent.get((Integer) map.get(MAP_IP_LIST_TYPE) == 0 ? DISTINGUISH_IPV4 + map.get(MAP_IP_LIST_ID).toString() : DISTINGUISH_IPV6 + map.get(MAP_IP_LIST_ID).toString()));
                if (ipAddressManagePowerTableDao.selectIPaddressById((Integer) map.get(MAP_IP_LIST_ID), (Integer) map.get(MAP_IP_LIST_TYPE)) == null) {
                    responIpDistributtionNewParam.setKeyTestValue(MUST_REMOVR_IP);
                } else {
                    responIpDistributtionNewParam.setKeyTestValue(ipAddressManagePowerTableDao.selectIPaddressById((Integer) map.get(MAP_IP_LIST_ID), (Integer) map.get(MAP_IP_LIST_TYPE)));
                }

                ResponseIpAddressOperHistoryParamOB responseIpAddressOperHistoryParamOB = ipAddressManagePowerTableDao.selectOperHistoryByApplicant(ipamProcessHistoryDTO.getId(), (Integer) map.get(MAP_IP_LIST_ID));
                if (responseIpAddressOperHistoryParamOB.getDisDesc() == null) {
                    desc = null;
                } else {
                    desc = responseIpAddressOperHistoryParamOB.getDisDesc();
                    responIpDistributtionNewParam.setIsDesc(true);
                }
                if (param.isSreacType() == false) {
                    responIpDistributtionNewParam.setDesc(desc);
                    desc = null;
                }
                responIpDistributtionNewParam.setBangDistri(param.getBangDistri());
                responIpDistributtionNewParam.setParentId(parendId);
                responIpDistributtionNewParam.setIdType((Integer) map.get(MAP_IP_LIST_TYPE) == 0 ? false : true);
                responIpDistributtionNewParam.setChildDrop((Integer) map.get(MAP_IP_LIST_TYPE) == 0 ?IPV4_JUDGE  :IPV6_JUDGE);
                responIpDistributtionNewParamList.add(responIpDistributtionNewParam);
                iplist_id = (Integer) map.get(MAP_IP_LIST_ID);
                iplist_type = (Integer) map.get(MAP_IP_LIST_TYPE);
            } else {
                mapList.add(map);
            }
        }
        List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = new ArrayList<>();
        responIpDistributtionNewParams.addAll(addChildren(mapList, iplist_id, iplist_type, parendId, desc, ipamProcessHistoryDTO.getId(), param, mapParent,iplist_id,iplist_type));
        responIpDistributtionNewParamList.get(0).getChildren().addAll(responIpDistributtionNewParams);


        return responIpDistributtionNewParamList;
    }


    private List<ResponIpDistributtionNewParam> addChildren(List<Map<String, Object>> maps, Integer iplistId, Integer iplistType, Integer parendId, String desc, Integer id, IsInput param, Map<String, String> mapParent,Integer mainId,Integer mainIdtype) {
        List<ResponIpDistributtionNewParam> responIpDistributtionNewParamList = new ArrayList<>();
        if (desc != null) {
            ResponIpDistributtionNewParam responIpDistributtionNewParam = new ResponIpDistributtionNewParam();
            responIpDistributtionNewParam.setId(-iplistId);
            responIpDistributtionNewParam.setIsfz(2);
            responIpDistributtionNewParam.setParentId(parendId);
            responIpDistributtionNewParam.setDesc(desc);
            responIpDistributtionNewParam.setRedomId(UUIDUtils.getUUID());
            responIpDistributtionNewParamList.add(responIpDistributtionNewParam);
        }

        for (Map<String, Object> map : maps) {
            if (map.get(PRIMARY_IP).equals(iplistId) && map.get(PRIMARY_IP_TYPE).equals(iplistType)) {
                ResponIpDistributtionNewParam responIpDistributtionNewParam = new ResponIpDistributtionNewParam();
                responIpDistributtionNewParam.setId((Integer) map.get(MAP_IP_LIST_ID));
                responIpDistributtionNewParam.setIsfz(1);
                responIpDistributtionNewParam.setBangDistri(param.getBangDistri());
                responIpDistributtionNewParam.setRedomId(UUIDUtils.getUUID());
                responIpDistributtionNewParam.setMainId(mainId);
                responIpDistributtionNewParam.setMainIdType(mainIdtype);
                responIpDistributtionNewParam.setKeyTestValue(ipAddressManagePowerTableDao.selectIPaddressById((Integer) map.get(MAP_IP_LIST_ID), (Integer) map.get(MAP_IP_LIST_TYPE)));
                ResponseIpAddressOperHistoryParamOB responseIpAddressOperHistoryParamOB = ipAddressManagePowerTableDao.selectOperHistoryByApplicant(id, (Integer) map.get(MAP_IP_LIST_ID));
                if (responseIpAddressOperHistoryParamOB == null) {
                    desc = null;
                } else {
                    desc = responseIpAddressOperHistoryParamOB.getDisDesc();
                    responIpDistributtionNewParam.setIsDesc(true);
                }
                if (param.isSreacType() == false) {
                    responIpDistributtionNewParam.setDesc(desc);
                    desc = null;
                }
                responIpDistributtionNewParam.setParentId(parendId);
                responIpDistributtionNewParam.setLabel(mapParent.get((Integer) map.get(MAP_IP_LIST_TYPE) == 0 ? DISTINGUISH_IPV4 + map.get(MAP_IP_LIST_ID).toString() : DISTINGUISH_IPV6 + map.get(MAP_IP_LIST_ID).toString()));
                responIpDistributtionNewParam.setIdType((Integer) map.get(MAP_IP_LIST_TYPE) == 0 ? false : true);
                responIpDistributtionNewParam.setChildDrop((Integer) map.get(MAP_IP_LIST_TYPE) == 0 ?IPV4_JUDGE  :IPV6_JUDGE);
                List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = new ArrayList<>();
                responIpDistributtionNewParams.addAll(addChildren(maps, (Integer) map.get(MAP_IP_LIST_ID), (Integer) map.get(MAP_IP_LIST_TYPE), parendId, desc, id, param, mapParent,mainId,mainIdtype));
                if (responIpDistributtionNewParams.size() > 0) {
                    responIpDistributtionNewParam.getChildren().addAll(responIpDistributtionNewParams);
                }
                if (responIpDistributtionNewParams!=null){
                    responIpDistributtionNewParamList.add(responIpDistributtionNewParam);
                }

            }
        }
        return responIpDistributtionNewParamList;
    }

    private RequestIpAddressDistributtionSeniorParam salectSenr(Integer id, boolean idType, Map<String, Integer> primary, String iptype,String getDristi) throws ParseException {
        RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam = new RequestIpAddressDistributtionSeniorParam();
        /*IpAddressDistributtionSeniorParam ipAddressDistributtionSeniorParam = ipAddressManagePowerTableDao.selectDistributtionSenior(primary.get(PRIMARY_IP), iptype,getDristi);*/
        IpamProcessHistoryDTO ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicant(primary.get(PRIMARY_IP), iptype,getDristi);
        requestIpAddressDistributtionSeniorParam.setApplicant(ipamProcessHistoryDTO.getApplicant());
        requestIpAddressDistributtionSeniorParam.setApplicantionDate(ipamProcessHistoryDTO.getApplicantDate());
        List<Label> labels = ipAddressManagePowerTableDao.selectLabel(ipamProcessHistoryDTO.getId(), Level_SENIOR, DataType.IPHIS.getName());
        List<Label> labelList = new ArrayList<>();
        for (Label label : labels) {
            label.setLabelIpId(id);
            label.setLabelIpType(idType == true ? 0 : 1);
            if (label.getInputFormat().equals(INPUT_TYPE_TWO)) {
                SimpleDateFormat df = new SimpleDateFormat(TIME_STATUS);
                if (label.getTestValue()==null||label.getTestValue().equals("")){
                    label.setDateTagboard(null);
                }else {
                    label.setDateTagboard(df.parse(label.getTestValue()));
                }
            } else if (label.getInputFormat().equals(INPUT_TYPE_THERE)) {
                label.setDropTagboard(label.getLabelDropId());
            } else if (label.getInputFormat().equals(INPUT_TYPE_ONE)) {
                label.setTagboard(label.getTestValue());
            }
            List<LabelCheck> checks = ipAddressManagePowerTableDao.browDrop(label.getLabelDrop());
            label.setLabelChecks(checks);
            labelList.add(label);
        }
        requestIpAddressDistributtionSeniorParam.setAttrParam(labelList);
        requestIpAddressDistributtionSeniorParam.setApplicanttext(ipAddressManagePowerTableDao.selectUserName(ipamProcessHistoryDTO.getApplicant()));

          /*  try{
                if (ipAddressDistributtionSeniorParam.getOrgIds()==null){
                    ipAddressDistributtionSeniorParam.setOrgIds(FALSE_JUDGE);
                }
            }catch (Exception e){
                ipAddressDistributtionSeniorParam.setOrgIds(FALSE_JUDGE);
            }
        ipAddressDistributtionSeniorParam.setOrgIds(FALSE_JUDGE);*/

       /* List<List<Integer>> orgids = StringChangeList(ipAddressDistributtionSeniorParam.getOrgIds());
        requestIpAddressDistributtionSeniorParam.setOa(ipAddressDistributtionSeniorParam.getOa()).setOatext(ipAddressDistributtionSeniorParam.getOatext())
                .setOaurl(ipAddressDistributtionSeniorParam.getOaurl()).setOaurltext(ipAddressDistributtionSeniorParam.getOaurltext()).setOrgIds(orgids);
        try {
            requestIpAddressDistributtionSeniorParam.setOrgtext(Arrays.asList(ipAddressDistributtionSeniorParam.getOrgtext().substring(1, ipAddressDistributtionSeniorParam.getOrgtext().length() - 1).split(",")));
        } catch (Exception e) {

        }*/
        return requestIpAddressDistributtionSeniorParam;
    }

    private List<IpAddressManageTableParam> selectIpGrop(QueryIpAddressManageParam qParam, DataPermission dataPermission, Integer userId, String loginName) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<IpAddressManageTableParam> list = new ArrayList<>();
        switch (dataPermission) {
            case PRIVATE:
                qParam.setUserId(userId);
                if (qParam.getPageNumber() == null) {
                } else {
                    PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                }
                Map priCriteria = PropertyUtils.describe(qParam);
                list = mwIpAddressManageTableDao.selectPriIpAddress(priCriteria);
                for (IpAddressManageTableParam val :
                        list) {
                    IpAddressManageTableParam va2 = new IpAddressManageTableParam();
                    queryAddress(val, va2);
                    val.setLongitude(va2.getLongitude());
                    val.setLatitude(va2.getLatitude());
                }
                break;
            case PUBLIC:
                // List<String> orgNames = mwUserOrgMapperDao.getOrgNameByLoginName(loginName);
                List<String> orgNames = mwOrgCommonService.getOrgNamesByNodes(loginName);

                List<Integer> orgIds = new ArrayList<>();
                Boolean isAdmin = false;
                for (String orgName : orgNames) {
                    if (orgName.equals(MWUtils.ORG_NAME_TOP)) {
                        isAdmin = true;
                        break;
                    }
                }
                if (!isAdmin) {
                    //  List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
                    //  orgIds = mwUserOrgMapperDao.getOrgIdByUserId(loginName);

                    orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);

                }
                if (null != orgIds && orgIds.size() > 0) {
                    qParam.setOrgIds(orgIds);
                }
                qParam.setIsAdmin(isAdmin);
                Map priCriteria1 = PropertyUtils.describe(qParam);
                list = mwIpAddressManageTableDao.selectPubIpAddress(priCriteria1);
                break;
        }
        return list;
    }

    private void createdNewHis(List<String> ipAddress, List<MwAssetsLabelDTO> attrData, RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam, Map<String, Object> objectMap, int tyle,String getDistri,Integer signId) {
        IpamProcessHistoryDTO ipamProcessHistoryDTO = new IpamProcessHistoryDTO();
        Integer applicantId = 0;
        if (requestIpAddressDistributtionSeniorParam != null) {
            ipamProcessHistoryDTO.setApplicant(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
            ipamProcessHistoryDTO.setApplicantDate(requestIpAddressDistributtionSeniorParam.getApplicantionDate());
            ipAddressManagePowerTableDao.insterApplicant(ipamProcessHistoryDTO);
            applicantId = ipamProcessHistoryDTO.getId();
        } else {
//                boolean c = isIPv4Address(responIpDistributtionParam.getPrimaryIp());
//                ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicant(responIpDistributtionParam.getId(), c == true ?IPV4_JUDGE  :IPV6_JUDGE);
//                applicantId = ipamProcessHistoryDTO.getId();
        }
        List<MwAssetsLabelDTO> mwAssetsLabelDTOS = new ArrayList<>();
        if (tyle == 0) {
            mwAssetsLabelDTOS.addAll(attrData);
            mwAssetsLabelDTOS.addAll(requestIpAddressDistributtionSeniorParam.getAttrData());
        }

        if (tyle == 0) {
            mwLabelCommonServcie.insertLabelboardMapper(requestIpAddressDistributtionSeniorParam.getAttrData(), applicantId.toString(), DataType.IPHIS.getName());
        }
        List<String> strings = ipAddress;
        for (String string : strings) {
            if (isIPv4Address(string)) {
                Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(string,signId);
                mwLabelCommonServcie.deleteLabelBoard(integer.toString(), DataType.IP.getName());
                if (tyle == 0) {
                    mwLabelCommonServcie.insertLabelboardMapper(mwAssetsLabelDTOS, integer.toString(), DataType.IP.getName());
                }
                IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                ipamOperHistoryDTO.setBangDistri(getDistri).setCreateDate(new Date()).setType(tyle).setCreator(iLoginCacheInfo.getLoginName()).setIpType(false).setRlistId(integer).setApplicant(ipamProcessHistoryDTO.getId()).setDesc(objectMap.get(DISTINGUISH_IPV4 + integer.toString()) == null ? null : objectMap.get(DISTINGUISH_IPV4 + integer.toString()).toString());
                ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);

            } else {
                Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(string,signId);
                mwLabelCommonServcie.deleteLabelBoard(integer.toString(), DataType.IPV6.getName());
                if (tyle == 0) {
                    mwLabelCommonServcie.insertLabelboardMapper(mwAssetsLabelDTOS, integer.toString(), DataType.IPV6.getName());
                }
                IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                ipamOperHistoryDTO.setBangDistri(getDistri).setCreateDate(new Date()).setType(tyle).setCreator(iLoginCacheInfo.getLoginName()).setIpType(true).setRlistId(integer).setApplicant(ipamProcessHistoryDTO.getId()).setDesc(objectMap.get(DISTINGUISH_IPV6 + integer.toString()) == null ? null : objectMap.get(DISTINGUISH_IPV6 + integer.toString()).toString());
                ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);
            }

        }


    }

    private List<Map<String, Object>> createNewDistributtionRecord(List<Map<String, Object>> maps, RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam,String getDistri) {
        String org =  mwOrgCommonService.getOrgIdsByNodes(iLoginCacheInfo.getLoginName()).toString();
        String orgtest = "";
        if (requestIpAddressDistributtionSeniorParam.getOrgtext() != null) {
            orgtest = requestIpAddressDistributtionSeniorParam.getOrgtext().toString();
        }
        ipAddressManagePowerTableDao.insterNew(maps, requestIpAddressDistributtionSeniorParam, org, orgtest,getDistri);
        List<Integer> ipv4 = new ArrayList<>();
        List<Integer> ipv6 = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            if ((boolean) map.get(IP_TYPE_ID) == false) {
                ipv4.add((Integer) map.get(IP_ID));
            } else {
                ipv6.add((Integer) map.get(IP_ID));
            }
        }
        if (ipv4.size() > 0) {
            ipAddressManagePowerTableDao.UpdateIPListDistributionTrue(ipv4, true);
        }

        if (ipv6.size() > 0) {
            ipAddressManagePowerTableDao.UpdateIPV6ListDistributionTrue(ipv6, true);
        }

        return null;
    }

    //初始化所有ip数据
    private List<Map<String, Object>> OrganizeIpData(List<String> ipAddress, List<Map<String, Object>> descMap, List<ResponIpDistributtionNewParam> responIpDistributtionNewParentParams, int parenId, int sourceId, boolean parenIdType, boolean sourceIdType, int forInt) {

        List<Map<String, Object>> mapList = new ArrayList<>();

        for (ResponIpDistributtionNewParam responIpDistributtionNewParam : responIpDistributtionNewParentParams) {
            /*首节点非关系型参数*/
            boolean firstBoolen = false;

            Map<String, Object> map = new HashMap<>();
            if (forInt == 0) {
                parenId = responIpDistributtionNewParam.getId();
                parenIdType = responIpDistributtionNewParam.getIdType();
                sourceId = responIpDistributtionNewParam.getId();
                sourceIdType = responIpDistributtionNewParam.getIdType();
                if (responIpDistributtionNewParam.getIsfz() == 3) {
                    firstBoolen = true;
                }
            }
            //非关系型
            if (responIpDistributtionNewParam.getIsfz() == 3) {
                List<Integer> code = responIpDistributtionNewParam.getIds();
                List<String> ips = Arrays.asList(responIpDistributtionNewParam.getKeyTestValue().split(","));
                for (Integer i : code) {
                    Map<String, Object> maplist = new HashMap<>();
                    maplist.put(IP_ID, i);
                    maplist.put(IP_TYPE_ID, responIpDistributtionNewParam.getIdType());
                    if (firstBoolen) {
                        maplist.put(PARENT_ID, i);
                        maplist.put(PARENT_IP_TYPE, responIpDistributtionNewParam.getIdType());
                        maplist.put(SOURCR_ID, i);
                        maplist.put(SOURCE_IP_TYPE, responIpDistributtionNewParam.getIdType());
                        mapList.add(maplist);
                    } else {
                        maplist.put(PARENT_ID, parenId);
                        maplist.put(PARENT_IP_TYPE, parenIdType);
                        maplist.put(SOURCR_ID, sourceId);
                        maplist.put(SOURCE_IP_TYPE, sourceIdType);
                        mapList.add(maplist);
                    }
                }
                if (responIpDistributtionNewParam.getIsDesc()) {
                    List<ResponIpDistributtionNewParam> responIpDistributtionNewParams = responIpDistributtionNewParam.getChildren();
                    for (ResponIpDistributtionNewParam r : responIpDistributtionNewParams) {
                        if (r.getIsfz() == 2) {
                            for (Integer i : code) {
                                Map<String, Object> stringObjectHashMap = new HashMap<>();
                                stringObjectHashMap.put(BAND_IP_ID, i);
                                stringObjectHashMap.put(BAND_IP_TYPE, responIpDistributtionNewParam.getIdType());
                                stringObjectHashMap.put(DESCRIPTION, responIpDistributtionNewParam.getDesc());
                                descMap.add(stringObjectHashMap);
                            }
                        }
                    }
                }
                for (String s : ips) {
                    ipAddress.add(ipAddress.contains(s) ? HAVE_REPEAT_IP : s);
                }
            }
            //描述
            if (responIpDistributtionNewParam.getIsfz() == 2) {
                map.put(BAND_IP_ID, parenId);
                map.put(BAND_IP_TYPE, parenIdType);
                map.put(DESCRIPTION, responIpDistributtionNewParam.getDesc());
                descMap.add(map);
            }
            //关系型
            if (responIpDistributtionNewParam.getIsfz() == 1) {
                map.put(IP_ID, responIpDistributtionNewParam.getId());
                map.put(IP_TYPE_ID, responIpDistributtionNewParam.getIdType());
                map.put(PARENT_ID, parenId);
                map.put(PARENT_IP_TYPE, parenIdType);
                map.put(SOURCR_ID, sourceId);
                map.put(SOURCE_IP_TYPE, sourceIdType);
                ipAddress.add(ipAddress.contains(responIpDistributtionNewParam.getKeyTestValue()) ? HAVE_REPEAT_IP: responIpDistributtionNewParam.getKeyTestValue());
                mapList.add(map);
            }

            List<ResponIpDistributtionNewParam> responIpDistributtionNewParentParamList = responIpDistributtionNewParam.getChildren();
            if (responIpDistributtionNewParentParamList != null && responIpDistributtionNewParentParamList.size() > 0) {
                List<Map<String, Object>> maps = OrganizeIpData(ipAddress, descMap, responIpDistributtionNewParentParamList, responIpDistributtionNewParam.getId(), sourceId, responIpDistributtionNewParam.getIdType(), sourceIdType, 1);
                mapList.addAll(maps);
            }
        }
        return mapList;
    }

    private Boolean changeBeforeSort(IpAddressManageTableParam table1Param, boolean draggingType,
                                     boolean dropNodeType, IpAddressManageTableParam table1Paramte) {
        List<Map<String, Object>> maps = mwIpAddressManageTableDao.selectAllIpAddressByParenId(table1Param.getId(), draggingType, table1Paramte.getParentId());
        List<Map<String, Object>> changeMaps = new ArrayList<>();
        Integer countSort = maps.size() + 1;
        for (Map<String, Object> map : maps) {
            if (map.get(IPV4_JUDGE).equals(dropNodeType == true ? 1 : 0) && map.get(IP_ID).equals(table1Paramte.getId())) {
                map.put(INDEX_SORT, countSort);
                countSort--;
                Map<String, Object> mapIndex = new HashMap<>();
                mapIndex.put(IP_ID, table1Param.getId());
                mapIndex.put(INDEX_SORT, countSort);
                mapIndex.put(IPV4_JUDGE, draggingType == true ? 1 : 0);
                changeMaps.add(mapIndex);
            } else {
                map.put(INDEX_SORT, countSort);
            }
            countSort--;
            changeMaps.add(map);
        }

        for (Map<String, Object> map : changeMaps) {
            mwIpAddressManageTableDao.changeAfterSort((Integer) map.get(IP_ID), (Integer) map.get(IPV4_JUDGE) == 1, (Integer) map.get(INDEX_SORT));
        }
        return true;

    }


    private Boolean changeAfterSort(IpAddressManageTableParam table1Param,
                                    boolean draggingType, IpAddressManageTableParam table1Paramte) {
        mwIpAddressManageTableDao.changeAfterSort(table1Param.getId(), draggingType, table1Paramte.getIndexSort() + 1);
        return true;
    }


    private List<Object> getIPv4(QueryIpAddressManageParam qParam) throws
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<IpAddressManageTableParam> list = new ArrayList<>();

        List<IpAddressManageTableParam> listone = getAllIPv4(qParam, list,1,new ArrayList<>(),new ArrayList<>());
        List<Object> listte = new ArrayList<>();
        listte.addAll(listone);

        return listte;
    }

    private List<IpAddressManageTableParam> getAllIPv4(QueryIpAddressManageParam
                                                               qParam, List<IpAddressManageTableParam> list,int k,List<Integer> ipv4,List<Integer> ipv6 ) throws
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        QueryIpAddressManageParam qParams = new QueryIpAddressManageParam();
        qParams.setParentId(qParam.getParentId());
        qParams.setType(IP_GROUPING);
        qParams.setSignId(qParam.getSignId());
        List<IpAddressManageTableParam> listIP = queryIpv4(qParams);

        Collections.sort(listIP, new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                IpAddressManageTableParam s1 = JSONObject.parseObject(JSONObject.toJSONString(o1), IpAddressManageTableParam.class);
                IpAddressManageTableParam s2 = JSONObject.parseObject(JSONObject.toJSONString(o2), IpAddressManageTableParam.class);
                return s1.getIndexSort().compareTo(s2.getIndexSort());
            }
        });
        list.addAll(listIP);
        List<IpAddressManageTableParam> list1 = new ArrayList<>();
        for (int i = 0; i < listIP.size(); i++) {
            IpAddressManageTableParam ipAddressManageTableParam = listIP.get(i);
            if (ipAddressManageTableParam.getType().equals(IP_GROUPING)) {
                if (k==1){
                    ipv4.clear();
                    ipv6.clear();
                }
                qParams.setParentId(ipAddressManageTableParam.getId());
                qParams.setSignId(qParam.getSignId());
                List<IpAddressManageTableParam> list2 = getAllIPv4(qParams, list,2,ipv4,ipv6);
                List<Ipv6ManageTableParam> listOne = queryipv6(qParams);
                List<Object> te = new ArrayList<>();
                if (list2.size() > 0) {
                    te.addAll(list2);
                }
                if (listOne.size() > 0){
                    for (int j = 0; j <listOne.size() ; j++) {
                        ipv6.add(listOne.get(j).getId());
                    }
                    te.addAll(listOne);
                }
                //判断这个是否适合建立临时地址段
                if (k==1){
                    IpAddressManageTableParam ipAddressManageTableParamLin  = new IpAddressManageTableParam();
                    ipAddressManageTableParamLin.setLabel(TEMPORARY_IP_LIST);
                    List<IpAddressManageTableParam> ipAddressManageTableParams = ipAddressManagePowerTableDao.selectLin();
                    List<IpAddressManageTableParam> iPv6Lin = ipAddressManagePowerTableDao.selectIPv6Lin();
                    List<Object> lin = new ArrayList<>();
                    for (IpAddressManageTableParam ip:ipAddressManageTableParams){
                        if (ipv4.contains(ip.getId())){
                            ip.setIPv4(true);
                            ip.setId(-ip.getId());
                            ip.setTreeType(1);
                            ip.setType(TEMPORARY_IP);
                            lin.add(ip);
                        }
                    }
                    for (IpAddressManageTableParam ip:iPv6Lin){
                        if (ipv6.contains(ip.getId())){
                            ip.setIPv4(false);
                            ip.setId(-ip.getId());
                            ip.setTreeType(2);
                            ip.setType(TEMPORARY_IP);
                            lin.add(ip);
                        }
                    }
                    ipAddressManageTableParamLin.setChildren(lin);
                    ipAddressManageTableParamLin.setId(-ipAddressManageTableParam.getId());
                    ipAddressManageTableParamLin.setType(TEMPORARY_GROUP);
                    ipAddressManageTableParamLin.setIndexSort(-2);
                    te.add(ipAddressManageTableParamLin);
                }
                Collections.sort(te, new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        IpAddressManageTableParam s1 = JSONObject.parseObject(JSONObject.toJSONString(o1), IpAddressManageTableParam.class);
                        IpAddressManageTableParam s2 = JSONObject.parseObject(JSONObject.toJSONString(o2), IpAddressManageTableParam.class);
                        return s1.getIndexSort().compareTo(s2.getIndexSort());
                    }
                });


                ipAddressManageTableParam.setChildren(te);
            }
            if (!ipAddressManageTableParam.getType().equals(IP_GROUPING)){
                if (ipAddressManageTableParam.isIPv4()){
                    ipv4.add(ipAddressManageTableParam.getId());
                }else {
                    ipv6.add(ipAddressManageTableParam.getId());
                }

            }
            list1.add(ipAddressManageTableParam);
        }
        return list1;
    }


    private void addDistribution
            (List<Check> maplist, List<MwAssetsLabelDTO> attrParam, List<Check> ipv4, List<Check> ipv6, Integer
                    id, Boolean idType,String getDistri,Integer signId) {
        IpAddressDistributtionSeniorParam map = ipAddressManagePowerTableDao.selectDistributtionSenior(id, idType == true ?IPV4_JUDGE  :IPV6_JUDGE,getDistri);
        List<Integer> s = new ArrayList<>();
        List<Integer> sv = new ArrayList<>();
        for (Check string : maplist) {
            if (isIPv4Address(string.getKeyTestValue())) {
                Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(string.getKeyTestValue(),signId);
                for (Check c : ipv4) {
                    if (string.getKeyTestValue().equals(c.getKeyTestValue())) {
                        s.add(integer);
                        ipAddressManagePowerTableDao.insterDes(map, id, !idType, integer, 0, string.getParentId());
                    }
                }
                // shabi
                mwLabelCommonServcie.deleteLabelBoard(integer.toString(), DataType.IP.getName());
                mwLabelCommonServcie.insertLabelboardMapper(attrParam, integer.toString(), DataType.IP.getName());


            } else {
                for (Check c : ipv6) {
                    Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(string.getKeyTestValue(),signId);
                    if (string.getKeyTestValue().equals(c.getKeyTestValue())) {
                        sv.add(integer);
                        ipAddressManagePowerTableDao.insterDes(map, id, !idType, integer, 1, string.getParentId());
                    }
                    mwLabelCommonServcie.deleteLabelBoard(integer.toString(), DataType.IPV6.getName());
                    mwLabelCommonServcie.insertLabelboardMapper(attrParam, integer.toString(), DataType.IPV6.getName());


                }
            }
        }


        //修改地址的分配状态
        if (s.size() > 0) {
            ipAddressManagePowerTableDao.UpdateIPListDistributionTrue(s, true);
        }
        if (sv.size() > 0) {
            ipAddressManagePowerTableDao.UpdateIPV6ListDistributionTrue(sv, true);
        }


    }

    private void createdHisbyIpAddress(List<String> ipv4, List<String> ipv6, Integer ipId, Boolean ipStatus,
                                       int status,String getDistri,Integer signId) {
        IpamProcessHistoryDTO ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicant(ipId, ipStatus == true ?IPV4_JUDGE  :IPV6_JUDGE,getDistri);
        ipv4.addAll(ipv6);
        for (String string : ipv4) {
            if (isIPv4Address(string)) {
                Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(string,signId);

                IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                ipamOperHistoryDTO.setCreateDate(new Date()).setType(status).setCreator(iLoginCacheInfo.getLoginName()).setIpType(false).setRlistId(integer).setApplicant(ipamProcessHistoryDTO.getId());
                ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);
            } else {
                Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(string,signId);

                IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                ipamOperHistoryDTO.setCreateDate(new Date()).setType(status).setCreator(iLoginCacheInfo.getLoginName()).setIpType(true).setRlistId(integer).setApplicant(ipamProcessHistoryDTO.getId());
                ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);
            }

        }
    }

    private void cleanDistribution(List<String> ipv4, List<String> ipv6,String getDistri,Integer signId) {
        List<Integer> s = new ArrayList<>();
        List<Integer> sv = new ArrayList<>();
        if (ipv4.size() > 0) {
            List<Integer> integers = ipAddressManagePowerTableDao.selectIPaddress(ipv4, 1,signId);
            mwLabelCommonServcie.deleteLabelBoard(integers.toString(), DataType.IP.getName());
            ipAddressManagePowerTableDao.deleteDstributionByIpv4(integers,getDistri);
            ipAddressManagePowerTableDao.UpdateIPListDistributionTrue(integers, false);
        }
        if (ipv6.size() > 0) {
            List<Integer> integers = ipAddressManagePowerTableDao.selectIPaddress(ipv6, 1,signId);
            mwLabelCommonServcie.deleteLabelBoard(integers.toString(), DataType.IPV6.getName());
            ipAddressManagePowerTableDao.deleteDstributionByIpv6(integers,getDistri);
            ipAddressManagePowerTableDao.UpdateIPV6ListDistributionTrue(integers, false);
        }

    }

    private List<List<Integer>> StringChangeList(String s) {
        List<List<Integer>> strings = new ArrayList<>();
        if (s == FALSE_JUDGE) {

        } else {
            s = s.substring(1, s.length() - 1);

            char[] chars = s.toCharArray();
            String list = "";
            for (char c : chars) {
                char a = '[';
                char b = ']';
                if (c == a) {
                    list = "";
                } else if (c == b) {
                    String[] string = list.split(",");
                    List<Integer> stringList = new ArrayList<>();
                    for (int i = 0; i < string.length; i++) {
                        stringList.add(Integer.valueOf(string[i].trim()));
                    }

                    strings.add(stringList);
                } else {
                    list = list + c;
                }
            }
        }
        return strings;
    }

    private List<Ipv6ManageTableParam> queryipv6(QueryIpAddressManageParam qParam) throws
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<Ipv6ManageTableParam> list = new ArrayList<>();
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
        DataPermission dataPermission = DataPermission.valueOf(perm);
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            qParam.setGroupIds(groupIds);
        }
        switch (dataPermission) {
            case PRIVATE:
                qParam.setUserId(userId);
                Map priCriteria = PropertyUtils.describe(qParam);
                list = mwIpv6ManageTableDao.selectPriIpAddress(priCriteria);
                for (Ipv6ManageTableParam val :
                        list) {
                    Ipv6ManageTableParam va2 = new Ipv6ManageTableParam();
                    queryAddressipv6(val, va2);
                    val.setLongitude(va2.getLongitude());
                    val.setLatitude(va2.getLatitude());
                }
                break;
            case PUBLIC:
                List<String> orgNames = mwOrgCommonService.getOrgNamesByNodes(loginName);
                String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                List<Integer> orgIds = new ArrayList<>();
                Boolean isAdmin = false;
                for (String orgName : orgNames) {
                    if (orgName.equals(MWUtils.ORG_NAME_TOP)) {
                        isAdmin = true;
                        break;
                    }
                }
                if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                    isAdmin = true;
                }
                if (!isAdmin) {
                    orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);
                }
                if (null != orgIds && orgIds.size() > 0) {
                    qParam.setOrgIds(orgIds);
                }
                qParam.setIsAdmin(isAdmin);

                Map priCriteria1 = PropertyUtils.describe(qParam);
                list = mwIpv6ManageTableDao.selectPubIpAddress(priCriteria1);
                for (Ipv6ManageTableParam val :
                        list) {
                    Ipv6ManageTableParam va2 = new Ipv6ManageTableParam();
                    queryAddressipv6(val, va2);
                    val.setLongitude(va2.getLongitude());
                    val.setLatitude(va2.getLatitude());
                }
                break;
        }

        List<Ipv6ManageTableParam> listOne = new ArrayList<>();
        for (Ipv6ManageTableParam var : list) {
            var.setIPv4(false);
            if (IP_GROUPING.equals(var.getType())) {
                int i = mwIpv6ManageTableDao.checkIsLeaf(var.getId());
                if (i == 0) {
                    var.setLeaf(true);
                }
            } else {
                var.setTreeType(2);
                listOne.add(var);
            }

        }
        return listOne;
    }

    private void queryAddressipv6(Ipv6ManageTableParam val, Ipv6ManageTableParam va2) {
        //IpAddressManageTableParam val3=new IpAddressManageTableParam();
        if (val != null) {
            if (val.getLatitude() == null || val.getLongitude() == null) {
                Ipv6ManageTableParam table1Param = mwIpv6ManageTableDao.selectPictureIpv6ById1(val.getParentId());
                queryAddressipv6(table1Param, va2);
            } else {
                va2.setLatitude(val.getLatitude());
                va2.setLongitude(val.getLongitude());
            }
        }

    }

    private List<IpAddressManageTableParam> queryIpv4(QueryIpAddressManageParam qParam) throws
            IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<IpAddressManageTableParam> list = new ArrayList<>();
        List<IpAddressManageTableParam> listOne = new ArrayList<>();

        //listOne 新增表结构
        if (qParam.getSureUp() == false && qParam.getParentId() != 0) {
//            IpAddressManageTableParam IPFOur = new IpAddressManageTableParam();
//            IPFOur.setParentId(qParam.getParentId());
//            IPFOur.setIPv4(true);
//            IPFOur.setId(qParam.getParentId() * 100000);
//            IPFOur.setLabel("IPv4");
//            IPFOur.setLeaf(true);
//            IpAddressManageTableParam IPSix = new IpAddressManageTableParam();
//            IPSix.setParentId(qParam.getParentId());
//            IPSix.setIPv4(false);
//            IPSix.setId(-qParam.getParentId());
//            IPSix.setLabel(IPV6_JUDGE);
//            IPSix.setLeaf(true);
//            listOne.add(IPFOur);
//            listOne.add(IPSix);
        }
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
        DataPermission dataPermission = DataPermission.valueOf(perm);

        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            qParam.setGroupIds(groupIds);
        }
        switch (dataPermission) {
            case PRIVATE:
                qParam.setUserId(userId);

                Map priCriteria = PropertyUtils.describe(qParam);
                list = mwIpAddressManageTableDao.selectPriIpAddress(priCriteria);
                for (IpAddressManageTableParam val :
                        list) {
                    IpAddressManageTableParam va2 = new IpAddressManageTableParam();
                    queryAddress(val, va2);
                    val.setLongitude(va2.getLongitude());
                    val.setLatitude(va2.getLatitude());
                }
                break;
            case PUBLIC:
                // List<String> orgNames = mwUserOrgMapperDao.getOrgNameByLoginName(loginName);
                List<String> orgNames = mwOrgCommonService.getOrgNamesByNodes(loginName);
                String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                List<Integer> orgIds = new ArrayList<>();
                Boolean isAdmin = false;
                for (String orgName : orgNames) {
                    if (orgName.equals(MWUtils.ORG_NAME_TOP)) {
                        isAdmin = true;
                        break;
                    }
                }
                if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                    isAdmin = true;
                }
                if (!isAdmin) {
                    //  List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
                    //  orgIds = mwUserOrgMapperDao.getOrgIdByUserId(loginName);

                    orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);

                }
                if (null != orgIds && orgIds.size() > 0) {
                    qParam.setOrgIds(orgIds);
                }
                qParam.setIsAdmin(isAdmin);
                Map priCriteria1 = PropertyUtils.describe(qParam);
                list = mwIpAddressManageTableDao.selectPubIpAddress(priCriteria1);
                for (IpAddressManageTableParam val : list) {
                    IpAddressManageTableParam va2 = new IpAddressManageTableParam();
                    queryAddress(val, va2);
                    val.setLongitude(va2.getLongitude());
                    val.setLatitude(va2.getLatitude());
                }
                break;
        }
        //去空
        dealNull(list);
        for (IpAddressManageTableParam var : list) {
            var.setIPv4(true);
            if (IP_GROUPING.equals(var.getType())) {
                int i = mwIpAddressManageTableDao.checkIsLeaf(var.getId());
                if (i == 0) {
                    var.setLeaf(true);
                }
                var.setTreeType(0);
                listOne.add(var);
//                if (qParam.getPop() == true) {
//                    var.setLeaf(false);
//                    listOne.add(var);
//                }

            } else {
                var.setTreeType(1);
            }

        }
        if (qParam.getPop() == true) {
            return listOne;
        }
        return list;
    }

    private Integer saveCheck(Label label) {
        if (label.getLabelDropId() == null) {
            return 0;
        } else {
            if (label.getLabelDropId() == 0 && label.getDropValue() != null) {
                LabelCheck labelCheck = new LabelCheck();
                labelCheck.setDropValue(label.getDropValue());
                labelCheck.setLabelDrop(label.getLabelDrop());
                labelCheck.setDropKey(label.getLabelChecks().size() + 1);
                createLabel(labelCheck);
                return labelCheck.getDropId();
            } else {

                return label.getLabelDropId();
            }
        }
    }


   /* private void deleteDistributttion(ResponIpDistributtionParam
                                              responIpDistributtionParam, List<String> strings) {
        ipAddressManagePowerTableDao.deleteDstribution(responIpDistributtionParam.getId(), responIpDistributtionParam.getIdType());
        List<Check> checkList = responIpDistributtionParam.getRadio();
        List<Integer> s = new ArrayList<>();
        List<Integer> sv = new ArrayList<>();
        if (isIPv4Address(responIpDistributtionParam.getPrimaryIp())) {
            s.add(responIpDistributtionParam.getId());
        } else {
            sv.add(responIpDistributtionParam.getId());
        }
        for (Check check : checkList) {
            if (check.isRadioStatus()) {
                Integer id = ipAddressManagePowerTableDao.selectIPaddressId(check.getKeyTestValue());
                if (isIPv4Address(check.getKeyTestValue())) {
                    s.add(id);
                } else {
                    sv.add(id);
                }
            }
        }
        //修改地址的分配状态
        if (s.size() > 0) {
            ipAddressManagePowerTableDao.UpdateIPListDistributionTrue(s, false);
        }
        if (sv.size() > 0) {
            ipAddressManagePowerTableDao.UpdateIPV6ListDistributionTrue(sv, false);
        }
    }
*/

    private void createdHis(ResponIpDistributtionParam
                                    responIpDistributtionParam, RequestIpAddressDistributtionSeniorParam
                                    requestIpAddressDistributtionSeniorParam, Integer tyle,String getDistri,Integer signId) {
        IpamProcessHistoryDTO ipamProcessHistoryDTO = new IpamProcessHistoryDTO();
        Integer applicantId = 0;
        if (requestIpAddressDistributtionSeniorParam != null) {
            ipamProcessHistoryDTO.setApplicant(requestIpAddressDistributtionSeniorParam.getApplicant());
            ipamProcessHistoryDTO.setApplicantDate(requestIpAddressDistributtionSeniorParam.getApplicantionDate());
            ipAddressManagePowerTableDao.insterApplicant(ipamProcessHistoryDTO);
            applicantId = ipamProcessHistoryDTO.getId();
        } else {
            boolean c = isIPv4Address(responIpDistributtionParam.getPrimaryIp());
            ipamProcessHistoryDTO = ipAddressManagePowerTableDao.selectAplicant(responIpDistributtionParam.getId(), c == true ?IPV4_JUDGE  :IPV6_JUDGE,getDistri);
            applicantId = ipamProcessHistoryDTO.getId();
        }
        List<MwAssetsLabelDTO> mwAssetsLabelDTOS = new ArrayList<>();
        if (tyle == 0) {
            mwAssetsLabelDTOS.addAll(responIpDistributtionParam.getAttrData());
            mwAssetsLabelDTOS.addAll(requestIpAddressDistributtionSeniorParam.getAttrData());
        }


        List<String> strings = createdAddIP(responIpDistributtionParam);
        for (String string : strings) {
            if (isIPv4Address(string)) {
                Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(string,signId);
                mwLabelCommonServcie.deleteLabelBoard(integer.toString(), DataType.IP.getName());
                if (tyle == 0) {
                    mwLabelCommonServcie.insertLabelboardMapper(mwAssetsLabelDTOS, integer.toString(), DataType.IP.getName());
                    mwLabelCommonServcie.insertLabelboardMapper(requestIpAddressDistributtionSeniorParam.getAttrData(), applicantId.toString(), DataType.IPHIS.getName());
                }
                IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                ipamOperHistoryDTO.setCreateDate(new Date()).setType(tyle).setCreator(iLoginCacheInfo.getLoginName()).setIpType(false).setRlistId(integer).setApplicant(ipamProcessHistoryDTO.getId());
                ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);

            } else {
                Integer integer = ipAddressManagePowerTableDao.selectIPaddressId(string,signId);
                mwLabelCommonServcie.deleteLabelBoard(integer.toString(), DataType.IPV6.getName());
                if (tyle == 0) {
                    mwLabelCommonServcie.insertLabelboardMapper(mwAssetsLabelDTOS, integer.toString(), DataType.IPV6.getName());
                    mwLabelCommonServcie.insertLabelboardMapper(requestIpAddressDistributtionSeniorParam.getAttrData(), applicantId.toString(), DataType.IPHIS.getName());
                }
                IpamOperHistoryDTO ipamOperHistoryDTO = new IpamOperHistoryDTO();
                ipamOperHistoryDTO.setCreateDate(new Date()).setType(tyle).setCreator(iLoginCacheInfo.getLoginName()).setIpType(true).setRlistId(integer).setApplicant(ipamProcessHistoryDTO.getId());
                ipAddressManagePowerTableDao.insterOperHistoryDTO(ipamOperHistoryDTO);
            }

        }

    }


    private List<ResponIpDistributtionParam> createdAddIPAddress(Boolean tyle, ResponIpDistributtionParam
            responIpDistributtionParam,String getDistri,Integer signId) {
        List<Integer> s = new ArrayList<>();
        List<Integer> sv = new ArrayList<>();
        if (tyle) {
            s.add(responIpDistributtionParam.getId());
        } else {
            sv.add(responIpDistributtionParam.getId());
        }

        List<ResponIpDistributtionParam> responIpDistributtionParams = new ArrayList<>();
        List<Check> checkList = responIpDistributtionParam.getRadio();
        for (Check check : checkList) {

            ResponIpDistributtionParam responIpDistributtionParamnew = new ResponIpDistributtionParam();
            responIpDistributtionParamnew.setId(responIpDistributtionParam.getId());
            responIpDistributtionParamnew.setPrimaryIp(responIpDistributtionParam.getPrimaryIp());
            responIpDistributtionParamnew.setKeyValue(responIpDistributtionParam.getKeyValue());
            responIpDistributtionParamnew.setRadio(responIpDistributtionParam.getRadio());
            Integer id = ipAddressManagePowerTableDao.selectIPaddressId(check.getKeyTestValue(),signId);
            responIpDistributtionParamnew.setChose(id.toString());
            responIpDistributtionParamnew.setChoseType(check.isIdType());
            responIpDistributtionParamnew.setKeyValue(check.getKeyValue());
            responIpDistributtionParamnew.setBangDistri(getDistri);
            responIpDistributtionParams.add(responIpDistributtionParamnew);
            if (isIPv4Address(check.getKeyTestValue())) {
                s.add(id);
            } else {
                sv.add(id);
            }

        }
        //修改地址的分配状态
        if (s.size() > 0) {
            ipAddressManagePowerTableDao.UpdateIPListDistributionTrue(s, true);
        }
        if (sv.size() > 0) {
            ipAddressManagePowerTableDao.UpdateIPV6ListDistributionTrue(sv, true);
        }

        return responIpDistributtionParams;
    }

    private List<String> createdAddIP(ResponIpDistributtionParam responIpDistributtionParam) {
        List<String> strings = new ArrayList<>();
        List<Check> radio = responIpDistributtionParam.getRadio();
        for (Check c : radio) {
            strings.add(c.getKeyTestValue());
        }
        return strings;
    }

    public boolean isIPv4Address(String input) {
        return IPV4_REGEX.matcher(input).matches();
    }


    public static String CalculateTime(Date afterTime, Date beforeTime) {
        long nowTime = afterTime.getTime(); // 获取当前时间的毫秒数
        String msg = "";
        long reset = beforeTime.getTime(); // 获取指定时间的毫秒数
        long dateDiff = nowTime - reset;
        if (dateDiff < 0) {
            msg = "--";
        } else {
            Calendar nextDate = Calendar.getInstance();
            nextDate.setTime(afterTime);
            Calendar previousDate = Calendar.getInstance();
            previousDate.setTime(beforeTime);
            previousDate.add(Calendar.SECOND, -60);
            int year = nextDate.get(Calendar.YEAR) - previousDate.get(Calendar.YEAR);
            int month = nextDate.get(Calendar.MONTH) - previousDate.get(Calendar.MONTH);
            int day = nextDate.get(Calendar.DAY_OF_MONTH) - previousDate.get(Calendar.DAY_OF_MONTH);
            int hour = nextDate.get(Calendar.HOUR_OF_DAY) - previousDate.get(Calendar.HOUR_OF_DAY);// 24小时制
            int min = nextDate.get(Calendar.MINUTE) - previousDate.get(Calendar.MINUTE);
            int second = nextDate.get(Calendar.SECOND) - previousDate.get(Calendar.SECOND);
            boolean hasBorrowDay = false;// "时"是否向"天"借过一位

            if (second < 0) {
                second += 60;
                min--;
            }
            if (min < 0) {
                min += 60;
                hour--;
            }
            if (hour < 0) {
                hour += 24;
                day--;
                hasBorrowDay = true;
            }
            if (day < 0) {
                // 计算截止日期的上一个月有多少天，补上去
                Calendar tempDate = (Calendar) nextDate.clone();
                tempDate.add(Calendar.MONTH, -1);// 获取截止日期的上一个月
                day += tempDate.getActualMaximum(Calendar.DAY_OF_MONTH);
                // nextDate是月底最后一天，且day=这个月的天数，即是刚好一整个月，比如20160131~20160229，day=29，实则为1个月
                if (!hasBorrowDay
                        && nextDate.get(Calendar.DAY_OF_MONTH) == nextDate.getActualMaximum(Calendar.DAY_OF_MONTH)// 日期为月底最后一天
                        && day >= nextDate.getActualMaximum(Calendar.DAY_OF_MONTH)) {// day刚好是nextDate一个月的天数，或大于nextDate月的天数（比如2月可能只有28天）
                    day = 0;// 因为这样判断是相当于刚好是整月了，那么不用向 month 借位，只需将 day 置 0
                } else {// 向month借一位
                    month--;
                }
            }
            if (month < 0) {
                month += 12;
                year--;
            }
            msg += year + "年";
            msg += month + "个月";
            msg += day + "天";
            msg += hour + "时";
            msg += min + "分";
            msg += second + "秒";

            // msg = getLastTime(dateDiff/1000);
        }
        return msg;
    }


    public void queryAddress(IpAddressManageTableParam val, IpAddressManageTableParam va2) {
        //IpAddressManageTableParam val3=new IpAddressManageTableParam();
        if (val != null) {
            if (val.getLatitude() == null || val.getLongitude() == null) {
                IpAddressManageTableParam val2 = new IpAddressManageTableParam();
                val2.setId(val.getParentId());
                IpAddressManageTableParam table1Param = mwIpAddressManageTableDao.selectIpAddressById1(val2);
                queryAddress(table1Param, va2);
            } else {
                va2.setLatitude(val.getLatitude());
                va2.setLongitude(val.getLongitude());
            }
        }

    }

    public void dealNull(List<IpAddressManageTableParam> list) {
        if (list != null && list.size() > 0) {
            for (IpAddressManageTableParam a : list) {
                a.getOrgIds().removeAll(Collections.singleton(null));
                a.getGroupIds().removeAll(Collections.singleton(null));
                a.getPrincipal().removeAll(Collections.singleton(null));
            }
        }
    }

    //Object转Map
    public Map<String, Object> getObjectToMap(Object obj) throws IllegalAccessException {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        Class<?> clazz = obj.getClass();
        //System.out.println(clazz);
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String fieldName = field.getName();
            Object value = field.get(obj);
            if (value == null) {
                value = "";
            }
            map.put(fieldName, value);
        }
        return map;
    }


}
