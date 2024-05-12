package cn.mw.monitor.netflow.param;

import cn.mw.monitor.bean.BaseParam;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className IpGroupParam
 * @description IP地址组请求参数
 * @date 2022/8/24
 */
@Data
public class IpGroupRequestParam extends BaseParam {

    /**
     * 添加方式（1：NFA，即本地添加，2：IPAM，IP地址导入，9：全部）
     */
    @ApiModelProperty("添加方式（1：NFA，即本地添加，2：IPAM，IP地址导入，9：全部）")
    private Integer addType;

    /**
     * 是否可见（0：不可见，1：可见，9:全部）
     */
    @ApiModelProperty("是否可见（0：不可见，1：可见，9:全部）")
    private Integer visibleType;

    /**
     * 是否可见（0：不可见，1：可见）
     */
    @ApiModelProperty("是否可见（0：不可见，1：可见）")
    private Boolean visibleFlag;

    /**
     * 组名称
     */
    @ApiModelProperty("组名称")
    private String groupName;

    /**
     * ID集合，用于批量删除
     */
    @ApiModelProperty("ID集合，用于批量删除")
    private List<Integer> ids;

    /**
     * IP地址分组ID
     */
    @ApiModelProperty("IP地址分组ID")
    private Integer id;

    /**
     * IPAM导入参数
     */
    @ApiModelProperty("IPAM导入参数")
    private List<IpGroupIPAMRequestParam> ipamList;

    /**
     * NFA请求参数
     */
    @ApiModelProperty("NFA请求参数----IP列表")
    private List<IpGroupNFARequestParam> ipList;

    /**
     * NFA请求参数
     */
    @ApiModelProperty("NFA请求参数----IP地址段")
    private List<IpGroupNFARequestParam> ipPhase;

    /**
     * NFA请求参数
     */
    @ApiModelProperty("NFA请求参数----ip地址清单")
    private List<IpGroupNFARequestParam> ipRange;
}
