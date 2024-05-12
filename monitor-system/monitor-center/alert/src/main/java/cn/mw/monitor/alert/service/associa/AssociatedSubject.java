package cn.mw.monitor.alert.service.associa;

import cn.mwpaas.common.utils.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xhy
 * @date 2021/1/29 17:04
 */
@Component
public class AssociatedSubject {
    private List<AssociatedAlarm> alarmList = new ArrayList<>();
    private Boolean state;//判断是否添加告警关联信息

    public void addAssociatedAlarm(AssociatedAlarm associatedAlarm) {
        alarmList.add(associatedAlarm);
    }

    //通知所有关联模块
    public String notifyAllAssociated() {
        StringBuffer sb = new StringBuffer();
        for (AssociatedAlarm associatedAlarm : alarmList) {
            if (associatedAlarm.isActive) {
                String associatedAlarm1 = associatedAlarm.getAssociatedAlarm();
                if (StringUtils.isNotEmpty(associatedAlarm1)) {
                    sb.append(associatedAlarm1).append('\n');
                }
            }
        }
        return sb.toString();
    }


}
