package cn.mw.monitor.timetask.service;

import cn.mw.monitor.timetask.entity.MwBaseLineHealthValueDto;
import cn.mw.monitor.timetask.entity.MwBaseLineItemNameDto;
import cn.mw.monitor.timetask.entity.MwBaseLineManageDto;
import cn.mwpaas.common.model.Reply;

/**
 * @ClassName MwBaseLineService
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/4/6 10:31
 * @Version 1.0
 **/
public interface MwBaseLineService {

    //获取基线监控项数据
    Reply getItemName(MwBaseLineItemNameDto baseLineItemNameDto);

    //添加基线数据
    Reply addBaseLineData(MwBaseLineManageDto baseLineManageDto);

    //修改基线数据
    Reply updateBaseLineData(MwBaseLineManageDto baseLineManageDto);

    //删除基线数据
    Reply deleteBaseLineData(MwBaseLineManageDto baseLineManageDto);


    //查询基线数据
    Reply selectBaseLineData(MwBaseLineManageDto baseLineManageDto);


}
