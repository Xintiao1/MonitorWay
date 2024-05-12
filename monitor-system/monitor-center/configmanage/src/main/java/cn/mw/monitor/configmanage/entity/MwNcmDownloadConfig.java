package cn.mw.monitor.configmanage.entity;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;

@Data
public class MwNcmDownloadConfig extends BaseParam {

    private String id;

    private String assetsId;

    private String name;

    private String path;

    private String configType;

    private String creator;

    private Date createDate;

    private String context;

}
