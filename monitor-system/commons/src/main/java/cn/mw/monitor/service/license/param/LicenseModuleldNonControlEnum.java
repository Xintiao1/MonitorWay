package cn.mw.monitor.service.license.param;

import java.util.Arrays;
import java.util.List;

public enum LicenseModuleldNonControlEnum {

    NON_CONTROL( Arrays.asList(23,24,25,142,36,38,40,41,42,43,44,49,214,241));

    private List<Integer> moduleIds;
    LicenseModuleldNonControlEnum( List<Integer> moduleIds){

        this.moduleIds = moduleIds;
    }

    public List<Integer> getModuleIds(){
        return moduleIds;
    }

}
