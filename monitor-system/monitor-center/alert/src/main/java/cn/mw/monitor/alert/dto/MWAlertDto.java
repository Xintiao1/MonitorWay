package cn.mw.monitor.alert.dto;

import cn.mw.monitor.common.bean.BaseDTO;
import lombok.Data;

/**
 * @author xhy
 * @date 2020/3/27 15:39
 */
@Data
public class MWAlertDto extends BaseDTO implements Comparable<MWAlertDto> {
    private String alertid;
    private String actionid;
    private String objectid;
    private String triggerid;//触发器ID
    private String object;//告警对象
    private String hostid;//主机ID
    private String ip;
    private String eventid;//触发 Action 的事件 ID
    private String userid;//邮件发送到的用户的 ID
    private Long clock;//生成的时间
    private String severity;//告警等级
    private String altype;//告警类型
    private String time;//告警时间
    private String rtime;
    private Long topId;
    private String longTime;//持续时间
    private String mediatypeid;//用于发送消息的报警媒介类型的ID
    private String noticeType;
    private String sendto;//地址，用户名或接收者的其他标识符。用于消息告警
    private String subject;//消息主题。用于消息告警
    private String message;//消息文本。用于消息告警
    private String status;//显示 action 操作是否已执行成功的状态0 - 消息未发送(命令没有运行)1 - 消息已发送(命令运行成功)
    private String retries;//Zabbix 尝试发送消息的次数
    private String error;
    private String users;
    private String name;
    private String r_eventid;
    private String acknowledged;//已知晓的问题 0-不知道 1-知道
    private String acknowledges;//
    private Long r_clock;
    private String rclock;
    private String subtype;
    private Long r_ns;
    private Long ns;
    private String esc_step;//生成 Alert 后 Action 的处理步骤
    private String alerttype;//可能的值：0 - 信息;1 - 远程命令
    private String p_eventid;//生成告警的异常事件 ID
    private String acknowledgeid;//生成告警的确认 ID
    private String startTime;
    private String endTime;
    private int alertNum;//告警次数
    private String isServerDay;//是否是七天
    private String days;//是否是七天

    @Override
    public int compareTo(MWAlertDto o) {
        int flag = o.time.compareTo(this.time);//时间倒序排
        return flag;
    }

}
