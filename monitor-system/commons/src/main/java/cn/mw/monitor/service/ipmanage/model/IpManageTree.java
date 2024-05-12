package cn.mw.monitor.service.ipmanage.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lumingming
 * @createTime 2023313 4:05
 * @description
 */
@Data
public class IpManageTree {
    public  Integer id;
    public Integer parentId;
    public String type;
    public String label;
    //分组
    public List<String> labels = new ArrayList<>();
    public List<Integer> parentIds =  new ArrayList<>();
    //分配次数
    public Integer countDis=0;
    //变更次数
    public Integer countCha=0;
    //回收次数
    public Integer countCle=0;
    //使用率
    public Double statusPrecent;
    public Integer countIpadresses;
    public Integer gtEight=0;
    public Integer ltFri=0;
    public Integer gtFri=0;


}
