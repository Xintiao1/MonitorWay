package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MwModelMacroAuthDao;
import cn.mw.monitor.model.dao.MwModelViewDao;
import cn.mw.monitor.model.dto.ModelMacroSelectDataDTO;
import cn.mw.monitor.model.dto.MwModelMacrosManageDTO;
import cn.mw.monitor.model.param.DeleteModelMacrosParam;
import cn.mw.monitor.model.param.MwModelAuthNameManageParam;
import cn.mw.monitor.model.param.MwModelMacrosManageParam;
import cn.mw.monitor.model.service.MwModelMacroAuthService;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Strings;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author qzg
 * @date 2023/6/12
 */
@Service
@Slf4j
public class MwModelMacroAuthServiceImpl implements MwModelMacroAuthService {

    @Resource
    private MwModelMacroAuthDao mwModelMacroAuthDao;
    @Resource
    private MwModelViewDao mwModelViewDao;

    /**
     * 查询资产凭证信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply getMacroAuthList(MwModelMacrosManageParam param) {
        List<MwModelMacrosManageDTO> list = new ArrayList<>();
        try {
            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
            list = mwModelMacroAuthDao.getMacroAuthList(param);
            if (list != null) {
                for (MwModelMacrosManageDTO macrosManageDTO : list) {
                    if(macrosManageDTO!=null && !Strings.isNullOrEmpty(macrosManageDTO.getGroupNodes())){
                        List<String> groupNodes = Arrays.asList(macrosManageDTO.getGroupNodes().substring(1).split(","));
                        List<Integer> lists = groupNodes.stream().map(Integer::parseInt).collect(Collectors.toList());
                        macrosManageDTO.setModelGroupIdList(lists);
                    }
                }
            }
            PageInfo pageInfo = new PageInfo(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Throwable e) {
            log.error("查询资产凭证信息失败", e);
            return Reply.fail(500, "查询资产凭证信息失败");
        }

    }

    /**
     * 查询宏值字段下拉数据
     *
     * @return
     */
    @Override
    public Reply getAllMacroField() {
        List<ModelMacroSelectDataDTO> list = new ArrayList<>();
        try {
            list = mwModelMacroAuthDao.getAllMacroField();
        } catch (Throwable e) {
            log.error("查询宏值字段下拉数据失败", e);
            return Reply.fail(500, "查询宏值字段下拉数据失败");
        }
        return Reply.ok(list);
    }

    /**
     * 查询模型字段数据
     *
     * @return
     */
    @Override
    public Reply queryMacroFieldByModelId(MwModelMacrosManageParam param) {
        List<MwModelMacrosManageParam> list = new ArrayList<>();
        try {
            list = mwModelMacroAuthDao.queryMacroFieldByModelId(param.getModelId());
        } catch (Throwable e) {
            log.error("查询模型字段数据失败", e);
            return Reply.fail(500, "查询模型字段数据失败");
        }
        return Reply.ok(list);
    }

