package cn.mw.monitor.service.alert.dto;

public enum AlertEnum {
    DATASOURCES("数据来源"),
    ASSETS("资产"),
    ASSETSTYPE("资产类型"),
    AssetsName("资产名称"),

    DEVICETYPE("设备类型"),

    LOGTIME("日志时间"),

    LOGINFO("日志信息"),
    LOGLEVEL("日志等级"),

    DATASOURCE("数据源"),

    RULETAG("规则标签"),
    ASSETSNAMEEN("assetsName"),
    AssetsStatus("资产状态"),
    AssetsTypeSubId("资产类型子分类"),
    AssetsUuid("zabbix资产UUID"),
    AssetsSerialnum("zabbix资产序列号"),
    ALERTTITLE("告警标题"),
    ALERTTITLEEN("alerttitle"),
    ALERTINFO("告警信息"),
    ALERTINFOEN("alertinfo"),
    ALERTLEVEL("告警等级"),
    ALERTLEVELEN("alertlevel"),
    PROBLEMDETAILS("问题详情"),
    PROBLEMDETAILSEN("problemdetails"),
    NOWSTATE("当前状态"),
    NOWSTATEEN("nowstate"),
    LABEL("标签"),
    LABELEN("label"),
    TAG("tag"),
    OK("OK"),
    ORG("机构"),
    ORGEN("org"),
    GROUP("用户组"),
    //接口
    GigabitEthernet("GigabitEthernet"),

    Interface("接口"),
    InterfaceMode("接口模式"),
    InterfaceModeDesc("接口描述"),
    TARGET("指标"),
    BASELINE("基线"),
    CUSTOM("自定义"),
    FixedTime("固定时间"),
    NotSent("解析数据尚未发送"),
    CompressNumber("当前压缩告警条数"),
    AssetsList("assetsList"),
    CONTROLLER("控制器"),
    RECOVERYDETAILS("恢复详情"),
    RECOVERYDETAILSEN("recoverydetails"),
    AssociatedModule("关联模块"),
    HostNameZH("主机名称"),
    IPAddress("IP地址"),
    SystemInfo("系统信息"),
    SENDTIEM("发送时间"),
    SENDTIEMEN("sendtiem"),
    SYSTEMINFOEN("systeminfo"),
    DefaultSelection("默认选择"),
    AutoSequence("自增序列"),
    AssetsID("资产id"),
    InBandIp("带内ip"),
    PollingEngine("轮询引擎"),

    MODELCLASSIFY("领域"),
    MODELSYSTEMZH("厂别"),
    MonitorMode("监控方式"),
    Manufacturer("厂商"),
    Specifications("规格型号"),
    APPCATIONINFO("设备型号"),
    APPCATIONINFOEN("appcationinfo"),
    Description("描述"),
    DeleteFlag("删除标识符"),
    MonitorFlag("启动监控状态"),
    SettingFlag("启动配置状态"),
    Creator("创建人"),
    CreateDate("创建时间"),
    Modifier("修改人"),
    ModificationDate("修改时间"),
    ScanSuccessId("扫描成功表id"),
    MonitorServerId("监控服务器id"),
    Timing("定时间隔"),
    TpServerHostName("第三方监控服务器中主机名称"),
    TemplateId("第三方监控服务器中关联模板id"),
    WebMonitor("[网站监测]["),
    XianLu("[线路]["),
    Domain("区域"),
    DOMAINEN("domain"),
    AddLabel("请在该资产添加区域标签"),
    LINK("线路"),
    LINKNAME("线路名称"),
    ALERT("告警"),
    ALERTHAPPEN("告警产生时间"),
    RECOVERY("恢复"),
    ALERTTIME("告警时间"),
    ALERTTIMEEN("alerttime"),
    ALERTSTARTIME("告警开始时间"),
    ALERTSTARTIMEEN("alertstartime"),
    RECOVERYTIME("恢复时间"),
    RECOVERYTIMEEN("recoverytime"),
    FAILURETIME("故障时间"),
    FAILURETIMEEN("failuretime"),
    OPERATOR("运营商"),
    LINKTYPE("线路类型"),
    LINKNUM("线路编号"),
    LINKDISCONNECTION("线路断开"),
    LINKRECOVERY("线路恢复"),
    EVENTID("事件ID"),
    EVENTIDEN("eventid"),
    NEVER("从未"),
    NOTRECOVERED("未恢复"),
    UNCONFIRMED("未确定"),
    CONFIRMED("已确定"),
    VR("虚拟化"),
    RENEWSTATUS("恢复状态"),
    TOPIC("主题"),
    TOPICEN("topic"),
    PERSON("负责人"),
    PERSONEN("person"),
    LONGTIMEZH("告警时长"),
    CLOSETIME("关闭时间"),
    PROJECTNAME("项目名称"),
    ENDSAT("结束时间"),
    CLUSTERNAME("集群名称"),
    PODNAME("POD名称"),
    CLOSETIMEEN("closetime"),
    LASTEVENT("lastEvent"),
    Lastvalue("lastvalue"),
    ROOT("root"),
    VHost("vHost"),
    Default("default"),
    WANGKE("wanke"),

