package cn.mw.plugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pf4j.PluginWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;


/**
 * 服务可管理的所有插件.
 * @author qiyao(1210)
 * @date 2022-03-09
 */
public class NtsPlugin {
    private ApplicationContext mainApplicationContext;
    private AnnotationConfigApplicationContext pluginApplicationContext;
    private PluginWrapper pluginWrapper;
    private Path pluginPath;
    private String pluginId;
    private Map<String, List<Class<?>>> classGroupsMap = new HashMap<>();
    private List<RequestMappingInfo> requestMappingInfos = new ArrayList<>();

    private List<Resource> classResourceList = new ArrayList<>();
    private List<Resource> mapperXmlResourceList = new ArrayList<>();
    private List<Resource> otherFileResourceList = new ArrayList<>();

    public ApplicationContext getMainApplicationContext() {
        return mainApplicationContext;
    }

    public AnnotationConfigApplicationContext getPluginApplicationContext() {
        return pluginApplicationContext;
    }

    public PluginWrapper getPluginWrapper() {
        return pluginWrapper;
    }

    public List<Class<?>> getClassList(String groupKey) {
        List<Class<?>> list  = classGroupsMap.get(groupKey);
        return list == null ? new ArrayList<>() : list;
    }

    public List<Resource> getClassResourceList() {
        return classResourceList;
    }

    public void setClassResourceList(List<Resource> classResourceList) {
        this.classResourceList.addAll(classResourceList);
    }

    public void setMapperXmlResourceList(List<Resource> mapperXmlResourceList) {
        this.mapperXmlResourceList.addAll(mapperXmlResourceList);
    }

    public void addClassList(String groupKey, List<Class<?>> classList) {
        classGroupsMap.put(groupKey, classList);
    }

    public synchronized void addClassToGroup(String groupKey, Class<?> clazz) {
        if (!classGroupsMap.containsKey(groupKey)) {
            List<Class<?>> classes = new ArrayList<>();
            classes.add(clazz);
            classGroupsMap.put(groupKey, classes);
        } else {
            classGroupsMap.get(groupKey).add(clazz);
        }
    }

    public List<Resource> getMapperXmlResourceList() {
        return mapperXmlResourceList;
    }

    public NtsPlugin(PluginWrapper pluginWrapper, ApplicationContext applicationContext) {
        this.mainApplicationContext = applicationContext;
        this.pluginPath = pluginWrapper.getPluginPath();
        this.pluginId = pluginWrapper.getPluginId();
        setPluginWrapper(pluginWrapper);
    }

    public String getPluginState() {
        if (this.pluginWrapper != null) {
            return pluginWrapper.getPluginState().toString();
        }
        return "UNLOAD";
    }

    public void addPluginRequestMapping(RequestMappingInfo requestMappingInfo) {
        requestMappingInfos.add(requestMappingInfo);
    }

    public List<RequestMappingInfo> getRequestMappingInfos() {
        return requestMappingInfos;
    }

    public void unload() {
        requestMappingInfos.clear();
        classGroupsMap.clear();
        classResourceList.clear();
        otherFileResourceList.clear();
        if (null != pluginApplicationContext) {
            pluginApplicationContext.close();
            pluginApplicationContext.clearResourceCaches();
            pluginApplicationContext = null;
        }
        this.pluginWrapper = null;
    }

    public String getPluginId() {
        return pluginId;
    }

    public Path getPluginPath() {
        return pluginPath;
    }

    public void setPluginWrapper(PluginWrapper plugin) {
        this.pluginWrapper = plugin;
        pluginApplicationContext = new AnnotationConfigApplicationContext();
        pluginApplicationContext.setClassLoader(pluginWrapper.getPluginClassLoader());
        pluginApplicationContext.setParent(mainApplicationContext);
    }

    public void clearClassList() {
        classGroupsMap.forEach((k, v) -> v.clear());
        classGroupsMap.clear();
    }
}
