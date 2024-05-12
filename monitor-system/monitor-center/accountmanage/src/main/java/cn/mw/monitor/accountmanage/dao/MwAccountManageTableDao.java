package cn.mw.monitor.accountmanage.dao;


import cn.mw.monitor.accountmanage.entity.AddAccountManageParam;
import cn.mw.monitor.accountmanage.entity.MwAccountManageTable;
import cn.mw.monitor.accountmanage.entity.MwQueryAccountManageTable;
import cn.mw.monitor.accountmanage.entity.QueryAccountManageParam;
import org.apache.ibatis.annotations.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface MwAccountManageTableDao {

    /**
     * 根据主键查询
     *
     * @param qParam
     * @return
     */
    MwQueryAccountManageTable selectOne(QueryAccountManageParam qParam);

    List<MwAccountManageTable> selectList(Map pubCriteria);

    /**
     * 删除账号管理
     *
     * @param deleteBatch
     * @return
     */
    int deleteBatch(@Param("list") List<Integer> deleteBatch);

    //修改账号管理信息
    int update(AddAccountManageParam record);

    //新增账号管理
    int insert(AddAccountManageParam record);

    List<HashMap<String, String>> selectDrop();

    /**
     * 校验账户名称是否重复
     *
     * @param accountName 账户名称
     * @return
     */
    boolean checkAccountNameRepeat(@Param(value = "accountName") String accountName);

    /**
     * 根据账户名称获取账户信息
     *
     * @param accountName 账户名称
     * @return
     */
    MwQueryAccountManageTable getInfoByAccountName(@Param(value = "accountName") String accountName);
}
