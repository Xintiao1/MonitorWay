package cn.mw.monitor.visualized.service;

/**
 * @ClassName MwVisualizedDataChange
 * @Author gengjb
 * @Date 2022/4/26 9:55
 * @Version 1.0
 **/
public interface MwVisualizedDataChange {

    int[] getType();

    Object getData(Object data);
}
