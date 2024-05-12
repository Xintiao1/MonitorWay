package cn.mw.monitor.util;

import cn.mwpaas.common.utils.StringUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/5/13 8:49
 */
public class UnitsUtil {
//    /**
//     * @param value
//     * @param units
//     * @return 返回值value 带单位
//     */
//    public static String getDataUnits(String value, String units) {
//        if (null != units && StringUtils.isNotEmpty(units)) {
//            BigDecimal disk = new BigDecimal(value);
//            disk = disk.setScale(2, BigDecimal.ROUND_HALF_UP);//四舍五入保留两位小数
//            if (units.equals(Units.B.getUnits())) {
//                if (disk.doubleValue() < 1024) {
//                    value = disk + "B";
//                } else if (1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024) {
//                    value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP) + "KB";
//                } else if (1024 * 1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024 * 1024) {
//                    value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "MB";
//                } else if (1024 * 1024 * 1024 > (disk.doubleValue() / 1024) && disk.doubleValue() > 1024 * 1024 * 1024) {
//                    value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "GB";
//                } else {
//                    disk = disk.divide(new BigDecimal(1024), 10, BigDecimal.ROUND_HALF_UP);
//                    value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "TB";
//                }
//            } else if (units.equals(Units.KB.getUnits())) {
//                if (disk.doubleValue() < 1024) {
//                    value = disk + "KB";
//                } else if (1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024) {
//                    value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP) + "MB";
//                } else if (1024 * 1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024 * 1024) {
//                    value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "GB";
//                } else {
//                    value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "TB";
//                }
//            } else if (units.equals(Units.BPS.getUnits())) {
//                if (disk.doubleValue() < 1024) {
//                    value = disk + "Bps";
//                } else if (1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024) {
//                    value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP) + "KBps";
//                } else if (1024 * 1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024 * 1024) {
//                    value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "MBps";
//                } else {
//                    value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "MBps";
//                }
//            } else if (units.equals(Units.KBS.getUnits()) || units.equals(Units.kBS.getUnits())) {
//                if (disk.doubleValue() < 1024) {
//                    value = disk + "KB/s";
//                } else if (1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024) {
//                    value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP) + "MB/s";
//                } else if (1024 * 1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024 * 1024) {
//                    value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "GB/s";
//                } else {
//                    value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP) + "TB/s";
//                }
//            } else if (units.equals(Units.bps.getUnits())) {
//                if (disk.doubleValue() < 1000) {
//                    value = disk + "bps";
//                } else if (1000 * 1000 > disk.doubleValue() && disk.doubleValue() > 1000) {
//                    value = disk.divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP) + "Kbps";
//                } else if (1000 * 1000 * 1000 > disk.doubleValue() && disk.doubleValue() > 1000 * 1000) {
//                    value = disk.divide(new BigDecimal(1000 * 1000), 2, BigDecimal.ROUND_HALF_UP) + "Mbps";
//                } else {
//                    value = disk.divide(new BigDecimal(1000 * 1000 * 1000), 2, BigDecimal.ROUND_HALF_UP) + "Gbps";
//                }
//            } else if (units.equals(Units.Hz.getUnits())) {
//                if (disk.doubleValue() < 1000) {
//                    value = disk + "Hz";
//                } else if (1000 * 1000 > disk.doubleValue() && disk.doubleValue() > 1000) {
//                    value = disk.divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP) + "KHz";
//                } else if (1000 * 1000 * 1000 > disk.doubleValue() && disk.doubleValue() > 1000 * 1000) {
//                    value = disk.divide(new BigDecimal(1000 * 1000), 2, BigDecimal.ROUND_HALF_UP) + "MHz";
//                } else {
//                    value = disk.divide(new BigDecimal(1000 * 1000 * 1000), 2, BigDecimal.ROUND_HALF_UP) + "GHz";
//                }
//            } else if (units.equals(Units.rpm.getUnits()) || units.equals(Units.Voltage.getUnits()) || units.equals(Units.Celcius.getUnits())) {
//                value = disk + units;
//            } else if (units.equals(Units.s.getUnits())) {
//                value = disk.multiply(new BigDecimal(1000)).setScale(2, BigDecimal.ROUND_HALF_UP) + "ms";
//            } else {
//                if (disk.doubleValue() < 1000) {
//                    value = disk + units;
//                } else if (1000 * 1000 > disk.doubleValue() && disk.doubleValue() > 1000) {
//                    value = disk.divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP) + "K" + units;
//                } else if (1000 * 1000 * 1000 > disk.doubleValue() && disk.doubleValue() > 1000 * 1000) {
//                    value = disk.divide(new BigDecimal(1000 * 1000), 2, BigDecimal.ROUND_HALF_UP) + "G" + units;
//                } else {
//                    value = disk.divide(new BigDecimal(1000 * 1000 * 1000), 2, BigDecimal.ROUND_HALF_UP) + "T" + units;
//                }
//            }
//        }
//        return value;
//    }
//
//    /**
//     * @param value
//     * @param units
//     * @return 返回值value 和 units
//     */
//    public static Map<String, String> getHistoryyUnits(String value, String units) {
//        Map<String, String> map = new HashMap<>();
//        if (null != units && StringUtils.isNotEmpty(units)) {
//            BigDecimal disk = new BigDecimal(value);
//            disk = disk.setScale(2, BigDecimal.ROUND_HALF_UP);//四舍五入保留两位小数
//            if (units.equals(Units.B.getUnits())) {
//                if (disk.doubleValue() < 1024) {
//                    value = String.valueOf(disk);
//                } else if (1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024) {
//                    value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "KB";
//                } else if (1024 * 1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024 * 1024) {
//                    value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "MB";
//                } else if (1024 * 1024 * 1024 > (disk.doubleValue() / 1024) && disk.doubleValue() > 1024 * 1024 * 1024) {
//                    value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "GB";
//                } else {
//                    disk = disk.divide(new BigDecimal(1024), 10, BigDecimal.ROUND_HALF_UP);
//                    value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "TB";
//                }
//            } else if (units.equals(Units.KB.getUnits())) {
//                if (disk.doubleValue() < 1024) {
//                    value = String.valueOf(disk);
//                } else if (1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024) {
//                    value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "MB";
//                } else if (1024 * 1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024 * 1024) {
//                    value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "GB";
//                } else {
//                    value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "TB";
//                }
//            } else if (units.equals(Units.BPS.getUnits())) {
//                if (disk.doubleValue() < 1024) {
//                    value = String.valueOf(disk);
//                } else if (1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024) {
//                    value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "KBps";
//                } else if (1024 * 1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024 * 1024) {
//                    value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "MBps";
//                } else {
//                    value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "GBps";
//                }
//            } else if (units.equals(Units.KBS.getUnits()) || units.equals(Units.kBS.getUnits())) {
//                if (disk.doubleValue() < 1024) {
//                    value = String.valueOf(disk);
//                } else if (1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024) {
//                    value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "MB/s";
//                } else if (1024 * 1024 * 1024 > disk.doubleValue() && disk.doubleValue() > 1024 * 1024) {
//                    value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "GB/s";
//                } else {
//                    value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "TB/s";
//                }
//            } else if (units.equals(Units.bps.getUnits())) {
//                if (disk.doubleValue() < 1000) {
//                    value = String.valueOf(disk);
//                } else if (1000 * 1000 > disk.doubleValue() && disk.doubleValue() > 1000) {
//                    value = disk.divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "Kbps";
//                } else if (1000 * 1000 * 1000 > disk.doubleValue() && disk.doubleValue() > 1000 * 1000) {
//                    value = disk.divide(new BigDecimal(1000 * 1000), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "Mbps";
//                } else {
//                    value = disk.divide(new BigDecimal(1000 * 1000 * 1000), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "Gbps";
//                }
//            } else if (units.equals(Units.PRECENT.getUnits())) {
//                value = disk.toString();
//                units = "%";
//            } else {
//                if (disk.doubleValue() < 1000) {
//                    value = String.valueOf(disk);
//                } else if (1000 * 1000 > disk.doubleValue() && disk.doubleValue() > 1000) {
//                    value = disk.divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "K" + units;
//                } else if (1000 * 1000 * 1000 > disk.doubleValue() && disk.doubleValue() > 1000 * 1000) {
//                    value = disk.divide(new BigDecimal(1000 * 1000), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "G" + units;
//                } else {
//                    value = disk.divide(new BigDecimal(1000 * 1000 * 1000), 2, BigDecimal.ROUND_HALF_UP).toString();
//                    units = "T" + units;
//                }
//            }
//        }
//        map.put("value", value);
//        map.put("units", units);
//        return map;
//    }
//
//    /**
//     * @param value     未处理过的值
//     * @param lastUnits 根据峰值转换过的单位
//     * @param units     原始的item单位
//     * @return
//     */
//    public static Map<String, String> getValueByUnits(String value, String lastUnits, String units) {
//        Map<String, String> map = new HashMap<>();
//        if (null != units && StringUtils.isNotEmpty(units)) {
//            BigDecimal disk = new BigDecimal(value);
//            disk = disk.setScale(2, BigDecimal.ROUND_HALF_UP);//四舍五入保留两位小数
//            if (null != lastUnits && StringUtils.isNotEmpty(lastUnits)) {
//                if (lastUnits.equals(units)) {
//                    value = String.valueOf(disk);
//                } else {
//                    if (units.equals(Units.B.getUnits())) {
//                        if (lastUnits.equals("KB")) {
//                            value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "KB";
//                        } else if (lastUnits.equals("MB")) {
//                            value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "MB";
//                        } else {
//                            value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "GB";
//                        }
//                    } else if (units.equals(Units.KB.getUnits())) {
//                        if (lastUnits.equals("MB")) {
//                            value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "MB";
//                        } else if (lastUnits.equals("GB")) {
//                            value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "GB";
//                        } else {
//                            value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "TB";
//                        }
//                    } else if (units.equals(Units.BPS.getUnits())) {
//                        if (lastUnits.equals("KBps")) {
//                            value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "KBps";
//                        } else if (lastUnits.equals("MBps")) {
//                            value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "MBps";
//                        } else {
//                            value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "GBps";
//                        }
//                    } else if (units.equals(Units.KBS.getUnits()) || units.equals(Units.kBS.getUnits())) {
//                        if (lastUnits.equals("MB/s")) {
//                            value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "MB/s";
//                        } else if (lastUnits.equals("GB/s")) {
//                            value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "GB/s";
//                        } else {
//                            value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "TB/s";
//                        }
//                    } else if (units.equals(Units.bps.getUnits())) {
//                        if (lastUnits.equals("Kbps")) {
//                            value = disk.divide(new BigDecimal(1000), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "Kbps";
//                        } else if (lastUnits.equals("Mbps")) {
//                            value = disk.divide(new BigDecimal(1000 * 1000), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "Mbps";
//                        } else {
//                            value = disk.divide(new BigDecimal(1000 * 1000 * 1000), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "Gbps";
//                        }
//                    } else {
//                        if (lastUnits.equals("K" + units)) {
//                            value = disk.divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "K" + units;
//                        } else if (lastUnits.equals("G" + units)) {
//                            value = disk.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "G" + units;
//                        } else {
//                            value = disk.divide(new BigDecimal(1024 * 1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).toString();
//                            units = "T" + units;
//                        }
//                    }
//                }
//            }
//        }
//        map.put("value", value);
//        map.put("units", units);
//        return map;
//    }

