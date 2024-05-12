package cn.mw.monitor.weixin.entity;


import lombok.Data;

@Data
public class MwDatapermission {
    private Integer id;

    private String type;

    private String typeId;

    private boolean isUser;

    private boolean isGroup;

    private String description;





}
