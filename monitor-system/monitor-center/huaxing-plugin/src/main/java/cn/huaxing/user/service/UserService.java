package cn.huaxing.user.service;

import cn.huaxing.user.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author guiquanwnag
 * @datetime 2023/8/24
 * @Description 华星用户数据
 */
public interface UserService extends IService<UserInfo> {

    void saveAndUpdateUser();

}
