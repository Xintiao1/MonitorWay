package cn.mw.monitor.service.alert.dto;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.Date;

/**
 * @author
 * @date
 */
@Data
public class AlertReasonEditorParam{
   private String eventId;
   private Integer monitorServerId;
   private String triggerReason;
   private String solution;
}
