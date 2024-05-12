package cn.mw.monitor.script.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className ScriptAccountEntity
 * @description 脚本管理的账户数据
 * @date 2022/4/24
 */
@Data
@TableName("mw_script_account_manage_table")
public class ScriptAccountEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 账户名称
     */
    @TableField("account")
    private String account;

    /**
     * 账户别名
     */
    @TableField("account_alias")
    private String accountAlias;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * port
     */
    @TableField("port")
    private String port;

    /**
     * 创建人
     */
    @TableField("creator")
    private String creator;

    /**
     * 创建时间
     */
    @TableField("create_date")
    private Date createDate;

    /**
     * 修改人
     */
    @TableField("modifier")
    private String modifier;

    /**
     * 修改时间
     */
    @TableField("modification_date")
    private Date modificationDate;

    /**
     * 描述
     */
    @TableField("account_desc")
    private String accountDesc;

    /**
     * 系统类别（Linux  Windows  mysql）
     */
    @TableField("system_type")
    private String systemType = "Linux";

    /**
     * 父ID，当system_type=mysql的时候，生效
     */
    @TableField("pid")
    private Integer pid;

    /**
     * 是否为删除
     */
    @TableField("delete_flag")
    private Boolean deleteFlag;

    /**
     * 绑定机构列表
     */
    @TableField(exist = false)
    private List<List<Integer>> orgIds;

    /**
     * 绑定用户组列表
     */
    @TableField(exist = false)
    private List<Integer> groupIds;

    /**
     * 绑定责任人列表
     */
    @TableField(exist = false)
    private List<Integer> principal;

    /**
     * 机构ID列表
     */
    @TableField(exist = false)
    private List<Integer> orgIdss;
}
