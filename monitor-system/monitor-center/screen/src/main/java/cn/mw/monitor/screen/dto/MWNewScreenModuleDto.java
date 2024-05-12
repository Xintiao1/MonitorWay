package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @ClassName MWNewScreenModuleDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/12/3 10:46
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenModuleDto {
    //ID
    private String modelDataId;
    //模块ID
    private int bulkId;
    //初始化模块名称
    private String bulkName;
    //用户ID
    private int userId;
    //模块名称
    private String name;
    //创建时间
    private Date createDate;
    //模块路劲
    private String moduleUrl;
    private Boolean displayTime;
    //查询top几数量
    private Integer count;
    //查询模块时间
    private Integer dateType;
    //查询模块自定义开始时间
    private String startTime;
    //查询模块自定义结束时间
    private String endTime;

}
