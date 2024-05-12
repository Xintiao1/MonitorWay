package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.ipaddressmanage.dto.AssetsDto;
import cn.mw.monitor.service.vendor.model.VendorIconDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.models.auth.In;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("ip地址管理子表")
public class QueryIpAddressManageListParam extends BaseParam {
    //主键
    private Integer id;

    //ip地址管理表主键
    private Integer linkId;

    //ip地址
    private String searchAll;

    //ip地址
    private String ipAddress;


    private String ipType;

    //状态
    private Integer ipState;

    //备注
    private String remarks;

    private String creator;

    private Date createDate;

    private Date createDateStart;

    private Date createDateEnd;


    private Date scanTime;

    private String modifier;

    private Date modificationDate;

    private Date modificationDateStart;

    private Date modificationDateEnd;

    //在线状态  在线  离线
    private Integer online;

    //mAC地址
    private String mac;

    //厂商
    private String vendor;

    //接入设备
    private String accessEquip;

    //接入端口
    private String accessPort;

    //最后一次离线时间
    private Date lastDate;

    //同步数据时间间隔
    private Integer interval;

    //更新时间
    private Date updateDate;

    private String assetsId;

    //可用地址
    private Integer include;

    private String assetsName;

    //排序字段
    private String orderName;

    //排序规则
    private String orderType;

    //图标
    private VendorIconDTO vendorIconDTO;

    //资产详情
    private List<AssetsDto> assetsDetail;

    private String assetsTypeInOrOUt;

    //接入端口名称
    private String accessPortName;

    //资产类型
    private String assetsType;

    @ApiModelProperty(value="临时ip")
    private Integer isTem;

    @ApiModelProperty(value="ip地址分配状态")
    private Integer distributionStatus;

}
