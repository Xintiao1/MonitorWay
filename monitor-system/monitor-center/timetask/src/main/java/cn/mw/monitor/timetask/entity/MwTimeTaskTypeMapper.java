package cn.mw.monitor.timetask.entity;


import lombok.Data;


@Data
public class MwTimeTaskTypeMapper {

    //主键
    private Integer id;

    //类型名称(任务类型下拉框展示名称)
    private String typename;

    //执行方法全称
    private String typemethod;

    //执行方法所在类的全称
    private String typeclass;

    //关联配置下拉框查询方法url
    private String selecturl;

    //关联配置下拉框展示字段
    private String configname;

    //关联配置下拉框保存字段
    private String configid;

}
