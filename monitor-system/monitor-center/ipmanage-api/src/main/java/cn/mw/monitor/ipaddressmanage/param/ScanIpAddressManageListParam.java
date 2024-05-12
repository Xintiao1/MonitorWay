package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/12/11
 */
@Data
public class ScanIpAddressManageListParam {
    private Boolean isAll;
    private Integer linkId;
    private List<AddUpdateIpAddressManageListParam> uParam;
    private List<Integer> LinkIds;
}
