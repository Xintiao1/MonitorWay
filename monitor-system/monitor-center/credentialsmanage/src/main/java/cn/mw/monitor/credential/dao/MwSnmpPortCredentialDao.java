package cn.mw.monitor.credential.dao;

import cn.mw.monitor.credential.model.MwSnmpPortCredential;
import cn.mw.monitor.credential.model.MwSysCredential;
import cn.mw.monitor.credential.util.MapResultHandler;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwSnmpPortCredentialDao {
    int deleteByPrimaryKey(Integer id);

    int insert(MwSnmpPortCredential record);

    int insertSelective(MwSnmpPortCredential record);

    MwSnmpPortCredential selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MwSnmpPortCredential record);

    int updateByPrimaryKey(MwSnmpPortCredential record);

    void selectCredDropDown(MapResultHandler<String, String> credResultHandler);

    List<MwSnmpPortCredential> select(Map<String, Object> describe,@Param("loginName") String loginName);

    List<String> selectSNMPCred(@Param("loginName") String loginName,@Param("moduleId")Integer moduleId);

}