    SHENGRENYI("shengrenyi"),
    CHENGDUGUANWEI("chengduguanwei"),
    LONGTIMECH("持续时间"),
    HOSTID("HOSTID"),
    HOSTIP("HOSTIP"),
    HOSTNAME("HOSTNAME"),
    R_EVENTID("r_eventid"),
    HOSTS("hosts"),
    OBJECTID("objectid"),
    SEVERITY("severity"),
    CLUSTERNAMEEN("cluster_name"),
    CLUSTERNAMEENS("clusterName"),
    NAME("name"),
    ACKNOWLEDGED("acknowledged"),
    CLOCK("clock"),
    ALERTS("alerts"),
    LABELS("labels"),
    IMMC("IMMC"),
    ICMP("ICMP"),
    STARTSAT("startsAt"),
    ENDSATEN("endsAt"),
    PROJECTNAMEEN("project_name"),
    PROJECTNAMES("projectName"),
    PODNAMEEN("pod_name"),
    PODNAMEENS("podName"),
    DURATION("duration"),
    THRESHOLDVALUE("threshold_value"),
    ANNOTATIONS("annotations"),
    CURRENTVALUE("current_value"),
    OPERATION("OPERATION"),
    ALERTNAME("alert_name"),
    ALERTID("alertid"),
    MESSAGE("message"),
    SUBJECT("subject"),
    NS("ns"),
    MODELCLASSIFYEN("modelClassify"),
    RETRIES("retries"),
    COMMENTS("comments"),
    NOTIFYUSER("notifyUser"),
    FUNCTIONS("functions"),
    ITEMID("itemid"),
    VALUE_TYPE("value_type"),
    UNITS("units"),
    VALUE("value"),
    HOST("host"),
    TRIGGERS("triggers"),
    STATUS("status"),
    PRIORITY("priority"),
    TRIGGERID("triggerid"),
    DESCRIPTIONEN("description"),
    EXCELALERTPARAM("excelAlertParam"),
    OBJECTNAME("objectName"),
    ALERTTYPE("alertType"),
    IP("ip"),
    RCLOCK("rclock"),
    LONGTIME("longTime"),
    RECOVERTIME("recoverTime"),
    HISTORY("history"),
    SHEET("sheet"),
    USERNAME("userName"),
    TYPE("type"),

    ALERTTIMES("alertTimes"),

    SOLUTION("solution"),
    DEALUSER("dealUser"),
    TRIGGERREASON("triggerReason"),
    ASSETIDS("assetIds"),
    IDS("ids"),
    URL("url"),
    HTML("html"),
    PRIVATE("PRIVATE"),
    INFO("info"),
    FAULTTIME("faultTime"),
    ADDRESS("address"),
    RENEWSTATUSEN("renewStatus"),
    UNUSUAL("异常"),
    NORMAL("正常"),
    RECOVERYINFO("恢复信息"),
    RECOVERYLEVEL("恢复等级"),
    RECOVERYLEVELEN("recoverylevel"),
    DROPZH("点"),
    ALERTHTML("alert.html"),
    RECOVERYHTML("recovery.html"),
    RECOVERYTITLEEN("recoverytitle"),
    ALERTNOTICE("告警通知"),
    RECOVERYNOTICE("告警恢复通知"),
    ALARM("alarm"),
    RESTORE("restore"),
    HUAXING("huaxing"),
    Warning("警告"),
    ERROR("严重"),
    ALL("全部"),
    DISASTER("紧急"),
    AVERAGE("一般严重"),
    GuangZhouBank("guangzhoubank"),
    XIZANGDIANWANG("xizangdianwang"),
    ALARM_HUAXING("alarm_huaxing"),
    UPGARDE_HUAXING("upgarde_huaxing"),
    RESTORE_HUAXING("restore_huaxing"),
    QYWECHATCONTENT("qyweChatContent"),

    ASSETSIP("资产IP"),
    EMAILCONTENT("emailContent"),
    SHANYING("shanying"),
    MODELSYSTEM("业务系统"),
    ALERT_TAG("告警标记"),
    MODELSYSTEMEN("modelsystem"),
    ALARMEVENTNAME("alarmEventName"),
    ALARMLEVEL("alarmLevel"),
    BUSSINESSNAME("bussinessName"),
    TABLECONTENT("tableContent"),
    CONTENT("content"),
    DBID("dbid"),
    RELATIONSECTOR("relationSector"),
    CREATETIME("createTime"),
    TITLE("title"),
    RECOVERYTITLE("恢复标题"),
    KEYDEVICES("关键设备");


    private String name;
    AlertEnum(String name){
        this.name = name;
    }
    public String toString() {
        return name;
    }

}