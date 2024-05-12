package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.param.AddAndUpdateModelFirmParam;
import cn.mw.monitor.model.param.AddAndUpdateModelMACParam;
import cn.mw.monitor.model.param.AddAndUpdateModelSpecificationParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2022/4/28
 */
public interface MWModelVendorDao {
    Integer checkFirmByName(AddAndUpdateModelFirmParam param);

    Integer checkSpecification(AddAndUpdateModelSpecificationParam qParam);

    void modelFirmAdd(AddAndUpdateModelFirmParam param);

    void updateModelFirm(AddAndUpdateModelFirmParam param);

    void addBrandSpecification(AddAndUpdateModelSpecificationParam qParam);

    List<AddAndUpdateModelFirmParam> queryModelFirmTree();

    void deleteModelFirm(Integer brandId);

    void deleteSpecificationByBrand(Integer brandId);

    void updateBrandSpecification(AddAndUpdateModelSpecificationParam qParam);

    void deleteBrandSpecification(@Param("ids") List<Integer> ids);

    List<AddAndUpdateModelSpecificationParam> queryBrandSpecification(AddAndUpdateModelSpecificationParam qParam);

    List<AddAndUpdateModelSpecificationParam> querySpecificationByBrand(@Param("brand") String brand, @Param("specification") String specification);

    List<AddAndUpdateModelMACParam> queryMACInfoList(AddAndUpdateModelMACParam qParam);

    void addMACInfo(AddAndUpdateModelMACParam qParam);

    void editorMACInfo(AddAndUpdateModelMACParam qParam);

    void deleteMACInfo(@Param("macList") List<String> macList);

    Integer checkMACInfo(AddAndUpdateModelMACParam qParam);

    List<String> getMacVendorByShortName(AddAndUpdateModelMACParam qParam);

    List<Map<String, String>> fuzzSearchAllFiledByMAC();

    List<Map<String, String>> fuzzSearchAllFiledBySpecification();

    AddAndUpdateModelFirmParam getFirmByName(String name);

}
