package cn.mw.monitor.graph.modelAsset;

import com.alibaba.fastjson.JSON;
import org.neo4j.ogm.typeconversion.AttributeConverter;

public class ModelRelationDTOConverter implements AttributeConverter<ModelRelationDTO, String> {
    @Override
    public String toGraphProperty(ModelRelationDTO modelRelationDTO) {
        String ret = JSON.toJSONString(modelRelationDTO);
        return ret;
    }

    @Override
    public ModelRelationDTO toEntityAttribute(String s) {
        ModelRelationDTO modelRelationDTO = JSON.parseObject(s ,ModelRelationDTO.class);
        return modelRelationDTO;
    }
}
