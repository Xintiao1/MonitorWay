package cn.mw.monitor.ipaddressmanage.dao;



import cn.mw.monitor.ipaddressmanage.dto.IpamOperHistoryDTO;
import cn.mw.monitor.ipaddressmanage.dto.IpamProcessHistoryDTO;
import cn.mw.monitor.ipaddressmanage.param.*;
import cn.mw.monitor.service.ipmanage.model.IpManageTree;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * bkc
 */
public interface MwIpAddressManagePowerTableDao {
    List<String> selectListbrow();

    Integer selectCountDistribution(@Param("ids") List<Integer> ids,@Param("s") String s,@Param("status") Integer status);

    ResponIpDistributtionParam selectResponIpDistributtionParam(@Param("id") Integer id,@Param("s") String s);

    List<Check> selectListradio(@Param("s") String s);

    List<Integer> selectIPaddress(@Param("strings") List<String> strings, @Param("i")int i,@Param("signId") Integer signId);

    void inster(@Param("responIpDistributtionParamsAdd") List<ResponIpDistributtionParam> responIpDistributtionParamsAdd, @Param("requestIpAddressDistributtionSeniorParam") RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam, @Param("tyle") Boolean tyle, @Param("org")String org,@Param("orgtest") String orgtest);

    void UpdateIPListDistributionTrue(@Param("id")List<Integer> id,@Param("type")Boolean type);

    void UpdateIPV6ListDistributionTrue(@Param("id")List<Integer> id,@Param("type")Boolean type);

    Integer insterApplicant(IpamProcessHistoryDTO ipamProcessHistoryDTO);

    void insterOperHistoryDTO(@Param("ipamOperHistoryDTO") IpamOperHistoryDTO ipamOperHistoryDTO);

    Integer selectIPaddressId(@Param("keyTestValue") String keyTestValue,@Param("signId") Integer signId);

    List<CleanParam> selectCheck(@Param("s") Integer s, @Param("iptype") String iptype);

    Map<String,Integer> selectPrimary(@Param("s") Integer s, @Param("iptype") String iptype, @Param("getBangDistri") String getBangDistri);

    IpamProcessHistoryDTO selectAplicant(@Param("s") Integer s, @Param("iptype") String iptype, @Param("getBangDistri") String getBangDistri);

    IpamProcessHistoryDTO selectAplicantById(@Param("s") String s);

    void deleteDstribution(@Param("s") Integer s, @Param("type")Boolean type);


    List<Label> selectLabel(@Param("id") Integer id, @Param("labelLevel") Integer labelLevel, @Param("idType") String idType);

    void createLabel(@Param("param") LabelCheck param);

    void deleteLabel(@Param("id") Integer id);

    List<LabelCheck> browDrop(@Param("name")  String name);

    List<QueryIpAddressManageListParam> selectSonList(Map priCriteria);

    List<QueryIpAddressManageListParam> selectDisHave();

    List<QueryIpAddressManageListParam> selectIpv6SonList(Map priCriteria);

    void insterIpaddressDrop(@Param("label") Label label);

    void updateIpaddressDrop(@Param("label") Label label);

    Integer selectipv6rand(@Param("intrand") String intrand, @Param("keyValue") String keyValue);

    Integer selectIpv6(@Param("param")  String param,@Param("signId") Integer signId);

    void insterIpv6list(@Param("llinkid")Integer llinkid,@Param("param") String param, @Param("loginName")String loginName,@Param("signId") Integer signId);

    List<Check> selectListradioTwo(@Param("id") List<Integer> id);

    IpAddressManageTableParam selectIpAddressManageTableParam( @Param("id")Integer iPid);

    Integer selectIpParent(@Param("id") Integer id);

    Integer selectIpvsixParent(@Param("id")  Integer iPid);

    IpAddressDistributtionSeniorParam selectDistributtionSenior(@Param("ip")Integer primary_ip, @Param("iptype")String iptype,@Param("getDristi")String getDristi);

    String selectUserName(@Param("id")Integer id);

