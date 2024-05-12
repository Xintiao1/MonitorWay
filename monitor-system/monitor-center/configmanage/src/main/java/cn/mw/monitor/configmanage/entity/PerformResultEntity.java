package cn.mw.monitor.configmanage.entity;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class PerformResultEntity {

    private String ip;

    private String hostName;

    private Boolean isSuccess;

    private String results;

    private String path;
}
