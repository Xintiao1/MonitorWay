package cn.mw.monitor.credential.service;


import cn.mw.monitor.credential.api.param.QueryCredentialParam;
import cn.mw.monitor.credential.model.MwSnmpPortCredential;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * (MwSnmpPortCredential)表服务接口
 *
 * @author makejava
 * @since 2021-05-31 14:15:28
 */
public interface MwSnmpPortCredentialService {


    /**
     * 新增数据
     *
     * @param mwSnmpPortCredential 实例对象
     * @return 实例对象
     */
    Reply insert(MwSnmpPortCredential mwSnmpPortCredential);

    /**
     * 修改数据
     *
     * @param mwSnmpPortCredential 实例对象
     * @return 实例对象
     */
    Reply update(MwSnmpPortCredential mwSnmpPortCredential);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Reply deleteById(List<Integer> ids);

    Reply pageCredential(QueryCredentialParam param,boolean flag);

    Reply selectCredDropDown(QueryCredentialParam param,boolean flag);

    Reply selectCredById(Integer credId);
}
