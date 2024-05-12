package cn.mw.monitor.screen.dao;

import cn.mw.monitor.screen.dto.MWNewScreenAssetsFilterDto;
import cn.mw.monitor.screen.dto.MWNewScreenLabelDto;
import cn.mw.monitor.screen.dto.MWNewScreenModuleDto;
import cn.mw.monitor.screen.dto.MWTangibleassetsDto;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MWNewScreenManageDao
 * @Description 新大屏接口
 * @Author gengjb
 * @Date 2021/11/29 10:39
 * @Version 1.0
 **/
public interface MWNewScreenManageDao {

    //查询资产及资产类型
    String selectNewScreenAssets(Integer typeId);

    //获取时间范围内的资产数据
    List<Map<String,Object>> selectAssetsDateRegionData(@Param("startTime") String startTime, @Param("endTime") String endTime);

    //获取某天的资产数量
    Integer selectOneDayAssetsCount(@Param("time") String time);

    //查询新大屏初始化模块模块信息
    List<MWNewScreenModuleDto> selectNewScreenInitModule(@Param("type") Integer type);

    //根据用户查询用户模块信息
    List<MWNewScreenModuleDto> selectUserNewScreenModule(@Param("userId") int userId);

    //插入用户的模块数据
    int insertNewScreenUserModule(@Param("items") List<MWNewScreenModuleDto> screenModelDtos);

    //新增资产过滤数据
    int insertNewScreenAssetsFilter(MWNewScreenAssetsFilterDto assetsFilterDto);

    //修改资产过滤数据
    void updateNewScreenAssetsFilter(MWNewScreenAssetsFilterDto assetsFilterDto);

    //修改新大屏卡片名称
    void updateNewScreenModuleName(@Param("name") String name,@Param("modelDataId") String modelDataId,@Param("userId") int userId);

    //查询资产过滤数据信息
    MWNewScreenAssetsFilterDto selectNewScreenAssetsFilterData(@Param("modelDataId") String modelDataId,@Param("userId") int userId,@Param("modelId") int modelId);

    //删除用户模块信息
    void deleteNewScreenUserModule(@Param("modelDataId") String modelDataId,@Param("userId") int userId,@Param("bulkId") int modelId);

    //获取首页的过滤条件
    MwCommonAssetsDto getNewScreenFilterAssets(MWNewScreenAssetsFilterDto param);

    //修改模块创建时间
    void updateNewScreenCreateDate(@Param("createDate") Date createDate, @Param("modelDataId") String modelDataId, @Param("userId") int userId, @Param("bulkId") int bulkId);

    //擦汗寻资产数量
    int selectAssetsCount();

    //根据如期查询资产数量
    int saveAssetsAmountCensusData(@Param("count") int count,@Param("censusDate") String censusDate);

    //查询资产数据
    List<MWTangibleassetsDto> selectAsssetsData(@Param("assetsTypeId") Integer assetsTypeId,@Param("assetsTypeSubId") Integer assetsTypeSubId);

    //删除过滤规则
    int deleteMwAlertRuleSelect(String uuid);

    //添加过滤规则
    int insertMwAlertRuleSelect(List<MwRuleSelectParam> param);

    //查询过滤规则
    List<MwRuleSelectParam> selectMwAlertRuleSelect(String uuid);

    //查询标签信息
    List<String> selectLabelValue(@Param("dropCode") String dropCode);

    //查询初始化首页模块
    MWNewScreenModuleDto selectNewScreenInitModuleById(@Param("bulkId") int bulkId);

    //修改首页条件
    int updateNewHomeModule(MWNewScreenModuleDto moduleDto);

    List<Map<String,Object>> getAssetsIdAndServerId();

    //根据用户ID查询资产过滤数据信息
    List<MWNewScreenAssetsFilterDto> selectNewScreenAssetsFilterByUserId(@Param("userId") int userId);

    //删除模块
    void deleteModuleInfo(@Param("userId") int userId);

    //删除模块
    void deleteScreenFilterInfo(@Param("userId") int userId);

    //批量新增资产过滤数据
    int batchInsertNewScreenAssetsFilter(@Param("list") List<MWNewScreenAssetsFilterDto> assetsFilterDtos);

}
