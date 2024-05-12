package cn.mw.monitor.ipaddressmanage.paramv6;

import cn.mw.monitor.ipaddressmanage.dto.MwIpAddresses1DTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;


@Data
@ApiModel("ip地址管理主表")
public class QueryIpv6ManageParam {
    //主键
    private Integer id;

    //父级id
    private Integer parentId;

    //名称
    private String label;

    //类型: 包或地址段 grouping  iPaddresses
    private String type;

    private boolean leaf;

    //子网掩码
    private String mask;

    //描述
    private String descri;


    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;


    //IP地址段
    private String ipAddresses;

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


    private String perm;
    private Integer userId; //责任人
    private List<Integer> groupIds; //用户组
    private List<Integer> orgIds;  //机构

    private List<Integer> labels;  //标签

    private Boolean isAdmin;

    private String orderName;

    private String orderType;

    private String ipRandStart;

    private String ipRandEnd;

    @ApiModelProperty(value="是否做选项分组 0.不作为作为分组 1.作为为分组")
    private boolean radioStatus;
    /**
     * 第几页
     */
    private Integer pageNumber ;

    /**
     * 每页显示行数
     */
    private Integer pageSize ;


}
