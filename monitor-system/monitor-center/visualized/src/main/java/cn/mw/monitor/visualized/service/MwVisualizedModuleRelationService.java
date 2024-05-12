package cn.mw.monitor.visualized.service;

import cn.mw.monitor.visualized.dto.MwVisualizedModuleRelationDto;
import cn.mwpaas.common.model.Reply;

/**
 * @ClassName MwVisualizedModuleRelationService
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/12/5 11:40
 * @Version 1.0
 **/
public interface MwVisualizedModuleRelationService {

    //新增可视化与模块关联关系
    Reply addVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto);

    //修改可视化与模块关联关系
    Reply editorVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto);

    //查询可视化与模块关联关系
    Reply selectVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto);

    //删除可视化与模块关联关系
    Reply deleteVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto);
}
