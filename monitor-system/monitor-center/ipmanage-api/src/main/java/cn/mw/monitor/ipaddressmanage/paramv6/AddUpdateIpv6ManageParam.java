package cn.mw.monitor.ipaddressmanage.paramv6;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Data
@ApiModel("ipv6地址管理主表")
public class AddUpdateIpv6ManageParam {
    //批量删除时使用
    private Set<Integer> ids;
    //主键
    private Integer id;

    //父级id
    private Integer parentId;

    //名称
    private String label;

    //类型：iPaddresses  grouping
    private String type;

    //是否为叶子节点
    private Integer leaf;

    //描述
    @Length(max=100,message = "描述信息字数不能超过100个")
    private String descri;

    //子网掩码
    private String mask;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;


    //IP地址段
    private String ipAddresses;
    private Integer include;

    private Integer timing;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;
    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;
    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;
    /*
     * 地域id
     * */
    private Integer signId ;
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

    //ipv6起始范围
    private String ipRandStart;
    //ipv6结束范围
    private String ipRandEnd;

    @ApiModelProperty(value="是否做选项分组 0.不作为作为分组 1.作为为分组")
    private Integer radioStatus;
}