    /**
     * 新增凭证信息
     *
     * @param paramList
     * @return
     */
    @Override
    public Reply addMacroAuthInfo(List<MwModelMacrosManageParam> paramList) {
        try {
            if (CollectionUtils.isNotEmpty(paramList) && paramList.get(0).getModelId() != null) {
                Integer modelId = paramList.get(0).getModelId();
                List<MwModelMacrosManageParam> list = mwModelMacroAuthDao.queryMacroFieldByModelId(modelId);
                if (CollectionUtils.isNotEmpty(list)) {
                    List<MwModelMacrosManageParam> disAddList = paramList.stream().filter(item -> list.stream().noneMatch(s -> s.getMacroId().equals(item.getMacroId())
                            && s.getModelId().equals(item.getModelId()))).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(disAddList)) {
                        mwModelMacroAuthDao.insertModelMacroInfoMapper(disAddList);
                    }
                    List<MwModelMacrosManageParam> disDelList = list.stream().filter(item -> paramList.stream().noneMatch(s -> s.getMacroId().equals(item.getMacroId())
                            && s.getModelId().equals(item.getModelId()))).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(disDelList)) {
                        mwModelMacroAuthDao.deleteModelMacroInfoMapper(disDelList);
                    }
                } else {
                    mwModelMacroAuthDao.insertModelMacroInfoMapper(paramList);
                }
                int num = mwModelMacroAuthDao.checkAuthNameInfoNum(paramList.get(0));
                if (num > 0) {
                    return Reply.warn("相同模型下凭证名称不可重复");
                }
                mwModelMacroAuthDao.addMacroValueAuthName(paramList);
            }
        } catch (Throwable e) {
            log.error("新增资产凭证信息失败", e);
            return Reply.fail(500, "新增资产凭证信息失败");
        }
        return Reply.ok();
    }

    /**
     * 修改凭证信息
     *
     * @param paramList
     * @return
     */
    @Override
    public Reply updateMacroAuthNameInfo(List<MwModelMacrosManageParam> paramList) {
        try {
            if (CollectionUtils.isNotEmpty(paramList) && paramList.get(0).getModelId() != null) {

                Integer modelId = paramList.get(0).getModelId();
                List<MwModelMacrosManageParam> list = mwModelMacroAuthDao.queryMacroFieldByModelId(modelId);
                if (CollectionUtils.isNotEmpty(list)) {
                    List<MwModelMacrosManageParam> disAddList = paramList.stream().filter(item -> list.stream().noneMatch(s -> s.getMacroId().equals(item.getMacroId())
                            && s.getModelId().equals(item.getModelId()))).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(disAddList)) {
                        //新增字段
                        mwModelMacroAuthDao.insertModelMacroInfoMapper(disAddList);
                        //新增凭证数据
                        mwModelMacroAuthDao.addMacroValueAuthName(disAddList);
                    }
                    List<MwModelMacrosManageParam> disDelList = list.stream().filter(item -> paramList.stream().noneMatch(s -> s.getMacroId().equals(item.getMacroId())
                            && s.getModelId().equals(item.getModelId()))).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(disDelList)) {
                        //删除字段
                        mwModelMacroAuthDao.deleteModelMacroInfoMapper(disDelList);
                        //删除凭证数据
                        mwModelMacroAuthDao.deleteModelMacroNameInfo(disDelList);
                    }
                    //去除新增的数据，保留需要修改的数据
                    paramList.removeAll(disAddList);
                }
                if(CollectionUtils.isNotEmpty(paramList)){
                    mwModelMacroAuthDao.editorMacroValueAuthNameInfo(paramList);
                }
            }
        } catch (Throwable e) {
            log.error("修改凭证信息失败", e);
            return Reply.fail(500, "修改凭证信息失败");
        }
        return Reply.ok();
    }

    /**
     * 删除凭证信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply deleteMarcoInfoByModel(List<DeleteModelMacrosParam> param) {
        try {
            mwModelMacroAuthDao.deleteModelMacroNameInfoByName(param);
            return Reply.ok("删除成功");
        } catch (Throwable e) {
            log.error("删除凭证信息失败", e);
            return Reply.fail(500, "删除凭证信息失败");
        }
    }

    /**
     * 编辑回显数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply selectInfoPopup(MwModelMacrosManageParam param) {
        try {
            MwModelAuthNameManageParam mwModelAuthNameParam = new MwModelAuthNameManageParam();
            List<MwModelMacrosManageDTO> list = mwModelMacroAuthDao.selectInfoPopup(param);
            List<Integer> modelGroupIdList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(list)) {
                String groupNodeStr = list.get(0).getGroupNodes();
                String authName = list.get(0).getAuthName();
                if (!Strings.isNullOrEmpty(groupNodeStr)) {
                    List<String> groupNodes = Arrays.asList(groupNodeStr.substring(1).split(","));
                    List<Integer> lists = groupNodes.stream().map(Integer::parseInt).collect(Collectors.toList());
                    modelGroupIdList.addAll(lists);
                }
                mwModelAuthNameParam.setAuthName(authName);
                mwModelAuthNameParam.setModelGroupIdList(modelGroupIdList);
                mwModelAuthNameParam.setMacrosParam(list);
            }
            return Reply.ok(mwModelAuthNameParam);
        } catch (Throwable e) {
            log.error("编辑回显数据失败", e);
            return Reply.fail(500, "编辑回显数据失败");
        }
    }


}
