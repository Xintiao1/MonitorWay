package cn.mw.monitor.service.server.param;

import cn.mw.monitor.service.server.api.dto.AssetsBaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2021/5/8 17:32
 * @Version 1.0
 */
@Data
public class QueryNavigationBarParam extends AssetsBaseDTO {
    //详情页导航栏名称
    private String navigationBarName;

    //详情页导航栏id （初始化的id都是0）
    private int navigationBarId;
    //删除字段ids
    private List<Integer> navigationBarIds;

    @ApiModelProperty("是否增(create)，删(delete)，改(update)，查(select)")
    private String  operation;

    @ApiModelProperty("布局flag true为全局布局，false为自定义布局")
    private Boolean flag;

    @ApiModelProperty("布局flag true为全局布局，false为自定义布局")
    private List<Boolean> flagList;

    @ApiModelProperty("自定义布局表中的导航栏id")
    private int customNavigationBarId;

    @ApiModelProperty("自定义布局表中的导航栏id")
    private List<Integer> customNavigationBarIdList;

    @ApiModelProperty("是否为默认布局  页面设置的")
    private Boolean defaultFlag;

    private Integer type; //类型：0新增，1删除;
}
