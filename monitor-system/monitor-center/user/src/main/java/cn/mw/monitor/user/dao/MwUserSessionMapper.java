package cn.mw.monitor.user.dao;

import cn.mw.monitor.user.model.MwUserSession;
import cn.mw.monitor.user.model.UserSessionDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author swy
 * @since 2023-08-08
 */
public interface MwUserSessionMapper extends BaseMapper<MwUserSession> {


//    @Select("SELECT " +
//            "user_id, " +
//            "user_name, " +
//            "org_id, " +
//            "org_name, " +
//            "create_time, " +
//            "SUM( online_time ) AS TOTAL_ONLINE_TIME  " +
//            "FROM " +
//            "mw_user_session  " +
//            "${ew.customSqlSegment} " +
//            "GROUP BY " +
//            "user_id, " +
//            "user_name, " +
//            "create_time  " +
//            "HAVING  " +
//            "TOTAL_ONLINE_TIME >0 " +
//            "ORDER BY " +
//            "TOTAL_ONLINE_TIME")
//    List<UserSessionDTO> exportUserOnline(@Param(Constants.WRAPPER) Wrapper<MwUserSession> queryWrapper);


//    List<UserSessionDTO> exportUserOnline(@Param(Constants.WRAPPER) Wrapper<MwUserSession> queryWrapper);

    List<UserSessionDTO> exportUserOnline(@Param("userName")String userName,@Param("startTime") Date startTime,@Param("endTime") Date endTime);
}
