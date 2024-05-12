package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.data.InstanceSyncContext;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.ConnectCheckModelEnum;
import cn.mw.monitor.model.param.MwModelMacrosValInfoParam;
import cn.mw.monitor.model.param.virtual.QueryVirtualInstanceParam;
import cn.mw.monitor.model.service.MwModelVirtualizationService;
import cn.mw.monitor.model.type.MwModelMacrosValInfoParamType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MwModelInstanceSyncManager {

    @Autowired
    private MwModelVirtualizationService mwModelVirtualizationService;

    public String findProxyId(AddAndUpdateModelInstanceParam param){
        if(param.isSync() && null!= param.getSyncParams()){
            for(MwModelMacrosValInfoParam mwModelMacrosValInfoParam : param.getSyncParams()){
                if(MwModelMacrosValInfoParamType.EngineSel.getCode().equals(mwModelMacrosValInfoParam.getMacroType())){
                    return mwModelMacrosValInfoParam.getMacroVal();
                }
            }
        }
        return null;
    }

    public void sync(InstanceSyncContext syncContext){
        AddAndUpdateModelInstanceParam param = syncContext.getParam();

        //如果vcenter则需要同步vcenter信息
        try {
            if (ConnectCheckModelEnum.VCENTER.getModelId().equals(param.getModelId())) {
                QueryVirtualInstanceParam vitualParam = new QueryVirtualInstanceParam();
                vitualParam.extractFrom(param);
                mwModelVirtualizationService.syncVirtualDeviceInfo(vitualParam);
            }
        }catch (Exception e){
            log.error("sync" ,e);
        }
    }
}
