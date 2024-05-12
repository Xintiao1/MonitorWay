package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.ipaddressmanage.dto.AssetsDto;
import cn.mw.monitor.service.vendor.model.VendorIconDTO;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("ip地址管理子表")
public class AddUpdateIpAddressManageListParam extends BaseIpAddressManageListParam {
    private List<Integer> ids;

    //主键
    private Integer id;

    //ip地址管理表主键
    private Integer linkId;

    //ip地址管理表主键
    private Integer labelLinkId;

    //ip地址
    private String ipAddress;

    //ip类型
    private String ipType;

    //状态 1已使用，0未使用，2预留
    private Integer ipState;

    //备注 remarkes
    private String remarks;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

    //在线状态  1在线  0离线
    private Integer online;

    //最后一次离线时间
    private Date lastDate;

    private Date scanTime;

    //同步数据时间间隔
    private Integer interval;

    private Date updateDateStart;

    private Date updateDateEnd;

    private String assetsId;

    private String assetsName;

    //图标
    private VendorIconDTO vendorIconDTO;

    //资产详情
    private List<AssetsDto> assetsDetail;

    //资产类型
    private String assetsType;

    @ApiModelProperty(value="ip地址分配状态")
    private Integer distributionStatus;

    @ApiModelProperty(value="ip地址是否覆盖")
    private Integer isRewrite;
    @ApiModelProperty(value="是否扫描修改")
    private Integer isUpdate;

    @ApiModelProperty(value="地域id")
    private Integer signId;

    private String assetsTypeInOrOut;

    @ApiModelProperty(value="上联端口信息")
    private List<AddUpdatePortInfoParam>  portInfos;

    @ApiModelProperty(value="ip是否冲突")
    private boolean conflict;

    @ApiModelProperty(value="临时ip")
    private boolean isTem;

    public boolean isEmptyInfo(){
        if(StringUtils.isEmpty(getAccessEquip()) || StringUtils.isEmpty(getAccessPortName())){
            return true;
        }

        return false;
    }

    public boolean isEqualInfo(AddUpdateIpAddressManageListParam param){
        if(StringUtils.isNotEmpty(getAccessEquip()) && StringUtils.isNotEmpty(getAccessPortName())
        && StringUtils.isNotEmpty(param.getAccessEquip()) && StringUtils.isNotEmpty(param.getAccessPortName())
        && StringUtils.isNotEmpty(ipAddress) && StringUtils.isNotEmpty(param.getIpAddress())
        && getAccessEquip().equals(param.getAccessEquip()) && getAccessPortName().equals(param.getAccessPortName())
        && ipAddress.equals(param.getIpAddress())
        ){
            return true;
        }

        return false;
    }

}
