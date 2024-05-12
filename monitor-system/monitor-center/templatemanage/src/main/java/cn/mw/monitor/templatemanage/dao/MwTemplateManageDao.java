package cn.mw.monitor.templatemanage.dao;


import cn.mw.monitor.templatemanage.entity.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwTemplateManageDao {

    //根据主键查询
    MwQueryTemplateManageTable selectOne(QueryTemplateManageParam qParam);

    //查询品牌
    List<String> selectBrand(@Param("specification")String specification);

    //查询规格型号
    List<String> selectSpecification(@Param("brand")String brand);

    List<MwTemplateManageTable> selectList(Map pubCriteria);

    //批量删除
    int deleteBatch(@Param("idList") List<Integer> list);

    //修改模板管理
    int update(AddTemplateManageParam record);

    //新增模板管理
    int insert(AddTemplateManageParam record);

    List<DropDownParam> getDropdown();

    /**
     * 判断模板名称是否重复
     * @param templateName 模板名称
     * @return
     */
    int countTemplateName(@Param(value = "templateName") String templateName);

    /**
     * 通过模板名称获取信息
     *
     * @param templateName 模板名称
     * @return
     */
    MwQueryTemplateManageTable getInfoByTemplateName(@Param(value = "templateName") String templateName);
}
