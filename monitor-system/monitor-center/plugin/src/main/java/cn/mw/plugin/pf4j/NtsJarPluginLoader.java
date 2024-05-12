package cn.mw.plugin.pf4j;

import org.pf4j.ClassLoadingStrategy;
import org.pf4j.JarPluginLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;

import java.nio.file.Path;

/**
 * @author qiyao(1210)
 * @date 2022-03-09
 */
public class NtsJarPluginLoader extends JarPluginLoader {
    public NtsJarPluginLoader(PluginManager pluginManager) {
        super(pluginManager);
    }

    @Override
    public ClassLoader loadPlugin(Path pluginPath, PluginDescriptor pluginDescriptor) {
        SpringPluginClassLoader pluginClassLoader = new SpringPluginClassLoader(this.pluginManager, pluginDescriptor, this.getClass().getClassLoader(), ClassLoadingStrategy.APD);
        pluginClassLoader.addFile(pluginPath.toFile());
        return pluginClassLoader;
    }
}
