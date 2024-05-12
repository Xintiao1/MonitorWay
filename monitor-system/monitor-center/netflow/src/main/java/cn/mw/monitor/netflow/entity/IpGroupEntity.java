package cn.mw.monitor.netflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className IpGroupEntity
 * @description IP地址组信息
 * @date 2022/8/24
 */
@Data
@TableName(value = "mw_netflow_ip_group_info")
public class IpGroupEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * IP地址组名称
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 添加方式（1：NFA，即本地添加，2：IPAM，IP地址导入）
     */
    @TableField("add_type")
    private Integer addType;

    /**
     * 是否可见（0：不可见，1：可见）
     */
    @TableField("visible_flag")
    private Boolean visibleFlag;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 是否删除（0：未删除，1；已删除）
     */
    @TableField("delete_flag")
    private Boolean deleteFlag;

    /**
     * NFA请求参数--IP列表
     */
    @TableField(exist = false)
    private List<IpGroupNFAExpandEntity> ipList;

    /**
     * NFA请求参数--IP地址段
     */
    @TableField(exist = false)
    private List<IpGroupNFAExpandEntity> ipPhase;

    /**
     * NFA请求参数--ip地址清单
     */
    @TableField(exist = false)
    private List<IpGroupNFAExpandEntity> ipRange;

    /**
     * IP地址组列表
     */
    @TableField(exist = false)
    private List<String> ipGroupList;
}
