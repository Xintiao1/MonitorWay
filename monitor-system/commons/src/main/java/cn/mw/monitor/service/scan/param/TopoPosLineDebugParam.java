package cn.mw.monitor.service.scan.param;

import lombok.Data;

@Data
public class TopoPosLineDebugParam {

    private String from;

    Integer index;

    Integer startIndex;

    String startIfDesc;

    String startConLevel;

    Integer endIndex;

    String endIfDesc;

    String endConLevel;

}
