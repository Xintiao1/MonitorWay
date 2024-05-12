package cn.mw.monitor.server.serverdto;

import lombok.Data;

/**
 * @author syt
 * @Date 2021/5/27 16:57
 * @Version 1.0
 */
@Data
public class AssetsAvailableDTO {
    //开始节点
    private Integer gt;
    //结束节点
    private Integer lte;
    //颜色
    private String color;
}
