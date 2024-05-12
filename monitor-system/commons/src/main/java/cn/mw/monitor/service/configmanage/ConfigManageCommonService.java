package cn.mw.monitor.service.configmanage;

import java.util.Map;

/**
 * @author gui.quanwang
 * @className ConfigManageService
 * @description 配置服务类
 * @date 2022/6/7
 */
public interface ConfigManageCommonService {

    /**
     * 开关接口
     *
     * @param assetsId      资产ID
     * @param interfaceName 接口名称
     * @param switchState   开关状态（true:打开接口  false：关闭接口）
     * @return true:下发命令成功    false:下发命令失败
     */
    boolean switchInterface(String assetsId, String interfaceName, boolean switchState);

    Map<String, String> getAsstetByid(String assetsId);
}
