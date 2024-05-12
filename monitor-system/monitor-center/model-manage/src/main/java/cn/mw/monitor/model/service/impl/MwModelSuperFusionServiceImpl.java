package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.data.InstanceNotifyType;
import cn.mw.monitor.model.dto.SuperFusionInfo;
import cn.mw.monitor.model.dto.SuperFusionInstanceChangeParam;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.MatchFusionModelTypeEnum;
import cn.mw.monitor.model.param.MatchModelTypeEnum;
import cn.mw.monitor.model.param.MwModelMacrosParam;
import cn.mw.monitor.model.param.superfusion.*;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelSuperFusionService;
import cn.mw.monitor.model.util.ModelOKHttpUtils;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.param.AddModelInstancePropertiesParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceParam;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.util.*;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.mw.monitor.model.param.MatchFusionModelTypeEnum.*;
import static cn.mw.monitor.model.util.ModelUtils.*;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.INSTANCE_NAME_KEY;
import static cn.mw.monitor.service.model.util.ValConvertUtil.*;

/**
 * 超融合
 *
 * @author qzg
 * @date 2023/7/24
 */
@Slf4j
@Service
public class MwModelSuperFusionServiceImpl implements MwModelSuperFusionService {
    private static String tokens;
    private static String tickets;
    private static String loginCookie = "LoginAuthCookie=";
    private static String superFusionIp;
    /**
     * 返回成功标识
     */
    public static final String HTTP_SUCCESS = "1";

    public static String storageTreeType = "storage";

    @Autowired
    private MwModelInstanceService mwModelInstanceService;

    @Autowired
    private MwModelRancherServiceImpl mwModelRancherServiceImpl;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Autowired
    private MwModelSuperFusionRelationManager mwModelSuperFusionRelationManager;
    @Value("${mw.graph.enable}")
    private boolean graphEnable;

