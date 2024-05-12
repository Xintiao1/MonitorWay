package cn.mw.monitor.activiti.dao;


import cn.mw.monitor.activiti.entiy.DutyEntity;
import cn.mw.monitor.activiti.entiy.ShiftEntity;
import cn.mw.monitor.activiti.param.DutyManageParam;
import cn.mw.monitor.activiti.param.DutyShiftParam;
import cn.mw.monitor.activiti.param.QueryDutyInfoParam;
import cn.mw.monitor.service.activiti.param.DutyCommonsParam;
import cn.mw.monitor.service.user.model.MWUser;
import org.apache.ibatis.annotations.Param;
import java.util.HashSet;
import java.util.List;

public interface DutyDao {

    void insertDutyTable(@Param("list") List<DutyEntity> dutyEntities);

    List<QueryDutyInfoParam> selectDutyInfo(DutyManageParam param);

    HashSet<Integer> getDutyUserIds(DutyCommonsParam param);

    void deleteDuty(@Param("id") String id);

    void insertShiftTable(ShiftEntity param);

    List<MWUser> selectByUserId(@Param("list") HashSet<Integer> list);

    List<DutyShiftParam> selectShiftInfo(DutyShiftParam param);

    DutyShiftParam selectShiftInfoById(@Param("id") String id);

    void updateShiftTable(ShiftEntity param);

    void deleteShiftTable(@Param("list") List<String> ids);

}
