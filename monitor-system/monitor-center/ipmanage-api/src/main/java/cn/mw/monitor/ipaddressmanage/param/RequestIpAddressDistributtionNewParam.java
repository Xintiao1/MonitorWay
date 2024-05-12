package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("新ip地址分配新建")
public class RequestIpAddressDistributtionNewParam {
    //主键
    @ApiModelProperty(value="大标签")
    List<ResponIpDistributtionNewParentParam> responIpDistributtionParams;

    @ApiModelProperty(value="高级配置")
    RequestIpAddressDistributtionSeniorParam requestIpAddressDistributtionSeniorParam;

    @ApiModelProperty(value="ip关系ip")
    String bangDistri;

    @ApiModelProperty(value="是否为修改 true修改 false 不修改")
    boolean submitStatus = false;

    @ApiModelProperty(value="回收变更前查询")
    IsInput isInput ;

    @ApiModelProperty(value="内部节点数量")
    Integer num ;
    //上一节点
    @ApiModelProperty(value="首节点")
    Integer parentId;


    @ApiModelProperty(value="获取区域Id")
    Integer signId;
}