    /**
     * 获取公钥
     *
     * @return
     */
//    @Override
    public String getPublicKey() {
        String publicKeyUrl = "https://" + superFusionIp + "/vapi/json/public_key";
        String jsonText = ModelOKHttpUtils.builder().url(publicKeyUrl)
                .get()
                .sync();
        JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
        String publicKey = "";
        //接口返回成功
        if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
            publicKey = (String) strInfoJson.get("data");
        }
        return publicKey;
    }

    /**
     * 使用公钥进行加密
     *
     * @param plaintext
     * @return
     */
    private String encryptedPassWd(String plaintext) {
        String encryptStr = "";
        //获取公钥字符串
        String strRSAPublicKey = getPublicKey();
        log.info("get PublicKey::" + strRSAPublicKey);
        try {

            // 将Base64编码表示的公钥转换为公钥对象
            PublicKey publicKey = getPublicKeyFromBase64(strRSAPublicKey);
            log.info("get publicKey::" + publicKey);
            // 使用公钥进行加密
            byte[] encryptedData = encryptRSA(plaintext, publicKey);
            // 将加密后的数据转换为十六进制字符串并输出
            encryptStr = bytesToHex(encryptedData);
            log.info("加密后的数据：" + encryptStr);
        } catch (Exception e) {
            log.error("使用RSA公钥字符串加密失败", e);
        }
        return encryptStr;
    }

    /**
     * 获取token
     */
    public void getTicker(MwLoginSuperFusionParam param) {
        String username = param.getUserName();
        String password = param.getPassword();
        String ip = param.getUrl();
        String url = "https://" + ip + "/vapi/extjs/access/ticket";
        String data = "";
        try {
            data = ModelOKHttpUtils.builder().url(url)
                    // 有参数的话添加参数，可多个
                    .addParam("username", username)
                    .addParam("password", password)
                    // 也可以添加多个
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    //isJsonPost true等于json的方式提交数据，类似postman里post方法的raw,false等于普通的表单提交
                    //isDefaultMediaType true使用application/json,false使用application/x-www-form-urlencoded
                    .post(false, false)
                    .sync();
            log.info("getTicker数据::" + data);
            JSONObject strInfoJson = JSONObject.parseObject(data != null ? data : "");
            //接口返回成功
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                Map map = (Map) strInfoJson.get("data");
                if (map != null) {
                    if (map.get("CSRFPreventionToken") != null) {
                        tokens = map.get("CSRFPreventionToken").toString();
                    }
                    if (map.get("ticket") != null) {
                        tickets = map.get("ticket").toString();
                    }
                }
            }
            log.info("get tokens info::" + tokens + ";tickets:" + tickets);
        } catch (Exception e) {
            log.error("获取token接口失败", e);
        }
    }

    public Reply getAssetsLoginParam(QueryModelInstanceParam mParam) {
        try {
            //根据VCenter实例获取es数据信息
            List<Map<String, Object>> listInfo = mwModelInstanceService.getInfoByInstanceId(mParam);
            //获取连接信息，URL、用户名、密码
            String userName = "";
            String url = "";
            String password = "";
            String modelId = "0";
            List<MwModelMacrosParam> macrosParams = JSON.parseArray(JSONObject.toJSONString(listInfo), MwModelMacrosParam.class);
            for (MwModelMacrosParam m : macrosParams) {
                userName = m.getUSERNAME();
                url = m.getHOST();
                if (m.getPASSWORD().length() == 172) {
                    password = RSAUtils.decryptData(m.getPASSWORD(), RSAUtils.RSA_PRIVATE_KEY);
                } else {
                    password = m.getPASSWORD();
                }
                modelId = m.getModelId() != null ? m.getModelId().toString() : "0";
            }
            log.info("超融合登录信息：HOST:" + url + ";USERNAME:" + userName + ";password:" + password);
            MwLoginSuperFusionParam param = new MwLoginSuperFusionParam();
            param.setUserName(userName);
            param.setUrl(url);
            superFusionIp = url;
            //密码加密，生成指定密文
            String encryptStr = encryptedPassWd(password);
            param.setPassword(encryptStr);
            param.setModelId(Integer.valueOf(modelId));
            log.info("获取的超融合登录参数::" + param);
            //获取token、ticket
            getTicker(param);
            if (Strings.isNullOrEmpty(tokens) || Strings.isNullOrEmpty(tickets)) {
                return Reply.fail(500, "登录失败");
            }
        } catch (Exception e) {
            log.error("getAssetsLoginParam to fail::" + e);
            return Reply.fail(500, "登录失败");
        }
        return Reply.ok("登录成功");
    }


    /**
     * 根据存储设备Id获取详情信息
     */
    @Override
    public Reply getSuperFusionStorageInfo(QuerySuperFusionHistoryParam param) {
        SuperFusionBaseStorageParam storageBaseData = new SuperFusionBaseStorageParam();
        String cookie = loginCookie + tickets;
        try {
            //获取存储设备的基础数据
            String url = "https://" + superFusionIp + "/vapi/json/vs/vs_config/disk_info";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens + ";url:" + url);
            String jsonText = ModelOKHttpUtils.builder().url(url)
                    .addParam("disk_id", param.getNodeId())
                    .addParam("host_name", param.getPId())
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String jsonText = readTxt("D:\\files\\SuperFusion\\storageIdInfo.txt");
            log.info("获取存储设备的基础数据::" + jsonText);

            JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
            //接口返回成功
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                Map map = (Map) strInfoJson.get("data");
                if (map != null) {
                    //基础信息
                    storageBaseData.setDiskSn(strValueConvert(map.get("disk_sn")));
                    storageBaseData.setVolumeName(strValueConvert(map.get("volume_name")));
                    storageBaseData.setName(strValueConvert(map.get("disk_location")) + "号盘");
                    storageBaseData.setHostId(strValueConvert(map.get("host_name")));
                    storageBaseData.setStatus(strValueConvert(map.get("staus")));
                    storageBaseData.setType(strValueConvert(map.get("disk_type")));
                }
            }

            //获取存储设备的历史数据
            String url1 = "https://" + superFusionIp + "/vapi/json/vs/vs_status/disk_io_status";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens + ";url:" + url1);
            String disk_uuid = "";
            String host_id = "";
            if (!Strings.isNullOrEmpty(param.getNodeId()) && param.getNodeId().split("_").length > 0) {
                disk_uuid = param.getNodeId().split("_")[1];
            }
            if (!Strings.isNullOrEmpty(param.getPId()) && param.getPId().split("-").length > 0) {
                host_id = param.getPId().split("-")[1];
            }

            String json1Text = ModelOKHttpUtils.builder().url(url1)
                    .addParam("disk_uuid", disk_uuid)
                    .addParam("host_name", host_id)
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String json1Text = readTxt("D:\\files\\SuperFusion\\storageHistory.txt");
            log.info("获取存储设备的历史数据::" + json1Text);

            JSONObject strInfoJson1 = JSONObject.parseObject(json1Text != null ? json1Text : "");
            //接口返回成功
            if (strInfoJson1.get("success") != null && HTTP_SUCCESS.equals(strInfoJson1.get("success").toString())) {
                Map map = (Map) strInfoJson1.get("data");
                if (map != null) {
                    //历史信息
                    Map itemMap = new HashedMap();
                    //IO次数趋势
                    if (map.get("iops_data") != null) {
                        String item = "iops_data";
                        getHistoryMap(map, item, itemMap);
                    }
                    //IO速率趋势
                    if (map.get("throughoutput_data") != null) {
                        String item = "throughoutput_data";
                        getHistoryMap(map, item, itemMap);
                    }
                    storageBaseData.setData(itemMap);
                }
            }
        } catch (Exception e) {
            log.error("getSuperFusionStorageInfo to fail ::", e);
        }
        return Reply.ok(storageBaseData);
    }


    /**
     * 根据虚拟机Id获取详情信息
     */
    @Override
    public Reply getSuperFusionVmInfo(QuerySuperFusionHistoryParam param) {
        SuperFusionBaseVMData vmBaseData = new SuperFusionBaseVMData();
        String cookie = loginCookie + tickets;
        try {
            //获取虚拟机的详情数据
            String url = "https://" + superFusionIp + "/vapi/extjs/cluster/vm/" + param.getNodeId() + "/info";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens + ";url:" + url);
            String jsonText = ModelOKHttpUtils.builder().url(url)
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
            log.info("获取虚拟机的详情数据::" + jsonText);
//            String jsonText = readTxt("D:\\files\\SuperFusion\\vmIdInfo.txt");
            JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
            //接口返回成功
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                Map map = (Map) strInfoJson.get("data");
                if (map != null) {
                    //基础信息
                    vmBaseData.setName(strValueConvert(map.get("name")));
                    String upTime = SeverityUtils.getLastTime(longValueConvert(map.get("uptime")));
                    vmBaseData.setUptime(upTime);
                    vmBaseData.setHostName(strValueConvert(map.get("hostname")));
                    //CPU信息
                    if (map.get("cpu_status") != null) {
                        Map cpuMap = (Map) map.get("cpu_status");
                        vmBaseData.setMhz(getValueUnits(new BigDecimal(intValueConvert(cpuMap.get("mhz"))), "MHz"));
                        vmBaseData.setCpus(intValueConvert(cpuMap.get("cpus")));
                        vmBaseData.setCpuRatio(getValueRatio(cpuMap.get("ratio")));
                    }
                    //物理内存信息
                    if (map.get("mem_status") != null) {
                        Map memMap = (Map) map.get("mem_status");
                        vmBaseData.setMemRatio(getValueRatio(memMap.get("ratio")));
                        long memFree = longValueConvert(memMap.get("free"));
                        long memTotal = longValueConvert(memMap.get("total"));
                        vmBaseData.setMemFree(getValueUnits(new BigDecimal(memFree), "B"));
                        vmBaseData.setMemTotal(getValueUnits(new BigDecimal(memTotal), "B"));
                    }
                    //磁盘使用率
                    if (map.get("disk_status") != null) {
                        List<Map> diskList = (List<Map>) map.get("disk_status");
                        if (CollectionUtils.isNotEmpty(diskList)) {
                            Map diskMap = (Map) diskList.get(0);
                            vmBaseData.setDiskRatio(getValueRatio(diskMap.get("ratio")));
                            long diskFree = longValueConvert(diskMap.get("free"));
                            long diskTotal = longValueConvert(diskMap.get("total"));
                            vmBaseData.setDiskFree(getValueUnits(new BigDecimal(diskFree), "B"));
                            vmBaseData.setDiskTotal(getValueUnits(new BigDecimal(diskTotal), "B"));
                        }

                    }
                    Map itemMap = new HashedMap();
                    //流速趋势、包速率趋势
                    if (map.get("net_sheet") != null) {
                        Map netkMap = (Map) map.get("net_sheet");
                        if (netkMap.get("bps") != null) {
                            String item = "bps";
                            getHistoryMap(netkMap, item, itemMap);
                        }
                        if (netkMap.get("pps") != null) {
                            String item = "pps";
                            getHistoryMap(netkMap, item, itemMap);
                        }
                    }
                    //cpu趋势
                    if (map.get("cpu_sheet") != null) {
                        String item = "cpu_sheet";
                        getHistoryMap(map, item, itemMap);
                    }
                    //内存趋势
                    if (map.get("mem_sheet") != null) {
                        String item = "mem_sheet";
                        getHistoryMap(map, item, itemMap);
                    }
                    //IO速率趋势、IO次数趋势
                    if (map.get("disk_sheet") != null) {
                        Map mapSheet = (Map) map.get("disk_sheet");
                        if (mapSheet.get("oper") != null) {
                            String item = "oper";
                            getHistoryMap(mapSheet, item, itemMap);
                        }
                        if (mapSheet.get("speed") != null) {
                            String item = "speed";
                            getHistoryMap(mapSheet, item, itemMap);
                        }
                    }
                    vmBaseData.setData(itemMap);
                }
            }
        } catch (Exception e) {
            log.error("getSuperFusionVmInfo to fail ::", e);
        }
        return Reply.ok(vmBaseData);
    }

    /**
     * 获取主机监控项历史数据
     */
    @Override
    public Reply getSuperFusionHostHistory(QuerySuperFusionHistoryParam param) {
        List<SuperFusionHistoryData> historyDataList = new ArrayList<>();
        String cookie = loginCookie + tickets;
        try {
            //获取主机监控项历史数据
            String url = "https://" + superFusionIp + "/vapi/extjs/cluster/node_sheet";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens + ";url:" + url);
            String jsonText = ModelOKHttpUtils.builder().url(url)
                    .addParam("time_frame", param.getTimeFrame())
                    .addParam("node", param.getNodeId())
                    .addParam("data_type", param.getDataType())
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String jsonText = readTxt("D:\\files\\SuperFusion\\hostHistory.txt");
            log.info("获取主机监控项历史数据::" + jsonText);
            JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
            //接口返回成功
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                List<Map> list = (List<Map>) strInfoJson.get("data");
                historyDataList = historyValueConvert(list);
            }
        } catch (Exception e) {
            log.error("getSuperFusionHostHistory to fail::", e);
        }
        return Reply.ok(historyDataList);
    }


    /**
     * 根据主机Id获取主机详情信息
     */
    @Override
    public Reply getSuperFusionHostInfo(QuerySuperFusionHistoryParam param) {
        SuperFusionBaseHostData hostBaseData = new SuperFusionBaseHostData();
        String cookie = loginCookie + tickets;
        try {
            //获取主机的基础数据
            String url = "https://" + superFusionIp + "/vapi/extjs/nodes/" + param.getNodeId() + "/info";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens + ";url:" + url);
            String jsonText = ModelOKHttpUtils.builder().url(url)
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String jsonText = readTxt("D:\\files\\SuperFusion\\hostIdInfo.txt");
            log.info("获取主机的基础数据::" + jsonText);
            JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
            //接口返回成功
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                Map map = (Map) strInfoJson.get("data");
                if (map != null) {
                    //基础信息
                    hostBaseData.setIp(strValueConvert(map.get("ip")));
                    hostBaseData.setName(strValueConvert(map.get("name")));
                    hostBaseData.setOsVersion(strValueConvert(map.get("ow_version")));
                    hostBaseData.setVmNum(intValueConvert(map.get("running_vms")));
                    hostBaseData.setHardWareType(strValueConvert(map.get("hardware_type")));
                    String upTime = SeverityUtils.getLastTime(longValueConvert(map.get("uptime")));
                    hostBaseData.setUptime(upTime);
                    //物理内存信息
                    if (map.get("mem_status") != null) {
                        Map memMap = (Map) map.get("mem_status");
                        hostBaseData.setMemRatio(getValueRatio(memMap.get("ratio")));
                        long memFree = longValueConvert(memMap.get("free"));
                        long memTotal = longValueConvert(memMap.get("total"));
                        hostBaseData.setMemFree(getValueUnits(new BigDecimal(memFree), "B"));
                        hostBaseData.setMemTotal(getValueUnits(new BigDecimal(memTotal), "B"));
                    }
                    //CPU
                    if (map.get("cpu_status") != null) {
                        Map cpuMap = (Map) map.get("cpu_status");
                        hostBaseData.setCores(intValueConvert(cpuMap.get("cores")));
                        hostBaseData.setCpuThreads(intValueConvert(cpuMap.get("cpu_threads")));
                        hostBaseData.setCpuRatio(getValueRatio(cpuMap.get("ratio")));
                        hostBaseData.setSockets(intValueConvert(cpuMap.get("sockets")));
                        hostBaseData.setCpuType(strValueConvert(cpuMap.get("type")));
                    }
                    //配置内存信息
                    if (map.get("conf_mem_status") != null) {
                        Map confMap = (Map) map.get("conf_mem_status");
                        long baseTotal = longValueConvert(confMap.get("base_total_byte"));
                        long confUsed = longValueConvert(confMap.get("conf_used_byte"));
                        if (baseTotal == 0l) {
                            hostBaseData.setConfRatio("0");
                        } else {
                            hostBaseData.setConfRatio(getValueRatio((double) confUsed / (double) baseTotal));
                        }
                        hostBaseData.setConfTotalByte(getValueUnits(new BigDecimal(baseTotal), "B"));
                        hostBaseData.setConfUsedByte(getValueUnits(new BigDecimal(confUsed), "B"));
                    }

                }
            }
        } catch (Exception e) {
            log.error("getSuperFusionHostInfo to fail ::", e);
        }
        return Reply.ok(hostBaseData);
    }


    /**
     * 获取资产，集群级别的基础数据
     *
     * @return
     */
    @Override
    public Reply getSuperFusionBaseInfo() {
        SuperFusionBaseClusterData baseData = new SuperFusionBaseClusterData();
        String cookie = loginCookie + tickets;
        try {
            //获取基础信息数据
            String url = "https://" + superFusionIp + "/vapi/extjs/index/overview";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens);
            String jsonText = ModelOKHttpUtils.builder().url(url)
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String jsonText = readTxt("D:\\files\\SuperFusion\\overview.txt");
            log.info("获取基础信息数据::" + jsonText);
            JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
            //接口返回成功
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                Map map = (Map) strInfoJson.get("data");
                if (map != null) {
                    if (map.get("cl_info") != null) {
                        Map clInfoMap = (Map) map.get("cl_info");
                        baseData.setIp(strValueConvert(clInfoMap.get("ip")));
                        baseData.setName(strValueConvert(clInfoMap.get("name")));
                    }
                    if (map.get("vm") != null) {
                        Map vmMap = (Map) map.get("vm");
                        baseData.setVmNum(intValueConvert(vmMap.get("cnt")));
                        baseData.setVmOffNum(intValueConvert(vmMap.get("off")));
                        baseData.setVmOnNum(intValueConvert(vmMap.get("on")));
                    }
                    if (map.get("host") != null) {
                        Map hostMap = (Map) map.get("host");
                        baseData.setHostNum(intValueConvert(hostMap.get("cnt")));
                        baseData.setHostOffLine(intValueConvert(hostMap.get("offline")));
                        baseData.setHostOnLine(intValueConvert(hostMap.get("online")));
                    }
                    //配置内存
                    if (map.get("conf_mem") != null) {
                        Map confMemMap = (Map) map.get("conf_mem");
                        long baseTotal = longValueConvert(confMemMap.get("base_total_byte"));
                        long confUsed = longValueConvert(confMemMap.get("conf_used_byte"));
                        String totalConf = getValueUnits(new BigDecimal(baseTotal), "B");
                        baseData.setTotalConf(totalConf);
                        String usedConf = getValueUnits(new BigDecimal(confUsed), "B");
                        baseData.setUsedConf(usedConf);
                        if (baseTotal == 0l) {
                            baseData.setRatioConf("0");
                        } else {
                            baseData.setRatioConf(getValueRatio(((double) confUsed / (double) baseTotal)));
                        }
                    }
                    //CPU信息
                    if (map.get("cpu") != null) {
                        Map cpuMap = (Map) map.get("cpu");
                        baseData.setRatioCpu(getValueRatio(cpuMap.get("ratio")));
                        long totalCpu = longValueConvert(cpuMap.get("total"));
                        String totalCpuStr = getValueUnits(new BigDecimal(totalCpu), "MHz");
                        baseData.setTotalCpu(totalCpuStr);
                        long usedCpu = longValueConvert(cpuMap.get("used"));
                        baseData.setUsedCpu(getValueUnits(new BigDecimal(usedCpu), "MHz"));
                    }
                    //物理内存Memory
                    if (map.get("mem") != null) {
                        Map memMap = (Map) map.get("mem");
                        baseData.setRatioMem(getValueRatio(memMap.get("ratio")));
                        long totalMem = longValueConvert(memMap.get("total"));
                        baseData.setTotalMem(getValueUnits(new BigDecimal(totalMem), "B"));
                        long usedMem = longValueConvert(memMap.get("used"));
                        baseData.setUsedMem(getValueUnits(new BigDecimal(usedMem), "B"));
                    }
                    //存储信息
                    if (map.get("stg") != null) {
                        Map stgMap = (Map) map.get("stg");
                        baseData.setRatioStg(getValueRatio(stgMap.get("ratio")));
                        long totalStg = longValueConvert(stgMap.get("total"));
                        baseData.setTotalStg(getValueUnits(new BigDecimal(totalStg), "B"));
                        long usedStg = longValueConvert(stgMap.get("used"));
                        baseData.setUsedStg(getValueUnits(new BigDecimal(usedStg), "B"));
                    }

                }
            }
        } catch (Exception e) {
            log.error("getSuperFusionBaseInfo to fail::", e);
        }
        return Reply.ok(baseData);
    }


    /**
     * 获取所有虚拟化设备列表
     *
     * @return
     */
    @Override
    public Reply getAllStorageList(MwQuerySuperFusionParam mParam) {
        String cookie = loginCookie + tickets;
        List<SuperFusionStorageListParam> storageAllList = new ArrayList<>();
        try {
            //获取存储设备列表数据
            String url = "https://" + superFusionIp + "/vapi/json/vs/vs_config/get_all_disks";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens);
            String jsonText = ModelOKHttpUtils.builder().url(url)
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String jsonText = readTxt("D:\\files\\SuperFusion\\get_all_disks.txt");
            log.info("获取所有存储信息数据::" + jsonText);
            JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
            //接口返回成功
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                Map map = (Map) strInfoJson.get("data");
                Map<String, Long> mapCount = new HashedMap();
                if (map != null) {
                    //获取存储数据信息
                    List<Map> disks = (List<Map>) map.get("disks");
                    if (CollectionUtils.isNotEmpty(disks)) {
                        for (Map disk : disks) {
                            SuperFusionStorageListParam storageParam = new SuperFusionStorageListParam();
                            storageParam.setName(strValueConvert(disk.get("disk_name")));
                            storageParam.setId(strValueConvert(disk.get("disk")));
                            storageParam.setType(strValueConvert(disk.get("disk_type")));
                            //经测试，ip字段为主机名称，host_name字段为主机id
                            storageParam.setHostName(strValueConvert(disk.get("ip")));
                            storageParam.setHostId(strValueConvert(disk.get("host_name")));
                            storageParam.setVolumeName(strValueConvert(disk.get("volume_name")));
                            if (disk.get("iostat") != null) {
                                String units = "B/s";
                                Map ioMap = (Map) disk.get("iostat");
                                //IO读速率
                                long ioReadRate = longValueConvert(ioMap.get("io_read_rate"));
                                //单位转换
                                storageParam.setIoReadRate(getValueUnits(new BigDecimal(ioReadRate), units));
                                storageParam.setIoReadVal(ioReadRate);
                                //IO写速率
                                long ioWriteRate = longValueConvert(ioMap.get("io_write_rate"));
                                //单位转换
                                storageParam.setIoWriteRate(getValueUnits(new BigDecimal(ioWriteRate), units));
                                storageParam.setIoWriteVal(ioWriteRate);
                                //IO延时
                                storageParam.setIoAwait(strValueConvert(ioMap.get("io_await")) + "ms");
                            }
                            long diskSize = longValueConvert(disk.get("disk_size"));
                            long diskUsed = longValueConvert(disk.get("used"));
                            //容量使用率
                            if (diskSize == 0l) {
                                storageParam.setStorageRatio("0");
                                storageParam.setStorageVal(0d);
                            } else {
                                storageParam.setStorageRatio(getValueRatio(((double) diskUsed / (double) diskSize)));
                                storageParam.setStorageVal(Double.valueOf(storageParam.getStorageRatio()));
                            }
                            storageAllList.add(storageParam);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("getAllStorageList to fail::", e);
        }
        List<SuperFusionStorageListParam> sortList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(storageAllList)) {
            sortList = storageAllList.stream().sorted(Comparator.comparing(SuperFusionStorageListParam::getHostId).
                    thenComparing(SuperFusionStorageListParam::getName)
                    .reversed()).collect(Collectors.toList());
        }
        if (mParam.getSortField() != null && StringUtils.isNotEmpty(mParam.getSortField())) {
            ListSortUtil<SuperFusionStorageListParam> finalHostTableDtos = new ListSortUtil<>();
            finalHostTableDtos.sort(sortList, mParam.getSortField(), mParam.getSortMode());
        }
        return Reply.ok(sortList);
    }

    /**
     * 获取所有虚拟化设备列表
     *
     * @return
     */
    @Override
    public Reply getAllVmList(MwQuerySuperFusionParam mParam) {
        String cookie = loginCookie + tickets;
        List<SuperFusionVMListParam> vmAllList = new ArrayList<>();
        try {
            //获取虚拟化设备列表数据
            String url = "https://" + superFusionIp + "/vapi/extjs/cluster/vms";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens);
            String jsonText = ModelOKHttpUtils.builder().url(url)
                    .addParam("group_type", "host")
                    .addParam("scene", "resources_used")
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String jsonText = readTxt("D:\\files\\SuperFusion\\clusterVMs.txt");
            log.info("获取虚拟化设备列表数据::" + jsonText);
            JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
            //接口返回成功
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                List<Map> list = (List<Map>) strInfoJson.get("data");//获取主机分组信息
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Map m : list) {
                        if (m.get("id") != null && "default".equals(m.get("id").toString())) {//去除默认分组
                            continue;
                        }
                        if (m.get("data") != null && CollectionUtils.isNotEmpty((List<Map>) m.get("data"))) {//获取虚拟机信息
                            List<Map> vmList = (List<Map>) m.get("data");
                            for (Map vmMap : vmList) {
                                SuperFusionVMListParam vmListParam = new SuperFusionVMListParam();
                                vmListParam.setName(strValueConvert(vmMap.get("name")));
                                vmListParam.setId(strValueConvert(vmMap.get("vmid")));
                                vmListParam.setHost(strValueConvert(vmMap.get("host")));
                                vmListParam.setHostName(strValueConvert(vmMap.get("hostname")));
                                String status = "运行中";
                                if ("running".equals(strValueConvert(vmMap.get("status")))) {
                                    status = "运行中";
                                } else if ("stopped".equals(strValueConvert(vmMap.get("status")))) {
                                    status = "已关机";
                                } else if ("run_backup".equals(strValueConvert(vmMap.get("status")))) {
                                    status = "正在备份";
                                } else {
                                    status = "未知";
                                }
                                vmListParam.setStatus(status);
                                vmListParam.setCpuRatio(getValueRatio(vmMap.get("cpu_ratio")));
                                vmListParam.setCpuVal(doubleValueConvert(vmListParam.getCpuRatio()));
                                vmListParam.setMemRatio(getValueRatio(vmMap.get("mem_ratio")));
                                vmListParam.setMemVal(doubleValueConvert(vmListParam.getMemRatio()));
                                vmListParam.setIoRatio(getValueRatio(vmMap.get("io_ratio")));
                                vmListParam.setIoVal(doubleValueConvert(vmListParam.getIoRatio()));
                                vmAllList.add(vmListParam);
                            }
                        }
                    }
                }
            }
            log.info("获取虚拟化vmAllList::" + vmAllList);

            /*
             *  获取虚拟化设备Ip信息
             */
            String url1 = "https://" + superFusionIp + "/vapi/json/cluster/get_vms_ip_list";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens);
            String ipListStr = ModelOKHttpUtils.builder().url(url1)
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String ipListStr = readTxt("D:\\files\\SuperFusion\\get_vms_ip_list.txt");
            log.info("获取虚拟化设备IP数据::" + ipListStr);
            JSONObject ipListJson = JSONObject.parseObject(ipListStr != null ? ipListStr : "");
            Map ipMap = new HashedMap();
            //接口返回成功
            if (ipListJson.get("success") != null && HTTP_SUCCESS.equals(ipListJson.get("success").toString())) {
                ipMap = (Map) ipListJson.get("data");
            }

            if (CollectionUtils.isNotEmpty(vmAllList)) {
                for (SuperFusionVMListParam vm : vmAllList) {
                    if (ipMap != null && ipMap.containsKey(vm.getHost())) {
                        Map vmIpMap = (Map) ipMap.get(vm.getHost());
                        if (vmIpMap != null && vmIpMap.containsKey(vm.getId())) {
                            Map ipNet = (Map) vmIpMap.get(vm.getId());
                            List<String> ipLists = new ArrayList<>();
                            ipNet.forEach((k, v) -> {
                                if (v != null) {
                                    Map map = ((Map) v);
                                    map.forEach((key, val) -> {
                                        ipLists.add(strValueConvert(key));
                                    });
                                }
                            });
                            //设置Ip地址信息
                            if (CollectionUtils.isNotEmpty(ipLists)) {
                                vm.setIp(String.join(",", ipLists));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("getAllHostList to fail::", e);
        }
        List<SuperFusionVMListParam> sortList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(vmAllList)) {
            sortList = vmAllList.stream().sorted(Comparator.comparing(SuperFusionVMListParam::getName)
                    .reversed()).collect(Collectors.toList());
        }
        if (mParam.getSortField() != null && StringUtils.isNotEmpty(mParam.getSortField())) {
            ListSortUtil<SuperFusionVMListParam> finalHostTableDtos = new ListSortUtil<>();
            finalHostTableDtos.sort(sortList, mParam.getSortField(), mParam.getSortMode());
        }
        return Reply.ok(sortList);
    }


    /**
     * 获取所有主机列表数据
     */
    @Override
    public Reply getAllHostList(MwQuerySuperFusionParam mParam) {
        String cookie = loginCookie + tickets;
        List<SuperFusionHostListParam> hostList = new ArrayList<>();
        try {
            //获取所有主机列表数据
            String url = "https://" + superFusionIp + "/vapi/extjs/index/host_list";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens);
            String jsonText = ModelOKHttpUtils.builder().url(url)
                    .addParam("sort_key", "name")
                    .addParam("sort_direct", "DESC")
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String jsonText = readTxt("D:\\files\\SuperFusion\\host_list.txt");
            log.info("获取所有主机列表数据::" + jsonText);
            JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
            //接口返回成功
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                List<Map> list = (List<Map>) strInfoJson.get("data");//获取主机列表信息
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Map m : list) {
                        SuperFusionHostListParam hostParam = new SuperFusionHostListParam();
                        hostParam.setId(m.get("id") != null ? m.get("id").toString() : "");
                        hostParam.setCpuRatio(getValueRatio(m.get("cpu_ratio")));
                        hostParam.setCpuVal(doubleValueConvert(hostParam.getCpuRatio()));
                        hostParam.setMemRatio(getValueRatio(m.get("mem_ratio")));
                        hostParam.setMemVal(doubleValueConvert(hostParam.getMemRatio()));
                        hostParam.setName(m.get("name") != null ? m.get("name").toString() : "");
                        hostParam.setIp(m.get("ip") != null ? m.get("ip").toString() : "");
                        hostParam.setMaster(m.get("master") != null ? m.get("master").toString() : "0");
                        hostParam.setStatus(intValueConvert(m.get("status")));
                        if (m.get("conf_mem") != null) {
                            Map confMemMap = (Map) m.get("conf_mem");
                            long baseTotal = confMemMap.get("base_total_byte") != null ? Long.valueOf(confMemMap.get("base_total_byte").toString()) : 1l;
                            long confUsed = confMemMap.get("conf_used_byte") != null ? Long.valueOf(confMemMap.get("conf_used_byte").toString()) : 0l;
                            hostParam.setConfRatio(getValueRatio((double) confUsed / (double) baseTotal));
                            hostParam.setConfVal(doubleValueConvert(hostParam.getConfRatio()));
                        }
                        hostList.add(hostParam);
                    }
                }
            }

        } catch (Exception e) {
            log.error("getAllHostList to fail::", e);
        }
        List<SuperFusionHostListParam> sortList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(hostList)) {
            sortList = hostList.stream().sorted(Comparator.comparing(SuperFusionHostListParam::getName)
                    .reversed()).collect(Collectors.toList());
        }
        if (mParam.getSortField() != null && StringUtils.isNotEmpty(mParam.getSortField())) {
            ListSortUtil<SuperFusionHostListParam> finalHostTableDtos = new ListSortUtil<>();
            finalHostTableDtos.sort(sortList, mParam.getSortField(), mParam.getSortMode());
        }
        return Reply.ok(sortList);
    }

    /**
     * 获取超融合树结构
     *
     * @return
     */
    @Override
    public Reply getSuperFusionTree(MwQuerySuperFusionParam mParam) {
        String cookie = loginCookie + tickets;
        List<SuperFusionTreeParam> paramList = new ArrayList<>();
        //主机数量
        int hostNum = 0;
        try {
            SuperFusionTreeParam treeParam = new SuperFusionTreeParam();
            if (storageTreeType.equals(mParam.getTreeType())) {//数据存储树结构
                //获取所有存储信息数据
                String url = "https://" + superFusionIp + "/vapi/json/vs/vs_config/get_all_disks";
                log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens);
                String jsonText = ModelOKHttpUtils.builder().url(url)
                        .addHeader("CSRFPreventionToken", tokens)
                        .addHeader("Cookie", cookie)
                        .get()
                        .sync();
//                String jsonText = readTxt("D:\\files\\SuperFusion\\get_all_disks.txt");
                log.info("获取所有存储信息数据::" + jsonText);
                JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
                //接口返回成功
                if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                    Map map = (Map) strInfoJson.get("data");
                    Map<String, Long> mapCount = new HashedMap();
                    if (map != null) {
                        //获取存储数据信息
                        List<Map> disks = (List<Map>) map.get("disks");
                        if (CollectionUtils.isNotEmpty(disks)) {
                            mapCount = disks.stream().filter(s -> s.get("host_name") != null).collect(Collectors.groupingBy(s -> s.get("host_name").toString(), Collectors.counting()));
                            for (Map disk : disks) {
                                treeParam = new SuperFusionTreeParam();
                                //经测试，ip字段为主机名称，host_name字段为主机id
                                treeParam.setName(disk.get("disk_name") != null ? disk.get("disk_name").toString() : "");
                                treeParam.setId(disk.get("disk") != null ? disk.get("disk").toString() : "");
                                treeParam.setType("storage");
                                treeParam.setPId(disk.get("host_name") != null ? disk.get("host_name").toString() : "");
                                paramList.add(treeParam);
                            }
                        }
                        List<Map> volumes = (List<Map>) map.get("volumes");
                        if (CollectionUtils.isNotEmpty(volumes)) {
                            //获取存储主机分组信息
                            List<Map> hosts = (List<Map>) volumes.get(0).get("hosts");
                            for (Map m : hosts) {
                                treeParam = new SuperFusionTreeParam();
                                treeParam.setNum(0);
                                treeParam.setName(m.get("ip") != null ? m.get("ip").toString() : "");
                                treeParam.setId(m.get("host_name") != null ? m.get("host_name").toString() : "");//经测试，ip字段为名称，host_name字段为id
                                if (mapCount != null && mapCount.containsKey(treeParam.getId())) {
                                    treeParam.setNum(intValueConvert(mapCount.get(treeParam.getId())));
                                }
                                treeParam.setType("host");
                                treeParam.setPId("cluster_0");
                                hostNum++;
                                paramList.add(treeParam);
                            }
                        }
                    }
                }
            } else {//宿主机、虚拟机树结构
                //获取虚拟化设备分组（按主机分组）
                String url = "https://" + superFusionIp + "/vapi/extjs/cluster/vms";
                log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens);
                String jsonText = ModelOKHttpUtils.builder().url(url)
                        .addParam("group_type", "host")
                        .addParam("scene", "resources_used")
                        .addHeader("CSRFPreventionToken", tokens)
                        .addHeader("Cookie", cookie)
                        .get()
                        .sync();
