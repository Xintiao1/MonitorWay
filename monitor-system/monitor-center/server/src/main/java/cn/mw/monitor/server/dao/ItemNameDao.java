package cn.mw.monitor.server.dao;

import cn.mw.monitor.server.serverdto.ItemNameDto;
import cn.mw.monitor.service.server.api.dto.TypeFilterDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/5/21 8:59
 * @Version 1.0
 */
public interface ItemNameDao {
    List<ItemNameDto> getItemChNames(@Param("item_names") List<String> names);
    ItemNameDto getItemChName(@Param("item_name") String name);
    int updateItemChName(@Param("item_name") String item_name, @Param("descr") String descr);
    int insertItemChName(ItemNameDto record);

    List<Map<String,String>> getItemChNameAllInfo();
    TypeFilterDTO getTypeFilter(@Param("assetsId") String assetsId, @Param("nameType") String nameType);
    int insert(TypeFilterDTO record);
    int update(TypeFilterDTO record);
    String getParentItemName(String itemName);

    List<Map<String,Object>> getHealthValue(@Param("names") List<String> names,@Param("assetsId") String assetsId);
}
