package cn.mw.monitor.service.user.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageAuth {

    // id
    private Integer pageId;
    // 名称
    private String pageName;
    // 上级id
    private Integer pid;
    // 深度
    private Integer deep;
    // 权限
    private Auth auth;
    // 下级
    private List<PageAuth> children;

    // 下级节点解析协议, 用于对接不同类型的属性结构
    private String nodeProtocol;

    /**
     *
     */
    public void addChild(List<PageAuth> pageAuthList) {
        if (null == children) {
            children = new ArrayList<>();
        }
        children.addAll(pageAuthList);
    }

}
