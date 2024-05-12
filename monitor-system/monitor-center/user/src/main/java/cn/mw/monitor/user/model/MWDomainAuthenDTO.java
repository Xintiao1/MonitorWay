package cn.mw.monitor.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 认证的IP + 端口号  以及返回的部门信息
 * Created by zy.quaee on 2021/4/28 9:21.
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MWDomainAuthenDTO {

    private String adServerIpAdd;
    private String adPort;
    private String adServerName;
    private Date updateTime;
    private List<MWADInfoDTO> adInfos;
    private String domainName;
}
