package cn.mw.monitor.activiti.controller;

import cn.mw.monitor.activiti.entiy.MyProcess;
import cn.mw.monitor.activiti.entiy.MyTaskEntiy;
import cn.mw.monitor.activiti.entiy.MyTaskTable;
import cn.mw.monitor.activiti.entiy.TaskEntiy;
import cn.mw.monitor.activiti.model.ModuleView;
import cn.mw.monitor.activiti.param.*;
import cn.mw.monitor.activiti.service.ActivitiService;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.constant.Constants;
import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.model.param.QueryCustomModelCommonParam;
import cn.mw.monitor.service.model.param.QueryModelGroupParam;
import cn.mw.monitor.service.model.service.MwModelCustomService;
import cn.mw.monitor.user.model.MwModule;
import cn.mw.monitor.user.service.MwModuleService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ljb
 * @Date 2021/10/3 14:41
 * @Version 1.0
 */
@RequestMapping("/mwapi/process")
@Controller
@Api(value = "流程处理api", tags = "流程处理api")
@Slf4j
public class ProcessController extends BaseApiService {

    @Autowired
    private ActivitiService activitiService;

    @Autowired
    private MwModuleService mwModuleService;

    @Autowired
    private ModelServer modelServer;

    @Autowired
    private MwModelCustomService mwModelCustomService;

    /**
     * 创建流程
     */
    @PostMapping("/definition/create")
    @ResponseBody
    @ApiOperation(value = "创建流程")
    public ResponseBase proccessCreate(@RequestBody ProcessParam processParam) {
        Reply reply = null;
        try {
            reply = activitiService.createProcess(processParam);
            if (reply.getRes() == 99998) {
                return setResultFail("流程报错", null);
            }
        } catch (Exception e) {
            log.error("proccessCreate", e);
            return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
        }
        return setResultSuccess(reply);
    }

    /**
     * 流程查询
     */
    @PostMapping("/definition/browse")
    @ResponseBody
    @ApiOperation(value = "流程查询")
    public ResponseBase proccessBrowse(@RequestBody SearchProcessParam searchParam) {
        Reply reply = null;
        try {
            reply = activitiService.searchProcess(searchParam);
        } catch (Exception e) {
            log.error("proccessCreate", e);
            return setResultFail("流程查询", searchParam);
        }
        return setResultSuccess(reply);
    }

    /**
     * 流程编辑
     */
    @PostMapping("/definition/editor")
    @ResponseBody
    @ApiOperation(value = "流程编辑")
    public ResponseBase proccessEdit(@RequestBody ProcessParam processParam) {
        Reply reply = null;
        try {
            reply = activitiService.createProcess(processParam);
            if (reply.getRes() == 99998) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Exception e) {
            log.error("proccessEdit", e);
            return setResultFail("流程更新失败", processParam);
        }
        return setResultSuccess(processParam);
    }


