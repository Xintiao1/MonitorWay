package cn.mw.monitor.service.runtimeCache;

public abstract class CacheData {
    private boolean isFakeData = false;
    public void setFakeData(boolean isFakeData){
        this.isFakeData = isFakeData;
    }

    public boolean checkFakeData(){
        return this.isFakeData;
    }

    public abstract boolean isRemoveAble(long now);
}
