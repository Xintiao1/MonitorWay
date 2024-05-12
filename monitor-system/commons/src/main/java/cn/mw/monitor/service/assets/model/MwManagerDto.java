package cn.mw.monitor.service.assets.model;

import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/6/3 14:53
 */
@Data
public class MwManagerDto  {
    private List<Integer> groupIds;
    private List<Integer> orgIds;
    private Integer userId;
    private Integer assetsTypeId;
    private String assetsId;
    private Boolean isAdmin;
    private String id;
}
