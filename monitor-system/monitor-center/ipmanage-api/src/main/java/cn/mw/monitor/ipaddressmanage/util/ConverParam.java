package cn.mw.monitor.ipaddressmanage.util;

/**
 * 参数转换
 */
public class ConverParam {

    /*
    驼峰转下划线
     */
    public static String HumpToLine(String var){

        StringBuilder stringBuilder = new StringBuilder(var);
        int temp=0;
        if(!var.contains("_")){
            for (int i = 0; i <var.length() ; i++) {
                if(Character.isUpperCase(var.charAt(i))){
                    stringBuilder.insert(i+temp,"_");
                    temp+=1;
                }
            }
        }
        return stringBuilder.toString();



    }


}
