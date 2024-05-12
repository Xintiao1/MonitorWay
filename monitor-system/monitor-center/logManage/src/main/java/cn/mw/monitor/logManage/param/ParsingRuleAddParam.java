package cn.mw.monitor.logManage.param;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@ApiModel(value = "添加或更新解析规则")
public class ParsingRuleAddParam {

    @ApiModelProperty(value = "解析规则id,修改数据传")
    private Integer id;

    @ApiModelProperty(value = "解析规则名")
    private String ruleName;

    @ApiModelProperty(value = "源名称")
    private String sourcesName;

    @ApiModelProperty(value = "desc")
    private String desc;

    @ApiModelProperty(value = "解析规则类型")
    private String type;

    @ApiModelProperty(value = "字段信息")
    private JSONObject fieldInfo;

    @ApiModelProperty(value = "通道名")
    private List<String> vectorChannelNameList;

    @ApiModelProperty(value = "解析库名集合")
    private List<String> parsingLibNameList;

    @ApiModelProperty(value = "存储或者转发名，除了clickhouse都是转发")
    private List<String> storeOrForwardNameList;

    public void validation() {
        if (StringUtils.isBlank(ruleName)) {
            throw new IllegalArgumentException("名称为空");
        }
        if (StringUtils.isBlank(type)) {
            throw new IllegalArgumentException("类型为空");
        }
        if (ObjectUtils.isEmpty(fieldInfo)) {
            throw new IllegalArgumentException("字段信息为空");
        }
        if (CollectionUtils.isEmpty(parsingLibNameList)) {
            throw new IllegalArgumentException("解析库信息为空");
        }
        if (CollectionUtils.isEmpty(storeOrForwardNameList)) {
            throw new IllegalArgumentException("转发或存储信息为空");
        }
    }
}
