package cn.mw.plugin.pf4j;

import org.pf4j.ClassLoadingStrategy;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qiyao(1210)
 * @date 2022-03-09
 */
public class SpringPluginClassLoader extends PluginClassLoader {
    private static final Logger log = LoggerFactory.getLogger(SpringPluginClassLoader.class);

    private List<String> packagePrefixs = new ArrayList<>();

    public SpringPluginClassLoader(PluginManager pluginManager, PluginDescriptor pluginDescriptor, ClassLoader parent, ClassLoadingStrategy classLoadingStrategy) {
        super(pluginManager, pluginDescriptor, parent, classLoadingStrategy);
        packagePrefixs.add("org.springframework.");
        packagePrefixs.add("org.pf4j.");
        packagePrefixs.add("com.tiansu.nts.plugin.");
        packagePrefixs.add("com.baomidou.mybatisplus.");
        packagePrefixs.add("org.apache.ibatis.");
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        synchronized(this.getClassLoadingLock(className)) {
            if (className.startsWith("java.")) {
                return this.findSystemClass(className);
            } else if (packagePrefixs.stream().anyMatch(className::startsWith)) {
                return this.getParent().loadClass(className);
            } else {
                log.trace("Received request to load class '{}'", className);
                Class<?> loadedClass = this.findLoadedClass(className);
                if (loadedClass != null) {
                    log.trace("Found loaded class '{}'", className);
                    return loadedClass;
                } else {
                    return super.loadClass(className);
                }
            }
        }
    }
}
