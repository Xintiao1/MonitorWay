package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.api.exception.AssetsException;
import cn.mw.monitor.assets.api.param.assets.AddUpdateIntangAssetsParam;
import cn.mw.monitor.assets.api.param.assets.QueryIntangAssetsParam;
import cn.mw.monitor.assets.dao.MwIntangibleassetsTableDao;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.assets.dto.MwIntangibleassetsByIdDTO;
import cn.mw.monitor.assets.dto.MwIntangibleassetsDTO;
import cn.mw.monitor.assets.service.MwIntangibleAssetsService;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.param.UpdateAssetsStateParam;
import cn.mw.monitor.service.common.ListenerService;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by baochengbin on 2020/3/12.
 */
@Service
@Slf4j
public class MwIntangibleAssetsServiceImpl extends ListenerService implements MwIntangibleAssetsService {

    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/assets");

    @Resource
    private MwIntangibleassetsTableDao mwIntangibleAssetsDao;

    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    /**
     * 根据资产ID取机构信息
     *
     * @param assetsId 自增序列ID
     * @return
     */
    @Override
    public Reply selectById(String assetsId) {
        try {
            MwIntangibleassetsDTO mwTangAssets = mwIntangibleAssetsDao.selectById(assetsId);
            MwIntangibleassetsByIdDTO mtaDTto = CopyUtils.copy(MwIntangibleassetsByIdDTO.class, mwTangAssets);

            // usergroup重新赋值使页面可以显示
            List<Integer> groupIds = new ArrayList<>();
            mwTangAssets.getGroup().forEach(
                    groupDTO -> groupIds.add(groupDTO.getGroupId())
            );
            mtaDTto.setGroupIds(groupIds);
            // department重新赋值使页面可以显示
            List<List<Integer>> orgNodes = new ArrayList<>();
            if (mwTangAssets.getDepartment() != null) {
                mwTangAssets.getDepartment().forEach(department -> {
                            List<Integer> orgIds = new ArrayList<>();
                            List<String> nodes = Arrays.stream(department.getNodes().split(",")).collect(Collectors.toList());
                            nodes.forEach(node -> {
                                if (!"".equals(node))
                                    orgIds.add(Integer.valueOf(node));
                            });
                            orgNodes.add(orgIds);
                        }
                );
                mtaDTto.setOrgIds(orgNodes);
            }
            // user重新赋值
            List<Integer> userIds = new ArrayList<>();
            mwTangAssets.getPrincipal().forEach(
                    userDTO -> userIds.add(userDTO.getUserId())
            );
            mtaDTto.setPrincipal(userIds);

            //查询标签重新赋值给页面可以显示
            List<MwAssetsLabelDTO> labelBoard = mwLabelCommonServcie.getLabelBoard(assetsId, DataType.INASSETS.getName());
            mtaDTto.setAssetsLabel(labelBoard);
            return Reply.ok(mtaDTto);
        } catch (Exception e) {
            log.error("fail to selectById with assetsId={}, cause:{}", assetsId, e);
            return Reply.fail(ErrorConstant.TANGASSETSCODE_210106, ErrorConstant.TANGASSETS_MSG_210106);
        }
    }

