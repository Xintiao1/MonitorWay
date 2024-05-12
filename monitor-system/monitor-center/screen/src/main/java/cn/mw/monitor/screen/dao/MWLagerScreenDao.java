package cn.mw.monitor.screen.dao;

import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.screen.dto.*;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.screen.model.Model;
import cn.mw.monitor.screen.model.ModelBaseTable;
import cn.mw.monitor.screen.param.EnableParam;
import cn.mw.monitor.screen.param.MwLagerScreenParam;
import cn.mw.monitor.screen.param.ScreenNameParam;
import cn.mw.monitor.screen.param.UpdateModelDataParam;
import cn.mw.monitor.service.user.dto.DataPermissionDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/4/10 10:30
 */
public interface MWLagerScreenDao {

    Integer addLagerScreen(MwLagerScreenParam lagerScreen);

    Integer updateLagerScreen(MwLagerScreenParam lagerScreen);

    Integer deleteLagerScreen(String screenId);

    String selectScreenName(String screenId);

    //创建组件数据 当用户选择组件添加到块数据的时候
    Integer addModelData(@Param("id") String id, @Param("modelId") Integer modelId, @Param("openMapName")boolean openMapName);

    Integer updateBulkdata(@Param("id") String id, @Param("moduleDateId") String moduleDateId);

    @Deprecated
    int addScreenUser(List<ScreenUserMapper> userMapper);

    @Deprecated
    int addScreenGroup(List<ScreenGroupMapper> groupMapper);

    @Deprecated
    int addScreenOrg(List<ScreenOrgMapper> orgMapper);

    int deleteScreenUser(String screenId);

    int deleteScreenGroup(String screenId);

    int deleteScreenOrg(String screenId);

    int deleteLayoutData(String layoutDataId);

    int deleteLayoutDatabyBulkDataId(String bulkDataId);

    String selectLayoutDataId(String screenId);

    List<String> selectBulkDataId(String layoutDataId);

    List<String> selectModelData(List<String> bulkDataIds);

    int deleteBulkData(List<String> bulkDataIds);

    int deleteModelData(List<String> moduleDataIds);

    int deleteAssetsFilters(List<String> moduleDataIds);

    int deleteModelDataById(String moduleDataId);

    List<LagerScreenDataDto> getPriLargerScreenList(Map criteria);

    List<LagerScreenDataDto> getPubLargerScreenList(Map criteria);

    List<CoordinateAddress> getCoordinateAddress();

    String getCoordinateAddressByOrgId(Integer id);

    List<AssetOrgMapperDto> getOrgAssetInfo();

    int updateEnable(EnableParam enableParam);

    List<Integer> getLayoutBase();

    List<Model> getModelList();

    List<String> getModelType();

    //int updateBulkDataTime(@Param("bulkDataId") String bulkDataId, @Param("timelag") Integer timelag);

    int updateModelData(UpdateModelDataParam updateModelDataParam);

    int updateBulkData(@Param("bulkDataId") String bulkDataId, @Param("modelDateId") String modelDateId);

    Integer saveScreenImg(@Param("image") String image, @Param("screenId") String screenId);

    Integer insertDataPermission(DataPermissionDto dto);

    ModelContentDto getModelId(String modelDataId);

    Integer addLayoutData(LayoutDataDto layoutDataDto);

    Integer insertLayoutDataBulk(@Param("layoutDataId") String layoutDataId, @Param("bulkDateId") String bulkDateId);

    Integer deleteLayoutDataBulkMapper(String layoutDataId);

    Integer deleteScreenLayoutMapper(String layoutDataId);

    Integer deleteAssetsFilter(String moduleDataIds);

    Integer getLayoutCount(Integer layoutId);

    Integer addBulkdataId(String bulkDataId);

    List<ModelDataDto> getModelData();

    Integer updateModelDataId(String modelDataId);

    Integer deleteDataPermission(@Param("screenId") String screenId, @Param("screen") String screen);

    Integer updateScreenName(ScreenNameParam screenNameParam);

    MwCommonAssetsDto getFilterAssets(FilterAssetsParam param);

    int insertFilterAssets(FilterAssetsParam param);

    int updateFilterAssets(FilterAssetsParam param);

    List<ModelBaseTable> getModelAssetsTypeId(String moduleType);

    String getModelTypeById(Integer moduleId);

    int updateModelBase(@Param("assetsTypeName") String assetsTypeName, @Param("moduleId") Integer moduleId, @Param("assetsTypeId") String assetsTypeId);

    LagerScreenDataDto getLagerScreenById(String screenId);

    Integer getBulkDataTimeCount(String modelDataId, Integer userId);

    Integer getBulkDataTime(String modelDataId, Integer userId);

    int getFilterAssetsCount(FilterAssetsParam moduleDataId);

    int insertFilterTimeLag(@Param("modelDataId") String modelDateId, @Param("modelId") Integer modelId, @Param("timeLag") Integer timeLag, @Param("userId") Integer userId, @Param("type") String type);

    int insertLargeScreenLayoutMapper(@Param("screenId") String screenId, @Param("layoutDataId") String layoutDataId);

    String getLinkInterfaces(@Param("userId") Integer userId,@Param("type") String type,@Param("modelDataId") String modelDataId);

    int getfilterLinkCount(@Param("userId") Integer userId,@Param("type") String type,@Param("modelDataId") String modelDataId);

    int insertLinkFilter(@Param("userId") Integer userId,@Param("type") String type,@Param("modelDataId") String modelDataId,@Param("linkInterfaces") String linkInterfaces,@Param("timeLag") Integer timeLag);

    int updateLinkFilter(@Param("userId") Integer userId,@Param("type") String type,@Param("modelDataId") String modelDataId,@Param("linkInterfaces") String linkInterfaces);

    String getLinkEdit(String modelDataId,Integer userId);

    List<TargetAssetsIdDto> getTargetLinkAssetIds();

    List<TargetAssetsIdDto> getIcmpLinkAssetIds();

    //根据ip获取icmp链路信息
    List<TargetAssetsIdDto> getIcmpLinkAssetIdsByIp(List<String> ips);
}
