package cn.mw.monitor.smartdisc.common;

import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
public class NmapXMLUtil {
    public static final String xmlLocation = "D:\\zz";
//    public static final String ip = "10.18.5.33";

    public static List<Map<String, Object>> getResultFromNmap(String ip,StringBuilder command){

        List<Map<String, Object>> portList = new ArrayList<>();
        //nmap扫描
        getReturnData("D:\\ruanjian\\nmap\\nmap.exe", " -oA " + xmlLocation + " " + command);
        try {
        //解析xml为list集合
        File file = new File(xmlLocation + ".xml");
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(file);
        Element rootEl = document.getRootElement();
        if (rootEl != null && rootEl.element("host") !=null ) {
            Element addrEl = rootEl.element("host").element("address");
            if (addrEl != null) {
                //扫描到的ip
                String scanIp = addrEl.attributeValue("addr");
                if (scanIp.equals(ip)) {
//                    ////System.out.println("ip匹配成功，开始解析...");
                            log.info("ip匹配成功，开始解析...");
                    //ip类型
                    String ipType = StringUtils.isEmpty(addrEl.attributeValue("addrtype")) ? "" : addrEl.attributeValue("addrtype") + " ";
                    //获取所有子节点的根节点，xpath
                    List<Node> nodes = rootEl.selectNodes("//ports/port");
                    List<Element> ports = new ArrayList<>();
                    for (Iterator<Node> iter = nodes.iterator(); iter.hasNext(); ) {
                        //Node类型的元素转换为Element类型
                        Element element = (Element) iter.next();
                        ports.add(element);
                    }
                    if (ports.size() > 0) {
                        for (Element el : ports) {
                            Map<String, Object> portMap = new HashMap<>();
                            //协议
                            String agreement = el.attributeValue("protocol");
                            //端口号
                            String port = el.attributeValue("portid");

                            Element stateEl = el.element("state");
                            //状态
                            String state = StringUtils.isEmpty(stateEl.attributeValue("state")) ? "" : stateEl.attributeValue("state") + " ";
                            //reason
                            String reason = StringUtils.isEmpty(stateEl.attributeValue("reason")) ? "" : stateEl.attributeValue("reason") + " ";
                            //reason_ttl
                            String reasonTTL = StringUtils.isEmpty(stateEl.attributeValue("reason_ttl")) ? "" : stateEl.attributeValue("reason_ttl") + " ";
                            String mac = StringUtils.isEmpty(stateEl.attributeValue("mac")) ? "" : stateEl.attributeValue("mac") + " ";

                            Element serviceEl = el.element("service");
                            String servicename = "";
                            String product = "";
                            String ostype = "";
                            String hostname = "";
                            String extrainfo = "";
                            if (serviceEl != null) {
                                //服务名
                                 servicename = StringUtils.isEmpty(serviceEl.attributeValue("name")) ? "" : serviceEl.attributeValue("name") + " ";
                                //product
                                 product = StringUtils.isEmpty(serviceEl.attributeValue("product")) ? "" : serviceEl.attributeValue("product") + " ";
                                //操作系统
                                 ostype = StringUtils.isEmpty(serviceEl.attributeValue("ostype")) ? "" : serviceEl.attributeValue("ostype") + " ";
                                //主机名
                                 hostname = StringUtils.isEmpty(serviceEl.attributeValue("hostname")) ? "" : serviceEl.attributeValue("hostname") + " ";
                                //附加信息
                                 extrainfo = StringUtils.isEmpty(serviceEl.attributeValue("extrainfo")) ? "" : serviceEl.attributeValue("extrainfo") + " ";
                            }
//                            portMap.put("id", UUIDUtil.getUUID());
                            portMap.put("ip", ip);
//                            portMap.put("wcode", "12");
//                            portMap.put("website", "Nmap扫描工具");
                            portMap.put("port", port);
                            portMap.put("agreement", agreement);
                            portMap.put("state", state);
//                            String other = hostname + ostype + state + ipType + servicename + extrainfo + product + reason + reasonTTL;
                            portMap.put("hostName", hostname);
                            portMap.put("osType", ostype);
                            portMap.put("ipType", ipType);
                            portMap.put("serviceName", servicename);
                            portMap.put("extraInfo", extrainfo);
                            portMap.put("product", product);
                            portMap.put("reason", reason);
                            portMap.put("mac", mac);
                            portMap.put("reasonTTL", reasonTTL);
//                            portMap.put("other",other);
                            portList.add(portMap);
                        }
                                log.info("解析数据成功...");
                    }else {
                        Map<String, Object> portMap = new HashMap<>();
                        String reason = StringUtils.isEmpty(addrEl.attributeValue("reason")) ? "" : addrEl.attributeValue("reason") + " ";
                        //reason_ttl
                        String reasonTTL = StringUtils.isEmpty(addrEl.attributeValue("reason_ttl")) ? "" : addrEl.attributeValue("reason_ttl") + " ";
                        String mac = StringUtils.isEmpty(addrEl.attributeValue("mac")) ? "" : addrEl.attributeValue("mac") + " ";
                        portMap.put("ip", ip);
//                            portMap.put("wcode", "12");
//                            portMap.put("website", "Nmap扫描工具");
                        portMap.put("port", "");
                        portMap.put("agreement", "");
                        portMap.put("state", "");
//                            String other = hostname + ostype + state + ipType + servicename + extrainfo + product + reason + reasonTTL;
                        portMap.put("hostName", "");
                        portMap.put("osType", "");
                        portMap.put("ipType", ipType);
                        portMap.put("serviceName", "");
                        portMap.put("extraInfo", "");
                        portMap.put("product", "");
                        portMap.put("reason", reason);
                        portMap.put("mac", mac);
                        portMap.put("reasonTTL", reasonTTL);
                        portList.add(portMap);
                    }
                }
            }
        }else {
            log.info("解析数据为空...");
        }
    }catch (DocumentException e) {
            log.error(e.toString());
    }catch (Exception e) {
            log.error(e.toString()+"-----------------");
            throw e;
        }
        return portList;
    }

    /**
     * 调用nmap进行扫描
     * @param nmapDir nmap路径
     * @param command 执行命令
     *
     * @return 执行回显
     * */
    public static String getReturnData (String nmapDir, String command){
        Process process = null;
        StringBuilder stringBuffer = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(nmapDir + " " + command);
//            ////System.out.println("请稍等...");
            log.info("正在进行NMAP探测,请稍等...");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuffer.append(line).append("\n");
            }
        } catch (IOException e) {
            log.error(e.toString());
        }
        return stringBuffer.toString();
    }
}
