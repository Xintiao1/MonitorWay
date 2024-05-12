package cn.mw.monitor.assets.model;

/*****
 * 资产状态
 */
public enum TangibleAssetState {
    ACTIVE("ACTIVE", true, TangibleAssetMonitorState.TRUE)
    , DISACTIVE("DISACTIVE",false, TangibleAssetMonitorState.FALSE);

    private String name;
    private boolean enable;
    private TangibleAssetMonitorState tangibleAssetMonitorState;

    TangibleAssetState(String name, boolean enable, TangibleAssetMonitorState tangibleAssetMonitorState){
        this.name = name;
        this.enable = enable;
        this.tangibleAssetMonitorState = tangibleAssetMonitorState;
    }

    public boolean isEnable(){
        return this.enable;
    }

    public TangibleAssetMonitorState getTangibleAssetMonitorState(){
        return this.tangibleAssetMonitorState;
    }
}
