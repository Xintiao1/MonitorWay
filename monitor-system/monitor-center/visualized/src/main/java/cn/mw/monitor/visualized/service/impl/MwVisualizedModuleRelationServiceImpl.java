package cn.mw.monitor.visualized.service.impl;

import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.visualized.dao.MwVisualizedModuleRelationDao;
import cn.mw.monitor.visualized.dto.MwVisualizedModuleRelationDto;
import cn.mw.monitor.visualized.service.MwVisualizedModuleRelationService;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @ClassName MwVisualizedModuleRelationServiceImpl
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/12/5 11:40
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedModuleRelationServiceImpl implements MwVisualizedModuleRelationService {

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Resource
    private MwVisualizedModuleRelationDao visualizedModuleRelationDao;

    @Override
    public Reply addVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto) {
        try {
            //设置创建时间和创建人
            visualizedModuleRelationDto.setCreator(iLoginCacheInfo.getLoginName());
            visualizedModuleRelationDto.setCreateDate(new Date());
            visualizedModuleRelationDao.insertVisualizedModuleRelation(visualizedModuleRelationDto);
            return Reply.ok("新增成功");
        }catch (Throwable e){
            log.error("添加可视化模块关联失败",e);
            return Reply.fail("添加可视化模块关联失败"+e.getMessage());
        }
    }

    @Override
    public Reply editorVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto) {
        try {
            visualizedModuleRelationDto.setModifier(iLoginCacheInfo.getLoginName());
            visualizedModuleRelationDto.setModificationDate(new Date());
            visualizedModuleRelationDao.updateVisualizedModuleRelation(visualizedModuleRelationDto);
            return Reply.ok("修改成功");
        }catch (Throwable e){
            log.error("修改可视化模块关联失败",e);
            return Reply.fail("修改可视化模块关联失败"+e.getMessage());
        }
    }

    @Override
    public Reply selectVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto) {
        try {
            List<MwVisualizedModuleRelationDto> mwVisualizedModuleRelationDtos = visualizedModuleRelationDao.selectVisualizedModuleRelation(visualizedModuleRelationDto);
            return Reply.ok(mwVisualizedModuleRelationDtos);
        }catch (Throwable e){
            log.error("查询可视化模块关联失败",e);
            return Reply.fail("查询可视化模块关联失败"+e.getMessage());
        }
    }

    @Override
    public Reply deleteVisualizedModuleRelation(MwVisualizedModuleRelationDto visualizedModuleRelationDto) {
        try {
            visualizedModuleRelationDao.deleteVisualizedModuleRelation(visualizedModuleRelationDto.getIds());
            return Reply.ok("删除成功");
        }catch (Throwable e){
            log.error("删除可视化模块关联失败",e);
            return Reply.fail("删除可视化模块关联失败"+e.getMessage());
        }
    }
}
