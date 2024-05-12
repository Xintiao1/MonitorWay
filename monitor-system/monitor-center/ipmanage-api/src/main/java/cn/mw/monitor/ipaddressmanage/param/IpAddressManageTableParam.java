package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.ipaddressmanage.dto.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;
import org.apache.poi.ss.formula.functions.T;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/17
 */
@Data
@ApiModel("ip地址管理主表")
@Builder
public class IpAddressManageTableParam extends BaseParam {
    //主键
    private Integer id;

    //父级id
    private Integer parentId;

    //名称
    private String label;

    //类型: 包或地址段 grouping  iPaddresses
    private String type;

    private boolean leaf;

    //描述
    private String descri;

    //子网掩码
    private String mask;


    //使用ip数量
    private Integer useCount=0;
    //未使用ip数量
    private Integer notuseCount=0;
    //预留ip数量
    private Integer reservedCount=0;

    //在线ip数量
    private Integer online=0;
    //离线ip数量
    private Integer offline=0;

    //分配
    private Integer issueNo=0;
    //回收
    private Integer issueDone=0;

    //资产状态 已知
    private Integer assetTypeNo=0;
    //资产状态 未知
    private Integer assetTypeYes=0;


    private Integer timing;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    //IP地址段
    private String ipAddresses;
    private Integer include;

    @ApiModelProperty(value="IP地址段")
    private List<MwIpAddresses1DTO> ipsubnets;

    private List<UserDTO> principal;

    private List<OrgDTO> orgIds;

    private List<GroupDTO> groupIds;

    private List<LabelDTO> labels;

    //国家
    private String country;
    //省
    private String state;
    //市
    private String city;
    //县
    private String region;
    //详细地址
    private String addressDesc;
    //经度
    private String longitude;
    //纬度
    private String latitude;
    @ApiModelProperty(value="是否做选项分组 0.作为分组 1.不作为分组")
    private Integer radioStatus;



    //公有ip地理位置
    private List<PubIpDto> geoIp;

    //是否为ipv4
    private boolean isIPv4;


    //是否为ipv4
    private Integer treeType;

    //子节点
    private List<Object> children;

    //子节点
    private Integer indexSort;


    private boolean isLeaf;

    //是否是临时IP
    private boolean isTem;

    @Tolerate
    public IpAddressManageTableParam() {}
}
