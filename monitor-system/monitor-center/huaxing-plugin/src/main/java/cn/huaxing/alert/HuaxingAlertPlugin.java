package cn.huaxing.alert;

import cn.huaxing.user.entity.UserInfo;
import cn.huaxing.user.service.UserService;
import cn.mw.monitor.plugin.alert.AlertPlugin;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HuaxingAlertPlugin implements AlertPlugin {

    @Autowired
    private UserService userService;


    @Override
    public List<String> getUserNameByQyWechatId(String[] wechatIdLits) {
        if (wechatIdLits == null || wechatIdLits.length == 0) {
            return null;
        }
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.in("WECHAT_ID", wechatIdLits);
        wrapper.eq("DELETE_FLAG", false);
        List<UserInfo> list = userService.list(wrapper);
        return list.stream().map(UserInfo::getLoginName).collect(Collectors.toList());
    }
}
