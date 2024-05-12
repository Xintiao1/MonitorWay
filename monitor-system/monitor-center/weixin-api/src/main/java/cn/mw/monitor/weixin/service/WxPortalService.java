package cn.mw.monitor.weixin.service;

import cn.mw.monitor.accountmanage.dao.MwAlerthistory7daysTableDao;
import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.alert.dao.MwAlertActionDao;
import cn.mw.monitor.alert.param.*;
import cn.mw.monitor.api.param.usergroup.QueryGroupParam;
import cn.mw.monitor.common.util.AlertAssetsEnum;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.screen.service.WebSocket;
import cn.mw.monitor.service.action.param.AddAndUpdateAlertActionParam;
import cn.mw.monitor.service.action.param.UserIdsType;
import cn.mw.monitor.service.action.service.CommonActionService;
import cn.mw.monitor.service.alert.api.MWMessageNotifyService;
import cn.mw.monitor.service.alert.dto.*;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.*;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.configmanage.AutoManageSerice;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.model.param.MwModelInterfaceCommonParam;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.timetask.api.MwBaseLineValueService;
import cn.mw.monitor.service.timetask.dto.MwBaseLineHealthValueCommonsDto;
import cn.mw.monitor.service.user.api.MWGroupCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.dto.GroupUserDTO;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.user.dto.MwGroupDTO;
import cn.mw.monitor.user.service.MWGroupService;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.util.entity.EmailFrom;
import cn.mw.monitor.util.entity.GeneralMessageEntity;
import cn.mw.monitor.weixin.message.*;
import cn.mw.monitor.weixin.service.impl.EmailSendHuaXingImpl;
import cn.mw.monitor.weixin.service.impl.QyWxSendHuaXingAlertImpl;
import cn.mw.monitor.weixin.service.impl.TXinSendRancherMessageiImpl;
import cn.mw.monitor.weixin.util.AliyunApi;
import cn.mw.monitor.weixinapi.DelFilter;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;
import cn.mw.monitor.server.service.impl.MwServerManager;
import cn.mw.monitor.service.common.ListenerService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.util.RedisUtils;
import cn.mw.monitor.weixin.dao.MwWeixinTemplateDao;
import cn.mw.monitor.weixin.dao.MwWeixinUserDao;
import cn.mw.monitor.weixin.entity.*;
import cn.mw.monitor.weixin.entity.menu.Button;
import cn.mw.monitor.weixin.entity.menu.ViewButton;
import cn.mw.monitor.weixin.entity.message.*;
import cn.mw.monitor.weixin.util.LoadUtil;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.thoughtworks.xstream.XStream;
import gzcb.query.SmsSenderDelegate;
import gzcb.query.SmsSenderService;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.binary.Base64;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author bkc
 * @create 2020-06-30 11:17
 */
@Service
@Transactional
public class WxPortalService extends ListenerService implements MWMessageNotifyService {

    private static final Logger log = LoggerFactory.getLogger("MWWxController");

    @Autowired
    private AliyunApi aliyunApi;

    @Autowired
    private AutoManageSerice autoManageSerice;

    @Value("${qyweixin.ID}")
    private String qyweixinId;

    @Value("${qyweixin.SECRET}")
    private String qyweixinSecret;

    @Value("${qyweixin.AGENTID}")
    private Integer qyweixinAgentId;

    @Value("${weixin.APPID}")
    private String APPID;

    @Value("${weixin.APPSECRET}")
    private String APPSECRET;

    @Value("${weixin.TOKEN}")
    private String TOKEN;

    @Value("${weixin.ACCESSTOKEN}")
    private String GET_TOKEN_URL;

    @Value("${weixin.codeToToken}")
    private String code_to_token;

    @Value("${weixin.createMenu}")
    private String create_menu_url;

    @Value("${weixin.redirectUrl}")
    private String redirect_url;

    @Value("${qyweixin.url}")
    private String qyUrl;

    /*
     * @describe
     * 引导客户点击此连接，用户同意后 ，即可重定向到我们指定的页面 并且附带参数code
     * (利用code 获取access_token 从而进一步获取微信服务器用户信息,注意：获取code 只有五分钟时效性)
     */
    @Value("${weixin.getCodeUrl}")
    private String get_code_url;

    @Value("${weixin.getHy}")
    private String get_hy;

    @Value("${weixin.setHy}")
    private String set_hy;

    @Value("${weixin.getUserInfo}")
    private String get_user_info;

    @Value("${weixin.getListUser}")
    private String get_list_user;

    @Value("${weixin.getListTemplate}")
    private String get_list_template;

    @Value("${weixin.sendTemUrl}")
    private String send_tem_url;

    @Value("${mode.isjump}")
    private Boolean modeJump;

    //@Value("${qyweixin.sendMessageUrl}")
    private String send_qyweixin_message = "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=ACCESS_TOKEN";

    //@Value("${qyweixin.ACCESSTOKEN}")
    private String qyweixinToken = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=ID&corpsecret=SECRET";

    @Resource
    private MwWeixinUserDao mwWeixinUserDao;

    @Resource
    private MwTangibleAssetsTableDao mwTangibleAssetsTableDao;

    @Autowired
    private MwModelViewCommonService mwModelViewCommonService;

    @Resource
    private MwAlertActionDao mwAlertActionDao;

    @Resource
    private MWAlertAssetsDao assetsDao;

    @Resource
    private MwWeixinTemplateDao mwWeixinTemplateDao;

    @Autowired
    private MwModelCommonService mwModelCommonService;

    @Autowired
    private WeixinUserService weixinUserService;

    @Autowired
    private EmailService emailService;

    @Autowired
    MWGroupCommonService mwGroupCommonService;

    @Autowired
    private MWOrgCommonService mwOrgService;

    @Autowired
    MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    MwAlerthistory7daysTableDao mwAlerthistory7daysTableDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwBaseLineValueService mwBaseLineValueService;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private CommonActionService commonActionService;

    @Autowired
    private MWOrgService orgService;

    @Autowired
    private MWGroupService groupService;

    public static final int ADMIN = 106;

    private static String USERID = "106";

    @Value("${mode.webSocket}")
    private Boolean iswebSocket;

    @Autowired
    private MwTangibleAssetsService mwTangibleAssetsService;

    @Value("${alert.level}")
    private String alertLevel;


    /**
     * 向处暴露的获取token的方法
     *
     * @return
     */
    public String getAccessToken() {
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        boolean isHas = redisUtils.hasKey("AccessToken");
        if (!isHas) {
            getToken();
        }
        String token = (String) redisUtils.get("AccessToken");
        return token;
    }

    /**
     * 向处暴露的获取企业微信token的方法
     *
     * @return
     */
    public String getQyWeixinAccessToken(GeneralMessageEntity qiEntity) {
        synchronized (WxPortalService.class){
            RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
            boolean isHas = redisUtils.hasKey(qiEntity.getId() + "QyWeixinAccessToken");
            if (!isHas) {
                getQyWeixinToken(qiEntity);
            }
            String token = (String) redisUtils.get(qiEntity.getId() + "QyWeixinAccessToken");
            return token;
        }
    }

    /**
     * 访问微信端获取获取token并且存储起来
     */
    private void getToken() {
        String url = GET_TOKEN_URL.replace("APPID", APPID).replace("APPSECRET", APPSECRET);

        String tokenStr = LoadUtil.get(url);
        JSONObject jsonObject = JSONObject.parseObject(tokenStr);
        String token = jsonObject.getString("access_token");
        String expireIn = jsonObject.getString("expires_in");

        //将token放入redis中保存
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        redisUtils.set("AccessToken", token, Long.parseLong(expireIn));
    }

    /**
     * 访问企业微信端获取获取token并且存储起来
     */
    private void getQyWeixinToken(GeneralMessageEntity qiEntity) {
        String url = qyweixinToken.replace("ID", qiEntity.getId()).replace("SECRET", qiEntity.getSecret());

        String tokenStr = LoadUtil.get(url);
        JSONObject jsonObject = JSONObject.parseObject(tokenStr);
        String token = jsonObject.getString("access_token");
        String expireIn = jsonObject.getString("expires_in");

        //将token放入redis中保存
        RedisUtils redisUtils = SpringUtils.getBean(RedisUtils.class);
        redisUtils.set(qiEntity.getId() + "QyWeixinAccessToken", token, Long.parseLong(expireIn));
    }

    /**
     * 验证签名
     *
     * @param timestamp
     * @param nonce
     * @param signature
     * @return
     */
    public boolean check(String timestamp, String nonce, String signature) {
        //1 将token、timestamp、nonce三个参数进行字典序排序

        String[] strs = new String[]{TOKEN, timestamp, nonce};
        Arrays.sort(strs);
        //2 将三个参数字符串拼接成一个字符串进行sha1加密
        String str = strs[0] + strs[1] + strs[2];
        String mysig = sha1(str);
        //3 开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
        return mysig.equalsIgnoreCase(signature);
    }

