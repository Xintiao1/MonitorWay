package cn.mw.monitor.service.scan.param;

public class SnmpSearchAction {
    public static String ASSETS = "ASSETS";

    private boolean[] actionEnable;

    public boolean[] getActionEnable() {
        return actionEnable;
    }

    public void setActionEnable(boolean[] actionEnable) {
        this.actionEnable = actionEnable;
    }

    public void enableTest(){
        actionEnable = new boolean[]{true,true,true,true,false,false,false,false,true,true};
    }

    public void enableIpInfo(){
        actionEnable = new boolean[]{false,false,false,true,false,false,false,false,false,false};
    }

    //只获得接口信息
    public void enableInterfaceInfo(){
        actionEnable = new boolean[]{true,false,false,false,false,false,false,false,false,false};
    }

    //获取接口以及路由信息
    public void enableInterfaceRouteInfo(){
        actionEnable = new boolean[]{true,false,false,true,true,false,false,false,false,false};
    }

    //获取接口,mac表,路由,接口额外信息
    public void enableInterfaceMacRouteInfo(){
        actionEnable = new boolean[]{true,false,true,true,true,false,false,false,true,false};
    }

    //获取接口, arp表, mac表,接口额外信息, vlan信息
    public void enableIfArpMapVlan(){
        actionEnable = new boolean[]{true,true,true,true,false,false,false,false,true,true};
    }

    //拓扑搜索信息
    public void enableTopoInfo(){
        actionEnable = new boolean[]{true,true,true,true,true,true,true,true,false,false};
    }

    //mac转发表等搜索拓扑
    public void enableMacTopoInfo(){
        actionEnable = new boolean[]{true ,true ,true ,false ,false ,true ,true ,false,false,false};
    }

    //搜索路由信息
    public void enableRouteInfo(){
        actionEnable = new boolean[]{false,false,false,true,false,false,false,false,false,false};
    }

    //获取接口vlan信息
    public void enableInterfaceVlan(){
        actionEnable = new boolean[]{false,false,false,false,false,false,false,false,true,false};
    }

    public void mergeAction(SnmpSearchAction snmpSearchAction){
        boolean[] dest = snmpSearchAction.getActionEnable();
        boolean[] value = new boolean[actionEnable.length];
        for(int i = 0 ; i< actionEnable.length ;i++){
            value[i] = actionEnable[i] || dest[i];
        }
        actionEnable = value;
    }
}
