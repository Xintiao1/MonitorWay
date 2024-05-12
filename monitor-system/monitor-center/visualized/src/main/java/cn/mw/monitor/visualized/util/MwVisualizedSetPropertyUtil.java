package cn.mw.monitor.visualized.util;

import cn.mw.monitor.visualized.enums.VisualizedZkSoftWareItemEnum;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName
 * @Description 属性赋值
 * @Author gengjb
 * @Date 2023/3/16 15:50
 * @Version 1.0
 **/
@Slf4j
public class MwVisualizedSetPropertyUtil {

    private static final String regex = "\\d+\\.+\\d+";

    /**
     * 设置属性
     */
    public static void setProperty(Object obj, String name, String lastValue, String units) throws Exception {
        String proPerty = VisualizedZkSoftWareItemEnum.getProPerty(name);
        Field field = obj.getClass().getDeclaredField(proPerty);
        field.setAccessible(true);
        log.info("设置属性"+lastValue);
        if(StringUtils.isNotBlank(lastValue) && MwVisualizedUtil.checkStrIsNumber(lastValue)){
            Object oldValue = field.get(obj);
            if(oldValue != null && name.contains("MW_OUTPUT_CURRENT")){
                log.info("设置属性数字"+oldValue+":::"+lastValue);
                //提取数字
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(oldValue.toString());
                while (matcher.find()){
                    String group = matcher.group();
                    log.info("正则匹配数字"+group+":::"+lastValue);
                    Double value = (new BigDecimal(group).setScale(2, BigDecimal.ROUND_HALF_UP).add(new BigDecimal(lastValue).setScale(2, BigDecimal.ROUND_HALF_UP))).doubleValue();
                    field.set(obj,value+units);
                }
            }else{
                Double value = new BigDecimal(lastValue).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                field.set(obj,value+units);
            }
        }else{
            field.set(obj,lastValue+units);
        }
    }
}
