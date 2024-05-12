package cn.mw.monitor.model.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author guiquanwnag
 * @datetime 2023/6/30
 * @Description ARP视图查看类
 */
@Data
public class ModelTableViewARP implements Serializable {


    /**
     * MAC地址
     */
    @ExcelProperty(value = "MAC地址", index = 0)
    private String macAddress;

    /**
     * IP地址
     */
    @ExcelProperty(value = "IP地址", index = 1)
    private String ipAddress;


    /**
     * 虚拟局域网标识符
     */
    private String vlanId;



    /**
     * 地址老化
     */
    private String aging;

    /**
     * 类别
     */
    @ExcelProperty(value = "类别", index = 2)
    private String type;

    /**
     * 接口名称
     */
    @ExcelProperty(value = "接口名称", index = 3)
    private String interfaceName;

    /**
     * 虚拟局域网名称(MAC)
     */
    @ExcelProperty(value = "VLAN名称", index = 4)
    private String vlanName;
}

