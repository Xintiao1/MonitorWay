package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.service.user.api.MWUserLifeCycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MWUserLifeCycleManage {
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private List<MWUserLifeCycle> mwUserLifeCycleList = new ArrayList<>();
    @Autowired
    public void addUserCircleListener(List<MWUserLifeCycle> listeners) {
        for(MWUserLifeCycle listener : listeners){
            mwUserLifeCycleList.add(listener);
        }
    }

    public void login(){
        for(MWUserLifeCycle mwUserLifeCycle : mwUserLifeCycleList){
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    mwUserLifeCycle.login();
                }
            });
        }
    }
}
