package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.event.Event;
import cn.mw.monitor.user.service.IUserPermission;
import cn.mw.monitor.event.UpdPermitEvent;
import org.springframework.stereotype.Component;

@Component
public class UserPermissionProcesser implements IUserPermission {

    @Override
    public boolean ispermitted(Event event) {
        if(event instanceof UpdPermitEvent){
            UpdPermitEvent updPermitEvent = (UpdPermitEvent)event;
            if(!updPermitEvent.getSrcLoginName().equals(updPermitEvent.getDestLoginName())){
                return true;
            }
        }

        return false;
    }
}
