package cn.mw.plugin.entity;

import org.pf4j.PluginWrapper;

/**
 * @author lijubo(1210)
 */
public class PluginInfo {
    private String pluginPath;
    private String pluginId;
    private String pluginStatus;

    public PluginInfo(PluginWrapper pluginWrapper) {
        this.pluginId = pluginWrapper.getPluginId();
        this.pluginPath = pluginWrapper.getPluginPath().toString();
        this.pluginStatus = pluginWrapper.getPluginState().toString();
    }

    public String getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(String pluginPath) {
        this.pluginPath = pluginPath;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getPluginStatus() {
        return pluginStatus;
    }

    public void setPluginStatus(String pluginStatus) {
        this.pluginStatus = pluginStatus;
    }
}
