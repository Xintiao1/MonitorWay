package cn.mw.monitor.model.param;

/*****
 * 资产配置状态
 */
public enum ModelAssetSetState {
    TRUE("ACTIVE", true)
    , FALSE("DISACTIVE",false);

    private String name;
    private boolean enable;

    ModelAssetSetState(String name, boolean enable){
        this.name = name;
        this.enable = enable;
    }

    public boolean isEnable(){
        return this.enable;
    }
}
