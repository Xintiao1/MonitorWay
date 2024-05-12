package cn.mw.monitor.assets.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author baochengbin
 * @date 2020/4/22
 */
@Data
@Builder
public class MwAssetsUserMapper {
    private Integer userId;

    private String assetsId;
}