//                String jsonText = readTxt("D:\\files\\SuperFusion\\clusterVMs.txt");
                log.info("获取虚拟化设备分组::" + jsonText);
                JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
                //接口返回成功
                if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                    List<Map> list = (List<Map>) strInfoJson.get("data");//获取主机分组信息
                    if (CollectionUtils.isNotEmpty(list)) {
                        for (Map m : list) {
                            if (m.get("id") != null && "default".equals(m.get("id").toString())) {//去除默认分组
                                continue;
                            }
                            hostNum++;
                            treeParam = new SuperFusionTreeParam();
                            treeParam.setNum(intValueConvert(m.get("vm_num")));
                            treeParam.setName(m.get("name") != null ? m.get("name").toString() : "");
                            treeParam.setId(m.get("id") != null ? m.get("id").toString() : "");
                            treeParam.setType("host");
                            treeParam.setPId("cluster_0");
                            paramList.add(treeParam);
                            if (m.get("data") != null && CollectionUtils.isNotEmpty((List<Map>) m.get("data"))) {//获取虚拟机信息
                                List<Map> vmList = (List<Map>) m.get("data");
                                for (Map vm : vmList) {
                                    treeParam = new SuperFusionTreeParam();
                                    treeParam.setNum(1);
                                    treeParam.setName(vm.get("name") != null ? vm.get("name").toString() : "");
                                    treeParam.setId(vm.get("vmid") != null ? vm.get("vmid").toString() : "");
                                    treeParam.setType("vm");
                                    treeParam.setPId(vm.get("host") != null ? vm.get("host").toString() : "");
                                    paramList.add(treeParam);
                                }
                            }
                        }
                    }

                }
            }
            //获取集群名称
            String url1 = "https://" + superFusionIp + "/vapi/extjs/cluster/cluster_ip";
            String clusterText = ModelOKHttpUtils.builder().url(url1)
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String clusterText = readTxt("D:\\files\\SuperFusion\\cluster_ip.txt");
            log.info("获取获取集群名称::" + clusterText);
            JSONObject clusterJson = JSONObject.parseObject(clusterText != null ? clusterText : "");
            //接口返回成功
            if (clusterJson.get("success") != null && HTTP_SUCCESS.equals(clusterJson.get("success").toString())) {
                if (clusterJson.get("data") != null) {
                    Map map = (Map) clusterJson.get("data");
                    treeParam = new SuperFusionTreeParam();
                    treeParam.setName(map.get("name") != null ? map.get("name").toString() : "");
                    treeParam.setType("cluster");//类型为cluster
                    treeParam.setId("cluster_0");
                    treeParam.setNum(hostNum);
                    //将超融合实例id设备父节点
                    treeParam.setPId(mParam.getModelInstanceId() + "");
                    paramList.add(treeParam);
                }
            }

            //将超融合资产实例加入树结构
            treeParam = new SuperFusionTreeParam();
            treeParam.setName(mParam.getModelInstanceName());
            treeParam.setType("device");
            treeParam.setId(mParam.getModelInstanceId() + "");
            treeParam.setNum(1);
            //设置0为设备父节点
            treeParam.setPId("0");
            paramList.add(treeParam);
        } catch (Exception e) {
            log.error("getSuperFusionTree to fail::", e);
        }
        List<SuperFusionTreeParam> sortList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(paramList)) {
            sortList = paramList.stream().sorted(Comparator.comparing(SuperFusionTreeParam::getName)
                    .reversed()).collect(Collectors.toList());
        }
        return Reply.ok(sortList);
    }

    /**
     * 同步超融合设备
     *
     * @return
     */
    @Override
    public Reply saveSuperFusionDeviceData(MwQuerySuperFusionParam mParam) {
        QueryModelInstanceParam qParam = new QueryModelInstanceParam();
        qParam.setModelId(mParam.getModelId());
        qParam.setModelIndex(mParam.getModelIndex());
        qParam.setModelInstanceId(mParam.getModelInstanceId());
        //登录,获取Token
        getAssetsLoginParam(qParam);
        Map<Integer, List<ModelInfo>> modelAndParentInfoListMap = new HashMap<>();
        List<String> modelIndexs = new ArrayList<>();
        List<Integer> modelIdList = Arrays.asList(SUPER_FUSION.getModelId(), FUSION_CLUSTER.getModelId(), FUSION_HOST.getModelId(), FUSION_VM.getModelId(), FUSION_STORAGE.getModelId());
        mwModelRancherServiceImpl.getPropertyInfos(modelIndexs, modelIdList, modelAndParentInfoListMap);

        String cookie = loginCookie + tickets;
        List<SuperFusionTreeParam> paramList = new ArrayList<>();
        //主机数量
        int hostNum = 0;
        try {
            SuperFusionTreeParam treeParam = new SuperFusionTreeParam();
//            if (storageTreeType.equals(mParam.getTreeType())) {//数据存储树结构
            //获取所有存储信息数据
            String url = "https://" + superFusionIp + "/vapi/json/vs/vs_config/get_all_disks";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens);
            String jsonText = ModelOKHttpUtils.builder().url(url)
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String jsonText = readTxt("D:\\files\\SuperFusion\\get_all_disks.txt");
            log.info("获取所有存储信息数据::" + jsonText);
            JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
            //接口返回成功
            if (strInfoJson.get("success") != null && HTTP_SUCCESS.equals(strInfoJson.get("success").toString())) {
                Map map = (Map) strInfoJson.get("data");
                Map<String, Long> mapCount = new HashedMap();
                if (map != null) {
                    //获取存储数据信息
                    List<Map> disks = (List<Map>) map.get("disks");
                    if (CollectionUtils.isNotEmpty(disks)) {
                        mapCount = disks.stream().filter(s -> s.get("host_name") != null).collect(Collectors.groupingBy(s -> s.get("host_name").toString(), Collectors.counting()));
                        for (Map disk : disks) {
                            treeParam = new SuperFusionTreeParam();
                            //经测试，ip字段为主机名称，host_name字段为主机id
                            treeParam.setName(disk.get("disk_name") != null ? disk.get("disk_name").toString() : "");
                            treeParam.setId(disk.get("disk") != null ? disk.get("disk").toString() : "");
                            treeParam.setType("storage");
                            treeParam.setPId(disk.get("host_name") != null ? disk.get("host_name").toString() : "");
                            paramList.add(treeParam);
                        }
                    }
                }
            }
//            } else {//宿主机、虚拟机树结构
            //获取虚拟化设备分组（按主机分组）
            String url2 = "https://" + superFusionIp + "/vapi/extjs/cluster/vms";
            log.info("cookie::" + cookie + ";superFusionIp:" + superFusionIp + ";tokens:" + tokens);
            String jsonText2 = ModelOKHttpUtils.builder().url(url2)
                    .addParam("group_type", "host")
                    .addParam("scene", "resources_used")
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String jsonText2 = readTxt("D:\\files\\SuperFusion\\clusterVMs.txt");
            log.info("获取虚拟化设备分组::" + jsonText2);
            JSONObject strInfoJson2 = JSONObject.parseObject(jsonText2 != null ? jsonText2 : "");
            //接口返回成功
            if (strInfoJson2.get("success") != null && HTTP_SUCCESS.equals(strInfoJson2.get("success").toString())) {
                List<Map> list = (List<Map>) strInfoJson2.get("data");//获取主机分组信息
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Map m : list) {
                        if (m.get("id") != null && "default".equals(m.get("id").toString())) {//去除默认分组
                            continue;
                        }
                        hostNum++;
                        treeParam = new SuperFusionTreeParam();
                        treeParam.setNum(intValueConvert(m.get("vm_num")));
                        treeParam.setName(m.get("name") != null ? m.get("name").toString() : "");
                        treeParam.setId(m.get("id") != null ? m.get("id").toString() : "");
                        treeParam.setType("host");
                        treeParam.setPId("cluster_0");
                        paramList.add(treeParam);
                        if (m.get("data") != null && CollectionUtils.isNotEmpty((List<Map>) m.get("data"))) {//获取虚拟机信息
                            List<Map> vmList = (List<Map>) m.get("data");
                            for (Map vm : vmList) {
                                treeParam = new SuperFusionTreeParam();
                                treeParam.setNum(1);
                                treeParam.setName(vm.get("name") != null ? vm.get("name").toString() : "");
                                treeParam.setId(vm.get("vmid") != null ? vm.get("vmid").toString() : "");
                                treeParam.setType("vm");
                                treeParam.setPId(vm.get("host") != null ? vm.get("host").toString() : "");
                                paramList.add(treeParam);
                            }
                        }
                    }
                }

