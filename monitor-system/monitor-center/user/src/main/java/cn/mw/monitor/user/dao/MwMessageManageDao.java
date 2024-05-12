package cn.mw.monitor.user.dao;

import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.service.user.dto.SettingDTO;
import cn.mw.monitor.user.model.MwUserOrgTable;
import cn.mw.monitor.websocket.Message;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface MwMessageManageDao {


    void insert(Message message);

    List<Message> selectByreadUserLoginName(@Param("loginName")String loginName, @Param("type")int type);

    void chageEditor(@Param("loginName")String loginName, @Param("param") Integer param);

    void delete(@Param("loginName")String loginName, @Param("param") Integer param);

    Integer selectUnfinishActivitiByreadUserLoginName(@Param("loginName") String loginName);
}
