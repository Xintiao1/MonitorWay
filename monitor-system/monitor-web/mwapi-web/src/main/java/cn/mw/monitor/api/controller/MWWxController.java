package cn.mw.monitor.api.controller;


import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.alert.dto.*;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.model.param.MwRancherProjectUserListDTO;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.util.KafkaProducerUtil;
import cn.mw.monitor.weixin.dao.MwWeixinTemplateDao;
import cn.mw.monitor.weixin.entity.*;
import cn.mw.monitor.weixin.service.WxPortalService;
import cn.mw.monitor.weixin.service.impl.EmailSendHuaXingImpl;
import cn.mw.monitor.weixin.service.impl.QyWxSendHuaXingAlertImpl;
import cn.mw.monitor.weixin.service.impl.TXinSendRancherMessageiImpl;
import cn.mw.monitor.weixinapi.NotifyAlertMessage;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@RequestMapping("/mwapi/weixin")
@Controller
@Api(value = "WEIXIN")
public class MWWxController {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    @Autowired
    private WxPortalService wxPortalService;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    private List<NotifyAlertMessage> notifyAlertMessages;

    @Value("${weixin.redirectMwUrl}")
    private String redirectMwUrl;

    @Value("${qyweixin.url}")
    private String url;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Autowired
    private MwWeixinTemplateDao mwWeixinTemplateDao;

    @Resource
    private MWAlertAssetsDao assetsDao;

    private static final String ASSETS = "assets";
    private static final String LABEL = "label";
    /**
     * 网页登录
     * @return
     */
    @GetMapping(value="/getUserInfoAndRedicet")
    public  String getInfo(@RequestParam(name = "code") String code) {
        System.err.println("code:"+code);
        //TODO 这里应该考虑一下要不要处理一下code

        //根据codo获取access_code，注意这个access_code 跟我们操作服务号需要access_code不一样
        String result = wxPortalService.getCodeAccessToken(code);
        System.err.println("result:"+result);
        String at = JSONObject.parseObject(result).getString("access_token");
        String openid = JSONObject.parseObject(result).getString("openid");

        //拉取用户的基本信息
        result = wxPortalService.getUserInfo(openid);
        System.err.println("user:"+result);

        //TODO
        /*
           1：根据此用户的openid 和本系统中数据库关联的用户做出判断
           2：实现重定向到我们的手机端登录页面
         */
        String apam = "/?aparm=1";
        return "redirect:"+redirectMwUrl+apam;
    }

    /**
     * @describe 菜单初始化
     */
    @GetMapping(value="/initMenu",produces = "text/plain;charset=utf-8")
    @ApiOperation(value="initMenu")
    @ResponseBody
    public  String  initMenu(){
        try{
            String response = wxPortalService.setMenu();
            return response;
        }catch (Exception e){
            log.error("initMenu", e);
        }
        return null;
    }

    /**
     * @describe 获取所属行业
     */
    @ResponseBody
    @GetMapping(value="/getHy",produces = "text/plain;charset=utf-8")
    @ApiOperation(value="获取所属行业")
    public String  getHy() {
        try{
            String result = wxPortalService.getHy();
            return result;
        }catch (Exception e){
            log.error("getHy", e);
        }
        return "获取所属行业失败";
    }


    /**
     * @describe 设置所属行业
     */
    @GetMapping(value="/setHy")
    @ApiOperation(value="设置所属行业")
    @ResponseBody
    public String  setHy(@RequestParam(name = "industry_id1", required = false) String industry_id1,
                         @RequestParam(name = "industry_id2", required = false) String industry_id2) {
        try{
            String result = wxPortalService.setHy(industry_id1,industry_id2);
            return result;
        }catch (Exception e){
            log.error("setHy", e);
        }
        return "设置所属行业失败";
    }

    /**
     * 获取模板列表信息初始化到数据库中
     */
    @GetMapping(value="/initTemplateList")
    @ResponseBody
    @ApiOperation(value = "initTemplateList")
    public  List<MwWeixinTemplateTable> getTemplateList() {
        try {
            List<MwWeixinTemplateTable> lists = wxPortalService.initTemplate();
            return lists;
        }catch (Exception e){
            log.error("getTemplateList", e);
        }
        return null;
    }

