package cn.mw.monitor.util.entity;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/10/27
 */
@Data
public class PoiModel {
    private String content;
    private String oldContent;
    private int rowIndex;
    private int cellIndex;
}
