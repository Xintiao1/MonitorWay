package cn.mw.monitor.model.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author guiquanwnag
 * @datetime 2023/6/30
 * @Description MAC视图查看类
 */
@Data
public class ModelTableViewMAC implements Serializable {

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * MAC地址
     */
    @ExcelProperty(value = "MAC地址", index = 0)
    private String macAddress;

    /**
     * 虚拟局域网标识符
     */
    private String vlanId;

    /**
     * 接口名称
     */
    @ExcelProperty(value = "接口名称", index = 1)
    private String interfaceName;

    /**
     * 地址老化
     */
    private String aging;

    /**
     * 类别
     */
    private String type;

    /**
     * 状态
     */
    @ExcelProperty(value = "状态", index = 2)
    private String status;

    /**
     * 虚拟局域网名称(MAC)
     */
    @ExcelProperty(value = "VLAN名称", index = 3)
    private String vlanName;
}

