package cn.mw.monitor.service.netflow.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author guiquanwnag
 * @datetime 2023/8/28
 * @Description 查询参数
 */
@Data
public class NetflowSearchParam extends BaseParam implements Serializable  {

    /**
     * 资产ID列表
     */
    private List<String> assetsId;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

}
