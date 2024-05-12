package cn.mw.monitor.assets.utils.util;


public class UnitsFormatResult {
    private String value;//值

    private String suffix;//后缀  value + " " + suffix  组成 "25 %"

    private String adaptedUnits;//最合适的单位

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getAdaptedUnits() {
        return adaptedUnits;
    }

    public void setAdaptedUnits(String adaptedUnits) {
        this.adaptedUnits = adaptedUnits;
    }
}
