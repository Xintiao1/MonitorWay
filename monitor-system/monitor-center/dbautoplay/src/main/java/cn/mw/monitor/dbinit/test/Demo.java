package cn.mw.monitor.dbinit.test;

import java.util.ResourceBundle;

public class Demo {
    public static void main(String[] args) throws Exception {
        ResourceBundle resource = ResourceBundle.getBundle("application");

        String value = new String(resource.getString("ip").trim().getBytes("ISO-8859-1"), "UTF8");
    }
}
