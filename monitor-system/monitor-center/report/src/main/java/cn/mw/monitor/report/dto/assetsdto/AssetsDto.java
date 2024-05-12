package cn.mw.monitor.report.dto.assetsdto;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/12/29 14:45
 */
@Data
public class AssetsDto {
//    @ExcelProperty(value = {"资产主键"},index = 0)
//    private String id;
    @ExcelProperty(value = {"资产名称"},index = 0)
    private String assetsName;
    @ExcelProperty(value = {"IP地址"},index = 1)
    private String assertIp;
    @ExcelProperty(value = {"资产类型名称"},index = 2)
    private String assetsTypeName;
    @ExcelProperty(value = {"厂商"},index = 3)
    private String manufacturer;
    @ExcelProperty(value = {"规格型号"},index = 4)
    private String specifications;
    @ExcelProperty(value = {"描述"},index = 5)
    private String description;
    @ExcelProperty(value = {"设施分类标识符"},index = 6)//资产子类型
    private String category;
//    @ExcelProperty(value = {"投产日期"},index =7)
//    private String useDate;
    @ExcelProperty(value = {"在用状态"},index = 7)
    private String useState;
    @ExcelProperty(value = {"机构"},index = 8)
    private String orgName;
    @ExcelProperty(value = {"更新日期"},index = 9)
    private String modificationDate;
//    @ExcelProperty(value = {"资产编码"},index = 11)
//    private String assetsCode;
//    @ExcelProperty(value = {"品牌属地"},index = 12)
//    private String brandLand;
//    @ExcelProperty(value = {"操作系统版本信息"},index = 130)
//    private String systemVersion;
//    @ExcelProperty(value = {"设备高度"},index = 13)
//    private String deviceHeight;
//    @ExcelProperty(value = {"IPV6支持能力"},index = 14)
//    private String supportIpv6;
//    @ExcelProperty(value = {"影响系统"},index = 15)
//    private String influenceSystem;
//    @ExcelProperty(value = {"部署属性"},index = 16)
//    private String deployArea;
//    @ExcelProperty(value = {"运维属性"},index = 17)
//    private String operationDepartment;
//    @ExcelProperty(value = {"安装位置-机柜"},index = 18)
//    private String belongCabinet;
//    @ExcelProperty(value = {"安装位置-槽位号"},index = 19)
//    private String slotNo;


}
