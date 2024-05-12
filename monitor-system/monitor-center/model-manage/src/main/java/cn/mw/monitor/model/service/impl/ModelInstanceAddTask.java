package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dto.SystemLogDTO;
import cn.mw.monitor.service.assets.event.AddTangibleassetsEvent;
import cn.mw.monitor.service.assets.event.BatchAddAssetsEvent;
import cn.mw.monitor.service.common.ListenerService;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Slf4j
public class ModelInstanceAddTask<T> implements Runnable{
    private static final Logger mwlogger = LoggerFactory.getLogger("MWDBLogger");
    //日志时间
    private Date logTime;
    //操作登录名
    private String userName;
    //模块名
    private String modelName;
    //模型名称
    private String mwModelName;

    private T addEvent;
    private ListenerService listenerService;

    public ModelInstanceAddTask(T addEvent , ListenerService listenerService){
        this.addEvent = addEvent;
        this.listenerService = listenerService;
    }

    @Override
    public void run() {
        try {
            log.info("ModelInstanceAddTask start mwModelName:{}", mwModelName);
            if(addEvent instanceof BatchAddAssetsEvent) {
                BatchAddAssetsEvent batchAddAssetsEvent = (BatchAddAssetsEvent)addEvent;
                listenerService.publishFinishEvent(batchAddAssetsEvent);
                for (ScanResultSuccess scanResultSuccess : batchAddAssetsEvent.getScanResultSuccessList()) {
                    SystemLogDTO systemLogDTO = SystemLogDTO.builder().logTime(logTime).userName(userName)
                            .objName(scanResultSuccess.getIpAddress())
                            .modelName(modelName).mwModelName(mwModelName).operateDes(scanResultSuccess.toSystemInfo()).build();
                    mwlogger.info(JSON.toJSONString(systemLogDTO));
                }
            }else if(addEvent instanceof AddTangibleassetsEvent){
                AddTangibleassetsEvent addTangibleassetsEvent = (AddTangibleassetsEvent)addEvent;
                listenerService.publishPostEvent(addTangibleassetsEvent);
                SystemLogDTO systemLogDTO = SystemLogDTO.builder().logTime(logTime).userName(userName)
                            .objName(addTangibleassetsEvent.getAddTangAssetsParam().getHostName())
                            .modelName(modelName).mwModelName(mwModelName).operateDes(addTangibleassetsEvent.toSystemInfo()).build();
                    mwlogger.info(JSON.toJSONString(systemLogDTO));
            }
            log.info("ModelInstanceAddTask finish mwModelName:{}", mwModelName);
        }catch (Throwable e){
            log.error("ModelInstanceAddTask" ,e);
        }
    }

    public void init(Date logTime ,String userName ,String modelName ,String mwModelName){
        this.logTime = logTime;
        this.userName = userName;
        this.modelName = modelName;
        this.mwModelName = mwModelName;
    }
}
