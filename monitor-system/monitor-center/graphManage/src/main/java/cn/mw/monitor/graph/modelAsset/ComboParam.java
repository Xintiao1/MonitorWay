package cn.mw.monitor.graph.modelAsset;

import cn.mw.monitor.util.ListMapObjUtils;
import lombok.Data;

import java.util.Map;
import java.util.Objects;

@Data
public class ComboParam {
    private Integer id;
    private String label;

    public ComboParam(){
    }

    public ComboParam(Integer id ,String label){
        this.id = id;
        this.label = label;
    }

    public void extractFrom(Map<Integer ,ModelRelationDTO> modelAssetMap) throws Exception{
        ModelRelationDTO modelRelationDTO = modelAssetMap.get(this.id);
        if(null != modelRelationDTO){
            Map map = (Map)modelRelationDTO.getRelationInfoMap().get(this.id);
            ModelRelationInfo modelRelationInfo = ListMapObjUtils.mapToBean(map ,ModelRelationInfo.class);
            this.label = modelRelationInfo.getRelationName();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComboParam that = (ComboParam) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
