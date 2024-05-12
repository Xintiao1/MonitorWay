package cn.mw.monitor.scanrule.dto;

import cn.mw.monitor.scanrule.api.param.scanrule.AssetsScanParam;
import lombok.Builder;
import lombok.Data;

/**
 * @ClassName AssetsScanContext
 * @Description 资产扫描参数上下文
 * @Author gengjb
 * @Date 2022/9/21 9:28
 * @Version 1.0
 **/
@Data
public class AssetsScanContext {

    //扫描信息数据
    private AssetsScanParam scanParam;

    //是否保存扫描规则
    private boolean isSaveScanRuled;

    //是否获取批次号
    private boolean isRescan;

}
