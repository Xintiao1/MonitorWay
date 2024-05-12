package cn.mw.monitor.model.view;

import cn.mw.monitor.model.dto.ModelInstanceDto;
import cn.mw.monitor.model.dto.rancher.RancherInstance;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mwpaas.common.utils.BeansUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.mw.monitor.model.param.MatchModelTypeEnum.RANCHER;

@Data
public class RancherView {
    private String PId;
    private String esId;
    private String id;
    private String instanceName;
    private String modelId;
    private String modelIndex;
    private String modelInstanceId;
    private String type;
    private String clusterId;//namespace查询时使用
    private List<RancherView> children;

    public void addChildren(RancherView child) {
        if (null == children) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public void extractInfo(ModelInfo modelInfo, ModelInstanceDto instanceInfo, RancherInstance rancherInstance) {
        if (rancherInstance.getPId() == null) {
            this.PId = "-1";
        } else {
            this.PId = rancherInstance.getPId();
        }
        this.modelIndex = modelInfo.getModelIndex();
        this.clusterId = rancherInstance.getClusterId();
        this.esId = modelInfo.getModelIndex();
        if (StringUtils.isEmpty(rancherInstance.getId())) {
            this.id = rancherInstance.getAssetsId();
        } else {
            this.id = rancherInstance.getId();
        }

        this.instanceName = instanceInfo.getInstanceName();

        if (rancherInstance.getModelId() != null) {
            if (RANCHER.getModelId().intValue() == rancherInstance.getModelId().intValue()) {
                this.type = RANCHER.getType();
            } else {
                this.type = rancherInstance.getType();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RancherView that = (RancherView) o;
        return Objects.equals(modelId, that.modelId) &&
                Objects.equals(modelInstanceId, that.modelInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, modelInstanceId);
    }

    public RancherView clone() {
        RancherView virtualView = new RancherView();
        BeansUtils.copyProperties(this, virtualView);
        virtualView.setChildren(new ArrayList<>());

        if (null != children && children.size() > 0) {
            for (RancherView child : children) {
                RancherView cloneChild = child.clone();
                virtualView.addChildren(cloneChild);
            }
        }
        return virtualView;
    }
}
