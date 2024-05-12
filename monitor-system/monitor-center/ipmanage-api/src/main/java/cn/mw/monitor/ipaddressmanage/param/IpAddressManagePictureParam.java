package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.ipaddressmanage.dto.GroupDTO;
import cn.mw.monitor.ipaddressmanage.dto.MwIpAddresses1DTO;
import cn.mw.monitor.ipaddressmanage.dto.OrgDTO;
import cn.mw.monitor.ipaddressmanage.dto.UserDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/17
 */
@Data
public class IpAddressManagePictureParam  {
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

    private List<HashMap<String,Integer>> picture1 = new ArrayList<HashMap<String,Integer>>();
    /*//使用ip数量
    private Integer useCount;
    //未使用ip数量
    private Integer notuseCount;
    //预留ip数量
    private Integer reservedCount;*/

    private List<HashMap<String,Integer>> picture2 = new ArrayList<HashMap<String,Integer>>();
   /* //在线ip数量
    private Integer online;
    //离线ip数量
    private Integer offline;*/
   private List<HashMap<String,Integer>> picture3 = new ArrayList<HashMap<String,Integer>>();

    private List<HashMap<String,Integer>> picture4 = new ArrayList<HashMap<String,Integer>>();
    private String creator;
    private Date createDate;
    private String modifier;
    private Date modificationDate;

    //IP地址段
    private String ipAddresses;

    @ApiModelProperty(value="IP地址段")
    private List<MwIpAddresses1DTO> ipsubnets;

    private List<UserDTO> principal = new ArrayList<>();

    private List<OrgDTO> orgIds = new ArrayList<>();

    private List<GroupDTO> groupIds = new ArrayList<>();

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

}
