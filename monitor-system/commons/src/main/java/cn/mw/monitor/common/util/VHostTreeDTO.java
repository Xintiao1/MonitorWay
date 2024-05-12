package cn.mw.monitor.common.util;

import cn.mwpaas.common.utils.UUIDUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author syt
 * @Date 2020/12/9 17:29
 * @Version 1.0
 */
@Data
public class VHostTreeDTO {
    private int monitorServerId;
    private String monitorServerName;
    private String ip;
    private String selectIp;
    private String id;
    private String label;
    private String pId;

    private String assetHostId;
    //前面小图标名字信息
    private String url;
    //    用于区分是存储还是虚拟化 "vHost"是虚拟化；"store"是存储
    private String flag;
    // 用于前端特殊展示的唯一标识
    private String uuid;
    //每级对应展示的主机
    private List<GroupHosts> hostList;
    //每级对应展示的虚拟机
    private List<GroupHosts> vmList;
    //每级对应展示的存储数据
    private List<GroupHosts> storeList;

    private List<VHostTreeDTO> children;

    private Vector<String> hostIdStr;//主机Id集合

    private Vector<String> vmIdStr;//虚拟机id集合

    public void addChild(VHostTreeDTO vHostTreeDTO) {
        if (null == children) {
            children = new ArrayList<VHostTreeDTO>();
        }
        children.add(vHostTreeDTO);
    }

    public void addChildren(List<VHostTreeDTO> vHostTreeDTOS) {
        if (null == children) {
            children = new ArrayList<VHostTreeDTO>();
        }
        children.addAll(vHostTreeDTOS);
    }

    public VHostTreeDTO() {
        uuid = UUIDUtils.getUUID();
    }

    public void addHost(GroupHosts groupHosts) {
        if (null == hostList) {
            hostList = new ArrayList<GroupHosts>();
        }
        hostList.add(groupHosts);
    }

    public void addHostList(List<GroupHosts> groupHostsList) {
        if (null == hostList) {
            hostList = new ArrayList<GroupHosts>();
        }
        if (groupHostsList != null && groupHostsList.size() > 0) {
            hostList.addAll(groupHostsList);
        }
    }

    public void addVm(GroupHosts groupHosts) {
        if (null == vmList) {
            vmList = new ArrayList<GroupHosts>();
        }
        vmList.add(groupHosts);
    }

    public void addVmList(List<GroupHosts> list) {
        if (null == vmList) {
            vmList = new ArrayList<GroupHosts>();
        }
        if (list != null && list.size() > 0) {
            vmList.addAll(list);
        }
    }

    public void addStore(GroupHosts storeName) {
        if (null == storeList) {
            storeList = new ArrayList<GroupHosts>();
        }
        storeList.add(storeName);
    }

    public void addStoreList(List<GroupHosts> storeNameList) {
        if (null == storeList) {
            storeList = new ArrayList<GroupHosts>();
        }
        if (storeNameList != null && storeNameList.size() > 0) {
            storeList.addAll(storeNameList);
        }
    }
}
