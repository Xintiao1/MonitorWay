package cn.mw.monitor.model.param;

import cn.mw.monitor.service.assets.model.GroupDTO;
import cn.mw.monitor.service.assets.model.UserDTO;
import cn.mw.monitor.service.user.dto.OrgDTO;
import cn.mw.monitor.webMonitor.model.MwWebmonitorTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/4/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class MwModelWebMonitorDTO{

    private String webState;

    private String downloadSpeed;

    private String monitorCode;

    private String responseTime;

    private String fullTimeOut;

    private String monitorServer;

    private String assetsName;

    private String inBandIp;

    private String ipType;

    private Integer monitorServerId;

}
