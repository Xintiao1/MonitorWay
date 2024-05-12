package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.user.dao.MwUserOrgMapperDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class MWUserOrgServiceImpl implements MWUserOrgCommonService {

    @Resource
    private MwUserOrgMapperDao mwUserOrgMapperDao;

    @Override
    public String getRolePermByUserId(Integer userId) {
        return mwUserOrgMapperDao.getRolePermByUserId(userId);
    }

    @Override
    public String getRoleIdByLoginName(String loginName) {
        return mwUserOrgMapperDao.getRoleIdByLoginName(loginName);
    }

}
