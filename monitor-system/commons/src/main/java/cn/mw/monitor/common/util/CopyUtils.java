package cn.mw.monitor.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * Description: 对象拷贝工具类 <br>
 * Date: 2018-11-29 11:50 AM <br>
 * Author: sqye
 */
@Slf4j
public class CopyUtils {
    /**
     *
     * 拷贝对象
     *
     * <p><b>Example</b>
     *  Example2 example2 = CopyUtils.copy(Example2.class,source);
     *
     * @param clazz 目标对象Class
     * @param source 源对象
     * @return 目标对象实例
     * @throws Exception
     */
    public static <T> T copy(Class clazz,Object source) {
        if(source == null){
            return null;
        }
        T t = null;
        try {
            t = (T)clazz.newInstance();
        } catch (Exception e) {
            log.error("错误返回 :{}",e);
        }
        BeanUtils.copyProperties(source,t);
        return t;
    }

    public static void copyObj(Object source,Object dest) throws Exception{
        if(source == null || null == dest){
            return;
        }
        BeanUtils.copyProperties(source,dest);
    }

    /**
     * 拷贝list对象
     *
     *  <p><b>Example</b>
     *  List<Example2> list = CopyUtils.copyList(Example2.class,listSource);
     *
     * @param clazz 目标对象Class
     * @param source  源对象list
     * @return 拷贝list对象
     * @throws Exception
     */
    public static <T> List<T> copyList(Class clazz,List source) throws Exception{
        if(source == null){
            return null;
        }
        List<T> list =  new ArrayList<>();
        if(source!= null && !source.isEmpty()){
            for(Object o : source){
                list.add(copy(clazz,o));
            }
        }
        return list;
    }
}