    Integer selectCountDistributionByIp(@Param("ipList")List<String> ipList, @Param("iptype")String iptype, @Param("status")int i);


    void  deleteDstributionByIpv4(@Param("ipList")List<Integer> ipv4,@Param("getDistri")String getDistri);

    void deleteDstributionByIpv6(@Param("ipList")List<Integer> ipv4,@Param("getDistri")String getDistri);

    String selectIPaddressById(@Param("id")Integer id,@Param("type")Integer type);

    List<String> selectIPaddressByIds(@Param("ids")List<Integer> ids,@Param("type")Integer type);

    void insterDes(@Param("requestIpAddressDistributtionSeniorParam") IpAddressDistributtionSeniorParam map, @Param("id") Integer id, @Param("type") boolean b, @Param("integer") Integer integer, @Param("i") int i, @Param("groupid") Integer integer1);

    List<String> selectCountIPDIsEnabel(@Param("searchId")List<Integer> searchId);

    ResponseIpAddressOperHistoryParamOB selectOperHistoryByApplicant(@Param("labelLinkId")Integer labelLinkId, @Param("linkId") Integer linkId);

    List<ResponseIpAddressOperHistoryParamOB> selectOperHistory(@Param("s")Integer integer, @Param("type")boolean b, @Param("keytype") Integer keytype, @Param("updateDateStart") Date updateDateStart, @Param("updateDateEnd") Date updateDateEnd);

    IpAddressManageTableParam selectIpv6AddressManageTableParam(@Param("id") Integer parentId);

    List<Map<String, String>> fuzzSeachAllFiledData(@Param("table") String table, @Param("linkId")Integer linkId);

    void insterNew(@Param("map") List<Map<String, Object>> map, @Param("requestIpAddressDistributtionSeniorParam") RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam,@Param("org")String org,@Param("orgtest") String orgtest,@Param("getDistri") String getDistri);

    List<Map<String, Object>> selectDIs(@Param("parentId") Integer parentId, @Param("Type") Integer primaryType,@Param("getDistri")String getDistri);

    List<Map<String, String>> selectIPaddressMapById(List<Map<String, Object>> maps);


    void updateApplicant(@Param("applicantId")Integer applicantId, @Param("applicant")Integer applicant, @Param("integer")Integer integer, @Param("b")boolean b,@Param("s")String s);

    List<Map<String, Object>> slecetParentLabel(@Param("parentId")Integer primary_ip, @Param("Type")Integer primary_type);

    List<Map<String, Object>> selectAllManage(@Param("grouping") String grouping);

    List<Map<String, Object>> checkIP(@Param("ip") List<String> ip);

    List<RequestIpAddressDistributtionNewParamList> selectIPDristi(@Param("id")Integer id, @Param("iptype")String iptype);

    List<Integer> checkStauts(@Param("ips")List<Integer> ipv4, @Param("iptype")String iptype);

    List<IpAddressManageTableParam> selectLin();

    List<IpAddressManageTableParam> selectIPv6Lin();

    List<Label> selectLabelAll(@Param("labelLevel")int i, String name,@Param("input")int input);


    IPCountNum countNumList();

    List<IPCountPrictureDetails> countCreateByhour(@Param("start")String start, @Param("end")String end);

    List<IPCountPrictureDetails> countDeleteByhour(@Param("start")String start, @Param("end")String end);

    List<IPCountPrictureDetails> countHavingByhour(@Param("start")String start, @Param("end")String end,@Param("type")Integer type);

    List<IPCountPrictureDetails> countHavingByDay(@Param("start")String start, @Param("end")String end,@Param("type")Integer type, @Param("s")List<Integer> s);

    List<IPCountPrictureDetails> countCreateByDay(@Param("solarDataStart")String solarDataStart, @Param("solarDataEnd")String solarDataEnd, @Param("s")List<Integer> s);

    List<IPCountPrictureDetails> countDeleteByDay(@Param("solarDataStart")String solarDataStart, @Param("solarDataEnd")String solarDataEnd, @Param("s")List<Integer> s);