    /**
     * 获取所有关注用户的基本信息并保存到本系统中
     * @return
     */
    @GetMapping(value="/inituser")
    @ResponseBody
    @ApiOperation(value = "initUserInfo")
    public  String initUserInfo(){
        try{
            String users = wxPortalService.initUser();
            return users;
        }catch(Exception e){
            log.error("initUserInfo", e);
        }
        return null;
    }

    /**
     * 查看获取所有关注用户的基本信息
     * @return
     */
    @GetMapping(value="/getUserList")
    @ResponseBody
    @ApiOperation(value = "getUserListInfo")
    public  List<MwWeixinUserTable> getUserListInfo(){
        try{
            List<MwWeixinUserTable> users = wxPortalService.getUserList();
            return users;
        }catch(Exception e){
            log.error("getUserListInfo", e);
        }
        return null;
    }

    //项目启动时间
    private static long timeStart = new Date().getTime();

    //时间戳偏移大小（设置成>=自动提交时间一样）
    @Value("${spring.kafka.consumer.overdueOffset}")
    private long offset;

    /**
     * 消费者监听数据
     */
    @KafkaListener(topics = {"zabbix-alert"},containerFactory = "KafkaConsumerBatchConfig",autoStartup = "${spring.kafka.consumer.isAutoCommit}")
    public void con(List<ConsumerRecord<?, ?>> records) {
        try{
            log.info("kafka数据处理开始");
            List<String> msgsList = new ArrayList<>();
            List<MwOverdueTable> overdueTables = new ArrayList<>();
            for (ConsumerRecord<?, ?> record : records) {
                Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                long timestamp = record.timestamp();
                if (timestamp + offset < timeStart) {
                    log.info("项目启动前发生的信息");
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                        String msg = message.toString();
                        //将msg中所有Unicode字符串转为支持字符串
                        String msgs = wxPortalService.converUnicodeToChar(msg);
                        MwOverdueTable data = new MwOverdueTable();
                        data.setContext(msgs);
                        data.setConTime(new Date(timestamp));
                        data.setStartTime(new Date(timeStart));
                        data.setCreateDate(new Date());
                        data.setIsSend(false);
                        data.setDeleteFlag(false);
                        overdueTables.add(data);
                    }
                } else {
                    log.info("项目启动后发生的信息");
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                        String msg = message.toString();
                        //将msg中所有Unicode字符串转为支持字符串
                        JSONObject jsonObject = JSONObject.parseObject(msg);
                        message = jsonObject.get("alert");
                        String msgs = wxPortalService.converUnicodeToChar(message.toString());
                        msgsList.add(msgs);
                        log.info("zabbix-alert:{}", msgs);
                        //消息通知各个模块
                        for (NotifyAlertMessage notifyAlertMessage : notifyAlertMessages) {
                            notifyAlertMessage.sendMessage(msgs);
                        }


                    }
                }
            }
            if(CollectionUtils.isNotEmpty(overdueTables)){
                //将消息保存到告警过期表中
                wxPortalService.insertOverdue(overdueTables);
            }
            if(CollectionUtils.isNotEmpty(msgsList)){
                //处理消息内容 并根据处理结果发送信息
                MWAlertAssetsParam.tangibleassetsDTOMap = new ConcurrentHashMap<>();
                wxPortalService.dealMessage(msgsList);
            }
        }catch (Exception e){
            log.error("解析数据尚未发送:{}",e);
            AlertRecordTable recored = new AlertRecordTable();
            recored.setDate(new Date());
            recored.setMethod("解析数据前发生错误");
            recored.setText("解析数据前发生错误");
            recored.setIsSuccess(1);
            recored.setHostid("-1");
            recored.setError(e.getMessage());
            recored.setIp(null);
            recored.setTitle(null);
            recored.setIsAlarm(null);
            wxPortalService.insertRecord(recored);
        }


    }
    /**
     * 消费者监听数据
     */
    @KafkaListener(idIsGroup = false,groupId ="test2",topics = {"assets-message"},containerFactory = "KafkaConsumerConfig")
    public void conSql(ConsumerRecord<?, ?> record) {
        String msg = "";
        try{
            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            if (kafkaMessage.isPresent()) {
                msg = kafkaMessage.get().toString();
                log.info("kafka资产数据更新msg:" + msg);
                String[] s = msg.split("-");
                switch (s[0]){
                    case ASSETS:
                        if(s[1].equals(AlertAssetsEnum.Add.toString())){
                            MwTangibleassetsDTO dto = mwAssetsManager.getAssetsAndOrgs(s[2]);
                            String key = dto.getAssetsId() + AlertAssetsEnum.Dash.toString() + dto.getInBandIp();
                            MWAlertAssetsParam.tangibleassetsDTOMap.put(key,dto);
                        }else if(s[1].equals(AlertAssetsEnum.Del.toString())){
                            String key = KafkaProducerUtil.getLikeByMap(MWAlertAssetsParam.tangibleassetsDTOMap,s[2]);
                            if (key != null){
                                MWAlertAssetsParam.tangibleassetsDTOMap.remove(key);
                            }
                        }
                        break;
                    case LABEL:
                        if(s[1].equals(AlertAssetsEnum.Add.toString())){
                            List<MwAssetsLabelDTO> assetsLabelDTOS = mwLabelCommonServcie.getLabelBoard(s[2], AlertAssetsEnum.Assets.toString().toUpperCase());
                            MWAlertAssetsParam.mwAssetsLabelDTOMap.put(s[2],assetsLabelDTOS);
                        }else if(s[1].equals(AlertAssetsEnum.Del.toString())){
                            MWAlertAssetsParam.tangibleassetsDTOMap.remove(s[2]);
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception exc) {
            log.error("Kafka接收数据更新信息错误:{}", exc);
        }
    }

    /**
     * 消费者监听数据
     */
    @KafkaListener(topics = {"t5bczabbix-alert"},groupId ="huaxing",containerFactory = "KafkaConsumerBatchConfig")
    public void huaXingCon(List<ConsumerRecord<?, ?>> records) {
        try{
            log.info("kafka数据处理开始");
            List<String> msgsList = new ArrayList<>();
            List<MwOverdueTable> overdueTables = new ArrayList<>();
            for (ConsumerRecord<?, ?> record : records) {
                Optional<?> kafkaMessage = Optional.ofNullable(record.value());
                long timestamp = record.timestamp();
                if (timestamp + offset < timeStart) {
                    log.info("华星光电项目启动前发生的信息");
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                        String msg = message.toString();
                        //将msg中所有Unicode字符串转为支持字符串
                        String msgs = wxPortalService.converUnicodeToChar(msg);
                        MwOverdueTable data = new MwOverdueTable();
                        data.setContext(msg);
                        data.setConTime(new Date(timestamp));
                        data.setStartTime(new Date(timeStart));
                        data.setCreateDate(new Date());
                        data.setIsSend(false);
                        data.setDeleteFlag(false);
                        overdueTables.add(data);
                    }
                } else {
                    log.info("华星光电项目启动后发生的信息");
                    if (kafkaMessage.isPresent()) {
                        Object message = kafkaMessage.get();
                        String msg = message.toString();
                        //将msg中所有Unicode字符串转为支持字符串
                        JSONObject jsonObject = JSONObject.parseObject(msg);
                        message = jsonObject.get("alert");
                        msgsList.add(message.toString());
                        log.info("zabbix-alert:{}", message.toString());
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(overdueTables)){
                //将消息保存到告警过期表中
                wxPortalService.insertOverdue(overdueTables);
            }
            if(CollectionUtils.isNotEmpty(msgsList)){
                wxPortalService.workSystem(msgsList);
            }
        }catch (Exception e){
            log.error("t5bc解析数据尚未发送:{}",e);
            AlertRecordTable recored = new AlertRecordTable();
            recored.setDate(new Date());
            recored.setMethod("t5bc解析数据前发生错误");
            recored.setText("t5bc解析数据前发生错误");
            recored.setIsSuccess(1);
            recored.setHostid("-1");
            recored.setError(e.getMessage());
            recored.setIp(null);
            recored.setTitle(null);
            recored.setIsAlarm(null);
            wxPortalService.insertRecord(recored);
        }


    }


    @PostMapping("/open/sendMessage")
    @ResponseBody
    public ResponseBase sendMessage(@RequestBody JSONObject message) {

        try{
            log.info("接收数据：" + message);
            List<MwRancherProjectUserListDTO> rancherDTOList = mwModelViewCommonService.getAllRancherProjectUserInfo();
            log.info("ranch数量:" + rancherDTOList.size());
            if(CollectionUtils.isEmpty(rancherDTOList)){
                log.info("rancher为空");
                return new ResponseBase(Constants.HTTP_RES_CODE_300,"rancher为空", null);
            }
            JSONArray alerts = message.getJSONArray(AlertEnum.ALERTS.toString());
            for(int i=0; i < alerts.size(); i++){
                JSONObject alert = alerts.getJSONObject(i);
                JSONObject labels = alert.getJSONObject(AlertEnum.LABELS.toString());
                String projectName = null;
                if(labels.containsKey(AlertEnum.PROJECTNAMEEN.toString())){
                    projectName = labels.get(AlertEnum.PROJECTNAMEEN.toString()).toString();
                    projectName = projectName.replaceAll(" ","");
                    projectName = projectName.substring(projectName.indexOf(":")+1,projectName.lastIndexOf(")"));
                }else{
                    projectName = labels.get("cluster_name").toString();
                    projectName = projectName.substring(0,projectName.indexOf("("));
                }

                HashSet<Integer> userIds = new HashSet<>();
                log.info("projectName：" + projectName);
                String modelSystemName = null;
                MwRancherProjectUserListDTO rancher = new MwRancherProjectUserListDTO();
                for(MwRancherProjectUserListDTO dto : rancherDTOList){
                    log.info("ranch dto:" + dto);
                    if(projectName.equals(dto.getId())){
                        if(CollectionUtils.isEmpty(dto.getUserIds())){
                            save(labels.toJSONString(), "用户为空","WebHook");
                            return new ResponseBase(Constants.HTTP_RES_CODE_500,"发送失败", "用户为空");
                        }
                        userIds.addAll(dto.getUserIds());
                        if(StringUtils.isBlank(dto.getRelationModelSystem())){
                            log.info("业务系统为空");
                            save(labels.toJSONString(), "业务系统为空","WebHook");
                            return new ResponseBase(Constants.HTTP_RES_CODE_500,"发送失败", "业务系统为空");
                        }
                        modelSystemName = dto.getRelationModelSystem() + "-" + dto.getRelationModelClassify();
                        rancher = dto;
                        break;
                    }
                }
                log.info("userIds用户为：" + userIds);
                HashMap<String, String> map = dealContent(alert,userIds,modelSystemName,rancher);
                log.info("userIds：" + userIds);
                sendType(map,userIds);
            }
            return new ResponseBase(Constants.HTTP_RES_CODE_200,"接收成功", null);
        }catch (Exception e){
            log.error("sendMessage error:{}",e);
            save("解析失败", e.getMessage(),"WebHook");
            return new ResponseBase(Constants.HTTP_RES_CODE_500,"发送失败", e.getMessage());
        }
    }

    private void save(String msg, String error, String method){
        AlertRecordTable recored = new AlertRecordTable();
        recored.setDate(new Date());
        recored.setMethod(method);
        recored.setText(msg);
        recored.setIsSuccess(1);
        recored.setHostid("-1");
        recored.setError(error);
        recored.setIp(null);
        recored.setTitle(null);
        recored.setIsAlarm(null);
        wxPortalService.insertRecord(recored);
    }

    private HashMap<String, String> dealContent(JSONObject alert,HashSet<Integer> userIds, String modelSystemName,MwRancherProjectUserListDTO rancher){
        HashMap<String, String> map = new HashMap<>();
        JSONObject labels = alert.getJSONObject(AlertEnum.LABELS.toString());
        String startsAt = alert.get(AlertEnum.STARTSAT.toString()).toString();
        String endsAt = alert.get(AlertEnum.ENDSATEN.toString()).toString();
        ZonedDateTime startsAtZonedDateTime = ZonedDateTime.parse(startsAt).plusHours(8);
        ZonedDateTime endsAtZonedDateTime = ZonedDateTime.parse(endsAt).plusHours(8);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm:ss");
        startsAt = startsAtZonedDateTime.format(formatter);
        endsAt = endsAtZonedDateTime.format(formatter);
        String status = AlertHuaXingWebHookLevelENEnum.getName(alert.get(AlertEnum.STATUS.toString()).toString());
        String severity = AlertHuaXingWebHookLevelENEnum.getName(labels.get(AlertEnum.SEVERITY.toString()).toString());
        String clusterName = labels.get(AlertEnum.CLUSTERNAMEEN.toString()).toString();
        String alertName = labels.get(AlertEnum.ALERTNAME.toString()).toString();
        String projectName = null;
        if(labels.containsKey(AlertEnum.PROJECTNAMEEN.toString())){
            projectName = labels.get(AlertEnum.PROJECTNAMEEN.toString()).toString();
            projectName = projectName.replaceAll(" ","");
            projectName = projectName.substring(projectName.indexOf(":")+1,projectName.lastIndexOf(")"));
        }else{
            projectName = labels.get("cluster_name").toString();
            projectName = projectName.substring(0,projectName.indexOf("("));
        }

        String podName = labels.get(AlertEnum.PODNAMEEN.toString()).toString();
        String duration = labels.get(AlertEnum.DURATION.toString()).toString();
        String thresholdValue = labels.get(AlertEnum.THRESHOLDVALUE.toString()).toString();
        JSONObject annotations = alert.getJSONObject(AlertEnum.ANNOTATIONS.toString());
        String currentValue = annotations.get(AlertEnum.CURRENTVALUE.toString()).toString();
        String description = "Alert Name：" + alertName + "当前值(" + currentValue + ") 持续(" + duration + ") OPERATION(" + thresholdValue + ",阈值)";
        String userName = getUserName(userIds);
        if(alertName.contains(">")) description = description.replaceAll(AlertEnum.OPERATION.toString(),AlertAssetsEnum.greater.toString());
        if(alertName.contains("=")) description = description.replaceAll(AlertEnum.OPERATION.toString(),AlertAssetsEnum.equal.toString());
        if(alertName.contains("<")) description = description.replaceAll(AlertEnum.OPERATION.toString(),AlertAssetsEnum.less.toString());
        StringBuffer sb = new StringBuffer();
        sb.append(AlertEnum.ALERTTITLE.toString()).append(":").append(alertName).append('\n')
                .append(AlertEnum.PROJECTNAME.toString()).append(":").append(projectName).append('\n')
                .append(AlertEnum.ALERTSTARTIME.toString()).append(startsAt).append('\n')
                .append(AlertEnum.ENDSAT.toString()).append(":").append(endsAt).append('\n')
                .append(AlertEnum.MODELSYSTEM.toString()).append(modelSystemName).append('\n')
                .append(AlertEnum.PERSON.toString()).append(":").append(userName).append('\n')
                .append(AlertEnum.NOWSTATE.toString()).append(":").append(status).append('\n')
                .append(AlertEnum.ALERTLEVEL.toString()).append(":").append(severity).append('\n')
                .append(AlertEnum.CLUSTERNAME.toString()).append(":").append(clusterName).append('\n')
                .append(AlertEnum.PODNAME.toString()).append(":").append(podName).append('\n')
                .append(AlertEnum.ALERTINFO.toString()).append(":").append(description).append('\n');
        StringBuffer qyUrl = new StringBuffer(url);
        try{
            qyUrl.append(AlertAssetsEnum.QUESTION.toString()).append(AlertEnum.TITLE.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(alertName,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.PROJECTNAMES.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(projectName,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.ALERTSTARTIMEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(startsAt,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.MODELSYSTEMEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(modelSystemName,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.PERSONEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(userName,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.CLOSETIMEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(endsAt,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.NOWSTATEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(status,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.ALERTLEVELEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(severity,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.CLUSTERNAMEENS.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(clusterName,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.PODNAMEENS.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(podName,"UTF-8")).append(AlertAssetsEnum.AND.toString())
                    .append(AlertEnum.ALERTINFOEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(description,"UTF-8"));
        }catch (Exception e){
            log.error("url拼接错误:{}",e);
        }
        String eventId = UUIDUtils.getUUID();
        map.put(AlertEnum.URL.toString(),qyUrl.toString());
        map.put(AlertEnum.QYWECHATCONTENT.toString(),sb.toString());
        map.put(AlertEnum.EMAILCONTENT.toString(),sb.toString());
        map.put(AlertEnum.TITLE.toString().toUpperCase(),alertName);
        map.put(AlertEnum.EVENTIDEN.toString().toUpperCase(),eventId);
        map.put("TxinContent",sb.toString());
        HuaXingAlertParam param = new HuaXingAlertParam();
        param.setAlertName(alertName);
        param.setDuration(duration);
        param.setEndsAt(endsAt);
        param.setSeverity(severity);
        param.setStartsAt(startsAt);
        param.setStatus(status);
        param.setIp(clusterName);
        param.setAlertType("Ranche");
        param.setProjectName(rancher.getRelationName());
        param.setModelClassify("K8S平台");
        param.setModelSystem("T5");
        param.setEventid(eventId);
        if(status.equals(AlertEnum.OK.toString())){
            mwWeixinTemplateDao.updateHuaxingAlertTable(param);
        }else {
            mwWeixinTemplateDao.insertHuaxingAlertTable(param);
        }

        log.info("华星光电消息处理结果：" + sb.toString());
        return map;
    }

    public void sendType(HashMap<String, String> map,HashSet<Integer> userIds){
        ExecutorService pool = Executors.newFixedThreadPool(4);
        if( CollectionUtils.isNotEmpty(userIds)){
            pool.submit(new QyWxSendHuaXingAlertImpl(map, userIds));
            pool.submit(new EmailSendHuaXingImpl(map, userIds,null,null));
            pool.submit(new TXinSendRancherMessageiImpl(map,userIds));
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            log.error("线程错误：" + e);
        }
    }

    @PostMapping("/open/getHtml")
    @ResponseBody
    public ResponseBase getHtml(@RequestParam String dbid) {
        try{
            log.info("dbid：" + dbid);
            long time = System.currentTimeMillis();
            BussinessAlarmInfoParam param = assetsDao.selectBussinessAlarmInfoById(dbid);
            if(param == null) return new ResponseBase(Constants.HTTP_RES_CODE_500,"解析失败", "数据为空");
            StringReader reader = new StringReader(param.getContent());
            InputSource source = new InputSource(reader);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document parse = documentBuilder.parse(source);
            String wechatContent = parse.getElementsByTagName("WECHAT_CONTENT").item(0).getFirstChild().getNodeValue();
            log.info("接口请求花费时间：" + (System.currentTimeMillis() - time));
            return new ResponseBase(Constants.HTTP_RES_CODE_200,"解析成功", wechatContent);
        }catch (Exception e){
            log.error("sendMessage error:{}",e);
            return new ResponseBase(Constants.HTTP_RES_CODE_500,"解析失败", e.getMessage());
        }
    }

    public String getUserName(HashSet<Integer> userIds){
        StringBuffer sb = new StringBuffer();
        if(CollectionUtils.isNotEmpty(userIds)){
            List<UserInfo> userName = mwWeixinTemplateDao.selectUserName(userIds);
            for(UserInfo temp : userName){
                sb.append(temp.getUserName()).append("-").append(temp.getPhoneNumber()).append(",");
            }
            return sb.toString().substring(0,sb.length()-1);
        }
        return sb.toString();
    }


}
