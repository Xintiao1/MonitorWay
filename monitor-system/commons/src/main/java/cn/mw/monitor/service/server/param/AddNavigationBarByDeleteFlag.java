package cn.mw.monitor.service.server.param;

import lombok.Data;

/**
 * 新增删除标识
 * @author qzg
 * @date 2022/11/15
 */
@Data
public class AddNavigationBarByDeleteFlag {
    private Integer id;
    private Integer barId;
    private String barName;
    private String templateId;
    private String assetsId;
    private Boolean defaultFlag;
}
