package cn.mw.monitor.ipaddressmanage.paramv6;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.ipaddressmanage.dto.MwIpAddresses1DTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
@ApiModel("ip地址管理主表")
public class Ipv6ManageTable1Param extends BaseParam {
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

    private boolean include;
    //使用ip数量
    private Integer useCount;
    //未使用ip数量
    private Integer notuseCount;
    //预留ip数量
    private Integer reservedCount;

    //在线ip数量
    private Integer online;
    //离线ip数量
    private Integer offline;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    //IP地址段
    private String ipAddresses;


    private List<Integer> principal;

    private List<Integer> orgIdss;
    private List<List<Integer>> orgIds = new ArrayList<>();

    private List<Integer> groupIds;

    private List<Integer> labels;

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

    private String ipRandStart;

    private String ipRandEnd;

    @ApiModelProperty(value="是否做选项分组 0.不作为作为分组 1.作为为分组")
    private boolean radioStatus;
    //排序
    private Integer indexSort;

    private Integer signId;
}
