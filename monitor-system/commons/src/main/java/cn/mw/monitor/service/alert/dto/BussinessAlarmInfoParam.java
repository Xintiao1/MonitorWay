package cn.mw.monitor.service.alert.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author
 * @date
 */
@Data
public class BussinessAlarmInfoParam {
   private String status;
   private String content;
   private String dbid;
   private String tableContent;
   private String ip;
   private String bussinessName;
   private String alarmLevel;
   private String alarmEventName;
   private Date createTime;
   private String isSend;
   private String severity;
   private String modelSystem;
   private String modelClassify;
   private String objectName;
   private String alertType;
}
