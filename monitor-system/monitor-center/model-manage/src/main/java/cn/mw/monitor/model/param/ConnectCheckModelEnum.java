package cn.mw.monitor.model.param;

import java.util.ArrayList;
import java.util.List;

/**
 * 模型管理操作类型
 */
public enum ConnectCheckModelEnum {
    VCENTER("VCenter", 25),
    CITRIXADC("Citrix ADC", 21),
    RANCHER("Rancher", 22),
    //刀箱服务器
    HPCHASSIS("HPChassis", 69),
    SUPERFUSION("superFusion", 272);
    private String name;
    private Integer modelId;

    public Integer getModelId() {
        return modelId;
    }

    public void setModelId(Integer modelId) {
        this.modelId = modelId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    ConnectCheckModelEnum(String name, Integer modelId) {
        this.name = name;
        this.modelId = modelId;
    }

    public static ConnectCheckModelEnum valueOf(Integer modelId) {
        for (ConnectCheckModelEnum checkModelEnum : ConnectCheckModelEnum.values()) {
            if (modelId.equals(checkModelEnum.getModelId())) {
                return checkModelEnum;
            }
        }
        return null;
    }

    public static List<Integer> getAllModelIds() {
        List<Integer> modelIds = new ArrayList<>();
        for (ConnectCheckModelEnum checkModelEnum : ConnectCheckModelEnum.values()) {
            modelIds.add(checkModelEnum.getModelId());
        }
        return modelIds;
    }
}
