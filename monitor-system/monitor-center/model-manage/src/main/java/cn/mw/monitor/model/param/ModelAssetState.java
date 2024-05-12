package cn.mw.monitor.model.param;



/*****
 * 资产状态
 */
public enum ModelAssetState {
    ACTIVE("ACTIVE", true, ModelAssetMonitorState.TRUE)
    , DISACTIVE("DISACTIVE",false, ModelAssetMonitorState.FALSE);

    private String name;
    private boolean enable;
    private ModelAssetMonitorState modelAssetMonitorState;

    ModelAssetState(String name, boolean enable, ModelAssetMonitorState ModelAssetMonitorState){
        this.name = name;
        this.enable = enable;
        this.modelAssetMonitorState = ModelAssetMonitorState;
    }

    public boolean isEnable(){
        return this.enable;
    }

    public ModelAssetMonitorState getModelAssetMonitorState(){
        return this.modelAssetMonitorState;
    }
}
