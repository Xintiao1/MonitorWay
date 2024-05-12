package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MwPasswdplanOrgMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwPasswdplanOrgMapperDao {

    /**
     * 新增机构和密码策略的关联关系
     */
    int createOrgPasswdMapper(@Param("list")List<MwPasswdplanOrgMapper> mapper);
    /**
     * 删除机构和密码策略关联关系
     */
    int deletePasswdOrgMapper(@Param("passwdIds")List<Integer> passwdIds);

}
