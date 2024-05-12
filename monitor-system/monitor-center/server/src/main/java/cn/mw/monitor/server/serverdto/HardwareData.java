package cn.mw.monitor.server.serverdto;

import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/5/25 11:16
 * @Version 1.0
 */
@Data
public class HardwareData {
    private String titleName;
    private List<HardwareDto> dataList;
}
