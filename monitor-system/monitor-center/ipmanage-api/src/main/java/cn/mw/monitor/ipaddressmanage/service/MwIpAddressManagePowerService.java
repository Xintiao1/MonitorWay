package cn.mw.monitor.ipaddressmanage.service;

import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.ipaddressmanage.param.*;
import cn.mwpaas.common.model.Reply;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;


/**
 * bkc
 */
@ConditionalOnProperty(prefix = "mwModule", name = "ipPrower", havingValue = "true")
public interface MwIpAddressManagePowerService {


    Reply selectListbrow(QueryIpAddressDistributtionParam param);

    Reply createDistributtion(RequestIpAddressDistributtionParam param);


  /*  Reply selectListDistributtionbrow(QueryIpAddressDistributtionParam param) throws ParseException;*/

   /* Reply cleanIP(QueryIpAddressDistributtionParam param);*/

    Reply MWIpAddressPowerHistorybrow(QueryIpAddressDistributtionParam param) throws ParseException;

    Reply selectLabel(QueryIpAddressDistributtionParam param);

    Reply createLabel(LabelCheck param);

    Reply deleteLabel(LabelCheck param);

    Reply browDrop(String name);

    Reply selectSnoListTest(QueryIpAddressPowerManageListParam qParam) throws Exception;

    Reply selectSonList(QueryIpAddressPowerManageListParam qParam) throws Exception;

    Reply saveLabel(List<Label> name);

    boolean createIpv6(String param, String keyValue,Integer signId);

    Reply selcectCheck(QueryIpAddressDistributtionParam param);

    Reply  selectTree(QueryIpAddressManageParam qParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    Reply historyGroup(QueryIpAddressDistributtionParam param);

    /*Reply selectListDistributtionSeniorParamBrowse(QueryIpAddressDistributtionParam param) throws ParseException;*/

    /*Reply cleanSignleIP(List<QueryIpAddressDistributtionParam> param);*/


    Reply updateDistributtion(RequestIpAddressDistributtionParam param);

    Reply selectById(QueryCustomPageParam qParam);

    Reply getHisList(AddUpdateIpAddressManageListParam parm) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    Reply fuzzSeachAllFiledData(AddUpdateIpAddressManageListParam parm);

    Reply getAllIpManage(IpAllRequestBody ipAllRequestBody) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    Reply isInput(IsInput isInput);

    Reply getLeader();

    Reply changeIndex(IsInput isInput);

    Reply createNewDistributtion(RequestIpAddressDistributtionNewParam param, String operNum);

    Integer selectByIPaddress(String ip,Integer signId);

    Reply selectList(QueryIpAddressManageParam qParam, Integer level) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException;

    Reply selectListSeniorParam(IsInput param) throws ParseException;

    Reply changeOrRe(RequestIpAddressReciveParam param) throws ParseException;

    Reply changeParam(RequestIpAddressDistributtionNewParam param, String operNum) throws ParseException;

    Reply getAllIpGroupManage(IsInput isInput);

    XSSFWorkbook excelImport(MultipartFile file);

    List<String> ipImport(MultipartFile file, Integer id) throws IOException;

    Reply selectIPDristi(IsInput param);

    Reply checkBrow(IsInput param);

    Reply selectListComprehensive(Map<String,Object> param);

    Reply sreachLabel(QueryCustomPageParam qParam);

    Reply countNumList();

    Reply countCreate(seachLabelList param) throws ParseException;

    Reply countHaving(seachLabelList param);

    Reply getTree(seachLabelList param);

    Reply cancel(List<DeleteIpList> param);

    Reply change(List<seachLabelList> param);

    Reply browagain(seachLabelList param);

    Reply getInfo(Map<String, Object> param);

    void execlInfo( Map<String,String> param, HttpServletResponse response) throws IOException;

    Reply selectListSeniorseeInfo(IsInput param);

    Reply history();

    Reply changeList(IsInput isInput);

    void changeRes(List<RequestIpAddressDistributtionNewParam> params) throws ParseException;

    Reply inversion(IsInput id);


    void relation(RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam, ResponIpDistributtionNewParam r);

    Reply getIpAddressListDes(List<String> ipAddressList);

    Reply getIpSign();

    Reply createIpsign(IpAllRequestBody ipAllRequestBody);

    Reply deleteIpsign(IpAllRequestBody ipAllRequestBody);

    Reply excelImportDesc(MultipartFile file, Integer signId) throws IOException;

    Reply execlImport(MultipartFile file, Integer id) throws IOException;

    Reply getIpAddressOrg(AddUpdateIpAddressManageParam qParam);
}
