package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("ip地址分配历史重复查询")
public class ResponseIpAddressOperHistoryParamOB {
    //主键
    @ApiModelProperty(value="分配id")
    private Integer recoveryId;
    @ApiModelProperty(value="回收id")
    private Integer distributtionId;
    @ApiModelProperty(value="分配人")
    private String distributtioner;
    @ApiModelProperty(value="回收人")
    private String recoveryer;
    @ApiModelProperty(value="负责人")
    private String director;
    @ApiModelProperty(value="申请人")
    private String applicant;
    @ApiModelProperty(value="申请申请时间")
    private Date applicantDate;
    @ApiModelProperty(value="分配时间")
    private Date distributtionDate;
    @ApiModelProperty(value="回收时间 ")
    private Date recoveryDate;
    @ApiModelProperty(value="Oa流程url")
    private Integer labelLinkId;

    @ApiModelProperty(value="回收描述")
    private String disDesc;
    @ApiModelProperty(value="Oa流程")
    private String Oa;
    @ApiModelProperty(value="Oa流程url")
    private String OaUrl;
}
