package cn.huaxing.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gengjb
 * @description 华兴可视化数据源DTO
 * @date 2023/8/28 11:29
 */
@Data
@ApiModel("华兴可视化数据源DTO")
public class HuaxingVisualizedDataSourceDto {

    @ApiModelProperty("主键ID")
    private String id;

    @ApiModelProperty("登录名")
    private String userName;

    @ApiModelProperty("登陆密码")
    private String passWord;

    @ApiModelProperty("连接信息")
    private String url;

    @ApiModelProperty("数据库驱动")
    private String driver;

    @ApiModelProperty("需要查询该数据的SQL")
    List<HuaxingVisualizedDataSourceSqlDto> dataQuerySqls;
}
