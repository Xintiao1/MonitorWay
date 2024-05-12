package cn.mw.monitor.ipaddressmanage.param;

/**
 * @author bkc
 */

import lombok.Data;

import java.util.List;

@Data
public class DeleteIpAddressManageParam {

    private List<String> ipAddressmanageIds;
}
