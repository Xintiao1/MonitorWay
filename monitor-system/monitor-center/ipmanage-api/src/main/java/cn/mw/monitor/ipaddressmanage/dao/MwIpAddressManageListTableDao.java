package cn.mw.monitor.ipaddressmanage.dao;


import cn.mw.monitor.ipaddressmanage.model.BatchOnLineUpdate;
import cn.mw.monitor.ipaddressmanage.param.*;
import cn.mw.monitor.service.ipmanage.model.IpManageTree;
import cn.mw.monitor.service.user.model.ScanIpAddressManageQueueVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author bkc
 */
public interface MwIpAddressManageListTableDao {
    //查询父级节点id
    Set<Integer> getIdsByIds(@Param("ids") Set<Integer> ids);

    //设置ip地址状态等
    int updateBatch(List<IpAddressManageTableParam> updateList);

    //查询ip清单历史
    List<AddUpdateIpAddressManageListHisParam> getHisList(Map priCriteria);
    //查询ip清单历史
    List<Map<String,Object>> getHisListMap(Map priCriteria);
    List<AddUpdateIpAddressManageListHisParam> selectHis(Map priCriteria);

    //查询ip地址清单
    List<AddUpdateIpAddressManageListParam> selectListByLinkId(@Param("linkId") Integer linkId);

    //根据mac地址查询厂商
    MwOUIParam selectOUIByMac(@Param("mac") String mac);

    //批量更新 ip地址清单
    int batchUpdateList(List<AddUpdateIpAddressManageListParam> list);

    //批量更新 ip地址清单历史表
    int batchUpdateHis(List<AddUpdateIpAddressManageListHisParam> list);

    //批量添加 ip地址清单历史表
    int batchCreateHis(List<AddUpdateIpAddressManageListHisParam> list);

    //根据ip地址清单表主键查询历史表
    AddUpdateIpAddressManageListHisParam selectLastHis(@Param("linkId") Integer linkId);

    //根据ip地址清单表主键查询历史表
    List<AddUpdateIpAddressManageListHisParam> selectLastHisTwo(@Param("oa") List<Integer> oa,@Param("ob") List<List<Integer>> ob);



    //批量查询2
    List<MwIpAddressManageListTable> selectSonList2(@Param("ids") List<Integer> ids);
    //批量查询1
    List<MwIpAddressManageListTable> selectSonList1(@Param("id") Integer qParam);

    List<QueryIpAddressManageListParam> selectListByInterval(Integer qParam);

    //查询
    List<AddUpdateIpAddressManageListParam> selectSonList(Map priCriteria);

    //批量添加
    int insertBatch(List<AddUpdateIpAddressManageListParam> insertList);

    //批量添加ip对应端口信息
    int insertPortInfos(List<AddUpdatePortInfoParam> insertList);

    //删除添加ip对应端口信息
    void deletePortInfos(List<AddUpdatePortInfoParam> deleteList);

    //批量添加ip对应端口历史信息
    void insertHisPortInfos(List<AddUpdatePortInfoHisParam> insertList);

    List<AddUpdatePortInfoParam> selectPortInfos(List<Integer> ipManageIds);

    //批量修改1
    int updateBatch1(AddUpdateIpAddressManageListParam updateList);

    //批量删除
    int deleteBatch(AddUpdateIpAddressManageListParam updateList);

    //批量删除历史
    int deleteBatchList(AddUpdateIpAddressManageListParam updateList);

    //删除
    int deleteByLinkId(@Param("id") Integer id);

    //通过ip地址段主键删除ip清单历史
    int deleteHisByLinkId(@Param("linkId") Integer linkId);

    //模糊查询ip清单
    List<String> selectAllIpLIst(@Param("dimIp") String dimIp,@Param("signId") Integer signId);

    int updateAssetsTypeKnow(@Param("linkId") Integer linkId);

    int updateAssetsTypeUnKnow(@Param("linkId") Integer linkId);

    //还原一些已知资产
    int updateAssetsTypeRollback(@Param("linkId") Integer linkId);

    //查询ip地址分组对应的子网
    String selectIpAddresses(@Param("linkId") Integer linkId);

    void updateIpIncludecollect(@Param("include") Integer include,@Param("ids") List<String> strings);

    List<Map<String, Object>> selectMapString(@Param("strings")List<String> strings,@Param("i") Integer i );

    void addScanQueue(@Param("parm")String parm, @Param("userId")Integer userId, @Param("linkId")Integer linkId);

    List<ScanIpAddressManageQueueVO> selectqueue(@Param("id") Integer id);

    void deleteQueue(@Param("id") Integer id, @Param("linkId") Integer linkId);


    Integer selectIpMangeHaveDistribution(@Param("ids")  List<Integer> ids);

    void updateScanTime(@Param("type") int i, @Param("linkId") Integer o, @Param("ids") List<Integer> ids);

    //批量更新ip在线状态
    void batchUpdateOnLineList(BatchOnLineUpdate batchOnLineUpdate);

    List<AddUpdateIpAddressManageListParam> getIPaddresssByIds( @Param("ids") List<Integer> ids);

    void insertIpaddresStatus(@Param("addIpaddresStatusHiss")  List<AddIpaddresStatusHis> addIpaddresStatusHiss);

    List<IpManageTree> getAllManage(@Param("id") Integer id);


    List<IpManageTree> selectCountIpOper(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("ipManageTrees") List<IpManageTree> ipManageTrees);
}