    List<Map<String, Object>> selectLabelText(@Param("applicant")String applicant);

    List<Map<String, Object>> selectIPdisByApplicant(@Param("applicant") String applicant);

    List<Map<String, Object>>  selectLabelTextNotDrop(@Param("applicant") String applicant);

    List<Map<String, Object>> selectTreeAllbyIPids(@Param("ipids") List<String> ipids,@Param("s")  String s,@Param("applicant")String applicant);

    List<IpamOperHistoryDTO> getApplicantBydisttri(@Param("ids") List<Integer> ids, @Param("s") String s);

    List<IpDestribution> selectrealdtri(@Param("distriId") Integer distriId);

    void insertDstr(@Param("o") IpDestribution o);

    List<Map<String,Object>> selectGroups(@Param("labelId")Integer labelId, @Param("searchid")Integer searchid, @Param("sreach")String sreach,@Param("srechidby") Integer srechidby,@Param("creator")String creator, @Param("drop_value")String drop_value,@Param("drop_values")String drop_values);

    List<String> selectIPaddressByDtriGroup(@Param("s")  List<Integer> groups);

    void insertOperHis(@Param("dis_ipaddress") String o, @Param("dis_old_ipaddress") String toString, @Param("map_key_value") String o1, @Param("map_old_key_value") String toString1, @Param("group_id") String operNum, @Param("oper_time") Date oper_time, @Param("oper_int") int i, @Param("applicant_time") Date applicant_time, @Param("applicant_old_time") Date o2, @Param("recall_num") int i1, @Param("ip_discription") String o3, @Param("ip_old_discription") String toString2,@Param("ipaddress_relation") String ipAddress,@Param("oper_user") String oper_user,@Param("getDistri") String getDistri);

    List<Map<String, Object>> selectIPdisOperHis(@Param("group") Integer group,@Param("integer") Integer integer,@Param("distributtioner")  String distributtioner,@Param("start")  String start, @Param("end") String end, @Param("ipAddresses") String ipAddresses, @Param("operInt") String operInt,@Param("applicaturl")  String applicaturl,@Param("applicator")  String applicator,@Param("orderName")  String orderName,@Param("orderType")  String orderType,@Param("operStart")  String operStart, @Param("operEnd") String operEnd,@Param("group_id") String group_id);

    QueryIpAddressManageListParam selectIpAddressManageTableListParam(Integer id);

    QueryIpAddressManageListParam selectIpv6ddressManageTableListParam(Integer id);

    List<IpamOperHistoryDTO> selectAplicantGroupbyApplicant(@Param("type") int type,@Param("applicant") int applicant,@Param("group")Integer group);

    List<Map<String, Object>> selectClean();

    List<Map<String, Object>> selectIPdisOperHisByOperTimt(@Param("date") Date operTime);

    void deleteOperHis(@Param("id")  List<Integer> id);

    Integer getCountIssue(@Param("i") int i, @Param("id") List<Integer> linkids);

    List<Integer> selectFIPaddress( @Param("ids") List<Integer> ids);

    List<Map<String, Object>> getRelation(@Param("mainId")Integer mainId, @Param("mainIdType")Integer mainIdType, @Param("bangDistri")String bangDistri);

    String selectBangString(@Param("recoveryId") Integer recoveryId);

    Map<String, Object> selectOperCleanHis(@Param("ipaddress")String ipaddress,@Param("bang_distri") String bang_distri);

    List<IpManageTree> selectParenIds(@Param("strings") List<String> strings);

    List<Map<String, Object>> getDes(@Param("integer") Integer integer);

    List<IpAllRequestBody> getIpSign(@Param("id") Integer id);

    void deleteIpsign(@Param("id")  int i);

    void insertIpSign(@Param("ipAllRequestBody") IpAllRequestBody ipAllRequestBody);

    void updateOperHistory(@Param("maps")  List<Map<String, Object>>  maps, @Param("signId") Integer signId);

    void updateOtherBelong(@Param("maps") List<Map<String, Object>> maps,@Param("signId")  Integer signId);

    //查询子节点

}
