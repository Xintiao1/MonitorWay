package cn.mw.monitor.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2020/7/27 15:11
 * @Version 1.0
 */
public enum NewUnits {
//    没有转换的单位mapKey为-1
    PERCENT("%",0,-1),
    B("B",1024,0),
    KB("KB",1024,0),
    MB("MB",1024,0),
    GB("GB",1024,0),
    TB("TB",1024,0),

    bPS("bps",1000,1),
    KbPS("Kbps",1000,1),
    MbPS("Mbps",1000,1),
    GbPS("Gbps",1000,1),
    TbPS("Tbps",1000,1),

    BPS("Bps",1024,2),
    KBPS("KBps",1024,2),
    MBPS("MBps",1024,2),
    GBPS("GBps",1024,2),
    TBPS("TBps",1024,2),

    BS("B/s",1024,3),
    KBS("KB/s",1024,3),
    MBS("MB/s",1024,3),
    GBS("GB/s",1024,3),
    TBS("TB/s",1024,3),

    bS("b/s",1024,4),
    KbS("Kb/s",1024,4),
    MbS("Mb/s",1024,4),
    GbS("Gb/s",1024,4),
    TbS("Tb/s",1024,4),

    HZ("Hz",1000,5),
    KHZ("KHz",1000,5),
    MHZ("MHz",1000,5),
    GHZ("GHz",1000,5),
    THZ("THz",1000,5),

    MS("ms",1000,6),
    S("s",1000,6),

    T("条",1000,7),
    K("千",10,7),
    W("万",10,7),
    SW("十万",10,7),
    BW("百万",10,7),
    KW("千万",10,7),
    Y("亿",10,7),

    Byte("Byte/s",1024,8),
    KByte("KByte/s",1024,8),
    MByte("MByte/s",1024,8),
    GByte("GByte/s",1024,8),
    TByte("TByte/s",1024,8),

    Bsec("B/sec",1024,9),
    KBsec("KB/sec",1024,9),
    MBsec("MB/sec",1024,9),
    GBsec("GB/sec",1024,9),
    TBsec("TB/sec",1024,9),

    BI("Bi",1024,10),
    KI("Ki",1024,10),
    MI("Mi",1024,10),
    GI("Gi",1024,10),
    TI("Ti",1024,10);
    //单位的名称
    private String units;
    //单位转换的基数
    private Integer radix;
    //不同类型单位的mapkey值
    private Integer mapKey;
    public static final Map<Integer, List> UNITSMAP = new HashMap<>();
    static {
        //每种单位的转换流程
        UNITSMAP.put(0, Arrays.asList("B","KB","MB","GB","TB"));
        UNITSMAP.put(1,Arrays.asList("bps","Kbps","Mbps","Gbps"));
        UNITSMAP.put(2,Arrays.asList("Bps","KBps","MBps","GBps"));
        UNITSMAP.put(3,Arrays.asList("B/s","KB/s","MB/s","GB/s","TB/s"));
        UNITSMAP.put(4,Arrays.asList("b/s","Kb/s","Mb/s","Gb/s","Tb/s"));
        UNITSMAP.put(5,Arrays.asList("Hz","KHz","MHz","GHz","THz"));
        UNITSMAP.put(6,Arrays.asList("ms","s"));
        UNITSMAP.put(7,Arrays.asList("条","千","万","十万","百万","千万","亿"));
        UNITSMAP.put(8,Arrays.asList("Byte/s","KByte/s","MByte/s","GByte/s","TByte/s"));
        UNITSMAP.put(9,Arrays.asList("B/sec","KB/sec","MB/sec","GB/sec","TB/sec"));
        UNITSMAP.put(10,Arrays.asList("Bi","Ki","Mi","Gi","Ti"));
    }
    /**
     * 根据单位名称获取相应枚举
     * @param units
     * @return
     */
    public static NewUnits getInfoByUnits(String units) {
        for(NewUnits u : NewUnits.values()) {
            if(u.getUnits().equals(units)) {
                return u;
            }
        }
        return null;
    }

    NewUnits(String units, Integer radix, Integer mapKey) {
        this.units = units;
        this.radix = radix;
        this.mapKey = mapKey;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Integer getRadix() {
        return radix;
    }

    public void setRadix(Integer radix) {
        this.radix = radix;
    }

    public Integer getMapKey() {
        return mapKey;
    }

    public void setMapKey(Integer mapKey) {
        this.mapKey = mapKey;
    }
}
