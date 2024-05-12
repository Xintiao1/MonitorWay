package cn.mw.monitor.service.server.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xhy
 * @date 2020/4/29 22:59
 */
@Data
public class ApplicationParam extends AssetsIdsPageInfoParam {

    @ApiModelProperty("应用集名称")
    private String applicationName;//应用集名称 ALL表示全部 其他传应用集名称

    @ApiModelProperty("表示使用查询的分区监控项名称")
    private String nameType;

    @ApiModelProperty("是否含有描述信息")
    private boolean hasDescription;

    @ApiModelProperty("应用集下的监控项数量")
    private int count;

    @ApiModelProperty("应用集下的所有监控项id")
    private List<String> items = new ArrayList<>();

    @ApiModelProperty("排序的属性名称")
    private String sortField;
    @ApiModelProperty("0为升序；1为倒序")
    private Integer sortMode;

    //状态 NORMAL：正常 其他：不支持
    private String status;

    //模糊查询值
    private String queryVal;
}
