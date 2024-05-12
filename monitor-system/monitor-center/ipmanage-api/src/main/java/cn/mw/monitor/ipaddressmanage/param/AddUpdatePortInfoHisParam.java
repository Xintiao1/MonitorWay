package cn.mw.monitor.ipaddressmanage.param;

import lombok.Data;

@Data
public class AddUpdatePortInfoHisParam extends BaseAddUpdatePortInfoParam{
    private int id;
    private int ipManageListHisId;
}
