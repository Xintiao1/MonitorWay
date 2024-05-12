package cn.mw.monitor.report.service.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DiskInfoManage {
    private Pattern distUtilizationPattern = Pattern.compile("^\\[.+\\]MW_DISK_UTILIZATION$");
    private Pattern distFreePattern = Pattern.compile("^\\[.+\\]MW_DISK_FREE$");
    private Pattern distTotalPattern = Pattern.compile("^\\[.+\\]MW_DISK_TOTAL$");
    private Pattern distUsedPattern = Pattern.compile("^\\[.+\\]MW_DISK_USED$");
    private Pattern distNamePattern = Pattern.compile("^\\[(.+)\\]");

    public boolean matchDiskUtilization(String data){
        Matcher m = distUtilizationPattern.matcher(data);
        return m.find();
    }

    public boolean matchDiskFree(String data){
        Matcher m = distFreePattern.matcher(data);
        return m.find();
    }

    public boolean matchDiskTotal(String data){
        Matcher m = distTotalPattern.matcher(data);
        return m.find();
    }

    public boolean matchDiskUsed(String data){
        Matcher m = distUsedPattern.matcher(data);
        return m.find();
    }

    public String extractDiskName(String data){
        Matcher m = distNamePattern.matcher(data);
        if(m.find()){
            return m.group(1);
        }
        return "";
    }

    /*
    public static void main(String[] args){
        DiskInfoManage diskInfoManage = new DiskInfoManage();
        //System.out.println(diskInfoManage.extractDiskName("[/sdfsdf]sdfs"));
    }

     */
}
