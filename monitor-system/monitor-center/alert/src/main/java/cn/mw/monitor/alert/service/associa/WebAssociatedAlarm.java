package cn.mw.monitor.alert.service.associa;

import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.alert.service.associa.AssociatedAlarm;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2021/1/29 16:56
 */
public class WebAssociatedAlarm extends AssociatedAlarm {

    public WebAssociatedAlarm(MwTangibleassetsDTO mwTangibleassetsDTO, Boolean isActive) {
        super(mwTangibleassetsDTO, isActive);
    }

    public WebAssociatedAlarm(MwTangibleassetsDTO mwTangibleassetsDTO) {
        super(mwTangibleassetsDTO, true);
    }

    @Override
    public String getAssociatedAlarm() {
        StringBuffer sb = new StringBuffer();
        synchronized (sb){
            List<Map> webmap = assetsDao.getWebMonitor(mwTangibleassetsDTO.getId());
            if (null != webmap && webmap.size() > 0) {
                sb.append("关联Web监测:[");
                for (int i = 0; i < webmap.size(); i++) {
                    sb.append(webmap.get(i).get("webName").toString()).append(":").append(webmap.get(i).get("webUrl").toString()).append(",");
                    if (i == 2) {
                        sb.append("...");
                        sb.append("等" + webmap.size() + "条Web名称");
                        break;
                    }
                }
                sb.append("]");
            }
            return sb.toString();
        }
    }
}
