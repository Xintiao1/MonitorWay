package cn.mw.monitor.service.activitiAndMoudle;

import cn.mwpaas.common.model.Reply;

import java.util.Map;

/**
 * @author lumingming
 * @createTime 29 15:05
 * @description
 */
public interface KenwSever {

   Reply creatModelKenwSever(Object instanceParam, Integer type);

   Reply updateModelKenwSever(Object instanceParam,Integer type);

   Reply deleteModelKenwSever(Object instanceParam,Integer type);




}
