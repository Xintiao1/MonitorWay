package cn.mw.monitor.script.param;

import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className ScriptManageParam
 * @description 脚本查询参数
 * @date 2022/4/8
 */
@Data
public class ScriptManageParam extends DataPermissionParam {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 执行ID
     */
    private Integer execId;

    /**
     * id列表
     */
    private List<Integer> ids;

    /**
     * 脚本名称
     */
    private String scriptName;

    /**
     * 脚本所在节点ID
     */
    private Integer scriptTreeId;

    /**
     * 脚本类型
     */
    private String scriptType;

    /**
     * 账户ID
     */
    private Integer accountId;

    /**
     * 最大超时时间
     */
    private Integer maxOverTime;

    /**
     * 脚本内容
     */
    private String scriptContent;

    /**
     * 脚本描述
     */
    private String scriptDesc;

    /**
     * 下发指令资产列表
     */
    private List<String> assetsIdList;

    /**
     * 删除标志位
     */
    private Boolean deleteFlag;

    /**
     * 脚本参数
     */
    private String scriptParam;

    /**
     * 是否为敏感参数
     */
    private Boolean isSensitive;

    /**
     * 模糊查询内容
     */
    private String searchAll;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 资产IP
     */
    private String assetsIP;

    /**
     * 执行状态，0：初始化  1：执行中   2：执行结束   9：执行错误
     */
    private Integer execStatus;

    /**
     * 创建开始时间
     */
    @ApiModelProperty("创建开始时间")
    private Date createDateStart;

    /**
     * 创建结束时间
     */
    @ApiModelProperty("创建结束时间")
    private Date createDateEnd;

    /**
     * 全局账户ID
     */
    @ApiModelProperty("全局账户ID")
    private Integer defaultAccountId;

    /**
     * 是否自选字段
     */
    @ApiModelProperty("是否自选字段")
    private Integer isVarible;
    /**
     * 步骤名称
     */
    @ApiModelProperty("步骤名称")
    private String stepName;

    /**
     * 忽略错误
     */
    @ApiModelProperty("忽略错误")
    private Boolean ignoreError = false;

    /**
     * 下发指令资产列表
     */
    @ApiModelProperty("待下发资产数据")
    private List<TransAssets> transAssetsList;

    /**
     * 数据库名称（当脚本类型为SQL）
     */
    @ApiModelProperty("数据库名称")
    private String sqlDatabase;

    /**
     * mysql前置标识（当脚本类型为SQL）
     */
    @ApiModelProperty("mysql前置标识")
    private String sqlText;

    /**
     * 目的地址（当脚本类型为SQL）
     */
    @ApiModelProperty("目的地址")
    private String orderAddress;

    /**
     * 获取数据类别
     *
     * @return
     */
    @Override
    public DataType getBaseDataType() {
        return DataType.AUTO_MANAGE;
    }

    /**
     * 获取绑定的数据ID
     *
     * @return
     */
    @Override
    public String getBaseTypeId() {
        return id + "";
    }
}
