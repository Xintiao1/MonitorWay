package cn.huaxing.dao;

import cn.huaxing.dto.HuaxingVisualizedDataDto;
import cn.huaxing.dto.HuaxingVisualizedDataSourceDto;
import cn.huaxing.dto.HuaxingVisualizedDataSourceSqlDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author gengjb
 * @description 可视化插件DAO
 * @date 2023/8/28 10:43
 */
@Mapper
public interface HuaxingVisualizedDao {

    //查询华星的数据库连接信息
    List<HuaxingVisualizedDataSourceDto> getHuaxingDataBaseConnectionInfo();

    //查询华兴的SQL信息
    List<HuaxingVisualizedDataSourceSqlDto> getHuaxingDataBaseSqlInfo();

    //删除缓存的华兴数据
    void deleteHuaxingCacheData();

    //新增华兴缓存数据
    void insertHuaxingcacheData(@Param("list") List<HuaxingVisualizedDataDto> huaxingVisualizedDataDtos);

    //查询缓存数据
    HuaxingVisualizedDataDto selectHuaxingcacheData(@Param("chartType") Integer chartType,@Param("partitionName") String partitionName);

}
