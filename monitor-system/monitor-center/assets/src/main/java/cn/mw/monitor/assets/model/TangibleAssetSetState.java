package cn.mw.monitor.assets.model;

/*****
 * 资产配置状态
 */
public enum TangibleAssetSetState {
    TRUE("ACTIVE", true)
    , FALSE("DISACTIVE",false);

    private String name;
    private boolean enable;

    TangibleAssetSetState(String name, boolean enable){
        this.name = name;
        this.enable = enable;
    }

    public boolean isEnable(){
        return this.enable;
    }
}
