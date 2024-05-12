package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/14
 */
@Data
@ApiModel("属性")
public class Process {
    //主键
    @ApiModelProperty(value="标签名称")
    Integer applicant;


    @ApiModelProperty(value="下拉的对应对应下拉id")
    Date applicantDate;

}
