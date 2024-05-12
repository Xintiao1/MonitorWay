package cn.mw.monitor.configmanage.entity;

import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className TreeData
 * @description 树和数据整合
 * @date 2021/12/23
 */
@Data
public class TreeData {

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点ID
     */
    private String node;

    /**
     * 父节点
     */
    private String parentNode;

    /**
     * 节点类型（FILE文件夹   DATA数据）
     */
    private String type;

    /**
     * 如果类型为文件夹，则可能包含子级数据
     */
    private List<TreeData> childList;

}
