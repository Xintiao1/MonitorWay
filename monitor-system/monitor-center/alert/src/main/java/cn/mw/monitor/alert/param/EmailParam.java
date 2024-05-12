package cn.mw.monitor.alert.param;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * @author xhy
 * @date 2020/8/13 16:26
 *
 */
@Data
public class EmailParam {
    private String ruleId;
    private Boolean isSsl;
    private Boolean isSmtp;
   @Size(max=128,message = "最大长度不能超过128")
    private String emailServerAddress;
   @Size(max=128,message = "最大长度不能超过128")
    private String emailServerPort;
   @Size(max=128,message = "最大长度不能超过128")
    private String emailSendUserName;
   @Size(max=128,message = "最大长度不能超过128")
    private String emailSendPassword;
   @Size(max=128,message = "最大长度不能超过128")
    private String personal;
   private String emailHeaderTitle;
   private String logo;
   private String url;
   private Boolean isLogo;
   private Boolean isDelsuffix;
}