    /**
     * 当单位中带！号时默认不进行单位转换，否则返回一个根据单位转换过的带单位的值，String类型
     *
     * @param value
     * @param units
     * @return 返回一个带单位的值
     */
    public static String getValueWithUnits(String value, String units) {
        if (value != null && StringUtils.isNotEmpty(value)) {
            BigDecimal bValue = new BigDecimal(value);
            if (null != units && StringUtils.isNotEmpty(units)) {
                int index = units.indexOf("!");
                if (index != -1) {
                    //含有小数点，保留两位小数
                    if (value.indexOf(".") != -1) {
                        bValue = bValue.setScale(2, BigDecimal.ROUND_HALF_UP);
                        value = bValue + units.substring(index + 1);
                    } else {
                        //不含小数点。不做单位转换但是带单位的数据
                        value = bValue + units.substring(index + 1);
                    }
                } else {
//                做单位转换但是带单位的数据
                    Map<String, String> convertedValue = getConvertedValue(bValue, units);
                    value = convertedValue.get("value") + convertedValue.get("units");
                }
            } else {
                //含有小数点，保留两位小数
                if (value.indexOf(".") != -1) {
                    value = bValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                }
            }
        } else {
            value = 0 + units;
        }

        return value;
    }

    /**
     * 当单位中带！号时默认不进行单位转换
     *
     * @param value
     * @param units
     * @return 返回一个map，根据key为value和units 去获取相应的值
     */
    public static Map<String, String> getValueAndUnits(String value, String units) {
        Map<String, String> map = new HashMap<>();
        if (null != units && StringUtils.isNotEmpty(units)) {
            BigDecimal bValue = new BigDecimal(value);
            int index = units.indexOf("!");
            if (index != -1) {
//                不做单位转换但是带单位的数据
                value = bValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                units = units.substring(index + 1);
            } else {
//                做单位转换但是带单位的数据
                Map<String, String> convertedValue = getConvertedValue(bValue, units);
                value = convertedValue.get("value");
                units = convertedValue.get("units");
            }
        }
        map.put("value", value);
        map.put("units", units);
        return map;
    }

