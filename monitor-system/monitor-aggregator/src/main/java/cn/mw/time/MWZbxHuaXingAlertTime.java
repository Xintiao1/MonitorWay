package cn.mw.time;

import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.alert.dto.BussinessAlarmInfoParam;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.model.param.MwModelFromUserParam;
import cn.mw.monitor.service.model.param.QueryModelAssetsParam;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.MWGroupCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.dto.GroupUserDTO;
import cn.mw.monitor.weixin.service.impl.EmailSendHuaXingImpl;
import cn.mw.monitor.weixin.service.impl.QyWxSendHuaXingAlertImpl;
import cn.mw.monitor.weixin.service.impl.TXinSendRancherMessageiImpl;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URLEncoder;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author xhy
 * @date 2020/4/17 17:07
 */
@Component
@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MWZbxHuaXingAlertTime {

    @Resource
    private MWAlertAssetsDao assetsDao;

    @Autowired
    MWGroupCommonService mwGroupCommonService;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwModelCommonService mwModelCommonService;

    @Value("${qyweixin.url}")
    private String url;

    @Value("${spring.huaxingsqldatasource.url}")
    private String DB_URL;

    @Value("${spring.huaxingsqldatasource.username}")
    private String USER;

    @Value("${spring.huaxingsqldatasource.password}")
    private String PASS;

    @Value("${spring.huaxingT34sqldatasource.url}")
    private String T34DB_URL;

    @Value("${spring.huaxingT34sqldatasource.username}")
    private String T34USER;

    @Value("${spring.huaxingT34sqldatasource.password}")
    private String T34PASS;

    //@Scheduled(cron = "0 */5 * * * ?")
    public TimeTaskRresult sendHuaXingAlert() {
        log.info(">>>>>>>sendHuaXingAlert>>>>>>>>>>");
        log.info(">>>>>>>华星SQL告警启动>>>>>>>>>>");
        List<MwModelFromUserParam> modelFromUserParams = new ArrayList<>();
        modelFromUserParams = mwModelCommonService.getInstanceInfoByModelIndex();
        List<BussinessAlarmInfoParam> params = assetsDao.selectBussinessAlarmInfo();
        TimeTaskRresult taskRresult = new TimeTaskRresult();
        //进行数据添加
        taskRresult.setSuccess(true);
        if (CollectionUtils.isEmpty(params)){
            return taskRresult;
        }
        for(BussinessAlarmInfoParam param : params){
            parsingXML(param,modelFromUserParams);

        }
        log.info(">>>>>>>sendHuaXingAlert end>>>>>>>>>>");
        taskRresult.setResultType(0);
        taskRresult.setResultContext("华星SQL告警成功");
        return taskRresult;
    }


    public void parsingXML(BussinessAlarmInfoParam param,  List<MwModelFromUserParam> modelFromUserParams){
        int count = -1;
        String alertTitle = null;
        try{
            log.info("xml内容：" + param.getContent());
            HashMap<String, String> map = new HashMap<>( );
            HashSet<Integer> userIds = new HashSet<>();
            List<Integer> emailUserIds = new ArrayList<>();
            List<Integer> emailCCUserIds = new ArrayList<>();
            StringReader reader = new StringReader(param.getContent());
            InputSource source = new InputSource(reader);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document parse = documentBuilder.parse(source);
            StringBuffer qyUrl = new StringBuffer(url);
            String closeTime = null;
            //NodeList messageNode = parse.getElementsByTagName("MESSAGE");
            switch (param.getStatus()){
                case "0":
                case "1":
                    String prefix = "告警";
                    String alarmEventName = parse.getElementsByTagName("ALARM_EVENT_NAME").item(0).getFirstChild().getNodeValue();
                    alertTitle = alarmEventName;
                    log.info("param:" + param);
                    log.info("param:" + param.getStatus());
                    log.info("alarmEventName :" + alarmEventName);
                    String modelSystem = null;
                    for(MwModelFromUserParam modelFromUserParam : modelFromUserParams){
                        if(param.getStatus().equals("1") && (modelFromUserParam.getIsAlert() == null || !modelFromUserParam.getIsAlert())){
                            continue;
                        }
                        if(alarmEventName.equals(modelFromUserParam.getAlarmEventName())){
                            modelSystem = modelFromUserParam.getModelSystem();
                            if(CollectionUtils.isNotEmpty(modelFromUserParam.getUserIds())){
                                userIds.addAll(modelFromUserParam.getUserIds());
                                emailUserIds.addAll(modelFromUserParam.getUserIds());
                            }
                            if(CollectionUtils.isNotEmpty(modelFromUserParam.getGroupIds())){
                                for (Integer groupid : modelFromUserParam.getGroupIds()) {
                                    Reply selectGroupUser = mwGroupCommonService.selectGroupUser(groupid);
                                    if (selectGroupUser.getRes() == 0) {
                                        List<GroupUserDTO> groupUserData = (List<GroupUserDTO>) selectGroupUser.getData();
                                        if (null != groupUserData && groupUserData.size() > 0) {
                                            for (GroupUserDTO pri : groupUserData) {
                                                userIds.add(pri.getUserId());
                                                emailCCUserIds.add(pri.getUserId());
                                            }
                                        }
                                    }
                                }
                            }
                            QueryModelAssetsParam qparam = new QueryModelAssetsParam();
                            qparam.setSkipDataPermission(true);
                            qparam.setInBandIp(param.getIp());
                            qparam.setFilterQuery(true);
                            List<MwTangibleassetsDTO> list = mwModelViewCommonService.findModelAssets(MwTangibleassetsDTO.class, qparam);
                            if(CollectionUtils.isNotEmpty(list)){
                                param.setObjectName(list.get(0).getAssetsName());
                            }
                            param.setSeverity(modelFromUserParam.getModelAlertLevel());
                            param.setAlertType("SQL中间表");
                            param.setModelClassify(modelFromUserParam.getModelClassify());
                            param.setModelSystem(modelFromUserParam.getModelSystem());
                            break;
                        }
                    }
                    log.info("userIDs :" + userIds.size());
                    if(CollectionUtils.isEmpty(userIds)) {
                        break;
                    }
                    if(param.getStatus().equals("1")){
                        if(modelSystem == null){
                            log.info("modelSystem为空！");
                            return;
                        }
                        String wechatContent = parse.getElementsByTagName("WECHAT_CONTENT").item(0).getFirstChild().getNodeValue();
                        if(StringUtils.isBlank(wechatContent)){
                            param.setTableContent("wechatContent为空！");
                            log.info("wechatContent为空！");
                            return;
                        }
                        String group = null;
                        Pattern pattern = Pattern.compile("<td>(.*?)<\\/td>");
                        Matcher matcher = pattern.matcher(wechatContent);
                        boolean foundGroupName = false;
                        while (matcher.find()) {
                            String data = matcher.group(1);
                            if (!foundGroupName) {
                                if (data.matches("\\d+\\.\\w+")) {
                                    group = data;
                                    foundGroupName = true;
                                }
                            }
                        }
                        log.info("恢复告警modelSystem：" + modelSystem);
                        count = huaxingDBConnect(group,alarmEventName,modelSystem);
                        log.info("group :" + group);
                        if(count != 0) {
                            log.info("ETL_TASK_GROUP表查询不为空！");
                            return;
                        }
                        prefix = "恢复告警";
                        //wechatContent = wechatContent.substring(wechatContent.lastIndexOf("<tr>"));
                        Date date = new Date();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
                        closeTime = df.format(date);
                    }
                    qyUrl.append(AlertAssetsEnum.QUESTION.toString())
                            .append(AlertEnum.DBID.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(param.getDbid(),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.TITLE.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(prefix,"UTF-8"));
                    if(closeTime != null){
                        qyUrl.append(AlertAssetsEnum.AND.toString()).append(AlertEnum.CLOSETIMEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(closeTime,"UTF-8"));
                    }

                    Node mailContentNode = parse.getElementsByTagName("MAIL_CONTENT").item(0).getFirstChild();
                    String mailContent = null;
                    if(mailContentNode != null){
                        mailContent = mailContentNode.getNodeValue();
                        map.put("emailContent",mailContent);
                        if(mailContent.contains("<table")){
                            map.put("ishtml","true");
                        }
                    }
                    map.put("qyweChatContent","告警跳转");
                    map.put("TxinContent","告警跳转");
                    map.put("TITLE",prefix + alarmEventName);
                    break;
                case "3":
                    String userid = parse.getElementsByTagName("USERID").item(0).getFirstChild().getNodeValue();

                    map.put("qyWeChatUser",userid);
                    NodeList messageNode = parse.getChildNodes().item(0).getChildNodes();
                    StringBuffer sb = new StringBuffer();
                    qyUrl.append(AlertAssetsEnum.QUESTION.toString());
                    for (int i = 0; i < messageNode.getLength(); i++) {
                        Node node = messageNode.item(i);
                        if(!node.getNodeName().equals("USERID") && node instanceof Element){
                            qyUrl.append(node.getNodeName()).append(AlertAssetsEnum.EQUAL.toString()).append(node.getTextContent()).append(AlertAssetsEnum.AND.toString());
                            sb.append(node.getNodeName()).append(AlertAssetsEnum.EQUAL.toString()).append(node.getTextContent()).append("\n");
                        }
                    }
                    qyUrl = qyUrl.deleteCharAt(qyUrl.lastIndexOf(AlertAssetsEnum.AND.toString()));
                    alertTitle = parse.getElementsByTagName("TITLE").item(0).getFirstChild().getNodeValue();
                    if(param.getContent().contains("SENDTYPE")){
                        Integer sendType3 = Integer.parseInt(parse.getElementsByTagName("SENDTYPE").item(0).getFirstChild().getNodeValue());
                        if(sendType3 == 1){
                            map.put("qyweChatContent",sb.toString());
                        }
                        if(sendType3 == 2){
                            map.put("TxinContent",sb.toString());
                        }
                    }else {
                        map.put("qyweChatContent",sb.toString());
                    }

                    map.put("TITLE",alertTitle);
                    break;
                case "5":
                    Integer sendType = Integer.parseInt(parse.getElementsByTagName("SENDTYPE").item(0).getFirstChild().getNodeValue());

                    if(sendType == 2 || sendType == 3){
                        String qyWeChatUser = parse.getElementsByTagName("USERCODE").item(0).getFirstChild().getNodeValue();
                        map.put("qyWeChatUser",qyWeChatUser);
                        map.put("qyweChatContent","告警跳转");
                    }
                    alertTitle = parse.getElementsByTagName("TITLE").item(0).getFirstChild().getNodeValue();
                    String content = parse.getElementsByTagName("WECHAT_CONTENT").item(0).getFirstChild().getNodeValue();
                    qyUrl.append(AlertAssetsEnum.QUESTION.toString())
                            .append(AlertEnum.DBID.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(param.getDbid(),"UTF-8"));
                    map.put("TITLE",alertTitle);
                    //String qyweChatContent = content.substr ing(0,content.indexOf("<table"));
                    if((sendType == 1 || sendType == 3 || sendType == 5) && StringUtils.isNotEmpty(content)){
                        map.put("emailContent",content);
                        String email = parse.getElementsByTagName("USERMAIL").item(0).getFirstChild().getNodeValue();
                        map.put("email",email);
                    }
                    if(sendType == 4 || sendType == 5){
                        map.put("TxinContent","告警跳转");
                        String qyWeChatUser = parse.getElementsByTagName("USERCODE").item(0).getFirstChild().getNodeValue();
                        map.put("qyWeChatUser",qyWeChatUser);
                    }
                    map.put("ishtml","true");
                    break;
                default:
                    break;
            }
            log.info("url：" + qyUrl);
            log.info("map：" + map);
            map.put(AlertEnum.URL.toString(),qyUrl.toString());
            map.put(AlertEnum.EVENTIDEN.toString().toUpperCase(),param.getDbid());
            sendType(map,userIds, param, emailUserIds,emailCCUserIds);
        }catch (Exception e){
            log.error("xml解析失败：{}",e);
            param.setTableContent(e.getMessage());
        }finally {
            if(param.getStatus().equals("3") || param.getStatus().equals("5")){
                param.setStatus("4");
            }else if(param.getStatus().equals("0")){
                param.setStatus("1");
            }else if(param.getStatus().equals("1") && count <= 0){
                param.setIsSend("1");
            }
            param.setAlarmEventName(alertTitle);
            assetsDao.updateBussinessAlarmInfo(param);
        }

    }

    public void sendType(HashMap<String, String> map,HashSet<Integer> userIds,BussinessAlarmInfoParam param, List<Integer> emailUserIds,List<Integer> emailCCUserIds){
        ExecutorService pool = Executors.newFixedThreadPool(10);
        try{
            if((map.containsKey("qyWeChatUser") || CollectionUtils.isNotEmpty(userIds)) && map.containsKey("qyweChatContent")){
                pool.submit(new QyWxSendHuaXingAlertImpl(map, userIds));
            }
            if((map.containsKey("email") || CollectionUtils.isNotEmpty(emailUserIds)) && map.containsKey("emailContent")){
                pool.submit(new EmailSendHuaXingImpl(map, userIds, emailUserIds, emailCCUserIds));
            }
            if((map.containsKey("qyWeChatUser") || CollectionUtils.isNotEmpty(userIds)) && map.containsKey("TxinContent")){
                pool.submit(new TXinSendRancherMessageiImpl(map,userIds));
            }
        }catch (Exception e){
            log.error("告警错误：{}",e);
        }finally {
            pool.shutdown();
        }

        try {
            pool.awaitTermination(60, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("线程错误：" + e);
        }
    }

    public int huaxingDBConnect(String group,String alarmEventName,String modelSystem){
        log.info("连接数据库...");
        Connection conn = null;
        Statement stmt = null;
        int count = -1;
        try {
            // 注册 JDBC 驱动
            Class.forName("oracle.jdbc.driver.OracleDriver");
            /*json.string*/
            // 打开链接
            log.info("连接数据库...");
            String sql = null;
            if(modelSystem.equals("t5")){
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                sql = "SELECT count(*)" +
                        "FROM p5etlctl.ETL_JOB_INS_ERR" +
                        "\n" +
                        "WHERE TASK_GROUP_NAME ='" + group + "'";
            }
            if(modelSystem.equals("t3") || modelSystem.equals("t4")){
                conn = DriverManager.getConnection(T34DB_URL, T34USER, T34PASS);
                if(alarmEventName.equals("RPT_ETL_Alarm_Level3")){
                    sql = "SELECT count(*)\n" +
                            "FROM P4ETLCTL.ETL_TASK_GROUP A, P4ETLCTL.ETL_TASK_GROUP_STATUS B\n" +
                            "WHERE A.TASK_GROUP_STATUS_ID = B.ID_\n" +
                            "and instr(lower(task_group_name),'RPT_ETL_Alarm_Level3')=0\n" +
                            "and instr(lower(task_group_name),'test')=0\n" +
                            "AND task_group_name NOT LIKE 'O%'\n" +
                            "and task_group_name = '" + group + "'\n" +
                            "AND IS_ENABLED ='Y'\n" +
                            "and status_!='成功'\n" +
                            "order by task_group_name DESC";
                }else {
                    sql = "SELECT count(*)" +
                            "FROM p4etlctl.ETL_JOB_INS_ERR" +
                            "\n" +
                            "WHERE TASK_GROUP_NAME ='" + group + "'";
                }

            }
            // 执行查询
            log.info(" 实例化Statement对象...");
            stmt = conn.createStatement();
            log.info("*********数据库查询**********");
            log.info("拼接SQL:" + sql);
            ResultSet resultSet = stmt.executeQuery(sql);
            if(resultSet.next()){
                count = resultSet.getInt(1);
            }
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            // 处理 JDBC 错误
            log.error("错误信息："+se);
        } catch (Exception e) {
            // 处理 Class.forName 错误
            log.error("错误信息："+e);
        } finally {
            // 关闭资源
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se2) {
            }// 什么都不做
            try {
                if (conn != null) conn.close();
            } catch (SQLException se) {
                log.error("错误信息："+se);
            }
        }
        return count;
    }



}
