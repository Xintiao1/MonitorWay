package cn.mw.monitor.model.data;

import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.service.assets.param.AddUpdateTangAssetsParam;
import lombok.Data;

import java.util.ArrayList;

@Data
public class AddUpdModelInstanceContext {
    private AddAndUpdateModelInstanceParam addAndUpdateModelInstanceParam;
    private AddUpdateTangAssetsParam addUpdateTangAssetsParam;
}
