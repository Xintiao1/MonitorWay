package cn.mw.monitor.virtualization.dto;

import cn.mw.monitor.common.util.VirtualTableDto;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/7/1 16:16
 * @Version 1.0
 */
@Data
public class TableListDto {

    private List<HostTableDto> hostTableDtos;
    private List<VirtualTableDto> virtualTableDtos;
    private List<DataStoreTableDto> storeTableDtos;
    private List<String> tableNames;
}
