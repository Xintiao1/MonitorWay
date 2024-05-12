package cn.mw.monitor.ipaddressmanage.param;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author lumingming
 * @createTime 20220915 14:33
 * @description 15
 */
@Data
public class seachLabelList extends BaseParam {
    List<Label> labels;
    @ApiModelProperty(value="0.昨天 1.今天 2.本周 3.月度")
    Integer type;

    List<String> applicant;

    @ApiModelProperty(value="获取所在分布")
    String applicantId;

    @ApiModelProperty(value="回收的id集合")
    List<Integer> ids;

    Integer id;
    @ApiModelProperty(value="变更的主Id")
    Integer distriId;

    @ApiModelProperty(value="ipid")
    Integer iplist_id;
    @ApiModelProperty(value="ip类")
    Integer iplist_type;

    @ApiModelProperty(value="ip地址")
    String ip_address;

    @ApiModelProperty(value="ip地址")
    String descript;


    List<seachLabelList> childrens;
}
