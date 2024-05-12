package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dao.MwUserGroupMapperDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class MWUserGroupServiceImpl implements MWUserGroupCommonService {

    @Resource
    private MwUserGroupMapperDao mwUserGroupMapperDao;

    @Override
    public List<Integer> getGroupIdByLoginName(String loginName) {
        return mwUserGroupMapperDao.getGroupIdByLoginName(loginName);
    }

    @Override
    public String getUserIdIdByLoginName(String loginName) {
        return mwUserGroupMapperDao.getUserIdIdByLoginName(loginName);
    }

    @Override
    public List<String> getWxOpenId(String loginName) {
        return mwUserGroupMapperDao.getWxOpenId(loginName);
    }

    /**
     * 获取所有的用户组信息
     *
     * @param typeId   类别ID
     * @param dataType 类别
     * @return 所有的用户组数据
     */
    @Override
    public List<GroupDTO> getAllGroupList(int typeId, DataType dataType) {
        return mwUserGroupMapperDao.getAllGroupList(typeId, dataType.getName());
    }

    @Override
    public List<String> getGroupnamesByids(List<Integer> groupIds) {
        return mwUserGroupMapperDao.getGroupnamesByids(groupIds);
    }

}
