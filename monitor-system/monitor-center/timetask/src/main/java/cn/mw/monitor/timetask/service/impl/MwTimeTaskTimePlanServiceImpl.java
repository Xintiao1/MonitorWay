package cn.mw.monitor.timetask.service.impl;

import cn.mw.monitor.timetask.dao.MwNcmTimetaskTimePlanMapper;
import cn.mw.monitor.timetask.entity.MwNcmTimetaskTimePlan;
import cn.mw.monitor.timetask.service.MwNcmTimetaskTimePlanService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author lumingming
 * @createTime 28 10:09
 * @description
 */
@Service
public class MwTimeTaskTimePlanServiceImpl extends ServiceImpl<MwNcmTimetaskTimePlanMapper, MwNcmTimetaskTimePlan> implements MwNcmTimetaskTimePlanService {

    @Autowired
    private MwNcmTimetaskTimePlanMapper mwNcmTimetaskTimePlanMapper;
    @Value("${datasource.check}")
    private String DATACHECK;
    public static final String DATEBASEMYSQL = "mysql";
    public static final String DATEBASEORACLE = "oracle";


    @Override
    public List<MwNcmTimetaskTimePlan> listAll() {
        if (DATACHECK.equals(DATEBASEMYSQL)){
            return mwNcmTimetaskTimePlanMapper.selectList(new QueryWrapper<>());
        }
        return mwNcmTimetaskTimePlanMapper.listAll();
    }

    @Override
    public String llls() {
        return mwNcmTimetaskTimePlanMapper.llls();
    }

    @Override
    public void insertTaskTime(MwNcmTimetaskTimePlan param) {
        if (DATACHECK.equals(DATEBASEMYSQL)){
             mwNcmTimetaskTimePlanMapper.insert(param);
        }
        else {
            mwNcmTimetaskTimePlanMapper.insertTaskTime(param);
        }
    }

    @Override
    public void deleteById(Integer id) {
        if (DATACHECK.equals(DATEBASEMYSQL)){
            mwNcmTimetaskTimePlanMapper.deleteById(id);
        }else {
            mwNcmTimetaskTimePlanMapper.deleteByIdMy(id);
        }

    }

    @Override
    public void updateByIdMy(MwNcmTimetaskTimePlan param) {
        if (DATACHECK.equals(DATEBASEMYSQL)){
            mwNcmTimetaskTimePlanMapper.updateById(param);
        }else {
            mwNcmTimetaskTimePlanMapper.updateByIdMy(param);
        }
    }
}
