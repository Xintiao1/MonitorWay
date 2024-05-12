package cn.joinhealth.monitor.zbx.servce;

import cn.mwpaas.common.model.Reply;

public interface ZbxGraphService {

    public Reply getZbxGraphRegexp(String hostType,String modelName);
}
