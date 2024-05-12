package cn.mw.monitor.user.service;

import cn.mw.monitor.api.param.aduser.*;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mwpaas.common.model.Reply;

import javax.naming.NamingException;

public interface MWADUserService {
    Reply authenticAdmin(ADAuthenticParam param);

    Reply select(QueryADInfoParam param) throws NamingException;

    Reply addADUser(AddADUserParam param) throws Throwable;

    Reply configBrowse(QueryADInfoParam param);

    /**
     * 删除AD配置数据
     *
     * @param param
     * @return
     */
    Reply deleteById(QueryADInfoParam param);

    /**
     * 获取AD用户信息
     *
     * @param param 请求参数
     * @return 包含AD用户信息的返回数据
     */
    Reply selectByName(AddADUserParam param);

    Reply selectGroupUser(ADGroupUserParam param);

    Reply seletSyAdInfo();

    Reply insertAdInfo(AdCommonParam param);

    Reply deleteADUser(DeleteADUserParam param);

    Reply browseUser(QueryADInfoParam param);

    /**
     * 同步LDAP用户
     *
     * @param param 同步参数
     * @return
     */
    Reply syncADUser(SyncUserParam param);

    /**
     * 修改配置备注
     *
     * @param param 参数
     * @return
     */
    Reply updateConfigDesc(AddADUserParam param);

    /**
     * 同步AD用户
     * @return
     */
    TimeTaskRresult syncADUser();

    /**
     * 同步AD域机构数据
     * @return
     */
    Reply syncADOrg();
}
