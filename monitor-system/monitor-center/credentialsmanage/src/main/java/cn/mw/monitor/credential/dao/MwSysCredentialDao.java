package cn.mw.monitor.credential.dao;

import cn.mw.monitor.credential.common.MwModulesDTO;
import cn.mw.monitor.credential.model.MwSysCredential;
import cn.mw.monitor.credential.util.MapResultHandler;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MwSysCredentialDao {
    int deleteByPrimaryKey(Integer id);

    int insert(MwSysCredential record);

    int insertSelective(MwSysCredential record);

    MwSysCredential selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(MwSysCredential record);

    int updateByPrimaryKey(MwSysCredential record);

    /**
     * 批量获取
     *
     * @param describe 查询参数
     * @return 凭据列表数据
     */
    List<MwSysCredential> select(Map<String, Object> describe);

    /**
     * 获取下拉框数据
     *
     * @param credResultHandler 返回数据
     * @param criteria          查询数据
     */
    void selectCredDropDown(MapResultHandler<String, String> credResultHandler,
                            @Param("map") Map criteria);

    List<MwModulesDTO> selectAllModules();

}