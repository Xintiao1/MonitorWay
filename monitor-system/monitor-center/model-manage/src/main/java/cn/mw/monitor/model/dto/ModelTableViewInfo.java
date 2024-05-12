package cn.mw.monitor.model.dto;

import cn.mw.monitor.snmp.utils.SNMPUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * @author guiquanwnag
 * @datetime 2023/6/30
 * @Description ARP_MAC_IP视图查看类
 */
@Data
public class ModelTableViewInfo extends ModelTableViewRoot implements Serializable {

    /**
     * IP地址(ARP)
     */
    private String ipAddress;

    /**
     * MAC地址(ARP+MAC+IP)
     */
    private String macAddress;

    /**
     * 虚拟局域网标识符
     */
    private String vlanId;

    /**
     * 虚拟局域网名称(MAC)
     */
    private String vlanName;

    /**
     * 接口名称(MAC)
     */
    private String interfaceName;

    /**
     * 接口描述(IP)
     */
    private String interfaceDesc;

    /**
     * 地址老化
     */
    private String aging;

    /**
     * 类别(ARP)
     */
    private String type;

    /**
     * 状态(MAC+IP)
     */
    private String status;

    public void setMacAddress(String macAddress) {
        String macAddr = SNMPUtils.convertMacFromHexString(macAddress);
        if (isValidMacAddress(macAddr)) {
            this.macAddress = macAddr;
        } else {
            this.macAddress = "";
        }
    }


    // 正则表达式模式用于匹配合法的 MAC 地址格式
    private static final String MAC_ADDRESS_PATTERN = "^[0-9A-Fa-f]+$";

    // 编译正则表达式的 Pattern 对象
    private static final Pattern pattern = Pattern.compile(MAC_ADDRESS_PATTERN);

    /**
     * 校验 MAC 地址是否合规
     *
     * @param macAddress 待校验的 MAC 地址
     * @return true，如果 MAC 地址合规；否则返回 false
     */
    private static boolean isValidMacAddress(String macAddress) {
        String mac = macAddress.replaceAll("[:-]", "");
        if (mac == null) {
            return false;
        }
        // 使用正则表达式匹配 MAC 地址格式
        return pattern.matcher(mac).matches();
    }

}

