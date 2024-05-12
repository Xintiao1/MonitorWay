package cn.mw.monitor.credential.service;


import cn.mw.monitor.credential.api.param.QueryCredentialParam;
import cn.mw.monitor.credential.model.MwSnmpCredential;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * (MwSnmpCredential)表服务接口
 *
 * @author makejava
 * @since 2021-05-31 14:14:35
 */
public interface MwSnmpCredentialService {


    /**
     * 新增数据
     *
     * @param mwSnmpCredential 实例对象
     * @return 实例对象
     */
    Reply insert(MwSnmpCredential mwSnmpCredential);

    /**
     * 修改数据
     *
     * @param mwSnmpCredential 实例对象
     * @return 实例对象
     */
    Reply update(MwSnmpCredential mwSnmpCredential);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Reply deleteById(List<Integer> ids);

    Reply pageCredential(QueryCredentialParam param,boolean flag);

    Reply selectCredDropDown();

    Reply selectCredById(Integer credId);
}
