package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @ClassName MwVisualizedModuleRelationDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/12/5 11:42
 * @Version 1.0
 **/
@Data
public class MwVisualizedModuleRelationDto {

    private Integer id;

    @ApiModelProperty("可视化ID")
    private Integer visualizedId;

    @ApiModelProperty("生成页面名称")
    private String generateName;

    @ApiModelProperty("生成页面路径")
    private String generateUrl;

    @ApiModelProperty("生成页面ID")
    private Integer generateId;

    @ApiModelProperty("模块ID")
    private Integer moduleId;


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

    private List<Integer> ids;
}
