package cn.mw.monitor.screen.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IndexBulk {
    private String modelDataId;
    private int bulkId;
    private String bulkName;
    private int userId;
}
