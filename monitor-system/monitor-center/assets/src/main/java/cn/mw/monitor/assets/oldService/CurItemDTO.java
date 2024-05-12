package cn.mw.monitor.assets.oldService;

import cn.joinhealth.monitor.assets.dto.ItemDTO;
import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class CurItemDTO implements Calculate{
    @Override
    public List<ItemDTO> calculate(String name, List<ItemDTO> list) throws Exception {

        List<ItemDTO> ret = new ArrayList<ItemDTO>();
        float sum = 0;
        for(ItemDTO itemDTO : list){
            sum += Float.parseFloat(itemDTO.getLastvalue());
        }
        float avg = sum / list.size();
        ItemDTO temp = new ItemDTO();
        BeanUtils.copyProperties(temp,list.get(0));
        temp.setName("cur("+ name + ")");
        temp.setLastvalue(String.valueOf(avg));
        ret.add(temp);

        return ret;
    }
}
