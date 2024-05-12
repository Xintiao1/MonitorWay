package cn.mw.monitor.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by zy.quaee on 2021/5/8 15:06.
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ADAuthenticSuccDTO {
    private Date updateTime;
    private List<MWADInfoDTO> adInfos;
}
