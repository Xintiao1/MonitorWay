package cn.mw.monitor.service.timetask.api;

import java.util.List;

/**
 * @ClassName
 * @Description 定时任务公共接口
 * @Author gengjb
 * @Date 2023/5/30 9:28
 * @Version 1.0
 **/
public interface MwTimeTaskCommonsService {

    //执行首页定时任务
    void executeScreenTimeTask(Integer actionId);

    //根据模块查询定时任务actionId
    List<Integer> getTimeTaskActionIds(Integer modelId);
}
