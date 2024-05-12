package cn.mw.monitor.prometheus.utils;

import cn.mw.monitor.TPServer.dao.MwTPServerTableDao;
import cn.mw.monitor.TPServer.dto.MwTPServerDTO;
import cn.mw.monitor.TPServer.dto.QueryTPServerParam;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.prometheus.service.impl.PrometheusApiConnectorImpl;
import cn.mwpaas.common.utils.CollectionUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrometheusApiConnectorFactory {
    private static Map<Integer, PrometheusApiConnectorImpl> serverMap=new HashMap<>();

    private static void initData() throws Exception {
        QueryTPServerParam qsParam = new QueryTPServerParam();
        qsParam.setMonitoringServerType("Prometheus");
        Map pubCriteria = PropertyUtils.describe(qsParam);
        MwTPServerTableDao mwTPServerTableDao= SpringUtils.getBean(MwTPServerTableDao.class);
        List<MwTPServerDTO> mwTPServers = mwTPServerTableDao.selectPubList(pubCriteria);
        if (CollectionUtils.isNotEmpty(mwTPServers)){
            PrometheusApiConnectorImpl prometheusApiConnector;
            synchronized(serverMap){
                for (MwTPServerDTO mwTPServerDTO : mwTPServers) {
                    serverMap.put(mwTPServerDTO.getId(),new PrometheusApiConnectorImpl(mwTPServerDTO));
                }
            }
        }
    }

    public static PrometheusApiConnectorImpl createConnector(Integer serviceId) throws Exception {
        if (serverMap.containsKey(serviceId)){
            return serverMap.get(serviceId);
        }
        initData();
        return serverMap.get(serviceId);
    }
}
