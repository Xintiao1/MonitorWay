package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("ip地址分配高级查询")
@Accessors(chain = true)
public class RequestIpAddressDistributtionSeniorParam  {

    @ApiModelProperty(value="审核人时间")
    private Date applicantionDate; //用户组

    @ApiModelProperty(value="审核人")
    private Integer applicant; //用户组
    @ApiModelProperty(value="责任人")
    private List<Integer> userId; //责任人
    @ApiModelProperty(value="用户组")
    private List<Integer> groupIds; //用户组
    @ApiModelProperty(value="机构")
    private List<List<Integer>> orgIds = new ArrayList<>();  //机构


    @ApiModelProperty(value="审核人")
    private String applicanttext; //责任人
    @ApiModelProperty(value="用户组")
    private List<String> groupIdsString; //用户组
    @ApiModelProperty(value="机构")
    private List<String> orgtext;  //机构
    @ApiModelProperty(value="oa选项")
    private Integer oa; //用户组
    @ApiModelProperty(value="oa选项test文本")
    private String oatext; //用户组
    @ApiModelProperty(value="oaurl")
    private Integer oaurl; //用户组
    @ApiModelProperty(value="oaurl选项test文本")
    private String oaurltext; //用户组
    @ApiModelProperty(value="属性")
    private List<MwAssetsLabelDTO> attrData;

    @ApiModelProperty(value="属性")
    private List<Label> attrParam;
}
