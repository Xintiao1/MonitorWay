package cn.mw.monitor.visualized.util;

import cn.mw.monitor.util.UnitsUtil;
import cn.mwpaas.common.utils.StringUtils;

import java.util.Map;

/**
 * @ClassName MwVisualizedUnitChangeUtil
 * @Author gengjb
 * @Date 2022/5/23 14:41
 * @Version 1.0
 * 转换单位信息
 **/
public class MwVisualizedUnitChangeUtil {

    /**
     * 转换单位
     * @param unit 原来的单位
     * @param value 需转换的值
     * @return 返回新单位与新值
     */
    public static Map<String,String> changeUnit(String unit, String value){
        if("B".equals(unit)){
            if(StringUtils.isNotBlank(value) && Double.parseDouble(value) > 10000){
                return UnitsUtil.getValueMap(value,"GB",unit);
            }else{
                return UnitsUtil.getValueMap(value,"MB",unit);
            }
        }
        if("bps".equals(unit)){
            if(StringUtils.isNotBlank(value) &&  Double.parseDouble(value) > 10000){
                return UnitsUtil.getValueMap(value,"Gbps",unit);
            }else{
                return UnitsUtil.getValueMap(value,"Mbps",unit);
            }
        }
        return null;
    }
}
