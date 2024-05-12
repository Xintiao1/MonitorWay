package cn.mw.module.security.service.impl;

import cn.mw.module.security.common.AlertFieldEnum;
import cn.mw.module.security.dto.*;
import cn.mw.module.security.service.OperateDataSource;
import cn.mw.monitor.security.dao.DataSourceConfigureDao;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.monitor.util.RSAUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OperateKafkaDataSource implements OperateDataSource {

    /*@Resource
    private DataSourceConfigureDao dataSourceConfigureDao;*/

    @Override
    public Integer creatDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        ModuleIDManager moduleIDManager = new ModuleIDManager();
        //密码加密
        if(dataSource.getPassword() != null && dataSource.getPassword() != ""){
            dataSource.setPassword(RSAUtils.encryptData(dataSource.getPassword(),RSAUtils.RSA_PUBLIC_KEY));
        }
        //新增Kafka数据源
        int i = dataSourceConfigureDao.creatDataSourceInfo(dataSource);
        //保存topic信息
        List<TopicDTO> topicList = dataSource.getTopic();
        if(topicList != null && topicList.size() > 0){
            for(TopicDTO topic:topicList){
                String topicName = topic.getTopicName();
                String topicCode = String.valueOf(System.currentTimeMillis());
                topic.setTopicCode(topicCode);
                Boolean consumeRule = topic.getConsumeRule();
                List<TopicFieldDTO> fieldList = topic.getFieldList();
                if(fieldList != null && fieldList.size() > 0){
                    for(TopicFieldDTO field:fieldList){
                        field.setId(String.valueOf(moduleIDManager.getID(IDModelType.DataSource)));
                        field.setTopicCode(topicCode);
                        field.setTopicName(topicName);
                        field.setConsumeRule(consumeRule);
                        field.setKafkaId(dataSource.getId());
                        if (AlertFieldEnum.事件时间.getValue().equals(field.getFieldCode())
                                || AlertFieldEnum.事件来源.getValue().equals(field.getFieldCode())
                                || AlertFieldEnum.事件状态.getValue().equals(field.getFieldCode())) {
                            field.setMappingRuleName(String.valueOf(field.getMappingRule()));
                        }
                        dataSourceConfigureDao.creatTopicFieldInfo(field);

                        //保存事件等级映射信息
                        if(AlertFieldEnum.严重级别.getValue().equals(field.getFieldCode())){
                            List<Map<String,String>> mapList = (List<Map<String, String>>) field.getMappingRule();
                            List<FieldModelDTO> levelList = mapList.stream().map(map -> {
                                FieldModelDTO fieldModelDTO = new FieldModelDTO();
                                Field[] fields = FieldModelDTO.class.getDeclaredFields();
                                for(Field f :fields){
                                    f.setAccessible(true);
                                    try {
                                        Object value = map.get(f.getName());
                                        f.set(fieldModelDTO,value);
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                return fieldModelDTO;
                            }).collect(Collectors.toList());
                            for(FieldModelDTO alertLevel:levelList){
                                alertLevel.setFieldId(field.getId());
                                alertLevel.setTopicCode(topic.getTopicCode());
                            }
                            dataSourceConfigureDao.creatAlertLevelInfo(levelList);
                        }
                    }
                }
            }
        }
        return i;
    }

    @Override
    public DataSourceConfigureDTO getDataSourceInfo(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        //查询kafka的topicFiled信息
        List<TopicFieldDTO> fieldList = dataSourceConfigureDao.getTopicByKafkaId(dataSource.getId());
        //密码解密
        if(dataSource.getPassword() != null && !"".equals(dataSource.getPassword())){
            dataSource.setPassword(RSAUtils.decryptData(dataSource.getPassword(),RSAUtils.RSA_PRIVATE_KEY));
        }
        //kafka的运行状态监测
        //todo kafka运行状态监测响应时长需更加优化
        if(kafkaAlive(dataSource.getIp()+":"+dataSource.getPort(),dataSource.getIp(),dataSource.getPort())){
            dataSource.setStatus(1);
            //状态改为 启用
            dataSourceConfigureDao.editorDataSourceStatus(dataSource);
        }else{
            dataSource.setStatus(0);
            //状态改为 停用
            dataSourceConfigureDao.editorDataSourceStatus(dataSource);
        }

        List<TopicDTO> topicList = new ArrayList<>();
        //将字段信息根据topicCode分组
        if(fieldList == null || fieldList.size()==0){
            return dataSource;
        }
        //获取字段对应匹配模式
        for(TopicFieldDTO fieldDTO:fieldList){
            if(AlertFieldEnum.事件时间.getValue().equals(fieldDTO.getFieldCode())){
                fieldDTO.setFieldModel(dataSourceConfigureDao.getTimeModel());
                fieldDTO.setMappingRule(JSONObject.parseObject(String.valueOf(fieldDTO.getMappingRule()),new TypeReference<Map<String,String>>(){}));
            }
            if(AlertFieldEnum.事件来源.getValue().equals(fieldDTO.getFieldCode())){
                fieldDTO.setFieldModel(dataSourceConfigureDao.getEventSources());
                fieldDTO.setMappingRule(JSONObject.parseObject(String.valueOf(fieldDTO.getMappingRule()),new TypeReference<Map<String,String>>(){}));
            }
            if(AlertFieldEnum.事件状态.getValue().equals(fieldDTO.getFieldCode())){
                fieldDTO.setMappingRule(Boolean.valueOf(String.valueOf(fieldDTO.getMappingRule())));
            }
            if(AlertFieldEnum.严重级别.getValue().equals(fieldDTO.getFieldCode())){
                fieldDTO.setMappingRule(dataSourceConfigureDao.getLevelList(fieldDTO));
            }
        }
        Map<String,List<TopicFieldDTO>> fieldByTopicCode = fieldList.stream().collect(Collectors.groupingBy(x -> x.getTopicCode()));
        for(Map.Entry<String,List<TopicFieldDTO>>entry:fieldByTopicCode.entrySet()){
            TopicDTO topic = new TopicDTO();
            List<String> topicName = entry.getValue().stream().map(m -> m.getTopicName()).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
            topic.setTopicName(topicName.get(0));
            topic.setTopicCode(entry.getKey());
            topic.setFieldList(entry.getValue());
            topicList.add(topic);
        }
        dataSource.setTopic(topicList);
        return dataSource;
    }

    @Override
    public Integer editDataSourceInfo(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        ModuleIDManager moduleIDManager = new ModuleIDManager();
        //密码加密
        if(dataSource.getPassword() != null && dataSource.getPassword() != ""){
            dataSource.setPassword(RSAUtils.encryptData(dataSource.getPassword(),RSAUtils.RSA_PUBLIC_KEY));
        }
        //编辑topic信息
        List<TopicDTO> topicList = dataSource.getTopic();
        if(topicList != null && topicList.size() > 0){
            for(TopicDTO topic:topicList){
                String topicName = topic.getTopicName();
                Boolean consumeRule = topic.getConsumeRule();
                String topicCode;
                if("".equals(topic.getTopicCode()) || topic.getTopicCode() == null){
                    //新增topic
                    topicCode = String.valueOf(System.currentTimeMillis());
                }else {
                    //编辑topic
                    topicCode = topic.getTopicCode();
                }
                List<TopicFieldDTO> fieldList = topic.getFieldList();
                //编辑topic字段
                if(fieldList != null && fieldList.size() > 0){
                    for(TopicFieldDTO field:fieldList){
                        field.setTopicName(topicName);
                        field.setTopicCode(topicCode);
                        field.setConsumeRule(consumeRule);
                        field.setKafkaId(dataSource.getId());
                        if(AlertFieldEnum.事件时间.getValue().equals(field.getFieldCode())
                                || AlertFieldEnum.事件来源.getValue().equals(field.getFieldCode())
                                || AlertFieldEnum.事件状态.getValue().equals(field.getFieldCode())){
                            field.setMappingRuleName(String.valueOf(field.getMappingRule()));
                        }
                        //topic新增字段
                        if("".equals(field.getId()) || field.getId() == null){
                            field.setId(String.valueOf(moduleIDManager.getID(IDModelType.DataSource)));
                            //新增字段
                            dataSourceConfigureDao.creatTopicFieldInfo(field);
                        }else {
                            //编辑字段
                            dataSourceConfigureDao.editorTopicFieldInfo(field);
                        }
                        //保存事件等级映射信息
                        if(AlertFieldEnum.严重级别.getValue().equals(field.getFieldCode())){
                            dataSourceConfigureDao.deleteAlertLevelInfo(field.getId());
                            List<Map<String,String>> mapList = (List<Map<String, String>>) field.getMappingRule();
                            List<FieldModelDTO> levelList = mapList.stream().map(map -> {
                                FieldModelDTO fieldModelDTO = new FieldModelDTO();
                                Field[] fields = FieldModelDTO.class.getDeclaredFields();
                                for(Field f :fields){
                                    f.setAccessible(true);
                                    try {
                                        Object value = map.get(f.getName());
                                        f.set(fieldModelDTO,value);
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                return fieldModelDTO;
                            }).collect(Collectors.toList());
                            for(FieldModelDTO alertLevel:levelList){
                                alertLevel.setFieldId(field.getId());
                                alertLevel.setTopicCode(topic.getTopicCode());
                            }
                            dataSourceConfigureDao.creatAlertLevelInfo(levelList);
                        }
                    }
                }
                //编辑topic
                TopicFieldDTO field = new TopicFieldDTO();
                field.setTopicCode(topicCode);
                field.setTopicName(topicName);
                field.setConsumeRule(consumeRule);
                field.setId(null);
                dataSourceConfigureDao.editorTopicFieldInfo(field);
            }

        }
        //编辑kafka信息
        int i = dataSourceConfigureDao.editorDataSourceInfo(dataSource);
        //todo 调用flink处理kafka消息接口将编辑后的kafka信息存入处理队列中
        return i;
    }

    @Override
    public Integer shutDownDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        //todo 调用flink处理kafka消息接口将kafka从处理队列中删除
        //状态改为 禁用
        return dataSourceConfigureDao.editorDataSourceInfo(dataSource);
    }

    @Override
    public Integer initDataSource(DataSourceConfigureDao dataSourceConfigureDao, DataSourceConfigureDTO dataSource) {
        //todo 调用flink处理kafka消息接口将kafka加入处理队列中
        //状态改为 启用
        return dataSourceConfigureDao.editorDataSourceInfo(dataSource);
    }

    /**
     * 检测kafka是否正常连接状态
     * @return
     */
    public boolean kafkaAlive(String ipPort,String ip,Integer port){
        /*Properties properties = new Properties();
        properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, ipPort);
        properties.put(AdminClientConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 10000);
        properties.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 1000);
        try (AdminClient client = KafkaAdminClient.create(properties)) {
            Collection<Node> nodes = client.describeCluster().nodes().get();
            return nodes != null && nodes.size() > 0;
        } catch (Exception e){
            return false;
        }*/

        try(Socket socket= new Socket(ip,port)){
            return true;
        }catch (Exception e){
            return false;
        }
    }

}
