package cn.mw.monitor.ipaddressmanage.dao;
import cn.mw.monitor.ipaddressmanage.param.AddUpdateIpAddressManageListHisParam;
import cn.mw.monitor.ipaddressmanage.param.AddUpdateIpAddressManageListParam;
import cn.mw.monitor.ipaddressmanage.param.MwIpAddressManageListTable;
import cn.mw.monitor.ipaddressmanage.param.QueryIpAddressManageListParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwIpv6ManageListDao {

    List<AddUpdateIpAddressManageListParam> selectSonList(Map priCriteria);

    int insertIpv6List(List<QueryIpAddressManageListParam> paramList);

    int updateIpv6List(List<QueryIpAddressManageListParam> paramList);

    //批量查询2
    List<MwIpAddressManageListTable> selectSonList2(@Param("ids") List<Integer> ids);
    //批量查询1
    List<MwIpAddressManageListTable> selectSonList1(@Param("id") Integer qParam);

    //批量修改1
    int updateBatch(AddUpdateIpAddressManageListParam updateList);

    //批量删除
    int deleteBatch(AddUpdateIpAddressManageListParam updateList);

    //查询ip清单历史
    List<AddUpdateIpAddressManageListHisParam> getHisList(Map priCriteria);

    //查询ip清单历史
    AddUpdateIpAddressManageListHisParam getHisListForOne(@Param("ipAddress") String ipAddress);

    //批量更新 ip地址清单历史表
    int batchUpdateHis(List<AddUpdateIpAddressManageListHisParam> list);

    //批量添加 ip地址清单历史表
    int batchCreateHis(List<AddUpdateIpAddressManageListHisParam> list);

    int deleteHisByLinkId(@Param("linkId") Integer linkId);

    int deleteHisById(@Param("linkId") Integer linkId);


    void insertIpv6(@Param("item")  AddUpdateIpAddressManageListParam uParam);
}
