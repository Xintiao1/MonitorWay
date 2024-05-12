package cn.mw.monitor.netflow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className NetflowTreeEntity
 * @description 流量监控树结构数据
 * @date 2022/8/3
 */
@Data
@TableName(value = "mw_netflow_assets_tree_info")
public class NetflowTreeEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 节点名称
     */
    @TableField("item_name")
    private String itemName;

    /**
     * 节点类别（0：资产 1：接口）
     */
    @TableField("item_type")
    private Integer itemType;

    /**
     * 资产IP，类别为资产有效
     */
    @TableField("item_ip")
    private String itemIp;

    /**
     * 资产ID，类别为资产有效
     */
    @TableField("item_assets_id")
    private String itemAssetsId;

    /**
     * 接口索引，类别为接口有效
     */
    @TableField("item_index")
    private Integer itemIndex;

    /**
     * 节点父ID
     */
    @TableField("item_pid")
    private Integer itemPid;

    /**
     * 节点开启状态（0：关闭  1：开启）
     */
    @TableField("item_state")
    private Integer itemState;

    /**
     * 节点类别（0：资产 1：接口）
     */
    @TableField("delete_flag")
    private Boolean deleteFlag;

    /**
     * 下载速度速率值
     */
    @TableField(exist = false)
    private double inRateValue;

    /**
     * 下载速率单位
     */
    @TableField(exist = false)
    private String inRateUnit;

    /**
     * 上传速率值
     */
    @TableField(exist = false)
    private double outRateValue;

    /**
     * 上传速率单位
     */
    @TableField(exist = false)
    private String outRateUnit;

    /**
     * 总数据（入）
     */
    @TableField(exist = false)
    private long inSumData;

    /**
     * 总数据（出）
     */
    @TableField(exist = false)
    private long outSumData;

    /**
     * 子节点列表
     */
    @TableField(exist = false)
    private List<NetflowTreeEntity> childList;
}
