package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.ipaddressmanage.dto.LinkLabel;
import cn.mw.monitor.ipaddressmanage.dto.MwIpAddresses1DTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("ip地址管理主表")
@Slf4j
public class AddUpdateIpAddressManageParam implements Cloneable{
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

    @ApiModelProperty(value="IP地址段")
    private List<MwIpAddresses1DTO> ipsubnets;

    @ApiModelProperty(value = "机构")
    private List<List<Integer>> orgIds;
    @ApiModelProperty(value = "用户组")
    private List<Integer> groupIds;
    @ApiModelProperty(value = "责任人")
    private List<Integer> principal;

    //添加标签
    private List<LinkLabel>  labels;
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

    /*
     * 地域id
     * */
    private Integer signId ;

    //纬度
    @ApiModelProperty(value="是否做选项分组 0.作为分组 1.不作为分组")
    private Integer radioStatus;

    @ApiModelProperty(value="是否创建地址清单 0.作为分组 1.不作为分组")
    private Integer createIp=0;

    //是否为ipv4
    private Integer iPv4;

    private Integer IPAllCreate = 0;
    @Override
    public Object clone()  {
        AddUpdateIpAddressManageParam aP = null;
        try {
            aP = (AddUpdateIpAddressManageParam)super.clone();
        }catch (CloneNotSupportedException e){
            log.error("错误返回 :{}",e);
        }
        return aP;
    }
}
