package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.dao.MwAssetsLabelDao;
import cn.mw.monitor.assets.dto.AssetsLabel;
import cn.mw.monitor.assets.dto.AssetsLabelDTO;
import cn.mw.monitor.assets.param.QueryAssetsLabelParam;
import cn.mw.monitor.assets.service.MwAssetsLabelService;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2021/7/22 18:26
 * @Version 1.0
 */
@Service
@Slf4j
public class MwAssetsLabelServiceImpl implements MwAssetsLabelService {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/assets");
    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Resource
    MwAssetsLabelDao mwAssetsLabelDao;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;


    @Override
    public Reply selectLabelList(QueryAssetsLabelParam qParam) {
        String moduleType = "";
        String tableName = "";
        switch (qParam.getTableType()) {
            case 1:
                moduleType = DataType.ASSETS.getName();
                tableName = "mw_tangibleassets_table";
                break;
            case 2:
                moduleType = DataType.INASSETS.getName();
                tableName = "mw_intangibleassets_table";
                break;
            case 3:
                moduleType = DataType.OUTBANDASSETS.getName();
                tableName = "mw_outbandassets_table";
                break;
            default:
                break;
        }
        List<AssetsLabel> list = new ArrayList<>();
        if (qParam.getAssetsIds() != null && qParam.getAssetsIds().size() == 0) {
            return Reply.ok(list);
        }

        qParam.setTableName(tableName);
        qParam.setModuleType(moduleType);

        List<String> assetsIds = new ArrayList();
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            String perm = iLoginCacheInfo.getRoleInfo().getDataPerm(); //数据权限：private public
            DataPermission dataPermission = DataPermission.valueOf(perm);
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);//用户所在的用户组id
            if (null != groupIds && groupIds.size() > 0) {
                qParam.setGroupIds(groupIds);
            }
            if (null != qParam.getLogicalQueryLabelParamList() && qParam.getLogicalQueryLabelParamList().size() > 0) {
                assetsIds = mwLabelCommonServcie.getTypeIdsByLabel(qParam.getLogicalQueryLabelParamList());
                if (assetsIds.size() > 0) {
                    qParam.setAssetsIds(assetsIds);
                }
            }
            switch (dataPermission) {
                case PRIVATE:
                    qParam.setUserId(userId);
                    Map priCriteria = PropertyUtils.describe(qParam);
                    assetsIds = mwAssetsLabelDao.selectPriList(priCriteria);
                    break;
                case PUBLIC:
                    String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                    List<Integer> orgIds = new ArrayList<>();
                    Boolean isAdmin = false;
                    if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                        isAdmin = true;
                    }
                    if (!isAdmin) {
                        orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);
                    }
                    if (null != orgIds && orgIds.size() > 0) {
                        qParam.setOrgIds(orgIds);
                    }
                    Map pubCriteria = PropertyUtils.describe(qParam);
                    assetsIds = mwAssetsLabelDao.selectPubList(pubCriteria);
                    break;
            }
        } catch (Exception e) {
            logger.error("查询资产标签信息失败！" + e.getMessage());
            return Reply.fail("查询资产标签信息失败！");
        }

        if (assetsIds.size() > 0) {
            List<AssetsLabelDTO> assetsLabelDTOS = mwAssetsLabelDao.selectAssetsLabels(moduleType, assetsIds);
            if (assetsLabelDTOS != null && assetsLabelDTOS.size() > 0) {

                Map<String, List<String>> collect = assetsLabelDTOS.stream()
                        .collect(Collectors.groupingBy(AssetsLabelDTO::getFirstLabel, Collectors.mapping(AssetsLabelDTO::getAssetsId, Collectors.toList())));
                Map<String, List<String>> secondCollect = assetsLabelDTOS.stream()
                        .collect(Collectors.groupingBy(AssetsLabelDTO::getTotalLabel, Collectors.mapping(AssetsLabelDTO::getAssetsId, Collectors.toList())));
                if (collect != null) {
                    collect.putAll(secondCollect);
                    for (Map.Entry<String, List<String>> map : collect.entrySet()) {
                        AssetsLabel assetsLabel = AssetsLabel.builder()
                                .labelName(map.getKey()+ "(" + (map.getValue() != null ? map.getValue().size() : 0) + ")").assetsIds(map.getValue()).build();
                        list.add(assetsLabel);
                    }
                }
            }
        }
        return Reply.ok(list);
    }
}
