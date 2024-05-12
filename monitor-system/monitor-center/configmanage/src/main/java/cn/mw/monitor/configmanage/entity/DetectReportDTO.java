package cn.mw.monitor.configmanage.entity;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author gui.quanwang
 * @className DetectReportDTO
 * @description 配置管理——合约检测——检测报告
 * @date 2021/12/17
 */
@Data
@ApiModel("配置管理--检测报告管理")
public class DetectReportDTO extends DataPermissionParam {

    /**
     * 批量删除时的主键ID
     */
    private List<Integer> ids;

    /**
     * 报告ID
     */
    @ApiModelProperty(value = "主键")
    private Integer id;

    /**
     * 报告的UUID
     */
    private String reportUUID;

    /**
     * 报告名称
     */
    @ApiModelProperty(value = "检测报告名称")
    private String reportName;

    /**
     * 文件夹地址ID
     */
    @ApiModelProperty(value = "报告对应的文件夹地址ID")
    private Integer reportTreeGroup;

    /**
     * 报告描述
     */
    @ApiModelProperty(value = "报告描述")
    private String reportDesc;

    /**
     * 检测状态
     */
    @ApiModelProperty(value = "false：失效  true：生效")
    private Boolean reportState;

    /**
     * 策略列表
     */
    private List<String> policyList;

    /**
     * 创建用户
     */
    private String creator;

    /**
     * 更新用户
     */
    private String updater;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 更新时间
     */
    private Date updateDate;

    /**
     * 全局模糊搜索字段
     */
    @ApiModelProperty(value = "全局模糊搜索字段")
    private String searchAll;

    /**
     * 查询集合（内部使用。当用户为非系统管理员时，值生效）
     */
    private String findInSet;

    /**
     * 当前用户是否为系统管理员（内部使用）
     */
    private boolean systemUser;

    /**
     * 获取数据类别
     *
     * @return
     */
    @Override
    public DataType getBaseDataType() {
        return DataType.DETECT_REPORT_MANAGE;
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
