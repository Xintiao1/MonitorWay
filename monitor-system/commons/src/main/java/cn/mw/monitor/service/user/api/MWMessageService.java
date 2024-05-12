package cn.mw.monitor.service.user.api;

import cn.mw.monitor.service.user.model.MWUser;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author lvbaoqing
 * @createTime 202110/2929 14:54
 * @description 许可管理接口
 */
public interface MWMessageService {
     void sendActivitiMessage(String text, List<MWUser> users);

    void   createMessage(String text, Integer type, Integer percentScan,MWUser mwUser);

    Reply chageEditor(Integer param);

    void brokenPonint(String s);

    void createBrokenPonint(String toString);

    void sendTimeOutMessage(String text, List<MWUser> users,Boolean isRedirect,Object obj);

    void sendFailAlertMessage(String text, List<MWUser> users,String title, Boolean isRedirect,Object obj);

    /**
     * 发送同步资产名称成功消息提示
     * @param text
     * @param users
     */
    void sendAssetsNameSyncSuccessMessage(String text, List<MWUser> users);

    /**
     * 资产扫描任务完成消息提示
     * @param text
     * @param users
     */
    void sendAssetsScanCompleteMessage(String text, List<MWUser> users,String title);

    /**
     * 虚拟化扫描完成提示
     * @param text
     * @param users
     */
    void sendVirtualDeviceSuccessMessage(String text, List<MWUser> users);
}
