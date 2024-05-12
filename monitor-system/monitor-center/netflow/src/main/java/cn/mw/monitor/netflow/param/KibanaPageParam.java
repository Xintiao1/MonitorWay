package cn.mw.monitor.netflow.param;

import lombok.Data;

/**
 * @author gui.quanwang
 * @className KibanaPageParam
 * @description 支持KIBANA语法的类文件
 * @date 2023/3/15
 */
@Data
public class KibanaPageParam {

    /**
     * 索引结束
     */
    private int end;

    /**
     * 前端（无用）
     */
    private int id;

    /**
     * 是否为空格（true：是）
     */
    private boolean isEmpty;

    /**
     * 索引开始
     */
    private int start;

    /**
     * 是否为操作符（true：是 ）
     */
    private boolean symbolCode;

    /**
     *
     * {@link cn.mw.monitor.netflow.enums.KibanaType}
     */
    private String type;

    /**
     * 值
     */
    private String value;

}
