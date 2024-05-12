package cn.mw.monitor.service.assets.param;

import lombok.Data;

@Data
public class MwAssetsMainTainV1Period {
    public static String SEP = ":";
    String start;
    String end;

    Integer sustainHour;

    Long minute;
}
