package cn.mw.monitor.ipaddressmanage.param;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author bkc
 * @date 2020/7/17
 */
@Data
public class MwIpAddressManageListTable {
    //主键
    @ExcelIgnore
    private Integer id;

    //ip地址管理表主键
    @ExcelIgnore
    private Integer linkId;

    //ip地址
    @ExcelProperty(value="IP地址")
    private String ipAddress;

    //ip状态
    @ExcelProperty(value="使用状态")
    private String ipState;

    //备注
    @ExcelProperty(value="备注")
    private String remarks;

    @ExcelProperty(value="创建人")
    private String creator;
    @ExcelProperty(value="创建时间")
    private Date createDate;
    @ExcelProperty(value="修改人")
    private String modifier;
    @ExcelProperty(value="修改时间")
    private Date modificationDate;

    //在线状态  在线  离线
    @ExcelProperty(value="在线状态")
    private String online;

    //mAC地址
    @ExcelProperty(value="MAC地址")
    private String mac;

    //厂商
    @ExcelProperty(value="厂商")
    private String vendor;

    //接入设备
    @ExcelProperty(value="接入设备")
    private String accessEquip;

    //接入端口
//    @ExcelProperty(value="接入端口")
//    private String accessPort;

    //接入端口
    @ExcelProperty(value="接入端口名称")
    private String accessPortName;

    @ExcelProperty(value="资产名称")
    private String assetsName;

    @ExcelProperty(value="资产类型")
    private String assetsType;

    //最后一次离线时间
    @ExcelProperty(value="最后一次离线时间")
    private Date lastDate;

    //同步数据时间间隔
    @ExcelIgnore
    private Integer interval;

    //更新时间
    @ExcelProperty(value="更新时间")
    private Date updateDate;






}
