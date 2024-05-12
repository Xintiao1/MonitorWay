package cn.mw.monitor.script.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author gui.quanwang
 * @className ScriptManageEntity
 * @description 脚本管理实体类
 * @date 2022/4/8
 */
@Data
@TableName("mw_script_manage_table")
public class ScriptManageEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id",type = IdType.INPUT)
    private Integer id;

    /**
     * 脚本名称
     */
    @TableField("script_name")
    private String scriptName;

    /**
     * 脚本所在节点ID
     */
    @TableField("script_tree_id")
    private Integer scriptTreeId;

    /**
     * 脚本类别（1：shell  2：cmd）
     */
    @TableField("script_type")
    private String scriptType;

    /**
     * 脚本内容
     */
    @TableField("script_content")
    private String scriptContent;

    /**
     * 脚本版本
     */
    @TableField("script_version")
    private String scriptVersion;

    /**
     * 脚本描述
     */
    @TableField("script_desc")
    private String scriptDesc;

    /**
     * 账户ID
     */
    @TableField("account_id")
    private Integer accountId;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 创建人
     */
    @TableField("creator")
    private String creator;

    /**
     * 更新人
     */
    @TableField("updater")
    private String updater;

    /**
     * 删除标志位
     */
    @TableField("delete_flag")
    private Boolean deleteFlag;

    @Override
    public String toString() {
        return "ScriptManageEntity{" +
                "id=" + id +
                ", scriptName='" + scriptName + '\'' +
                ", scriptTreeId=" + scriptTreeId +
                ", scriptType=" + scriptType +
                ", scriptContent='" + scriptContent + '\'' +
                ", scriptVersion='" + scriptVersion + '\'' +
                ", accountId=" + accountId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", creator='" + creator + '\'' +
                ", updater='" + updater + '\'' +
                ", deleteFlag=" + deleteFlag +
                '}';
    }
}
