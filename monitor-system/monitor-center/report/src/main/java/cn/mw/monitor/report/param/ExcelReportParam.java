package cn.mw.monitor.report.param;

import cn.mw.monitor.report.dto.*;
import cn.mw.monitor.report.dto.assetsdto.AssetsDto;
import cn.mw.monitor.report.dto.linkdto.InterfaceReportDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.poi.ss.formula.functions.T;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author xhy
 * @date 2021/1/4 10:49
 */
@Data
public class ExcelReportParam {

    @ApiModelProperty("报表id")
    private Integer reportId;
    @ApiModelProperty("要导出的字段")
    private Set<String> fields;
    @ApiModelProperty("导出的文件名")
    private String name;
//    @ApiModelProperty("要导出的数据")
//    private List list;

    private List<CpuAndMemoryDto> cpulist;
    private List<TrendDiskDto> disklist;
    private List<TrendNetDto> netlist;
    private List<AssetsDto> assetsDtoList;

    private List<CpuAndMemorySyDto> cpulistsy;
    private List<TrendDiskSyDto> disklistsy;
    private List<TrendNetSyDto> netlistsy;

    private String syexport;
    @ApiModelProperty("选择的日期")
    private List<String> chooseTime;
    @ApiModelProperty("是否高级查询 true 是")
    private Boolean seniorchecked;
    @ApiModelProperty("资产类型ID")
    private Integer assetsTypeId;
    //颗粒度
    private String particle;
    //资产类型具体ip
    private String assertIp;
    //报表分类查询默认0
    private Integer dayType;

    //资产名称
    private String assetsName;
    //资产类型名称
    private String assetsTypeName;
    //更新日期
    private Date modificationDateStart;
    //更新日期
    private Date modificationDateEnd;
    //厂商
    private String manufacturer;
    //规格型号
    private String specifications;
    //设施分类标识符
    private String assetsTypeSubName;
    //在用状态
    private Boolean monitorFlag;
}
