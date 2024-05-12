package cn.mw.monitor.script.entity;

import cn.mw.monitor.script.param.FileTransParam;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author gui.quanwang
 * @className ScriptExecEntity
 * @description 脚本执行记录信息
 * @date 2022/4/12
 */
@Data
@TableName("mw_script_exe_history_log")
public class ScriptExecEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 执行ID
     */
    @TableField("exec_id")
    private Integer execId;

    /**
     * 脚本ID
     */
    @TableField("script_id")
    private Integer scriptId;

    /**
     * 脚本名称
     */
    @TableField("script_name")
    private String scriptName;

    /**
     * 资产ID
     */
    @TableField("assets_id")
    private String assetsId;

    /**
     * 资产IP
     */
    @TableField("assets_ip")
    private String assetsIP;

    /**
     * 资产端口
     */
    @TableField("assets_port")
    private String assetsPort;

    /**
     * 账户ID
     */
    @TableField("account_id")
    private Integer accountId;

    /**
     * 耗时（毫秒）
     */
    @TableField("cost_time")
    private Integer costTime;

    /**
     * 最大超时时间（毫秒）
     */
    @TableField("over_time")
    private Integer maxOverTime;

    /**
     * 返回内容
     */
    @TableField("return_content")
    private String returnContent;

    /**
     * 执行状态，0：初始化  1：执行中   2：执行结束   9：执行错误
     */
    @TableField("exec_status")
    private Integer execStatus;

    /**
     * 脚本参数
     */
    @TableField("script_param")
    private String scriptParam;

    /**
     * 是否为敏感参数
     */
    @TableField("is_sensitive")
    private Boolean isSensitive;

    /**
     * 1：页面执行
     */
    @TableField("exec_type")
    private Integer execType;

    /**
     * 1:脚本执行  2：文件分发  3:作业执行
     */
    @TableField("mission_type")
    private Integer missionType;

    /**
     * 创建人
     */
    @TableField("creator")
    private String creator;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 是否删除
     */
    @TableField("delete_flag")
    private Boolean deleteFlag;

    /**
     * 脚本内容
     */
    @TableField("script_content")
    private String scriptContent;

    /**
     * 忽略错误
     */
    @TableField("ignore_error")
    private Boolean ignoreError = false;

    /**
     * 脚本内容
     */
    @TableField("trans_file_param")
    private String transFileParam;

    /**
     * 是否是作业下发
     */
    @TableField("is_homework")
    private Boolean homeworkFlag = false;

    /**
     * 作业排序,当homeworkFlag=true时生效
     */
    @TableField("homework_sort")
    private Integer homeworkSort;

    /**
     * 作业关联ID,和作业关联表的ID
     */
    @TableField("homework_version_id")
    private Integer homeworkVersionId;

    /**
     * 作业步骤名称
     */
    @TableField(exist = false)
    private String stepName;

    /**
     * 脚本类型
     */
    @TableField("script_type")
    private String scriptType;

    /**
     * 全局账户ID
     */
    @TableField("default_account_id")
    private Integer defaultAccountId;

    /**
     * 文件下发数据
     */
    @TableField(exist = false)
    private FileTransParam fileTransParam;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private Date endTime;


    /**
     * 数据库名称（当脚本类型为SQL）
     */
    @TableField("sql_database")
    private String sqlDatabase;

    /**
     * mysql前置标识（当脚本类型为SQL）
     */
    @TableField("sql_text")
    private String sqlText;

    /**
     * 目的地址（当脚本类型为SQL）
     */
    @TableField("sql_order_address")
    private String orderAddress;

    /**
     * 目的地址（当脚本类型为SQL）
     */
    @TableField("is_varible")
    private Integer isVarible;

    @TableField("homework_id")
    private Integer homeworkId;
}
