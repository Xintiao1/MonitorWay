package cn.mw.monitor.labelManage.service.impl;

import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.dropDown.api.param.AddDropDownParam;
import cn.mw.monitor.dropDown.dao.MwDropdownTableDao;
import cn.mw.monitor.dropDown.dto.MwDropdownDTO;
import cn.mw.monitor.dropDown.model.MwDropdownTable;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.labelManage.api.exception.CheckDeleteLabelManageException;
import cn.mw.monitor.labelManage.api.param.AddUpdateLabelManageParam;
import cn.mw.monitor.labelManage.api.param.DeleteLabelManageParam;
import cn.mw.monitor.labelManage.api.param.QueryLabelManageParam;
import cn.mw.monitor.labelManage.api.param.UpdateLabelStateParam;
import cn.mw.monitor.labelManage.dao.MwAssetslabelTableDao;
import cn.mw.monitor.labelManage.dao.MwLabelManageTableDao;
import cn.mw.monitor.labelManage.dto.*;
import cn.mw.monitor.labelManage.model.MwLabelAssetsTypeMapper;
import cn.mw.monitor.labelManage.model.MwLabelModuleMapper;
import cn.mw.monitor.labelManage.service.MwLabelManageService;
import cn.mw.monitor.service.alert.dto.MWAlertAssetsParam;
import cn.mw.monitor.service.assets.model.MwAllLabelDTO;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.SimplifyLabelDTO;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.label.model.MWCommonLabel;
import cn.mw.monitor.service.label.model.QueryLabelParam;
import cn.mw.monitor.service.label.param.LogicalQueryLabelParam;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MwLabelManageServiceImpl implements MwLabelManageService, MwLabelCommonServcie, InitializingBean {

    @Resource
    private MwLabelManageTableDao mwLabelmanageTableDao;

    @Resource
    private MwDropdownTableDao mwDropdownTableDao;

    @Resource
    private MwAssetslabelTableDao mwAssetslabelTableDao;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    //标签文本表
    private static final String MW_LABEL_MAPPER = "mw_label_mapper";
    //标签日期表
    private static final String MW_LABEL_DATE_MAPPER = "mw_label_date_mapper";
    //标签下拉表
    private static final String MW_LABEL_DROP_MAPPER = "mw_label_drop_mapper";

    private static final String TEXT = "1";
    private static final String DATE = "2";
    private static final String DROP_DOWN = "3";

    private static final String FUZZYQUERY = "fuzzyQuery";

    private static final String TYPEID = "typeId";

    private static final String MODULETYPE = "moduleType";

    private static final String DROPID = "dropId";
    private static final String DROPKEY = "dropKey";
    private static final String DROPVALUE = "dropValue";
    private static final String LABELNAME = "labelName";




    private void setLabelParam(AddUpdateLabelManageParam auParam) {
        auParam.setEarlyWarning(false);
        auParam.setReport(false);
        auParam.setScreen(false);
        if (null != auParam.getModeList()) {
            if (auParam.getModeList().size() > 0) {
                if (auParam.getModeList().size() == 3) {
                    auParam.setEarlyWarning(true);
                    auParam.setReport(true);
                    auParam.setScreen(true);
                } else {
                    auParam.getModeList().forEach(
                            mode -> {
                                if (mode == 1) {
                                    auParam.setEarlyWarning(true);
                                } else if (mode == 2) {
                                    auParam.setReport(true);
                                } else {
                                    auParam.setScreen(true);
                                }
                            }
                    );
                }
            }
        }
    }

    private void bindLabelAssetsType(AddUpdateLabelManageParam auParam) {
        if (null != auParam.getAssetsTypeIdList() && auParam.getAssetsTypeIdList().size() > 0) {
            List<MwLabelAssetsTypeMapper> mappers = new ArrayList<>();
            auParam.getAssetsTypeIdList().forEach(assetsTypeId ->
                    mappers.add(MwLabelAssetsTypeMapper
                            .builder()
                            .assetsTypeId(assetsTypeId)
                            .labelId(auParam.getLabelId())
                            .build())
            );
            mwLabelmanageTableDao.createAssetsTypeLabelMapper(mappers);
        }
    }

    private void bindlabelModule(AddUpdateLabelManageParam auParam) {
        if (null != auParam.getModeList() && auParam.getModeList().size() > 0) {
            List<MwLabelModuleMapper> mappers = new ArrayList<>();
            auParam.getModeList().forEach(moduleId ->
                    mappers.add(MwLabelModuleMapper
                            .builder()
                            .moduleId(moduleId)
                            .labelId(auParam.getLabelId())
                            .build())
            );
            mwLabelmanageTableDao.createModuleLabelMapper(mappers);
        }
    }

    private void insertDropDown(AddUpdateLabelManageParam auParam, String dropdownValue) {
        if (null != auParam.getDropdownTable() && auParam.getDropdownTable().size() > 0) {
            auParam.getDropdownTable().forEach(dropDowntable ->
                    dropDowntable.setDropCode(dropdownValue)
            );
            mwDropdownTableDao.addDropDown(auParam.getDropdownTable());
        }
    }

    /**
     * 新增标签信息
     */
    @Override
    public Reply insert(AddUpdateLabelManageParam auParam) {
        try {
            // 从缓存中获取当前登录者的登录名，作为创建人
            String creator = iLoginCacheInfo.getLoginName();
            auParam.setCreator(creator);
            auParam.setModifier(creator);
            // step1:插入标签参数
            // 创建随机下拉框名称和labelcode
            String dropdownValue = UuidUtil.getUid();
            auParam.setLabelCode(dropdownValue);
            mwLabelmanageTableDao.insert(auParam);
            // step2:绑定资产类型和标签的关系
            bindLabelAssetsType(auParam);
            //step3 绑定模块标签的关系
            bindlabelModule(auParam);
            //  step4:插入下拉框数据
            insertDropDown(auParam, dropdownValue);
            return Reply.ok("新增成功");
        } catch (Exception e) {
            log.error("fail to insertLabelManage with AddUpdateLabelManageParam={}, cause:{}", auParam, e);
            throw new ServiceException(Reply.fail(ErrorConstant.LABELMANAGECODE_220104, ErrorConstant.LABELMANAGE_MSG_220104));
        }
    }

    /**
     * 删除标签信息
     */
    @Override
    public Reply delete(List<DeleteLabelManageParam> param) {
        try {
            List<Integer> labelIdList = new ArrayList<Integer>();
            List<String> strList = new ArrayList<String>();
            StringBuffer msg = new StringBuffer();
            StringBuffer mapperMsg = new StringBuffer();
            param.forEach(
                    label -> {
                        boolean deleteRestrict = label.getDeleteRestrict();
                        if (deleteRestrict) {
                            msg.append("【" + label.getLabelName() + "】");
                        }

                        Integer labelId = label.getLabelId();
                        int count = mwLabelmanageTableDao.getCountByLabelId(labelId, MW_LABEL_MAPPER);
                        int dateCount = mwLabelmanageTableDao.getCountByLabelId(labelId, MW_LABEL_DATE_MAPPER);
                        int dropDate = mwLabelmanageTableDao.getCountByLabelId(labelId, MW_LABEL_DROP_MAPPER);
                        if (count > 0 || dateCount > 0 || dropDate > 0) {
                            mapperMsg.append("【" + label.getLabelName() + "】");
                        }

                        Integer inputFormat = label.getInputFormat();
                        if (inputFormat == 3) {
                            strList.add(label.getDropdownValue());
                        }
                        labelIdList.add(labelId);
                    }

            );
            if (msg.length() > 0) {
                throw new CheckDeleteLabelManageException(ErrorConstant.LABELMANAGE_MSG_220111, msg.toString());
            }
            if (mapperMsg.length() > 0) {
                throw new CheckDeleteLabelManageException(ErrorConstant.LABELMANAGE_MSG_220107, mapperMsg.toString());
            }
            mwLabelmanageTableDao.delete(labelIdList, iLoginCacheInfo.getLoginName());
            mwLabelmanageTableDao.deleteAssetsTypeLableMapper(labelIdList);
            mwLabelmanageTableDao.deleteModelLabelapper(labelIdList);
            if (strList.size() > 0) {
                mwDropdownTableDao.deleteDropDownByCode(strList);
            }
            return Reply.ok("删除成功");
        } catch (Exception e) {
            if (e instanceof CheckDeleteLabelManageException) {
                log.error("fail to deleteLabelManage with param={}, cause:{}", param, e.getMessage());
                throw e;
            } else {
                log.error("fail to deleteLabelManage with param={}, cause:{}", param, e);
                throw new ServiceException(Reply.fail(ErrorConstant.LABELMANAGECODE_220105, ErrorConstant.LABELMANAGE_MSG_220105));
            }
        }
    }

    /**
     * 更新标签状态信息
     */
    @Override
    public Reply updateState(UpdateLabelStateParam stateParam) {
        try {
            stateParam.setModifier(iLoginCacheInfo.getLoginName());
            mwLabelmanageTableDao.updateState(stateParam);
            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("fail to updateLabelManageState with UpdateLabelStateParam={}, cause:{}", stateParam, e.getMessage());
            throw new ServiceException(Reply.fail(ErrorConstant.LABELMANAGECODE_220108, ErrorConstant.LABELMANAGE_MSG_220108));
        }
    }

    /**
     * 更新标签信息
     */
    @Override
    public Reply update(AddUpdateLabelManageParam auParam) {
        try {
            //因为现在下拉类型的数据是分页展示，导致修改之恶能修改当前页数据，需要进行处理
            handleLabelDropDownValuePage(auParam);
            // step1:重新插入标签参数
            setLabelParam(auParam);
            //处理标签类型改为下拉的逻辑
            Boolean falg = labelTypeEdit(auParam);
            if(!falg){
                List<Integer> ids = new ArrayList<>();
                ids.add(auParam.getLabelId());
                mwLabelmanageTableDao.deleteAssetsTypeLableMapper(ids);
                mwLabelmanageTableDao.deleteModelLabelapper(ids);
                bindLabelAssetsType(auParam);
                bindlabelModule(auParam);
                mwLabelmanageTableDao.update(auParam);
                return Reply.ok("更新成功");
            }else{
                // step1:重新插入标签参数
                setLabelParam(auParam);
                // step2:重新绑定资产类型和标签的关系,以及模型和标签的关系
                List<Integer> ids = new ArrayList<>();
                ids.add(auParam.getLabelId());
                mwLabelmanageTableDao.deleteAssetsTypeLableMapper(ids);
                mwLabelmanageTableDao.deleteModelLabelapper(ids);
                bindLabelAssetsType(auParam);
                bindlabelModule(auParam);
                //  step3:重新插入下拉框数据
                auParam.setDropdownValue(auParam.getLabelCode());
                List<String> strList = new ArrayList<String>();
                strList.add(auParam.getDropdownValue());
                mwDropdownTableDao.deleteDropDownByCode(strList);
                insertDropDown(auParam, auParam.getDropdownValue());
//            if (auParam.getInputFormat() != 3) {
//                auParam.setDropdownValue(null);
//            }
                auParam.setModifier(iLoginCacheInfo.getLoginName());
                mwLabelmanageTableDao.deleteLabelTextType(auParam.getLabelId(),MW_LABEL_DROP_MAPPER);
                mwLabelmanageTableDao.update(auParam);
                return Reply.ok("更新成功");
            }
        } catch (Exception e) {
            log.error("fail to updateLabelManage with AddUpdateLabelManageParam={}, cause:{}", auParam, e.getMessage());
            throw new ServiceException(Reply.fail(ErrorConstant.LABELMANAGECODE_220103, ErrorConstant.LABELMANAGE_MSG_220103));
        }
    }

    /**
     * 处理分页修改标签数据
     * @param auParam
     */
    private void handleLabelDropDownValuePage(AddUpdateLabelManageParam auParam){
        if(auParam.getInputFormat() == null || auParam.getInputFormat() != 3)return;
        String labelCode = auParam.getLabelCode();
        List<MwDropdownDTO> dropdownDTOS = mwDropdownTableDao.selectByCode(labelCode);
        List<AddDropDownParam> dropDownParams = new ArrayList<>();
        List<AddDropDownParam> operateDropDowns = auParam.getOperateDropDowns();
        if(CollectionUtils.isEmpty(operateDropDowns)){
            if(CollectionUtils.isNotEmpty(dropdownDTOS)){
                for (MwDropdownDTO dropdownDTO : dropdownDTOS) {
                    AddDropDownParam param = new AddDropDownParam();
                    param.setDropKey(dropdownDTO.getDropKey());
                    param.setDropId(dropdownDTO.getDropId());
                    param.setDropValue(dropdownDTO.getDropValue());
                    dropDownParams.add(param);
                }
            }
        }else{
            for (AddDropDownParam operateDropDown : operateDropDowns){
                if(operateDropDown.getDropId() == null){
                    operateDropDown.setOperateType(1);
                }
                Integer operateType = operateDropDown.getOperateType();
                if(operateType != null && operateType == 1){
                    dropDownParams.add(operateDropDown);
                }
            }
            for (MwDropdownDTO dropdownDTO : dropdownDTOS) {
                Integer dtoDropId = dropdownDTO.getDropId();
                boolean flag = true;
                for (AddDropDownParam operateDropDown : operateDropDowns) {
                    Integer operateType = operateDropDown.getOperateType();
                    Integer dropId = operateDropDown.getDropId();
                    if(operateType != null && operateType == 1){
                       continue;
                    }
                    if(dropId != null && dtoDropId != null && dropId.equals(dtoDropId)){
                        if(operateType != null && operateType == 2){
                            dropDownParams.add(operateDropDown);
                        }
                        flag = false;
                    }
                }
                if(flag){
                    AddDropDownParam param = new AddDropDownParam();
                    param.setDropKey(dropdownDTO.getDropKey());
                    param.setDropId(dropdownDTO.getDropId());
                    param.setDropValue(dropdownDTO.getDropValue());
                    dropDownParams.add(param);
                }
            }
        }
        auParam.setDropdownTable(dropDownParams);
    }


    /**
     * 分页查询标签列表信息
     */
    @Override
    public Reply selectList(QueryLabelManageParam qsParam) {
        try {
            PageHelper.startPage(qsParam.getPageNumber(), qsParam.getPageSize());
            Map criteria = PropertyUtils.describe(qsParam);
            List<MwLabelManageDTO> mwLabelList = mwLabelmanageTableDao.selectList(criteria);
            PageInfo pageInfo = new PageInfo<>(mwLabelList);
            pageInfo.setList(mwLabelList);
            log.info("LabelManage_LOG[]LabelManage[]扫描标签管理[]分页查询扫描标签信息[]{}[]", mwLabelList);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectListLabelManage with QueryLabelManageParam={}, cause:{}", qsParam, e);
            throw new ServiceException(Reply.fail(ErrorConstant.LABELMANAGECODE_220101, ErrorConstant.LABELMANAGE_MSG_220101));
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    /**
     * 根据标签ID查询标签信息
     */
    @Override
    public Reply selectById(Integer labelId) {
        try {
            MwLabelManageDTO msDto = mwLabelmanageTableDao.selectById(labelId);
            MwLabelManageByIdDTO manageByIdDTO = CopyUtils.copy(MwLabelManageByIdDTO.class, msDto);
            List<Integer> assetsTypeList = new ArrayList<>();
            if (null != msDto.getAssetsType() && msDto.getAssetsType().size() > 0) {
                msDto.getAssetsType().forEach(
                        assetsType -> assetsTypeList.add(assetsType.getId())
                );
                manageByIdDTO.setAssetsTypeIdList(assetsTypeList);
            }

            List<Integer> modeList = new ArrayList<>();
            if (null != msDto.getModuleType() && msDto.getModuleType().size() > 0) {
                msDto.getModuleType().forEach(
                        moduleType -> modeList.add(moduleType.getId())
                );
                manageByIdDTO.setModeList(modeList);
            }
            //下拉框的键值，获取已经关联的下拉数据
            List<MwDropdownDTO> dropdownTable = msDto.getDropdownTable();
            List<Integer> dropIds = new ArrayList<>();
            if(!CollectionUtils.isEmpty(dropdownTable)){
                for (MwDropdownDTO mwDropdownDTO : dropdownTable) {
                    dropIds.add(mwDropdownDTO.getDropId());
                }
            }
            if(!CollectionUtils.isEmpty(dropIds)){
                //查询关联关系
                List<Integer> associatedDropIds = mwLabelmanageTableDao.selectAssociatedDropId(dropIds);
                manageByIdDTO.setAssociatedDropIds(associatedDropIds);
            }
//            List<Integer> modeList = new ArrayList<>();
//            if (msDto.getEarlyWarning()) {
//                modeList.add(1);
//            }
//            if (msDto.getReport()) {
//                modeList.add(2);
//            }
//            if (msDto.getScreen()){
//                modeList.add(3);
//            }
//            manageByIdDTO.setModeList(modeList);
            log.info("LabelManage_LOG[]LabelManage[]扫描标签管理[]根据自增序列ID取标签信息[]{}", labelId);
            return Reply.ok(manageByIdDTO);
        } catch (Exception e) {
            log.error("fail to selectLabelManageById with labelId={}, cause:{}", labelId, e.getMessage());
            throw new ServiceException(Reply.fail(ErrorConstant.LABELMANAGECODE_220100, ErrorConstant.LABELMANAGE_MSG_220100));
        }
    }

    /**
     * 标签关联资产类型查询
     */
    @Override
    public Reply selectAsstsType(QueryLabelManageParam qsParam) {
        try {
            List<MwAssetsTypeDTO> mwLabelList = mwLabelmanageTableDao.selectAssetsType(qsParam.getLabelId());
            log.info("LabelManage_LOG[]LabelManage[]标签资产类型管理[]资产类型信息[]{}[]", mwLabelList);
            return Reply.ok(mwLabelList);
        } catch (Exception e) {
            log.error("fail to selectAsstsType with QueryLabelManageParam={}, cause:{}", qsParam, e.getMessage());
            throw new ServiceException(Reply.fail(ErrorConstant.LABELMANAGECODE_220106, ErrorConstant.LABELMANAGE_MSG_220106));
        }
    }

    @Override
    public Reply getLabelListByAssetsTypeId(Integer assetsTypeId) {
        try {
            List<MwLabelAssetsTypeDTO> mwLabelList = mwLabelmanageTableDao.getLabelListByAssetsTypeId(assetsTypeId);
            log.info("LabelManage_LOG[]LabelManage[]标签资产类型管理[]资产类型信息[]{}[]", mwLabelList);
            return Reply.ok(mwLabelList);
        } catch (Exception e) {
            log.error("fail to getLabelListByAssetsTypeId with assetsTypeId={}, cause:{}", assetsTypeId, e.getMessage());
            throw new ServiceException(Reply.fail(ErrorConstant.LABELMANAGECODE_220102, ErrorConstant.LABELMANAGE_MSG_220102));
        }
    }

    @Override
    public Reply selectModuleType(QueryLabelManageParam qsParam) {
        try {
            List<MwModuleTypeDTO> mwLabelList = mwLabelmanageTableDao.selectModuleType(qsParam.getLabelId());
            log.info("LabelManage_LOG[]LabelManage[]标签模块类型管理[]模块类型信息[]{}[]", mwLabelList);
            return Reply.ok(mwLabelList);
        } catch (Exception e) {
            log.error("fail to selectAsstsType with QueryLabelManageParam={}, cause:{}", qsParam, e.getMessage());
            throw new ServiceException(Reply.fail(ErrorConstant.LABELMANAGECODE_220110, ErrorConstant.LABELMANAGE_MSG_220110));
        }
    }

    @Override
    public Reply getDropLabelList(QueryLabelParam queryLabelParam) {
        try {
            // 获取本次查询的标签所有的标签值
            List<MwAllLabelDTO> allLabel = mwLabelmanageTableDao.selectLabelBrowse(queryLabelParam);
            return Reply.ok(allLabel);
        } catch (Exception e) {
            log.error("fail to getDropLabelList with queryLabelParam={}, cause:{}", queryLabelParam, e);
            return Reply.fail(ErrorConstant.LABELMANAGECODE_220102, ErrorConstant.LABELMANAGE_MSG_220102);
        }
    }

    @Override
    public Reply getDropLabelListByAssetsTypeList(QueryLabelParam queryLabelParam) {
        try {
            // 获取本次查询的标签所有的标签值
            List<Integer> assetsTypeList = queryLabelParam.getAssetsTypeList();
            if (assetsTypeList.size() > 0) {
                assetsTypeList = MWUtils.removeDuplicate(assetsTypeList);
                queryLabelParam.setAssetsTypeList(assetsTypeList);
            }
            List<MwAllLabelDTO> allLabel = mwLabelmanageTableDao.selectLabelBrowseByList(queryLabelParam);
            return Reply.ok(allLabel);
        } catch (Exception e) {
            log.error("fail to getDropLabelListByAssetsTypeList with queryLabelParam={}, cause:{}", queryLabelParam, e);
            return Reply.fail(ErrorConstant.LABELMANAGECODE_220102, ErrorConstant.LABELMANAGE_MSG_220102);
        }
    }


    /**
     * @param list
     * @param typeId     模块id
     * @param moduleType 模块类型
     *                   不同类型的标签存入不同的mapper表中
     */
    @Override
    public void insertLabelboardMapper(List<MwAssetsLabelDTO> list, String typeId, String moduleType) {
        //不同类型的标签存入不同的mapper表中
        List<MwAssetsLabelDTO> tagboardList = new ArrayList<>();
        List<MwAssetsLabelDTO> dropTagboardList = new ArrayList<>();
        List<MwAssetsLabelDTO> dateTagboardList = new ArrayList<>();

        list.forEach(
                mwAssetsLabelDTO -> {
                    Integer labelId = mwAssetsLabelDTO.getLabelId();
                    if (null != labelId && labelId != 0) {
                        String inputFormat = mwAssetsLabelDTO.getInputFormat();
                        mwAssetsLabelDTO.setTypeId(typeId);//资产id对应的typeId
                        mwAssetsLabelDTO.setModuleType(moduleType);
                        switch (inputFormat) {
                            case TEXT://1.文本
                                tagboardList.add(mwAssetsLabelDTO);
                                break;
                            case DATE:// 2.日期
                                dateTagboardList.add(mwAssetsLabelDTO);
                                break;
                            case DROP_DOWN://3.下拉
                                mwAssetsLabelDTO.setDropValue(mwAssetsLabelDTO.getDropValue().trim());
                                dropTagboardList.add(mwAssetsLabelDTO);
                                break;
                            default://4.其他
                                break;
                        }
                    }
                });
        if (tagboardList.size() > 0) {
            mwLabelmanageTableDao.createLabelMapper(tagboardList);
        }
        if (dateTagboardList.size() > 0) {
            mwLabelmanageTableDao.createLabelDateMapper(dateTagboardList);
        }
        if (dropTagboardList.size() > 0) {
            incrementDropDownValue(dropTagboardList);
            mwLabelmanageTableDao.createLabelDropMapper(dropTagboardList);
        }
    }

    /**
     * @param typeId
     * @return 根据模块id和模块类型查询三张表的标签值
     * @eparam moduleType
     */
    @Override
    @Transactional
    public List<MwAssetsLabelDTO> getLabelBoard(String typeId, String moduleType) {

        List<MwAssetsLabelDTO> boardList = mwLabelmanageTableDao.selectLabelBoard(typeId, moduleType);
        List<MwAssetsLabelDTO> dateBoardList = mwLabelmanageTableDao.selectLabelDateBoard(typeId, moduleType);
        List<MwAssetsLabelDTO> dropBoardList = mwLabelmanageTableDao.selectLabelDropBoard(typeId, moduleType);
        List<MwAssetsLabelDTO> list = new ArrayList<>();
        if (null != boardList && boardList.size() > 0) {
            boardList.forEach(dto -> {
                list.add(dto);
            });
        }
        if (null != dateBoardList && dateBoardList.size() > 0) {
            dateBoardList.forEach(dto -> {
                list.add(dto);
            });
        }
        if (null != dropBoardList && dropBoardList.size() > 0) {
            dropBoardList.forEach(dto -> {
                list.add(dto);
            });
        }
        return list;

    }

    @Override
    @Transactional
    public void deleteLabelBoard(String typeId, String moduleType) {
        mwLabelmanageTableDao.deleteLabelBoard(typeId, moduleType);
        mwLabelmanageTableDao.deleteLabelDateBoard(typeId, moduleType);
        mwLabelmanageTableDao.deleteLabelDropBoard(typeId, moduleType);
    }

    @Override
    @Transactional
    public void deleteLabelBoards(List<String> typeIds, String moduleType) {
        mwLabelmanageTableDao.deleteLabelBoards(typeIds, moduleType);
        mwLabelmanageTableDao.deleteLabelDateBoards(typeIds, moduleType);
        mwLabelmanageTableDao.deleteLabelDropBoards(typeIds, moduleType);
    }


    @Override
    public List<String> getLabelIdsByAssetsId(String id, String name) {
        return mwLabelmanageTableDao.getLabelIdsByAssetsId(id, name);
    }

    @Override
    public List<Map<String, String>> getLabelsByAssetsId(String id, String name) {
        return mwLabelmanageTableDao.getLabelsByAssetsId(id, name);
    }

    @Override
    public List<MwDropdownTable> selectOldLabel() {
        return mwLabelmanageTableDao.selectOldLabel() ;
    }

    @Override
    public void updateById(List<Integer> delete, Integer updateId) {
         mwLabelmanageTableDao.updateById(delete,updateId) ;
    }

    @Override
    public void deleteById(List<Integer> delete) {
        Set<Integer> set = new HashSet<>(delete);
         mwLabelmanageTableDao.deleteDropDownData(set) ;
    }

    @Override
    public void updateDeleteById(Integer updateId) {
        mwLabelmanageTableDao.updateDeleteById(updateId) ;
    }


    @Override
    public List<String> getTypeIdsByLabel(List<List<LogicalQueryLabelParam>> qParam) {
        ArrayList<String> typeIds = new ArrayList<>();

        //根据不同的标签格式查询不同的表,先拆分
        for (List<LogicalQueryLabelParam> qlist : qParam) {
            List<LogicalQueryLabelParam> list = new ArrayList<>();
            for (LogicalQueryLabelParam param : qlist) {
                String inputFormat = param.getInputFormat();
                switch (inputFormat) {
                    case TEXT:
                        param.setTableName(MW_LABEL_MAPPER);
                        break;
                    case DATE:
                        param.setTableName(MW_LABEL_DATE_MAPPER);
                        param.setStartdateTagboard(param.getTime().get(0));
                        param.setEnddateTagboard(param.getTime().get(1));
                        break;
                    case DROP_DOWN:
                        param.setTableName(MW_LABEL_DROP_MAPPER);
                        break;
                    default:
                        break;
                }
                list.add(param);
            }
            if (list.size() > 0) {
                List<String> intersectList = mwLabelmanageTableDao.getTypeIdsByTagboard(list, "intersect");
                if (null != intersectList && intersectList.size() > 0) {
                    intersectList.forEach(dto -> {
                        typeIds.add(dto);
                    });
                }
            }

        }
        return typeIds;
    }


    /**
     * 根据标签查询模块id
     *
     * @param commonLabel
     * @return
     */
    @Override
    public List<String> getIdListByLabel(MWCommonLabel commonLabel) {
        List<String> idListByLabel = mwLabelmanageTableDao.getIdListByLabel(commonLabel);
        return idListByLabel;
    }

    @Override
    public List<String> getAssetsIdByLabel(List<Integer> filterLabelIds,String modelType){
        return  mwLabelmanageTableDao.getAssetsIdByLabel(filterLabelIds,modelType);
    }

    /**
     * 标签修改有其他类型到下拉类型的修改处理
     * @param auParam 修改标签的参数
     */
    private Boolean labelTypeEdit(AddUpdateLabelManageParam auParam){
        //标签ID
        Integer labelId = auParam.getLabelId();
        //查询typeid和module
        AddUpdateLabelManageParam lableData = mwLabelmanageTableDao.getLableData(labelId);
        //删除原来的下拉数据

        if(lableData == null){
            return true;
        }
        Integer inputFormat = auParam.getInputFormat();
        Integer lableDataInputFormat = lableData.getInputFormat();
            //说明改了类型
            //获取原来类型数据
            switch (lableDataInputFormat){
                case 1:
                    //查询文本框类型的类型和模块
                    List<Map<String, String>> textTypeIdAndModule = mwLabelmanageTableDao.getTextTypeIdAndModule(labelId);
                    if(textTypeIdAndModule == null){
                        return true;
                    }
                    if(inputFormat == 3){
                        return handleDropDownTypeData(labelId, auParam, textTypeIdAndModule, MW_LABEL_MAPPER);
                    }
                    return true;
                case 2:
                    //查询标签时间类型的类型和模块
                    List<Map<String, String>> dateTypeIdAndModule = mwLabelmanageTableDao.getDateTypeIdAndModule(labelId);
                    if(dateTypeIdAndModule == null){
                        return true;
                    }
                    if(inputFormat == 3){
                        return handleDropDownTypeData(labelId,auParam,dateTypeIdAndModule,MW_LABEL_DATE_MAPPER);
                    }
                    return true;
                case 3:
                    Boolean aBoolean = processDropDownTypeUpdateData(auParam);
                    return aBoolean;
                default: return true;
            }
    }

    /**
     *处理标签有其他类型改为下拉类型的数据
     * @param labelId  标签ID
     * @param auParam  修改标签的数据
     * @param typeIdAndModule  标签的类型所对应的模块和模块ID
     * @param tableName  需要操作的表名
     */
    private Boolean handleDropDownTypeData(Integer labelId,AddUpdateLabelManageParam auParam, List<Map<String, String>> typeIdAndModule,String tableName){
        //设置下拉框的code
        if (null != auParam.getDropdownTable() && auParam.getDropdownTable().size() > 0) {
            auParam.getDropdownTable().forEach(dropDowntable ->
                    dropDowntable.setDropCode(auParam.getLabelCode())
            );
            //下拉框键值的集合
            List<AddDropDownParam> dropdownTable = auParam.getDropdownTable();
            if(dropdownTable == null){
                return true;
            }
            //将下拉框键值集合转换为本方法使用的新的参数
            List<LabelEditConvertDto> dropdowndtos = new ArrayList<LabelEditConvertDto>();
            //设置新的参数集合
            for (AddDropDownParam addDropDownParam : dropdownTable) {
                LabelEditConvertDto dto = new LabelEditConvertDto();
                dto.setDropCode(addDropDownParam.getDropCode());
                dto.setDropKey(addDropDownParam.getDropKey());
                dto.setDropValue(addDropDownParam.getDropValue());
                dropdowndtos.add(dto);
            }
            //删除数据
            mwLabelmanageTableDao.deleteDropdownTables(auParam.getLabelCode());
            //插入下拉框键值的数据
            mwLabelmanageTableDao.insertDropdownTables(dropdowndtos);
            //设置参数，建立下拉框标签与其他模块数据联系
            List<LabelEditConvertDto> editConvertDto = new ArrayList<LabelEditConvertDto>();
            for (LabelEditConvertDto dropdowndto : dropdowndtos) {
                Integer id = dropdowndto.getId();
                for (Map<String, String> typeMap : typeIdAndModule) {
                    LabelEditConvertDto convertDto = new LabelEditConvertDto();
                    convertDto.setId(id);
                    convertDto.setLabelId(labelId);
                    String typeId = typeMap.get(TYPEID);
                    String moduleType = typeMap.get(MODULETYPE);
                    convertDto.setTypeId(typeId);
                    convertDto.setModuleType(moduleType);
                    editConvertDto.add(convertDto);
                }
            }
            if(editConvertDto == null || editConvertDto.size() == 0){
                return true;
            }
            //删除原来的类型所关联的数据
            mwLabelmanageTableDao.deleteLabelTextType(labelId,tableName);
            //添加新的数据
            mwLabelmanageTableDao.insetLabelDropMapper(editConvertDto);
        }
        return false;
    }

    /**
     *
     * @param auParam 修改的数据
     */
    private Boolean processDropDownTypeUpdateData(AddUpdateLabelManageParam auParam){
        //获取下拉数据
        List<AddDropDownParam> dropdownTable = auParam.getDropdownTable();
        Integer labelId = auParam.getLabelId();
        //编号
        String labelCode = auParam.getLabelCode();
        if(CollectionUtils.isEmpty(dropdownTable)){
            return true;
        }
        List<Integer> dropIds = new ArrayList<>();
        for (AddDropDownParam addDropDownParam : dropdownTable) {
            dropIds.add(addDropDownParam.getDropId());

        }
        List<AddDropDownParam> updateDropDown = new ArrayList<>();
        List<AddDropDownParam> insertDropDown = new ArrayList<>();
        Set<Integer> deleteDropDown = new HashSet<>();
        //根据编号查询数据库中的标签信息
        List<Map<String,Object>> oldDrop = mwLabelmanageTableDao.selectDropDownId(labelCode);
        for (AddDropDownParam addDropDownParam : dropdownTable) {
            Integer dropId = addDropDownParam.getDropId();
            if(dropId == null){
                addDropDownParam.setDropCode(labelCode);
                insertDropDown.add(addDropDownParam);
            }
            Integer dropKey = addDropDownParam.getDropKey();
            String dropValue = addDropDownParam.getDropValue();
            for (Map<String, Object> map : oldDrop) {
                //原来数据库的ID
                Object oldDropId = map.get(DROPID);
                //原来下拉框的键
                Object oldDropKey = map.get(DROPKEY);
                //原来下拉框的值
                Object oldDropValue = map.get(DROPVALUE);
                AddDropDownParam newParam = null;
                if(oldDropId != null && dropId != null && dropId == Integer.parseInt(oldDropId.toString())){
                    addDropDownParam.setDropCode(labelCode);
                    if(oldDropKey != null && oldDropValue != null && dropKey == Integer.parseInt(oldDropKey.toString()) && dropValue.equals(oldDropValue.toString())){
                        continue;
                    }
                    if(oldDropKey != null && oldDropValue != null && (dropKey != Integer.parseInt(oldDropKey.toString()) || !dropValue.equals(oldDropValue.toString()))){
                        updateDropDown.add(addDropDownParam);
                    }
                }
                if(!dropIds.contains(oldDropId)){
                    deleteDropDown.add(Integer.parseInt(oldDropId.toString()));
                }
            }
        }
        //进行下拉数据的增删改
        if(!CollectionUtils.isEmpty(updateDropDown)){
            mwLabelmanageTableDao.updateDropDownData(updateDropDown);
        }
        if(!CollectionUtils.isEmpty(insertDropDown)){
            mwLabelmanageTableDao.insertDropDownData(insertDropDown);
        }
        if(!CollectionUtils.isEmpty(deleteDropDown)){
            mwLabelmanageTableDao.deleteDropDownData(deleteDropDown);
        }
        return false;
    }

    /**
     * 手动在在关联页面增加新的下拉框值处理
     * @param dropTagboardList 关联下拉的数据
     */
    private void incrementDropDownValue(List<MwAssetsLabelDTO> dropTagboardList){
        if(CollectionUtils.isEmpty(dropTagboardList)){
            return;
        }
        List<MwAssetsLabelDTO> newLabels = new ArrayList<>();
        Map<String,Object> newLabelMap = new HashMap<>();
        for (MwAssetsLabelDTO mwAssetsLabelDTO : dropTagboardList) {
            Integer dropId = mwAssetsLabelDTO.getDropTagboard();
            Integer labelId = mwAssetsLabelDTO.getLabelId();
            String labelCode = mwLabelmanageTableDao.getLabelCode(labelId);
            List<Integer> count = mwLabelmanageTableDao.getDropLabelRepeatData(labelCode, null, mwAssetsLabelDTO.getDropValue());
            if (dropId == null || dropId == 0){
                if(count.size() > 0){
                    mwAssetsLabelDTO.setDropTagboard(count.get(0));
                    mwAssetsLabelDTO.setDropId(count.get(0));
                }
            }
            //如果dropId为空，说明是新增的下拉值
            if(dropId == null || dropId == 0){
                if(newLabelMap.get(labelId+mwAssetsLabelDTO.getDropValue()) != null){
                    continue;
                }else{
                    newLabelMap.put(labelId+mwAssetsLabelDTO.getDropValue(),mwAssetsLabelDTO.getDropValue());
                }
                List<LabelEditConvertDto> newDropDowns = new ArrayList<>();
                //需要新增关联下拉框的键值
                //查询labelCode
                LabelEditConvertDto dropDownParam = new LabelEditConvertDto();
                dropDownParam.setDropCode(labelCode);
                if(StringUtils.isBlank(mwAssetsLabelDTO.getDropValue())){
                    continue;
                }

                if(mwAssetsLabelDTO.getDropKey() == null){
                    Integer dropKey = mwLabelmanageTableDao.getDropDownKeyMaxValue(labelCode);
                    dropDownParam.setDropKey(dropKey+1);
                }else{
                    dropDownParam.setDropKey(mwAssetsLabelDTO.getDropKey());
                }
                dropDownParam.setDropValue(mwAssetsLabelDTO.getDropValue());
                newDropDowns.add(dropDownParam);
                mwLabelmanageTableDao.insertDropdownTables(newDropDowns);
                Integer id = newDropDowns.get(0).getId();
                mwAssetsLabelDTO.setDropTagboard(id);
                newLabels.add(mwAssetsLabelDTO);
            }
        }
        if(CollectionUtils.isEmpty(newLabels)){
            return;
        }
        //添加新的下拉框关联关系
        mwLabelmanageTableDao.createLabelDropMapper(newLabels);
        //删除已经添加的数据
        Iterator<MwAssetsLabelDTO> iterator = dropTagboardList.iterator();
        while(iterator.hasNext()){
            MwAssetsLabelDTO next = iterator.next();
            if(next.getDropTagboard() == null || next.getDropTagboard() == 0){
                iterator.remove();
            }
        }
    }

    /**
     * 标签联想模糊查询
     * @return
     */
    @Override
    public Reply getLabelAssociateFuzzyQuery() {
        Map<String,Object> fuzzyQueryMap = new HashMap<>();
        //所有字段查询
        List<Map<String, Object>> fuzzyQueryLabelAllFiled = mwLabelmanageTableDao.fuzzyQueryLabelAllFiledData();
        if(!CollectionUtils.isEmpty(fuzzyQueryLabelAllFiled)){
            Set<String> allFiledSet = new HashSet<>();
            for (Map<String, Object> map : fuzzyQueryLabelAllFiled) {
                for (Map.Entry<String, Object> stringObjectEntry : map.entrySet()) {
                    Object filed = stringObjectEntry.getValue();
                    if(filed != null){
                        allFiledSet.add(filed.toString());
                    }
                }
            }
            fuzzyQueryMap.put(FUZZYQUERY,allFiledSet);
        }
        //名称模糊查询
        List<String> labelNames = mwLabelmanageTableDao.fuzzyQueryLabelNames();
        fuzzyQueryMap.put(LABELNAME,labelNames);
        return Reply.ok(fuzzyQueryMap);
    }

    /**
     * 查询下拉类型标签值。分页处理
     * @param param
     * @return
     */
    @Override
    public Reply selectDropDownLabelValue(QueryLabelManageParam param) {
        try {
            List<MwDropdownTable> dropdownTable = new ArrayList<>();
            Integer labelId = param.getLabelId();
            Reply reply = selectById(labelId);
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
                MwLabelManageByIdDTO manageByIdDTO = (MwLabelManageByIdDTO) reply.getData();
               dropdownTable = manageByIdDTO.getDropdownTable();
            }
            if(CollectionUtils.isEmpty(dropdownTable))return Reply.ok(new PageInfo<>());
            Integer pageNumber = param.getPageNumber();
            Integer pageSize = param.getPageSize();
            int fromIndex = pageSize * (pageNumber -1);
            int toIndex = pageSize * pageNumber;
            if(toIndex > dropdownTable.size()){
                toIndex = dropdownTable.size();
            }
            List<MwDropdownTable> mwDropdownTables = dropdownTable.subList(fromIndex, toIndex);
            PageInfo pageInfo = new PageInfo<>(mwDropdownTables);
            pageInfo.setTotal(dropdownTable.size());
            pageInfo.setList(mwDropdownTables);
            return Reply.ok(pageInfo);
        }catch (Throwable e){
            log.error("分页查询下拉类型标签值失败,失败信息:",e);
            return Reply.fail("分页查询下拉类型标签值失败"+e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet(){
        try{
            if(MWAlertAssetsParam.mwAssetsLabelDTOMap.size() == 0){
                synchronized (MWAlertAssetsParam.mwAssetsLabelDTOMap){
                    if(MWAlertAssetsParam.mwAssetsLabelDTOMap.size() == 0){
                        List<MwAssetsLabelDTO> assetsLabelDTOS = getLabelBoard(null, DataType.ASSETS.getName());
                        Map<String,List<MwAssetsLabelDTO>> maps = assetsLabelDTOS.stream().collect(Collectors.groupingBy(MwAssetsLabelDTO::getTypeId));
                        for(Map.Entry<String, List<MwAssetsLabelDTO>> entry : maps.entrySet()){
                            MWAlertAssetsParam.mwAssetsLabelDTOMap.put(entry.getKey(),entry.getValue());
                        }
                    }
                }
            }
        }catch (Exception e){
            log.error("查询标签错误：" + e.getMessage());
        }

    }

    @Override
    public Map<String, List<MwAssetsLabelDTO>> getLabelBoards(List<String> typeIds, String moduleType) {
        List<MwAssetsLabelDTO> boardList = mwLabelmanageTableDao.selectLabelBoards(typeIds, moduleType);
        List<MwAssetsLabelDTO> dateBoardList = mwLabelmanageTableDao.selectLabelDateBoards(typeIds, moduleType);
        List<MwAssetsLabelDTO> dropBoardList = mwLabelmanageTableDao.selectLabelDropBoards(typeIds, moduleType);
        Map<String, List<MwAssetsLabelDTO>> lableMap = new HashMap<>();
        List<MwAssetsLabelDTO> list = new ArrayList<>();
        if (null != boardList && boardList.size() > 0) {
            boardList.forEach(dto -> {
                list.add(dto);
            });
        }
        if (null != dateBoardList && dateBoardList.size() > 0) {
            dateBoardList.forEach(dto -> {
                list.add(dto);
            });
        }
        if (null != dropBoardList && dropBoardList.size() > 0) {
            dropBoardList.forEach(dto -> {
                list.add(dto);
            });
        }
        if(CollectionUtils.isEmpty(list))return lableMap;
        //进行分组处理
        list.forEach(labelDTO -> {
            if(lableMap.containsKey(labelDTO.getTypeId())){
                List<MwAssetsLabelDTO> labelDTOS = lableMap.get(labelDTO.getTypeId());
                labelDTOS.add(labelDTO);
                lableMap.put(labelDTO.getTypeId(),labelDTOS);
            }else{
                List<MwAssetsLabelDTO> labelDTOS = new ArrayList<>();
                labelDTOS.add(labelDTO);
                lableMap.put(labelDTO.getTypeId(),labelDTOS);
            }
        });
        return lableMap;
    }

    @Override
    public List<SimplifyLabelDTO> getSimplifyLabel() {
        List<SimplifyLabelDTO> simplifyLabelList = new ArrayList<>();
        HashMap<Integer, Integer> labelIndexMap = new HashMap<>();
        SimplifyLabelDTO simplifyLabel;
        List<MwAssetsLabelDTO> assetsLabelList = mwLabelmanageTableDao.getAssetsSimplifyLabelList();
        if (CollectionUtils.isEmpty(assetsLabelList)) {
            return simplifyLabelList;
        }
        int labelId;
        for (MwAssetsLabelDTO label : assetsLabelList) {
            labelId = label.getLabelId();
            if (labelIndexMap.containsKey(labelId)) {
                simplifyLabel = simplifyLabelList.get(labelIndexMap.get(labelId));
                simplifyLabel.getTagBoardList().add(label.getLabelValue());
            } else {
                simplifyLabel = new SimplifyLabelDTO();
                simplifyLabel.setLabelId(labelId);
                simplifyLabel.setLabelName(label.getLabelName());
                simplifyLabel.setInputFormat(label.getInputFormat());
                Set<String> list = new HashSet<>();
                list.add(label.getLabelValue());
                simplifyLabel.setTagBoardList(list);
                simplifyLabelList.add(simplifyLabel);
                labelIndexMap.put(labelId, simplifyLabelList.size() - 1);
            }
        }
        return simplifyLabelList;
    }

    /**
     * 根据标签信息获取对应的资产
     *
     * @param labelList 标签列表
     * @return
     */
    @Override
    public List<String> getAssetsIdByLabel(List<MwAssetsLabelDTO> labelList) {
        List<String> assetsIdList = new ArrayList<>();
        try {
            if (CollectionUtils.isEmpty(labelList)) {
                return null;
            }
            assetsIdList = mwLabelmanageTableDao.getAssetsIdByLabelList(labelList);
        } catch (Exception e) {
            log.error("根据标签信息获取对应的资产失败", e);
        }
        return assetsIdList;
    }
}
