package cn.mw.monitor.service.impl;

import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author xhy
 * @date 2020/7/20 14:56
 */
@Component
@Slf4j
public class MWLinkZabbixManger {

    @Autowired
    private MWTPServerAPI mwtpServerAPI;


    public Map<String, Object> getItemValue(NetWorkLinkDto netWorkLinkDto) {
        log.info("netWorkLinkDto{}", netWorkLinkDto);
        String linkId = netWorkLinkDto.getLinkId();
        String scanType = "";
        String valuePort = netWorkLinkDto.getValuePort();
        String bandHostid = "";
        String stateHostid = "";
        Integer bandServerId = 0;
        Integer stateServerId = 0;
        String port = "";
        String linkTargetIp = "";
        if (valuePort.equals("ROOT")) {
            bandHostid = netWorkLinkDto.getRootAssetsParam().getAssetsId();
            bandServerId = netWorkLinkDto.getRootAssetsParam().getMonitorServerId();
            port = netWorkLinkDto.getRootPort();
        } else {
            bandHostid = netWorkLinkDto.getTargetAssetsParam().getAssetsId();
            bandServerId = netWorkLinkDto.getTargetAssetsParam().getMonitorServerId();
            port = netWorkLinkDto.getTargetPort();
        }
        List<String> bandNameList = new ArrayList<>();
        bandNameList.add("[" + port + "]" + "INTERFACE_BANDWIDTH");// 上行/下行带宽使用一个值
        bandNameList.add("[" + port + "]" + "MW_INTERFACE_STATUS");
        bandNameList.add("[" + port + "]" + "INTERFACE_IN_UTILIZATION"); // in/out带宽利用率
        bandNameList.add("[" + port + "]" + "INTERFACE_OUT_UTILIZATION");

        log.info("bandNameList{}", bandNameList);
        log.info("bandHostid{}", bandHostid);

        Map<String, Object> map = new HashMap<>();

        if (bandServerId != 0 && StringUtils.isNotEmpty(port) && StringUtils.isNotEmpty(bandHostid)) {
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(bandServerId, bandNameList, bandHostid);
            if (result.getCode() == 0) {
                JsonNode jsonNode = (JsonNode) result.getData();
                if (jsonNode.size() > 0) {
                    for (JsonNode node : jsonNode) {
                        String name = node.get("name").asText();
                        String units = node.get("units").asText();
                        name = name.substring(name.indexOf("]") + 1, name.length());
                        String lastValue = node.get("lastvalue").asText();
                        switch (name) {
                            case "INTERFACE_BANDWIDTH":
                                String valueWithUnits = UnitsUtil.getValueWithUnits(lastValue, units);

                                map.put("UP_INTERFACE_BANDWIDTH", valueWithUnits);
                                map.put("DOWN_INTERFACE_BANDWIDTH", valueWithUnits);
                                break;
                            case "MW_INTERFACE_STATUS":
                                map.put("MW_INTERFACE_STATUS", "1".equals(lastValue) ? "正常" : "异常");
                                break;
                            case "INTERFACE_IN_UTILIZATION":
                                map.put("IN_BANDWIDTH_UTILIZATION", (lastValue != null && StringUtils.isNotEmpty(lastValue)) ? Double.valueOf(lastValue) : 0.0);
                                break;
                            case "INTERFACE_OUT_UTILIZATION":
                                map.put("OUT_BANDWIDTH_UTILIZATION", (lastValue != null && StringUtils.isNotEmpty(lastValue)) ? Double.valueOf(lastValue) : 0.0);
                                break;
                            default:
                                break;

                        }
                    }
                }
            } else {
                log.info("线路查询zabbbix带宽利用率失败{}", result.getData().toString());
                throw new RuntimeException("线路查询zabbbix带宽利用率失败");
            }
        }
        if (netWorkLinkDto.getEnable().equals("ACTIVE")) {
            scanType = netWorkLinkDto.getScanType();
            if (null != scanType && StringUtils.isNotEmpty(scanType)) {
                if (scanType.equals("ICMP")) {//ICMP
                    stateHostid = netWorkLinkDto.getTargetAssetsParam().getAssetsId();
                    stateServerId = netWorkLinkDto.getTargetAssetsParam().getMonitorServerId();
                    List<String> stateNameList = new ArrayList<>();
                    stateNameList.add("ICMP_PING");
                    stateNameList.add("ICMP_LOSS");
                    stateNameList.add("ICMP_RESPONSE_TIME");
                    MWZabbixAPIResult stateResult = mwtpServerAPI.itemGetbyFilter(stateServerId, stateNameList, stateHostid);
                    if (stateResult.getCode() == 0) {
                        JsonNode jsonNode = (JsonNode) stateResult.getData();
                        if (jsonNode.size() > 0) {
                            for (JsonNode node : jsonNode) {
                                String name = node.get("name").asText();
                                String lastValue = node.get("lastvalue").asText();
                                switch (name) {
                                    case "ICMP_PING":
                                        if (lastValue.equals("1")) {
                                            map.put("ICMP_PING", "正常");
                                        } else {
                                            map.put("ICMP_PING", "异常");
                                        }
                                        break;
                                    case "ICMP_LOSS":
                                        map.put("ICMP_LOSS", lastValue + node.get("units").asText());
                                        break;
                                    case "ICMP_RESPONSE_TIME":
                                        String units = node.get("units").asText();
                                        String time = UnitsUtil.getValueWithUnits(lastValue, units);
                                        map.put("ICMP_RESPONSE_TIME", time);
                                        break;
                                }
                            }
                        }
                    }
                } else if (scanType.equals("NQA")) {//NQA NQA 要使用目标ip 拼接ItemName
                    stateHostid = netWorkLinkDto.getRootAssetsParam().getAssetsId();
                    stateServerId = netWorkLinkDto.getRootAssetsParam().getMonitorServerId();
                    linkTargetIp = netWorkLinkDto.getLinkTargetIp();
                    log.info("nqa_linkTargetIp{}", linkTargetIp);
                    List<String> stateNameList = new ArrayList<>();
                    stateNameList.add("[" + linkTargetIp + "]" + "PING_AVGRTT");
//                    stateNameList.add("[" + linkTargetIp + "]" + "NQA_ENABLED");
                    stateNameList.add("[" + linkTargetIp + "]" + "NQA_SENT");
                    stateNameList.add("[" + linkTargetIp + "]" + "NQA_SUCESS");
                    MWZabbixAPIResult stateResult = mwtpServerAPI.itemGetbyFilter(stateServerId, stateNameList, stateHostid);
                    if (stateResult.getCode() == 0) {
                        JsonNode jsonNode = (JsonNode) stateResult.getData();
                        log.info("nqa_jsonNode{}", jsonNode);
                        if (jsonNode.size() > 0) {
                            for (JsonNode node : jsonNode) {
                                String name = node.get("name").asText();
                                name = name.substring(name.indexOf("]"), name.length());
                                String lastValue = node.get("lastvalue").asText();
                                switch (name) {
                                    case "NQA_SENT":
                                        map.put("NQA_SENT", Integer.valueOf(lastValue));
                                        break;
                                    case "NQA_SUCESS":
                                        if (lastValue.equals("0")) {
                                            map.put("ICMP_PING", "异常");
                                        } else {
                                            map.put("ICMP_PING", "正常");
                                        }
                                        map.put("NQA_SUCESS", Integer.valueOf(lastValue));
                                        break;
                                    case "PING_AVGRTT":
                                        String time = UnitsUtil.getValueWithUnits(lastValue, "s");
                                        map.put("ICMP_RESPONSE_TIME", time);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            Object sent = map.get("NQA_SENT");
                            Object success = map.get("NQA_SUCESS");
                            if (null != sent && null != success) {
                                Double nqaSent = Double.valueOf(sent.toString());
                                Double nqaSuccess = Double.valueOf(success.toString());
                                log.info("nqaSuccess{},nqaSent{}", nqaSuccess, nqaSent);
                                if (nqaSuccess == 0 || nqaSent == 0) {
                                    map.put("ICMP_LOSS", "100%");
                                } else {
                                    String loss = String.valueOf((nqaSuccess / nqaSent) * 100);
                                    BigDecimal disk = new BigDecimal(loss);
                                    map.put("ICMP_LOSS", disk.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                                }
                            } else {
                                map.put("ICMP_LOSS", "100%");
                            }
                        }
                    }
                } else if (scanType.equals("IPSLA")) {//IPSLA 要使用目标ip 拼接ItemName
                    stateHostid = netWorkLinkDto.getRootAssetsParam().getAssetsId();
                    stateServerId = netWorkLinkDto.getRootAssetsParam().getMonitorServerId();
                    linkTargetIp = netWorkLinkDto.getLinkTargetIp();
                    log.info("nqa_linkTargetIp{}", linkTargetIp);
                    List<String> stateNameList = new ArrayList<>();
                    stateNameList.add("[" + linkTargetIp + "]" + "IPSLA_STATUS");
//                    stateNameList.add("[" + linkTargetIp + "]" + "IPSLA_ENABLED");
                    stateNameList.add("[" + linkTargetIp + "]" + "IPSLA_SENT");
                    stateNameList.add("[" + linkTargetIp + "]" + "IPSLA_SUCESS");
                    MWZabbixAPIResult stateResult = mwtpServerAPI.itemGetbyFilter(stateServerId, stateNameList, stateHostid);
                    if (stateResult.getCode() == 0) {
                        JsonNode jsonNode = (JsonNode) stateResult.getData();
                        log.info("ipsla_jsonNode{}", jsonNode);
                        if (jsonNode.size() > 0) {
                            for (JsonNode node : jsonNode) {
                                String name = node.get("name").asText();
                                name = name.substring(name.indexOf("]"));
                                String lastValue = node.get("lastvalue").asText();
                                switch (name) {
                                    case "IPSLA_SENT":
                                        map.put("NQA_SENT", Integer.valueOf(lastValue));
                                        break;
                                    case "IPSLA_SUCESS":
                                        if (lastValue.equals("0")) {
                                            map.put("ICMP_PING", "异常");
                                        } else {
                                            map.put("ICMP_PING", "正常");
                                        }
                                        map.put("NQA_SUCESS", Integer.valueOf(lastValue));
                                        break;
                                    case "IPSLA_STATUS":
                                        String time = UnitsUtil.getValueWithUnits(lastValue, "s");
                                        map.put("ICMP_RESPONSE_TIME", time);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            Object sent = map.get("NQA_SENT");
                            Object success = map.get("NQA_SUCESS");
                            if (null != sent && null != success) {
                                Double nqaSent = Double.valueOf(sent.toString());
                                Double nqaSuccess = Double.valueOf(success.toString());
                                log.info("nqaSuccess{},nqaSent{}", nqaSuccess, nqaSent);
                                if (nqaSuccess == 0 || nqaSent == 0) {
                                    map.put("ICMP_LOSS", "100%");
                                } else {
                                    String loss = String.valueOf((nqaSuccess / nqaSent) * 100);
                                    BigDecimal disk = new BigDecimal(loss);
                                    map.put("ICMP_LOSS", disk.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
                                }
                            } else {
                                map.put("ICMP_LOSS", "100%");
                            }
                        }
                    }
                }
            }
        }
//
//        Double upBandwidth = Double.valueOf(netWorkLinkDto.getDownLinkBandwidth());
//        Double downBandwidth = Double.valueOf(netWorkLinkDto.getUpLinkBandwidth());
//
//        Double inTraffic = new BigDecimal(null == map.get("INTERFACE_OUT_TOTALTRAFFIC") ? "0" : String.valueOf(map.get("INTERFACE_OUT_TOTALTRAFFIC"))).doubleValue();
//        Double outTraffic = new BigDecimal(null == map.get("INTERFACE_IN_TOTALTRAFFIC") ? "0" : String.valueOf(map.get("INTERFACE_IN_TOTALTRAFFIC"))).doubleValue();
//        log.info("INTERFACE_OUT_TOTALTRAFFIC{}", inTraffic);
//        log.info("INTERFACE_IN_TOTALTRAFFIC{}", outTraffic);
//        if (upBandwidth != 0 && inTraffic != 0) {
//            String units = map.get("IN_UNITS").toString();
//            String bandUnit = netWorkLinkDto.getBandUnit();
//            Map<String, String> valueMap = UnitsUtil.getValueMap(String.valueOf(inTraffic), bandUnit, units);
//            String value = valueMap.get("value");
//            inTraffic = Double.valueOf(value);
//            Double inBandWidthUtilization = inTraffic / (upBandwidth == 0 ? inTraffic : upBandwidth) * 100;
//            BigDecimal inDisk = new BigDecimal(inBandWidthUtilization.toString());
//            inDisk = inDisk.setScale(2, BigDecimal.ROUND_HALF_UP);//四舍五入保留两位小数
//            map.put("IN_BANDWIDTH_UTILIZATION", inDisk.doubleValue());
//        } else {
//            map.put("IN_BANDWIDTH_UTILIZATION", 0);
//        }
//        if (downBandwidth != 0 && outTraffic != 0) {
//            String units = map.get("OUT_UNITS").toString();
//            String bandUnit = netWorkLinkDto.getBandUnit();
//            Map<String, String> valueMap = UnitsUtil.getValueMap(String.valueOf(outTraffic), bandUnit, units);
//            String value = valueMap.get("value");
//            outTraffic = Double.valueOf(value);
//            Double outBandWidthUtilization = outTraffic / (downBandwidth == 0 ? outTraffic : downBandwidth) * 100;
//            BigDecimal outDisk = new BigDecimal(outBandWidthUtilization.toString());
//            outDisk = outDisk.setScale(2, BigDecimal.ROUND_HALF_UP);//四舍五入保留两位小数
//            map.put("OUT_BANDWIDTH_UTILIZATION", outDisk.doubleValue());
//
//        } else {
//            map.put("OUT_BANDWIDTH_UTILIZATION", 0);
//        }

        return map;
    }


    public Map<String, Map<String, Object>> getLinkValue(Integer serverId, List<String> hostIds, List<String> itemNames) {
        long time1 = System.currentTimeMillis();
        Map<String, Map<String, Object>> linkValue = new HashMap<>();
        MWZabbixAPIResult result = mwtpServerAPI.itemGetbyFilter(serverId, itemNames, hostIds);
        long time2 = System.currentTimeMillis();
//        ////System.out.println("测试调用api时间：" + (time2 - time1));
        Map<String, Object> map = new HashMap<>();
        if (result.getCode() == 0) {
            JsonNode jsonNode = (JsonNode) result.getData();
            if (jsonNode.size() > 0) {
                for (JsonNode node : jsonNode) {
                    String typePort = "";
                    String name = node.get("name").asText();
                    String units = node.get("units").asText();
                    if (name.indexOf("]") != -1) {
                        typePort = name.substring(1,name.indexOf("]"));
                        name = name.substring(name.indexOf("]") + 1);
                    }
                    String lastValue = node.get("lastvalue").asText();
                    String hostId = node.get("hostid").asText();
                    if (linkValue.containsKey(hostId + "_" + serverId)) {
                        map = linkValue.get(hostId + "_" + serverId);
                    } else {
                        map = new HashMap<>();
                    }

                    switch (name) {
                        case "INTERFACE_BANDWIDTH":
                            String valueWithUnits = UnitsUtil.getValueWithUnits(lastValue, units);
                            map.put(typePort + "UP_INTERFACE_BANDWIDTH", valueWithUnits);
                            map.put(typePort + "DOWN_INTERFACE_BANDWIDTH", valueWithUnits);
                            break;
//                        case "MW_INTERFACE_STATUS":
//                            map.put(typePort + "MW_INTERFACE_STATUS", "1".equals(lastValue) ? "正常" : "异常");
//                            break;
                        case "MW_INTERFACE_IN_TRAFFIC":
                            map.put(typePort + "MW_INTERFACE_IN_TRAFFIC", (lastValue != null && StringUtils.isNotEmpty(lastValue)) ? Double.valueOf(lastValue)+"_"+units : 0.0+"_"+units);
                            break;
                        case "MW_INTERFACE_OUT_TRAFFIC":
                            map.put(typePort + "MW_INTERFACE_OUT_TRAFFIC", (lastValue != null && StringUtils.isNotEmpty(lastValue)) ? Double.valueOf(lastValue)+"_"+units : 0.0+"_"+units);
                            break;
//                        case "IPSLA_STATUS":
//                        case "ICMP_PING":
//                            if (lastValue.equals("1")) {
//                                map.put("ICMP_PING", "正常");
//                            } else {
//                                map.put("ICMP_PING", "异常");
//                            }
//                            break;
//                        case "ICMP_LOSS":
//                            BigDecimal loss = new BigDecimal(lastValue);
//                            map.put("ICMP_LOSS", loss.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + node.get("units").asText());
//                            break;
//                        case "PING_AVGRTT":
//                        case "ICMP_RESPONSE_TIME":
//                            String time = UnitsUtil.getValueWithUnits(lastValue, units);
//                            map.put("ICMP_RESPONSE_TIME", time);
//                            break;
//                        case "NQA_SENT":
//                        case "IPSLA_SENT":
//                            map.put("NQA_SENT", Integer.valueOf(lastValue));
//                            break;
//                        case "IPSLA_SUCESS":
//                        case "NQA_SUCESS":
//                            if (lastValue.equals("0")) {
//                                map.put("ICMP_PING", "异常");
//                            } else {
//                                map.put("ICMP_PING", "正常");
//                            }
//                            map.put("NQA_SUCESS", Integer.valueOf(lastValue));
//                            break;
                        default:
                            break;
                    }
//                    Object sent = map.get("NQA_SENT");
//                    Object success = map.get("NQA_SUCESS");
//                    if (null != sent && null != success) {
//                        Double nqaSent = Double.valueOf(sent.toString());
//                        Double nqaSuccess = Double.valueOf(success.toString());
//                        if (nqaSuccess == 0 || nqaSent == 0) {
//                            map.put(typePort + "ICMP_LOSS", "100%");
//                        } else {
//                            String loss = String.valueOf((nqaSuccess / nqaSent) * 100);
//                            BigDecimal disk = new BigDecimal(loss);
//                            map.put(typePort + "ICMP_LOSS", disk.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
//                        }
//                    }
//                    else {
//                        map.put("ICMP_LOSS", "100%");
//                    }
                    linkValue.put(hostId + "_" + serverId, map);
                }
            }
        }
        long time3 = System.currentTimeMillis();
//        ////System.out.println("测试处理数据时间：" + (time3 - time2));
        return linkValue;
    }


    /**
     * 添加链路到zabbix中监控
     *
     * @param
     * @return
     */
    public Boolean hostMassUpdate(Integer monitorServerId, String hostId, String scanType) {
        log.info("MWLinkZabbixManger{} hostMassUpdate() monitorServerId::"+monitorServerId+"::hostId:"+hostId+":::scanType:"+scanType);
        String templateid = null;
        if (scanType.equals("ICMP")) {
            templateid = templateGet(monitorServerId, "MW_ICMP_通用模版[ICMP][子模版]");
        } else if (scanType.equals("NQA")) {
            templateid = templateGet(monitorServerId, "MW_NQA链路探测模版[SNMP][子模版]");
        } else if (scanType.equals("IPSLA")) {
            templateid = templateGet(monitorServerId, "MW_IPSLA链路探测模版[SNMP][子模版]");
        }
        log.info("MWLinkZabbixManger{} hostMassUpdate() templateid::"+templateid);
        if (null != templateid) {
            //先要判断这个资产有没有关联这个模板或者是否连接上这个子模板   1查询主机所有模板

            MWZabbixAPIResult result0 = mwtpServerAPI.hostgetByTempalteid(monitorServerId, hostId, templateid);
            log.info("MWLinkZabbixManger{} hostMassUpdate() result0::"+result0);
            if (result0.getCode() == 0) {
                JsonNode data = (JsonNode) result0.getData();
                //需要链接的模板已经存在于主机上
                if ((data.toString().endsWith("inherited from another template")) || (data.size() == 1 && data.get(0).toString().equals(hostId))) {
                    return true;
                }
                //添加线路
                MWZabbixAPIResult result = mwtpServerAPI.hostMassUpdate(monitorServerId, hostId, templateid);
                log.info("MWLinkZabbixManger{} hostMassUpdate() result::"+result);
                if (result.getCode() == 0 || (result.getData().toString().endsWith("inherited from another template."))) {
                    return true;
                }
            }
        }
        log.info("判断这个资产有没有关联这个模板查询失败{}");
        return false;
    }

    /**
     * 去掉链路关联的模板
     *
     * @param
     * @return
     */
    public Boolean hostMassRemove(Integer monitorServerId, String hostId, String scanType) {
        String templateid = null;
        if (scanType.equals("ICMP")) {
            templateid = templateGet(monitorServerId, "MW_ICMP_通用模版[ICMP][子模版]");
        } else if (scanType.equals("NQA")) {
            templateid = templateGet(monitorServerId, "MW_NQA链路探测模版[SNMP][子模版]");
        } else if (scanType.equals("IPSLA")) {
            templateid = templateGet(monitorServerId, "MW_IPSLA链路探测模版[SNMP][子模版]");
        }
        if (null != templateid) {
            MWZabbixAPIResult result = mwtpServerAPI.hostMassRemove(monitorServerId, hostId, templateid);
            if (result.getCode() == 0) {
                return true;
            }
        }
        return false;
    }


    private String templateGet(Integer monitorServerId, String name) {
        MWZabbixAPIResult result = mwtpServerAPI.templateGet(monitorServerId, name, true);
        if (result.getCode() == 0) {
            JsonNode jsonNode = (JsonNode) result.getData();
            String templateid = jsonNode.get(0).get("templateid").asText();
            return templateid;
        }
        return null;
    }

    /**
     * ip地址itemName TARGET_IPADDR
     * 端口itemName INTERFACE_NAME  端口状态
     * 带宽 INTERFACE_BANDWIDTH
     *
     * @param itemName
     * @param hostid
     * @return
     */
    public List<Object> getIpAddressList(Integer monitorServerId, String itemName, String hostid) {
        boolean portFlag = false;
        MWZabbixAPIResult result = null;
        if ("INTERFACE_NAME".equals(itemName)) {
            portFlag = true;
            itemName = "MW_INTERFACE_STATUS";
        }
        if ("INTERFACE_IPADDR".equals(itemName)) {
            result = mwtpServerAPI.itemGetbySearchNames(monitorServerId, "IPADDRESS", itemName, hostid);
        } else {
            result = mwtpServerAPI.itemGetbyType(monitorServerId, itemName, hostid, false);
        }
        List<Object> ipList = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        if (result.getCode() == 0) {
            JsonNode jsonNode = (JsonNode) result.getData();
            if (jsonNode.size() > 0) {
                if (!portFlag) {
                    for (JsonNode data : jsonNode) {
                        String lastvalue = "";
                        if ("NQA_SENT".equals(itemName) || "IPSLA_STATUS".equals(itemName)) {
                            lastvalue = data.get("name").asText().substring(1, data.get("name").asText().indexOf("]"));
                        } else {
                            lastvalue = data.get("lastvalue").asText();
                        }
                        if (StringUtils.isNotEmpty(lastvalue)) {
                            ipList.add(lastvalue);
                        }
                    }
                } else {
                    jsonNode.forEach(data -> {
                        String lastvalue = data.get("lastvalue").asText();
                        String name = data.get("name").asText();
                        name = name.substring(name.indexOf("[") + 1, name.indexOf("]"));
                        if (name != null && StringUtils.isNotEmpty(name)) {
                            map.put("name", name);
                            map.put("status", "1".equals(lastvalue) ? "up" : "down");
                            Object o = JSON.parseObject(JSON.toJSONString(map), Object.class);
                            ipList.add(o);
                        }
                    });
                }
            }
        }
        return ipList;
    }

    public Map<String, Object> getInterfaceBandwidth(int monitorServerId, String hostId, String port) {
        Map<String, Object> map = new HashMap<>();
        if (monitorServerId != 0 && StringUtils.isNotEmpty(port) && StringUtils.isNotEmpty(hostId)) {
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(monitorServerId, "[" + port + "]" + "INTERFACE_BANDWIDTH", hostId, true);
            if (result.getCode() == 0) {
                JsonNode jsonNode = (JsonNode) result.getData();
                if (jsonNode.size() > 0) {
                    JsonNode node = jsonNode.get(0);
                    String units = node.get("units").asText();
                    Double lastValue = node.get("lastvalue").asDouble();
                    map.put("UP_INTERFACE_BANDWIDTH", lastValue);
                    map.put("DOWN_INTERFACE_BANDWIDTH", lastValue);
                    map.put("units", units);
                }
            }
        } else {
            log.info("线路查询上下行带宽失败 monitor ServerId：{}，hostId:{},port：{}", monitorServerId, hostId, port);
        }
        return map;
    }


}
