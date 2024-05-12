package cn.mw.monitor.user.service;

import cn.mw.monitor.api.param.user.ExportUserOnlineParam;
import cn.mw.monitor.service.user.dto.LoginInfo;
import cn.mw.monitor.user.model.MwUserSession;
import cn.mwpaas.common.model.Reply;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author swy
 * @since 2023-08-08
 */
public interface MwUserSessionService extends IService<MwUserSession> {

    Integer saveUserSession(LoginInfo loginInfo);

    void saveLogoutTime(Integer sessionId);

    void timeOutLogout(Integer sessionId);

    void exportUserOnline(ExportUserOnlineParam param,HttpServletResponse response);

    Reply queryPage(ExportUserOnlineParam param);
}
