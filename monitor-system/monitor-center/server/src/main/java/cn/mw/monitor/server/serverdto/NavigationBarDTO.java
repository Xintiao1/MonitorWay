package cn.mw.monitor.server.serverdto;

import cn.mwpaas.common.utils.UUIDUtils;
import lombok.Data;

/**
 * @author syt
 * @Date 2021/5/8 16:36
 * @Version 1.0
 */
@Data
public class NavigationBarDTO {
    //详情页导航栏id （初始化的id都是0）
    private int navigationBarId;
    //详情页导航栏名称
    private String navigationBarName;
    //zabbix模板id
    private String templateId;
    //唯一标识
    private String uuid;
    private Boolean flag;

    private int customNavigationBarId;

    public NavigationBarDTO(int navigationBarId, String navigationBarName) {
        this.navigationBarId = navigationBarId;
        this.navigationBarName = navigationBarName;
        this.uuid = UUIDUtils.getUUID();
    }

    public NavigationBarDTO() {
        this.uuid = UUIDUtils.getUUID();
    }
}
