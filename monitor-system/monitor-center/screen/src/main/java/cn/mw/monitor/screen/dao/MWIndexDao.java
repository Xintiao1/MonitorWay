package cn.mw.monitor.screen.dao;

import cn.mw.monitor.screen.model.IndexBulk;
import cn.mw.monitor.screen.model.IndexModelBase;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xhy
 * @date 2020/9/1 14:48
 */
public interface MWIndexDao {
    int getTodayMessage(List<String> hostIds);

    int getTodaySuccessMessage(List<String> hostIds);

    int getSumMessage(List<String> hostIds);

    int getSumSuccessMessage(List<String> hostIds);

    List<IndexModelBase> getIndexBase();

    List<IndexBulk> selectBulkByUser(Integer userId);
    int selectBulkCount(Integer userId);

    int insertIndexModel(List<IndexModelBase> indexModelBase);

    int deleteIndexModel(Integer bulkId);

    int deleteBulkByUser(@Param("userId") Integer userId);

    List<IndexModelBase> getPageSelectBase();

    List<IndexModelBase> getPageSelectBase2();

    int insertIndexBulk(List<IndexBulk> indexBulk);

    int deleteIndexBulk(@Param("userId") Integer userId,@Param("modelDataId") String modelDataId);

    int updateBulkName(@Param("modelDataId") String modelDataId,@Param("bulKName") String bulkName);

}
