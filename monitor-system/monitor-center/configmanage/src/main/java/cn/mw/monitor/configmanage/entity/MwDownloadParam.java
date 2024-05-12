package cn.mw.monitor.configmanage.entity;

import lombok.Data;

import java.util.List;


@Data
public class MwDownloadParam {

    private List<MwTangibleassetsTable> param;
    private String configType;
    private String cmds;

}
