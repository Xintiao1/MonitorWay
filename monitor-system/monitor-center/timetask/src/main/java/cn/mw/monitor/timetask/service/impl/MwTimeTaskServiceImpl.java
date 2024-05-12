package cn.mw.monitor.timetask.service.impl;

import cn.mw.monitor.api.common.LoadUtil;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.service.timetask.api.MwTimeTaskCommonsService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.timetask.component.CronUtil;
import cn.mw.monitor.timetask.component.EncryptUtil;
import cn.mw.monitor.timetask.component.TimmerComponent;
import cn.mw.monitor.timetask.dao.MwNcmTimetaskTimePlanMapper;
import cn.mw.monitor.timetask.dao.MwTimeTaskDao;
import cn.mw.monitor.timetask.entity.*;
import cn.mw.monitor.timetask.service.MwTimeTaskService;
import cn.mw.monitor.util.RedisUtils;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@EnableAsync
public class MwTimeTaskServiceImpl implements MwTimeTaskService, MwTimeTaskCommonsService {

    @Autowired
    private TimmerComponent timmerComponent;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Resource
    MwTimeTaskDao mwTimeTaskDao;

    @Autowired
    private MwNcmTimetaskTimePlanMapper mwNcmTimetaskTimePlanMapper;

    @Value("${scheduling.taskTimeUrl}")
    private String taskTimeUrl;

    @Value("${scheduling.hasTask}")
    private boolean hasTask;

    @Value("${scheduling.enabled}")
    private boolean enable;


    @Autowired
    private RedisUtils redisUtils;

    public static final String TIME_REFRESH = "/TimeServer/refresh";
    public static final String TIME_RUN_NOW = "/timeAllTask/runNewTime";

