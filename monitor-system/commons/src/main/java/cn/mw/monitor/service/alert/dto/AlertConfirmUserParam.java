package cn.mw.monitor.service.alert.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author
 * @date
 */
@Data
public class AlertConfirmUserParam {
   private String eventId;
   private Integer monitorServerId;
   private Integer userId;
   private String userName;
   private String type;
   private Date confirmDate;
   private String phoneNumber;
   private String orgName;

}
