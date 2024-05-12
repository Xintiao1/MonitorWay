package cn.mw.monitor.timetask.component;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.timetask.dao.MwNcmTimetaskTimePlanMapper;
import cn.mw.monitor.timetask.dao.MwTimeTaskDao;
import cn.mw.monitor.timetask.entity.MwNcmTimetaskTimePlanRun;
import cn.mw.monitor.timetask.entity.MwTimeTaskTable;
import cn.mw.monitor.timetask.entity.NewTimeTask;
import cn.mw.monitor.timetask.entity.TimetaskActrion;
import cn.mw.monitor.timetask.service.MwTimeTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Component
public class TimmerComponent {
    @Value("${customScheduling.enabled}")
    private Boolean enabled;

    @Value("${scheduling.enabled}")
    private Boolean processEnable;

    @Value("${scheduling.hasTask}")
    private Boolean hasTask;

    @Value("${customScheduling.size}")
    private Integer size;

    @Resource
    MwTimeTaskDao mwTimeTaskDao;

    @Autowired
    private MwNcmTimetaskTimePlanMapper mwNcmTimetaskTimePlanMapper;

    //保存任务
    private Map<Integer, ScheduledFuture<?>> futuresMap = new ConcurrentHashMap<Integer, ScheduledFuture<?>>();

    @Autowired
    private MwTimeTaskService mwTimeTaskService;

    //创建ThreadPoolTaskScheduler线程池
    @Autowired
    @Lazy
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        //设置线程池大小，在线程池中执行的方法都是异步，但是当同一时间需要执行的方法大于线程池的大小时，就会出现等待连接
        //相当于同步的情况，需要注意
        threadPoolTaskScheduler.setPoolSize(size);
        return threadPoolTaskScheduler;
    }

    // 初始化任务
    @Bean
    public void initTimmer(){
        if(enabled){
            if(processEnable==true || (processEnable==false&&hasTask==true)){
                List<MwNcmTimetaskTimePlanRun> timeIds =  mwNcmTimetaskTimePlanMapper.selectTimePlan(null);
                log.info("初始化了表结构");
                for (MwNcmTimetaskTimePlanRun timeId : timeIds){
                    log.info("初始化动作："+timeId.getTimeName());
                    ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(getRunnable(timeId), getTrigger(timeId));
                    futuresMap.put(timeId.getTaskId(), future);
                }
            }
        }
    }



    //    *
//     * 添加任务
//     * @param s
    public void addTask(MwNcmTimetaskTimePlanRun s){
        if(enabled){
            ScheduledFuture<?> future = threadPoolTaskScheduler.schedule(getRunnable(s), getTrigger(s));
            futuresMap.put(s.getTaskId(), future);
        }
    }

    //    *
//     * 暂停任务/删除任务
//     * @param s
//     * @return
//
    public boolean pauseTask(MwNcmTimetaskTimePlanRun s) {
        if(enabled){
            ScheduledFuture toBeRemovedFuture = futuresMap.remove(s.getTaskId());
            if (toBeRemovedFuture != null) {
                toBeRemovedFuture.cancel(true);
                return true;
            } else {
                return false;
            }
        }else {
            return true;
        }
    }
    //
//    *
//     * 更新任务
//     * @param s
//
    public void updateTask(MwNcmTimetaskTimePlanRun s) {
        if(enabled){
            ScheduledFuture toBeRemovedFuture = futuresMap.remove(s.getTaskId());
            if (toBeRemovedFuture != null) {
                toBeRemovedFuture.cancel(true);
            }
            addTask(s);
        }
    }



    //     *
