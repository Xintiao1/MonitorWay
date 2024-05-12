package cn.mw.monitor.api.param.aduser;

import cn.mw.monitor.bean.BaseParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author gui.quanwang
 * @className SyncUserParam
 * @description 同步LDAP用户请求参数
 * @date 2021/10/14
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncUserParam extends BaseParam {

    /**
     * 是否启用定时任务：1：启用：0：不启用
     */
    private int useFlag;

    /**
     * 当传立即同步时，其他参数不生效(1:立即同步)
     */
    private int syncNow;

    /**
     * 间隔时间
     */
    private long intervalTime;

    /**
     * 间隔类别（1：秒 2：分 3：小时  4：天）
     */
    private int timeUnit;
}
