package cn.mw.monitor.service.dropdown.param;

import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import lombok.Data;

/**
 * @author xhy
 * @date 2021/1/28 15:12
 */
@Data
public class SelectCharDropDto {
    private Integer id;
    private String dropKey;
    private String dropValue;
    private String monitorServerId;

    public static SelectCharDropDto genLocalhost(){
        SelectCharDropDto selectCharDropDto = new SelectCharDropDto();
        selectCharDropDto.setId(0);
        selectCharDropDto.setDropKey(MwEngineCommonsService.LOCALHOST_KEY);
        selectCharDropDto.setDropValue(MwEngineCommonsService.LOCALHOST_NAME);
        return selectCharDropDto;
    }
}
