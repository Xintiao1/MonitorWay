package cn.mw.monitor.visualized.dto;

import cn.mw.monitor.visualized.param.MwVisualizedIndexQueryParam;
import lombok.Data;

import java.util.List;

/**
 * @ClassName MwVisualizedClassifyDto
 * @Author gengjb
 * @Date 2022/4/21 15:14
 * @Version 1.0
 **/
@Data
public class MwVisualizedClassifyDto {

    /**
     * 视图分类ID
     */
    private Integer classifyId;

    /**
     * 视图分类名称
     */
    private String classifyName;

    /**
     * 分类下视图数量
     */
    private int visualizedCount;

    /**
     * 分类ID
     */
    private List<Integer> classifyIds;

    private Integer uuid;

    /**
     * 可视化视图数据
     */
    private List<MwVisualizedViewDto> views;


    /**
     * 视图ID集合
     */
    private List<Integer> viewIds;


}
