package cn.mw.monitor.assets.dao;

import cn.mw.monitor.service.assets.model.AssetsInterfaceDTO;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import cn.mw.monitor.service.model.param.MwModelFilterInterfaceParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/5/11
 */
public interface MwAssetsInterfaceDao {
    List<QueryAssetsInterfaceParam> getAllInterface(@Param("assetsId") String assetsId, @Param("interfaceName") String interfaceName,@Param("vlanFlag") Boolean vlanFlag);

    MwModelFilterInterfaceParam getFilterInfo();

    //查询全表数据
    List<AssetsInterfaceDTO> getAllInterfaceDTO();

    //根据条件查询全表数据
    List<AssetsInterfaceDTO> getAllInterfaceDTOByCriteria(Map map);

    int batchInsert(List<AssetsInterfaceDTO> assetsInterfaceDTOS);

    void insertInterface(AssetsInterfaceDTO assetsInterfaceDTOS);

    int deleteIntefaces(List<DeleteTangAssetsID> ids);

    int deleteIntefacesById(List<Integer> ids);

    Integer getSettingByAssets(@Param("assetsId") String assetsId);

    void updataInterfaceStatus(@Param("id") Integer id,@Param("interfaceSetState") Boolean interfaceSetState);

    String getUUIDByAssetsId(String assetsId);


}
