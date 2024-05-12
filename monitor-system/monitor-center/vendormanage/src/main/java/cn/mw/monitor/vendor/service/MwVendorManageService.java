package cn.mw.monitor.vendor.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.vendor.param.AddOrUpdateVendorManageParam;
import cn.mw.monitor.vendor.param.QueryVendorManageParam;

import java.util.List;

/**
 * @author syt
 * @Date 2021/1/20 10:16
 * @Version 1.0
 */
public interface MwVendorManageService {
    Reply selectById(Integer id);

    Reply selectList(QueryVendorManageParam qsParam);

    Reply update(AddOrUpdateVendorManageParam uParam);

    Reply delete(List<Integer> ids,List<Integer> vendorId);

    Reply insert(AddOrUpdateVendorManageParam ausDTO) throws Exception;

}
