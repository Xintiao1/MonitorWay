package cn.mw.monitor.server.dao;

import cn.mw.monitor.server.model.MwBaseComponent;
import cn.mw.monitor.server.param.AddOrUpdateComLayoutParam;
import cn.mw.monitor.server.param.AddOrUpdateComLayoutVersionParam;
import cn.mw.monitor.server.serverdto.NavigationBarDTO;
import cn.mw.monitor.service.server.param.AddNavigationBarByDeleteFlag;
import cn.mw.monitor.service.server.param.QueryNavigationBarParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2021/2/3 14:12
 * @Version 1.0
 */
public interface MwMyMonitorDao {
    List<MwBaseComponent> selectBaseComponents();

    int insert(AddOrUpdateComLayoutParam record);

    int insertComLayoutVersion(AddOrUpdateComLayoutVersionParam record);

    void updateComLayoutVersion(AddOrUpdateComLayoutVersionParam record);

    List<AddOrUpdateComLayoutParam> selectByFilter(@Param("userId") Integer userId,
                                                   @Param("monitorServerId") Integer monitorServerId,
                                                   @Param("templateId") String templateId,
                                                   @Param("defaultFlag") Boolean defaultFlag,
                                                   @Param("navigationBarId") Integer navigationBarId,
                                                   @Param("assetsId") Integer assetsId);

    Integer selectComLayoutVersionCount(Integer comLayoutId);

    List<AddOrUpdateComLayoutParam> selectByFilterByCustom(AddOrUpdateComLayoutParam param);

    int update(AddOrUpdateComLayoutParam record);

    Map selectComLayoutVersion(Integer comLayoutId);

    AddOrUpdateComLayoutParam selectComLayoutByVersion(@Param("comLayoutId") Integer comLayoutId,@Param("version") Integer version);

    void deleteComLayoutByMinVersion(@Param("comLayoutId") Integer comLayoutId,@Param("version") Integer version);

    AddOrUpdateComLayoutParam selectComLayoutDataById(Integer comLayoutId);

    List<NavigationBarDTO> selectNavigationBar(@Param("templateId") String templateId,@Param("assetsId") String assetsId);

    int insertNavigationBar(QueryNavigationBarParam record);

    int insertCustomNavigationBar(QueryNavigationBarParam record);

    void updateCustomNavigationBar(QueryNavigationBarParam record);

    int updateNavigationBar(QueryNavigationBarParam record);

    int updateBycustomNavigationBarId(QueryNavigationBarParam record);

    int deleteNavigationBar(QueryNavigationBarParam record);

    int delete(List<Integer> navigationBarIds);

    int deleteComponentLayoutByCustom(@Param("navigationBarId") Integer navigationBarId,@Param("assetsId") String assetsId);

    int checkByNavigation(QueryNavigationBarParam record);

    void deleteCustomNavigationBar(QueryNavigationBarParam record);

    void deleteCustomNavigationBarByAdd(QueryNavigationBarParam record);

    int checkByCustomNavigation(QueryNavigationBarParam record);

   void insertCustomNavigationByDeleteTemplate(AddNavigationBarByDeleteFlag deleteFlag);

    void insertCustomNavigationByDeleteAssets(AddNavigationBarByDeleteFlag deleteFlag);

    List<AddNavigationBarByDeleteFlag> getAllNavigationBarByDeleteFlag(QueryNavigationBarParam param);
}
