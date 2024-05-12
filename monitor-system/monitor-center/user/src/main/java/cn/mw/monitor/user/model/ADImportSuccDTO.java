package cn.mw.monitor.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by zy.quaee on 2021/5/8 14:48.
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ADImportSuccDTO {

    private Integer adCount;
    private Integer adUserCount;
}
