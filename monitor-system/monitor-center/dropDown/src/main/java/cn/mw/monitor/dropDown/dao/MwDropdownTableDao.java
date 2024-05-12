package cn.mw.monitor.dropDown.dao;

import cn.mw.monitor.dropDown.api.param.AddDropDownParam;
import cn.mw.monitor.dropDown.dto.MwDropdownDTO;
import cn.mw.monitor.dropDown.dto.SelectCharDropDto;
import cn.mw.monitor.dropDown.dto.SelectNumDropDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwDropdownTableDao {

    /**
     * 新增下拉框信息
     */
    int addDropDown(@Param("list") List<AddDropDownParam> list);
    /**
     * 根据下拉框code删除下拉框
     */
    int deleteDropDownByCode(@Param("dropCodes")List<String> dropCodes);
    /**
     * 根据下拉框Code查询下拉框信息
     */
    List<MwDropdownDTO> selectByCode(@Param("dropCode")String dropCode);

    List<SelectNumDropDto> pageSelectNumUrl(String type);

    List<SelectCharDropDto> pageSelectCharUrl(String type);

    List<Object> selectDropDown(@Param("fieldName") String fieldName, @Param("tableName") String tableName);
}
