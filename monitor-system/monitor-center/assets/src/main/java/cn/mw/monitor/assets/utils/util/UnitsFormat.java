package cn.mw.monitor.assets.utils.util;

import java.text.SimpleDateFormat;

public class UnitsFormat {
    /**
     * @param value 原始数据
     * @param units 原始单位
     * @return 转成最合适 无溢出的单位
     */
    public static UnitsFormatResult transformValueByUnits(String value,String units){
        UnitsFormatResult formatResult = new UnitsFormatResult();

        String valueReturn = "";
        String suffix = "";
        String adaptedUnits = "";
        switch (units){
            case "":
            {
                if(value.contains(".")){//小数默认保留两位
                    valueReturn = String.format("%.2f",Double.valueOf(value));
                }else{
                    valueReturn = value;
                }
                break;
            }

            case "%":
                valueReturn = String.format("%.2f",Float.valueOf(value));
                suffix = "%";
                adaptedUnits = suffix;
                break;
            case "B":
            case "bps":
            case "B/s":
            {
                Long temp = null;
                if(value.contains(".")){
                    temp = Double.valueOf(value).longValue();//遇到浮点数 先转长整数 。zabbix给B/s的 传参可为浮点数
                }else{
                    temp = Long.valueOf(value);
                }
                if(temp < Long.valueOf(1024)) {
                    valueReturn = temp.toString();
                    suffix = units.equals("B")?"B":"B/s";

                }else if(temp < Long.valueOf(1024)*1024){
                    valueReturn = String.format("%.2f",temp/1024.0);
                    suffix = units.equals("B")?"KB":"KB/s";

                }else if(temp < Long.valueOf(1024)*1024*1024){
                    valueReturn = String.format("%.2f",temp/1024.0/1024.0);
                    suffix = units.equals("B")?"MB":"MB/s";

                }else if(temp < Long.valueOf(1024)*1024*1024*1024){
                    valueReturn = String.format("%.2f",temp/1024.0/1024.0/1024.0);
                    suffix = units.equals("B")?"GB":"GB/s";

                }else{
                    valueReturn = String.format("%.2f",temp/1024.0/1024.0/1024.0/1024.0);
                    suffix = units.equals("B")?"TB":"TB/s";
                }
                adaptedUnits = suffix;
                break;
            }
            case "unixtime":
            {
                if(!value.equals("0")){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                  //  valueReturn = dateFormat.format(DateUtlis.sec2Date(value));
                }
                break;
            }
            case "s":{
                Long temp = Long.valueOf(value);
                if(temp < 60){
                    valueReturn = temp + "s";
                    adaptedUnits = "s";

                }else if(temp < 60*60){
                    valueReturn = temp/60 + "m " + temp%60 + "s";
                    adaptedUnits = "m";

                }else if(temp < 60*60*24){
                    valueReturn = temp/60/60 + "h ";
                    temp = temp%(60*60);
                    valueReturn += temp/60 + "m " + temp%60 + "s";
                    adaptedUnits = "h";

                }else if(temp < Long.valueOf(60)*60*24*365){
                    valueReturn = temp/60/60/24 + "d " ;
                    temp = temp%(60*60*24);
                    valueReturn += temp/60/60 + "h ";
                    temp = temp%(60*60);
                    valueReturn += temp/60 + "m " + temp%60 + "s";
                    adaptedUnits = "d";

                }else{
                    valueReturn = temp/60/60/24/365 + "y ";
                    temp = temp%(Long.valueOf(60)*60*24*365);
                    valueReturn += temp/60/60/24 + "d ";
                    temp = temp%(60*60*24);
                    valueReturn += temp/60/60 + "h ";
                    temp = temp%(60*60);
                    valueReturn += temp/60 + "m " + temp%60 + "s";
                    adaptedUnits = "y";

                }
                break;
            }
            default:
                valueReturn = value;
                suffix = units;
                adaptedUnits = units;
        }

        formatResult.setValue(valueReturn);
        formatResult.setSuffix(suffix);
        formatResult.setAdaptedUnits(adaptedUnits);

        return formatResult;
    }

