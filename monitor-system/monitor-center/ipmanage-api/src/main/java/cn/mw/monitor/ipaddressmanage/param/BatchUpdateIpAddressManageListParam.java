package cn.mw.monitor.ipaddressmanage.param;

import lombok.Data;

import java.util.List;

/**
 * @author bkc
 * @date 2020/7/31
 */
@Data
public class BatchUpdateIpAddressManageListParam {

    private List<AddUpdateIpAddressManageListParam> batchUpdate;

}
