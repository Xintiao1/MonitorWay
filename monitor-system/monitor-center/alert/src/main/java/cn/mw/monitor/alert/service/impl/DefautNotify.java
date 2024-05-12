package cn.mw.monitor.alert.service.impl;

import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mw.monitor.alert.param.UserTypeEnum;
import cn.mw.monitor.service.alert.callback.BusinessIds;
import cn.mw.monitor.service.alert.callback.BusinessIdsFetch;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWGroupCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.dto.DataAuthorityDTO;
import cn.mw.monitor.service.user.dto.GroupUserDTO;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefautNotify implements NotifyMethod{

    private MWCommonService mwCommonService;
    private MWGroupCommonService mwGroupCommonService;

    private MWOrgCommonService mwOrgCommonService;

    public DefautNotify(MWCommonService mwCommonService ,MWGroupCommonService mwGroupCommonService ,MWOrgCommonService mwOrgCommonService){
        this.mwCommonService = mwCommonService;
        this.mwGroupCommonService = mwGroupCommonService;
        this.mwOrgCommonService = mwOrgCommonService;
    }

    @Override
    public Set<Integer> getUserIds(MessageContext messageContext) {
        AddAndUpdateAlertActionParam param = (AddAndUpdateAlertActionParam)messageContext.getKey(AddAndUpdateAlertActionParam.MESSAGECONTEXT_KEY);
        BusinessIdsFetch businessIdsFetch = (BusinessIdsFetch)messageContext.getKey(BusinessIdsFetch.MESSAGECONTEXT_KEY);
        Set<Integer> useids = new HashSet<>();
        //默认用户，查询资产的用户
        if (null != param && null != businessIdsFetch) {
            BusinessIds businessIds = businessIdsFetch.getBusinessIds();
            if(null != businessIds && null != businessIds.getBusinessIds() && businessIds.getBusinessIds().size() > 0){
                List<DataAuthorityDTO> dataAuthorityDTOS = mwCommonService.getDataAuthById(businessIds.getDataType() ,businessIds.getBusinessIds());

                //资产负责人
                if(param.getUserTypes() == null || param.getUserTypes().contains(UserTypeEnum.USER.getName())) {
                    for (DataAuthorityDTO dataAuthorityDTO : dataAuthorityDTOS) {
                        useids.addAll(dataAuthorityDTO.getUserIdList());
                    }
                }

                //查询用户组中所有用户
                if(param.getUserTypes() == null || param.getUserTypes().contains(UserTypeEnum.GROUP.getName())){
                    List<Integer> groupids = new ArrayList<>();
                    for (DataAuthorityDTO dataAuthorityDTO : dataAuthorityDTOS) {
                        if(null != dataAuthorityDTO.getGroupIdList()){
                            groupids.addAll(dataAuthorityDTO.getGroupIdList());
                        }
                    }

                    if(groupids.size() > 0) {
                        Reply reply = mwGroupCommonService.selectGroupUsers(groupids);
                        if(null != reply && reply.getRes() == PaasConstant.RES_SUCCESS){
                            List<GroupUserDTO> guDTOlist = (List<GroupUserDTO>) reply.getData();
                            if(null != guDTOlist){
                                for (GroupUserDTO groupUserDTO: guDTOlist) {
                                    useids.add(groupUserDTO.getUserId());
                                }
                            }
                        }
                    }
                }

                //查询机构用户
                if(param.getUserTypes() == null || param.getUserTypes().contains(UserTypeEnum.ORG.getName())){
                    List<Integer> orgids = new ArrayList<>();
                    for (DataAuthorityDTO dataAuthorityDTO : dataAuthorityDTOS) {
                        if(null != dataAuthorityDTO.getGroupIdList()){
                            orgids.addAll(dataAuthorityDTO.getOrgIdList());
                        }
                    }

                    if(orgids.size() > 0){
                        List<Integer> orgUserIds = mwOrgCommonService.selectPubUserIdByOrgId(orgids);
                        if(null != orgUserIds){
                            useids.addAll(orgUserIds);
                        }
                    }
                }
            }
        }
        return useids;
    }
}
