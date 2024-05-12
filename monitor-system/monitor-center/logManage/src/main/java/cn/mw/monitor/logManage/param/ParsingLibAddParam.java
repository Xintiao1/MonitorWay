package cn.mw.monitor.logManage.param;

import cn.mw.monitor.logManage.dto.ParsingLibDataAddDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@ApiModel(value = "解析库添加数据")
@Data
public class ParsingLibAddParam {

    @ApiModelProperty(value = "解析库id,修改数据传")
    private Integer id;

    @ApiModelProperty(value = "解析库名称")
    private String parsingName;

    @ApiModelProperty(value = "解析库类型")
    private String parsingType;

    @ApiModelProperty(value = "解析库描述")
    private String parsingDesc;

    @ApiModelProperty(value = "表名，最终调用此接口的时候传递")
    private String tableName;

    @ApiModelProperty(value = "最终添加的结果，最终调用此接口的时候传递")
    private List<ParsingLibDataAddDTO> resultDTOList;


    public void validation() {
        if (StringUtils.isBlank(parsingName)) {
            throw new IllegalArgumentException("名称为空");
        }
        if (StringUtils.isBlank(parsingType)) {
            throw new IllegalArgumentException("类型为空");
        }
    }

}
