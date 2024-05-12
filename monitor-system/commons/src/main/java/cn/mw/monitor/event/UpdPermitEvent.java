package cn.mw.monitor.event;

import lombok.Data;

@Data
public class UpdPermitEvent<T> extends Event<T> {
    private String srcLoginName;
    private String destLoginName;

    public UpdPermitEvent(String srcLoginName, String destLoginName){
        this.srcLoginName = srcLoginName;
        this.destLoginName = destLoginName;
    }
}
