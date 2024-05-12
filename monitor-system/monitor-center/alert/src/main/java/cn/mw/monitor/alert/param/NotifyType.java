package cn.mw.monitor.alert.param;

import cn.mw.monitor.util.RelationTypeEnum;

public enum NotifyType {
    ChooseUser(0) ,Default(1) ,Custom(2);
    private int code;

    NotifyType(int code){
        this.code = code;
    }

    public static NotifyType getNotifyType(int code){
        for (NotifyType s : NotifyType.values()) {
            if(code == s.code){
                return  s;
            }
        }
        return null;
    }
}
