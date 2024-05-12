package cn.mw.monitor.user.advcontrol;

import lombok.Data;

import java.util.Arrays;

@Data
public class MacStrategy implements UserControlStra<MacMessage>{
    private String macRule;
    private  ControlType controlType = ControlType.MAC;

    public MacStrategy(String rule){
        this.macRule = rule;
    }
    @Override
    public boolean check(MacMessage macMessage) {
        return true;
        /*String mac = macMessage.getMac();

        //访问标识，默认为false，限制访问
        boolean flag = false;

        String[] allowMacRanges = macRule.split(",");

        if (Arrays.asList(allowMacRanges).contains(mac)) {
            flag = true;
        }
        return flag;*/
    }
}
