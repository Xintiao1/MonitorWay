package cn.mw.zbx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.Map;

@Data
public class MWMapRequest extends MWRequestAbstract {

    private Map params;

    public MWMapRequest() {

    }

    @Override
    void setParams(Object params) {
        this.params = (Map) params;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
