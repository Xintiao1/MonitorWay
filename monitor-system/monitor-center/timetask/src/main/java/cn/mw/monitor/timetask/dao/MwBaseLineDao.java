package cn.mw.monitor.timetask.dao;

import cn.mw.monitor.timetask.entity.MwBaseLineHealthValueDto;
import cn.mw.monitor.timetask.entity.MwBaseLineItemNameDto;
import cn.mw.monitor.timetask.entity.MwBaseLineManageDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwBaseLineDao
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/4/6 10:36
 * @Version 1.0
 **/
public interface MwBaseLineDao {

    //获取基线监控项数据
    List<MwBaseLineItemNameDto> getItemNames();

    //添加基线数据
    int insertBaseLine(MwBaseLineManageDto baseLineManageDto);

    //修改基线数据
    int updateBaseLine(MwBaseLineManageDto baseLineManageDto);

    //删除基线数据
    int deleteBaseLine(@Param("ids") List<Integer> ids);

    //查询基线数据
    List<MwBaseLineManageDto> selectBaseLine(MwBaseLineManageDto baseLineManageDto);

    //根据ID获取基线监控项数据
    List<MwBaseLineItemNameDto> getItemNamesByIds(@Param("itemIds") List<Integer> itemIds);

    //删除健康值数据
    void deleteBaseLineHealthData();

    //添加健康值数据
    int insertBaseLineHealthData(List<MwBaseLineHealthValueDto> healthValueDtos);

    List<String> getItemIds();

    List<MwBaseLineHealthValueDto> getHealthValue(@Param("names") List<String> names,@Param("assetsId") String assetsId);

    /**
     * 获取所有基线健康值
     * @return
     */
    List<Map<String,Object>> getAllHealthValue();
}
