package cn.mw.monitor.assetsSubType.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author syt
 * @Date 2021/1/28 9:54
 * @Version 1.0
 */
public class TypeTreeDTO {

    private Integer id;
    /**
     * 类型名称
     */
    private String typeName;

    private Integer pid = 0;
    //下级类型
    private List<TypeTreeDTO> children = new ArrayList<>();

    private Boolean isNode;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id == null ? null : id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid == null ? null : pid;
    }

    public List<TypeTreeDTO> getChildren() {
        return children;
    }

    public void setChildren(List<TypeTreeDTO> children) {
        this.children = children;
    }

    public void setNode() {
        isNode = getNode();
    }

    public Boolean getNode() {
        isNode = this.children.size() <= 0 ? true : false;
        return isNode;
    }
}
