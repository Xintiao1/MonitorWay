package cn.mw.monitor.service.action.service;

import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mw.monitor.service.action.param.UserIdsType;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;

import java.util.HashSet;
import java.util.List;

/**
 * @author xhy
 * @date 2020/12/18 9:31
 */
public interface CommonActionService {

    /**
     * 通过actionId查询要发送的用户
     * @param actionId
     * @return id 资产主键id
     */
    UserIdsType getActionUserIds(String actionId, String id);

    UserIdsType getActionUserIds(String actionId, String id, MwTangibleassetsDTO assets);

    /**
     * 通过actionId查询要带外资产发送的用户
     * @param actionId
     * @return id 资产主键id
     */
    UserIdsType getOutbandUserIds(String actionId, String id);

    /**
     * 通过actionId查询要抄送的用户
     * @param actionId
     * @return
     */
    HashSet<Integer> getActionEamilCCUserIds(String actionId);


    /**
     * 通过actionId查询要发送虚拟化的用户
     * @param actionId
     * @return hostid
     */
    HashSet<Integer> getVrUserIds(String actionId,String hostid);


    /**
     * 通过actionId查询对应要发送的资产
     * @param actionId
     * @return
     */
    List<String> getActionAssetsIds(String actionId);

    /**
     * 通过资产表id查询对应资产能够匹配的规则
     * @param id
     * @return
     */
    List<String> getActionByHostId(String id);

    AddAndUpdateAlertActionParam selectPopupAction(String actionId);


}
