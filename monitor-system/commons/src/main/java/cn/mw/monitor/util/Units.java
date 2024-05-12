package cn.mw.monitor.util;

/**
 * @author xhy
 * @date 2020/5/15 23:11
 */
public enum Units {
    OTHERS("no"),//无法转换的单位
    KB("KB"),
    B("B"),
    BPS("Bps"),
    bps("bps"),
    KBS("KB/s"),
    kBS("kB/s"),
    rpm("rpm"),
    Amps("Amps"),
    s("s"),
    Voltage("Voltage"),
    Celcius("Celcius"),
    uptime("uptime"),
    PRECENT("%"),
    Hz("Hz");

    private String units;

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    Units(String units) {
        this.units = units;
    }
}