    /**
     * 进行sha1加密
     *
     * @param str
     * @return
     */
    private static String sha1(String str) {
        try {
            //获取一个加密对象
            MessageDigest md = MessageDigest.getInstance("sha1");
            //加密
            byte[] digest = md.digest(str.getBytes());
            char[] chars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            StringBuilder sb = new StringBuilder();
            //处理加密结果
            for (byte b : digest) {
                sb.append(chars[(b >> 4) & 15]);
                sb.append(chars[b & 15]);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("加密失败:",e);
        }
        return null;
    }

    /**
     * 解析xml数据包
     *
     * @param requestBody
     * @return
     */
    public static Map<String, String> parseRequest(String requestBody) {
        Map<String, String> map = new HashMap<>();
        try {
            //根据xml字符串获取dom对象根节点
            Element root = DocumentHelper.parseText(requestBody).getRootElement();
            //获取根节点的所有的子节点
            List<Element> elements = root.elements();
            for (Element e : elements) {
                map.put(e.getName(), e.getStringValue());
            }
        } catch (DocumentException e) {
            log.error("失败:",e);
        }
        return map;
    }

    /**
     * 用于处理所有的事件和消息的回复
     *
     * @param requestMap
     * @return 返回的是xml数据包
     */
    public static String getRespose(Map<String, String> requestMap) {
        BaseMessage msg = null;
        String msgType = requestMap.get("MsgType");
        switch (msgType) {
            //处理文本消息
            case "text":
                //msg=dealTextMessage(requestMap);
                break;
            case "image":
                //msg=dealImage(requestMap);
                break;
            case "voice":

                break;
            case "video":

                break;
            case "shortvideo":

                break;
            case "location":

                break;
            case "link":

                break;
            case "event":
                //msg = dealEvent(requestMap);
                break;
            default:
                break;
        }

        //把消息对象处理为xml数据包
        if (msg != null) {
            return beanToXml(msg);
        }
        return null;
    }

    /**
     * 处理事件推送
     *
     * @param requestMap
     * @return
     */
    private static BaseMessage dealEvent(Map<String, String> requestMap) {
        String event = requestMap.get("Event");
        BaseMessage msg = null;
        switch (event) {
            case "CLICK":
                //处理点击事件
                return dealClick(requestMap);
            case "VIEW":
                break;
            default:
                break;
        }
        return msg;
    }

    private static BaseMessage dealClick(Map<String, String> requestMap) {
        String key = requestMap.get("EventKey");
        switch (key) {
            case "1":
                //处理点击了第一个一级菜单
                return new TextMessage(requestMap, "你点击了第一个一级菜单");
            case "32":
                //处理点击了第三个一级菜单的第二个子菜单
                return new TextMessage(requestMap, "你点击了第三个一级菜单的第二子菜单");
            default:
                break;
        }
        return null;
    }

    /**
     * 把消息对象处理为xml数据包
     *
     * @param msg
     * @return
     */
    private static String beanToXml(BaseMessage msg) {
        XStream stream = new XStream();
        //设置需要处理XStreamAlias("xml")注释的类
        stream.processAnnotations(TextMessage.class);
        stream.processAnnotations(ImageMessage.class);
        stream.processAnnotations(MusicMessage.class);
        stream.processAnnotations(NewsMessage.class);
        stream.processAnnotations(VideoMessage.class);
        stream.processAnnotations(VoiceMessage.class);
        String xml = stream.toXML(msg);

        return xml;
    }

    /**
     * 处理文本消息
     *
     * @param requestMap
     * @return
     */
    private static BaseMessage dealTextMessage(Map<String, String> requestMap) {
        //用户发来的内容
        String msg = requestMap.get("Content");
        return null;
    }

    public List<MwTangibleassetsDTO> getTangibleDtos(List<MwTangibleassetsTable> mwTangibleassetsDTOS){
        if(CollectionUtils.isEmpty(mwTangibleassetsDTOS)){
            return null;
        }
        List<MWOrgDTO> mworgDtos = orgService.getAllOrgList();
        List<MwTangibleassetsDTO> resultTemp = new ArrayList<>();
        QueryGroupParam qsDTO = new QueryGroupParam();
        qsDTO.setPageSize(99999);
        List<MwGroupDTO> mwScanList = new ArrayList<>();
        Reply reply = groupService.selectList(qsDTO);
        if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
            PageInfo pageInfo = (PageInfo) reply.getData();
            mwScanList = pageInfo.getList();
        }
        for(MwTangibleassetsTable tangTable : mwTangibleassetsDTOS){
            MwTangibleassetsDTO result = new MwTangibleassetsDTO();
            BeanUtils.copyProperties(tangTable,result);
            List<OrgDTO> orgDTOS = new ArrayList<>();
            List<GroupDTO> groupDtos = new ArrayList<>();
            List<List<Integer>> modelViewOrgIds = tangTable.getModelViewOrgIds();
            if(CollectionUtils.isNotEmpty(modelViewOrgIds) && CollectionUtils.isNotEmpty(mworgDtos)){
                for(List<Integer> mos : modelViewOrgIds){
                    List<MWOrgDTO> childList = mworgDtos;
                    for(int i=0;i<mos.size();i++){
                        Integer orgId = mos.get(i);
                        MWOrgDTO dto = mworgDtos.stream().filter(u -> u.getOrgId().equals(orgId)).findFirst().orElse(null);
                        if(dto != null && CollectionUtils.isNotEmpty(dto.getChilds())){
                            childList.addAll(dto.getChilds());
                        }
                        if(i == mos.size() - 1){
                            OrgDTO reOrgDto = new OrgDTO();
                            if(null != dto){
                                BeanUtils.copyProperties(dto,reOrgDto);
                                orgDTOS.add(reOrgDto);
                            }
                        }
                    }
                }
                result.setDepartment(orgDTOS);
            }
            List<Integer> modelViewGroupIds = tangTable.getModelViewGroupIds();
            if(CollectionUtils.isNotEmpty(modelViewGroupIds) && CollectionUtils.isNotEmpty(mwScanList)){
                List<MwGroupDTO> finalMwScanList = mwScanList;
                groupDtos = modelViewGroupIds.parallelStream().map(groupId ->{
                    MwGroupDTO dto = finalMwScanList.stream().filter(u -> u.getGroupId().equals(groupId)).findFirst().orElse(null);
                    GroupDTO groupTemp = new GroupDTO();
                    if(null != dto){
                        BeanUtils.copyProperties(dto,groupTemp);
                    }
                    return groupTemp;
                }).collect(Collectors.toList());
                result.setGroup(groupDtos);
            }

            if(result.getAssetsId() != null){
                resultTemp.add(result);
            }
        }

        return resultTemp;
    }

    public MwTangibleassetsDTO getTangibleDto(List<MwTangibleassetsDTO> getTangibleDtos, String hostid, String hostip, String webName){
        if(webName != null){
            log.info("获取的web资产名称：" + webName);
            MwTangibleassetsDTO temp = new MwTangibleassetsDTO();
            for(MwTangibleassetsDTO dto : getTangibleDtos){
                log.info("资产子类型Id：" + dto.getAssetsTypeSubId());
                log.info("资产名称：" + dto.getAssetsName());
                if(dto.getAssetsTypeSubId() != null && dto.getAssetsName() != null && dto.getAssetsTypeSubId().equals(72) && dto.getAssetsName().equals(webName)){
                    BeanUtils.copyProperties(dto,temp);
                    log.info("获取的web资产：" + dto);
                    return temp;
                }
            }
        }
        for(MwTangibleassetsDTO dto : getTangibleDtos){
            if(dto.getInBandIp() != null && dto.getInBandIp().equals(hostip) && dto.getAssetsId().equals(hostid)){
                return dto;
            }
        }
        return null;
    }

    public void dealMessage(List<String> msg){
        dealMessage(msg,null,null);
    }

