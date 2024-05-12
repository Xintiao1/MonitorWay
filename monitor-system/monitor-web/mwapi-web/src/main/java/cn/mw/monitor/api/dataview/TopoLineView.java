package cn.mw.monitor.api.dataview;


import cn.mw.monitor.service.server.api.dto.HostIfPerformanceInfo;
import cn.mw.monitor.service.server.api.dto.HostPerformanceInfoDto;
import cn.mw.monitor.snmp.param.scan.ViewTopoIfParam;
import cn.mw.monitor.topology.model.NetFlowEntry;
import cn.mw.monitor.topology.param.LineColorParam;
import cn.mwpaas.common.utils.StringUtils;
import lombok.Data;

import java.util.*;

@Data
public class TopoLineView {
    //线索引
    private int index;
    private int upGraphIndex;
    private String upDevice;
    private String upIp;
    private int upMonitorServerId;
    private String upHostId;

    private int downGraphIndex;
    private String downDevice;
    private String downIp;
    private int downMonitorServerId;
    private String downHostId;

    //接口table信息
    private int total;
    private int pageSize;
    private int pageNumber;
    private List<TopoIfView> ifList = new ArrayList<>();

    //接口监控规则
    private LineColorParam linecolor;

    public void extractData(List<HostPerformanceInfoDto> list , List<ViewTopoIfParam> upDownIfMap ,Map<Integer
            ,List<NetFlowEntry>> netFlowMap){

        Map<String, HostIfPerformanceInfo> ifMap = new HashMap<>();

        for(HostPerformanceInfoDto hostPerformanceInfoDto:list){
            for(HostIfPerformanceInfo hostIfPerformanceInfo:hostPerformanceInfoDto.getIfPerformanceInfoList()){
                if(StringUtils.isEmpty(hostPerformanceInfoDto.getHostId())
                || StringUtils.isEmpty(hostIfPerformanceInfo.getIfName())){
                    continue;
                }

                String key = hostPerformanceInfoDto.getHostId() + hostIfPerformanceInfo.getIfName();
                ifMap.put(key ,hostIfPerformanceInfo);
            }
        }

        for(ViewTopoIfParam viewTopoIfParam : upDownIfMap){
            TopoIfView topoIfView = new TopoIfView();
            topoIfView.setId(viewTopoIfParam.getId());
            topoIfView.setConLevel(viewTopoIfParam.getConLevel());
            String upKey = upHostId + viewTopoIfParam.getUpIfName();

            HostIfPerformanceInfo upIfInfo = ifMap.get(upKey);
            if(null == upIfInfo){
                upIfInfo = new HostIfPerformanceInfo();
            }
            IfView upIf = new IfView();
            upIf.setIfName(viewTopoIfParam.getUpIfName());
            upIf.extractData(upIfInfo);
            topoIfView.setUpIf(upIf);

            String downKey = downHostId + viewTopoIfParam.getDownIfName();
            HostIfPerformanceInfo downIfInfo = ifMap.get(downKey);
            if(null == downIfInfo){
                downIfInfo = new HostIfPerformanceInfo();
            }
            IfView downIf = new IfView();
            downIf.setIfName(viewTopoIfParam.getDownIfName());
            downIf.extractData(downIfInfo);
            topoIfView.setDownIf(downIf);

            List<NetFlowEntry> netFlowEntries = netFlowMap.get(topoIfView.getId());
            if (null != netFlowEntries && netFlowEntries.size() > 0) {
                //设置是否显示流量信息
                extractFlowInfo(topoIfView, netFlowEntries);
            }

            ifList.add(topoIfView);
        }

    }

    private void extractFlowInfo(TopoIfView topoIfView , List<NetFlowEntry> netFlowEntries){
        Set<String> netFlowEntrieSet = new HashSet<>();

        for(NetFlowEntry netFlowEntry : netFlowEntries){
            netFlowEntrieSet.add(netFlowEntry.getIfType() + netFlowEntry.getIfName());
        }

        String key = NetFlowEntry.UP_IF + topoIfView.getUpIf().getIfName();
        if(netFlowEntrieSet.contains(key)){
            topoIfView.setUpFlow(true);
        }

        key = NetFlowEntry.DOWN_IF + topoIfView.getDownIf().getIfName();
        if(netFlowEntrieSet.contains(key)){
            topoIfView.setDownFlow(true);
        }
    }

    @Override
    public String toString() {
        return "TopoLineView{" +
                "upDevice='" + upDevice + '\'' +
                ", upIp='" + upIp + '\'' +
                ", downDevice='" + downDevice + '\'' +
                ", downIp='" + downIp + '\'' +
                '}';
    }

    public TopoLineView() {
        this.upDevice = "";
        this.upIp = "";
        this.downDevice = "";
        this.downIp = "";
    }
}
