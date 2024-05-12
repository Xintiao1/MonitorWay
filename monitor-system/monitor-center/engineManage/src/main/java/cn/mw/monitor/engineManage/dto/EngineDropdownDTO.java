package cn.mw.monitor.engineManage.dto;

import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import lombok.Data;

/**
 * @author syt
 * @Date 2020/11/9 14:23
 * @Version 1.0
 */
@Data
public class EngineDropdownDTO {
    //引擎id
    private String id;
    //引擎名称
    private String engineName;

    private String monitorServerId;

    public static EngineDropdownDTO genLocalhost(){
        EngineDropdownDTO engineDropdownDTO = new EngineDropdownDTO();
        engineDropdownDTO.setId(MwEngineCommonsService.LOCALHOST_KEY);
        engineDropdownDTO.setEngineName(MwEngineCommonsService.LOCALHOST_NAME);
        return engineDropdownDTO;
    }
}
