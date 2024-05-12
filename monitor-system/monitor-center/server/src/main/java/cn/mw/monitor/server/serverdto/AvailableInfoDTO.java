package cn.mw.monitor.server.serverdto;

import cn.mw.monitor.service.webmonitor.model.MwHistoryDTO;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2021/5/27 17:47
 * @Version 1.0
 */
@Data
public class AvailableInfoDTO {
    private List<MwHistoryDTO> historyDTOList;
    private List<AssetsAvailableDTO> colorData;
    private String availablePer;
}
