package cn.mw.monitor.user.dto;

import lombok.Data;

import java.util.List;

/**
 * @ClassName MwOrgLongitudeDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/6/7 10:59
 * @Version 1.0
 **/
@Data
public class MwOrgLongitudeDto {

    //区域ID
    private int id;

    private int parentId;

    //行政区域等级 1-省 2-市 3-区县 4-街道镇
    private int level;

    //名称
    private String name;

    //完整名称
    private String wholeName;

    //本区域经度
    private String lon;

    //本区域维度
    private String lat;

    //电话区号
    private String cityCode;

    //邮政编码
    private String zipCode;

    //行政区划代码
    private String areaCode;

    private List<MwOrgLongitudeDto> children;

    private String coordinate;

    private boolean leaf;

}
