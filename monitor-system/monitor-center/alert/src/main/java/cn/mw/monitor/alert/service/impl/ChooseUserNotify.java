package cn.mw.monitor.alert.service.impl;

import cn.mw.monitor.alert.dao.MwAlertActionDao;
import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mw.monitor.service.user.api.MWGroupCommonService;
import cn.mw.monitor.service.user.dto.GroupUserDTO;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChooseUserNotify implements NotifyMethod{
    private String actionId;

    private MwAlertActionDao mwAlertActionDao;

    private MWGroupCommonService mwGroupCommonService;

    public ChooseUserNotify(String actionId ,MwAlertActionDao mwAlertActionDao ,MWGroupCommonService mwGroupCommonService){
        this.actionId = actionId;
        this.mwAlertActionDao = mwAlertActionDao;
        this.mwGroupCommonService = mwGroupCommonService;
    }

    @Override
    public Set<Integer> getUserIds(MessageContext messageContext) {
        AddAndUpdateAlertActionParam param = (AddAndUpdateAlertActionParam)messageContext.getKey(AddAndUpdateAlertActionParam.MESSAGECONTEXT_KEY);
        Set<Integer> userIds = new HashSet<>();
        if(null != param.getActionUserIds()){
            userIds.addAll(param.getActionUserIds());
        }

        if (null != param.getGroupIds() && param.getGroupIds().size() > 0) {
            Reply reply = mwGroupCommonService.selectGroupUsers(param.getGroupIds());
            if(null != reply && reply.getRes() == PaasConstant.RES_SUCCESS){
                List<GroupUserDTO> guDTOlist = (List<GroupUserDTO>) reply.getData();
                if(null != guDTOlist){
                    for (GroupUserDTO groupUserDTO: guDTOlist) {
                        userIds.add(groupUserDTO.getUserId());
                    }
                }
            }
        }
        return userIds;
    }
}
