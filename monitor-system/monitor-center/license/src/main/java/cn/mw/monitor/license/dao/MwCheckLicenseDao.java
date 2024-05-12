package cn.mw.monitor.license.dao;

import cn.mw.monitor.service.license.param.LicenseXmlParam;
import cn.mw.monitor.service.license.param.QueryLicenseParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MwCheckLicenseDao {

    int insertLicense(QueryLicenseParam param);
    int deleteLicenseByModuleId(@Param("moduleId") String moduleId);
    List<QueryLicenseParam> queryAllExpireLicense();
    int updateLicenseDatail(LicenseXmlParam param);
    int initInsertLicenseDatail(LicenseXmlParam param);
    int initDeleteLicenseDatail();
    List<LicenseXmlParam> queryLicenseDatail();

    int selectTableCount(@Param("tableName") String tableName, @Param("deleteFlag") boolean deleteFlag);

    int selectAssetsCount(@Param("assetsTypeIds") List<Integer> assetsTypeIds, @Param("monitorModes") List<Integer> monitorModes);

    Integer updateMwModule(@Param("moduleIds")List<Integer> moduleId, @Param("deleteFlag")Boolean deleteFlag,@Param("nonControlIds")List<Integer> nonControlIds);

    Integer updateRoleModulePerMapper(@Param("moduleIds")List<Integer> moduleId, @Param("enable") Boolean enable,@Param("nonControlIds")List<Integer> nonControlIds);

    List<Integer> selectMwModuleByIds(@Param("moduleIds")List<Integer> moduleId);

    List<Integer> selectMwModuleByPids(@Param("moduleIds")List<Integer> moduleId);

    Integer selectCountMwModule(@Param("moduleId")Integer moduleId);

    Integer selectMwModuleById(@Param("moduleId")Integer moduleId);
}
