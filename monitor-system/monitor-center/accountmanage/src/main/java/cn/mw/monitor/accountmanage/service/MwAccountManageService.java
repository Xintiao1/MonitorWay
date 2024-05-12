package cn.mw.monitor.accountmanage.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.accountmanage.entity.AddAccountManageParam;
import cn.mw.monitor.accountmanage.entity.QueryAccountManageParam;
import com.alibaba.fastjson.JSONArray;

import java.util.List;

public interface MwAccountManageService {

    /**
     * 获取单个账户信息
     *
     * @param param 账户信息
     * @return
     * @throws Exception
     */
    Reply selectList1(QueryAccountManageParam param) throws Exception;

    Reply selectList(QueryAccountManageParam param);

    /**
     * 删除账号管理
     *
     * @param record
     * @return
     * @throws Exception
     */
    Reply delete(List<Integer> record) throws Exception;

    /**
     * 修改账号管理
     *
     * @param record
     * @return
     * @throws Exception
     */
    Reply update(AddAccountManageParam record) throws Exception;

    /**
     * 添加账号管理
     *
     * @param record
     * @return
     * @throws Exception
     */
    Reply insert(AddAccountManageParam record) throws Exception;

    /**
     * 下拉框选择
     *
     * @return
     * @throws Exception
     */
    Reply selectDrop() throws Exception;

}