//     * 根据spring默认规则 以类名获取对象名称
//     （当为类手动设置对象名称可能对应不上）
//     * @param str
//     * @return
    public static String lowerFirstCapse(String str) {
        char[] chars = str.toCharArray();
        if(chars.length>=2){
            boolean a =  Character.isUpperCase(chars[0]);
            if(a){
                boolean b =  Character.isUpperCase(chars[1]);
                if(b){
                    return String.valueOf(chars);
                }
            }
        }
        chars[0] += 32;
        return String.valueOf(chars);
    }




    private Runnable getRunnable(MwNcmTimetaskTimePlanRun scheduleConfig){
        return new Runnable() {
            @Override
            public void run() {
                Class<?> clazz;
                try {
                    NewTimeTask newTimeTask = new NewTimeTask();
                    newTimeTask.setTimeStartTime(new Date());
                    CronSequenceGenerator generator = new CronSequenceGenerator(scheduleConfig.getTimeCron());
                    Date afterTime = generator.next(newTimeTask.getTimeStartTime());
                    newTimeTask.setTimeEndTime(afterTime);
                    newTimeTask.setId(scheduleConfig.getNewtimetaskId());
                    NewTimeTask newTimeTasks = mwNcmTimetaskTimePlanMapper.selectTimeAllTask(newTimeTask).get(0);
                    newTimeTasks.setTimeStartTime(new Date());
                    newTimeTasks.setTimeEndTime(afterTime);
                    List<String> objectId = mwNcmTimetaskTimePlanMapper.getObjectId(scheduleConfig.getNewtimetaskId());
                    TimetaskActrion timetaskActrions = mwNcmTimetaskTimePlanMapper.getAction(scheduleConfig.getNewtimetaskId());
                    log.info("执行当前定时任务类:{}",timetaskActrions.getActionImpl());
                    log.info("执行当前定时任务动作:{}",timetaskActrions.getActionMethod());
                    //组方法
                    log.info("getRunnable getNewtimetaskId:{}" ,scheduleConfig.getNewtimetaskId());
                    clazz = Class.forName(timetaskActrions.getActionImpl());
                    String className = lowerFirstCapse(clazz.getSimpleName());
                    Object bean = (Object) ApplicationContextProvider.getBean(className);
                    List<TimeTaskRresult> objects = new ArrayList<>();

                    if (objectId.size()==0||objectId==null){
                        TimeTaskRresult res = new TimeTaskRresult();
                        try {
                            res =  (TimeTaskRresult)getMethodReflection(bean,timetaskActrions.getActionMethod(),"",newTimeTasks.getTimeObject());
                        }catch (Exception e){
                            res.setSuccess(false);
                            res.setFailReason(e.toString());
                        }
                        res.setNewTimetaskId(scheduleConfig.getNewtimetaskId());
                        res.setResultEndDate(res.getStartTime(),new Date());
                        objects.add(res);
                    }else {
                        for (String s:objectId) {
                            TimeTaskRresult res = new TimeTaskRresult();
                            try {
                                res =  (TimeTaskRresult)getMethodReflection(bean,timetaskActrions.getActionMethod(),s,newTimeTasks.getTimeObject());
                            }catch (Exception e){
                                res.setSuccess(false);
                                res.setFailReason(e.toString());
                            }
                            res.setObjectId(s);
                            res.setNewTimetaskId(scheduleConfig.getNewtimetaskId());
                            res.setResultEndDate(res.getStartTime(),new Date());
                            objects.add(res);
                        }
                    }
                    //生成历史记录

                    cleartHis(newTimeTasks,objects);

                } catch (ClassNotFoundException e) {
                    log.error("错误返回 :{}",e);
                }
            }

            private void cleartHis(NewTimeTask newTimeTasks, List<TimeTaskRresult> objects) {
                Date now = new Date();
                Long timecont = (now.getTime()-newTimeTasks.getTimeStartTime().getTime())/1000;
                newTimeTasks.setTimeCount(timecont.intValue());
                mwNcmTimetaskTimePlanMapper.updateNewTimeTask(newTimeTasks);
                mwNcmTimetaskTimePlanMapper.insertTimeTakHis(objects);
            }

            private Object getMethodReflection(Object bean, String actionMethod, String s, String timeObject) {
                Method method = null;
                method =ReflectionUtils.findMethod(bean.getClass(), actionMethod);
                if (method==null){
                    method =ReflectionUtils.findMethod(bean.getClass(), actionMethod,String.class);
                }if (method==null){
                    method =ReflectionUtils.findMethod(bean.getClass(), actionMethod,String.class,String.class);
                }

                Object res = new Object();
                Class<?> []clazz = method.getParameterTypes();
                switch (clazz.length){
                    case 0:
                        res = ReflectionUtils.invokeMethod(method, bean);
                        break;
                    case 1:
                        res = ReflectionUtils.invokeMethod(method, bean,s);
                        break;
                    case 2:
                        res = ReflectionUtils.invokeMethod(method, bean,s,timeObject);
                        break;
                    default:
                        break;
                }
                return res;
            }
        };
    }




    private void upSch(MwTimeTaskTable scheduleConfig, String lastRes, Date lastTime) {
        CronSequenceGenerator generator = new CronSequenceGenerator(scheduleConfig.getCron());
        Date afterTime = generator.next(lastTime);
        scheduleConfig.setLastResult(lastRes);
        scheduleConfig.setLastTime(lastTime);
        scheduleConfig.setAfterTime(afterTime);
        mwTimeTaskDao.updateSomeThree(scheduleConfig);
    }


    private Trigger getTrigger(MwNcmTimetaskTimePlanRun mwNcmTimetaskTimePlanRun){
        return new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                CronTrigger trigger = new CronTrigger(mwNcmTimetaskTimePlanRun.getTimeCron());
                Date nextExec = trigger.nextExecutionTime(triggerContext);
                return nextExec;
            }
        };

    }
}