    @PostMapping("/definition/browselist")
    @ResponseBody
    @ApiOperation(value = "流程查询")
    public ResponseBase proccessListBrowse(@RequestBody SearchProcessParam searchParam) {
        Reply reply = null;
        try {
            reply = activitiService.proccessListBrowse(searchParam);
        } catch (Exception e) {
            log.error("proccessCreate", e);
            return setResultFail("流程查询", searchParam);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/definition/browseDelete")
    @ResponseBody
    @ApiOperation(value = "流程删除")
    public ResponseBase browseDelete(@RequestBody SearchProcessParam searchParam) {
        Reply reply = null;
        try {
            reply = activitiService.browseDelete(searchParam);
        } catch (Exception e) {
            log.error("proccessCreate", e);
            return setResultFail("流程查询", searchParam);
        }
        return setResultSuccess(reply);
    }
    /**
     * 流程编辑
     */
    @PostMapping("/definition/editortwo")
    @ResponseBody
    @ApiOperation(value = "流程编辑2")
    public ResponseBase editortwo(@RequestBody ProcessParam processParam) {
        Reply reply = null;
        try {
            reply = activitiService.createProcessTwo(processParam);
            if (reply.getRes() == 99998) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Exception e) {
            log.error("proccessEdit", e);
            return setResultFail("流程更新失败", processParam);
        }
        return setResultSuccess(processParam);
    }


    @PostMapping("/definition/createMoudle")
    @ResponseBody
    @ApiOperation(value = "新增模型实例")
    public ResponseBase createMoudle(@RequestBody ProcessParam processParam) {
        Reply reply = null;
        try {
            reply = activitiService.createMoudle(processParam);
            if (reply.getRes() == 99998) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Exception e) {
            log.error("proccessEdit", e);
            return setResultFail("流程更新失败", processParam);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/definition/candleMoudle")
    @ResponseBody
    @ApiOperation(value = "删除绑定")
    public ResponseBase candleMoudle(@RequestBody ProcessParam processParam) {
        Reply reply = null;
        try {
            reply = activitiService.candleMoudle(processParam);
            if (reply.getRes() == 99998) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Exception e) {
            log.error("proccessEdit", e);
            return setResultFail("流程更新失败", processParam);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/definition/addMoudleLine")
    @ResponseBody
    @ApiOperation(value = "增加模型对应位置")
    public ResponseBase addMoudleLine(@RequestBody ProcessParam processParam) {
        Reply reply = null;
        try {
            reply = activitiService.addMoudleLine(processParam);
            if (reply.getRes() == 99998) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Exception e) {
            log.error("proccessEdit", e);
            return setResultFail("流程更新失败", processParam);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/definition/disMoudle")
    @ResponseBody
    @ApiOperation(value = "某块绑定流程")
    public ResponseBase disMoudle(@RequestBody ProcessParam processParam) {
        Reply reply = null;
        try {
            reply = activitiService.disMoudle(processParam);
            if (reply.getRes() == 99998) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Exception e) {
            log.error("proccessEdit", e);
            return setResultFail("流程更新失败", processParam);
        }
        return setResultSuccess(processParam);
    }

    /**
     * 流程删除
     */
    @PostMapping("/definition/delete")
    @ResponseBody
    @ApiOperation(value = "流程删除")
    public ResponseBase proccessDelete(@RequestBody DeleteParam deleteParam) {
        if (null != deleteParam.getProcessIds() && deleteParam.getProcessIds().size() > 0) {
            activitiService.deleteProcessById(deleteParam);
            return setResultSuccess("删除成功");
        }
        return setResultFail("删除失败", null);
    }

    @PostMapping("/definition/activite")
    @ResponseBody
    @ApiOperation(value = "流程激活/关闭")
    public ResponseBase activite(@RequestBody ActivitiActParam activitiActParam) {

        activitiService.activite(activitiActParam);
        return setResultSuccess("删除成功");
    }


    @PostMapping("/getTask/browses")
    @ResponseBody
    @ApiOperation(value = "代办审批列表")
    public ResponseBase getTasklist(@RequestBody TaskEntiy taskEntiy) {
        Reply reply;
        try {

            reply = activitiService.getTask(taskEntiy);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Throwable e) {
          throw e;

        }
        return setResultSuccess(reply);
    }


    @PostMapping("/getTask/model")
    @ResponseBody
    @ApiOperation(value = "获取模型数据")
    public ResponseBase getTaskModel(@RequestBody QueryCustomModelCommonParam queryCustomModelCommonParam) {
        Reply reply;
        try {

            reply = modelServer.selectModelInstanceFiledList(queryCustomModelCommonParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("获取模型数据失败", null);
            }
        } catch (Throwable e) {
            throw e;

        }
        return setResultSuccess(reply);
    }


    @PostMapping("/getprocess/getProcessInfo")
    @ResponseBody
    @ApiOperation(value = "查看流程图的")
    public ResponseBase getProcessInfo(@RequestBody TaskEntiy taskEntiy) {
        Reply reply;
        try {

            reply = activitiService.getProcessInfo(taskEntiy);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("查看流程图失败", null);
            }
        } catch (Throwable e) {

            return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
        }
        return setResultSuccess(reply);
    }



    @PostMapping("/getTask/complete")
    @ResponseBody
    @ApiOperation(value = "审批节点完成")
    public ResponseBase complete(@RequestBody MyProcess myProcess) {
        Reply reply;
        try {

            reply = activitiService.complete(myProcess);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Exception e) {
            throw e;
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/getTask/completeBatch")
    @ResponseBody
    @ApiOperation(value = "批量删除流程")
    public ResponseBase completeBatch(@RequestBody List<MyProcess> myProcess) {
        try {
            for (MyProcess m:myProcess) {
                activitiService.complete(m);
            }
        } catch (Exception e) {
            throw e;
        }
        return setResultSuccess(Reply.ok());
    }



    @PostMapping("/getTask/checkMyTask")
    @ResponseBody
    @ApiOperation(value = "查看当前流程节点的modelid")
    public ResponseBase checkMyTask(@RequestBody MyProcess myProcess) {
        Reply reply;
        try {

            reply = activitiService.checkMyTask(myProcess);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Exception e) {
            throw e;
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/myTask/table")
    @ResponseBody
    @ApiOperation(value = "模块内开启表单模式")
    public ResponseBase getMyTaskTable(@RequestBody MyTaskTable myTaskTable) {
        Reply reply;
        try {
            reply = activitiService.getMyTaskTable(myTaskTable);
        } catch (Throwable e) {

            return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/getMyTask/browse")
    @ResponseBody
    @ApiOperation(value = "我提交的审批:")
    public ResponseBase getMyTask(@RequestBody MyTaskEntiy taskEntiy) {
        Reply reply;
        try {
            reply = activitiService.getMyTask(taskEntiy);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Throwable e) {

            return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
        }
        return setResultSuccess(reply);
    }




    @PostMapping("/getprocess/browse")
    @ResponseBody
    @ApiOperation(value = "流程历史:")
    public ResponseBase getprocess(@RequestBody MyProcess myProcess) {
        Reply reply;
        try {
            reply = activitiService.getProcess(myProcess);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
            }
        } catch (Throwable e) {

            return setResultFail("流程模块出错，请联系管理员", "代办审批列表");
        }
        return setResultSuccess(reply);
    }


    /**
     * 关联模块下拉框
     */
    @PostMapping("process/modules/browse")
    @ResponseBody
    @ApiOperation(value = "关联模块下拉框")
    public ResponseBase proccessModulesBrowse(@RequestBody ModuleDropDownParam moduleDropDownParam) {

        //目前只返回资源中心的模块
        Map criteria = new HashMap<>();
        String NODE_LIMIT = "216";
        List<ModuleView> ret = new ArrayList<>();
        if (StringUtils.isEmpty(moduleDropDownParam.getNodeProtocol())) {
            if (null == moduleDropDownParam.getNodeId()) {
                criteria.put("id", NODE_LIMIT);
            } else {
                criteria.put("pid", moduleDropDownParam.getNodeId());
            }
            Reply reply = mwModuleService.getModuleInfo(criteria);
            if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                List<MwModule> list = (List<MwModule>) reply.getData();
                list.forEach(data -> {
                    ModuleView moduleView = new ModuleView();
                    moduleView.extractFromMwModule(data);
                    ret.add(moduleView);
                });
            }
            return setResultSuccess(ret);
        }

        //获取模型管理信息
        if (Constants.NODE_PROT_MODEL_MANAGE.equals(moduleDropDownParam.getNodeProtocol())) {
            String modelPrefix = Constants.NODE_PROT_MODEL_MANAGE + ModuleView.ID_SEP;
            QueryModelGroupParam param = new QueryModelGroupParam();
            if (moduleDropDownParam.getNodeId().indexOf(modelPrefix) < 0) {
                param.setModelGroupId(0);
            } else {
                String[] ids = moduleDropDownParam.getNodeId().split(ModuleView.ID_SEP);
                param.setModelGroupId(Integer.parseInt(ids[1]));
            }
            Reply reply = mwModelCustomService.selectModelGroupList(param);
            if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                List<Map> replyData = (List) reply.getData();
                if (null != replyData) {
                    replyData.forEach(data -> {
                        Object nodeId = data.get("modelGroupId");
                        Object nodeName = data.get("modelGroupName");
                        ModuleView moduleView = new ModuleView(nodeId.toString()
                                , nodeName.toString(), Constants.NODE_PROT_MODEL_MANAGE);
                        ret.add(moduleView);
                    });
                }
            }
        }
        return setResultSuccess(ret);
    }
}