    /**
     * 查询资产信息list
     *
     * @param mtaDTO
     * @return
     */
    @Override
    public Reply selectList(QueryIntangAssetsParam mtaDTO) {
        try {
            PageHelper.startPage(mtaDTO.getPageNumber(), mtaDTO.getPageSize());
            List mwTangAssetses = new ArrayList();
            List<String> ids = mtaDTO.getAssetsIds();
            if (ids != null && ids.size() == 0) {
                PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
                pageInfo.setList(mwTangAssetses);

                logger.info("ASSETS_LOG[]assets[]无形资产管理[]查询无形资产信息[]{}[]", mtaDTO);

                return Reply.ok(pageInfo);
            }

            if (null != mtaDTO.getLogicalQueryLabelParamList() && mtaDTO.getLogicalQueryLabelParamList().size() > 0) {
                List<String> assetsIds = mwLabelCommonServcie.getTypeIdsByLabel(mtaDTO.getLogicalQueryLabelParamList());
                if (null != assetsIds && assetsIds.size() > 0) {
//                    mtaDTO.setAssetsIds(assetsIds);

                    if (ids != null && ids.size() > 0) {
                        assetsIds.retainAll(ids);
                    }
                    if (null != assetsIds && assetsIds.size() > 0) {
                        mtaDTO.setAssetsIds(assetsIds);
                    } else {
                        PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
                        pageInfo.setList(mwTangAssetses);

                        logger.info("ASSETS_LOG[]assets[]无形资产管理[]查询无形资产信息[]{}[]", mtaDTO);

                        return Reply.ok(pageInfo);
                    }

                } else {
                    PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
                    pageInfo.setList(mwTangAssetses);

                    logger.info("ASSETS_LOG[]assets[]无形资产管理[]查询无形资产信息[]{}[]", mtaDTO);

                    return Reply.ok(pageInfo);
                }
            }
//            if (mtaDTO.getIsSelectLabel()) {
//                QueryLabelParam labelParam = new QueryLabelParam();
//                labelParam.setAssetsTypeId(mtaDTO.getAssetsTypeId());
//                // 获取本次查询的标签所有的标签值
//                List<MwAllLabelDTO> allLabel = mwTangibleAssetsDao.selectAllLabel(labelParam);
//                mtaDTO.setAllLabelList(allLabel);
//                mwTangAssetses = mwIntangibleAssetsDao.selectLabelList(mtaDTO);
//
//                PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
//                pageInfo.setList(mwTangAssetses);
//            } else {
            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
            DataPermission dataPermission = DataPermission.valueOf(perm);
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
            if (null != groupIds && groupIds.size() > 0) {
                mtaDTO.setGroupIds(groupIds);
            }
            switch (dataPermission) {
                case PRIVATE:
                    mtaDTO.setUserId(userId);
                    PageHelper.startPage(mtaDTO.getPageNumber(), mtaDTO.getPageSize());
                    Map priCriteria = PropertyUtils.describe(mtaDTO);
                    mwTangAssetses = mwIntangibleAssetsDao.selectPriList(priCriteria);
                    break;
                case PUBLIC:
                    String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                    List<Integer> orgIds = new ArrayList<>();
                    Boolean isAdmin = false;
                    if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                        isAdmin = true;
                    }
                    if (!isAdmin) {
                        //List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
                        //orgIds = mwUserOrgMapperDao.getOrgIdByUserId(loginName);
                        orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);

                    }
                    if (null != orgIds && orgIds.size() > 0) {
                        mtaDTO.setOrgIds(orgIds);
                    }
                    mtaDTO.setIsAdmin(isAdmin);
                    PageHelper.startPage(mtaDTO.getPageNumber(), mtaDTO.getPageSize());
                    Map pubCriteria = PropertyUtils.describe(mtaDTO);
                    mwTangAssetses = mwIntangibleAssetsDao.selectPubList(pubCriteria);
                    break;
            }
//            }
            PageInfo pageInfo = new PageInfo<>(mwTangAssetses);
            pageInfo.setList(mwTangAssetses);

            logger.info("ACCESS_LOG[]org[]无形资产管理[]查询无形资产信息[]{}[]", mtaDTO);

