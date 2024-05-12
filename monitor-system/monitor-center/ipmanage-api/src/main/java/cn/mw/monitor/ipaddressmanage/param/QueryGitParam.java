package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("git地图")
public class QueryGitParam {

    //地区
    private String area;
    //详细信息
    private String info;
    //经度
    private String lng;
    //纬度
    private String lat;
    //类型
    private String type;


}
