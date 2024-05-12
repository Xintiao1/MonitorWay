package cn.mw.monitor.model.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.List;

/**
 * @author guiquanwnag
 * @datetime 2023/6/30
 * @Description 资产查看表格数据查询数据
 */
@Data
public class ModelTableViewParam extends BaseParam {

//    private String ipAddress;
    private String macAddress;

    private String interfaceName;

    private String vlanName;


    private String fuzzyQuery;


    private int viewType;

    /**
     * 资产ID
     */
    private int assetsId;

    /**
     * 导出的下载ID（缓存在redis）
     */
    private List<String> idList;

    private boolean fake;
}
