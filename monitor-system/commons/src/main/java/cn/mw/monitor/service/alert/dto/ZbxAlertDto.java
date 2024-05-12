package cn.mw.monitor.service.alert.dto;

import cn.mw.monitor.common.bean.BaseDTO;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/4/15 11:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZbxAlertDto extends BaseDTO implements Comparable<ZbxAlertDto> {

    @ExcelIgnore
    private String assetsId;//资产主键id
    @ExcelIgnore
    private String monitorServerName;//监控服务器主机名称
    @ExcelProperty(value = {"事件ID"},index = 0)
    private String eventid;
    @ExcelIgnore
    private String alertid;
    @ExcelProperty(value = {"对象ID"},index = 1)
    private String objectid;
    @ExcelIgnore
    private String r_eventid;
    @ExcelProperty(value = {"告警标题"},index = 2)
    private String name;
    @ExcelProperty(value = {"告警级别"},index = 3)
    private String severity;//告警等级
    @ExcelProperty(value = {"告警对象"},index = 4)
    private String objectName;
    @ExcelProperty(value = {"告警类型"},index = 5)
    private String alertType;//主机的类型 从数据库中获取
    @ExcelProperty(value = {"IP地址"},index = 6)
    private String ip;
    @ExcelProperty(value = {"告警时间"},index = 7)
    private String clock;//告警时间

    @ExcelProperty(value = {"恢复时间"},index = 10)
    private String rclock;
    @ExcelProperty(value = {"持续时间"},index = 8)
    private String longTime;//持续时间
    @ExcelProperty(value = {"处理状态"},index = 9)
    private String acknowledged;//确认状态
    @ExcelProperty(value = {"触发原因"},index = 11)
    private String triggerReason;
    @ExcelProperty(value = {"解决方案"},index = 12)
    private String solution;
    @ExcelProperty(value = {"处理人"},index = 13)
    private String userName;
    @ExcelProperty(value = {"次数"},index = 14)
    private Integer alertTimes;
    @ExcelProperty(value = {"告警内容"},index = 15)
    private String message;//消息文本。用于消息告警
    @ExcelProperty(value = {"厂别"},index = 16)
    private String modelSystem;
    @ExcelProperty(value = {"领域"},index = 17)
    private String modelClassify;
    @ExcelProperty(value = {"告警通知用户"},index = 18)
    private String notifyUser;
    @ExcelIgnore
    private List<String> dealUser;
    @ExcelIgnore
    private String hostid;//主机id
    @ExcelIgnore
    private String hostName;//主机名称
    @ExcelIgnore
    private String problem;//问题详情
    @ExcelIgnore
    private String state;//当前状态
    //基本信息
    @ExcelIgnore
    private String subject;//消息主题。用于消息告警
    @ExcelIgnore
    private String recoverTime;//恢复时间
    //告警历史
    @ExcelIgnore
    private List<MWHistDto> hist;//告警历史
    @ExcelIgnore
    private Integer monitorServerId;
    @ExcelIgnore
    private Boolean isJump = true;
    //跳转链接
    @ExcelIgnore
    private String url;
    //参数数据
    @ExcelIgnore
    private Map param;
    //告警时间
    @ExcelIgnore
    private Date alertDate;
    @ExcelIgnore
    private Integer modelId;
    @ExcelIgnore
    private String modelIndex;
    @ExcelIgnore
    private Integer modelInstanceId;
    @ExcelIgnore
    private String instanceName;
    @ExcelIgnore
    private Integer monitorMode;
    @ExcelIgnore
    private String id;
    @ExcelIgnore
    private Integer assetsTypeId;
    @ExcelIgnore
    private Integer assetsTypeSubId;
    @ExcelIgnore
    private String groupNodes;
    @ExcelIgnore
    private String relationSiteRoomName;
    @ExcelIgnore
    private String relationSiteCabinetName;
    @ExcelIgnore
    private String positionByCabinetName;
    @ExcelIgnore
    private String modelClassifyName;
    @ExcelIgnore
    private String modelTag;
    @ExcelIgnore
    private String modelArea;
    @ExcelIgnore
    private String FromUser;
    @ExcelIgnore
    private String AlarmEventName;
    @ExcelIgnore
    private String tpServerHostName;
    @ExcelIgnore
    private String assetsName;
    @ExcelIgnore
    private String inBandIp;
    @ExcelIgnore
    private String outBandIp;
    @ExcelIgnore
    private String assetsTypeName;
    @ExcelIgnore
    private String assetsTypeSubName;
    @ExcelIgnore
    private String pollingEngine;
    @ExcelIgnore
    private String monitorModeName;
    @ExcelIgnore
    private Integer snmpLev;
    @ExcelIgnore
    private String manufacturer;
    @ExcelIgnore
    private String specifications;
    @ExcelIgnore
    private String description;
    @ExcelIgnore
    private String enable;
    @ExcelIgnore
    private Boolean deleteFlag;
    @ExcelIgnore
    private Boolean monitorFlag;
    @ExcelIgnore
    private Boolean settingFlag;
    @ExcelIgnore
    private Integer scanSuccessId;
    @ExcelIgnore
    private String timing;
    @ExcelIgnore
    private String vendorSmallIcon;
    @ExcelIgnore
    private Integer vendorCustomFlag;
    @ExcelIgnore
    private String templateId;
    @ExcelIgnore
    private String itemAssetsStatus;
    @ExcelIgnore
    private String orgName;
    @ExcelIgnore
    private Map<String,String> customFieldValue;
    @ExcelIgnore
    private boolean openConnect;
    @ExcelIgnore
    private String pollingEngineName;
    @ExcelIgnore
    private String rngineradio;
    @ExcelIgnore
    private String linkAlert;
    @ExcelIgnore
    private List<AlertConfirmUserParam> editorParams;

    @Override
    public int compareTo(ZbxAlertDto o) {
        int flag = o.clock.compareTo(this.clock);//时间倒序排
        return flag;
    }
}
