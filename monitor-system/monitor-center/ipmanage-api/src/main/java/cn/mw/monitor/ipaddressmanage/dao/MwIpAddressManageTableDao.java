package cn.mw.monitor.ipaddressmanage.dao;


import cn.mw.monitor.ipaddressmanage.dto.LinkLabel;
import cn.mw.monitor.ipaddressmanage.param.*;
import org.apache.ibatis.annotations.Param;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * bkc
 */
public interface MwIpAddressManageTableDao {
    //查询子节点
    List<IpAddressManageTableParam> selectListById(@Param("id") Integer id);

    //查询父级节点
    List<IpAddressManageTableParam> selectListByIds(@Param("ids") Set<Integer> ids);

    //查询
    List<IpAddressManageTableParam> selectPubIpAddress(Map priCriteria);

    //查询
    List<IpAddressManageTableParam> selectPriIpAddress(Map priCriteria);

    //根据主键查询指定ip
    IpAddressManageTableParam selectIpAddressById1(IpAddressManageTableParam qParam);

    //根据主键查询指定ip
    IpAddressManageTable1Param selectIpAddressById(QueryIpAddressManageParam qParam);

    List<IpAddressManageTableParam> selectSubIpAddress(@Param("id") Integer id);

    //修改ip地址管理信息
    int update(AddUpdateIpAddressManageParam record);

    //新增ip地址管理
    int insert(AddUpdateIpAddressManageParam record);

    //删除ip地址管理
    int delete(Integer id);

    List<String> selectOrg2(@Param("orgIds") List<Integer> id);

    //新增 ip地址管理标签关系
    int insertLabelLink(@Param("list") List<LinkLabel> list);

    //删除ip地址管理标签关系
    int deleteLabelLink(@Param("ipid") Integer ipid);

    //查询所有的IP地址段
    List<String> selectAllIpaddresses(@Param("signId") Integer signId);

    //删除IP地址段
    int deleteByIpaddresses(@Param("ipAddresses") String ipAddresses);

    List<Integer> selectIdBytime(@Param("time") Integer time);

    int addPubIP(List<AddPubIpAddressParam> list);

    int deletePubIp(Integer id);

    List<AddPubIpAddressParam> selectPubIp();

    List<IpAddressManageTableParam> selectPriIp();

    int checkIsLeaf(int id);

    List<String> selectIpKillPremit(Map priCriteria1);


    List<String> selectIpSixKillPremit(Map priCriteria1);

    List<MwIpConnection> selectIpConnection(@Param("ipAddress") String ipAddress);

    List<String> getOrgNameByIp(String ip);

    int insertIpConnection(List<MwIpConnection> mwIpConnections);

    List<String> getOrgNameByIpv6(String ip);

    List<MwIpConnection> getMwIPConnection(@Param("maps") List<Map<String, String>> maps);

    int updateIpConnection(@Param("mwIpConnections") List<MwIpConnection> mwIpConnections);

    List<String> selectOrgName(@Param("ip") String ip);

    List<Integer> selectPicture(@Param("id") Integer id);


    IpAddressManageTableParam selectAllIpAddressById(IpAddressManageTableParam qParam);

    void changeParentId(@Param("id") Integer id, @Param("draggingType") boolean draggingType, @Param("parentId") Integer parentId);

    void changeAfterSort(@Param("id") Integer id, @Param("draggingType") boolean draggingType, @Param("indexSort") Integer indexSort);


    List<Map<String, Object>> selectAllIpAddressByParenId(@Param("id")Integer id, @Param("draggingType")boolean draggingType, @Param("parentId")Integer parentId);

    void updateIpIncludecollect(@Param("ids")List<String> ipAddressList);

    List<AddUpdateIpAddressManageListParam> selectEndAnbleIpAddress();

    Integer selectCount();

    List<Map<String,String>> getIpaddressList();

    Integer selectIpMangeHaveDistribution(@Param("id")  Integer id);

    List<String> selectIncleudeIP(@Param("id") Integer id);

    void updateIpInclude(@Param("id")Integer link, @Param("startIP")String startIP,@Param("endIP") String endIP);

    void updateIpIncludeTwo(@Param("id")Integer link, @Param("startIP")String startIP,@Param("endIP") String endIP);

    List<String> getListIPaddressByLinkId(@Param("id") Integer id);

    void insertIpDelete(@Param("ipaddresses") List<String> ipaddress);

    List<AddIpaddresStatusHis> selectIpaddressStatusHis(Map uParam );
}