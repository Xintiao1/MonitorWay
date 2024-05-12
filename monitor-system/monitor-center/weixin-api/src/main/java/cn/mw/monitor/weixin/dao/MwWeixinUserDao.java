package cn.mw.monitor.weixin.dao;


import cn.mw.monitor.weixin.entity.MwDatapermission;
import cn.mw.monitor.weixin.entity.MwOrgMapper;
import cn.mw.monitor.weixin.entity.MwWeixinUserTable;
import org.apache.ibatis.annotations.Param;

import java.util.HashSet;
import java.util.List;

public interface MwWeixinUserDao {

    int insert(MwWeixinUserTable user);

    //int insertBatch(List<MwWeixinUserTable> users);

    int delete(String  openid);

    int updateById(MwWeixinUserTable record);

    MwWeixinUserTable selectOne(String  openid);

    List<MwWeixinUserTable> selectList();

    MwWeixinUserTable selectOneByMwLoginName(String  loginName);

    MwOrgMapper select1(@Param("assetsId") String  assetsId);
    List<Integer> select2(@Param("orgId") Integer  orgId);
    MwDatapermission select3(@Param("typeId") String  typeId);
    List<Integer> select4(@Param("typeId") String  typeID);
    List<Integer> select5(@Param("typeId") String  typeID);
    HashSet<String> select6(@Param("list") HashSet<Integer> list);
    HashSet<String> select7(@Param("list") HashSet<Integer> list);

}
