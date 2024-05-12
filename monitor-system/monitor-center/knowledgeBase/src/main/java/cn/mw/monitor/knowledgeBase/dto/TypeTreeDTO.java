package cn.mw.monitor.knowledgeBase.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author syt
 * @Date 2020/8/28 15:08
 * @Version 1.0
 */
public class TypeTreeDTO extends KnowledgeTypeDTO {
    private Boolean isNode;
    //下级类型
    private List<TypeTreeDTO> children = new ArrayList<>();

    public void addChild(TypeTreeDTO typeTreeDTO) {
        if (null == children) {
            children = new ArrayList<TypeTreeDTO>();
        }
        children.add(typeTreeDTO);
    }

    public void addChildren(List<TypeTreeDTO> typeTreeDTOs) {
        if (null == children) {
            children = new ArrayList<TypeTreeDTO>();
        }
        children.addAll(typeTreeDTOs);
    }

    public TypeTreeDTO(Boolean isNode, List<TypeTreeDTO> children) {
        this.isNode = isNode;
        this.children = children;
    }

    public TypeTreeDTO() {
    }

    public void setNode() {
        isNode = getNode();
    }

    public Boolean getNode() {
        isNode = this.children.size() <= 0 ? true : false;
        return isNode;
    }

    public List<TypeTreeDTO> getChildren() {
        return children;
    }

    public void setChildren(List<TypeTreeDTO> children) {
        this.children = children;
    }
}