    /**
     *
     * @param value 原始数据
     * @param units 原始单元
     * @param targetUnits 目标单位
     * @return
     */
    public static UnitsFormatResult transformValueToTargetUnits(String value,String units,String targetUnits){
        UnitsFormatResult formatResult = new UnitsFormatResult();
        String valueReturn = "";
        String suffix = "";
        String adaptedUnits = "";
        switch (units){
            case "":
            {
                if(value.contains(".")){//小数默认保留两位
                    valueReturn = String.format("%.2f",Float.valueOf(value));
                }else{
                    valueReturn = value;
                }
                break;
            }

            case "%":
                valueReturn = String.format("%.2f",Float.valueOf(value));
                suffix = "%";
                adaptedUnits = suffix;
                break;
            case "B":
            case "B/s":
            {
                Long temp = null;
                if(value.contains(".")){
                    temp = Float.valueOf(value).longValue();//遇到浮点数 先转长整数 。zabbix给B/s的 传参可为浮点数
                }else{
                    temp = Long.valueOf(value);
                }
                if(targetUnits.equals("B") || targetUnits.equals("B/s")) {
                    valueReturn = temp.toString();
                    suffix = units.equals("B")?"B":"B/s";

                }else if(targetUnits.equals("KB") || targetUnits.equals("KB/s")){
                    valueReturn = String.format("%.2f",temp/1024.0);
                    suffix = units.equals("B")?"KB":"KB/s";

                }else if(targetUnits.equals("MB") || targetUnits.equals("MB/s")){
                    valueReturn = String.format("%.2f",temp/1024.0/1024.0);
                    suffix = units.equals("B")?"MB":"MB/s";

                }else if(targetUnits.equals("GB") || targetUnits.equals("GB/s")){
                    valueReturn = String.format("%.2f",temp/1024.0/1024.0/1024.0);
                    suffix = units.equals("B")?"GB":"GB/s";

                }else{
                    valueReturn = String.format("%.2f",temp/1024.0/1024.0/1024.0/1024.0);
                    suffix = units.equals("B")?"TB":"TB/s";
                }
                adaptedUnits = suffix;
                break;
            }
            case "unixtime":
            {
                if(!value.equals("0")){
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                   // valueReturn = dateFormat.format(DateUtlis.sec2Date(value));
                }
                break;
            }
            case "s":{
                Long temp = Long.valueOf(value);
                if(targetUnits.equals("s")){
                    valueReturn = temp + "s";

                }else if(targetUnits.equals("m")){
                    valueReturn = temp/60 + "m " + temp%60 + "s";

                }else if(targetUnits.equals("h")){
                    valueReturn = temp/60/60 + "h ";
                    temp = temp%(60*60);
                    valueReturn += temp/60 + "m " + temp%60 + "s";

                }else if(targetUnits.equals("d")){
                    valueReturn = temp/60/60/24 + "d " ;
                    temp = temp%(60*60*24);
                    valueReturn += temp/60/60 + "h ";
                    temp = temp%(60*60);
                    valueReturn += temp/60 + "m " + temp%60 + "s";

                }else{
                    valueReturn = temp/60/60/24/365 + "y ";
                    temp = temp%(Long.valueOf(60)*60*24*365);
                    valueReturn += temp/60/60/24 + "d ";
                    temp = temp%(60*60*24);
                    valueReturn += temp/60/60 + "h ";
                    temp = temp%(60*60);
                    valueReturn += temp/60 + "m " + temp%60 + "s";
                }
                adaptedUnits = targetUnits;
                break;
            }
            default:
                valueReturn = value;
                suffix = units;
                adaptedUnits = units;
        }
        formatResult.setValue(valueReturn);
        formatResult.setSuffix(suffix);
        formatResult.setAdaptedUnits(adaptedUnits);
        return formatResult;
    }
}