    public void dealMessage(List<String> msgs, ActionLevelRuleParam alr, HashSet<Integer> userIds){
        //获取虚拟化资产
        MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
        mwCommonAssetsDto.setUserId(ADMIN);
        mwCommonAssetsDto.setAlertQuery(true);
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
        mwTangibleassetsDTOS = mwAssetsManager.getAllAssetsByUserId(mwCommonAssetsDto);
        List<MwTangibleassetsDTO> tangibleDtos = getTangibleDtos(mwTangibleassetsDTOS);
        if(CollectionUtils.isEmpty(tangibleDtos)) {
            log.info("tangibleDtos为空");
            return;
        }
        List<Map> monitorMapList = assetsDao.getMonitorServerName();
        Map<Integer,String> monitorMap = MonitorServerNameUtil.listMapConvertMap(monitorMapList);
        //判断是否为分级告警
        log.info("dealMessage alr:" + alr);
        List<MwRuleSelectListParam> selectListParams = new ArrayList<>();
        if (alr == null) {
            selectListParams = mwAlertActionDao.selectMwAlertAction(null);
            alr = new ActionLevelRuleParam();
        } else {
            selectListParams = mwAlertActionDao.selectMwAlertAction(alr.getActionId());
        }
        for(String msg : msgs){
            try{
                //将告警信息封装成map去操作
                log.info("发送告警dealMessage msg:" + msg);
                msg = toJsonString(msg);
                HashMap<String, String> map = new HashMap<>();

                msg = converUnicodeToChar(msg);

                String[] strs = msg.split(",");
                String regex = ":";
                for (String s : strs) {
                    String s1 = s.substring(0, s.indexOf(regex) + 1).replaceAll(":", "");
                    String s2 = s.substring(s.indexOf(regex) + 1);
                    map.put(s1, s2);
                }
                try {

                    autoManageSerice.triggerAuto(map);
                }catch (Exception e){
                    log.error("autoManageSerice失败",map);
                }

                try{
                    mwModelCommonService.getModelAssetsChangeMessage(map);
                }catch (Exception e){
                    log.error("获取资产cpu内存变更记录数据失败",map);
                }
                if(CollectionUtils.isEmpty(selectListParams)){
                    log.info("selectListParams为空");
                    return;
                }
                //根据资产id和ip去查询发送信息的规则
                //1 判断资产是否存在 资产不存在不发送信息
                log.info("发送告警dealMessage map:" + map);
                Boolean isAlarm = map.get(AlertEnum.ALERTTITLE.toString()) == null ? map.get(AlertEnum.RECOVERYTITLE.toString()) == null ? null : false : true;
                String title = "";
                if (isAlarm) {
                    title = AlertEnum.ALERTTITLE.toString();
                } else {
                    title = AlertEnum.RECOVERYTITLE.toString();
                }
                String hostid = map.get(AlertEnum.HOSTID.toString());
                String hostip = map.get(AlertEnum.HOSTIP.toString());
                if(map.get(title).contains(AlertEnum.VR.toString())){
                    String hostName = map.get(AlertEnum.HOSTNAME.toString());
                    hostName = hostName.substring(hostName.indexOf("<")+1,hostName.lastIndexOf(">"));
                    String regexIp = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
                    Pattern pattern = Pattern.compile(regexIp);
                    Matcher matcher = pattern.matcher(hostName);
                    log.info("虚拟化资产ip处理前：" + hostName);
                    if(matcher.find()){
                        hostip = matcher.group();
                        log.info("虚拟化资产ip处理后：" + hostip);
                    }
                }
                log.info("dealMessage,hostid:{},hostip:{}", hostid, hostip);
                HashMap<String, Object> assetsMap = new HashMap<>();
                String webName = null;
                if (map.get(title).contains(AlertEnum.WebMonitor.toString())) {
                    String[] s = map.get(title).split("]");
                    webName = s[1].substring(1,s[1].length());
                }
                MwTangibleassetsDTO assets = getTangibleDto(tangibleDtos,hostid,hostip,webName);
                List<MwAssetsLabelDTO> labelList = new ArrayList<>();
                String monitorServerName = null;
                String assetsTypeName = null;
                List<String> labelValue = new ArrayList<>();
                Map<String, String> itemMap = new HashMap();
                Map<String, String> itemSelectMap = new HashMap();
                List<String> orgNames = new ArrayList<>();
                List<String> groupNames = new ArrayList<>();
                String assetsIp = null;
                String assetsName = null;
                if (assets != null) {
                    log.info("isAlarm:" + isAlarm);
                    log.info("alertLevel:" + alertLevel);
                    String key = hostid + AlertAssetsEnum.Dash.toString() + hostip;
                    MWAlertAssetsParam.tangibleassetsDTOMap.put(key,assets);
                    log.info("dealMessage,assets：" + assets.debugInfo());
                    assetsIp = assets.getInBandIp();
                    monitorServerName = assets.getMonitorServerName()==null ? monitorMap.get(assets.getMonitorServerId()) : assets.getMonitorServerName();
                    assetsName = assets.getAssetsName();
                    assetsTypeName = assets.getAssetsTypeName();
                    if(StringUtils.isNotBlank(assets.getModelTag())){
                        labelValue.add(assets.getModelTag());
                    }
                    List<OrgDTO> department = assets.getDepartment();
                    List<GroupDTO> groupIdsMap = assets.getGroup();
                    if(assets.getId() != null){
                        //labelList = mwLabelCommonServcie.getLabelBoard(assets.getId(), "ASSETS");
                        labelList = assets.getAssetsLabel();
                        if (CollectionUtils.isNotEmpty(labelList)) {
                            for (MwAssetsLabelDTO s : labelList) {
                                if (s.getTagboard() != null) {
                                    labelValue.add(s.getTagboard());
                                }
                                if (s.getDropValue() != null) {
                                    labelValue.add(s.getDropValue());
                                }
                                if (s.getDateTagboard() != null) {
                                    labelValue.add(s.getDateTagboard().toString());
                                }
                            }
                            log.info("dealMessage labelList size：" + labelList.size());
                        }
                        Map tempMap = new HashMap();
                        List list = new ArrayList();
                        list.add(assets.getId());
                        tempMap.put("ids", list);
                        Reply reply = mwTangibleAssetsService.selectListWithExtend(tempMap);
                        if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                            List<MwTangibleassetsDTO> datas = (List) reply.getData();
                            MwTangibleassetsDTO temp = new MwTangibleassetsDTO();
                            if(CollectionUtils.isNotEmpty(datas)){
                                temp = datas.get(0);
                                if(CollectionUtils.isEmpty(department)){
                                    department = temp.getDepartment();
                                }
                                if(CollectionUtils.isEmpty(groupIdsMap)){
                                    groupIdsMap = temp.getGroup();
                                }
                            }
                        }

                    }
                    if(CollectionUtils.isNotEmpty(department)){
                        for(OrgDTO org : department){
                            orgNames.add(org.getOrgName());
                        }
                    }
                    if(CollectionUtils.isNotEmpty(groupIdsMap)){
                        for (GroupDTO groupDTO : groupIdsMap){
                            groupNames.add(groupDTO.getGroupName());
                        }
                    }
                    //取该笔资产基线
                    Reply reply = mwBaseLineValueService.selectBaseLineHealthValue(null, hostid);
                    List<MwBaseLineHealthValueCommonsDto> commonsDtos = new ArrayList<>();
                    if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                        commonsDtos = (List<MwBaseLineHealthValueCommonsDto>) reply.getData();
                        if (CollectionUtils.isNotEmpty(commonsDtos)) {
                            for (MwBaseLineHealthValueCommonsDto itemp : commonsDtos) {
                                itemSelectMap.put(itemp.getItemName(), itemp.getValue());
                            }
                        }
                    }

                    assetsMap.put(AlertEnum.DATASOURCES.toString(), monitorServerName);
                    assetsMap.put(AlertEnum.ASSETS.toString(), assetsName);
                    assetsMap.put(AlertEnum.ASSETSTYPE.toString(), assetsTypeName);
                    assetsMap.put(AlertEnum.LABEL.toString(), labelValue);
                    assetsMap.put(AlertEnum.ORG.toString(), orgNames);
                    assetsMap.put(AlertEnum.GROUP.toString(), groupNames);
                    assetsMap.put(AlertEnum.ALERTTITLE.toString(),map.get(title));
                    assetsMap.put(AlertEnum.MODELSYSTEM.toString(), assets.getModelSystem());
                    String ifMode = null;
                    String alertTag = "false";
                    String interfaceDesc = null;
                    String keyDevices = null;
                    if(assets.getIsKeyDevices() != null){
                        keyDevices = assets.getIsKeyDevices().toString();
                    }
                    if(map.get(title).toString().contains(AlertEnum.Interface.toString()) && map.get(title).toString().contains(AlertAssetsEnum.LeftBracket.toString())) {
                        String nameMode = map.get(title).toString().substring(map.get(title).toString().lastIndexOf(AlertAssetsEnum.LeftBracket.toString())+1,map.get(title).toString().indexOf(AlertAssetsEnum.RightBracket.toString()));
                        log.info("资产主键id:" + assets.getId());
                        log.info("nameMode:" + nameMode);
                        ifMode = mwTangibleAssetsTableDao.getIfMode(assets.getId(),nameMode);
                        log.info("ifMode:" + ifMode);
                        log.info("modeJump:" + modeJump);
                        if(modeJump && ifMode == null){
                            continue;
                        }
                        MwModelInterfaceCommonParam interfaceCommonParam = new MwModelInterfaceCommonParam();
                        interfaceCommonParam.setHostIp(hostip);
                        interfaceCommonParam.setHostId(hostid);
                        List<MwModelInterfaceCommonParam> interfaceCommonParams = mwModelCommonService.queryInterfaceInfoAlertTag(interfaceCommonParam);
                        //log.info("接口描述返回数据：" + interfaceCommonParams);
                        if(CollectionUtils.isNotEmpty(interfaceCommonParams)){
                            for(MwModelInterfaceCommonParam temp : interfaceCommonParams){
                                //log.info("接口描述返回数据temp：" + temp);
                                if(temp.getInterfaceName() != null && map.get(title).toString().contains(temp.getInterfaceName()+ "-")){
                                    if(temp.getAlertTag() != null){
                                        alertTag = temp.getAlertTag().toString();
                                    }
                                    interfaceDesc = temp.getInterfaceDesc();
                                    log.info("接口描述返回数据interfaceDesc：" + interfaceDesc);
                                    break;
                                }
                            }
                        }
                    }else{
                        alertTag = "true";
                    }
                    assetsMap.put(AlertEnum.ALERT_TAG.toString(), alertTag);
                    assetsMap.put(AlertEnum.InterfaceModeDesc.toString(), interfaceDesc);
                    assetsMap.put(AlertEnum.InterfaceMode.toString(),ifMode);
                    assetsMap.put(AlertEnum.KEYDEVICES.toString(),keyDevices);
                    MessageContext messageContext = new MessageContext();
                    messageContext.setKey(assetsMap);
                    log.info("dealMessage assetsMap:" + assetsMap);
                    //判断筛选条件是否为空，不为空则匹配筛选条件
                    log.info("dealMessage selectListParams size:" + selectListParams.size());
                    log.info("dealMessage selectListParams:" + selectListParams);
                    if (selectListParams != null && selectListParams.size() > 0) {
                        for (MwRuleSelectListParam selectListParam : selectListParams) {
                            log.info("dealMessage selectListParam:" + selectListParam.getActionId());
                            if (selectListParam.getState() != 1) {
                                log.info("State:" + selectListParam.getState());
                                continue;
                            }

                            if (selectListParam.getEffectTimeSelect() != null) {
                                if (selectListParam.getEffectTimeSelect().equals(AlertEnum.FixedTime.toString())) {
                                    Date now = new Date();
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                                    String strNow = sdf.format(now);
                                    Date startDate = null;
                                    Date endDate = null;
                                    try {
                                        now = sdf.parse(strNow);
                                        startDate = sdf.parse(selectListParam.getStartTime());
                                        endDate = sdf.parse(selectListParam.getEndTime());
                                    }catch (Exception e){
                                        log.error("时间转换错误:{}",e);
                                    }
                                    if (!(now.after(startDate) && now.before(endDate))) {
                                        continue;
                                    }
                                }
                            }
                            List<MwRuleSelectParam> ruleSelectList = mwAlertActionDao.selectMwAlertRuleSelect(selectListParam.getActionId());
                            Boolean resultBoolean = true;
                            if (ruleSelectList.size() > 2) {
                                log.info("ruleSelectParams star");
                                List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
                                List<String> itemNameList = new ArrayList<>();
                                String hostIPSelect = null;
                                for (MwRuleSelectParam s : ruleSelectList) {
                                    if (s.getParentKey().equals(AlertEnum.ROOT.toString())) {
                                        ruleSelectParams.add(s);
                                    }
                                    if(s.getName() != null){
                                        if(s.getName().contains(AlertEnum.TARGET.toString())){
                                            String itemName = s.getName().substring(s.getName().lastIndexOf(AlertAssetsEnum.Comma.toString())+1);
                                            String hostIPSelectTemp = s.getName().substring(s.getName().lastIndexOf(AlertAssetsEnum.Dash.toString())+1,s.getName().lastIndexOf(AlertAssetsEnum.Comma.toString()));
                                            //判断产生告警的资产与筛选条件的资产是否一致
                                            if(hostIPSelectTemp.equals(assetsIp)){
                                                hostIPSelect = hostIPSelectTemp;
                                                itemNameList.add(itemName);
                                                if(s.getValue().equals(AlertEnum.BASELINE.toString())){
                                                    s.setValue(itemSelectMap.get(itemName));
                                                }
                                            }
                                        }
                                    }
                                }
                                //判断产生告警的资产与筛选条件的资产是否一致
                                if(StringUtils.isNotEmpty(hostIPSelect) && hostIPSelect.equals(assetsIp)){
                                    MWZabbixAPIResult itemResult = mwtpServerAPI.itemGetbySearch(assets.getMonitorServerId(), itemNameList, hostid);
                                    if (itemResult.getCode() == 0) {
                                        JsonNode jsonNode = (JsonNode) itemResult.getData();
                                        if (jsonNode.size() > 0) {
                                            jsonNode.forEach(jsonNode1 -> {
                                                String name = jsonNode1.get(AlertEnum.NAME.toString()).asText();
                                                String lastValue = jsonNode1.get(AlertEnum.Lastvalue.toString()).asText();
                                                itemMap.put(name,lastValue);
                                            });
                                        }
                                    }
                                }
                                assetsMap.put(AlertEnum.TARGET.toString() + AlertAssetsEnum.Dash.toString() + assetsIp, itemMap);
                                for (MwRuleSelectParam s : ruleSelectParams) {
                                    s.setConstituentElements(getChild(s.getKey(), ruleSelectList));
                                }
                                resultBoolean = DelFilter.delFilter(ruleSelectParams, messageContext, ruleSelectList);
                                log.info("ruleSelectParams:" + ruleSelectParams);
                                log.info("messageContext:" + messageContext);
                                log.info("ruleSelectParams:" + ruleSelectParams);
                                log.info("result:" + resultBoolean);
                            }
                            if (resultBoolean) {
                                Integer successNum = selectListParam.getSuccessNum() == null ? 0 : selectListParam.getSuccessNum();
                                selectListParam.setSuccessNum(successNum + 1);
                                //判断是否需要告警压缩
                                if (selectListParam.getAlarmCompressionSelect() != null) {
                                    if (selectListParam.getAlarmCompressionSelect().equals(AlertEnum.CUSTOM.toString())) {
                                        MwRuleSelectEventParam mwRuleSelectEventParam = new MwRuleSelectEventParam();
                                        MwRuleSelectEventParam mwruleParam = mwAlertActionDao.selectMwAlertRuleSelectEventBytitle(map.get(title), assetsIp, isAlarm, selectListParam.getActionId());

                                        if (mwruleParam != null) {
                                            if(alertLevel.equals(AlertEnum.CHENGDUGUANWEI.toString())){
                                                Date date = new Date();
                                                GregorianCalendar gc = new GregorianCalendar();
                                                gc.setTime(date);
                                                gc.add(GregorianCalendar.MINUTE, -selectListParam.getCustomTime());
                                                Date gcDate = gc.getTime();
                                                Date resultDate = mwruleParam.getDate();
                                                if(resultDate.getTime() <= gcDate.getTime()){
                                                    MessageExecuter messageExecuter = new MessageExecuter();
                                                    alr.setActionId(selectListParam.getActionId());
                                                    messageExecuter.execute(msg, alr, userIds, null);
                                                    mwAlertActionDao.deleteMwAlertRuleSelectEvent(mwruleParam);
                                                }
                                            }else{
                                                mwruleParam.setSize(mwruleParam.getSize() + 1);
                                                mwAlertActionDao.upMwAlertRuleSelectEvent(mwruleParam);
                                            }

                                        } else {
                                            if(alertLevel.equals(AlertEnum.CHENGDUGUANWEI.toString())){
                                                MessageExecuter messageExecuter = new MessageExecuter();
                                                alr.setActionId(selectListParam.getActionId());
                                                messageExecuter.execute(msg, alr, userIds, null);
                                            }
                                            mwRuleSelectEventParam.setHostid(hostid);
                                            mwRuleSelectEventParam.setIp(assetsIp);
                                            mwRuleSelectEventParam.setIsAlarm(isAlarm);
                                            mwRuleSelectEventParam.setText(msg);
                                            mwRuleSelectEventParam.setTitle(map.get(title));
                                            mwRuleSelectEventParam.setSize(1);
                                            mwRuleSelectEventParam.setDate(new Date());
                                            mwRuleSelectEventParam.setUuid(selectListParam.getActionId());
                                            mwAlertActionDao.insertMwAlertRuleSelectEvent(mwRuleSelectEventParam);
                                        }
                                        continue;
                                    }
                                }

                                MessageExecuter messageExecuter = new MessageExecuter();
                                alr.setActionId(selectListParam.getActionId());
                                messageExecuter.execute(msg, alr, userIds, null);
                                //return;
                            } else {
                                Integer failsNum = selectListParam.getFailNum() == null ? 0 : selectListParam.getFailNum();
                                selectListParam.setFailNum(failsNum + 1);
                            }
                            mwAlertActionDao.upMwAlertAction(selectListParam);
                        }
                    } /*else {
                        MessageExecuter messageExecuter = new MessageExecuter();
                        log.info("dealMessage 执行发送");
                        messageExecuter.execute(msg, alr, userIds, null);
                    }*/
                }
            }catch (Exception e){
                log.error("con:{}", e);
                try{
                    //将解析数据中出错的告警保存记录表
                    AlertRecordTable recored = new AlertRecordTable();
                    recored.setDate(new Date());
                    recored.setMethod(AlertEnum.NotSent.toString());
                    recored.setText(msg);
                    recored.setIsSuccess(1);
                    recored.setHostid(AlertAssetsEnum.MinusOne.toString());
                    recored.setError(e.getMessage());
                    recored.setIp(null);
                    recored.setTitle(null);
                    recored.setIsAlarm(null);
                    insertRecord(recored);
                } catch (Exception exc) {
                    log.error("保存解析数据尚未发送时出错:{}", exc);
                }
            }
        }


    }

    public void sendMessage(MessageContext messageContext){
        List<MwRuleSelectListParam> selectListParams = mwAlertActionDao.selectMwAlertAction(null);
        MwRuleSelectListParam matchRule = null;
        if (selectListParams != null && selectListParams.size() > 0) {
            boolean resultBoolean = false;
            for (MwRuleSelectListParam selectListParam : selectListParams) {
                List<MwRuleSelectParam> ruleSelectList = mwAlertActionDao.selectMwAlertRuleSelect(selectListParam.getActionId());
                if (ruleSelectList.size() > 2) {
                    List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
                    for (MwRuleSelectParam s : ruleSelectList) {
                        if (s.getParentKey().equals("root")) {
                            ruleSelectParams.add(s);
                        }
                    }

                    for (MwRuleSelectParam s : ruleSelectParams) {
                        s.setConstituentElements(getChild(s.getKey(), ruleSelectList));
                    }

                    resultBoolean = DelFilter.delFilter(ruleSelectParams, messageContext, ruleSelectList);
                    if(resultBoolean){
                        matchRule = selectListParam;
                        break;
                    }
                }
            }

            if(resultBoolean){
                AddAndUpdateAlertActionParam alertActionParam = commonActionService.selectPopupAction(matchRule.getActionId());
                messageContext.addKey(AddAndUpdateAlertActionParam.MESSAGECONTEXT_KEY ,alertActionParam);
                MessageExecuter messageExecuter = new MessageExecuter();
                messageExecuter.executeMatchedActionId(messageContext ,alertActionParam);
            }
        }
    }

    private static List<MwRuleSelectParam> getChild(String key, List<MwRuleSelectParam> rootList){
        List<MwRuleSelectParam> childList = new ArrayList<>();
        for(MwRuleSelectParam s : rootList){
            if(s.getParentKey().equals(key)){
                childList.add(s);
            }
        }
        for(MwRuleSelectParam s : childList){
            s.setConstituentElements(getChild(s.getKey(),rootList));
        }
        if(childList.size() == 0){
            return null;
        }
        return childList;

    }


    //将json格式多余的双引号去掉
    private static String toJsonString(String s) {
        char[] tempArr = s.toCharArray();
        int tempLength = tempArr.length;
        for (int i = 0; i < tempLength; i++) {
            if (tempArr[i] == ':' && tempArr[i + 1] == '"') {
                for (int j = i + 2; j < tempLength; j++) {
                    if (tempArr[j] == '"') {
                        if (tempArr[j + 1] != ',' && tempArr[j + 1] != '}') {
                            tempArr[j] = '”'; // 将value中的 双引号替换为中文双引号
                        } else if (tempArr[j + 1] == ',' || tempArr[j + 1] == '}') {
                            break;
                        }
                    }
                }
            }
        }
        return new String(tempArr);
    }

    public HashSet<Integer> getUserIds(String typeId){
        HashSet<Integer> userIds = mwAlertActionDao.selectUserMapper(typeId);
        List<Integer> groupIds = mwAlertActionDao.selectGroupMapper(typeId);
        for (Integer groupid : groupIds) {
            Reply selectGroupUser = mwGroupCommonService.selectGroupUser(groupid);
            if (selectGroupUser.getRes() == 0) {
                List<GroupUserDTO> groupUserData = (List<GroupUserDTO>) selectGroupUser.getData();
                if (null != groupUserData && groupUserData.size() > 0) {
                    for (GroupUserDTO pri : groupUserData) {
                        userIds.add(pri.getUserId());
                    }
                }
            }
        }
        List<Integer> orgIds = mwAlertActionDao.selectOrgMapper(typeId);
        List<Integer> orgUserIDs = mwOrgService.selectPubUserIdByOrgId(orgIds);
        if (null != orgUserIDs && orgUserIDs.size() > 0) {
            for (Integer pri : orgUserIDs) {
                userIds.add(pri);
            }
        }
        return userIds;
    }


    private void dealDXMessage(HashMap<String, String> map, HashSet<Integer> userIds,
                               HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) {
        try {
            //根据系统用户id,查询手机号
            List<String> phones = mwWeixinTemplateDao.selectPhones(userIds);
            HashSet<String> sendPhones = (HashSet<String>) phones.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());

            //整理发送消息内容
            if (map.get("告警标题") != null) {
                //告警等级满足规则
                String level = map.get("告警等级");
                if (!severity.contains(level)) {
                    return;
                }

                //拼接发送信息
                String wtxq = getWtxq(map.get("问题详情"));
                map.put("问题详情", wtxq);
                map.put("主机名称", assets.getHostName());
                map.put("IP地址", assets.getInBandIp());
                StringBuffer content = new StringBuffer();
                content.append("告警标题:").append(map.get("告警标题")).append(",").append('\n')
                        .append("告警信息:").append(map.get("告警信息")).append(",").append('\n')
                        .append("告警等级:").append(map.get("告警等级")).append(",").append('\n')
                        .append("IP地址:").append(map.get("IP地址")).append(",").append('\n')
                        .append("告警时间:").append(map.get("告警时间")).append(",").append('\n')
                        .append("问题详情:").append(map.get("问题详情")).append(",").append('\n')
                        .append("当前状态:").append(map.get("当前状态")).append(",").append('\n')
                        .append("关联模块:").append(map.get("关联模块")).append(",").append('\n')
                        .append("事件ID:").append(map.get("事件ID"));

                //发送短信
                sendSMS(sendPhones, content, assets);

            } else if (map.get("恢复标题") != null) {
                //告警等级满足规则
                String level = map.get("恢复等级");
                if (!severity.contains(level)) {
                    return;
                }

                //拼接短信
                String wtxq = getWtxq(map.get("恢复详情"));
                map.put("恢复详情", wtxq);
                map.put("IP地址", assets.getInBandIp());
                map.put("主机名称", assets.getHostName());
                StringBuffer content = new StringBuffer();
                content.append("恢复标题:").append(map.get("恢复标题")).append(",").append('\n')
                        .append("恢复信息:").append(map.get("恢复信息")).append(",").append('\n')
                        .append("恢复等级:").append(map.get("恢复等级")).append(",").append('\n')
                        .append("IP地址:").append(map.get("IP地址")).append(",").append('\n')
                        .append("故障时间:").append(map.get("故障时间")).append(",").append('\n')
                        .append("恢复时间:").append(map.get("恢复时间")).append(",").append('\n')
                        .append("恢复详情:").append(map.get("恢复详情")).append(",").append('\n')
                        .append("恢复状态:").append(map.get("恢复状态")).append(",").append('\n')
                        .append("关联模块:").append(map.get("关联模块")).append(",").append('\n')
                        .append("事件ID:").append(map.get("事件ID")).append('\n');

                //发送短信
                sendSMS(sendPhones, content, assets);
            }
        } catch (Exception e) {
            log.error("失败:",e);
        }
    }

    private void sendSMS(HashSet<String> sendPhones, StringBuffer content, MwTangibleassetsDTO assets) {
        StringBuffer sb = new StringBuffer();
        int isSuccess = 0;
        try {
            if (sendPhones != null && sendPhones.size() > 0) {
                sendPhones.forEach(phone -> {
                    sb.append(phone + ":");
                    SmsSenderService service = new SmsSenderService();
                    SmsSenderDelegate port = service.getSmsSenderPort();
                    String res = port.queryReport("Ivan03738", phone, content.toString());
                    sb.append(res + ",");
                });
            }
        } catch (Exception e) {
            isSuccess = 1;
            sb.append(e.getMessage());
            log.error("失败:",e);
        } finally {
            //保存历史记录
            AlertRecordTable recored = new AlertRecordTable();
            recored.setDate(new Date());
            recored.setMethod("SMS_SEND_NEW短信");
            recored.setText(sb.toString() + "\n" + content.toString());
            recored.setIsSuccess(isSuccess);
            recored.setHostid(assets.getAssetsId());
            mwWeixinTemplateDao.insertRecord(recored);
        }
    }

    @Autowired
    private MwServerManager mwServerManager;

    @Autowired
    private MWAlertService mwAlertService;

    //处理企业微信消息
    private void dealQyWeixinMessage(HashMap<String, String> map, HashSet<Integer> user_ids, HashSet<String> severity, MwTangibleassetsDTO assets, String ruleId) {
        //根据系统用户id,查询微信userId
        List<String> userIds = mwWeixinTemplateDao.selectQyWeixinUserId(user_ids);

        //处理需要userIds,转换格式
        String touser = getSendTouer(userIds);

        //整理发送消息内容
        if (map.get("告警标题") != null) {
            //告警等级满足规则
            String level = map.get("告警等级");
            if (!severity.contains(level)) {
                log.info("dealQyWeixinMessage no alert level:{}", level);
                return;
            }

            String wtxq = getWtxq(map.get("问题详情"));
            map.put("问题详情", wtxq);

            map.put("主机名称", assets.getHostName());
            map.put("IP地址", assets.getInBandIp());
            StringBuffer content = new StringBuffer();
            content.append("告警标题:").append(map.get("告警标题")).append(",").append('\n')
                    .append("告警信息:").append(map.get("告警信息")).append(",").append('\n')
                    .append("告警等级:").append(map.get("告警等级")).append(",").append('\n')
                    .append("IP地址:").append(map.get("IP地址")).append(",").append('\n')
                    .append("告警时间:").append(map.get("告警时间")).append(",").append('\n')
                    .append("问题详情:").append(map.get("问题详情")).append(",").append('\n')
                    .append("当前状态:").append(map.get("当前状态")).append(",").append('\n')
                    .append("关联模块:").append(map.get("关联模块")).append(",").append('\n')
                    .append("事件ID:").append(map.get("事件ID"));

            //查询发送信息
            GeneralMessageEntity qyEntity = mwWeixinTemplateDao.findWeiXinMessage(ruleId);

            //处理发送信息格式
            HashMap<String, Object> sendDataMap = new HashMap<>();
            sendDataMap.put("touser", touser);
            sendDataMap.put("msgtype", "textcard");
            sendDataMap.put("agentid", qyEntity.getAgentId());
            HashMap<String, String> firstdata = new HashMap<>();
            firstdata.put("title", "[系统告警]\n主机名称:" + map.get("主机名称") + "\n ");
            firstdata.put("btntxt", "详情");
            firstdata.put("description", content.toString());
            firstdata.put("url", "URL");
            sendDataMap.put("textcard", firstdata);
            String sendStr = JSON.toJSONString(sendDataMap);

            //发送信息
            String res = sendQyWeixinMessage(sendStr, qyEntity);

            //保存记录
            Integer errcode = JSONObject.parseObject(res).getInteger("errcode");
            AlertRecordTable recored = new AlertRecordTable();
            recored.setDate(new Date());
            recored.setMethod("企业微信");
            recored.setText(sendStr);
            recored.setIsSuccess(errcode);
            recored.setHostid(map.get("HOSTID"));
            mwWeixinTemplateDao.insertRecord(recored);

        } else if (map.get("恢复标题") != null) {
            //告警等级满足规则
            String level = map.get("恢复等级");
            if (!severity.contains(level)) {
                return;
            }

            String wtxq = getWtxq(map.get("恢复详情"));
            map.put("恢复详情", wtxq);

            map.put("IP地址", assets.getInBandIp());
            map.put("主机名称", assets.getHostName());
            StringBuffer content = new StringBuffer();
            content.append("恢复标题:").append(map.get("恢复标题")).append(",").append('\n')
                    .append("恢复信息:").append(map.get("恢复信息")).append(",").append('\n')
                    .append("恢复等级:").append(map.get("恢复等级")).append(",").append('\n')
                    .append("IP地址:").append(map.get("IP地址")).append(",").append('\n')
                    .append("故障时间:").append(map.get("故障时间")).append(",").append('\n')
                    .append("恢复时间:").append(map.get("恢复时间")).append(",").append('\n')
                    .append("恢复详情:").append(map.get("恢复详情")).append(",").append('\n')
                    .append("恢复状态:").append(map.get("恢复状态")).append(",").append('\n')
                    .append("关联模块:").append(map.get("关联模块")).append(",").append('\n')
                    .append("事件ID:").append(map.get("事件ID")).append('\n');

            //查询发送信息
            GeneralMessageEntity qyEntity = mwWeixinTemplateDao.findWeiXinMessage(ruleId);

            //处理发送信息格式
            HashMap<String, Object> sendDataMap = new HashMap<>();
            sendDataMap.put("touser", touser);
            sendDataMap.put("msgtype", "textcard");
            sendDataMap.put("agentid", qyEntity.getAgentId());
            HashMap<String, String> firstdata = new HashMap<>();
            firstdata.put("title", "[恢复通知]\n主机名称:" + map.get("主机名称") + "\n ");
            firstdata.put("btntxt", "详情");
            firstdata.put("description", content.toString());
            firstdata.put("url", "URL");
            sendDataMap.put("textcard", firstdata);
            String sendStr = JSON.toJSONString(sendDataMap);

            //发送信息
            String res = sendQyWeixinMessage(sendStr, qyEntity);

            //保存记录
            Integer errcode = JSONObject.parseObject(res).getInteger("errcode");
            AlertRecordTable recored = new AlertRecordTable();
            recored.setDate(new Date());
            recored.setMethod("企业微信");
            recored.setText(sendStr);
            recored.setIsSuccess(errcode);
            recored.setHostid(map.get("HOSTID"));
            mwWeixinTemplateDao.insertRecord(recored);
        }else{
            log.info("dealQyWeixinMessage no title");
        }
    }

    //获取问题详细转换后的数据
    public String getWtxq(String str) {
        if(str != null){
            String[] details = str.split(":");
            if (details.length == 2) {
                String srr = mwServerManager.getChName(details[0]);
                return srr + details[1];
            } else if (details.length < 2) {
                return mwServerManager.getChName(str);
            } else if (details.length > 2) {
                return str;
            }
            return str;
        }else {
            return null;
        }
    }

    public String getSendTouer(List<String> userIds) {
        StringBuffer touser = new StringBuffer();
        HashSet<String> userIds1 = (HashSet<String>) userIds.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toSet());
        if (userIds1 != null && userIds1.size() > 0 && userIds1.size() <= 1000) {
            Iterator iterator = userIds1.iterator();
            while (iterator.hasNext()) {
                touser.append("|").append(iterator.next());
            }
        }
        touser.replace(0, 1, "");
        return touser.toString();
    }

    //处理邮件消息，发送，记录
    private void dealEmailMessage(HashMap<String, String> map, String actionId, HashSet<String> severity, HashSet<Integer> userIds, String ruleId, MwTangibleassetsDTO assets) throws UnsupportedEncodingException, MessagingException, ParseException {
        if (map.get("告警标题") != null) {
            String keyword4 = map.get("告警等级");
            if (!severity.contains(keyword4)) {
                return;
            }

            map.put("主机名称", assets.getHostName());

            //接收人
            List<String> to = mwWeixinTemplateDao.selectEmail(userIds);
            List<String> to_1 = (List<String>) to.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toList());
            if (to_1 == null || to_1.size() <= 0) {
                return;
            }
            String[] tos = to_1.toArray(new String[to_1.size()]);


            //发件人
            EmailFrom from = mwWeixinTemplateDao.selectEmailFrom(ruleId);
            if (from == null) {
                return;
            }

            //邮件参数
            HtmlTemplateEmailParam param = new HtmlTemplateEmailParam();
            String wtxq = getWtxq(map.get("问题详情"));
            map.put("问题详情", wtxq);
            String hostid = map.get("HOSTID");
            String keyword1 = assets.getHostName();
            String keyword2 = assets.getInBandIp();
            param.setOption(keyword1);
            param.setAddress(keyword2);
            param.setContext(map.get("告警信息"));
            param.setLevel(map.get("告警等级"));
            param.setDate(map.get("告警时间"));
            param.setTitle(map.get("告警标题"));
            param.setMessage(map.get("告警信息"));
            param.setIp(map.get("IP地址"));
            param.setDetail(map.get("问题详情"));
            param.setState(map.get("当前状态"));
            param.setId(map.get("事件ID"));
            param.setName(map.get("主机名称"));
            param.setAssetsMonitor(map.get("关联模块"));
            String message = emailService.sendAlertHtmlTemplate(tos, from, "猫维告警通知", param);
            String assetsMonitor = mwAlertService.getAssetsMonitor(assets);
            if (null != assetsMonitor) {
                param.setAssetsMonitor(assetsMonitor);
            }
            if (message.equals("success")) {
                AlertRecordTable recored = new AlertRecordTable();
                recored.setDate(new Date());
                recored.setMethod("邮件");
                recored.setText(param.toString());
                recored.setIsSuccess(0);
                recored.setHostid(hostid);
                mwWeixinTemplateDao.insertRecord(recored);
            } else {
                AlertRecordTable recored = new AlertRecordTable();
                recored.setDate(new Date());
                recored.setMethod("邮件");
                recored.setText(param.toString() + "\n异常信息：" + message);
                recored.setIsSuccess(1);
                recored.setHostid(hostid);
                mwWeixinTemplateDao.insertRecord(recored);
            }

        } else if (map.get("恢复标题") != null) {
            String hfdl = map.get("恢复等级");
            if (!severity.contains(hfdl)) {
                return;
            }

            map.put("主机名称", assets.getHostName());

            //接收人
            List<String> to = mwWeixinTemplateDao.selectEmail(userIds);
            List<String> to_1 = (List<String>) to.stream().filter(e -> !"".equals(e) && e != null).collect(Collectors.toList());
            if (to_1 == null || to_1.size() <= 0) {
                return;
            }
            String[] tos = to_1.toArray(new String[to_1.size()]);

            //发件人
            EmailFrom from = mwWeixinTemplateDao.selectEmailFrom(ruleId);
            if (from == null) {
                return;
            }

            //邮件参数
            String wtxq = getWtxq(map.get("恢复详情"));
            map.put("恢复详情", wtxq);
            HtmlTemplateEmailParam param = new HtmlTemplateEmailParam();
            String hostid = map.get("HOSTID");
            String keyword1 = assets.getHostName();
            param.setOption(keyword1);
            param.setContext(map.get("恢复信息"));
            String keyword3 = map.get("故障时间");
            String keyword4 = map.get("恢复时间");
            param.setDate1(keyword3);
            param.setDate2(keyword4);
            param.setTitle(map.get("恢复标题"));
            param.setMessage(map.get("恢复信息"));
            param.setLevel(map.get("恢复等级"));
            param.setIp(map.get("IP地址"));
            param.setDate(map.get("故障时间"));
            param.setHfdate(map.get("恢复时间"));
            param.setDetail(map.get("恢复详情"));
            param.setState(map.get("恢复状态"));
            param.setId(map.get("事件ID"));
            param.setName(map.get("主机名称"));
            param.setAssetsMonitor(map.get("关联模块"));

            String assetsMonitor = mwAlertService.getAssetsMonitor(assets);
            if (null != assetsMonitor) {
                param.setAssetsMonitor(assetsMonitor);
            }

            //调用发邮件方法
            String message = emailService.sendRestoreHtmlTemplate(tos, from, "猫维告警恢复通知", param);
            if (message.equals("success")) {
                AlertRecordTable recored = new AlertRecordTable();
                recored.setDate(new Date());
                recored.setMethod("邮件");
                recored.setText(param.toString());
                recored.setIsSuccess(0);
                recored.setHostid(hostid);
                mwWeixinTemplateDao.insertRecord(recored);
            } else {
                AlertRecordTable recored = new AlertRecordTable();
                recored.setDate(new Date());
                recored.setMethod("邮件");
                recored.setText(param.toString() + "\n异常信息：" + message);
                recored.setIsSuccess(1);
                recored.setHostid(hostid);
                mwWeixinTemplateDao.insertRecord(recored);
            }
        }
    }

    //处理微信消息，发送，记录
    public void dealWxMessage(HashMap<String, String> map, HashSet<Integer> userIds, HashSet<String> severity, MwTangibleassetsDTO assets) throws ParseException {
        //获取模板信息
        String template_id = ""; //模板id
        String first = "";
        String keyword1 = "";
        String keyword2 = "";
        String keyword3 = "";
        String keyword4 = "";
        String keyword5 = "";
        String remark = "";
        String hostid = map.get("HOSTID");
        //MwTangibleassetsDTO assets = mwTangibleAssetsTableDao.selectByHostId(hostid);
        if (assets == null) {
            return;
        }
        //查询所有负责人openids
        HashSet<String> opendids = getOpenids(userIds);

        if (map.get("告警标题") != null) {
            MwWeixinTemplateTable tem = mwWeixinTemplateDao.selectOneByTemplateName("监控告警通知");
            template_id = tem.getTemplateId();
            keyword1 = assets.getHostName();
            keyword2 = assets.getInBandIp();
            keyword3 = map.get("告警信息");
            keyword4 = map.get("告警等级");
            if (!severity.contains(keyword4)) {
                return;
            }
            keyword5 = map.get("告警时间");
            remark = "请运维人员相互告知！";
            first = map.get("告警标题");
        } else if (map.get("恢复标题") != null) {
            MwWeixinTemplateTable tem = mwWeixinTemplateDao.selectOneByTemplateName("告警恢复通知");
            template_id = tem.getTemplateId();
            keyword1 = assets.getHostName();
            keyword2 = map.get("恢复信息");
            keyword3 = map.get("故障时间");
            keyword4 = map.get("恢复时间");
            String hfdl = map.get("恢复等级");
            if (!severity.contains(hfdl)) {
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
            Date d1 = sdf.parse(keyword3);
            Date d2 = sdf.parse(keyword4);
            long ms = d2.getTime() - d1.getTime();
            keyword5 = formatTime(ms);
            first = "告警恢复通知";
            remark = "告警已恢复！";
        }

        //将获取的模板信息处理成map 再生成json数据发送给微信模板
        List<String> jsonList = new ArrayList<>();
        if (opendids != null && opendids.size() > 0) {
            Iterator<String> it = opendids.iterator();
            while (it.hasNext()) {
                String touser = it.next();
                HashMap<String, Object> d = new HashMap<>();
                d.put("touser", touser);
                d.put("template_id", template_id);
                HashMap<String, Object> data = new HashMap<>();
                HashMap<String, String> firstdata = new HashMap<>();
                firstdata.put("value", first);
                firstdata.put("color", "#173177");
                data.put("first", firstdata);
                HashMap<String, String> k1data = new HashMap<>();
                k1data.put("value", keyword1);
                k1data.put("color", "#173177");
                data.put("keyword1", k1data);
                HashMap<String, String> k2data = new HashMap<>();
                k2data.put("value", keyword2);
                k2data.put("color", "#173177");
                data.put("keyword2", k2data);
                HashMap<String, String> k3data = new HashMap<>();
                k3data.put("value", keyword3);
                k3data.put("color", "#173177");
                data.put("keyword3", k3data);
                HashMap<String, String> k4data = new HashMap<>();
                k4data.put("value", keyword4);
                k4data.put("color", "#173177");
                data.put("keyword4", k4data);
                HashMap<String, String> k5data = new HashMap<>();
                k5data.put("value", keyword5);
                k5data.put("color", "#173177");
                data.put("keyword5", k5data);
                HashMap<String, String> redata = new HashMap<>();
                redata.put("value", remark);
                redata.put("color", "#173177");
                data.put("remark", redata);
                d.put("data", data);
                jsonList.add(JSON.toJSONString(d));
            }
        }

        //发送模板消息
        if (jsonList != null && !jsonList.equals("")) {
            for (String s : jsonList) {
                String resultData = sendTemplateMessage(s);
                Integer errcode = JSONObject.parseObject(resultData).getInteger("errcode");

                AlertRecordTable recored = new AlertRecordTable();
                recored.setDate(new Date());
                recored.setMethod("微信");
                recored.setText(s);
                recored.setIsSuccess(errcode);
                recored.setHostid(hostid);
                mwWeixinTemplateDao.insertRecord(recored);

            }
        }
    }

    /**
     * @describ 1根据用户id 查询所有责任人的openid
     */
    public HashSet<String> getOpenids(HashSet<Integer> list) {

        HashSet<String> openids = mwWeixinUserDao.select6(list);
        return openids;
    }

    /* *
     * @describ
     * 1根据资产信息查询所有责任人
     * 2查询所有责任人的openid
     */
    public HashSet<String> getOpenids(MwTangibleassetsDTO assets) {
        HashSet<Integer> list = new HashSet<>();
        MwOrgMapper mwOrgMapper = mwWeixinUserDao.select1(assets.getId());
        if (mwOrgMapper != null) {
            List<Integer> f1 = mwWeixinUserDao.select2(mwOrgMapper.getOrgId());
            list.addAll(f1);
        }

        MwDatapermission mwDatapermission = mwWeixinUserDao.select3(assets.getId());
        if (mwDatapermission != null) {
            Boolean isUser = mwDatapermission.isUser();
            Boolean isGroup = mwDatapermission.isGroup();
            if (isUser) {
                List<Integer> f2 = mwWeixinUserDao.select4(assets.getId());
                list.addAll(f2);
            }
            if (isGroup) {
                List<Integer> f3 = mwWeixinUserDao.select5(assets.getId());
                list.addAll(f3);
            }
        }
        HashSet<String> openids = mwWeixinUserDao.select6(list);
        return openids;
    }

    //根据负责人查询企业微信userIds
    public HashSet<String> getQyWeixinUserids(MwTangibleassetsDTO assets) {
        HashSet<Integer> list = new HashSet<>();
        MwOrgMapper mwOrgMapper = mwWeixinUserDao.select1(assets.getId());
        if (mwOrgMapper != null) {
            List<Integer> f1 = mwWeixinUserDao.select2(mwOrgMapper.getOrgId());
            list.addAll(f1);
        }

        MwDatapermission mwDatapermission = mwWeixinUserDao.select3(assets.getId());
        if (mwDatapermission != null) {
            Boolean isUser = mwDatapermission.isUser();
            Boolean isGroup = mwDatapermission.isGroup();
            if (isUser) {
                List<Integer> f2 = mwWeixinUserDao.select4(assets.getId());
                list.addAll(f2);
            }
            if (isGroup) {
                List<Integer> f3 = mwWeixinUserDao.select5(assets.getId());
                list.addAll(f3);
            }
        }
        HashSet<String> userIds = mwWeixinUserDao.select7(list);
        return userIds;
    }

    /**
     * @describe 发送企业微信txet消息
     */
    public String sendQyWeixinMessage(String jsonData, GeneralMessageEntity qiEntity) {
        String str = send_qyweixin_message.replace("ACCESS_TOKEN", getQyWeixinAccessToken(qiEntity));
        String result = LoadUtil.post(str, jsonData);
        return result;
    }

    /**
     * @describe 发送微信模板消息
     */
    public String sendTemplateMessage(String jsonData) {
        String str = send_tem_url.replace("ACCESS_TOKEN", getAccessToken());
        String result = LoadUtil.post(str, jsonData);
        return result;
    }

    /**
     * @describe 将unicode字符串转为正常字符串, 去除一些不规范符号
     */
    public String converUnicodeToChar(String str) {
        str = str.replaceAll("\\\\r", "")
                .replaceAll("\\\\n", "")
                .replaceAll("\\\\f", "")
                .replaceAll("\\\\b", "")
                .replaceAll("\\r", "")
                .replaceAll("\\n", "")
                //.replaceAll(" ", "")
                .replaceAll("\\\\\"", "\"");

        Pattern pattern = Pattern.compile("(\\\\u(\\w{4}))");
        Matcher matcher = pattern.matcher(str);

        //迭代，将str的unicode都转为字符
        while (matcher.find()) {
            String unicodeFull = matcher.group(1);
            String unicodeNum = matcher.group(2);

            char singleChar = (char) Integer.parseInt(unicodeNum, 16);
            str = str.replace(unicodeFull, singleChar + "");
        }
        return converChar(str);
    }

    public String converChar(String str) {
        if(!str.contains(AlertEnum.VR.toString())) return str;
        Pattern pattern1 = Pattern.compile("(?<=\\<)[^\\>]+");
        Matcher matcher1 = pattern1.matcher(str);
        String regexIp = "((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|\\d)";
        Pattern pattern = Pattern.compile(regexIp);
        while(matcher1.find()){
            Matcher matcher = pattern.matcher(matcher1.group());
            if(matcher.find()){
                String ss = matcher1.group().replaceAll(",","-");
                str = str.replaceAll(matcher1.group(),ss);
            }

        }
        return str;
    }


    /**
     * @describe 将毫秒数转换成一个合适的输出字符串
     */
    public static String formatTime(Long ms) {
        Integer ss = 1000;
        Integer mi = ss * 60;
        Integer hh = mi * 60;
        Integer dd = hh * 24;

        Long day = ms / dd;
        Long hour = (ms - day * dd) / hh;
        Long minute = (ms - day * dd - hour * hh) / mi;
        Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        Long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        StringBuffer sb = new StringBuffer();
        if (day > 0) {
            sb.append(day + "天");
        }
        if (hour > 0) {
            sb.append(hour + "小时");
        }
        if (minute > 0) {
            sb.append(minute + "分");
        }
        if (second > 0) {
            sb.append(second + "秒");
        }
        if (milliSecond > 0) {
            sb.append(milliSecond + "毫秒");
        }
        return sb.toString();
    }


    /**
     * @describe 初始化用户列表
     */
    public String initUser() throws UnsupportedEncodingException {
        //获取所有openid
        String url = get_list_user.replace("ACCESS_TOKEN", getAccessToken());
        String result = LoadUtil.get(url);

        //转成 好操作 的格式
        JSONObject jsonObject = JSONObject.parseObject(result);
        Map<String, Object> map = new HashMap<>();
        Iterator it = jsonObject.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
            map.put(entry.getKey(), entry.getValue());
        }

        //将openid相关信息封装到集合中
        Map<String, Object> datas = (Map<String, Object>) map.get("data");
        List<String> opendids = (List<String>) datas.get("openid");
        List<MwWeixinUserTable> users = new ArrayList<>();
        for (String openid : opendids) {
            MwWeixinUserTable user = new MwWeixinUserTable();
            user.setOpenid(openid);
            String info = getUserInfo(openid);
            JSONObject userInfoJson = JSONObject.parseObject(info);
            String nn = (String) userInfoJson.get("nickname");
            String mm = Base64.encodeBase64String(nn.getBytes("UTF-8"));
//            String ff = new String(Base64.decodeBase64(mm),"UTF-8");
//            ////System.out.println("ff: "+ff);
            //用户昵称含有特殊字符
            user.setNickname(mm);
            user.setSex((Integer) userInfoJson.get("sex"));
            user.setCountry((String) userInfoJson.get("country"));
            user.setProvince((String) userInfoJson.get("province"));
            user.setCity((String) userInfoJson.get("city"));
            users.add(user);
        }

        //将用户集合保存在数据库中
        for (MwWeixinUserTable user : users) {
            MwWeixinUserTable u = weixinUserService.selectOne(user.getOpenid());
            if (null == u) {
                weixinUserService.insert(user);
            } else {
                weixinUserService.updateById(user);
            }
        }

        return users.toString();
    }

    /**
     * @describe 获取用户列表
     */
    public List<MwWeixinUserTable> getUserList() throws UnsupportedEncodingException {
//            String ff = new String(Base64.decodeBase64(mm),"UTF-8");
//            ////System.out.println("ff: "+ff);

        List<MwWeixinUserTable> users = weixinUserService.selectList();
        for (MwWeixinUserTable u : users) {
            String nm = u.getNickname();
            u.setNickname(new String(Base64.decodeBase64(nm), "UTF-8"));
        }
        return users;
    }

    /**
     * @describe 根据openid查询单个用户基本信息
     */
    public String getUserInfo(String openid) {
        String url = get_user_info.replace("ACCESS_TOKEN", getAccessToken()).replace("OPENID", openid);
        String result = LoadUtil.get(url);
        return result;
    }

    /**
     * @describe 初始化模板列表
     */
    public List<MwWeixinTemplateTable> initTemplate() {
        //获取所有模板
        String url = get_list_template.replace("ACCESS_TOKEN", getAccessToken());
        String result = LoadUtil.get(url);

        //转成 好操作 的格式
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONArray c = jsonObject.getJSONArray("template_list");
        List<MwWeixinTemplateTable> lists = JSONObject.parseArray(c.toJSONString(), MwWeixinTemplateTable.class);

        //保存到数据库中
        for (MwWeixinTemplateTable tem : lists) {
            MwWeixinTemplateTable re = mwWeixinTemplateDao.selectOne(tem.getTemplateId());
            if (re == null) {
                mwWeixinTemplateDao.insert(tem);
            }
        }
        return lists;
    }

    /**
     * @describe 设置所属行业
     */
    public String setHy(String str1, String str2) {
        String url = set_hy.replace("ACCESS_TOKEN", getAccessToken());

        Map<String, String> map = new HashMap<>();
        map.put("industry_id1", str1);
        map.put("industry_id2", str2);
        String jsonData = JSON.toJSONString(map);

        String result = LoadUtil.post(url, jsonData);
        return result;
    }

    /**
     * @describe 获取所属行业
     */
    public String getHy() {
        String url = get_hy.replace("ACCESS_TOKEN", getAccessToken());

        String result = LoadUtil.get(url);
        return result;
    }

    /**
     * @describe 设置菜单
     */
    public String setMenu() {
        //目前菜单栏只有一个菜单
        Button button = new Button();
        String viewStr = get_code_url.replace("APPID", APPID)
                .replace("REDIRECT_URI", redirect_url)
                .replaceAll("STATE", "111"); //自定义一个state参数，随意填或不填
        button.getButton().add(new ViewButton("猫维登录", viewStr));
        //button.getButton().add(new ViewButton("test登录","https://www.baidu.com"));

        String createMenuUrl = create_menu_url.replace("ACCESS_TOKEN", getAccessToken());
        String jsonStr = JSONObject.toJSONString(button);


        String responseJson = LoadUtil.post(createMenuUrl, jsonStr);

        return responseJson;
    }

    /**
     * @describe 根据code获取access_token等信息
     */
    public String getCodeAccessToken(String code) {
        String url = code_to_token.replace("APPID", APPID).
                replace("SECRET", APPSECRET)
                .replace("CODE", code);
        String result = LoadUtil.get(url);
        return result;
    }

    public Reply selectList(MwOverdueTable qParam) {
        try {
            List<MwOverdueTable> list = new ArrayList<>();
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            Map pubCriteria = PropertyUtils.describe(qParam);
            list = mwWeixinTemplateDao.selectList(pubCriteria);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("", e);
            return Reply.fail(500, "过期告警查询失败");
        }
    }

    public Reply delete(List<MwOverdueTable> list) {
        mwWeixinTemplateDao.deleteBatch(list);
        return Reply.ok("删除成功");
    }

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    public Reply update(MwOverdueTable param) throws Exception {
        List<MwOverdueTable> lists = mwWeixinTemplateDao.selectOverdue(param.getIds());
        List<MwOverdueTable> updates = new ArrayList<>();
        if(lists!=null && lists.size()>0){
            for (MwOverdueTable list : lists) {
                if(list.getIsSend() == true){
                    continue;
                }
                String msgs = list.getContext();
                try {
                    log.info("zabbix-alert:{}", msgs);
                    //处理消息内容 并根据处理结果发送信息
                    //dealMessage(msgs);
                    list.setIsSend(true);
                    list.setModificationDate(new Date());
                    list.setModifier(iLoginCacheInfo.getLoginName());
                    updates.add(list);
                }catch (Exception e) {
                    log.error("保存解析数据尚未发送时出错:{}", e);
                    AlertRecordTable recored = new AlertRecordTable();
                    recored.setDate(new Date());
                    recored.setMethod("解析数据尚未发送");
                    recored.setText(msgs);
                    recored.setIsSuccess(1);
                    recored.setHostid("-1");
                    recored.setError(e.getMessage());
                    mwWeixinTemplateDao.insertRecord(recored);
                }
            }
        }
        if(updates.size()>0){
            mwWeixinTemplateDao.batUpdate(lists);
        }
        return Reply.ok("发送成功");
    }

    public Reply insertOverdue(List<MwOverdueTable> data){
        mwWeixinTemplateDao.insertOverdue(data);
        return Reply.ok("保存成功");
    }

    public Reply insertRecord(AlertRecordTable alertRecordTable){
        mwWeixinTemplateDao.insertRecord(alertRecordTable);
        return Reply.ok("保存成功");
    }

    public void workSystem(List<String> msgs){
        List<MwRuleSelectListParam> selectListParams = mwAlertActionDao.selectMwAlertAction(null);
        log.info("T5BC规则:" + selectListParams.size());
        if(CollectionUtils.isEmpty(selectListParams)){
            log.info("告警动作为空");
            return;
        }
        for(String msg : msgs){
            try{
                //将告警信息封装成map去操作
                log.info("工单发送告警dealMessage msg:" + msg);
                String unTreated = msg;
                msg = toJsonString(msg);
                ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
                msg = converUnicodeToChar(msg);
                String[] strs = msg.split(",");
                String regex = ":";
                for (String s : strs) {
                    String s1 = s.substring(0, s.indexOf(regex) + 1).replaceAll(":", "");
                    String s2 = s.substring(s.indexOf(regex) + 1).trim();
                    map.put(s1, s2);
                }
                saveHuaxingAlertTable(map);
                String title = "";
                String level = "";
                HashMap<String, Object> assetsMap = new HashMap<>();
                Boolean isAlarm = map.get(AlertEnum.ALERTTITLE.toString()) == null ? map.get(AlertEnum.RECOVERYTITLE.toString()) == null ? null : false : true;
                StringBuffer url = new StringBuffer(qyUrl);
                url.append(AlertAssetsEnum.QUESTION.toString());
                StringBuffer content = new StringBuffer();
                content.append(AlertEnum.HostNameZH.toString().toLowerCase()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.HostNameZH.toString())).append("\n")
                        .append(AlertEnum.IPAddress.toString().toLowerCase()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.IPAddress.toString())).append("\n");
                if (isAlarm) {
                    title = AlertEnum.ALERTTITLE.toString();
                    level = AlertEnum.ALERTLEVEL.toString();
                    url.append(AlertEnum.TITLE.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode("告警事件-通知","UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.HOSTNAME.toString().toLowerCase()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.HostNameZH.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.IP.toString().toLowerCase()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.IPAddress.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.TOPICEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(title),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.ALERTINFOEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.PROBLEMDETAILS.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.ALERTSTARTIMEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.ALERTTIME.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.NOWSTATEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.NOWSTATE.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString());
                    content.append(AlertEnum.PROBLEMDETAILS.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.PROBLEMDETAILS.toString())).append("\n")
                            .append(AlertEnum.ALERTTIME.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.ALERTTIME.toString())).append("\n")
                            .append(AlertEnum.NOWSTATE.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.NOWSTATE.toString())).append("\n");

                } else {
                    title = AlertEnum.RECOVERYTITLE.toString();
                    level = AlertEnum.RECOVERYLEVEL.toString();
                    url.append(AlertEnum.TITLE.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode("告警事件-恢复","UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.HOSTNAME.toString().toLowerCase()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.HostNameZH.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.IP.toString().toLowerCase()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.IPAddress.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.TOPICEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(title),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.ALERTINFOEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.RECOVERYDETAILS.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.ALERTTIMEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.FAILURETIME.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.CLOSETIMEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.RECOVERYTIME.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                            .append(AlertEnum.NOWSTATEEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.RENEWSTATUS.toString()),"UTF-8")).append(AlertAssetsEnum.AND.toString());
                    content.append(AlertEnum.RECOVERYDETAILS.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYDETAILS.toString())).append("\n")
                            .append(AlertEnum.FAILURETIME.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.FAILURETIME.toString())).append("\n")
                            .append(AlertEnum.RECOVERYTIME.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RECOVERYTIME.toString())).append("\n")
                            .append(AlertEnum.RENEWSTATUS.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.RENEWSTATUS.toString())).append("\n");

                }
                url.append(AlertEnum.ALERTLEVELEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(MWAlertLevelParam.actionAlertLevelMap.get(map.get(level)),"UTF-8")).append(AlertAssetsEnum.AND.toString())
                        .append(AlertEnum.EVENTIDEN.toString()).append(AlertAssetsEnum.EQUAL.toString()).append(URLEncoder.encode(map.get(AlertEnum.EVENTID.toString()),"UTF-8"));
                content.append(level).append(AlertAssetsEnum.COLON.toString()).append(MWAlertLevelParam.actionAlertLevelMap.get(map.get(level))).append("\n")
                        .append(AlertEnum.EVENTID.toString()).append(AlertAssetsEnum.COLON.toString()).append(map.get(AlertEnum.EVENTID.toString()));
                log.info("工单发送告警map:" + map);
                String levelEn = unTreated.substring(unTreated.indexOf(level+":"),unTreated.length());
                levelEn = levelEn.substring(levelEn.indexOf(":") + 1,levelEn.indexOf(","));
                log.info("工单发送告警levelEn:" + levelEn);
                unTreated = unTreated.replaceAll(levelEn,MWAlertLevelParam.actionAlertLevelMap.get(map.get(level)));
                assetsMap.put(AlertEnum.ALERTTITLE.toString(),map.get(title));
                MessageContext messageContext = new MessageContext();
                messageContext.setKey(assetsMap);
                log.info("dealMessage assetsMap:" + assetsMap);
                for (MwRuleSelectListParam selectListParam : selectListParams) {
                    log.info("T5BC 告警动作ID:" + selectListParam.getActionId());
                    HashSet<String> severity = mwWeixinTemplateDao.selectLevel(selectListParam.getActionId());
                    if(!severity.contains(map.get(level))){
                        log.info("告警级别不符合！");
                        continue;
                    }
                    List<MwRuleSelectParam> ruleSelectList = mwAlertActionDao.selectMwAlertRuleSelect(selectListParam.getActionId());
                    log.info("T5BC ruleSelectList:" + ruleSelectList);
                    Boolean resultBoolean = true;
                    if (ruleSelectList.size() > 2) {
                        List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
                        for (MwRuleSelectParam s : ruleSelectList) {
                            if (s.getParentKey().equals(AlertEnum.ROOT.toString())) {
                                ruleSelectParams.add(s);
                            }
                        }
                        for (MwRuleSelectParam s : ruleSelectParams) {
                            s.setConstituentElements(getChild(s.getKey(), ruleSelectList));
                        }
                        resultBoolean = DelFilter.delFilter(ruleSelectParams, messageContext, ruleSelectList);
                        log.info("ruleSelectParams:" + ruleSelectParams);
                        log.info("messageContext:" + messageContext);
                        log.info("ruleSelectParams:" + ruleSelectParams);
                    }
                    log.info("result:" + resultBoolean);
                    if(!resultBoolean) continue;
                    UserIdsType userIdsType = new UserIdsType();
                    userIdsType = commonActionService.getActionUserIds(selectListParam.getActionId(), null);
                    HashSet<Integer> userIds = new HashSet<>();
                    if(CollectionUtils.isNotEmpty(userIdsType.getGroupUserIds())){userIds.addAll(userIdsType.getGroupUserIds());}
                    if(CollectionUtils.isNotEmpty(userIdsType.getPersonUserIds())){userIds.addAll(userIdsType.getPersonUserIds());}
                    HashMap<String, String> qymap = new HashMap<>();
                    qymap.put(AlertEnum.TITLE.toString().toUpperCase(),map.get(title));
                    qymap.put(AlertEnum.QYWECHATCONTENT.toString(),unTreated);
                    qymap.put(AlertEnum.EMAILCONTENT.toString(),unTreated);
                    qymap.put(AlertEnum.URL.toString(),url.toString());
                    qymap.put(AlertEnum.EVENTIDEN.toString(),map.get(AlertEnum.EVENTID.toString()));
                    qymap.put("TxinContent",content.toString());
                    List<ActionRule> rules = mwWeixinTemplateDao.selectRuleMapper(selectListParam.getActionId());
                    ExecutorService pool = Executors.newFixedThreadPool(2);
                    log.info("T5BC userIds:" + userIds);
                    for (ActionRule rule : rules){
                        if(rule.getActionType() == 20){
                            pool.submit(new QyWxSendHuaXingAlertImpl(qymap,userIds));
                            pool.submit(new EmailSendHuaXingImpl(qymap,userIds,null,null));
                            pool.submit(new TXinSendRancherMessageiImpl(qymap,userIds));
                        }
                        pool.shutdown();
                        try {
                            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                        } catch (InterruptedException e) {
                            log.error("线程错误：" + e);
                        }
                    }

                }
            }catch (Exception e){
                log.error("工单流程错误:{}",e);
            }
        }
    }

    private void saveHuaxingAlertTable(ConcurrentHashMap<String, String> map){
        Boolean isAlarm = map.get(AlertEnum.ALERTTITLE.toString()) == null ? map.get(AlertEnum.RECOVERYTITLE.toString()) == null ? null : false : true;
        String alertName = null;
        String startsAt = null;
        String endsAt = null;
        String severity = null;
        String status = null;
        log.info("工单发送告警dealMessage map:" + map);
        if(isAlarm){
            alertName = map.get(AlertEnum.ALERTTITLE.toString());
            startsAt = map.get(AlertEnum.ALERTTIME.toString());
            severity = map.get(AlertEnum.ALERTLEVEL.toString());
            status = map.get(AlertEnum.NOWSTATE.toString());
        }else{
            alertName = map.get(AlertEnum.RECOVERYTITLE.toString());
            startsAt = map.get(AlertEnum.FAILURETIME.toString());
            endsAt = map.get(AlertEnum.RECOVERYTIME.toString()).trim();
            severity = map.get(AlertEnum.RECOVERYLEVEL.toString());
            status = map.get(AlertEnum.RENEWSTATUS.toString());
        }
        HuaXingAlertParam param = new HuaXingAlertParam();
        param.setAlertName(alertName);
        param.setDuration("0");
        param.setEndsAt(endsAt);
        param.setSeverity(MWAlertLevelParam.actionAlertLevelMap.get(severity));
        param.setStartsAt(startsAt.trim());
        param.setStatus(status);
        param.setAlertType("操作系统");
        param.setModelClassify("BC");
        param.setIp(map.get(AlertEnum.IPAddress.toString()));
        param.setProjectName(map.get(AlertEnum.HostNameZH.toString()));
        String bc = map.get(AlertEnum.HostNameZH.toString()).substring(0,2);
        param.setModelSystem(HuaxingAlertBCEnum.getName(bc));
        if(isAlarm){
            mwWeixinTemplateDao.insertHuaxingAlertTable(param);
        }else {
            mwWeixinTemplateDao.updateHuaxingAlertTable(param);
        }

    }


}
