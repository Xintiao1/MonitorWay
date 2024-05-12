package cn.mw.monitor.util;

public class MWCUtils {
    static{
        //System.out.println("MWCUtils lookup:" + System.mapLibraryName("mwc"));
        System.loadLibrary("mwc");  // 这行是调用动态链接库
    }

    public native static boolean checkServer(String digest);

    public native static boolean getServerDigest();

//    public static void main(String args[]){
//        getServerDigest();
//    }

}
