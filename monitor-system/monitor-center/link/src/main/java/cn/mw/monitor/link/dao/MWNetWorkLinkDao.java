package cn.mw.monitor.link.dao;

import cn.mw.monitor.link.dto.LinkDirectoryDetailDto;
import cn.mw.monitor.link.dto.MwLinkDropDowmDto;
import cn.mw.monitor.link.dto.MwLinkTreeDto;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/7/20 11:40
 */
public interface MWNetWorkLinkDao {

    int insert(AddAndUpdateParam addAndUpdateParam);

    int update(AddAndUpdateParam addAndUpdateParam);

    int delete(List<String> linkIds);

    List<NetWorkLinkDto> getLinkList();


    List<NetWorkLinkDto> getPubLinkList(Map pubCriteria);

    List<NetWorkLinkDto> getAllLinkList(Map pubCriteria);

    int selectHostIdCount(@Param("hostId") String hostId, @Param("scanType") String scanTYpe);

    NetWorkLinkDto selectNetWorkLinkDto(String linkId);

    NetWorkLinkDto selectById(String linkId);




    int enableActive(@Param("enable") String enable, @Param("linkId") String linkId);

    List<String> selectAssetsLink(List<DeleteTangAssetsID> idList);

    List<Map<String, String>> getLinkListIds();

    List<Map<String,String>> fuzzSearchAllFiled(@Param("value") String value);


    List<NetWorkLinkDto> linkFuzzSearchAllFiled(@Param("value") String value);

    List<NetWorkLinkDto> getAllLink();


    //查询线路树状结构
    List<MwLinkTreeDto> selectLinkTree(Map map);

    //查询关联ID数据
    List<Map<String,Object>> selectLinkIdAndTreeId();

    /**
     * 添加线路目录
     * @param mwLinkTreeDto 目录数据
     */
    void insertLinkTree(MwLinkTreeDto mwLinkTreeDto);

    /**
     * 修改线路目录
     * @param mwLinkTreeDto 目录数据
     */
    void updateLinkTree(MwLinkTreeDto mwLinkTreeDto);

    /**
     * 删除线路目录数据
     * @param id 目录ID
     */
    void deleteLinkTree(@Param("id") Integer id);

    /**
     * 添加线路目录与线路关系
     * @param treeId 目录ID
     * @param linkIds 线路ID集合
     */
    void insertLinkIdAndTreeId(@Param("treeId") Integer treeId,@Param("linkIds") List<String> linkIds);

    /**
     * 删除目录关联线路数据
     * @param treeId 目录ID
     */
    void deleteLinkIdAndTreeId(@Param("treeId") Integer treeId);

    /**
     * 查询所有线路Id与名称
     * @return
     */
    List<Map<String,String>> selectLinkIdAndName();

    void updateLinkTreeParentId(@Param("originId") Integer originId,@Param("targetId") Integer targetId);

    void deleteLinkId(@Param("linkId") String linkId);


    List<Integer> selectLinkTreeId(@Param("parentId") Integer parentId);

    void deleteLinkTreeIds(@Param("ids") List<Integer> ids);

    List<Map<String,Object>> selectLinkTreeDropDown(@Param("ids") List<String> ids);

    Integer selectTreeId(@Param("linkId") String linkId);

    void deleteTreeLinkIds(@Param("ids") List<String> ids);


    /**
     * 查询ICMP资产信息
     * @param linkTargetIp ip
     * @return
     */
    Map<String,Object> selectIcmpAssets(@Param("ip") String linkTargetIp);

    /**
     * 根据assetsId和IP地址查询对应设备线路
     * @param linkNames 线路名称集合
     * @param linkTargetIp 目标IP
     * @return
     */
    List<AddAndUpdateParam> getLinkListByAssetsIdAndIp(@Param("linkNames") List<String> linkNames,@Param("linkTargetIp")String linkTargetIp);

    /**
     * 查询线路下拉内容
     * @return
     */
    List<MwLinkDropDowmDto> selectLinkDropDown();

    /**
     * 查询线路目录信息
     * @param linkIds
     * @return
     */
    List<LinkDirectoryDetailDto> selectLinkDirectoryByLinkId(@Param("linkIds") List<String> linkIds);

}