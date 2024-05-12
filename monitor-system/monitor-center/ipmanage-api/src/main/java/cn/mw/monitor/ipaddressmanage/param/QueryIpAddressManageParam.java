package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.ipaddressmanage.dto.MwIpAddresses1DTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/17
 */
@Data
@ApiModel("ip地址管理主表")
public class QueryIpAddressManageParam  {
    //主键
    private Integer id;

    //父级id
    private Integer parentId=0;

    //名称
    private String label;

    //类型: 包或地址段 grouping  iPaddresses
    private String type;

    private boolean leaf;

    //子网掩码
    private String mask;

    //描述
    private String descri;

    private Boolean isIPv4;

    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    //IP地址段
    private List<String> ip;
    //IP地址段
    private List<String> ipSix;
    //IP地址段
    private String searchIp;
    //IP地址段
    private String ipAddresses;
    @ApiModelProperty(value="IP地址段")
    private List<MwIpAddresses1DTO> ipsubnets;

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


   /* @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;

    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;*/

    private String perm;
    private Integer userId; //责任人
    private List<Integer> groupIds; //用户组
    private List<Integer> orgIds;  //机构

    private List<Integer> labels;  //标签

    private Boolean isAdmin;


    /**
     * 第几页
     */
    private Integer pageNumber ;

    private Boolean pop=false;


    private Boolean sureUp=true;
    /**
     * 每页显示行数
     */
    private Integer pageSize ;

    /*
    * 地域id
    * */
    private Integer signId ;
}
