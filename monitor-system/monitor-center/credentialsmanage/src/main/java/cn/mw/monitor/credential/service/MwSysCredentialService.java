package cn.mw.monitor.credential.service;


import cn.mw.monitor.credential.api.param.MwSysCredParam;
import cn.mw.monitor.credential.api.param.QueryCredentialParam;
import cn.mw.monitor.credential.model.MwSysCredential;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * (MwSysCredential)表服务接口
 *
 * @author zhaoy
 * @since 2021-05-31 14:16:07
 */
public interface MwSysCredentialService {


    /**
     * 新增数据
     *
     * @param mwSysCredential 实例对象
     * @return 实例对象
     */
    Reply insert(MwSysCredential mwSysCredential);

    /**
     * 修改数据
     *
     * @param mwSysCredential 实例对象
     * @return 实例对象
     */
    Reply update(MwSysCredential mwSysCredential);

    /**
     * 删除凭据
     *
     * @param ids 主键
     * @return 是否成功
     */
    Reply deleteById(List<Integer> ids);

    Reply pageCredential(QueryCredentialParam param);

    Reply selectCredDropDown(MwSysCredParam param);

    /**
     * 获取所有模块信息
     */
    Reply getModulesDropDown();

    Reply selectCredById(Integer credId);

}
