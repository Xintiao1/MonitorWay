package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author lumingming
 * @createTime 04 16:12
 * @description
 */
@Data
@ApiModel("ip地址管理子表历史表")
public class AddIpaddresStatusHis extends BaseParam {
    //主键
    @ApiModelProperty(value="主键")
    private Integer id;

    //之前使用状态
    @ApiModelProperty(value="之前使用状态")
    private Integer oldUseStatus;

    //使用状态
    @ApiModelProperty(value="使用状态")
    private Integer useStatus;


    //之前使用状态
    @ApiModelProperty(value="之前使用状态")
    private Integer oldIsTem;

    //新的临时状态
    @ApiModelProperty(value="新的临时状态")
    private Integer isTem;

    //ip地址
    @ApiModelProperty(value="ip地址")
    private String ipAddress;


    //资产名称
    @ApiModelProperty(value="资产名称")
    private String oldAssetsName;

    //接入端口
    @ApiModelProperty(value="Mac")
    private String oldMac;

    //接入端口名
    @ApiModelProperty(value="备注")
    private String oldRemarks;


    //资产名称
    @ApiModelProperty(value="资产名称")
    private String assetsName;

    //接入端口
    @ApiModelProperty(value="Mac")
    private String mac;

    //接入端口名
    @ApiModelProperty(value="备注")
    private String remarks;

    @ApiModelProperty(value="创建人")
    private String creator;
    @ApiModelProperty(value="创建时间")
    private Date createDate;
}
