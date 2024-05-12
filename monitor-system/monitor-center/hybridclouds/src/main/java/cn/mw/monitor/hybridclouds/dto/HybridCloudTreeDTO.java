package cn.mw.monitor.hybridclouds.dto;

import cn.mwpaas.common.utils.UUIDUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qzg
 * @date 2021/6/8
 */
@Data
public class HybridCloudTreeDTO {
    private int monitorServerId;
    private String ip;
    private String id;
    private String label;

    private String assetHostId;
    //前面小图标名字信息
    private String url;
    //    用于区分是存储还是虚拟化 "vHost"是虚拟化；"store"是存储
    private String flag;
    // 用于前端特殊展示的唯一标识
    private String uuid;
    //每级对应展示的主机群组lsit
    private List<GroupHost> groupList;
    //每级对应展示的混合云list
    private List<GroupHosts> hcList;

    private List<HybridCloudTreeDTO> children;

    public void addChild(HybridCloudTreeDTO vHostTreeDTO) {
        if (null == children) {
            children = new ArrayList<HybridCloudTreeDTO>();
        }
        children.add(vHostTreeDTO);
    }

    public void addChildren(List<HybridCloudTreeDTO> vHostTreeDTOS) {
        if (null == children) {
            children = new ArrayList<HybridCloudTreeDTO>();
        }
        children.addAll(vHostTreeDTOS);
    }

    public HybridCloudTreeDTO() {
        uuid = UUIDUtils.getUUID();
    }

    public void addGroup(GroupHost groupHost) {
        if (null == groupList) {
            groupList = new ArrayList<GroupHost>();
        }
        groupList.add(groupHost);
    }

    public void addGroupList(List<GroupHost> groupHostList) {
        if (null == groupList) {
            groupList = new ArrayList<GroupHost>();
        }
        groupList.addAll(groupHostList);
    }

    public void addHc(GroupHosts groupHosts) {
        if (null == hcList) {
            hcList = new ArrayList<GroupHosts>();
        }
        hcList.add(groupHosts);
    }

    public void addHcList(List<GroupHosts> list) {
        if (null == hcList) {
            hcList = new ArrayList<GroupHosts>();
        }
        hcList.addAll(list);
    }
}
