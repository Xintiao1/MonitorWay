package cn.mw.monitor.agent.impl;

import cn.mw.monitor.agent.model.AgentView;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class NacosAllInstanceClient {
    private NacosAgentManage nacosAgentManage;

    public NacosAllInstanceClient(NacosAgentManage nacosAgentManage){
        this.nacosAgentManage = nacosAgentManage;
    }

    public <T,O,P> List<T>  getResultFromClient(Class<O> apiType , Class<T> retDataType, String methodName
            ,Object param , Class<P> paramType) throws NoSuchMethodException {
        List<AgentView> agentViewList = nacosAgentManage.getAgentViews();
        List<T> retData = new ArrayList<>();

        List<O> clientList = new ArrayList<>();
        for(AgentView agentView : agentViewList){
            String url = "http://" + agentView.getIp() + ":" + agentView.getServicePort();
            Feign.Builder builder = Feign.builder().client(new NacosClient());
            if(!nacosAgentManage.isEncryptEnable()){
                builder.decoder(new GsonDecoder()) // json 解码器，用于解码收到的response
                        .encoder(new GsonEncoder()); // json 编码器，用于编码发送请求
            }else{
                builder.decoder(new DecryptGsonDecoder())
                        .encoder(new EncryptGsonEncoder());
            }
            O client = builder.target(apiType, url);
            clientList.add(client);
        }

        int clientSize = clientList.size();
        if( clientSize > 0){
            List<Future<T>> retList = new ArrayList<>();
            ExecutorService executorService = Executors.newFixedThreadPool(clientSize);
            for(O client : clientList){
                Method method = client.getClass().getDeclaredMethod(methodName ,paramType);
                if(null != method){
                    Future<T> ret = executorService.submit(new Callable<T>() {
                        @Override
                        public T call() throws Exception {
                            T data = (T)method.invoke(client ,param);
                            return data;
                        }
                    });
                    retList.add(ret);
                }
            }

            for(Future<T> result : retList){
                try {
                    T data = result.get(nacosAgentManage.getNacosClientTimeout() , TimeUnit.SECONDS);
                    retData.add(data);
                }catch (Exception e){
                    log.error("NacosAllInstanceClient" ,e);
                }
            }
        }
        return retData;
    }
}
