package cn.mw.monitor.activiti.service.impl;

import cn.mw.monitor.activiti.dao.DutyDao;
import cn.mw.monitor.activiti.entiy.DutyEntity;
import cn.mw.monitor.activiti.entiy.ShiftEntity;
import cn.mw.monitor.activiti.param.DutyManageParam;
import cn.mw.monitor.activiti.param.DutyShiftParam;
import cn.mw.monitor.activiti.param.QueryDutyInfoParam;
import cn.mw.monitor.activiti.service.DutyManageService;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.service.activiti.param.DutyCommonsParam;
import cn.mw.monitor.service.activiti.param.service.DutyManageCommonService;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.BeansUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author
 * @Date
 * @Version
 */
@Service
@Slf4j
public class DutyManageServiceImpl implements DutyManageService, DutyManageCommonService {

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private DutyDao dutyDao;

    @Autowired
    private MWMessageService mwMessageService;

    @Override
    public Reply createDuty(List<DutyManageParam> params) {
        try{
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            if(!iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName()).equals("0")){
                return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, "非管理员用户不能执行该操作！");
            }
            List<DutyEntity> dutyEntities = new ArrayList<>();
            HashSet<Integer> userIds = new HashSet<>();
            for(DutyManageParam param : params){
                DutyEntity temp = new DutyEntity();
                BeansUtils.copyProperties(param,temp);
                temp.setCreateUser(userId);
                temp.setId(UUIDUtils.getUUID());
                dutyEntities.add(temp);
                userIds.add(param.getUserId());
            }
            dutyDao.insertDutyTable(dutyEntities);
            List<MWUser> mwUsers = dutyDao.selectByUserId(userIds);
            mwMessageService.sendFailAlertMessage("您有新的排班，请注意查看！",mwUsers,"新增排班提醒",false,null);
        }catch (Exception e){
            log.error("值班添加失败:{}",e);
            return Reply.fail("添加失败！");
        }
        return Reply.ok("添加成功！");
    }

    @Override
    public Reply queryDuty(DutyManageParam param) {
        try{
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            switch (param.getType()){
                case 1:
                    List<Date> month = getMonth();
                    param.setStartDate(format.format(month.get(0)));
                    param.setEndDate(format.format(month.get(1)));
                    break;
                case 2:
                    Date date = new Date();
                    param.setStartDate(format.format(date));
                    param.setEndDate(format.format(date));
                    break;
            }
            List<QueryDutyInfoParam> result = dutyDao.selectDutyInfo(param);
            return Reply.ok(result);
        }catch (Exception e){
            log.error("值班查询失败:{}",e);
            return Reply.fail("查询失败！");
        }

    }

    @Override
    public Reply showDuty(DutyManageParam param) {
        try {
            Reply reply = queryDuty(param);
            Map<Date,List<QueryDutyInfoParam>> result = new HashMap<>();
            if(reply != null && reply.getRes() == PaasConstant.RES_SUCCESS){
                List<QueryDutyInfoParam> params = (List<QueryDutyInfoParam>)reply.getData();
                if(CollectionUtils.isEmpty(params)) return  Reply.ok();
                result = params.stream().collect(Collectors.groupingBy(QueryDutyInfoParam::getDate));
            }
            return Reply.ok(result);
        }catch (Exception e){
            log.error("值班展示失败:{}",e);
            return Reply.fail("值班展示失败！");
        }

    }

    public static List<Date> getMonth(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH,0);
        int actualMinimum = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        int actualMaximum = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONDAY),actualMinimum,00,00,00);
        Date dataStart = calendar.getTime();
        calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONDAY),actualMaximum,23,59,59);
        Date dateEnd = calendar.getTime();
        List<Date> list=new ArrayList<>();
        list.add(dataStart);
        list.add(dateEnd);
        return list;
    }

    @Override
    public Reply deleteDuty(String id) {
        try {
            //Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            if(!iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName()).equals("0")){
                return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, "非管理员用户不能执行该操作！");
            }
            dutyDao.deleteDuty(id);
            return Reply.ok("删除成功！");
        }catch (Exception e){
            log.error("值班删除失败:{}",e);
            return Reply.fail("删除失败！");
        }
    }

    @Override
    public Reply shiftCreate(DutyShiftParam param) {
        try{
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            if(!iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName()).equals("0")){
                return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, "非管理员用户不能执行该操作！");
            }
            ShiftEntity shiftEntity = new ShiftEntity();
            BeansUtils.copyProperties(param,shiftEntity);
            shiftEntity.setId(UUIDUtils.getUUID());
            shiftEntity.setCreateUser(userId);
            dutyDao.insertShiftTable(shiftEntity);
        }catch (Exception e){
            log.error("班次添加失败:{}",e);
            return Reply.fail("添加失败！");
        }
        return Reply.ok("添加成功！");
    }

    @Override
    public Reply shiftBrowse(DutyShiftParam param) {
        try{
            List<DutyShiftParam> result = dutyDao.selectShiftInfo(param);
            PageList pageList = new PageList();
            List newList = pageList.getList(result, param.getPageNumber(), param.getPageSize());
            PageInfo pageInfo = new PageInfo<>(result);
            pageInfo.setPages(pageList.getPages());
            pageInfo.setPageNum(param.getPageNumber());
            pageInfo.setEndRow(pageList.getEndRow());
            pageInfo.setStartRow(pageList.getStartRow());
            pageInfo.setList(newList);
            return Reply.ok(pageInfo);
        }catch (Exception e){
            log.error("班次查询失败:{}",e);
            return Reply.fail("查询失败！");
        }

    }

    @Override
    public Reply shiftDelete(List<String> ids) {
        try{
            if(!iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName()).equals("0")){
                return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, "非管理员用户不能执行该操作！");
            }
            dutyDao.deleteShiftTable(ids);
            return Reply.ok("删除成功！");
        }catch (Exception e){
            log.error("班次删除失败:{}",e);
            return Reply.fail("删除失败！");
        }

    }

    @Override
    public Reply shiftEditorBefore(String id) {
        try {
            DutyShiftParam result = dutyDao.selectShiftInfoById(id);
            return Reply.ok(result);
        }catch (Exception e){
            log.error("班次编辑前查询失败:{}",e);
            return Reply.fail("班次编辑前查询失败！");
        }

    }

    @Override
    public Reply shiftEditor(DutyShiftParam param) {
        try{
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            if(!iLoginCacheInfo.getRoleId(iLoginCacheInfo.getLoginName()).equals("0")){
                return Reply.fail(ErrorConstant.ALARM_HANDLER_CODE_300004, "非管理员用户不能执行该操作！");
            }
            ShiftEntity entity = new ShiftEntity();
            BeansUtils.copyProperties(param,entity);
            entity.setUpdateUser(userId);
            dutyDao.updateShiftTable(entity);
            return Reply.ok("编辑成功！");
        }catch (Exception e){
            log.error("班次编辑失败:{}",e);
            return Reply.fail("班次编辑失败！");
        }


    }

    @Override
    public Reply dropBrowse(DutyShiftParam param) {
        try{
            List<DutyShiftParam> result = dutyDao.selectShiftInfo(param);
            return Reply.ok(result);
        }catch (Exception e){
            log.error("班次下拉选择失败:{}",e);
            return Reply.fail("班次下拉选择失败！");
        }
    }


    @Override
    public HashSet<Integer> getDutyUserIds(DutyCommonsParam param) {
        HashSet<Integer> result = new HashSet<>();
        result = dutyDao.getDutyUserIds(param);
        return result;
    }
}
