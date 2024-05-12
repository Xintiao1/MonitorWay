package cn.mw.monitor.util.entity;

import lombok.Data;

/**
 * 邮件参数实体类
 * @author
 *
 */
@Data
public class HtmlTemplateEmailParam {

    private String title;  //标题
    private String url;  //图片路径
    private String name; //主机名称
    private String message; //信息
    private String level;   //等级
    private String ip;  //IP地址
    private String date;    //告警时间  故障时间
    private String hfdate; //恢复时间
    private String detail; //问题详细  恢复详情
    private String state; // 当前状态  恢复状态
    private String id; //事件id
    private  String domain;//区域

    private String option;  //告警对象
    private String address; //告警地址
    private String context; //告警内容

    private String date1;    //告警发生时间
    private String date2;    //告警恢复时间
    private String date3;    //告警恢复时长

    private String assetsMonitor; //关联模块
    private String specifications;//设备型号

    private String host;//资产IP
    private String hostName;//资产名称
    private String severity_label;//日志等级
    private String facility_label;//设备类型
    private String timestamp;//日志时间
    private String dataSourceName;//数据源
    private String tagName;//规则标签
    private String hidden;
    private String longTime;//告警时长
    private String systemHidden;
    private String sendDate;//发送时间
    private String person;//负责人
    private String applicationSystem;//应用系统

    public String getApplicationSystem(){return applicationSystem;}

    public void setApplicationSystem(String applicationSystem){this.applicationSystem = applicationSystem;}

    public String getPerson(){return person;}

    public void setPerson(String person){this.person=person;}

    public String getSendDate(){return sendDate;}

    public void setSendDate(String sendDate){this.sendDate = sendDate;}

    public String getLongTime(){return longTime;}

    public void setLongTime(String longTime){this.longTime=longTime;}

    public String getSystemHidden() {
        return systemHidden;
    }

    public void setSystemHidden(String systemHidden) {
        this.systemHidden = systemHidden;
    }

    public String getHidden() {
        return hidden;
    }


    public void setHidden(String hidden) {
        this.hidden = hidden;
    }

    public String getSpecifications() {
        return specifications;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAssetsMonitor() {
        return assetsMonitor;
    }

    public void setAssetsMonitor(String assetsMonitor) {
        this.assetsMonitor = assetsMonitor;
    }


    public String getHfdate() {
        return hfdate;
    }

    public void setHfdate(String hfdate) {
        this.hfdate = hfdate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDate1() {
        return date1;
    }

    public void setDate1(String date1) {
        this.date1 = date1;
    }

    public String getDate2() {
        return date2;
    }

    public void setDate2(String date2) {
        this.date2 = date2;
    }

    public String getDate3() {
        return date3;
    }

    public void setDate3(String date3) {
        this.date3 = date3;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "HtmlTemplateEmailParam{" +
                "option='" + option + '\'' +
                ", address='" + address + '\'' +
                ", context='" + context + '\'' +
                ", level='" + level + '\'' +
                ", date='" + date + '\'' +
                ", date1='" + date1 + '\'' +
                ", date2='" + date2 + '\'' +
                ", date3='" + date3 + '\'' +
                '}';
    }
}
