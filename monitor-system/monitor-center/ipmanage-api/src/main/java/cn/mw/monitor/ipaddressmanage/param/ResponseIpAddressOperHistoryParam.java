package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("ip地址分配历史查询")
public class ResponseIpAddressOperHistoryParam {
    //主键
    @ApiModelProperty(value="分配id")
    private Integer recoveryId;
    @ApiModelProperty(value="回收id")
    private Integer distributtionId;
    @ApiModelProperty(value="分配时间")
    private Date distributtionDate;
    @ApiModelProperty(value="回收时间 ")
    private Date recoveryDate;
    @ApiModelProperty(value="分配时间差")
    private String countTime;
    @ApiModelProperty(value="重复数据 ")
    private ResponseIpAddressOperHistoryParamOB paramOB;
    @ApiModelProperty(value="属性")
    private List<Label> attrParam;
}
