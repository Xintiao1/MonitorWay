package cn.mw.module.solarwind.dto;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/6/29 21:53
 */
@Data
public class SolarReportDetailDto {
    private Integer interfaceID;
    private String caption;
    private String carrierName;
    private Float inBandwidth;
    private Float inAveragebps;
    private Float inMinbps;
    private Float inMaxbps;
    private Float outMinbps;
    private Float outAveragebps;
    private Float outMaxbps;
    private Float inProportionTen;
    private Float inProportionFifty;
    private Float inProportionEighty;
    private Float inProportionHundred;
    private Float outProportionTen;
    private Float outProportionFifty;
    private Float outProportionEighty;
    private Float outProportionHundred;
}
