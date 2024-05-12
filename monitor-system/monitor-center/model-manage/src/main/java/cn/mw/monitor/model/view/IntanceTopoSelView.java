package cn.mw.monitor.model.view;

import cn.mw.monitor.graph.modelAsset.ComboParam;
import cn.mw.monitor.service.graph.NodeParam;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class IntanceTopoSelView {
    private Integer id;
    private String name;
    private List<IntanceTopoSelView> childs;

    public void extractChild(NodeParam childNode){
        if(null == childs){
            childs = new ArrayList<>();
        }

        IntanceTopoSelView child = new IntanceTopoSelView();
        child.setId(childNode.getRealId());
        child.setName(childNode.getLabel());
        childs.add(child);
    }

    public void extractFrom(ComboParam comboParam){
        this.id = comboParam.getId();
        this.name = comboParam.getLabel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntanceTopoSelView that = (IntanceTopoSelView) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