    /**
     * 根据最新单位和原始单位的关系，获取以最新单位为标准的valueMap
     *
     * @param value
     * @param lastUnits 需要转换成的单位
     * @param units     原始的item单位
     * @return
     */
    public static Map<String, String> getValueMap(String value, String lastUnits, String units) {
        Map<String, String> map = new HashMap<>();
        BigDecimal bValue = new BigDecimal(value);
        if (null != units && StringUtils.isNotEmpty(units)) {
            int index = units.indexOf("!");
            if (index != -1) {
//                不做单位转换但是带单位的数据
                value = bValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                units = units.substring(index + 1);
            } else {
//                做单位转换但是带单位的数据
                Map<String, String> convertedValue = getConvertedValue(bValue, lastUnits, units);
                value = convertedValue.get("value");
                units = convertedValue.get("units");
            }
        } else {
            value = bValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        }
        map.put("value", value);
        map.put("units", lastUnits);
        return map;
    }

    /**
     * 根据单位和value值的大小，进行合适的单位转换
     *
     * @param value
     * @param units
     * @return
     */
    public static Map<String, String> getConvertedValue(BigDecimal value, String units) {
        Map<String, String> map = new HashMap<>();
        if (null != units && StringUtils.isNotEmpty(units)) {
            NewUnits infoByUnits = NewUnits.getInfoByUnits(units);
            if (infoByUnits != null && infoByUnits.getMapKey() >= 0) {
                List unitsList = NewUnits.UNITSMAP.get(infoByUnits.getMapKey());
                while (value.doubleValue() > infoByUnits.getRadix()) {
                    if (units.equals(unitsList.get(unitsList.size() - 1))) {
//                     当数值单位已经是最大时，value值还时大于infoByUnits.getRadix()，将不再循环
                        break;
                    }
                    value = value.divide(new BigDecimal(infoByUnits.getRadix()), 6, BigDecimal.ROUND_HALF_UP);
                    units = getNextUnits(units, infoByUnits.getMapKey(), true);
                    infoByUnits = NewUnits.getInfoByUnits(units);
                }
                while (0 < value.doubleValue() && value.doubleValue() < 1) {
                    if (units.equals(unitsList.get(0))) {
//                     当数值单位已经是最小时，value值还没有大于1，将不再循环
                        break;
                    }
                    value = value.multiply(new BigDecimal(infoByUnits.getRadix())).setScale(2, BigDecimal.ROUND_HALF_UP);
                    units = getNextUnits(units, infoByUnits.getMapKey(), false);
                    infoByUnits = NewUnits.getInfoByUnits(units);
                }
            }
        }
        //数值含有小数点
        if (String.valueOf(value).indexOf(".") != -1) {
            value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        DecimalFormat decimalFormat = new DecimalFormat("###################.###########");
        map.put("value", decimalFormat.format(value));
        map.put("units", units);
        return map;
    }

    /**
     * 返回以lastUnits为单位的数据
     *
     * @param value     未处理过的值
     * @param lastUnits 根据峰值转换过的单位
     * @param units     原始的item单位
     * @return
     */
    public static Map<String, String> getConvertedValue(BigDecimal value, String lastUnits, String units) {
        Map<String, String> map = new HashMap<>();
        if (null != units && StringUtils.isNotEmpty(units)) {
            NewUnits infoByUnits = NewUnits.getInfoByUnits(units);
            if (infoByUnits != null && infoByUnits.getMapKey() >= 0) {
                Integer unitsIndex = getUnitsIndex(lastUnits, units, infoByUnits.getMapKey());
                if (unitsIndex < 0) {
                    for (int i = 1; i <= -unitsIndex; i++) {
                        String otherUnits = getOtherUnits(lastUnits, i - 1, infoByUnits.getMapKey());
                        infoByUnits = NewUnits.getInfoByUnits(otherUnits);
                        value = value.multiply(new BigDecimal(infoByUnits.getRadix()));
                    }
                } else if (unitsIndex > 0) {
                    for (int i = 1; i <= unitsIndex; i++) {
                        String otherUnits = getOtherUnits(units, i - 1, infoByUnits.getMapKey());
                        infoByUnits = NewUnits.getInfoByUnits(otherUnits);
                        value = value.divide(new BigDecimal(infoByUnits.getRadix()), 6, BigDecimal.ROUND_HALF_UP);
                    }
                }
            }
            value = value.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        map.put("value", value.toString());
        map.put("units", units);
        return map;
    }

    /**
     * @param units 当前单位
     * @param key   当前单位所在的value对应的key
     * @param flag  true为当前单位右边的值，false为当前单位左边的值
     * @return 当前单位左边或右边的值
     */
    public static String getNextUnits(String units, Integer key, Boolean flag) {
        List<String> list = NewUnits.UNITSMAP.get(key);
        int index = 0;
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (units.equals(list.get(i))) {
                    index = i;
                }
            }
        }
        if (flag) {
            return list.get(index + 1);
        } else {
            if (index > 0) {
                return list.get(index - 1);
            } else {
                return list.get(0);
            }
        }
    }

    /**
     * 获取两个单位间的等级差
     *
     * @param lastUnits 需要转换成的单位
     * @param units     原始的item单位
     * @param key
     * @return
     */
    public static Integer getUnitsIndex(String lastUnits, String units, Integer key) {
        List<String> list = NewUnits.UNITSMAP.get(key);
        int index = 0;
        int lastIndex = 0;
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (units.equals(list.get(i))) {
                    index = i;
                }
                if (lastUnits.equals(list.get(i))) {
                    lastIndex = i;
                }
            }
        }
        return (lastIndex - index);
    }

    /**
     * 获取当前单位所在数组的下标+数值，获取所得下标的单位名称
     *
     * @param units
     * @param key
     * @return
     */
    public static String getOtherUnits(String units, Integer index, Integer key) {
        List<String> list = NewUnits.UNITSMAP.get(key);
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (units.equals(list.get(i))) {
                    index = index + i;
                }
            }
        }
        return list.get(index);
    }

    public static void main(String[] args) {
        System.out.println(getValueWithUnits(String.valueOf(3057744398L),NewUnits.B.getUnits()));
    }
}
