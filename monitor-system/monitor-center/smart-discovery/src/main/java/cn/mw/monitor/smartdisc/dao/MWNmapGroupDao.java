package cn.mw.monitor.smartdisc.dao;

import cn.mw.monitor.smartdisc.model.*;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

public interface MWNmapGroupDao {

    Integer insertNodeGroup(@Param("nodeName")String nodeName,@Param("detectTargetInput")String detectTargetInput);

    Integer insertPortGroup(@Param("portName")String portName,@Param("tcpPort")String tcpPort,@Param("udpPort") String udpPort);

    Integer insertFingerDetectGroup(@Param("input")String input);

    Integer insertIpLiveDetectGroup(@Param("ipLiveName")String ipLiveName,@Param("input")String input);

    Integer insertExceptionIPGroup(@Param("exceptionIp")String exceptionIp,@Param("input")String input);

    List<MWNmapFingerNodeGroup> selectFingerNodeGroup();

    List<MWNmapExceptionNodeGroup> selectExceptionNodeGroup();

    List<MWNmapPortGroup> selectPortGroup();

    List<MWNmapLiveNodeGroup> selectLiveNodeGroup();

    List<MWNmapNodeGroup> selectNodeGroup();

    MWNmapPortGroup selectPortGroupById(@Param("portGroupKey") Integer portGroupKey);

}
