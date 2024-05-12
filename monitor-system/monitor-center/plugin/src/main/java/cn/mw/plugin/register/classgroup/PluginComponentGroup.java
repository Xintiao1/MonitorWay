package cn.mw.plugin.register.classgroup;

/**
 * @author qiyao(1210)
 * @date 2022-03-11
 */
public class PluginComponentGroup extends CompoundClassGroup {

    public PluginComponentGroup() {
        groupList.add(new SpringComponentGroup());
        groupList.add(new PluginGroup());
    }

    @Override
    public String groupId() {
        return "plugin_component";
    }
}
