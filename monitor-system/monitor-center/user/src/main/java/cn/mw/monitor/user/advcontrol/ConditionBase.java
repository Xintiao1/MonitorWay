package cn.mw.monitor.user.advcontrol;

import java.util.ArrayList;
import java.util.List;

public abstract class  ConditionBase implements Condition {
    List<UserControlStra> stralist = new ArrayList<UserControlStra>();

    public void add(UserControlStra userControlStra){
        stralist.add(userControlStra);
    }

    protected List<UserControlStra> getUserControlStra(){
        return stralist;
    }

}
