package cn.mw.monitor.vendor.dao;

import cn.mw.monitor.service.dropdown.param.DropdownDTO;
import cn.mw.monitor.vendor.dto.MwVendorManageTableDTO;
import cn.mw.monitor.vendor.model.MwBaseVendorIcon;
import cn.mw.monitor.vendor.model.MwVendorManageTable;
import cn.mw.monitor.vendor.param.AddOrUpdateVendorManageParam;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2021/1/20 9:44
 * @Version 1.0
 */
public interface MwVendorManageDao {

    int delete(List<Integer> ids);

    int deleteVendorIcon(List<Integer> vendorIds);

    int insert(AddOrUpdateVendorManageParam record);

    MwVendorManageTable selectById(Integer id);

    List<MwVendorManageTableDTO> selectList(Map record);

    int update(AddOrUpdateVendorManageParam record);

    List<MwBaseVendorIcon> selectVendorDropdownList(String specification);

    List<DropdownDTO> selectDropdown();

    List<MwBaseVendorIcon> selectBVendorDropdownList();

    List<String> selectVModelDropdownList(String vendor);

    int selectCountVendorByName(String vendor);

    int selectCountMacVendor(String vendor);

    int insertBaseVendor(AddOrUpdateVendorManageParam addParam);

    int insertMacVendor(AddOrUpdateVendorManageParam addParam);

    int selectVendorIdBYName(String vendor);

}
