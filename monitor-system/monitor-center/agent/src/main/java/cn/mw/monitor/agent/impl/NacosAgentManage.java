package cn.mw.monitor.agent.impl;

import cn.mw.monitor.agent.api.AgentManage;
import cn.mw.monitor.agent.api.FilterRuleChange;
import cn.mw.monitor.agent.model.AgentConfigModel;
import cn.mw.monitor.agent.model.AgentView;
import cn.mw.monitor.agent.model.NetflowAgent;
import cn.mw.monitor.agent.param.NetFlowConfigParam;
import cn.mw.monitor.agent.param.NetflowAgentConfigParam;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@Data
public class NacosAgentManage implements AgentManage, InitializingBean {

    private static final String ServiceName = "AgentService";

    @Value("${agent.enabled}")
    private boolean enabled;

    @Value("${agent.nacos.serverAddr}")
    private String nacosServerAddr;

    @Value("${agent.nacos.user}")
    private String nacosUser;

    @Value("${agent.nacos.passwd}")
    private String nacosPasswd;

    @Value("${agent.nacos.dataId}")
    private String dataId;

    @Value("${agent.group}")
    private String group;

    @Value("${agent.nacos.configGetTimeout}")
    private long configTimeout;

    @Value("${agent.nacosAllInstanceClient.timeout}")
    private long nacosClientTimeout;

    @Value("${agent.encrypt.enable}")
    private boolean encryptEnable;

    private ConfigService configService;

    private NamingService namingService;

    @Override
    public boolean updateFilterRule(FilterRuleChange filterRuleChange) throws Exception{
        String content = configService.getConfig(dataId ,group, 5000);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        Yaml yaml = new Yaml(options);
        AgentConfigModel agentConfigModel = null;
        if(StringUtils.isNotEmpty(content)){
            agentConfigModel = yaml.loadAs(content ,AgentConfigModel.class);
        }else{
            agentConfigModel = new AgentConfigModel();
            agentConfigModel.writeFilterRule("");
        }

        String ruleJson = agentConfigModel.getNetflow().getFilterRule().getData();
        ruleJson = filterRuleChange.getChangeRule(ruleJson);

        agentConfigModel.writeFilterRule(ruleJson);
        String data = yaml.dump(agentConfigModel);

        boolean ret = configService.publishConfig(dataId ,group ,data , ConfigType.YAML.name());

        return ret;
    }

    @Override
    public boolean netflowConfig(NetFlowConfigParam netFlowConfigParam) {
        boolean ret = false;
        Map<String , NetflowAgent> netflowAgentMap = new HashMap<>();
        //获取配置
        try {
            String content = configService.getConfig(dataId, group, configTimeout);
            Yaml yaml = new Yaml();
            AgentConfigModel agentConfigModel = null;
            if(StringUtils.isNotEmpty(content)){
                agentConfigModel = yaml.loadAs(content , AgentConfigModel.class);
            }

            if(null != agentConfigModel){
                List<NetflowAgent> agentList = agentConfigModel.getNetflow().getAgentList();
                if(null != agentList){
                    netflowAgentMap = agentList.stream().collect(Collectors.toMap(NetflowAgent::getIp, NetflowAgent -> NetflowAgent));
                }else{
                    agentList = new ArrayList<>();
                    agentConfigModel.getNetflow().setAgentList(agentList);
                }
            }else{
                agentConfigModel = new AgentConfigModel();
                agentConfigModel.init();
            }

            boolean isChange = false;
            if(null != netFlowConfigParam.getAgentParam()){
                for(NetflowAgentConfigParam param : netFlowConfigParam.getAgentParam()){
                    isChange = compareAndModifyNetflowAgent(param ,netflowAgentMap ,agentConfigModel.getNetflow().getAgentList());
                }
            }

            //更新nacos
            if(isChange){
                String data = yaml.dump(agentConfigModel);
                ret = configService.publishConfig(dataId ,group ,data , ConfigType.YAML.name());
            }else{
                ret = true;
            }

        }catch (Exception e){
            log.error(ServiceName ,e);
        }
        return ret;
    }

    private boolean compareAndModifyNetflowAgent(NetflowAgentConfigParam param ,Map<String ,NetflowAgent> netflowAgentMap
            ,List<NetflowAgent> agentList){
        boolean isChange = false;
        int newPort = param.getPort();
        if(0 != newPort && StringUtils.isNotEmpty(param.getIp())){
            NetflowAgent netflowAgent = netflowAgentMap.get(param.getIp());
            if(null != netflowAgent){
                int oldPort = netflowAgent.getPort();
                if(newPort != oldPort){
                    netflowAgent.setPort(newPort);
                    isChange = true;
                }
            }else{
                NetflowAgent newAgent = new NetflowAgent();
                newAgent.extractFromParem(param);
                agentList.add(newAgent);
                isChange = true;
            }
        }

        return isChange;
    }

    @Override
    public List<AgentView> getAgentViews() {
        List<AgentView> list = new ArrayList<>();
        try {
            List<Instance> instances = namingService.selectInstances(ServiceName ,true);
            List<ServiceInfo> serviceInfos = new ArrayList<>();
            for(Instance instance : instances){
                log.info(instance.toString());
                if(instance.isEnabled() && instance.isHealthy()){
                    AgentView agentView = new AgentView();
                    agentView.extractFromInstance(instance);
                    list.add(agentView);
                }
            }
        }catch (Exception e){
            log.error("getAgentViews" ,e);
        }
        return list;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            if(enabled){
                Properties properties = new Properties();
                properties.put(PropertyKeyConst.SERVER_ADDR, nacosServerAddr);
                if(StringUtils.isNotEmpty(nacosUser)){
                    properties.put(PropertyKeyConst.USERNAME ,nacosUser);
                    properties.put(PropertyKeyConst.PASSWORD ,nacosPasswd);
                }
                configService = NacosFactory.createConfigService(properties);
                namingService = NacosFactory.createNamingService(properties);
            }
        } catch (NacosException e) {
            log.error("MWNetflowServiceImpl" ,e);
        }
    }
}
