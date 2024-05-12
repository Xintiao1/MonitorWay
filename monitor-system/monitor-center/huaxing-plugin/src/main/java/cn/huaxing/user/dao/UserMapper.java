package cn.huaxing.user.dao;

import cn.huaxing.user.entity.AdServerInfo;
import cn.huaxing.user.entity.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author guiquanwnag
 * @datetime 2023/8/24
 * @Description
 */
@Mapper
public interface UserMapper extends BaseMapper<UserInfo> {

    AdServerInfo selectAdServerInfo();

}