//                }
            }
            //获取集群名称
            String url1 = "https://" + superFusionIp + "/vapi/extjs/cluster/cluster_ip";
            String clusterText = ModelOKHttpUtils.builder().url(url1)
                    .addHeader("CSRFPreventionToken", tokens)
                    .addHeader("Cookie", cookie)
                    .get()
                    .sync();
//            String clusterText = readTxt("D:\\files\\SuperFusion\\cluster_ip.txt");
            log.info("获取获取集群名称::" + clusterText);
            JSONObject clusterJson = JSONObject.parseObject(clusterText != null ? clusterText : "");
            //接口返回成功
            if (clusterJson.get("success") != null && HTTP_SUCCESS.equals(clusterJson.get("success").toString())) {
                if (clusterJson.get("data") != null) {
                    Map map = (Map) clusterJson.get("data");
                    treeParam = new SuperFusionTreeParam();
                    treeParam.setName(map.get("name") != null ? map.get("name").toString() : "");
                    treeParam.setType("cluster");//类型为cluster
                    treeParam.setId("cluster_0");
                    treeParam.setNum(hostNum);
                    //将超融合实例id设备父节点
                    treeParam.setPId(mParam.getModelInstanceId() + "");
                    paramList.add(treeParam);
                }
            }

            //将超融合资产实例加入树结构
            treeParam = new SuperFusionTreeParam();
            treeParam.setName(mParam.getModelInstanceName());
            treeParam.setType("superfusion");
            treeParam.setId(mParam.getModelInstanceId() + "");
            treeParam.setNum(1);
            //设置0为设备父节点
            treeParam.setPId("0");
            paramList.add(treeParam);
            log.info("数据同步到的资产::" + paramList);
            if (CollectionUtils.isNotEmpty(paramList)) {
                List<AddAndUpdateModelInstanceParam> instanceInfoList = new ArrayList<>();
                log.info("获取插入es的数据:" + instanceInfoList);
                //数据校验匹配
                SuperFusionInstanceChangeParam changeParam = mwModelSuperFusionRelationManager.compareSuperFusionInfo(mParam.getModelInstanceId(), paramList);
                List<SuperFusionInfo> superFusionInfos = new ArrayList<>();
                //第一次数据同步
                if (!changeParam.isHasSuperFusionData()) {
                    getPropertiesInfoList(paramList, instanceInfoList, superFusionInfos, mParam.getRelationInstanceId(), modelAndParentInfoListMap);
                    log.info("获取superFusion插入es的数据:" + instanceInfoList);
                    syncAddData(instanceInfoList, superFusionInfos, mParam);
                } else {
                    log.info("superFusion同步匹配数据:" + changeParam);
                    //是否有新增数据
                    if (CollectionUtils.isNotEmpty(changeParam.getAddDatas())) {
                        //新增的数据
                        List<SuperFusionTreeParam> addLists = changeParam.getAddDatas();
                        getPropertiesInfoList(addLists, instanceInfoList, superFusionInfos, mParam.getRelationInstanceId(), modelAndParentInfoListMap);

                        //父节点数据
                        List<AddAndUpdateModelInstanceParam> instanceInfoListParent = new ArrayList<>();
                        //取父节点数据的superFusionInfos，存入neo4j中，形成完整的网状结构，
                        List<SuperFusionTreeParam> parentLists = changeParam.getPIdDatas();
                        getPropertiesInfoListByEditor(parentLists, instanceInfoListParent, superFusionInfos, mParam, modelAndParentInfoListMap);
                        log.info("获取superFusion新增数据:" + addLists + ";获取superFusion插入es的数据:" + instanceInfoList);
                        if (CollectionUtils.isNotEmpty(superFusionInfos)) {
                            syncAddData(instanceInfoList, superFusionInfos, mParam);
                        }
                    }
                    //是否有删除的数据
                    if (CollectionUtils.isNotEmpty(changeParam.getDeleteDatas())) {
                        List<SuperFusionTreeParam> deleteLists = changeParam.getDeleteDatas();
                        superFusionInfos = new ArrayList<>();
                        instanceInfoList = new ArrayList<>();
                        getPropertiesInfoListByEditor(deleteLists, instanceInfoList, superFusionInfos, mParam, modelAndParentInfoListMap);
                        if (CollectionUtils.isNotEmpty(deleteLists)) {
                            syncDeleteData(deleteLists);
                            if (graphEnable) {
                                //删除neo4j的关系数据
                                mwModelSuperFusionRelationManager.updateSuperFusionInfo(mParam, superFusionInfos, InstanceNotifyType.VirtualSyncDelete);
                            }
                        }
                    }
                    if (CollectionUtils.isNotEmpty(changeParam.getUpdateDatas())) {
                        //修改的数据
                        superFusionInfos = new ArrayList<>();
                        instanceInfoList = new ArrayList<>();
                        List<SuperFusionTreeParam> updateLists = changeParam.getUpdateDatas();
                        getPropertiesInfoListByEditor(updateLists, instanceInfoList, superFusionInfos, mParam, modelAndParentInfoListMap);
                        mwModelInstanceDao.batchEditorInstanceName(instanceInfoList);
                        mwModelInstanceService.editorData(instanceInfoList);
                    }
                }
            }
        } catch (Exception e) {
            log.error("saveSuperFusionDeviceData to fail::", e);
        }
        return Reply.ok("同步数据成功");
    }

    private static List<SuperFusionHistoryData> historyValueConvert(List<Map> list) {
        List<SuperFusionHistoryData> historyDataList = new ArrayList<>();
        String units = "";
        int interval = 0;
        long startTime = 0l;
        if (list != null && list.size() > 2) {//历史接口数据最少分为3层，第一层为时间，第二次为单位，后面的为具体的数据
            for (int x = 0; x < list.size(); x++) {
                Map ms = list.get(x);
                if (ms != null) {
                    String name = (String) ms.get("name");
                    if ("time".equals(name)) {
                        Map m = (Map) ms.get("data");
                        //时间间隔
                        interval = intValueConvert(m.get("interval"));
                        //起始时间点
                        startTime = longValueConvert(m.get("start"));
                    }
                    if ("unit".equals(name)) {
                        units = (String) ms.get("data");
                    }
                }
            }

            for (int x = 0; x < list.size(); x++) {
                Map ms = list.get(x);
                if (ms != null) {
                    String name = (String) ms.get("name");
                    List<String> strDataList = new ArrayList<>();
                    List<Long> valData = new ArrayList<>();
                    SuperFusionHistoryData historyData = new SuperFusionHistoryData();
                    String valueUnits = "";
                    if (!"time".equals(name) && !"unit".equals(name)) {
                        valData = JSONArray.parseArray(JSONArray.toJSONString(ms.get("data")), Long.class);
                        //获取list的平均值
                        long avgVal = getRoundedAverageValue(valData);
                        //使用平均值，来转换合适的单位
                        Map<String, String> convertedValue = UnitsUtil.getConvertedValue(new BigDecimal(avgVal), units);
                        valueUnits = convertedValue.get("units");
                        if (Strings.isNullOrEmpty(convertedValue.get("units"))) {
                            valueUnits = units;
                        }
                        //输出转为指定单位的数值
                        for (Long lg : valData) {
                            Map<String, String> convertedMap = UnitsUtil.getConvertedValue(new BigDecimal(lg), valueUnits, units);
                            String value = convertedMap.get("value");
                            // 创建DecimalFormat对象，指定保留两位小数
                            DecimalFormat decimalFormat = new DecimalFormat("#.##");
                            // 将double类型的数值格式化为字符串
                            String formattedNumber = decimalFormat.format(doubleValueConvert(value));
                            strDataList.add(formattedNumber);
                        }
                        List<String> timeList = generateTimePeriods(startTime * 1000l, interval, valData.size());
                        historyData.setUnits(valueUnits);
                        historyData.setData(strDataList);
                        historyData.setName(name);
                        historyData.setTime(timeList);
                        historyDataList.add(historyData);
                    }
                }
            }
        }
        return historyDataList;
    }

    private static void getHistoryMap(Map mapSheet, String item, Map itemMap) {
        if (mapSheet.get(item) != null) {
            Map mapInfo = (Map) mapSheet.get(item);
            Map dataMap = new HashedMap();
            if (mapInfo != null && mapInfo.get("min") != null) {
                List<Map> dayList = (List<Map>) mapInfo.get("min");
                List<SuperFusionHistoryData> historyList = historyValueConvert(dayList);
                dataMap.put("min", historyList);
            }
            if (mapInfo != null && mapInfo.get("ten_min") != null) {
                List<Map> dayList = (List<Map>) mapInfo.get("ten_min");
                List<SuperFusionHistoryData> historyList = historyValueConvert(dayList);
                dataMap.put("ten_min", historyList);
            }
            if (mapInfo != null && mapInfo.get("hour") != null) {
                List<Map> hourList = (List<Map>) mapInfo.get("hour");
                List<SuperFusionHistoryData> historyList = historyValueConvert(hourList);
                dataMap.put("hour", historyList);
            }
            if (mapInfo != null && mapInfo.get("day") != null) {
                List<Map> dayList = (List<Map>) mapInfo.get("day");
                List<SuperFusionHistoryData> historyList = historyValueConvert(dayList);
                dataMap.put("day", historyList);
            }
            if (mapInfo != null && mapInfo.get("month") != null) {
                List<Map> monthList = (List<Map>) mapInfo.get("month");
                List<SuperFusionHistoryData> historyList = historyValueConvert(monthList);
                dataMap.put("month", historyList);
            }
            itemMap.put(item, dataMap);
        }
    }

    private void getPropertiesInfoList(List<SuperFusionTreeParam> superFusionList, List<AddAndUpdateModelInstanceParam> instanceInfoList,
                                       List<SuperFusionInfo> superFusionInfos, Integer relationInstanceId, Map<Integer, List<ModelInfo>> modelAndParentInfoListMap) {
        //循环获取到的设备
        for (SuperFusionTreeParam info : superFusionList) {
            if (info != null) {
                if (com.google.common.base.Strings.isNullOrEmpty(info.getName())) {
                    log.info("没有设备名称的name:" + info);
                    continue;
                }
                Map<String, Object> m = new HashMap(ListMapObjUtils.beanToMap(info));
                String modelIndex = "";
                Integer modelId = 0;
                String modelName = "";
                List<ModelInfo> modelInfos = modelAndParentInfoListMap.get(MatchFusionModelTypeEnum.getModelId(info.getType()));
                if (CollectionUtils.isEmpty(modelInfos)) {
                    continue;
                }
                List<PropertyInfo> propertyInfos = new ArrayList<>();
                for (ModelInfo modelInfo : modelInfos) {
                    if (modelInfo.getModelId().equals(MatchFusionModelTypeEnum.getModelId(info.getType()))) {
                        //获取模型本体信息
                        modelIndex = modelInfo.getModelIndex();
                        modelId = modelInfo.getModelId();
                        modelName = modelInfo.getModelName();
                    }
                    propertyInfos.addAll(modelInfo.getPropertyInfos());
                }

                //通过匹配模型名称，获取对应的模型数据
                log.info("获取PropertyInfos" + propertyInfos);
                List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();
                //虚拟化设备的type和模型的name相同，则该设备加入到模型中

                if (propertyInfos != null && propertyInfos.size() > 0) {
                    for (PropertyInfo propertyInfo : propertyInfos) {
                        AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                        instanceParam.extractFromPropertyInfo(propertyInfo);

//                    TransferUtils.transferBean(p, instanceParam);
                        //获取到的设备字段值 和 es模型中的字段值相同时，将数据同步到模型实例中取
                        instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? String.valueOf(m.get(instanceParam.getPropertiesIndexId())) : null);
                        if (INSTANCE_NAME_KEY.equals(instanceParam.getPropertiesIndexId())) {
                            instanceParam.setPropertiesIndexId(INSTANCE_NAME_KEY);
                            instanceParam.setPropertiesValue(info.getName());
                            instanceParam.setPropertiesType(1);
                        }
                        propertiesParamLists.add(instanceParam);
                    }
                }
                AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                instanceParam.setModelIndex(modelIndex);
                instanceParam.setModelId(modelId);
                instanceParam.setModelName(modelName);
                instanceParam.setInstanceType(DataType.INSTANCE_MANAGE.getName());
                instanceParam.setInstanceName(info.getName());
                instanceParam.setRelationInstanceId(relationInstanceId);
                instanceParam.setInstanceId(info.getModelInstanceId());
                instanceParam.setPropertiesList(propertiesParamLists);
                SuperFusionInfo superFusionInfo = new SuperFusionInfo(instanceParam, info);
                superFusionInfos.add(superFusionInfo);
                if ((!Strings.isNullOrEmpty(instanceParam.getModelIndex()))) {
                    instanceInfoList.add(instanceParam);
                }
            }

        }
    }


    /**
     * 对list数据，取平均值
     *
     * @param longList
     * @return
     */
    public static long getRoundedAverageValue(List<Long> longList) {
        if (longList == null || longList.isEmpty()) {
            return 0l;
        }
        double average = longList.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
        return Math.round(average);
    }

    /**
     * 获取时间间隔list
     *
     * @param startTimeVal 起始时间点，时间戳（毫秒）
     * @param interval     时间间隔单位秒
     * @param size         list长度
     * @return
     */
    public static List<String> generateTimePeriods(long startTimeVal, int interval, int size) {
        String startTime = getFormatDate(startTimeVal);
        List<String> timePeriodList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime currentDateTime = LocalDateTime.parse(startTime, formatter);
        for (int i = 0; i < size; i++) {
            timePeriodList.add(currentDateTime.format(formatter));
            currentDateTime = currentDateTime.plusSeconds(interval);
        }
        return timePeriodList;
    }

    private static String getValueUnits(BigDecimal value, String units) {
        Map<String, String> convertedValue = UnitsUtil.getConvertedValue(value, units);
        String valueStr = convertedValue.get("value");
        String valueUnits = convertedValue.get("units");
        return valueStr + valueUnits;
    }

    private static String getValueRatio(Object value) {
        Double douVal = doubleValueConvert(value) * 100;
        BigDecimal bValue = new BigDecimal(douVal);
        String valRatio = bValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        // 创建DecimalFormat对象，指定保留两位小数
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        // 将double类型的数值格式化为字符串
        String formattedNumber = decimalFormat.format(doubleValueConvert(valRatio));
        return formattedNumber;
    }

    private void getPropertiesInfoListByEditor(List<SuperFusionTreeParam> superFusionAllList, List<AddAndUpdateModelInstanceParam> instanceInfoList,
                                               List<SuperFusionInfo> superFusionInfos, MwQuerySuperFusionParam param, Map<Integer, List<ModelInfo>> modelAndParentInfoListMap) {
        //获取到的数据
        for (SuperFusionTreeParam info : superFusionAllList) {
            if (info != null) {
                if (com.google.common.base.Strings.isNullOrEmpty(info.getName())) {
                    log.info("没有设备名称的name:" + info);
                    continue;
                }
                Map<String, Object> m = new HashMap(ListMapObjUtils.beanToMap(info));
                String modelIndex = "";
                Integer modelId = 0;
                String modelName = "";
                List<ModelInfo> modelInfos = modelAndParentInfoListMap.get(MatchModelTypeEnum.getModelId(info.getType()));
                List<PropertyInfo> propertyInfos = new ArrayList<>();
                if (CollectionUtils.isEmpty(modelInfos)) {
                    continue;
                }
                for (ModelInfo modelInfo : modelInfos) {
                    if (modelInfo.getModelId().equals(MatchModelTypeEnum.getModelId(info.getType()))) {
                        //获取模型本体信息
                        modelIndex = modelInfo.getModelIndex();
                        modelId = modelInfo.getModelId();
                        modelName = modelInfo.getModelName();
                    }
                    propertyInfos.addAll(modelInfo.getPropertyInfos());
                }

                //通过匹配模型名称，获取对应的模型数据
                log.info("获取superFusion的PropertyInfos" + propertyInfos);
                List<AddModelInstancePropertiesParam> propertiesParamLists = new ArrayList<>();
                //虚拟化设备的type和模型的name相同，则该设备加入到模型中

                if (propertyInfos != null && propertyInfos.size() > 0) {
                    for (PropertyInfo propertyInfo : propertyInfos) {
                        AddModelInstancePropertiesParam instanceParam = new AddModelInstancePropertiesParam();
                        instanceParam.extractFromPropertyInfo(propertyInfo);

//                    TransferUtils.transferBean(p, instanceParam);
                        //获取到的虚拟化设备字段值 和 es模型中的字段值相同时，将数据同步到模型实例中取
                        instanceParam.setPropertiesValue(m.get(instanceParam.getPropertiesIndexId()) != null ? String.valueOf(m.get(instanceParam.getPropertiesIndexId())) : null);
                        if (INSTANCE_NAME_KEY.equals(instanceParam.getPropertiesIndexId())) {
                            instanceParam.setPropertiesIndexId(INSTANCE_NAME_KEY);
                            instanceParam.setPropertiesValue(info.getName());
                            instanceParam.setPropertiesType(1);
                        }
                        propertiesParamLists.add(instanceParam);
                    }
                }
                AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
                instanceParam.setModelIndex(info.getModelIndex());
                instanceParam.setModelId(info.getModelId());
                instanceParam.setInstanceType(DataType.INSTANCE_MANAGE.getName());
                instanceParam.setInstanceName(info.getName());
                instanceParam.setInstanceId(info.getModelInstanceId());
                instanceParam.setEsId(info.getModelIndex() + info.getModelInstanceId());
                instanceParam.setRelationInstanceId(param.getModelInstanceId());
                instanceParam.setPropertiesList(propertiesParamLists);
                SuperFusionInfo superFusionInfo = new SuperFusionInfo(instanceParam, info);
                superFusionInfos.add(superFusionInfo);
                if ((!com.google.common.base.Strings.isNullOrEmpty(instanceParam.getModelIndex()))) {
                    instanceInfoList.add(instanceParam);
                }
            }
        }
    }

    private void syncDeleteData(List<SuperFusionTreeParam> deleteLists) throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        List<Integer> delInstanceIds = new ArrayList<>();
        //es删除数据
        for (SuperFusionTreeParam superFusionTreeParam : deleteLists) {
            String modelIndex = superFusionTreeParam.getModelIndex();
            Integer instanceId = superFusionTreeParam.getModelInstanceId();
            String esId = modelIndex + instanceId;
            bulkRequest.add(new DeleteRequest(modelIndex, esId));
            delInstanceIds.add(instanceId);
            if (bulkRequest.estimatedSizeInBytes() > 5 * 1024 * 1024) {//每次删除5Mb的数据
                restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                bulkRequest = new BulkRequest();
            }
            if (delInstanceIds.size() > 800) {
                //删除mysql数据
                mwModelInstanceDao.deleteBatchInstanceById(delInstanceIds);
                delInstanceIds = new ArrayList<>();
            }
        }
        if (bulkRequest.estimatedSizeInBytes() > 0) {
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        }
        if (delInstanceIds.size() > 0) {
            //删除mysql数据
            mwModelInstanceDao.deleteBatchInstanceById(delInstanceIds);
        }
    }

    private void syncAddData(List<AddAndUpdateModelInstanceParam> instanceInfoList, List<SuperFusionInfo> superFusionInfos, MwQuerySuperFusionParam param) throws Exception {
        log.info("获取插入es的数据:" + instanceInfoList);
        if (instanceInfoList != null && instanceInfoList.size() > 0) {
            mwModelInstanceService.saveData(instanceInfoList, true, true);
        }
        if (graphEnable) {
            //保存虚拟化设备拓扑关系
            AddAndUpdateModelInstanceParam instanceParam = new AddAndUpdateModelInstanceParam();
            instanceParam.setInstanceId(param.getModelInstanceId());
            instanceParam.setModelIndex(param.getModelIndex());
            instanceParam.setModelId(param.getModelId());
            SuperFusionTreeParam superFusionTreeParam = new SuperFusionTreeParam();
            superFusionTreeParam.setType("device");
            superFusionTreeParam.setId(param.getModelInstanceId() + "");
            SuperFusionInfo superFusionInfo = new SuperFusionInfo(instanceParam, superFusionTreeParam);
            superFusionInfos.add(superFusionInfo);
            //获取节点和线数据，存入neo4j数据
            mwModelSuperFusionRelationManager.updateSuperFusionInfo(param, superFusionInfos, InstanceNotifyType.VirtualSyncInit);
            log.info("存入neo4j数据成功");
        }
    }

    // 将Base64编码表示的公钥转换为公钥对象
    public static PublicKey getPublicKeyFromBase64(String publicKeyBase64) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        BigInteger exp = new BigInteger("10001", 16);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(publicKeyBase64, 16), exp);
        return keyFactory.generatePublic(spec);
    }

    // 使用公钥进行加密
    public static byte[] encryptRSA(String plaintext, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA"); // 添加算法和填充方式
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
    }

    // 将字节数组转换为十六进制字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }


}
