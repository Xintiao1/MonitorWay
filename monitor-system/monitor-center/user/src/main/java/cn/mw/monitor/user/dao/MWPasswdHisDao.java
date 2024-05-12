package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MWPasswdHis;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MWPasswdHisDao {

    /**
     * 查询密码历史列表
     * @param
     * @return
     */
    public List<MWPasswdHis> selectList(MWPasswdHis mwpasswdHis);

    /**
     * 查询最近几次密码历史列表
     * @param userId
     * @param hisNum
     * @return
     */
    @Deprecated
    public List<MWPasswdHis> selectRecentList(@Param("userId") Integer userId, @Param("hisNum") Integer hisNum);

    /**
     * 新增密码历史
     * @param mwpasswdHis
     * @return
     */
    public Integer insert(MWPasswdHis mwpasswdHis);

    /**
     * 查询用户的密码记录个数
     * @param userId
     * @return
     */
    public Integer selectCount(@Param("userId") Integer userId);

    /**
     * 删除保留数量之外的记录
     * @param
     * @return
     */
    public Integer deleteRedun(@Param("userId") Integer userId, @Param("hisNum") Integer hisNum);
}
