package cn.mw.monitor.user.model;

import lombok.Data;

@Data
public class MwModule {

    // 模块Id
    private Integer id;
    // 上级模块Id
    private Integer pid;
    // 模块名称
    private String moduleName;
    // 模块描述
    private String moduleDesc;
    // 模块url
    private String url;
    // 是否叶子节点
    private Boolean isNode;
    // 深度标识
    private Integer deep;
    // 节点Id
    private String nodes;
    // 启用状态
    private String enable;
    // 版本
    private Integer version;
    //删除标识符
    private Boolean deleteFlag;
    //下级节点访问协议
    private String nodeProtocol;
}