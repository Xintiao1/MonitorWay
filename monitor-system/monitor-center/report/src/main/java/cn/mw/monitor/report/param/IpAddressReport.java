package cn.mw.monitor.report.param;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Set;

/**
 * @author lumingming
 * @createTime 202111/0505 15:09
 * @description
 */
@Data
public class IpAddressReport {

    @ApiModelProperty("站点名称")
    @ExcelProperty(value = {"站点名称"},index = 1)
    private String label;
    @ApiModelProperty("ip地址")
    @ExcelProperty(value = {"ip地址"},index = 2)
    private String ipAddress;
    @ApiModelProperty("状态")
    private Integer status;
    @ApiModelProperty("type 0.纳管 1.在线")
    private Integer type;
    @ApiModelProperty("时间")
    @ExcelProperty(value = {"时间"},index = 6)
    private Date updateDate;
    @ApiModelProperty("状态")
    private String stringStatus;

    private Integer linkId;

    private int colorStatus;

    //使用状态
    private Integer useStatus;

    //在线状态
    private Integer onLineType;

    //纳管状态
    @ExcelProperty(value = {"纳管状态"},index = 3)
    private String nanoTubeStatus;

    private int nanoTubeColor;

    @ExcelProperty(value = {"使用状态"},index = 5)
    private String strUseStatus;

    @ExcelProperty(value = {"在线状态"},index = 4)
    private String strOnLineType;

    private int id;

    //时间区域
    @ExcelProperty(value = {"时间区域"},index = 0)
    private String time;
}
