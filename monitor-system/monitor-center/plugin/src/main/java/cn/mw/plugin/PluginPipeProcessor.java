
package cn.mw.plugin;

/**
 * 插件 加载和卸载的处理类 接口
 * @author qiyao(1210)
 * @date 2022-03-09
 */
public interface PluginPipeProcessor {

    /**
     * 插件组件初始化
     * @throws Exception
     */
    void initialize();

    /**
     * 插件组件注册
     * @param ntsPlugin
     * @throws Exception
     */
    void registry(NtsPlugin ntsPlugin ,PluginRegistContext pluginRegistContext) throws Exception;

    /**
     * 插件组件取消注册
     * @param ntsPlugin
     * @throws Exception
     */
    void unRegistry(NtsPlugin ntsPlugin) throws Exception;

}
