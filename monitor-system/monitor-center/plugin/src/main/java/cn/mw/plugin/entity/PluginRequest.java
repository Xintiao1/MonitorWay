package cn.mw.plugin.entity;

/**
 * @author lijubo(1210)
 */
public class PluginRequest {
    private String pluginId;
    private String pluginPath;
    private boolean unload;

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public String getPluginPath() {
        return pluginPath;
    }

    public void setPluginPath(String pluginPath) {
        this.pluginPath = pluginPath;
    }

    public boolean isUnload() {
        return unload;
    }

    public void setUnload(boolean unload) {
        this.unload = unload;
    }
}
