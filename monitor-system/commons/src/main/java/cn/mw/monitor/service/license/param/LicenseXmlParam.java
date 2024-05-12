package cn.mw.monitor.service.license.param;

import lombok.Data;


@Data
public class LicenseXmlParam {
    private String moduleType;
    private String moduleName;
    private String moduleId;
    //描述
    String describe;
    //许可数量
    Integer count;
    //过期时间
    private String expireDate;
    //主机信息
    private String hostMsg;
    //是否停用
    private boolean stopIs=false;
    //成功标识
    private int code =0;
    //返回信息
    private String msg ="SUCESS";
    //
    private Integer state;

    private Integer usedCount;

    private String imgUrl;
}
