/**
 * 插件 加载和卸载的处理类 接口
 * @author qiyao(1210)
 * @date 2022-03-09
 */
package cn.mw.plugin;

import cn.mw.plugin.entity.PluginInfo;

import java.nio.file.Path;
import java.util.List;

/**
 * @author： wjun_java@163.com
 * @date： 2021/5/12
 * @description：
 * @modifiedBy：
 * @version: 1.0
 */
public interface PluginManagerService {

	/**
	 * 安装插件
	 * @param path
	 * @throws Exception
	 */
	String installSpringPlugin(Path path) throws Exception;

	/**
	 * 启动已安装插件
	 * @param pluginId
	 * @return
	 * @throws Exception
	 */
	boolean startSpringPlugin(String pluginId) throws Exception;

	/**
	 * 停止/卸载插件
	 * @param pluginId
	 * @throws Exception
	 */
	void stopSpringPlugin(String pluginId, boolean unload) throws Exception;

	/**
	 * 插件环境初始化
	 * @throws Exception
	 */
	void initPlugins() throws Exception;

	/**
	 * 获取所有插件列表
	 * @return
	 */
	List<PluginInfo> getInstallSpringPlugins();
}
