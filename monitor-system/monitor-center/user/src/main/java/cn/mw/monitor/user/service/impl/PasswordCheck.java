package cn.mw.monitor.user.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.Constants;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.event.AddUserEvent;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.event.GenPasswdEvent;
import cn.mw.monitor.event.UpdUserEvent;
import cn.mw.monitor.service.user.dto.MWPasswordPlanDTO;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.model.MWPasswdPlan;
import cn.mw.monitor.shiro.PasswordManage;
import cn.mw.monitor.user.dao.MWPasswCompTypeDao;
import cn.mw.monitor.user.dao.MWPasswdDAO;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mw.monitor.user.model.MWPassCompType;
import cn.mw.monitor.user.model.MWPasswdInform;
import cn.mw.monitor.user.service.IMWUserListener;
import cn.mw.monitor.user.service.PasswdComCheck;
import cn.mw.monitor.user.service.PasswdRepeatCheck;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class PasswordCheck implements IMWUserListener, InitializingBean {

    @Resource
    private MWPasswdDAO mwpasswdDAO;

    @Autowired
    PasswordManage passwordManage;

    @Resource
    private MWUserDao mwuserDao;

    @Resource
    MWPasswCompTypeDao mwpasswCompTypeDao;

    @Autowired
    PasswdRepeatCheck passwdRepeatCheck;

    private Map<Integer, PasswdComCheck> passwdComCheckMap;


    @Override
    public List<Reply> handleEvent(Event event) {

        List<Reply> faillist = new ArrayList<Reply>();
        if(event instanceof AddUserEvent){
            AddUserEvent addUserEvent = (AddUserEvent) event;
            List<Reply> retlist = proccessAddUserEvent(addUserEvent);
            faillist.addAll(retlist);
        }

        if(event instanceof UpdUserEvent){
            UpdUserEvent updUserEvent = (UpdUserEvent) event;
            List<Reply> retlist = proccessUpdUserEvent(updUserEvent);
            faillist.addAll(retlist);
        }

        //对生成密码进行检查
        if(event instanceof GenPasswdEvent){
            GenPasswdEvent genPasswdEvent = (GenPasswdEvent) event;
            List<Reply> retlist = proccessGenPasswdEvent(genPasswdEvent);
            faillist.addAll(retlist);
        }
        return faillist;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        passwdComCheckMap = new HashMap<Integer, PasswdComCheck>();
        List<MWPassCompType> mwlist = mwpasswCompTypeDao.selectList();
        for(MWPassCompType mwpassCompType : mwlist){
            PasswdComCheck passwdComCheck = new PasswdComCheckImpl(mwpassCompType.getTypeNum());
            passwdComCheckMap.put(mwpassCompType.getId(), passwdComCheck);
        }

    }

    private List<Reply> proccessAddUserEvent(AddUserEvent addUserEvent){
        List<Reply> faillist = new ArrayList<Reply>();
        MWPasswdPlan mwPasswdPlan = mwpasswdDAO.selectById(Constants.defaultPasswdPlanId);

        //检查密码长度
        if(addUserEvent.getPassword().length() < mwPasswdPlan.getPasswdMinLen()){
            faillist.add(Reply.warn(ErrorConstant.USER_MSG_100110 + mwPasswdPlan.getPasswdMinLen()));
        }

        //检查密码复杂度
        PasswdComCheck passwdComCheck = passwdComCheckMap.get(mwPasswdPlan.getPasswdComplexId());
        List<Reply> retlist = passwdComCheck.checkComplex(addUserEvent.getPassword());
        if(retlist.size() > 0){
            faillist.addAll(retlist);
        }

        return faillist;
    }

    private List<Reply> proccessUpdUserEvent(UpdUserEvent updUserEvent){
        List<Reply> faillist = new ArrayList<Reply>();
        UserDTO newuserDTO = updUserEvent.getNewuserdto();
        MWPasswordPlanDTO mwPasswdPlan = updUserEvent.getMwpasswordPlanDTO();
        String passwd = updUserEvent.getPassword();

       /* //禁止修改登录名
        if(!StringUtils.isEmpty(newuserDTO.getLoginName())){
            faillist.add(Reply.warn(ErrorConstant.USER_MSG_100117 , new String[]{"登录名"}));
        }*/

        boolean ispasswordEmpty = StringUtils.isEmpty(passwd);
        if(!ispasswordEmpty){
            //检查密码长度
            if(passwd.length() < mwPasswdPlan.getPasswdMinLen()) {
                faillist.add(Reply.warn(ErrorConstant.USER_MSG_100110 + mwPasswdPlan.getPasswdMinLen()));
            }
            //检查密码复杂度
            PasswdComCheck passwdComCheck = passwdComCheckMap.get(mwPasswdPlan.getPasswdComplexId());
            List<Reply> retlist = passwdComCheck.checkComplex(passwd);
            if(retlist.size() > 0){
                faillist.addAll(retlist);
            }


        }

//        //盐值和散列类型修改时,密码也需要一起修改
//        if((!StringUtils.isEmpty(newuserDTO.getSalt()) || !StringUtils.isEmpty(newuserDTO.getHashTypeId()))
//                && ispasswordEmpty){
//            faillist.add(Reply.fail(ErrorConstant.USER_MSG_100111 ));
//        }

        return faillist;
    }

    private List<Reply> proccessGenPasswdEvent(GenPasswdEvent genPasswdEvent){
        List<Reply> faillist = new ArrayList<Reply>();
        UserDTO userDTO = genPasswdEvent.getUserdto();
        MWPasswdInform mwPasswdInform = mwuserDao.selectInformByUserId(userDTO.getUserId());
        MWPasswdPlan mwPasswdPlan;
        if (mwPasswdInform != null) {
             mwPasswdPlan = mwpasswdDAO.selectById(mwPasswdInform.getInoperactivePasswdPlan());
        }else {
            mwPasswdPlan = mwpasswdDAO.selectById(userDTO.getActivePasswdPlan());
        }
        if (mwPasswdPlan.getHisCheckEnable()) {
            //检查密码是否使用过
            List<Reply> list = passwdRepeatCheck.repeatCheck(userDTO.getUserId(), genPasswdEvent.getPassword());
            if(list.size() > 0){
                faillist.addAll(list);
            }
        }

        return faillist;
    }
}
