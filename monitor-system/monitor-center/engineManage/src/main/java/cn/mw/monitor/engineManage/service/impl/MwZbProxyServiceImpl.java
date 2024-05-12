package cn.mw.monitor.engineManage.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.engineManage.dto.ProxyDTO;
import cn.mw.monitor.engineManage.service.MwZbProxyService;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mw.zbx.MWZabbixApi;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by syt on 2020/4/27.
 */
@Service
@Slf4j
@Transactional
public class MwZbProxyServiceImpl implements MwZbProxyService {
    private static final Logger logger = LoggerFactory.getLogger(MwZbProxyService.class);
//    @Autowired
    private MWZabbixApi mwZabbixApi;
//    @Autowired
//    private ZbxMyHostService zbxMyHostService;
    @Override
    public List<ProxyDTO> getProxyList(ProxyDTO dto) {
//        String serverIp,String port,String dns
        List<ProxyDTO> list = new ArrayList<>();
        try{
            MWZabbixAPIResult mwZabbixAPIResult = mwZabbixApi.proxyGetByServerIp(dto.getIp(), dto.getPort(), dto.getDns());
            JsonNode proxy_data = (JsonNode) mwZabbixAPIResult.getData();
            if(proxy_data.size()>0){
                proxy_data.forEach(bean->{
                    ProxyDTO dtop = new ProxyDTO();
                    dtop.setProxyid(bean.get("proxyid").asText());
                    dtop.setHost(bean.get("host").asText());
//                    dtop.setDescription(bean.get("description").asText());
                    dtop.setStatus(bean.get("status").asText());
//                    dtop.setLastaccess(bean.get("lastaccess").asLong());
                    if(null != bean.get("interface") && bean.get("interface").size()>0){
                        dtop.setIp(bean.get("interface").get("ip").asText());
                        dtop.setDns(bean.get("interface").get("dns").asText());
                        dtop.setPort(bean.get("interface").get("port").asText());
                        dtop.setIp(bean.get("interface").get("ip").asText());
                        dtop.setUseip(bean.get("interface").get("useip").asText());
                        dtop.setHostid(bean.get("interface").get("hostid").asText());
                    }
                    list.add(dtop);
                });
            }
            return list;
        }catch (Exception e){
            logger.error("fail to getProxyList param{}, case by {}",dto, e);
            return list;
        }
    }

    @Override
    public Reply addProxy(ProxyDTO dto) {
//        String engineName,String serverIp,String status,String port,String dns
        try{
            MWZabbixAPIResult proxy = mwZabbixApi.createProxy(dto.getHost(), dto.getIp(), dto.getStatus(), dto.getPort(), dto.getDns());
            logger.info("ACCESS_LOG[][]ProxysService-addProxy[][]创建引擎[].");
            if(!proxy.isFail()){
                JsonNode proxy_data = (JsonNode) proxy.getData();
                String proxyids = proxy_data.get("proxyids").get(0).asText();
                return Reply.ok(proxyids);
            }else{
                return Reply.fail(1,"创建引擎失败");
            }
        }catch (Exception e) {
            logger.info("ERROR_LOG[][]ProxysService-addProxy[][]创建引擎失败[].");
            return Reply.fail(1,"创建引擎失败");
        }
    }
    @Override
    public Reply delProxy(List<String> proxyIds) {
        try{
            MWZabbixAPIResult mwZabbixAPIResult = mwZabbixApi.proxyDelete(proxyIds);
            logger.info("ACCESS_LOG[][]ProxysService-delProxy[][]删除轮询引擎[].");
            if(!mwZabbixAPIResult.isFail()){
//                zbxMyHostService.addLogs("删除轮询引擎",proxyid);
                return Reply.ok();
            }else{
                return Reply.fail(1,"删除轮询引擎失败");
            }
        }catch (Exception e) {
            logger.info("ERROR_LOG[][]ProxysService-delProxy[][]删除轮询引擎失败[]proxyid:[]{}.", proxyIds.toString());
            return Reply.fail(1,"删除轮询引擎失败");
        }
    }

    @Override
    public Reply editProxy(ProxyDTO dto) {
//        String proxyId,String engineName,String serverIp,String status,String port,String dns
        try{
            MWZabbixAPIResult mwZabbixAPIResult = mwZabbixApi.updateProxy(dto.getProxyid(), dto.getHost(), dto.getIp(), dto.getStatus(), dto.getPort(), dto.getDns());
            logger.info("ACCESS_LOG[][]ProxysService-editProxy[][]修改轮询引擎[].");
            if(!mwZabbixAPIResult.isFail()){
//                zbxMyHostService.addLogs("修改轮询引擎",dto.getHost());
                JsonNode proxy_data = (JsonNode) mwZabbixAPIResult.getData();
                String proxyids = proxy_data.get("proxyids").get(0).asText();
                return Reply.ok(proxyids);
            }else{
                return Reply.fail(1,"修改轮询引擎失败");
            }
        }catch (Exception e) {
            logger.info("ERROR_LOG[][]ProxysService-editProxy[][]修改轮询引擎失败[]proxyid:[]{}.",dto.getProxyid());
            return Reply.fail(1,"修改轮询引擎失败");
        }
    }

}
