package cn.mw.monitor.assets.dao;

import cn.mw.monitor.service.assets.param.MWMainTainHostParam;
import cn.mw.monitor.service.assets.param.MwAssetsMainTainParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MwAssetsMainTainDao
 * @Description 资产管理维护的数据层
 * @Author gengjb
 * @Date 2021/7/27 14:34
 * @Version 1.0
 **/
public interface MwAssetsMainTainDao {

    /**
     * 添加资产管理维护基础数据
     * @param param 资产管理维护添加数据
     */
    void addAssetsMainTain(MwAssetsMainTainParam param);

    /**
     * 添加基础数据表与主机ID表的关联关系及主机数据
     * @param hostids 主机ID的集合
     */
    void addAssetsMainTainHost(List<Map<String,Object>> hostids);


//    /**
//     * 添加基础数据表与主机组ID表的关联关系及主机组数据
//     * @param groupids 主机组ID的集合
//     */
//    void addAssetsMainTainHostGroup(List<Map<String,Object>> groupids);
//
//    /**
//     * 添加时间段数据
//     * @param times 时间段数据集合
//     */
//    void addAssetsMainTainTimes(List<HashMap> times);


    /**
     * 查询维护页面的基本数据
     * @param param 查询条件的数据
     * @return
     */
    List<MwAssetsMainTainParam> selectMainTain(MwAssetsMainTainParam param);

    /**
     *查询该维护下的主机
     * @param mainTainIds 维护基础数据的主键
     * @return
     */
    List<Map<String,Object>> selectMainTainHostData(List<Integer> mainTainIds);


//    /**
//     *查询该维护下的主机组
//     * @param mainTainIds 维护基础数据的主键
//     * @return
//     */
//    List<Map<String,Object>> selectMainTainHostGroupData(List<Integer> mainTainIds);
//
//    /**
//     *查询该维护下的时间段
//     * @param mainTainIds 维护基础数据的主键
//     * @return
//     */
//    List<HashMap<String,Object>> selectMainTainTimeSlotData(List<Integer> mainTainIds);


    /**
     *修改维护页面数据
     * @param param 修改的数据
     */
    void updatemainTain(MwAssetsMainTainParam param);

    /**
     *删除主机数据
     * @param mainTainIds 维护页面表的主键
     */
    void deleteHostIdDate(List<Integer> mainTainIds);

//    /**
//     *删除主机组数据
//     * @param mainTainIds 维护页面表的主键
//     */
//    void deleteHostGroupIdDate(List<Integer> mainTainIds);
//
//    /**
//     *删除时间段数据
//     * @param mainTainIds 维护页面表的主键
//     */
//    void deleteTimeSlotDate(List<Integer> mainTainIds);

    /**
     * 删除维护的数据
     * @param ids 资产管理中维护页面主键
     */
    void deleteMainTain(List<Integer> ids);

    /**
     *查询主机群组数据
     * @param serverId 监控服务器ID
     * @return
     */
    List<Map<String,Object>> selectHostGroupDropDown(Integer serverId);

    /**
     *查询主机数据
     * @param mainTainParam 资产类型ID
     * @return
     */
    List<Map<String,Object>> selectHostDropDown(MwAssetsMainTainParam mainTainParam);


//    /**
//     * 添加基础数据表与标记
//     * @param tags 标记数据
//     */
//    void addAssetsMainTainTags(List<Map<String,Object>> tags);
//
//    /**
//     *查询该维护下的标记
//     * @param mainTainIds 维护基础数据的主键
//     * @return
//     */
//    List<Map<String,Object>> selectMainTainTagData(List<Integer> mainTainIds);
//
//
//    /**
//    *删除标记
//     * @param mainTainIds 维护页面表的主键
//     */
//    void deleteTagData(List<Integer> mainTainIds);

    Integer selectRepeatName(@Param("name") String name, @Param("id") Integer id);

    List<String> selectMainTainPlanNames();

    //查询主机
    List<MWMainTainHostParam> getHostInfo(@Param("mainTainId") Integer mainTainId);
}
