package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.assets.service.IMWAssetsCheckProcesser;
import cn.mw.monitor.assets.service.PostAddTangibleAssetsListener;
import cn.mw.monitor.assets.service.PostUpdateTangibleAssetsListener;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.service.assets.api.IMWAssetsListener;
import cn.mw.monitor.service.assets.event.AddTangibleassetsEvent;
import cn.mw.monitor.service.assets.event.UpdateTangibleassetsEvent;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.state.DataType;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/4/17
 */
@Service
@Slf4j
public class PostAssetsProcesser implements PostUpdateTangibleAssetsListener, PostAddTangibleAssetsListener, IMWAssetsCheckProcesser, IMWAssetsListener {

    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsTableDao;

    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {
        //新建资产进行绑定事件
        if (event instanceof AddTangibleassetsEvent) {
            AddTangibleassetsEvent addEvent = (AddTangibleassetsEvent) event;
            List<Reply> faillist = processAddPostTangibleAssets(addEvent.getAddTangAssetsParam());
            return faillist;
        }

        //新建资产进行绑定事件
        if (event instanceof UpdateTangibleassetsEvent) {
            UpdateTangibleassetsEvent updateEvent = (UpdateTangibleassetsEvent) event;
            List<Reply> faillist = processUpdatePostTangibleAssets(updateEvent.getUpdateTangAssetsParam());
            return faillist;
        }
        return null;
    }

