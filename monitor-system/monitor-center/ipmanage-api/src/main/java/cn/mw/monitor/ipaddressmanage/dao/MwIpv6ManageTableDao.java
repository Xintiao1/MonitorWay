package cn.mw.monitor.ipaddressmanage.dao;


import cn.mw.monitor.ipaddressmanage.paramv6.AddUpdateIpv6ManageParam;
import cn.mw.monitor.ipaddressmanage.paramv6.Ipv6ManageTable1Param;
import cn.mw.monitor.ipaddressmanage.paramv6.Ipv6ManageTableParam;
import cn.mw.monitor.ipaddressmanage.paramv6.QueryIpv6ManageParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MwIpv6ManageTableDao {

    List<String> selectOrg2(@Param("orgIds") List<Integer> id);

    //根据主键查询指定ip
    Ipv6ManageTable1Param selectIpv6ById(QueryIpv6ManageParam qParam);

    //查询
    List<Ipv6ManageTableParam> selectPubIpAddress(Map priCriteria);
    //查询
    List<Ipv6ManageTableParam> selectPriIpAddress(Map priCriteria);

    //查询子节点
    List<Ipv6ManageTableParam> selectSubIpAddress(@Param("id") Integer id);

    //查询子节点
    List<Ipv6ManageTableParam> selectListByIds(@Param("ids") Set<Integer> ids);

    //删除ip地址管理
    int delete(@Param("id") Integer id);

    //删除ip地址管理右侧id
    int deleteList(@Param("id") Integer id);

    //单个修改ipv6管理
    int update(AddUpdateIpv6ManageParam record);

    //单个添加ipv6管理
    int insert(AddUpdateIpv6ManageParam record);

    List<String> selectAllIpaddresses();

    //查询子节点
    List<Ipv6ManageTableParam> selectIPv6IpAddress();
    //查询子节点
    Ipv6ManageTableParam selectPictureIpv6ById1(@Param("id") Integer id);

    List<Integer> selectPicture(@Param("id") Integer id);

    int checkIsLeaf(int id);


    List<Ipv6ManageTable1Param> countIPv6(@Param("ipRandStrat") String s,@Param("ipRandEnd")String g,@Param("signId")Integer signId);


    //查看ipv6被分配了没有
    Integer selectListByIdsHaveOper(@Param("ids") Set<Integer> ids);
}
