package cn.mw.monitor.timetask.service;

import cn.mw.monitor.timetask.entity.MwNcmTimetaskTimePlan;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author lumingming
 * @createTime 28 10:06
 * @description
 */
public interface MwNcmTimetaskTimePlanService extends IService<MwNcmTimetaskTimePlan> {

    List<MwNcmTimetaskTimePlan> listAll();
    String llls();

    void insertTaskTime(MwNcmTimetaskTimePlan param);

    void deleteById(Integer id);

    void updateByIdMy(MwNcmTimetaskTimePlan param);
}