    @Override
    public List<Reply> processAddPostTangibleAssets(AddUpdateTangAssetsParam aParam) {
        List<Reply> faillist = new ArrayList<>();
        try {
            //添加负责人
            addMapperAndPerm(aParam);

            //插入agent信息
            if (1 == aParam.getMonitorMode() ) {
                if (aParam.getAgentAssetsDTO() != null) {
                    aParam.getAgentAssetsDTO().setAssetsId(aParam.getId());
                    mwTangibleAssetsTableDao.createAssetsAgent(aParam.getAgentAssetsDTO());
                }
            } else if (2 == aParam.getMonitorMode()) {
                //插入snmp信息
                RuleType rt = RuleType.valueOf(aParam.getVersion());
                switch (rt){
                    case SNMPv1v2:
                        aParam.getSnmpV1AssetsDTO().setAssetsId(aParam.getId());
                        mwTangibleAssetsTableDao.createAssetsSnmpv12(aParam.getSnmpV1AssetsDTO());
                        break;
                    case SNMPv3:
                        aParam.getSnmpAssetsDTO().setAssetsId(aParam.getId());
                        mwTangibleAssetsTableDao.createAssetsSnmpv3(aParam.getSnmpAssetsDTO());
                        break;
                    default:
                }
            } else if (3 == aParam.getMonitorMode()) {
                //插入port信息
                if (null != aParam.getPortAssetsDTO()) {
                    aParam.getPortAssetsDTO().setAssetsId(aParam.getId());
                    mwTangibleAssetsTableDao.createAssetsPort(aParam.getPortAssetsDTO());
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            faillist.add(Reply.fail(e.getMessage()));
            throw new ServiceException(faillist);
        }
        return faillist;
    }

    @Override
    public List<Reply> processUpdatePostTangibleAssets(AddUpdateTangAssetsParam uParam) {
        List<Reply> faillist = new ArrayList<>();
        try {
            MwTangibleassetsDTO oldassets = mwTangibleAssetsTableDao.check(QueryTangAssetsParam.builder().id(uParam.getId()).build()).get(0);
            //删除负责人
            DeleteDto deleteDto = DeleteDto.builder().typeId(uParam.getId()).type(DataType.ASSETS.getName()).build();
            mwCommonService.deleteMapperAndPerm(deleteDto);

            //添加负责人
            addMapperAndPerm(uParam);

            //绑定资产与机构关系
           /* if (null != uParam.getOrgIds() && uParam.getOrgIds().size() >0) {
                mwTangibleAssetsTableDao.deleteAssetsOrgMapperByAssetsId(uParam.getId());
                if (uParam.getOrgIds().size() > 0) {
                    List<MwAssetsOrgMapper> orgmapper = new ArrayList<>();
                    uParam.getOrgIds().forEach(
                            orgIds -> orgmapper.add(MwAssetsOrgMapper.builder().orgId(orgIds.get(orgIds.size() - 1)).assetsId(uParam.getId()).build())
                    );
                    mwTangibleAssetsTableDao.createAssetsOrgMapper(orgmapper);
                }
            }

            //绑定资产与用户组关系
            if (null != uParam.getGroupIds() && uParam.getGroupIds().size() >0) {
                mwTangibleAssetsTableDao.deleteAssetsGroupMapperByAssetsId(uParam.getId());
                if (uParam.getGroupIds().size() > 0) {
                    List<MwAssetsGroupMapper> groupMapper = new ArrayList<>();
                    uParam.getGroupIds().forEach(
                            groupId -> groupMapper.add(MwAssetsGroupMapper.builder().assetsId(uParam.getId()).groupId(groupId).build())
                    );
                    mwTangibleAssetsTableDao.createAssetsGroupMapper(groupMapper);
                }
            }

            //绑定资产与用户关系
            if (null != uParam.getPrincipal() && uParam.getPrincipal().size() >0) {
                mwTangibleAssetsTableDao.deleteAssetsUserMapperByAssetsId(uParam.getId());
                if (uParam.getPrincipal().size() > 0) {
                    List<MwAssetsUserMapper> userMapper = new ArrayList<>();
                    uParam.getPrincipal().forEach(
                            userId -> userMapper.add(MwAssetsUserMapper.builder().assetsId(uParam.getId()).userId(userId).build())
                    );
                    mwTangibleAssetsTableDao.createAssetsUserMapper(userMapper);
                }
            }*/
            //如果管理方式改变需要重置子表
            if (oldassets.getMonitorMode() != uParam.getMonitorMode()) {
                if (oldassets.getMonitorMode() == RuleType.ZabbixAgent.getMonitorMode()) {
                    mwTangibleAssetsTableDao.deleteAssetsAgentByAssetsId(uParam.getId());

                } else if (oldassets.getMonitorMode() == RuleType.SNMPv1v2.getMonitorMode()) {
                    if (oldassets.getSnmpLev() == 1) {
                        mwTangibleAssetsTableDao.deleteAssetsSnmpv12ByAssetsId(uParam.getId());
                    } else {
                        mwTangibleAssetsTableDao.deleteAssetsSnmpv3ByAssetsId(uParam.getId());
                    }

                } else if (oldassets.getMonitorMode() == RuleType.Port.getMonitorMode()) {
                    mwTangibleAssetsTableDao.deleteAssetsPortByAssetsId(uParam.getId());

                } else if (oldassets.getMonitorMode() == RuleType.IOT.getMonitorMode()){
                    mwTangibleAssetsTableDao.deleteAssetsIOTByAssetsId(uParam.getId());
                }

                //插入agent信息
                if (RuleType.ZabbixAgent.getMonitorMode() == uParam.getMonitorMode()) {
                    if (uParam.getAgentAssetsDTO() != null) {
                        uParam.getAgentAssetsDTO().setAssetsId(uParam.getId());
                        mwTangibleAssetsTableDao.createAssetsAgent(uParam.getAgentAssetsDTO());
                    }

                } else if (uParam.getMonitorMode() == RuleType.SNMPv1v2.getMonitorMode()) {
                    //插入snmpv1/2信息
                    if (null != uParam.getSnmpAssetsDTO()) {
                        if (uParam.getSnmpLev() == 1)
                            uParam.getSnmpV1AssetsDTO().setAssetsId(uParam.getId());
                        mwTangibleAssetsTableDao.createAssetsSnmpv12(uParam.getSnmpV1AssetsDTO());
                    } else {
                        //插入snmpv3信息
                        uParam.getSnmpAssetsDTO().setAssetsId(uParam.getId());
                        mwTangibleAssetsTableDao.createAssetsSnmpv3(uParam.getSnmpAssetsDTO());
                    }

                } else if (uParam.getMonitorMode() == RuleType.Port.getMonitorMode()) {
                    //插入port信息
                    if (null != uParam.getPortAssetsDTO()) {
                        uParam.getPortAssetsDTO().setAssetsId(uParam.getId());
                        mwTangibleAssetsTableDao.createAssetsPort(uParam.getPortAssetsDTO());
                    }
                }

            } else if (uParam.getMonitorMode() == RuleType.IOT.getMonitorMode()) {
                //插入port信息
                if (null != uParam.getMwIOTAssetsDTO()) {
                    uParam.getMwIOTAssetsDTO().setAssetsId(uParam.getId());
                    mwTangibleAssetsTableDao.createAssetsIOT(uParam.getMwIOTAssetsDTO());
                }
            }
             //删除标签参数
            mwLabelCommonServcie.deleteLabelBoard(uParam.getId(), DataType.ASSETS.getName());
            //插入标签参数
            if(null != uParam.getAssetsLabel() && uParam.getAssetsLabel().size() >0){

                mwLabelCommonServcie.insertLabelboardMapper(uParam.getAssetsLabel(),uParam.getId(), DataType.ASSETS.getName());
//                mwTangibleAssetsTableDao.deleteAssetsLabelByAssetsId(uParam.getId());
//                //下拉框和文本框放在同一个字段上保存
//                uParam.getAssetsLabel().forEach(
//                        mwAssetsLabelDTO -> {
//                            if(null != mwAssetsLabelDTO.getDropId())
//                                mwAssetsLabelDTO.setTagboard(mwAssetsLabelDTO.getDropId().toString());
//                            mwAssetsLabelDTO.setAssetsId(uParam.getId());
//                        });
//                mwTangibleAssetsTableDao.createAssetsLabel(uParam.getAssetsLabel());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            faillist.add(Reply.fail(e.getMessage()));
            throw new ServiceException(faillist);
        }
        return faillist;
    }

    /**
     * 添加負責人
     * @param uParam
     */
    private void addMapperAndPerm(AddUpdateTangAssetsParam uParam) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(uParam.getGroupIds())  //用户组
                .userIds(uParam.getPrincipal()) //责任人
                .orgIds(uParam.getOrgIds()) //机构
                .typeId(uParam.getId())    //资产数据主键
                .type(DataType.ASSETS.getName())  //ASSETS
                .desc(DataType.ASSETS.getDesc()).build(); //资产
        mwCommonService.addMapperAndPerm(insertDto);
    }

}
