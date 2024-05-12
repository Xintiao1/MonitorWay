package cn.mw.monitor.user.advcontrol;

import java.util.List;

public class AllNotSatisfy extends ConditionBase {
    @Override
    public boolean isPassed(RequestInfo requestInfo) {
        List<UserControlStra> strategyList = getUserControlStra();
        boolean ispassed = true;
        loop:
        for(UserControlStra strategy : strategyList){
            ControlType type = strategy.getControlType();
            switch (type) {
                case IP:
                    IPMessage ipMessage = new IPMessage(requestInfo.getIp());
                    ispassed = strategy.check(ipMessage);
                    if (ispassed) {
                        break loop;
                    }
                    continue;
                case MAC:
                    MacMessage macMessage = new MacMessage(requestInfo.getMac());
                    ispassed = strategy.check(macMessage);
                    if (ispassed) {
                        break loop;
                    }
                    continue;
                case TIME:
                    TimeMessage timeMessage = new TimeMessage(requestInfo.getTime());
                    ispassed = strategy.check(timeMessage);
                    if (ispassed) {
                        break loop;
                    }
                    continue;
                default:
            }
        }

        if(ispassed){
            return false;
        }

        return true;
    }
}
