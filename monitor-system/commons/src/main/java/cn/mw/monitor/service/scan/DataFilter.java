package cn.mw.monitor.service.scan;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;

import java.util.List;

/**
 * @author guiquanwnag
 * @datetime 2023/5/19
 * @Description 数据分析器（利用责任链处理数据）
 */
public interface DataFilter {

    /**
     * 如果有多个过滤器，依次顺序执行
     *
     * @param filter
     */
    void setNext(DataFilter filter);

    /**
     * 执行方法(进行过滤)
     *
     * @param object
     */
    void process(Object object);

}
