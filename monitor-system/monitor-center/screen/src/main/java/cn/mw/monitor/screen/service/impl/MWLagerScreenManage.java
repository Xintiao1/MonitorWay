package cn.mw.monitor.screen.service.impl;

import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.screen.dao.MWLagerScreenDao;
import cn.mw.monitor.screen.dto.*;
import cn.mw.monitor.screen.param.ModelDataParam;
import cn.mw.monitor.screen.param.MwLagerScreenParam;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.state.DataType;
import com.github.pagehelper.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xhy
 * @date 2020/4/10 16:11
 */
@Component
@Slf4j
public class MWLagerScreenManage {
    @Resource
    private MWLagerScreenDao dao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWCommonService mwCommonService;


    /**
     * @param mwLagerScreenParam
     * @return 创建新的大屏
     */
    @Transactional
    public String addLagerScreen(MwLagerScreenParam mwLagerScreenParam) {
        String screenId = UuidUtil.getUid();
        String layoutDataId = UuidUtil.getUid();
        dao.insertLargeScreenLayoutMapper(screenId, layoutDataId);
        //添加大屏和布局关系的映射
        LayoutDataDto layoutDataDto = LayoutDataDto.builder().screenId(screenId)
                .layoutId(mwLagerScreenParam.getLayoutId())
                .layoutDataId(layoutDataId).build();
        dao.addLayoutData(layoutDataDto);
        //添加布局数据和块i映射表
        int count = dao.getLayoutCount(mwLagerScreenParam.getLayoutId());
        for (int i = 0; i < count; i++) {
            String bulkDataId = UuidUtil.getUid();
            dao.insertLayoutDataBulk(layoutDataId, bulkDataId);
            dao.addBulkdataId(bulkDataId);
        }
        mwLagerScreenParam.setScreenId(screenId);
        String creatName = iLoginCacheInfo.getLoginName();
        mwLagerScreenParam.setCreator(creatName);
        dao.addLagerScreen(mwLagerScreenParam);
        InsertDto screen = InsertDto.builder()
                .groupIds(mwLagerScreenParam.getGroupIds())
                .userIds(mwLagerScreenParam.getUserIds())
                .orgIds(mwLagerScreenParam.getOrgIds())
                .typeId(mwLagerScreenParam.getScreenId())
                .type(DataType.SCREEN.getName())
                .desc(DataType.SCREEN.getDesc()).build();
        //添加负责人
        mwCommonService.addMapperAndPerm(screen);
        return screenId;

    }


    @Transactional
    public String addLagerScreenData(ModelDataParam modelDataParam) {
        String modelDateId = UuidUtil.getUid();
        Boolean openMapName = modelDataParam.getIsOpenMapName();
        if(openMapName == null){
            openMapName = false;
        }
        dao.addModelData(modelDateId, modelDataParam.getModelId(),openMapName);
        dao.updateBulkdata(modelDataParam.getBulkDataId(), modelDateId);
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        //mw_model_base model_id 为31（线路类型）特殊处理，在screen.properties 里配置刷新时间间隔
        if( modelDataParam.getModelId()!=31 && modelDataParam.getModelId()!=32 ){
            dao.insertFilterTimeLag(modelDateId, modelDataParam.getModelId(), 600, userId, DataType.SCREEN.getName());//刷新时间间隔
        }

        // dao.insertLayoutDataBulk(modelDataParam.getLayoutDataId(), modelDataParam.getBulkDataId());
        return modelDateId;
    }

    /**
     * 删除大屏
     *
     * @param screenId
     */
    @Transactional
    public void deleteLagerScreen(String screenId) {
        dao.deleteLagerScreen(screenId);

        //删除负责人和权限
        DeleteDto screen = DeleteDto.builder().typeId(screenId).type(DataType.SCREEN.getName()).build();
        mwCommonService.deleteMapperAndPerm(screen);

        //布局数据id
        String layoutDataId = dao.selectLayoutDataId(screenId);
        if (null != layoutDataId && StringUtil.isNotEmpty(layoutDataId)) {
            //删除布局数据
            dao.deleteLayoutData(layoutDataId);
            dao.deleteLayoutDataBulkMapper(layoutDataId);
            dao.deleteScreenLayoutMapper(layoutDataId);
            List<String> bulkDataIds = dao.selectBulkDataId(layoutDataId);
            if (null != bulkDataIds && bulkDataIds.size() > 0) {
                //组件数据id
                List<String> modelDataIds = dao.selectModelData(bulkDataIds);
                //删除块数据
                dao.deleteBulkData(bulkDataIds);
                //删除组件数据
                dao.deleteModelData(modelDataIds);
                //删除组件数据对应的过滤数据
                dao.deleteAssetsFilters(modelDataIds);
            }

        }
    }

    //修改大屏
    @Transactional
    public void updateLagerScreen(MwLagerScreenParam mwLagerScreenParam) {
        String screenId = mwLagerScreenParam.getScreenId();
        mwLagerScreenParam.setModifier(iLoginCacheInfo.getLoginName());
        dao.updateLagerScreen(mwLagerScreenParam);
        //先删除后添加
        dao.deleteScreenUser(screenId);
        dao.deleteScreenGroup(screenId);
        dao.deleteScreenOrg(screenId);

        InsertDto screen = InsertDto.builder()
                .groupIds(mwLagerScreenParam.getGroupIds())
                .userIds(mwLagerScreenParam.getUserIds())
                .orgIds(mwLagerScreenParam.getOrgIds())
                .typeId(mwLagerScreenParam.getScreenId())
                .type(DataType.SCREEN.getName())
                .desc(DataType.SCREEN.getDesc()).build();

        //添加负责人
        mwCommonService.addMapperAndPerm(screen);
    }

}
