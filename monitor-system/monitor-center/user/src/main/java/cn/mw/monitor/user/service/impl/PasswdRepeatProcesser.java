package cn.mw.monitor.user.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.user.dao.MWPasswdHisDao;
import cn.mw.monitor.service.user.dto.MWPasswordPlanDTO;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.user.model.MWPasswdHis;
import cn.mw.monitor.user.service.IMWUserPostProcesser;
import cn.mw.monitor.user.service.PasswdRepeatCheck;
import cn.mw.monitor.event.AddUserEvent;
import cn.mw.monitor.event.PostUpdUserEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PasswdRepeatProcesser implements PasswdRepeatCheck, IMWUserPostProcesser {

    private static final Logger logger = LoggerFactory.getLogger("service-" + PasswdRepeatProcesser.class.getName());

    @Resource
    MWPasswdHisDao mwpasswdHisDao;

    @Override
    public List<Reply> repeatCheck(Integer userid, String passwd) {

        List<Reply> faillist = new ArrayList<Reply>();
        MWPasswdHis mwpasswdHis = new MWPasswdHis(userid, passwd);
        List<MWPasswdHis> list = mwpasswdHisDao.selectList(mwpasswdHis);
        if(null != list && list.size() > 0){
            faillist.add(Reply.warn(ErrorConstant.USER_MSG_100119, new String[]{"密码"}));
        }
        return faillist;
    }

    @Override
    public List<Reply> handleEvent(Event event){
        List<Reply> faillist = new ArrayList<Reply>();
        if(event instanceof AddUserEvent){
            //新增密码记录
            AddUserEvent addUserEvent = (AddUserEvent)event;
            UserDTO userDTO = addUserEvent.getUserDTO();
            MWPasswdHis mwpasswdHis = new MWPasswdHis(userDTO.getUserId(), addUserEvent.getPassword());
            try{
                mwpasswdHisDao.insert(mwpasswdHis);
            }catch (Exception e){
                logger.error(e.getMessage());
                faillist.add(Reply.fail(e.getMessage()));
                throw new ServiceException(faillist);
            }
        }

        if(event instanceof PostUpdUserEvent){
            PostUpdUserEvent postUpdUserEvent = (PostUpdUserEvent) event;
            //检查密码历史表,只保留要求的记录数
            UserDTO olduser  = postUpdUserEvent.getOldUserdto();
            UserDTO newuser  = postUpdUserEvent.getNewUserdto();

            if(null == newuser || null == newuser.getPassword() || "".equals(newuser.getPassword())){
                return faillist;
            }

            MWPasswordPlanDTO plan  = postUpdUserEvent.getPasswordPlanDTO();
            Integer userid = olduser.getUserId();
            Integer hisnum = plan.getHisNum();
            try{
                int count = mwpasswdHisDao.selectCount(userid);
                MWPasswdHis mwpasswdHis = new MWPasswdHis(userid, olduser.getPassword());
                mwpasswdHisDao.insert(mwpasswdHis);
                if(count >= hisnum){
                    mwpasswdHisDao.deleteRedun(userid, hisnum);
                }

            }catch (Exception e){
                logger.error(e.getMessage());
                faillist.add(Reply.fail(e.getMessage()));
                throw new ServiceException(faillist);
            }
        }

        return faillist;
    }
}
