package cn.mw.monitor.ipaddressmanage.param;

import lombok.Data;

@Data
public class AddUpdatePortInfoParam extends BaseAddUpdatePortInfoParam{
    private int id;
    private int ipManageListId;
}
