package cn.mw.monitor.visualized.service;

/**
 * @ClassName MwVisualizedModule
 * @Description 组件区获取内容接口
 * @Author gengjb
 * @Date 2023/4/17 10:17
 * @Version 1.0
 **/
public interface MwVisualizedModule {

    int[] getType();

    Object getData(Object data);
}
