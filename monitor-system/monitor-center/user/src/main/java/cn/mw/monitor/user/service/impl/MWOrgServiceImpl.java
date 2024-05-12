package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.api.exception.CheckDeleteOrgException;
import cn.mw.monitor.api.exception.CheckOrgNameException;
import cn.mw.monitor.api.exception.CheckUpdateOrgStateException;
import cn.mw.monitor.api.param.org.*;
import cn.mw.monitor.common.constant.Constants;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.model.OrgMapperDTO;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.dto.*;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.listener.LoginContext;
import cn.mw.monitor.service.user.model.MWOrg;
import cn.mw.monitor.shiro.UserType;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.state.UserActiveState;
import cn.mw.monitor.user.dao.*;
import cn.mw.monitor.user.dto.MwOrgLongitudeDto;
import cn.mw.monitor.user.dto.UserGroupDTO;
import cn.mw.monitor.user.dto.UserOrgDTO;
import cn.mw.monitor.user.model.MwUserOrgTable;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.user.service.view.OrgView;
import cn.mw.monitor.util.ErrorMsgUtils;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.Pinyin4jUtil;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MWOrgServiceImpl implements MWOrgService, MWOrgCommonService {

    public static final Integer ROOT_ORG = 0;

    @Value("${datasource.check}")
    private String check;

    @Resource
    private MWOrgDao mworgDao;

    @Resource
    private MwUserOrgMapperDao mwUserOrgMapperDao;

    @Resource
    private MwGroupOrgMapperDao mwGroupOrgMapperDao;

    @Resource
    private MwOrgAssetsMapperDao mwOrgAssetsMapperDao;

    @Resource
    private MWUserDao mwUserDao;

    @Resource
    private MwOrgMonitorMapperDao mwOrgMonitorMapperDao;

    @Resource
    private MwOrgEngineMapperDao mwOrgEngineMapperDao;

    @Autowired
    private ILoginCacheInfo loginCacheInfo;

    /**
     * mysql
     */
    private static final String DATABASE_MYSQL = "mysql";

    /**
     * oracle
     */
    private static final String DATABASE_ORACLE = "oracle";

    /**
     * 绑定用户
     */
    @Override
    public Reply bindUserOrg(BindUserOrgParam qParam) {
        try {
            if (qParam.getUserIds().size() > 0) {
                List<MwUserOrgTable> ouList = new ArrayList<>();
                if (qParam.getUserIds().size() >= qParam.getOrgIds().size()) {
                    qParam.getUserIds().forEach(
                            userId -> ouList.add(MwUserOrgTable
                                    .builder()
                                    .orgId(qParam.getOrgIds().get(0))
                                    .userId(userId)
                                    .build()
                            )
                    );
                } else {
                    qParam.getOrgIds().forEach(
                            orgId -> ouList.add(MwUserOrgTable
                                    .builder()
                                    .orgId(orgId)
                                    .userId(qParam.getUserIds().get(0))
                                    .build()
                            )
                    );
                }
                if (qParam.getFlag() == 2 || qParam.getFlag() == 4) {
                    if (qParam.getFlag() == 4) {
                        mwUserOrgMapperDao.deleteBatchByOrgId(qParam.getOrgIds());
                    } else {
                        mwUserOrgMapperDao.deleteBatchByUserId(qParam.getUserIds());
                    }
                }
                mwUserOrgMapperDao.insertBatch(ouList);
            } else {
                mwUserOrgMapperDao.deleteBatchByOrgId(qParam.getOrgIds());
            }
            return Reply.ok("绑定成功！");
        } catch (Exception e) {
            log.error("fail to bindUserOrg with BindUserOrgParam=【{}】, cause:【{}】",
                    qParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ORG_100409, ErrorConstant.ORG_MSG_100409));
        }
    }

    /**
     * 新增机构信息
     */
    @Override
    public Reply insert(AddUpdateOrgParam aParam) {
        try {
            insertOrg(aParam);
            return Reply.ok("新增成功！");
        } catch (Exception e) {
            log.error("fail to insertOrg with AddUpdateOrgParam=【{}】, cause:【{}】",
                    aParam, e.getMessage());
            if (e instanceof CheckOrgNameException) {
                throw e;
            }
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ORG_100401, ErrorConstant.ORG_MSG_100401));
        }
    }

    /**
     * 新增机构信息
     *
     * @param aParam
     * @return orgId
     */
    @Override
    public int insertOrg(AddUpdateOrgParam aParam) {
        String coordinate = aParam.getCoordinate();
        if (StringUtils.isNotBlank(coordinate)) {
            if (coordinate.split(",").length < 3) {
                aParam.setCoordinate(aParam.getOrgName() + "," + coordinate);
            }
            if (coordinate.split(",").length == 3) {
                aParam.setCoordinate(aParam.getOrgName() + "," + coordinate.split(",")[1] + "," + coordinate.split(",")[2]);
            }
        }
        MWOrg mwOrg = CopyUtils.copy(MWOrg.class, aParam);
        Integer count = 0;
        if (mwOrg.getPid() != null) {
            count = mworgDao.selectPidCount(mwOrg.getOrgName(), mwOrg.getPid());
        } else {
            count = mworgDao.selectOneOrgByName(mwOrg.getOrgName());
        }
        if (count != null && count > 0) {
            throw new CheckOrgNameException(ErrorConstant.ORG_MSG_100418);
        }
        String creator = loginCacheInfo.getLoginName();
        mwOrg.setCreator(creator);
        mwOrg.setModifier(creator);
        Map<String, Object> map = null;
        // 判断是否不为一级机构
        if (null != mwOrg.getPid() && mwOrg.getPid() != ROOT_ORG) {
            mworgDao.updateIsNoteById(mwOrg.getPid(), false, creator);
            map = mworgDao.selectDeepNodesById(mwOrg.getPid());
            mwOrg.setDeep(Integer.parseInt(String.valueOf(map.get("deep"))) + 1);
        } else {
            mwOrg.setPid(ROOT_ORG);
            mwOrg.setDeep(1);
        }
        //编辑机构内部信息
        mwOrg.setIsNode(true);
        mworgDao.insert(mwOrg);
        if (null == map) {
            mwOrg.setNodes("," + mwOrg.getOrgId() + ",");
        } else {
            mwOrg.setNodes(map.get("nodes").toString() + mwOrg.getOrgId() + ",");
        }
        mworgDao.updateNoteByOrgId(mwOrg.getOrgId(), mwOrg.getNodes());
        return mwOrg.getOrgId();
    }

    /**
     * 删除机构信息
     */
    @Override
    public Reply delete(List<Integer> orgIds) {
        try {
            Map<String, StringBuffer> maps = new HashMap<>();
            orgIds.forEach(
                    orgId -> {
                        String name = mworgDao.selectOrgNameById(orgId);
                        int count = mworgDao.countOrgByPid(orgId);
                        if (count > 0) {
                            if (maps.get(ErrorConstant.ORG_MSG_100413) == null) {
                                maps.put(ErrorConstant.ORG_MSG_100413, new StringBuffer());
                            }
                            maps.get(ErrorConstant.ORG_MSG_100413).append("、").append(name);
                        }
                        count = mwUserOrgMapperDao.countByOrgId(orgId);
                        if (count > 0) {
                            if (maps.get(ErrorConstant.ORG_MSG_100407) == null) {
                                maps.put(ErrorConstant.ORG_MSG_100407, new StringBuffer());
                            }
                            maps.get(ErrorConstant.ORG_MSG_100407).append("、").append(name);
                        }
                        count = mwGroupOrgMapperDao.countByOrgId(orgId);
                        if (count > 0) {
                            if (maps.get(ErrorConstant.ORG_MSG_100415) == null) {
                                maps.put(ErrorConstant.ORG_MSG_100415, new StringBuffer());
                            }
                            maps.get(ErrorConstant.ORG_MSG_100415).append("、").append(name);
                        }/*
                        count = mwOrgAssetsMapperDao.countAssetsByOrgId(orgId);
                        if (count > 0) {
                            if (maps.get(ErrorConstant.ORG_MSG_100410) == null) {
                                maps.put(ErrorConstant.ORG_MSG_100410, new StringBuffer());
                            }
                            maps.get(ErrorConstant.ORG_MSG_100410).append("、").append(name);
                        }
                        count = mwOrgMonitorMapperDao.countMonitorByOrgId(orgId);
                        if (count > 0) {
                            if (maps.get(ErrorConstant.ORG_MSG_100411) == null) {
                                maps.put(ErrorConstant.ORG_MSG_100411, new StringBuffer());
                            }
                            maps.get(ErrorConstant.ORG_MSG_100411).append("、").append(name);
                        }*/
                        count = mwOrgEngineMapperDao.countEngineByOrgId(orgId);
                        if (count > 0) {
                            if (maps.get(ErrorConstant.ORG_MSG_100412) == null) {
                                maps.put(ErrorConstant.ORG_MSG_100412, new StringBuffer());
                            }
                            maps.get(ErrorConstant.ORG_MSG_100412).append("、").append(name);
                        }
                        if (Constants.SYSTEM_ORG.equals(orgId)){
                            if (maps.get(ErrorConstant.ORG_MSG_100419) == null) {
                                maps.put(ErrorConstant.ORG_MSG_100419, new StringBuffer());
                            }
                            maps.get(ErrorConstant.ORG_MSG_100419).append("、").append(name);
                        }
                    }
            );
            String msg = ErrorMsgUtils.getErrorMsg(maps);
            if (StringUtils.isNotEmpty(msg)) {
                throw new CheckDeleteOrgException(ErrorConstant.ORG_MSG_100402, msg);
            }
            mworgDao.delete(orgIds, loginCacheInfo.getLoginName());
            // 删除机构和资产关联关系
            mwOrgAssetsMapperDao.deleteOrgMapper(orgIds);
            return Reply.ok("删除成功！");
        } catch (Exception e) {
            log.error("fail to deleteOrg with orgIds=【{}】, cause:【{}】",
                    orgIds, e.getMessage());
            if (e instanceof CheckDeleteOrgException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.ORG_100402, ErrorConstant.ORG_MSG_100402));
            }
        }
    }

    /**
     * 机构状态修改
     */
    @Override
    public Reply updateOrgState(UpdateOrgStateParam bParam) {
        try {
            MWOrg mwOrg = CopyUtils.copy(MWOrg.class, bParam);
            mwOrg.setModifier(loginCacheInfo.getLoginName());
            if (UserActiveState.DISACTIVE.getName().equals(mwOrg.getEnable())) {
                List<Integer> userIds = mworgDao.selectUserIdByOrgId(mwOrg.getOrgId());
                if (userIds.size() > 0) {
                    mworgDao.updateUserState(userIds, mwOrg.getEnable(), mwOrg.getModifier());
                }
                mworgDao.updateChildOrgState(mwOrg);
            } else {
                String enable = mworgDao.selectOrgEnableByChildOrgId(mwOrg.getOrgId());
                if (enable != null && UserActiveState.DISACTIVE.getName().equals(enable)) {
                    throw new CheckUpdateOrgStateException(
                            ErrorConstant.ORG_100414, ErrorConstant.ORG_MSG_100414);
                }
                mworgDao.updateOrgState(mwOrg);
            }
            return Reply.ok("更新状态成功！");
        } catch (Exception e) {
            log.error("fail to updateOrgState with UpdateOrgStateParam=【{}】, cause:【{}】",
                    bParam, e.getMessage());
            if (e instanceof CheckUpdateOrgStateException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.ORG_100408, ErrorConstant.ORG_MSG_100408));
            }
        }
    }

    /**
     * 更新机构信息
     */
    @Override
    public Reply update(AddUpdateOrgParam uParam) {
        try {
            //批量修改只支持修改机构描述，邮政编码，联系人，联系电话
            if (CollectionUtils.isNotEmpty(uParam.getOrgIdList())) {
                MWOrg orgInfo = new MWOrg();
                orgInfo.setOrgDesc(StringUtils.isEmpty(uParam.getOrgDesc()) ? "" : uParam.getOrgDesc());
                orgInfo.setPostCode(StringUtils.isEmpty(uParam.getPostCode()) ? "" : uParam.getPostCode());
                orgInfo.setContactPerson(StringUtils.isEmpty(uParam.getContactPerson()) ? "" : uParam.getContactPerson());
                orgInfo.setContactPhone(StringUtils.isEmpty(uParam.getContactPhone()) ? "" : uParam.getContactPhone());
                orgInfo.setModifier(loginCacheInfo.getLoginName());
                for (Integer orgId : uParam.getOrgIdList()) {
                    orgInfo.setOrgId(orgId);
                    mworgDao.update(orgInfo);
                }
            } else {
                String coordinate = uParam.getCoordinate();
                if(StringUtils.isNotBlank(coordinate)){
                    if(coordinate.split(",").length < 3){
                        uParam.setCoordinate(uParam.getOrgName()+","+coordinate);
                    }
                    if(coordinate.split(",").length == 3){
                        uParam.setCoordinate(uParam.getOrgName()+","+coordinate.split(",")[1]+","+coordinate.split(",")[2]);
                    }
                }
                MWOrg mwOrg = CopyUtils.copy(MWOrg.class, uParam);
                mwOrg.setModifier(loginCacheInfo.getLoginName());
                mworgDao.update(mwOrg);
            }
            return Reply.ok("更新成功！");
        } catch (Exception e) {
            log.error("fail to updateOrg with AddUpdateOrgParam=【{}】, cause:【{}】",
                    uParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ORG_100403, ErrorConstant.ORG_MSG_100403));
        }
    }

    private void getChild(MWOrgDTO mwOrgDTO, List<MWOrgDTO> mwOrgDTOList, Set<Integer> orgIdSet) {
        List<MWOrgDTO> childList = new ArrayList<>();
        mwOrgDTOList.stream()
                // 判断是否已循环过当前对象
                .filter(child -> !orgIdSet.contains(child.getOrgId()))
                // 判断是否为父子关系
                .filter(child -> child.getPid().equals(mwOrgDTO.getOrgId()))
                // orgIdSet集合大小不超过mwOrgDTOList的大小
                .filter(child -> orgIdSet.size() <= mwOrgDTOList.size())
                .forEach(
                        // 放入orgIdSet,递归循环时可以跳过这个项目，提交循环效率
                        child -> {
                            orgIdSet.add(child.getOrgId());
                            //获取当前类目的子类目
                            getChild(child, mwOrgDTOList, orgIdSet);
                            childList.add(child);
                        }
                );
        mwOrgDTO.addChild(childList);
    }

    /**
     * 机构列表查询
     */
    @Override
    public Reply selectList(QueryOrgParam param) {
        try {
            int fromIndex = (param.getPageNumber() - 1) * param.getPageSize();
            int toIndex = param.getPageNumber() * param.getPageSize();
            Map criteria = PropertyUtils.describe(param);
            List<MWOrgDTO> mworges = mworgDao.selectList(criteria);
            // TODO: 2023/1/10  19点04分 需要前端配置更改userDTOS的获取方式
            for (MWOrgDTO org : mworges) {
                List<MwSubUserDTO> userList = mworgDao.selectUser(org.getOrgId());
                org.setUserDTOS(userList);
            }
            //处理经纬度数据 gengjb
            hangdleOrgLonAndLat(mworges);
            List<MWOrgDTO> orgTopList = new ArrayList<>();
            List<MWOrgDTO> childList = new ArrayList<>();
            mworges.forEach(
                    mwOrgDTO -> {
                        // 深度为1时，是最高层
                        if (mwOrgDTO.getDeep() == 1) {
                            orgTopList.add(mwOrgDTO);
                        } else {
                            childList.add(mwOrgDTO);
                        }
                    }
            );
            Set<Integer> orgIdSet = new HashSet<>(childList.size());
            orgTopList.forEach(
                    orgTop -> getChild(orgTop, childList, orgIdSet)
            );
            if (fromIndex < 0 || fromIndex > orgTopList.size()) {
                fromIndex = 0;
                toIndex = param.getPageSize();
            }
            if (toIndex > orgTopList.size()) {
                toIndex = orgTopList.size();
            }
            List<MWOrgDTO> resultList = orgTopList.subList(fromIndex, toIndex);
            PageInfo<?> pageInfo = new PageInfo<>(resultList);
            pageInfo.setTotal(orgTopList.size());
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectListOrg with QueryOrgParam=【{}】, cause:【{}】",
                    param, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ORG_100406, ErrorConstant.ORG_MSG_100406));
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    /**
     * 根据机构ID取机构信息
     */
    @Override
    public Reply selectByOrgId(Integer orgId) {
        try {
            MWOrg mworg = mworgDao.selectByOrgId(orgId);
            return Reply.ok(mworg);
        } catch (Exception e) {
            log.error("fail to selectByOrgId with orgId=【{}】, cause:【{}】",
                    orgId, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ORG_100404, ErrorConstant.ORG_MSG_100404));
        }
    }

    /**
     * 查询当前登录用户的机构/部门和其子机构
     */
    @Override
    public Reply selectDorpdownList(QueryOrgForDropDown qParam) {
        try {
            String loginName = loginCacheInfo.getLoginName();
            String roleId = mwUserOrgMapperDao.getRoleIdByLoginName(loginName);
            UserDTO userDTO = mwUserDao.selectByLoginName(loginName);
            boolean flag =UserType.LDAP.getType().equals(userDTO.getUserType());
            if (!(roleId.equals(MWUtils.ROLE_TOP_ID) || flag)) {
                List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
                qParam.setNodeList(nodes);
            }
            Map criteria = PropertyUtils.describe(qParam);
            List<MWOrgDTO> mworges = new ArrayList<>();
            //权限控制，放开父级结构
            mworges = mworgDao.selectListByXZYCSys(criteria);
            for (MWOrgDTO mwOrgDTO : mworges) {
                if (mwOrgDTO.getEnable().equals("ACTIVE")) {
                    mwOrgDTO.setDisabledFlag(false);
                } else {
                    mwOrgDTO.setDisabledFlag(true);
                }
            }
            List<MWOrgDTO> orgTopList = new ArrayList<>();
            List<MWOrgDTO> childList = new ArrayList<>();
            Integer minDeep = mwUserOrgMapperDao.getMinDeepByLoginName(loginName);
            if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                mworges.forEach(
                        mwOrgDTO -> {
                            if (mwOrgDTO.getDeep() == 1) {
                                orgTopList.add(mwOrgDTO);
                            } else {
                                childList.add(mwOrgDTO);
                            }
                        }
                );
            } else {
                mworges.forEach(
                        mwOrgDTO -> {
                            if (mwOrgDTO.getDeep() == minDeep) {
                                orgTopList.add(mwOrgDTO);
                            } else {
                                childList.add(mwOrgDTO);
                            }
                        }
                );
            }
            List<MWOrgDTO> copyChildList = new ArrayList<>(childList);
            findTopOrg(orgTopList, childList);
            Set<Integer> orgIdSet = new HashSet<>(copyChildList.size());
            orgTopList.forEach(
                    orgTop -> getChild(orgTop, copyChildList, orgIdSet)
            );
            List<MWOrgDTO> mwOrgDTOS = null;
            if(orgTopList != null && orgTopList.size() > 0){
                Comparator<Object> com = Collator.getInstance(Locale.CHINA);
                Pinyin4jUtil pinyin4jUtil = new Pinyin4jUtil();
                mwOrgDTOS = orgTopList.stream().sorted((o1, o2) -> ((Collator) com).compare(pinyin4jUtil.getStringPinYin(o1.getOrgName()), pinyin4jUtil.getStringPinYin(o2.getOrgName()))).collect(Collectors.toList());
            }
            if(mwOrgDTOS != null && mwOrgDTOS.size() > 0){
                return Reply.ok(mwOrgDTOS);
            }else{
                return Reply.ok(orgTopList);
            }
        } catch (Exception e) {
            log.error("fail to selectDropdownList with QueryOrgForDropDown=【{}】, cause:【{}】",
                    qParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ORG_100406, ErrorConstant.ORG_MSG_100406));
        }
    }

    private List removeAllList(List<MWOrgDTO> listA, List<MWOrgDTO> listB) {
        List<MWOrgDTO> list = new ArrayList<>();
        HashSet hA = new HashSet(listA);
        HashSet hB = new HashSet(listB);
        hA.removeAll(hB);
        list.addAll(hA);
        return list;
    }

    private void findTopOrg(List<MWOrgDTO> orgTopList, List<MWOrgDTO> childList) {
        List<MWOrgDTO> list = new ArrayList<>();
        for (MWOrgDTO m : orgTopList) {
            for (MWOrgDTO n : childList) {
                if (n.getNodes().contains(m.getNodes())) {
                    list.add(n);
                }
            }
        }
        childList = removeAllList(childList, list);
        if (childList.size() > 0) {
            Set<Integer> set = new HashSet<>();
            for (MWOrgDTO m : childList) {
                set.add(m.getDeep());
            }
            List<MWOrgDTO> tt = new ArrayList<>();
            for (MWOrgDTO m : childList) {
                if (m.getDeep() == Collections.min(set))
                    tt.add(m);

            }
            childList = removeAllList(childList, tt);
            orgTopList.addAll(tt);
            findTopOrg(orgTopList, childList);
        }
    }

    /**
     * 根据权限查询机构信息
     */
    @Override
    public Reply selectListFilterByPerm(QueryOrgParam qParam) {
        qParam.setPageSize(Integer.MAX_VALUE);
        String loginName = loginCacheInfo.getLoginName();
//        String loginName = "admin";
        MwRoleDTO roleDTO = loginCacheInfo.getRoleInfo();
        LoginContext loginContext = loginCacheInfo.getCacheInfo(loginName);
        LoginInfo loginInfo = loginContext.getLoginInfo();
        List<MWOrgDTO> orgs = loginInfo.getOrgs();
        List<OrgView> filterOrgs = new ArrayList<OrgView>();
        Map<Integer, MWOrgDTO> mapOrgs = new HashMap<Integer, MWOrgDTO>();
        try {
            DataPermission dataPermission = DataPermission.valueOf(roleDTO.getDataPerm());
            switch (dataPermission) {
                case PRIVATE:
                    orgs.forEach(
                            value -> {
                                if (value.getIsNode()) {
                                    OrgView orgView = new OrgView();
                                    orgView.setValue(value.getOrgId());
                                    orgView.setLabel(value.getOrgName());
                                    filterOrgs.add(orgView);
                                }
                            }
                    );
                    break;
                case PUBLIC:
                    for (MWOrgDTO item : orgs) {
                        MWOrgDTO porg = mapOrgs.get(item.getPid());
                        mapOrgs.put(item.getOrgId(), item);
                        if (null != porg) {
                            porg.addChild(item);
                        }
                    }
                    for (MWOrgDTO item : orgs) {
                        if (item.getDeep() == 1) {
                            OrgView orgView = new OrgView();
                            orgView.setValue(item.getOrgId());
                            orgView.setLabel(item.getOrgName());
                            if (null != item.getChilds() && item.getChilds().size() > 0) {
                                orgView.addChildren(item);
                            }
                            filterOrgs.add(orgView);
                        }
                    }
                    break;
                default:
            }
            return Reply.ok(filterOrgs);
        } catch (Exception e) {
            String[] param = new String[]{loginName, roleDTO.getDataPerm()};
            String message = Reply.replaceMsg(ErrorConstant.DATAPERMISSIONCODE_MSG_290001 + e.getMessage(), param);
            log.error(message);
            return Reply.fail(ErrorConstant.DATAPERMISSIONCODE_290001, message);
        }
    }


