package cn.mw.monitor.netflow.entity;

/**
 * @author gui.quanwang
 * @className IP
 * @description IP类
 * @date 2023/4/12
 */
public class IP {

    /**
     * ip
     */
    private String value;

    public IP(String value) {
        this.value = value;
    }

    public IP() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
