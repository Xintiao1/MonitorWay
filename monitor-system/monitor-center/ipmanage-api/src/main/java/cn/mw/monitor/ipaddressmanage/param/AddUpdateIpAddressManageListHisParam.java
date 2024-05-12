package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.ipaddressmanage.dto.AssetsDto;
import cn.mw.monitor.service.vendor.model.VendorIconDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/12/10
 */
@Data
@ApiModel("ip地址管理子表历史表")
public class AddUpdateIpAddressManageListHisParam extends BaseIpAddressManageListParam {

    //主键
    private Integer id;

    //ip地址管理表主键
    private Integer linkId;

    //ip地址
    private String ipAddress;

    private Date updateDateStart;

    private Date updateDateEnd;

    //图标
    private VendorIconDTO vendorIconDTO;

    //资产详情
    private List<AssetsDto> assetsDetail;

    //资产类型
    private String assetsType;

    //上联端口信息
    private List<AddUpdatePortInfoParam>  portInfos;

    //更新批次
    private String batchId;

    //change_ip_status 修改状态 1.修改 在线状态 2.修改了使用状态 3.两种状态都改变
    private Integer changeIpStatus;
}
