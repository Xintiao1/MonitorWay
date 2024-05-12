package cn.mw.monitor.service.alert.param;


import lombok.Data;
import java.util.Date;
import java.util.List;


/**
 * @author
 * @date
 */
@Data
public class AssetsStatusQueryParam {
    private List<String> hostids;
    private Date startTime;//开始时间
    private Date endTime;//结束时间
}