    @Override
    public TimeProcessResult refreshServers(List<MwNcmTimetaskTimePlanRun> param) {
        try {
            synchronized (LoadUtil.class) {
                if (enable == false && hasTask == true) {
                    String str = LoadUtil.post2(taskTimeUrl, JSONObject.toJSONString(param));
                    Integer resCode = JSONObject.parseObject(str).getInteger("rtnCode");
                    log.info("刷新定时器执行返回结果 :{}", str);
                    return new TimeProcessResult(resCode == 200 ? true : false, "刷新定时器返回结果");
                } else {
                    log.info("刷新定时器中");
                    for (MwNcmTimetaskTimePlanRun p : param) {
                        try {
                            timmerComponent.pauseTask(p);
                        } catch (Exception e) {
                        }
                        if (p.getIsButton() == 1) {
                            timmerComponent.addTask(p);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("MwTimeTaskServiceImpl", e);
            return new TimeProcessResult(false, "MwTimeTaskServiceImpl 异常!");
        }
        return new TimeProcessResult(true, "MwTimeTaskServiceImpl 更新成功!");
    }

    @Override
    public Reply editorBrowse(AddTimeTaskParam param) {
        MwTimeTaskTable s = mwTimeTaskDao.selectOne(param);
        return Reply.ok(s);
    }

    @Override
    public Reply selectListHis(QueryTimeTaskParam param) {
        List<MwTimeTaskDownloadHis> list = new ArrayList<>();
        PageHelper.startPage(param.getPageNumber(), param.getPageSize());
        Map pubCriteria = null;
        try {
            pubCriteria = PropertyUtils.describe(param);
        } catch (IllegalAccessException e) {
            log.error("刷新定时器执行返回结果 :{}", e);
        } catch (InvocationTargetException e) {
            log.error("刷新定时器执行返回结果 :{}", e);
        } catch (NoSuchMethodException e) {
            log.error("刷新定时器执行返回结果 :{}", e);
        }
        list = mwTimeTaskDao.selectListHis(pubCriteria);
        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);

        return Reply.ok(pageInfo);
    }

    @Override
    public Reply selectList(QueryTimeTaskParam qParam) {
        try {
            List<MwTimeTaskTable> list = new ArrayList<>();
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            Map pubCriteria = PropertyUtils.describe(qParam);
            list = mwTimeTaskDao.selectList(pubCriteria);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);

            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("刷新定时器执行返回结果 :{}", e);
            return Reply.fail(500, "定时任务查询失败");
        }
    }

    @Override
    public List<MwTimeTaskTable> selectList() {
        try {
            List<MwTimeTaskTable> list = new ArrayList<>();
            list = mwTimeTaskDao.selectAllList();
            return list;
        } catch (Exception e) {
            log.error("刷新定时器执行返回结果 :{}", e);
            return null;
        }
    }

    @Override
    public Reply delete(List<AddTimeTaskParam> auParam) throws Exception {
        //删除
        mwTimeTaskDao.deleteBatch(auParam);

        //判断定时器
        for (AddTimeTaskParam param : auParam) {
            mwTimeTaskDao.deleteHisList(param.getId());
            /*if(param.getStatus()==true){
                MwTimeTaskTable data = new MwTimeTaskTable();
                BeanUtils.copyProperties(param,data);
                timmerComponent.pauseTask(data);
            }*/
        }
        return Reply.ok("删除成功");
    }

    @Override
    public Reply update(AddTimeTaskParam auParam) throws Exception {
        //修改模板管理 主信息
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        auParam.setModificationDate(new Date());
        if (auParam.getTimeCustom() != null) {
            if (!auParam.getTimeCustom()) {
                String cron = getCron(auParam);
                auParam.setCron(cron);
            } else {
                String cron = auParam.getCron();
                boolean isTrue = CronSequenceGenerator.isValidExpression(cron);
                if (!isTrue) {
                    return Reply.fail("请输入正确的定时器时间规则" + cron);
                }
            }
        }
        auParam.setPlan(CronUtil.translateToChinese(auParam.getCron()));
        mwTimeTaskDao.update(auParam);

        //判断 定时器情况

        return Reply.ok(auParam);
    }

    @Override
    public Reply insert(AddTimeTaskParam auParam) throws Exception {

        //1 添加定时任务信息
        auParam.setCreator(iLoginCacheInfo.getLoginName());
        auParam.setCreateDate(new Date());
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        auParam.setModificationDate(new Date());
        if (!auParam.getTimeCustom()) {
            String cron = getCron(auParam);
            auParam.setCron(cron);
            boolean isTrue = CronSequenceGenerator.isValidExpression(cron);
            if (!isTrue) {
                return Reply.fail("请输入正确的定时器时间规则" + cron);
            }
        } else {
            String cron = auParam.getCron();
            boolean isTrue = CronSequenceGenerator.isValidExpression(cron);
            if (!isTrue) {
                return Reply.fail("请输入正确的定时器时间规则" + cron);
            }
        }
        auParam.setPlan(CronUtil.translateToChinese(auParam.getCron()));
        mwTimeTaskDao.insert(auParam);

        //2 如果定时任务已开启，则添加定时器
        /*if(auParam.getStatus()==true){
            MwTimeTaskTable data = new MwTimeTaskTable();
            BeanUtils.copyProperties(auParam,data);
            timmerComponent.addTask(data);
        }*/
        return Reply.ok(auParam);
    }

    @Override
    public Reply getTypeList(Integer type) {
        List<MwTimeTaskTypeMapper> list = mwTimeTaskDao.selectTypeList(type);
        return Reply.ok(list);
    }

    @Override
    public Reply browseHisFile(MwTimeTaskDownloadHis param) {
        String s = showTxt(param.getPath() + "/" + param.getName());
        param.setContext(s);
        return Reply.ok(param);
    }

    @Override
    public void downHisFile(MwTimeTaskDownloadHis param, HttpServletResponse response) {
        String pathname = param.getPath() + "/" + param.getName();
        String fileName = param.getName();
        String str = showTxt(pathname);
        OutputStream os = null;

        response.setContentType("application/force-download");
        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);

        try {
            os = response.getOutputStream();
            os.write(str.getBytes("UTF-8"));
            os.close();
        } catch (Exception e) {
            log.error("刷新定时器执行返回结果 :{}", e);
        }

    }

    @Override
    public Reply deleteHis(List<MwTimeTaskDownloadHis> param) {
        mwTimeTaskDao.deleteDownloadHis(param);
        for (MwTimeTaskDownloadHis data : param) {
            String fileName = data.getPath() + "/" + data.getName();
            File file = new File(fileName);
            file.delete();
        }
        return Reply.ok("删除成功！");
    }

    @Override
    public Reply getTree(TimeTaskBase param) {
        List<TimetaskModel> timetaskModels = mwTimeTaskDao.getTree(param.getType(), param.getSreach());
        for (TimetaskModel task : timetaskModels) {
            List<TimetaskActrion> timeTaskActions = mwTimeTaskDao.findTreeAction(task.getId());
            task.setTimetaskActrions(timeTaskActions);
        }

        if (!param.isActionHave()) {
            for (TimetaskModel timetaskModel : timetaskModels) {
                timetaskModel.setTimetaskActrions(new ArrayList<>());
            }
        }
        if (param.getModelId() != 0) {
            List<TimetaskModel> timetaskModelList = new ArrayList<>();
            for (TimetaskModel timetaskModel : timetaskModels) {
                if (timetaskModel.getModelId().equals(param.getModelId())) {
                    timetaskModelList.add(timetaskModel);
                }
            }
            timetaskModels = timetaskModelList;
        }
        if (param.getSreach() != null) {
            List<TimetaskModel> timetaskModelList = new ArrayList<>();
            for (TimetaskModel timetaskModel : timetaskModels) {
                if (timetaskModel.getModelName().contains(param.getSreach())) {
                    timetaskModelList.add(timetaskModel);
                } else if (timetaskModel.getTimetaskActrions().stream().filter(e -> e.getActionName().contains(param.getSreach())).collect(Collectors.toList()).size() > 0) {
                    timetaskModelList.add(timetaskModel);
                }
            }
            timetaskModels = timetaskModelList;
        }

        return Reply.ok(timetaskModels);
    }

    @Override
    public Reply insertTimeCreate(MwNcmTimetaskTimePlan param) {

        return null;
    }

    @Override
    public Reply getTransferStationTree(Transfer transfer) {
        String modelName = mwNcmTimetaskTimePlanMapper.getModelName(transfer.getModelId());
        if (transfer.getActionId() != null) {
            if (modelName == null || !modelName.equals("")) {
                modelName = mwNcmTimetaskTimePlanMapper.getModelNamebyActionId(transfer.getActionId());
            }
            PageInfo pageInfo = getObjectTree(modelName, new ArrayList<String>(), transfer.getActionId().toString(), transfer.getPageNumber(), transfer.getPageSize(), transfer);
            transfer.setTreeHave(true);
            if (transfer.getModelId() != 1 && transfer.getModelId() != 4 && transfer.getModelId() != 5) {
                transfer.setTreeHave(false);
            }

            transfer.setTree(pageInfo);
        }

        return Reply.ok(transfer);
    }

    private PageInfo getObjectTree(String modelName, List<String> strings, String actionId, Integer pageNumber, Integer pageSize, Transfer transfer) {
        List<Tree> trees = new ArrayList<>();
        PageHelper.startPage(pageNumber, pageSize);
        switch (actionId) {
            /*报表任务*/
            case "1":
            case "2":
            case "3":
                trees = mwNcmTimetaskTimePlanMapper.getReportChild(modelName, strings, transfer.getSearch());
                break;
            /*配置管理*/
            case "6":
            case "7":
            case "11":
                trees = mwNcmTimetaskTimePlanMapper.getTangibleassets(modelName, strings, transfer.getSearch());
                break;
            case "8":
                trees = mwNcmTimetaskTimePlanMapper.getTangibleassets(modelName, strings, transfer.getSearch());
                break;
            /*IP地址管理*/
            case "9":
                trees = mwNcmTimetaskTimePlanMapper.getIPListChild(modelName, strings, transfer.getSearch());
                break;
            case "36":
                trees = mwNcmTimetaskTimePlanMapper.getAutoProgram(modelName, strings, transfer.getSearch());
                break;
            case "43":
                trees = mwNcmTimetaskTimePlanMapper.getTangibleassets(modelName, strings, transfer.getSearch());
                break;
        }
        PageInfo pageInfo = new PageInfo<>(trees);
        pageInfo.setList(trees);
        return pageInfo;
    }


    private List<Tree> getObjectTreeList(String modelName, List<String> strings, Integer actionId) {
        List<Tree> trees = new ArrayList<>();
        switch (actionId) {
            /*报表任务*/
            case 1:
            case 2:
            case 3:
                trees = mwNcmTimetaskTimePlanMapper.getReportChild(modelName, strings, null);
                break;
            /*配置管理*/
            case 6:
            case 7:
                break;
            case 8:
                break;
            /*IP地址管理*/
            case 5:
                trees = mwNcmTimetaskTimePlanMapper.getIPListChild(modelName, strings, null);
                break;
        }
        return trees;
    }

    @Override
    public Reply createTimeAllTask(NewTimeTask newTimeTask) {
        Integer isbutton = newTimeTask.getTimeButton();
        if (newTimeTask.getNewtimetaskMapperTimes().size() == 0 && (newTimeTask.getId() != null || newTimeTask.getId() != "")) {
            NewTimeTaskEditor newTimeTasks = (NewTimeTaskEditor) timeAllTaskObjectEditorBrow(newTimeTask).getData();
            BeanUtils.copyProperties(newTimeTasks, newTimeTask);
            newTimeTask.setTimeButton(isbutton);
        }
        if (newTimeTask.getCycle() != null && !newTimeTask.getCycle().trim().equals("")) {
            newTimeTask.setTimeObject(newTimeTask.getCycle());
        }
        if (newTimeTask.getNewtimetaskMapperTimes().size() > 0) {
            if (newTimeTask.getId() == null || newTimeTask.getId() == "") {
                newTimeTask.setId(UuidUtil.getUid());
                mwNcmTimetaskTimePlanMapper.insertTimeTask(newTimeTask);
            } else {
                List<MwNcmTimetaskTimePlanRun> timeIds = mwNcmTimetaskTimePlanMapper.selectTimePlan(newTimeTask.getId());
                for (MwNcmTimetaskTimePlanRun s : timeIds) {
                    s.setIsButton(0);
                }
                mwNcmTimetaskTimePlanMapper.deleteMapperObjectId(newTimeTask.getId());
                mwNcmTimetaskTimePlanMapper.deleteMapperTimeId(newTimeTask.getId());
                mwNcmTimetaskTimePlanMapper.updateNewTimeTask(newTimeTask);
                refreshServers(timeIds);
                for (MwNcmTimetaskTimePlanRun s : timeIds) {
                    s.setIsButton(isbutton);
                }
            }
            //数据层面
            if (newTimeTask.getId() != null || newTimeTask.getId() != "") {
                if (newTimeTask.getNewtimetaskMapperObjects().size() > 0) {
                    mwNcmTimetaskTimePlanMapper.insertTimeTaskMapperObject(newTimeTask.getId(), newTimeTask.getNewtimetaskMapperObjects());
                }
                mwNcmTimetaskTimePlanMapper.insertTimeTaskMapperTime(newTimeTask.getId(), newTimeTask.getNewtimetaskMapperTimes());
                List<MwNcmTimetaskTimePlanRun> timeIds = mwNcmTimetaskTimePlanMapper.selectTimePlan(newTimeTask.getId());
                refreshServers(timeIds);
            } else {
                return Reply.fail("未选择执行对象或者执行时间");
            }
        } else {
            return Reply.fail("未选择执行对象或者执行时间");
        }
        return Reply.ok();
    }

    @Override
    public Reply timeAllTaskBrows(NewTimeTask newTimeTask) {
        PageHelper.startPage(newTimeTask.getPageNumber(), newTimeTask.getPageSize());
        List<NewTimeTask> newTimeTasks = mwNcmTimetaskTimePlanMapper.selectTimeAllTask(newTimeTask);
        PageInfo pageInfo = new PageInfo<>(newTimeTasks);
        pageInfo.setList(newTimeTasks);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply timeAllTaskObjectBrows(NewTimeTask newTimeTask) {

        String modelName = mwNcmTimetaskTimePlanMapper.getModelName(newTimeTask.getModelId());
        List<String> ids = mwNcmTimetaskTimePlanMapper.getObjectId(newTimeTask.getId());
        Transfer transfer = new Transfer();
        PageInfo tree = getObjectTree(modelName, ids, newTimeTask.getActionId().toString(), newTimeTask.getPageNumber(), newTimeTask.getPageSize(), transfer);
        return Reply.ok(tree);
    }

    @Override
    public Reply timeAllTaskObjectEditorBrow(NewTimeTask newTimeTask) {
        NewTimeTask newTimeTasks = mwNcmTimetaskTimePlanMapper.selectTimeAllTask(newTimeTask).get(0);
        NewTimeTaskEditor newTimeTaskEditor = new NewTimeTaskEditor();
        BeanUtils.copyProperties(newTimeTasks, newTimeTaskEditor);
        if (newTimeTasks.getTimeButton().equals(1)) {
            newTimeTaskEditor.setTimeButton(true);
        }
        List<String> TreeIds = mwNcmTimetaskTimePlanMapper.getObjectId(newTimeTasks.getId());
        String modelName = mwNcmTimetaskTimePlanMapper.getModelName(newTimeTaskEditor.getModelId());
        Transfer transfer = new Transfer();
        List<Tree> trees = getObjectTree(modelName, TreeIds, newTimeTaskEditor.getActionId(), newTimeTask.getPageNumber(), 1000000000, transfer).getList();
        List<MwNcmTimetaskTimePlan> timetaskTimePlans = mwNcmTimetaskTimePlanMapper.getTimePlan(newTimeTasks.getId());
        newTimeTaskEditor.setNewtimetaskMapperObjects(trees);
        if (newTimeTaskEditor.getTimeObject() != null && !newTimeTaskEditor.getTimeObject().trim().equals("")) {
            newTimeTaskEditor.setCycle(newTimeTaskEditor.getTimeObject());
        }

        newTimeTaskEditor.setNewtimetaskMapperTimes(timetaskTimePlans);
        return Reply.ok(newTimeTaskEditor);
    }

    @Override
    public void action(Integer id, String action) {
        List<MwNcmTimetaskTimePlanRun> timeIds = mwNcmTimetaskTimePlanMapper.selectTimePlanbyTimeid(id);
        for (MwNcmTimetaskTimePlanRun p : timeIds) {
            if (action == "delete") {
                p.setIsButton(0);
            }
        }
        refreshServers(timeIds);
    }

    @Override
    public Reply timeAllTaskDelete(DeleteTimeTask newTimeTask) {
        List<String> id = newTimeTask.getNewTimeTaskid();
        for (String k : id) {
            List<MwNcmTimetaskTimePlanRun> timeIds = mwNcmTimetaskTimePlanMapper.selectTimePlan(k);
            for (MwNcmTimetaskTimePlanRun s : timeIds) {
                s.setIsButton(0);
            }
            refreshServers(timeIds);

            mwNcmTimetaskTimePlanMapper.deleteMapperObjectId(k);
            mwNcmTimetaskTimePlanMapper.deleteMapperTimeId(k);
            mwNcmTimetaskTimePlanMapper.deleteNewTimeTask(k);
        }

        return Reply.ok();
    }

    @Override
    public Reply getHistory(Transfer transfer) {
        PageHelper.startPage(transfer.getPageNumber(), transfer.getPageSize());
        List<TimeTaskRresult> n = mwNcmTimetaskTimePlanMapper.getHistory(transfer.getNewtimetaskId());
        NewTimeTask newTimeTask = new NewTimeTask();
        newTimeTask.setId(transfer.getNewtimetaskId());
        NewTimeTask newTimeTasks = mwNcmTimetaskTimePlanMapper.selectTimeAllTask(newTimeTask).get(0);
        for (TimeTaskRresult t : n) {
            t.setActionName(newTimeTasks.getTimeAction()).setActionModel(newTimeTasks.getTimeModel());
            t.setFailReason(t.getResultContext());
            if (t.getObjectId() != null && !t.getObjectId().trim().equals("")) {
                List<String> strings = new ArrayList<>();
                strings.add(t.getObjectId());
                List<Tree> list = getObjectTreeList(newTimeTasks.getTimeModel(), strings, Integer.parseInt(newTimeTasks.getActionId()));
                try {
                    t.setObjectName(list.get(0).getTreeName());
                } catch (Exception e) {
                }
            }
        }
        PageInfo pageInfo = new PageInfo<>(n);
        pageInfo.setList(n);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply timeAllTaskHisDelete(DeleteTimeTask newTimeTask) {
        mwNcmTimetaskTimePlanMapper.timeAllTaskHisDelete(newTimeTask);
        return Reply.ok();
    }

    @Override
    @Async
    public void runNewTime(Transfer transfer) {
        if (enable == false && hasTask == true) {
            String str = LoadUtil.post2(taskTimeUrl.replace(TIME_REFRESH, TIME_RUN_NOW), JSONObject.toJSONString(transfer));
            Integer resCode = JSONObject.parseObject(str).getInteger("rtnCode");
            log.info("刷新定时器执行返回结果 :{}", str);
        } else {
            log.info("刷新定时器中");
            Class<?> clazz;
            NewTimeTask newTimeTask = new NewTimeTask();
            TimetaskActrion timetaskActrions = new TimetaskActrion();

            List<String> objectId = new ArrayList<>();
            if (transfer.getActionId() != -1) {
                if (redisUtils.get("timeTask" + transfer.getActionId()) == null) {
                    try {
                        redisUtils.set("timeTask" + transfer.getActionId(), "false", 3600);
                        timetaskActrions = mwNcmTimetaskTimePlanMapper.getActionById(transfer.getActionId());
                        clazz = Class.forName(timetaskActrions.getActionImpl());
                        String className = lowerFirstCapse(clazz.getSimpleName());
                        Object bean = (Object) ApplicationContextProvider.getBean(className);
                        TimeTaskRresult taskRresult = new TimeTaskRresult();
                        log.info("runNewTime action start");
                        try {
                            taskRresult = (TimeTaskRresult) getMethodReflection(bean, timetaskActrions.getActionMethod(), "", "");
                            log.info(taskRresult.toString());
                        } catch (Exception e) {
                            log.error("runNewTime ERROR:{}", e);
                        }
                    } catch (Exception e) {
                        log.error("runNewTime ERROR:{}", e);
                    }
                }

            } else {
                List<MwNcmTimetaskTimePlanRun> scheduleConfigs = mwNcmTimetaskTimePlanMapper.selectTimePlan(transfer.getNewtimetaskId());
                MwNcmTimetaskTimePlanRun scheduleConfig = scheduleConfigs.get(0);
                objectId = mwNcmTimetaskTimePlanMapper.getObjectId(scheduleConfig.getNewtimetaskId());
                timetaskActrions = mwNcmTimetaskTimePlanMapper.getAction(scheduleConfig.getNewtimetaskId());
                CronSequenceGenerator generator = new CronSequenceGenerator(scheduleConfig.getTimeCron());
                newTimeTask.setTimeStartTime(new Date());
                Date afterTime = generator.next(newTimeTask.getTimeStartTime());
                newTimeTask.setTimeEndTime(afterTime);
                newTimeTask.setId(scheduleConfig.getNewtimetaskId());
                NewTimeTask newTimeTasks = mwNcmTimetaskTimePlanMapper.selectTimeAllTask(newTimeTask).get(0);
                newTimeTasks.setTimeStartTime(new Date());
                newTimeTasks.setTimeEndTime(afterTime);
                try {
                    //组方法
                    clazz = Class.forName(timetaskActrions.getActionImpl());
                    String className = lowerFirstCapse(clazz.getSimpleName());
                    Object bean = (Object) ApplicationContextProvider.getBean(className);
                    List<TimeTaskRresult> objects = new ArrayList<>();
                    log.info("runNewTime timetask start");
                    if (objectId.size() == 0 || objectId == null) {
                        TimeTaskRresult res = new TimeTaskRresult();
                        try {
                            res = (TimeTaskRresult) getMethodReflection(bean, timetaskActrions.getActionMethod(), "", newTimeTasks.getTimeObject());
                        } catch (Exception e) {
                            res.setSuccess(false);
                            res.setFailReason(e.toString());
                        }
                        res.setNewTimetaskId(scheduleConfig.getNewtimetaskId());
                        res.setResultEndDate(res.getStartTime(), new Date());
                        objects.add(res);
                    } else {
                        for (String s : objectId) {
                            TimeTaskRresult res = new TimeTaskRresult();
                            try {
                                res = (TimeTaskRresult) getMethodReflection(bean, timetaskActrions.getActionMethod(), s, newTimeTasks.getTimeObject());
                            } catch (Exception e) {
                                res.setSuccess(false);
                                res.setFailReason(e.toString());
                            }
                            res.setObjectId(s);
                            res.setNewTimetaskId(scheduleConfig.getNewtimetaskId());
                            res.setResultEndDate(res.getStartTime(), new Date());
                            objects.add(res);
                        }
                    }
                    //生成历史记录
                    cleartHis(newTimeTasks, objects);
                } catch (ClassNotFoundException e) {
                    log.error("刷新定时器执行返回结果 :{}", e);
                }
            }


        }
    }


    private void cleartHis(NewTimeTask newTimeTasks, List<TimeTaskRresult> objects) {
        Date now = new Date();
        Long timecont = (now.getTime() - newTimeTasks.getTimeStartTime().getTime()) / 1000;
        newTimeTasks.setTimeCount(timecont.intValue());
        mwNcmTimetaskTimePlanMapper.updateNewTimeTask(newTimeTasks);
        mwNcmTimetaskTimePlanMapper.insertTimeTakHis(objects);
    }

    public String showTxt(String filename) {
        StringBuffer sb = new StringBuffer();
        BufferedReader br = null;
        String str = "";
        try {
            br = new BufferedReader(new FileReader(filename));
            String s = null;
            while ((s = br.readLine()) != null) {
                sb.append(System.lineSeparator() + s);
            }

            String result = sb.toString();
            str = EncryptUtil.decrypt(result);
        } catch (Exception e) {
            log.error("刷新定时器执行返回结果 :{}", e);
            return e.getMessage();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.error("刷新定时器执行返回结果 :{}", e);
            }
        }

        return str;
    }


    private String getCron(AddTimeTaskParam auParam) {
        String timetype = auParam.getTimetype();
        String hms = auParam.getHms();
        String cron = "";
        if ("H".equals(timetype)) {
            String[] hmss = hms.split(":");
            cron = reduceCap(hmss[2]) + " " + reduceCap(hmss[1]) + " " + reduceCap(hmss[0]) + " * * ?";
        } else if ("W".equals(timetype)) {
            String[] hmss = hms.split(":");
            cron = reduceCap(hmss[2]) + " " + reduceCap(hmss[1]) + " " + reduceCap(hmss[0]) + " ? * " + resolveWeek(auParam.getWeek());
        } else if ("M".equals(timetype)) {
            String[] hmss = hms.split(":");
            cron = reduceCap(hmss[2]) + " " + reduceCap(hmss[1]) + " " + reduceCap(hmss[0]) + " " + auParam.getDay() + " * ?";
        } else if ("S".equals(timetype)) {
            cron = auParam.getCron();
        }
        return cron;
    }

    public String resolveWeek(String s) {
        Integer week = Integer.parseInt(s);
        Integer trueWeek = week + 1;
        if (trueWeek == 8) {
            trueWeek = 1;
            return String.valueOf(trueWeek);
        } else {
            return String.valueOf(trueWeek);
        }
    }

    public String reduceCap(String s) {
        char[] c = s.toCharArray();
        if (c[0] == '0') {
            return String.valueOf(c[1]);
        } else {
            return String.valueOf(c);
        }
    }

    public void bkctest() {
        System.err.println("bkcbkcbkc");
        System.err.println(new Date());
    }

    private Object getMethodReflection(Object bean, String actionMethod, String s, String timeObject) {
        Method method = null;
        method = ReflectionUtils.findMethod(bean.getClass(), actionMethod);
        if (method == null) {
            method = ReflectionUtils.findMethod(bean.getClass(), actionMethod, String.class);
        }
        if (method == null) {
            method = ReflectionUtils.findMethod(bean.getClass(), actionMethod, String.class, String.class);
        }

        Object res = new Object();
        Class<?>[] clazz = method.getParameterTypes();
        switch (clazz.length) {
            case 0:
                res = ReflectionUtils.invokeMethod(method, bean);
                break;
            case 1:
                res = ReflectionUtils.invokeMethod(method, bean, s);
                break;
            case 2:
                res = ReflectionUtils.invokeMethod(method, bean, s, timeObject);
                break;
            default:
                break;
        }
        return res;
    }

    public static String lowerFirstCapse(String str) {
        char[] chars = str.toCharArray();
        if (chars.length >= 2) {
            boolean a = Character.isUpperCase(chars[0]);
            if (a) {
                boolean b = Character.isUpperCase(chars[1]);
                if (b) {
                    return String.valueOf(chars);
                }
            }
        }
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 执行首页定时任务
     *
     * @param actionId
     */
    @Async
    @Override
    public void executeScreenTimeTask(Integer actionId) {
        try {
            Transfer transfer = new Transfer();
            transfer.setActionId(actionId);
            log.info("MwTimeTaskServiceImpl{} executeScreenTimeTask()::"+transfer);
            runNewTime(transfer);
        } catch (Throwable e) {
            log.error("MwTimeTaskServiceImpl{} executeScreenTimeTask()", e);
        }
    }

    @Override
    public List<Integer> getTimeTaskActionIds(Integer modelId) {
        List<Integer> actionIds = new ArrayList<>();
        try {
            actionIds = mwTimeTaskDao.selectActionIdsBymodelId(modelId);
        } catch (Throwable e) {
            log.error("MwTimeTaskServiceImpl{} getTimeTaskActionIds()", e);
        }
        return actionIds;
    }
}
