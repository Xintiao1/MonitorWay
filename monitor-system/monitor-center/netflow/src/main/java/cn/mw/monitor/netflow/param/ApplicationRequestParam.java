package cn.mw.monitor.netflow.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className ApplicationRequestParam
 * @description 应用请求数据
 * @date 2022/8/25
 */
@Data
public class ApplicationRequestParam extends BaseParam {

    /**
     * 应用ID
     */
    @ApiModelProperty("应用ID")
    private Integer id = 0;

    /**
     * 端口ID
     */
    @ApiModelProperty("端口ID")
    private Integer portId = 0;

    /**
     * 应用Id集合，用于批量删除
     */
    @ApiModelProperty("应用Id集合，用于批量删除")
    private List<Integer> ids;

    /**
     * 树ID
     */
    @ApiModelProperty("树ID")
    private Integer treeId = 0;

    /**
     * 应用名称
     */
    @ApiModelProperty("应用名称")
    private String applicationName;

    /**
     * 监控状态（0：未监控，1：监控中）
     */
    @ApiModelProperty("监控状态（0：未监控，1：监控中）")
    private Boolean monitorState;

    /**
     * 协议类别（0:全部，1：TCP  ，2：UDP）
     */
    @ApiModelProperty("协议类别（0:全部，1：TCP  ，2：UDP）")
    private Integer protocolType;

    /**
     * IP组源ID（存储IP地址组基础信息表主键ID）
     */
    @ApiModelProperty("IP组源ID（存储IP地址组基础信息表主键ID）")
    private Integer sourceIpId;

    /**
     * IP组目标ID（存储IP地址组基础信息表主键ID）
     */
    @ApiModelProperty("IP组目标ID（存储IP地址组基础信息表主键ID）")
    private Integer destIpId;

    /**
     * 端口内容，多个用,拼接
     */
    @ApiModelProperty("端口内容，多个用,拼接")
    private String portContent;

    /**
     * 关键字
     */
    @ApiModelProperty("关键字")
    private String keyWord;
}


