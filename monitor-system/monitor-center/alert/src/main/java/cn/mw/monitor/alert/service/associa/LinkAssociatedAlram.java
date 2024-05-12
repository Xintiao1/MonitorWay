package cn.mw.monitor.alert.service.associa;

import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2021/1/29 16:59
 */
public class LinkAssociatedAlram extends AssociatedAlarm {

    private static final Logger log = LoggerFactory.getLogger("LinkAssociatedAlram");

    public LinkAssociatedAlram(MwTangibleassetsDTO mwTangibleassetsDTO, Boolean isActive) {
        super(mwTangibleassetsDTO, isActive);
    }
    public LinkAssociatedAlram(MwTangibleassetsDTO mwTangibleassetsDTO) {
        super(mwTangibleassetsDTO,true);
    }

    @Override
    public String getAssociatedAlarm() {
        StringBuffer sb = new StringBuffer();
        synchronized (sb){
            List<Map> linkmap = assetsDao.getLink(mwTangibleassetsDTO.getAssetsId(), mwTangibleassetsDTO.getMonitorServerId(),mwTangibleassetsDTO.getInBandIp());
            log.info("关联线路名称：" + linkmap);
            log.info("关联线路名称getAssetsId：" + mwTangibleassetsDTO.getAssetsId());
            log.info("关联线路名称getMonitorServerId：" + mwTangibleassetsDTO.getMonitorServerId());
            if (null != linkmap && linkmap.size() > 0) {
                sb.append("关联线路名称:[");
                for (int i = 0; i < linkmap.size(); i++) {
                    sb.append(linkmap.get(i).get("linkName").toString()).append(",");
                    if (i == 2) {
                        sb.append("...");
                        sb.append("等"+linkmap.size()+"条线路名称");
                        break;
                    }
                }
                sb.append("]");
            }
            return sb.toString();
        }
    }
}
