package cn.mw.monitor.activiti.dao;

import cn.mw.monitor.activiti.dto.ProcessDefDTO;
import cn.mw.monitor.activiti.dto.ProcessModuleBindDTO;
import cn.mw.monitor.activiti.param.BindTask;
import cn.mw.monitor.activiti.param.SearchProcessParam;
import cn.mw.monitor.service.user.model.MWUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ProcessDao {
    ProcessModuleBindDTO getProcessModuleBindDTO(Map map);
    void updateProcessModuleBindDTO(ProcessModuleBindDTO processModuleBindDTO);

    void insertProcessDef(ProcessDefDTO processDefDTO);
    void updateProcessDef(ProcessDefDTO processDefDTO);
    ProcessDefDTO getProcessDefDTO(Map map);
    List<ProcessDefDTO> listProcessDefDTO(@Param("map")Map map, @Param("sreachtype")Integer sreachtype);

    //根据模型id查找流程id
    List<String> selectProcessId( @Param("processId") String processId, @Param("modeIds") List<Integer> modeIds, @Param("set") List<Integer> set);

    void deleteProcessAndMoudle(@Param("status") Integer status,@Param("noProcess") String noProcess);

    void insertProcessAndMoudle(@Param("modeId") List<Integer> modeId, @Param("set") List<Integer> set,  @Param("defId")String defId, @Param("id") String id);

    void insertProcessStartActiviti(@Param("moudleId") String moudleId, @Param("processInstanceId") String processInstanceId, @Param("processDefinitionId") String processDefinitionId, @Param("status") Integer status,@Param("oper") Integer oper );

    List<String> selectProcessInstanceKey(@Param("modeId")String moudleId,@Param("operInt") Integer operInt);

    void deleteProcessDef(@Param("status") Integer status,@Param("s") String s);

    void insertProcessMyTask(@Param("processInstanceId")String processInstanceId, @Param("loginName")String loginName);

    List<String> selectTaskProcessId(@Param("loginName") String loginName);

    Integer selectOper(@Param("processInstanceId")String processInstanceId);



    List<String> selectHaveProcess(@Param("loginName") List<String> userLoginName);

    void UpdateCount(@Param("strings")List<String> strings, @Param("i")int i, @Param("type")int type);

    void insertProcessCount(@Param("addstrings")List<String> addstrings, @Param("i")int i);

    Integer selectModel(@Param("s") Integer s, @Param("processInstanceId") String processInstanceId);

    void insetBandTask(@Param("activitiId") String activitiId,@Param("defId")  String defId,@Param("bindTasks") List<BindTask> bindTasks);

    Integer selectNumBind(@Param("moudleId")  Integer moudleId);

    void insertMoudelBind(@Param("processId")  String processId, @Param("moudleId") Integer moudleId);

    Map<String, Object> selectmodelAndprocess(@Param("moudleId")  Integer moudleId);

    BindTask selectStartTask(@Param("activitiId")   String activitiId);

    void candleMoudle(@Param("moudleId")  Integer moudleId);

    void addMoudleLine(@Param("moudleId")   Integer moudleId, @Param("position")  String position);

    String selectPosition(@Param("moudleId")  Integer moudleId);

    List<Map<String, Object>> selectProcessTaskBindModel(@Param("moudleId")String moudleId, @Param("modelId")Integer modelId);

    void createTaskList(@Param("name") String name, @Param("loginName") String loginName, @Param("successful") Integer successful, @Param("modelid") Integer modelid, @Param("instanceId") Integer instanceId, @Param("mouldId") String mouldId, @Param("processInstanceId") String processInstanceId, @Param("type") String type);

    List<Map<String, Object>> proccessListBrowse(@Param("searchParam")SearchProcessParam searchParam);

    List<Map<String, Object>> selectByTaskId(@Param("definitionId")String definitionId,@Param("taskId")String taskId);

    void updateProcessedToTable(@Param("definitionNewId")String processDefinitionId, @Param("definitionId")String processId);

    void updateProcessedToMoudle(@Param("definitionNewId")String processDefinitionId, @Param("instanceKey")String instanceKey,@Param("definitionId")String processId);

    void UpdateTaskList(@Param("processInstanceId")String processInstanceId,@Param("i") int i);

    void insertTaskCompleteNotifier(@Param("notifier") Map<String, List<Integer>> notifier);

    List<MWUser> selectByDefintId(@Param("taskDefinitionKey") String taskDefinitionKey);

    void proccessListDelete(@Param("processId") List<Integer> ids);

    Integer checkMoudelAndModel(@Param("moudleId")String moudleId, @Param("modelId")Integer modelId);
}
