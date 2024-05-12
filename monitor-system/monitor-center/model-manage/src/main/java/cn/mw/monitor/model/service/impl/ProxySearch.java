package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.dto.ModelInstanceDto;
import cn.mw.monitor.scan.pattern.RpcRequest;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.scan.model.ProxyInfo;
import cn.mw.monitor.snmp.service.scan.ProxyManager;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProxySearch {

    @Resource
    private MwModelManageDao mwModelManageDao;

    @Value("${model.virtual.proxyTimeout}")
    private int proxyTimeout;

    @Autowired
    private ProxyManager proxyManager;

    @Autowired
    private MwEngineCommonsService mwEngineCommonsService;

    public <T> T doProxySearch(Class<T> ret, List<ProxyInfo> proxyInfos, String engineId, String beanName, String mehtodName, Object param, Type type) {
        T result = null;
        List<ProxyInfo> infos = mwEngineCommonsService.genProxyInfoById(engineId);
        if (null != infos && infos.size() > 0) {
            proxyInfos.addAll(infos);
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setBeanName(beanName);
            rpcRequest.setMethodName(mehtodName);
            String[] parameters = new String[1];
            parameters[0] = JSONObject.toJSONString(param);
            rpcRequest.setParameters(parameters);
            if (ret == List.class) {
                result = (T) proxyManager.proxyScanList(proxyInfos, proxyTimeout, rpcRequest, type, null);
            } else if (ret == Map.class) {
                result = (T) proxyManager.proxyScanMap(proxyInfos, proxyTimeout, rpcRequest, type, null);
            } else {
                result = proxyManager.proxyScan(proxyInfos, proxyTimeout, rpcRequest, ret, null);
            }
        }
        return result;
    }

    public  <T> T doProxySearch(Class<T> ret, List<ProxyInfo> proxyInfos
            , Integer instanceId, String beanName, String mehtodName, Object param, Type type) {
        T result = null;
        Map criteria = new HashMap();
        criteria.put("modelInstanceId", instanceId);
        List<ModelInstanceDto> modelInstanceDtos = mwModelManageDao.selectModelInstance(criteria);
        if (null != modelInstanceDtos && modelInstanceDtos.size() > 0) {
            ModelInstanceDto modelInstanceDto = modelInstanceDtos.get(0);
            if (StringUtils.isNotEmpty(modelInstanceDto.getProxyId()) && !MwEngineCommonsService.LOCALHOST_KEY.equals(modelInstanceDto.getProxyId())) {
                List<ProxyInfo> infos = mwEngineCommonsService.genProxyInfoById(modelInstanceDto.getProxyId());
                if (null != infos && infos.size() > 0) {
                    proxyInfos.addAll(infos);
                    RpcRequest rpcRequest = new RpcRequest();
                    rpcRequest.setBeanName(beanName);
                    rpcRequest.setMethodName(mehtodName);
                    String[] parameters = new String[1];
                    parameters[0] = JSONObject.toJSONString(param);
                    rpcRequest.setParameters(parameters);
                    if (ret == List.class) {
                        result = (T) proxyManager.proxyScanList(proxyInfos, proxyTimeout, rpcRequest, type, null);
                    } else if (ret == Map.class) {
                        result = (T) proxyManager.proxyScanMap(proxyInfos, proxyTimeout, rpcRequest, type, null);
                    } else {
                        result = proxyManager.proxyScan(proxyInfos, proxyTimeout, rpcRequest, ret, null);
                    }
                }
            }
        }
        return result;
    }
}
