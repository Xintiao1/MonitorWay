package cn.mw.monitor.service.label.api;

import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.SimplifyLabelDTO;
import cn.mw.monitor.service.label.param.LogicalQueryLabelParam;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/11/16 11:02
 */
public interface MwLabelCommonServcie {

    /**
     * @param list
     * @param typeId     模块id
     * @param moduleType 模块类型
     */
    void insertLabelboardMapper(List<MwAssetsLabelDTO> list, String typeId, String moduleType);

    /**
     * @param typeId
     * @param moduleType
     * @return 根据模块id和模块类型查询三张表的标签值
     */
    List<MwAssetsLabelDTO> getLabelBoard(String typeId, String moduleType);

    /**
     * @param typeId
     * @param moduleType
     * @return 根据模块id和模块类型删除三张表的标签值
     */
    void deleteLabelBoard(String typeId, String moduleType);

    /**
     * @param qParam
     * @return 多逻辑标签条件查询
     * 返回模块的主键ids
     */
    List<String> getTypeIdsByLabel(List<List<LogicalQueryLabelParam>> qParam);


    void deleteLabelBoards(List<String> linkIds, String moduleType);


    /**
     *
     * @param typeId
     * @param moduleType
     * @return 根据模块id和和模块类型查询标签id
     */
    List<String> getLabelIdsByAssetsId(String typeId, String moduleType);

    /**
     *
     * @param typeId
     * @param moduleType
     * @return 根据模块id和和模块类型查询标签id和标签值
     */
    List<Map<String, String>> getLabelsByAssetsId(String typeId, String moduleType);

    /**
     * @param typeIds 模块数据ID集合
     * @param moduleType 模块类型
     * @return 根据模块id集合和模块类型查询三张表的标签值
     */
    Map<String,List<MwAssetsLabelDTO>> getLabelBoards(List<String> typeIds, String moduleType);

    /**
     * 获取简化的标签数据（只能获取资产相关的）（针对大商所项目）
     *
     * @return
     */
    List<SimplifyLabelDTO> getSimplifyLabel();

    /**
     * 根据标签信息获取对应的资产
     *
     * @param labelList 标签列表
     * @return
     */
    List<String> getAssetsIdByLabel(List<MwAssetsLabelDTO> labelList);
}
