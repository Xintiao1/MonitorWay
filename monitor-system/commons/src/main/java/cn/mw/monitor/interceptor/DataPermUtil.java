package cn.mw.monitor.interceptor;

public class DataPermUtil {
    public static ThreadLocal<DataPermissionSql> dataPermissionThreadLocal = new ThreadLocal<DataPermissionSql>();
    public static void startDataPerm(DataPermissionSql dp){
        dataPermissionThreadLocal.set(dp);
    }

    public static DataPermissionSql getDataPerm(){
        return dataPermissionThreadLocal.get();
    }

    public static void remove(){
        dataPermissionThreadLocal.remove();
    }
}
