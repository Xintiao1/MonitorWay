package cn.mw.monitor.model.view;

import cn.mw.monitor.model.dto.ModelInstanceDto;
import cn.mw.monitor.model.dto.VirtualInstance;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.virtual.dto.VirtualizationType;
import cn.mwpaas.common.utils.BeansUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class VirtualView{
    private String Pid;
    private String esId;
    private String id;
    private String instanceName;
    private String modelId;
    private String modelIndex;
    private String modelInstanceId;
    private String type;
    private List<VirtualView> children;

    public void addChildren(VirtualView child){
        if(null == children){
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public void extractInfo(ModelInfo modelInfo , ModelInstanceDto instanceInfo , VirtualInstance virtualInstance){
        this.Pid = virtualInstance.getPid();
        this.modelIndex = modelInfo.getModelIndex();
        this.esId = modelInfo.getModelIndex();
        if(StringUtils.isEmpty(virtualInstance.getId())){
            this.id = virtualInstance.getAssetsId();
        }else{
            this.id = virtualInstance.getId();
        }

        this.instanceName = instanceInfo.getInstanceName();

        //vcenter添加时,并不会把type加入es,需要通过specification转换
        if(StringUtils.isEmpty(virtualInstance.getType()) && StringUtils.isNotEmpty(virtualInstance.getSpecifications())){
            if(VirtualizationType.VCENTER.name().equals(virtualInstance.getSpecifications())){
                this.type = VirtualizationType.VCENTER.getType();
            }
        }else {
            this.type = virtualInstance.getType();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VirtualView that = (VirtualView) o;
        return Objects.equals(modelId, that.modelId) &&
                Objects.equals(modelInstanceId, that.modelInstanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelId, modelInstanceId);
    }

    public VirtualView clone(){
        VirtualView virtualView = new VirtualView();
        BeansUtils.copyProperties(this ,virtualView);
        virtualView.setChildren(new ArrayList<>());

        if(null != children && children.size() > 0){
            for(VirtualView child : children){
                VirtualView cloneChild = child.clone();
                virtualView.addChildren(cloneChild);
            }
        }
        return virtualView;
    }
}
