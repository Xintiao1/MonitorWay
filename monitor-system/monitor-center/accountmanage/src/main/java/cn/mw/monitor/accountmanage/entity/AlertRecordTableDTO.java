package cn.mw.monitor.accountmanage.entity;

import lombok.Data;

import java.util.Date;

@Data
public class AlertRecordTableDTO {
    private Date date;
    private String method;
    private String text;
    private int isSuccess;
    private String hostid;
    private String error;
    //private String title;
    //private String ip;
    private String userName;
    private String resultState;
    private Integer id;
    private Integer userId;
    private String eventId;

    public String toString(){
        return "date=" + date
                + ",method="+ method
                + ",text="+ text
                + ",isSuccess="+ isSuccess
                + ",hostid="+ hostid
                + ",error="+ error
                + ",userName="+ userName
                + ",resultState="+ resultState
                + ",id="+ id
                + ",userId="+ userId
                + ",eventId="+ eventId;
    }
}
