package cn.mw.monitor.configmanage.entity;

import lombok.Data;

@Data
public class MwNcmPath {

    //自增序列
    private Integer id;

    //下载配置文件路径
    private String downloadPath;

    //执行脚本文件路径
    private String perfromPath;

    /**
     * 最大保存数量
     */
    private Integer maxCount;

    /**
     * 最久存储时间（单位：天）
     */
    private Integer maxTime;

    /**
     * 生效类别(1:天数 2:数量)
     */
    private Integer validType;

    /**
     * 获取生效条目是否为最大天数
     *
     * @return
     */
    public boolean checkMaxTime() {
        boolean ret;
        if (validType == null) {
            ret = false;
        } else {
            ret = 1 == validType;
        }
        return ret;
    }

    /**
     * 获取生效条目是否为最大数量
     *
     * @return
     */
    public boolean checkMaxCount(){
        return !checkMaxTime();
    }
}
