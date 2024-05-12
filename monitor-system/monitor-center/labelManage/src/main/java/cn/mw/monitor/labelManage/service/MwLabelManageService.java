package cn.mw.monitor.labelManage.service;

import cn.mw.monitor.dropDown.model.MwDropdownTable;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.labelManage.api.param.*;
import cn.mw.monitor.service.label.model.QueryLabelParam;
import cn.mw.monitor.service.label.model.MWCommonLabel;

import java.util.List;

public interface MwLabelManageService {

    /**
     * 新增标签信息
     */
    Reply insert(AddUpdateLabelManageParam auParam);

    /**
     * 标签删除信息
     */
    Reply delete(List<DeleteLabelManageParam> param);

    /**
     * 更新标签状态信息
     */
    Reply updateState(UpdateLabelStateParam stateParam);

    /**
     * 更新标签信息
     */
    Reply update(AddUpdateLabelManageParam ausDTO);

    /**
     * 分页查询标签列表信息
     */
    Reply selectList(QueryLabelManageParam qsDTO);

    /**
     * 根据标签ID查询标签信息
     */
    Reply selectById(Integer id);

    /**
     * 标签关联资产类型查询
     */
    Reply selectAsstsType(QueryLabelManageParam qsDTO);

    Reply getLabelListByAssetsTypeId(Integer assetsTypeId);

    Reply selectModuleType(QueryLabelManageParam param);

    Reply getDropLabelList(QueryLabelParam queryLabelParam);

    Reply getDropLabelListByAssetsTypeList(QueryLabelParam queryLabelParam);

    /**
     * 根据标签查询模块id
     *
     * @param commonLabel
     * @return
     */
    List<String> getIdListByLabel(MWCommonLabel commonLabel);

    List<String> getAssetsIdByLabel(List<Integer> labelId,String modelType);

    /**
     * 获取标签联想模糊数据查询
     * @return
     */
    Reply getLabelAssociateFuzzyQuery();

    List<MwDropdownTable> selectOldLabel();

    void updateById(List<Integer> delete, Integer updateId);

    void deleteById(List<Integer> delete);

    void updateDeleteById(Integer updateId);

    /**
     * 查询下拉类型的标签值，分页处理
     * @param param
     * @return
     */
    Reply selectDropDownLabelValue(QueryLabelManageParam param);
}
