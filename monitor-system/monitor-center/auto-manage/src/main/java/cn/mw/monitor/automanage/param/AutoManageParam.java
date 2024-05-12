package cn.mw.monitor.automanage.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

/**
 * @author gui.quanwang
 * @className AutoManageParam
 * @description 自动化运维参数
 * @date 2022/4/4
 */
@Data
public class AutoManageParam extends BaseParam {


    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 服务是否有效
     */
    private Boolean serverEnable;

}
