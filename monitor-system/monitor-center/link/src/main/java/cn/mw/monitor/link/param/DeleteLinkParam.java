package cn.mw.monitor.link.param;

import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2021/7/12 15:13
 * @Version 1.0
 */
@Data
public class DeleteLinkParam {
    private List<AddAndUpdateParam> addAndUpdateParams;
    //是否删除ICMP 线路资产
    private boolean deleteICMPFlag;
}
