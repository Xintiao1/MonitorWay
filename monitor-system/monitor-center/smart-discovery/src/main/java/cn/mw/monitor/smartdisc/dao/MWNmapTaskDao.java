package cn.mw.monitor.smartdisc.dao;

import ch.qos.logback.core.rolling.helper.IntegerTokenConverter;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.smartdisc.model.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MWNmapTaskDao {

    /*
    * 添加扫描任务
    * */
    Integer insertNmapTask(MWNmapExploreTask task);

    /*
    * 查询新增的id自增序列号
    * */
    Object selectMaxId();

    /*
    * 根据任务名查询任务id
    * */
    Integer selectTaskIdByName(@Param("taskName")String taskName);

    /*
    * 中间表添加数据
    * */
    Integer insertIpService(@Param("ipId") Integer ipId,@Param("ids") List<Integer> serviceIds);

    Integer insertService(MWNmapService service);

    Integer insertIp(MWNmapIp nmapIp);

    Integer insertTaskIp(@Param("taskId") Integer taskId,@Param("ipId") Integer ipId);

    Integer selectMaxServiceId();

    MWNmapExploreTask selectTaskById(Integer taskId);

    List<MWNmapIpService> selectIpService(Integer taskId);

    List<MWNmapExplore> selectList(Map describe);

    Integer updateTask(MWNmapExploreTask task);

    Integer selectTaskByName(@Param("taskName")String taskName);

    MWNmapExploreTask selectNmapTaskByName(@Param("taskName") String taskName);

    Integer delete(List<Integer> idList);

    Integer deleteExceptTask(Integer taskId);

    List<Integer> selectIpByTaskId(Integer taskId);

    List<MWNmapIpService> selectServiceByIpId(Integer ipId);

    List<String> selectLiveIPByTaskId(Integer taskId);

    void insertNonLiveIP(@Param("taskId")Integer taskId,@Param("strIp")String strIp);


/*    Integer deleteTaskIP(List<Integer> idList);

    Integer deleteIpService(List<Integer> idList);

    Integer deleteService(List<Integer> idList);*/
}
