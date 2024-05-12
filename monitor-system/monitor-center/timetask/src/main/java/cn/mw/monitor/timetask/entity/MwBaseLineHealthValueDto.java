package cn.mw.monitor.timetask.entity;

import lombok.Data;

import java.util.List;

/**
 * @ClassName MwBaseLineHealthValueCommonsDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/4/6 15:26
 * @Version 1.0
 **/
@Data
public class MwBaseLineHealthValueDto {

    private Integer id;

    private String assetsId;

    private String itemName;

    //带单位的值
    private String value;

    private List<String> names;

    private String assetsName;

    private String macro;

    private Integer serverId;

    //不带单位的值
    private Double dValue;
}
