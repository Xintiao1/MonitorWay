package cn.mw.monitor.user.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mw.monitor.user.dao.MwUserRoleMapperDao;
import cn.mw.monitor.user.model.MwUserRoleMap;
import cn.mw.monitor.user.service.IMWUserPostProcesser;
import cn.mw.monitor.event.AddUserEvent;
import cn.mw.monitor.event.PostUpdUserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class UserRoleMapProcesser implements IMWUserPostProcesser {
    @Resource
    MWUserDao mwuserDao;
    @Resource
    MwUserRoleMapperDao mwUserRoleMapperDao;
    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {
        if(event instanceof AddUserEvent){
            AddUserEvent addUserEvent = (AddUserEvent)event;
            int userId = addUserEvent.getUserDTO().getUserId();
            int roleId = addUserEvent.getUserDTO().getRoleId();
            mwUserRoleMapperDao.insertUserRoleMapper(MwUserRoleMap
                    .builder()
                    .userId(userId)
                    .roleId(roleId)
                    .build()
            );
        }
        if(event instanceof PostUpdUserEvent){
            PostUpdUserEvent udpateUserEvent = (PostUpdUserEvent)event;
            // 修改密码的情况下跳过这一步
            if(udpateUserEvent.getNewUserdto().getRoleId() == null){
                return null;
            }
            int userId = udpateUserEvent.getNewUserdto().getUserId();
            int roleId = udpateUserEvent.getNewUserdto().getRoleId();
            mwuserDao.updateUserRoleMapper(MwUserRoleMap
                    .builder()
                    .userId(userId)
                    .roleId(roleId)
                    .build()
            );
        }
        return null;
    }
}
