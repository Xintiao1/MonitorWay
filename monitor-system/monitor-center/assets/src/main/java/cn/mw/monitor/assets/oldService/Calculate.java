package cn.mw.monitor.assets.oldService;

import cn.joinhealth.monitor.assets.dto.ItemDTO;

import java.util.List;

public interface Calculate {
    public List<ItemDTO> calculate(String name, List<ItemDTO> list) throws Exception;
}