//    @Override
//    public Reply selectListByUserId(Integer userId) {
//        try {
//            List<MWOrgDTO> list = null;
//            List<MWOrg> mworges = mworgDao.selectByUserId(userId);
//            list = resovleOrgs(mworges);
//            return Reply.ok(list);
//        } catch (Exception e) {
//            log.error("fail to selectListByUserId with userId={}, cause:{}", userId, e.getMessage());
//            return Reply.fail(ErrorConstant.ORG_100406, ErrorConstant.ORG_MSG_100406);
//        }
//    }

    @Override
    public List<MWOrgDTO> selectByLoginName(String loginName) {
        List<MWOrgDTO> rootlist = null;
        try {
            List<MWOrg> mworges = mworgDao.selectListByLoginName(loginName);
            rootlist = resovleOrgs(mworges);
        } catch (Exception e) {
            log.error("fail to selectByLoginName , cause:{}", e.getMessage());
        }
        return rootlist;
    }

    @Override
    public Reply getOrgList(QueryOrgParam qParam) {
        try {
            Reply reply = null;
            String loginName = loginCacheInfo.getLoginName();
            String roleId = loginCacheInfo.getRoleId(loginName);
            List<Integer> orgIds = mwUserOrgMapperDao.getOrgIds(loginName);
            qParam.setOrgIds(orgIds);
            if (MWUtils.ROLE_TOP_ID.equals(roleId)) {
                reply = selectList(qParam);
            } else {
                reply = selectCernetList(qParam);
            }
            return Reply.ok(reply);
        } catch (Exception e) {
            return Reply.fail(ErrorConstant.ORG_100405, ErrorConstant.ORG_MSG_100405);
        }
    }

    @Override
    public Reply selectCernetList(QueryOrgParam qParam) {
        try {
            PageHelper.startPage(qParam.getPageNumber(),qParam.getPageSize());
            List<MWOrg> mwOrgs = new ArrayList<>();
            qParam.getOrgIds().forEach(
                    id ->{
                        List<MWOrg> orgs = mworgDao.selectOrgById(id);
                        mwOrgs.addAll(orgs);
                    }
            );
            PageInfo<?> pageInfo = new PageInfo<>(mwOrgs);
            return Reply.ok(pageInfo);
        }catch (Exception e) {
            log.error("fail to selectListOrg with QueryOrgParam=【{}】, cause:【{}】",
                    qParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ORG_100406, ErrorConstant.ORG_MSG_100406));
        }
    }

    /**
     * 批量查询组织信息
     *
     * @param qParam 查询组织参数
     * @return
     */
    @Override
    public Reply batchQueryOrg(QueryOrgParam qParam) {
        try {
            List<MWOrg> orgList = mworgDao.selectOrgByOrgId(qParam.getOrgIds());
            return Reply.ok(orgList);
        } catch (Exception e) {
            log.error("fail to selectListOrg with QueryOrgParam=【{}】, cause:【{}】",
                    qParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ORG_100406, ErrorConstant.ORG_MSG_100406));
        }
    }

    /**
     * 根据组织ID获取组织数据
     *
     * @param orgId 组织ID
     * @return 组织数据
     */
    @Override
    public MWOrgDTO getByOrgId(int orgId) {
//        List<MWOrgDTO> list = mworgDao.
        return null;
    }

    /**
     * 根据资产ID获取组织信息
     *
     * @param assetsId 资产id
     * @return 组织数据
     */
    @Override
    public List<MWOrgDTO> selectByAssetsId(String assetsId) {
        return null;
    }

    /**
     * 获取所有的组织列表数据
     *
     * @return 所有组织列表数据
     */
    @Override
    public List<MWOrgDTO> getAllOrgList() {
        try {
            Map criteria = new HashMap();
            List<MWOrgDTO> mworges = mworgDao.selectList(criteria);
            List<MWOrgDTO> orgTopList = new ArrayList<>();
            List<MWOrgDTO> childList = new ArrayList<>();
            mworges.forEach(
                    mwOrgDTO -> {
                        // 深度为1时，是最高层
                        if (mwOrgDTO.getDeep() == 1) {
                            orgTopList.add(mwOrgDTO);
                        } else {
                            childList.add(mwOrgDTO);
                        }
                    }
            );
            Set<Integer> orgIdSet = new HashSet<>(childList.size());
            orgTopList.forEach(
                    orgTop -> getChild(orgTop, childList, orgIdSet)
            );
            return orgTopList;
        } catch (Exception e) {
            log.error("fail to selectListOrg with QueryOrgParam=【{}】, cause:【{}】",
                    null, e.getMessage());
            return null;
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

//    @Override
//    public Reply selectByLoginName() {
//        List<MWOrgDTO> rootlist = null;
//        try {
//            List<MWOrg> mworges = mworgDao.selectListByLoginName(loginCacheInfo.getLoginName());
//            rootlist = resovleOrgs(mworges);
//        } catch (Exception e) {
//            log.error("fail to selectByLoginName , cause:{}", e.getMessage());
//        }
//        return Reply.ok(rootlist);
//    }

    private List<MWOrgDTO> resovleOrgs(List<MWOrg> mworges) throws Exception {
        List<MWOrgDTO> mworgDTOs = null;

        mworgDTOs = CopyUtils.copyList(MWOrgDTO.class, mworges);
        Map<Integer, MWOrgDTO> mworgMap = new HashMap<Integer, MWOrgDTO>();
        List<MWOrgDTO> rootlist = new ArrayList<MWOrgDTO>();

        for (MWOrgDTO mworgdto : mworgDTOs) {
            mworgMap.put(mworgdto.getOrgId(), mworgdto);
            if (ROOT_ORG == mworgdto.getPid()) {
                rootlist.add(mworgdto);
                continue;
            }
            MWOrgDTO pmwOrgDTO = mworgMap.get(mworgdto.getPid());
            pmwOrgDTO.addChild(mworgdto);
        }

        return rootlist;
    }

    /**
     * 查询机构的所有公有权限的用户
     * @param orgIds
     * @return
     */
    @Override
    public List<Integer> selectPubUserIdByOrgId(List<Integer> orgIds) {
        List<Integer> userIds = mworgDao.selectPubUserIdByOrgId(orgIds);
        return userIds;

    }

    /**
     *根据用户登录名称查询机构id
     */
    @Override
    public List<Integer> getOrgIdsByNodes(String loginName) {
        List<Integer> orgIds;
        List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
        String sb = getStringBuffer(nodes);
        orgIds = mwUserOrgMapperDao.getOrgIdByUserId(sb);
        return orgIds;
    }

    /**
     *根据用户登录名称查询机构名称
     */
    @Override
    public List<String> getOrgNamesByNodes(String loginName) {
        List<String> orgNames;
        List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
        String sb = getStringBuffer(nodes);
        orgNames = mwUserOrgMapperDao.getOrgNameByLoginName(sb);
        return orgNames;
    }

    /**
     * 根据用户登录名称查询机构
     *
     * @param loginName 登录名称
     * @return 所有组织机构ID列表
     */
    @Override
    public List<Integer> getAllOrgIdsByName(String loginName) {
        List<Integer> resultOrgList = new ArrayList<>();
        List<Integer> orgIds;
        List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
        String sb = getStringBuffer(nodes);
        orgIds = mwUserOrgMapperDao.getOrgIdByUserId(sb);
        for (int orgId : orgIds) {
            updateOrgList(resultOrgList, orgId);
        }
        return resultOrgList.stream().distinct().collect(Collectors.toList());
    }

    /**
     * @param typeId   类别ID
     * @param dataType 类别
     * @return 机构列表
     */
    @Override
    public List<OrgDTO> getAllOrgList(int typeId, DataType dataType) {
        return mworgDao.getAllOrgList(typeId, dataType.getName());
    }

    @Override
    public List<String> getOrgnamesByids(List<Integer> organizes) {
        return mworgDao.getOrgnamesByids(organizes);
    }

    /**
     * 更新当前机构ID下所有子机构列表
     *
     * @param resultOrgList 机构ID列表
     * @param orgId         当前机构ID
     */
    private void updateOrgList(List<Integer> resultOrgList, int orgId) {
        HashMap map = new HashMap();
        map.put("pid", orgId);
        resultOrgList.add(orgId);
        List<MWOrgDTO> list = mworgDao.selectList(map);
        if (CollectionUtils.isNotEmpty(list)) {
            for (MWOrgDTO orgDTO : list) {
                resultOrgList.add(orgDTO.getOrgId());
                updateOrgList(resultOrgList, orgDTO.getOrgId());
            }
        }
    }


    private String getStringBuffer(List<String> nodes) {
        StringBuffer sb = new StringBuffer();
        boolean isOracle = DATABASE_ORACLE.equalsIgnoreCase(check);
        for (int i = 0; i < nodes.size(); i++) {
            if (isOracle) {
                sb.append(" \"nodes\" like '%" + nodes.get(i) + "%'");
            } else {
                sb.append(" nodes like '%" + nodes.get(i) + "%'");
            }
            if (i < nodes.size() - 1) {
                sb.append(" or ");
            }
        }
        return sb.toString();
    }

    /**
     * 查询机构地址的经纬度数据
     * @param qParam 查询对应地址的经纬度
     * @return
     */
    @Override
     public Reply getOrgLongitudeDropDown(QueryOrgParam qParam) {
        String orgName = qParam.getOrgName();
        List<MwOrgLongitudeDto> provinceLongitudeDtos = mworgDao.selectOrgLongitudeDropDown(orgName,1);//省信息
        List<MwOrgLongitudeDto> cityLongitudeDtos = mworgDao.selectOrgLongitudeDropDown(orgName,2);//市信息
        List<MwOrgLongitudeDto> areaLongitudeDtos = mworgDao.selectOrgLongitudeDropDown(orgName,3);//区信息
        //将省市区组成树状结构数据
        if(CollectionUtils.isNotEmpty(provinceLongitudeDtos)){
            for (MwOrgLongitudeDto provinceLongitudeDto : provinceLongitudeDtos) {
                int id = provinceLongitudeDto.getId();
                provinceLongitudeDto.setCoordinate(provinceLongitudeDto.getLat()+","+provinceLongitudeDto.getLon());
                provinceLongitudeDto.setLeaf(false);
                List<MwOrgLongitudeDto> provinceChildrens = new ArrayList<>();
                for (MwOrgLongitudeDto cityLongitudeDto : cityLongitudeDtos) {
                    int cityId = cityLongitudeDto.getId();
                    int parentId = cityLongitudeDto.getParentId();
                    cityLongitudeDto.setCoordinate(cityLongitudeDto.getLat()+","+cityLongitudeDto.getLon());
                    provinceLongitudeDto.setLeaf(false);
                    List<MwOrgLongitudeDto> cityChildrens = new ArrayList<>();
                    for (MwOrgLongitudeDto areaLongitudeDto : areaLongitudeDtos) {
                        int areaParentId = areaLongitudeDto.getParentId();
                        areaLongitudeDto.setCoordinate(areaLongitudeDto.getLat()+","+areaLongitudeDto.getLon());
                        areaLongitudeDto.setLeaf(true);
                        if(cityId == areaParentId){
                            cityChildrens.add(areaLongitudeDto);
                        }
                    }
                    cityLongitudeDto.setChildren(cityChildrens);
                    if(parentId == id){
                        provinceChildrens.add(cityLongitudeDto);
                    }
                }
                provinceLongitudeDto.setChildren(provinceChildrens);
            }
        }
        return Reply.ok(provinceLongitudeDtos);
    }

    @Override
    public Reply selectOrgByParamsAndIds(Map map) {
        List<OrgMapperDTO> ret = mworgDao.selectOrgByParamsAndIds(map);
        return Reply.ok(ret);
    }

    @Override
    public Reply selectOrgMapByParamsAndIds(Map map) {
        List<OrgMapperDTO> ret = mworgDao.selectOrgByParamsAndIds(map);
        Map<String ,List<OrgMapperDTO>> retMap = null;
        if(null != ret){
            retMap = ret.parallelStream().collect(Collectors.groupingBy(OrgMapperDTO::getTypeId));
        }

        return Reply.ok(retMap);
    }

    @Override
    public Reply getUserListByOrgIds(List<Integer> orgIds) {
        List<UserOrgDTO> userListByGroupIds = mwUserOrgMapperDao.getUserListByOrgIds(orgIds);
        Map<Integer, List<UserOrgDTO>> collect = userListByGroupIds.stream().collect(Collectors.groupingBy(UserOrgDTO::getOrgId));
        return Reply.ok(collect);
    }


    /**
     * 处理查询的机构经纬度数据
     * @param mworges
     */
    private void hangdleOrgLonAndLat(List<MWOrgDTO> mworges){
        if(CollectionUtils.isNotEmpty(mworges)){
            for (MWOrgDTO mworge : mworges) {
                String coordinate = mworge.getCoordinate();
                if(StringUtils.isNotBlank(coordinate)){
                    String[] split = coordinate.split(",");
                    mworge.setCoordinate(split[1]+","+split[2]);
                }
            }
        }
    }
}
