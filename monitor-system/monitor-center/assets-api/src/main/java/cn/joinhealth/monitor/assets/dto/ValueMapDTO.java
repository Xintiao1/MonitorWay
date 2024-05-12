package cn.joinhealth.monitor.assets.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 值映射
 * Created by dev on 2020/1/22.
 */
@Data
public class ValueMapDTO {
    private String valuemapid;
    private String name;
    private List<ValueMapDes> mappings;
    private Map<String, String> valuemap = new HashMap<String, String>();

    /**
     * 将list转换成map
     * Created by dev on 2020/1/22.
     */
    public void convertToMap(){
        if(null != mappings){
            for(ValueMapDes valueMapDes : mappings){
                valuemap.put(valueMapDes.getValue(), valueMapDes.getNewvalue());
            }
        }
    }
}
