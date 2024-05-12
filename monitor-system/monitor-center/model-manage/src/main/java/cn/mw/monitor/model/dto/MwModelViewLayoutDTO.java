package cn.mw.monitor.model.dto;

import lombok.Data;

/**
 * @author qzg
 * @date 2022/11/30
 */
@Data
public class MwModelViewLayoutDTO {
    //机柜布局中所占的起始位置
    private int startIndex;
    //机柜布局中所占的结束位置
    private int endIndex;
    //刀片布局中所占的行数位置
    private int bayRow;
    //刀片布局中所占的列数位置
    private int bayCol;
}
