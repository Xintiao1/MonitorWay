package cn.mw.monitor.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author xhy
 * @date 2021/1/6 14:58
 */
@Data
@ApiModel
public class ExcelExportParam {
    @ApiModelProperty("导出的数据有那些字段")
    private Set<String> fields;

    @ApiModelProperty("导出文件名")
    private String name;

    @ApiModelProperty("导出的数据")
    private List list;

}
