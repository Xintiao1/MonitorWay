package cn.mw.monitor.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author xhy
 * @date 2021/3/1 9:14
 */
@Data
public class MwModelInstanceRelationDto extends ModelInstanceRelationDto {
    private List<ModelInstanceRelationDto> childs;

    public void addChilds(ModelInstanceRelationDto modelInstanceRelationDto){
        if(null==childs){
            childs=new ArrayList<>();
        }
        childs.add(modelInstanceRelationDto);
    }

    public void addChilds(List<ModelInstanceRelationDto> list){
        if(null==childs){
            childs=new ArrayList<>();
        }
        childs.addAll(list);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModelInstanceRelationDto modelInstanceRelationDto = (ModelInstanceRelationDto) o;
        return Objects.equals(getInstanceRelationsId(), modelInstanceRelationDto.getInstanceRelationsId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getInstanceRelationsId());
    }
}
