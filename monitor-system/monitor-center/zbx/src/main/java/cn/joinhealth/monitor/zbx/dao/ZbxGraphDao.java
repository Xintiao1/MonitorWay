package cn.joinhealth.monitor.zbx.dao;

import cn.joinhealth.monitor.zbx.model.ZbxGraph;

import java.util.List;

public interface ZbxGraphDao {

    public List<ZbxGraph> selectGraph(ZbxGraph bean);
    public List<ZbxGraph> selectGraphRegexp(ZbxGraph bean);
}
