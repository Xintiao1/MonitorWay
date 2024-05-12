package cn.mw.monitor.ipaddressmanage.paramv6;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.ipaddressmanage.dto.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

import java.util.Date;
import java.util.List;

@Data
@ApiModel("ip地址管理主表")
@Builder
public class Ipv6ManageTableParam extends BaseParam {
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
    private Integer useCount;
    //未使用ip数量
    private Integer notuseCount;
    //预留ip数量
    private Integer reservedCount;

    //分配
    private Integer issueNo=0;
    //回收
    private Integer issueDone=0;

    //资产状态 已知
    private Integer assetTypeNo=0;
    //资产状态 未知
    private Integer assetTypeYes=0;

    //在线ip数量
    private Integer online;
    //离线ip数量
    private Integer offline;

    private Integer timing;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    //IP地址段
    private String ipAddresses;
    private Boolean include;

    private List<UserDTO> principal;

    private List<OrgDTO> orgIds;

    private List<GroupDTO> groupIds;

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

    //是否为ipv4
    private boolean isIPv4;

    //是否为ipv4
    private Integer treeType;

    //子节点
    private Integer indexSort;

    @Tolerate
    public Ipv6ManageTableParam() {}
}
