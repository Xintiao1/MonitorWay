package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.visualized.param.MwVisualizedIndexQueryParam;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MwVisualizedViewDto
 * @Author gengjb
 * @Date 2022/4/21 15:34
 * @Version 1.0
 **/
@Data
public class MwVisualizedViewDto extends BaseParam {

    /**
     * 主键
     */
    @ApiModelProperty("可视化ID")
    private Integer id;

    /**
     * 分类ID
     */
    @ApiModelProperty("可视化分类ID")
    private Integer classifyId;

    /**
     * 可视化视图名称
     */
    @ApiModelProperty("可视化名称")
    private String visualizedViewName;

    //创建人
    @ApiModelProperty("创建人")
    private String creator;

    //创建时间
    @ApiModelProperty("创建时间")
    private Date createDate;

    //修改人
    @ApiModelProperty("修改人")
    private String modifier;

    //修改时间
    @ApiModelProperty("修改时间")
    private Date modificationDate;

    /**
     * 视图ID集合
     */
    private List<Integer> ids;

    //前端视图数据
    private Map visualizedDatas;

    private String visualizedDatasStr;

    //视图的过滤规则
    List<MwRuleSelectParam> mwRuleSelectListParam;


    private List<MwVisualizedIndexQueryParam> indexQueryParams;

    //视图图片Base64
    private String visualizedImage;

    @ApiModelProperty("时间分类类型")
    private Integer type;

    @ApiModelProperty("时间取值类型")
    private Integer dateType;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    private Integer uuid;

    @ApiModelProperty("背景图片")
    private String backGroundImage;

    @ApiModelProperty("是否导出  0：非导出 1：导出")
    private int isExport;

    @ApiModelProperty("是否开启动画效果")
    private boolean animation;

    @ApiModelProperty("是否开启自动更新")
    private boolean setTime;

    @ApiModelProperty("是否模板视图")
    private Integer isTemplate;

    private List<MwVisualizedImageDto> imageDtos;

}
