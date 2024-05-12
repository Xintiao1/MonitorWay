package cn.mw.monitor.screen.service;

import cn.mw.monitor.screen.dto.LargeScreenMapDto;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @ClassName MWLargeScreenMapService
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/9/2 8:47
 * @Version 1.0
 **/
public interface MWLargeScreenMapService {

    //获取地图选择信息
    Reply getScreenMapChoiceInformation();

    //创建地图展示信息
    Reply createScreenMapShowInformation(LargeScreenMapDto screenMapDto);


    //查询地图展示信息
    Reply selectScreenMapShowInformation(LargeScreenMapDto screenMapDto);

    //修改地图展示信息
    Reply updateScreenMapShowInformation(LargeScreenMapDto screenMapDto);

    //删除地图展示信息
    Reply deleteScreenMapShowInformation(LargeScreenMapDto screenMapDto);

    //查询监控项信息
    Reply getScreenItemName();
}
