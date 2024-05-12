package cn.mw.monitor.credential.dao;

import cn.mw.monitor.credential.model.MwSnmpCredential;
import cn.mw.monitor.credential.util.MapResultHandler;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwSnmpCredentialDao {
    int deleteByPrimaryKey(Integer id);

    int insert(MwSnmpCredential record);

    int insertSelective(MwSnmpCredential record);

    MwSnmpCredential selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MwSnmpCredential record);

    int updateByPrimaryKey(MwSnmpCredential record);

    void selectCredDropDown(MapResultHandler<String, String> credResultHandler);

    List<MwSnmpCredential> select(Map<String, Object> describe,@Param("loginName") String loginName);

    List<String> selectSNMPCommCred(@Param("loginName") String loginName,@Param("moduleId")Integer moduleId);

}