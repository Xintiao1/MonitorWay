package cn.mw.monitor.service.activiti.param;

/**
 * @author lumingming
 * @createTime 29 15:22
 * @description
 */

public enum ActivitiOper {
    ADD(0)//新增
    ,DELETE(1) //删除
    ,UPDATE(2)//修改
    ,ALL(3);//全操作

    private int type;

    ActivitiOper(int type){
        this.type = type;
    }
}
