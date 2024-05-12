package cn.mw.monitor.service.activitiAndMoudle;

import org.apache.poi.ss.formula.functions.T;

import java.util.Map;

/**
 * @author lumingming
 * @createTime 29 15:05
 * @description
 */
public interface ActivitiSever {

   Map<String,Object> OperMoudleContainActiviti(String moduleId,Integer operInt, Object data);

   Map<String,Object> OperMoudleContainActivitiTwo(String moduleId,Integer operInt, Object data,Integer modelId,Map<String,Object> map);
}