            return Reply.ok(pageInfo);

        } catch (Exception e) {
            log.error("fail to selectList with mtaDTO={}, cause:{}", mtaDTO, e);
            return Reply.fail(ErrorConstant.TANGASSETSCODE_210108, ErrorConstant.TANGASSETS_MSG_210108);
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    /**
     * 更新资产信息
     *
     * @param uParam
     * @return
     */
    @Override
    public Reply update(AddUpdateIntangAssetsParam uParam) {
        List<String> items = new ArrayList<>();
        uParam.setModifier(iLoginCacheInfo.getLoginName());
        //重复性校验
        List<MwIntangibleassetsDTO> ckeckDTO1 = mwIntangibleAssetsDao.checkAdd(QueryIntangAssetsParam.builder().assetsNumber(uParam.getAssetsNumber()).build());
        if (ckeckDTO1.size() == 1) {
            if (!ckeckDTO1.get(0).getId().equals(uParam.getId())) {
                items.add("资产编号重复");
            }
        }
        List<MwIntangibleassetsDTO> ckeckDTO2 = mwIntangibleAssetsDao.checkAdd(QueryIntangAssetsParam.builder().assetsName(uParam.getAssetsName()).build());
        if (ckeckDTO2.size() == 1) {
            if (!ckeckDTO2.get(0).getId().equals(uParam.getId())) {
                items.add("资产名称重复");
            }
        }
        List<MwIntangibleassetsDTO> ckeckDTO3 = mwIntangibleAssetsDao.checkAdd(QueryIntangAssetsParam.builder().assetsContent(uParam.getAssetsContent()).build());
        if (ckeckDTO3.size() == 1) {
            if (!ckeckDTO3.get(0).getId().equals(uParam.getId())) {
                items.add("资产内容重复");
            }
        }
        if (items.size() > 0) {
            throw new AssetsException(StringUtils.join(new String[]{ErrorConstant.TANGASSETS_MSG_210103, StringUtils.join(items, "、 ")}));
        }

        mwIntangibleAssetsDao.update(uParam);
        //删除负责人
        deleteMapperAndPerm(uParam.getId());
        //添加负责人
        addMapperAndPerm(uParam);
        //删除标签参数
        mwLabelCommonServcie.deleteLabelBoard(uParam.getId(), DataType.INASSETS.getName());
        //插入标签参数
        if (null != uParam.getAssetsLabel() && uParam.getAssetsLabel().size() > 0) {
            mwLabelCommonServcie.insertLabelboardMapper(uParam.getAssetsLabel(), uParam.getId(), DataType.INASSETS.getName());
        }
        return Reply.ok("更新成功");

    }


    /**
     * 新增资产信息
     *
     * @param aParam
     * @return
     */
    @Override
    public Reply insert(AddUpdateIntangAssetsParam aParam) {
        List<String> items = new ArrayList<>();
        aParam.setCreator(iLoginCacheInfo.getLoginName());
        aParam.setModifier(iLoginCacheInfo.getLoginName());
        aParam.setId(UUIDUtils.getUUID());
        //重复性校验
        List<MwIntangibleassetsDTO> ckeckDTO1 = mwIntangibleAssetsDao.checkAdd(QueryIntangAssetsParam.builder().assetsNumber(aParam.getAssetsNumber()).build());
        if (ckeckDTO1.size() == 1) {
            items.add("资产编号重复");
        }
        List<MwIntangibleassetsDTO> ckeckDTO2 = mwIntangibleAssetsDao.checkAdd(QueryIntangAssetsParam.builder().assetsName(aParam.getAssetsName()).build());
        if (ckeckDTO2.size() == 1) {
            items.add("资产名称重复");
        }
        List<MwIntangibleassetsDTO> ckeckDTO3 = mwIntangibleAssetsDao.checkAdd(QueryIntangAssetsParam.builder().assetsContent(aParam.getAssetsContent()).build());
        if (ckeckDTO3.size() == 1) {
            items.add("资产内容重复");
        }

        if (items.size() > 0) {
            throw new AssetsException(StringUtils.join(new String[]{ErrorConstant.TANGASSETS_MSG_210104, StringUtils.join(items, "、 ")}));
        }
        mwIntangibleAssetsDao.insert(aParam);
        //添加负责人
        addMapperAndPerm(aParam);

        //插入标签参数
        if (null != aParam.getAssetsLabel() && aParam.getAssetsLabel().size() > 0) {
            mwLabelCommonServcie.insertLabelboardMapper(aParam.getAssetsLabel(), aParam.getId(), DataType.INASSETS.getName());
        }
        return Reply.ok("新增成功");

    }


    /**
     * 删除资产信息
     *
     * @param ids
     * @return
     */
    @Override
    public Reply delete(List<String> ids) {
        try {
            mwIntangibleAssetsDao.delete(ids);

            ids.forEach(
                    id -> {
                        mwTangibleAssetsDao.deleteAssetsLabelByAssetsId(id);
                        //删除标签
                        mwLabelCommonServcie.deleteLabelBoard(id, DataType.INASSETS.getName());

                        /*mwTangibleAssetsDao.deleteAssetsGroupMapperByAssetsId(id);
                        mwTangibleAssetsDao.deleteAssetsUserMapperByAssetsId(id);
                        mwTangibleAssetsDao.deleteAssetsOrgMapperByAssetsId(id);*/
                        //删除负责人
                        deleteMapperAndPerm(id);
                    }
            );
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("fail to deleteAssets with ids={}, cause:{}", ids, e.getMessage());
            return Reply.fail(ErrorConstant.TANGASSETSCODE_210111, ErrorConstant.TANGASSETS_MSG_210111);
        }
    }


    @Override
    public Reply updateState(UpdateAssetsStateParam updateAssetsStateParam) {
        try {
            mwIntangibleAssetsDao.updateAssetsState(updateAssetsStateParam);
        } catch (Exception e) {
            log.error("fail to updateState with ids={}, cause:{}", updateAssetsStateParam.getIdList().toString(), e.getMessage());
            return Reply.fail(ErrorConstant.TANGASSETSCODE_210109, ErrorConstant.TANGASSETS_MSG_210109);
        }
        return Reply.ok("更新成功");
    }

//    @Override
//    public Reply getDropdown() {
//        try {
//            List<AssetsStatesParam> lists = mwIntangibleAssetsDao.getDropdown();
//            return Reply.ok(lists);
//        } catch (Exception e) {
//            return Reply.fail("下拉框查询", 500);
//        }
//    }

    /**
     * 删除负责人
     *
     * @param id
     */
    private void deleteMapperAndPerm(String id) {
        DeleteDto deleteDto = DeleteDto.builder().typeId(id).type(DataType.INASSETS.getName()).build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    /**
     * 添加负责人
     *
     * @param uParam
     */
    private void addMapperAndPerm(AddUpdateIntangAssetsParam uParam) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(uParam.getGroupIds())
                .userIds(uParam.getPrincipal())
                .orgIds(uParam.getOrgIds())
                .typeId(uParam.getId())
                .type(DataType.INASSETS.getName())
                .desc(DataType.INASSETS.getDesc()).build();
        //添加负责人
        mwCommonService.addMapperAndPerm(insertDto);
    }

}
