package cn.mw.monitor.api.controller;

import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.activiti.dto.KnowledgeBaseParam;
import cn.mw.monitor.activiti.dto.OneClickParam;
import cn.mw.monitor.activiti.dto.SelectFlowViewParams;
import cn.mw.monitor.activiti.dto.StartVariablesDTO;
import cn.mw.monitor.activiti.service.ActivitiService;
import cn.mw.monitor.activiti.util.ActivitiUtils;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.util.Arrays;
import java.util.List;

/**
 * @author syt
 * @Date 2020/9/21 10:16
 * @Version 1.0
 */
@RequestMapping("/mwapi")
@Slf4j
@Controller
@Api(value = "知识库", tags = "工作流")
public class ActivitiController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + ActivitiController.class.getName());

    @Autowired
    private ActivitiService activitiDemoService;


    @PostMapping("/activiti/proDefList")
    @ResponseBody
    @ApiOperation(value = "流程模板列表")
    public ResponseBase modelList(@RequestBody BaseParam param) {
        // 查询分页
        Reply reply;
        try {
            reply = activitiDemoService.getModelList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/activiti/myWaitingToDo/browse")
    @ResponseBody
    @ApiOperation(value = "我的待办")
    public ResponseBase getActList(@RequestBody BaseParam param) {
        // 查询分页
        Reply reply;
        try {
            reply = activitiDemoService.getActList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @GetMapping("/ws/myWaitingToDo/count/browse")
    @ResponseBody
    @ApiOperation(value = "我的待办数量")
    public ResponseBase getActListCount(@Param("userId") Integer userId, @Param("count") Long count) {
        // 查询分页
        Reply reply;
        try {
            reply = activitiDemoService.countActList(userId, count);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/activiti/myApply/browse")
    @ResponseBody
    @ApiOperation(value = "我的申请")
    public ResponseBase getMyApplyList(@RequestBody BaseParam param) {
        // 查询分页
        Reply reply;
        try {
            reply = activitiDemoService.myApplyList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @GetMapping("/activiti/delete")
    @ResponseBody
    @ApiOperation(value = "一键删除")
    public ResponseBase oneDel(@PathParam(value = "ids") String[] ids) {
        // 查询分页
        Reply reply;
        try {
            List<String> list = Arrays.asList(ids);
            reply = activitiDemoService.batchDel(list);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/activiti/process/create")
    @ResponseBody
    @ApiOperation(value = "启动流程")
    public ResponseBase startProcess(@RequestBody StartVariablesDTO sParam) {
        Reply reply;
        try {
            reply = activitiDemoService.startWorkFlow(sParam.getInstanceKey(), sParam.getVariables());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/activiti/process/delete")
    @ResponseBody
    @ApiOperation(value = "删除流程")
    public ResponseBase deleteProcess(@RequestBody KnowledgeBaseParam dParam) {
        Reply reply;
        try {
            reply = activitiDemoService.recallWorkFlow(dParam.getProcessId(), dParam.getId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/activiti/batchPass")
    @ResponseBody
    @ApiOperation(value = "一键通过/一键驳回")
    public ResponseBase batchPass(@RequestBody OneClickParam param) {
        Reply reply = new Reply<>();
        try {
            if (param != null) {
                reply = activitiDemoService.batchPass(param);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/activiti/redisDataDelete")
    @ResponseBody
    @ApiOperation(value = "删除对应的实时工作流审核已通过数量")
    public ResponseBase deleteRedisByUserId(@RequestBody Integer userId) {
        Reply reply = new Reply<>();
        try {
            if (userId != null) {
                reply = activitiDemoService.deleteRedisPassCountByUserId(userId);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @GetMapping("/activiti/getTaskComments")
    @ResponseBody
    @ApiOperation(value = "获取任务批注")
    public ResponseBase getTaskComments(@PathParam(value = "processId") String processId) {
        Reply reply = new Reply<>();
        try {
            if (processId != null) {
                reply = activitiDemoService.getTaskCommentsByProcessId(processId);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/activiti/getActivitiView/browse")
    @ResponseBody
    @ApiOperation(value = "查看当前流程")
    public ResponseBase getFlowView(@RequestBody SelectFlowViewParams sParam, HttpServletResponse response) {
        Reply reply = new Reply<>();
        try {
            if (sParam.getProcessId() != null && StringUtils.isNotEmpty(sParam.getProcessId())) {
                reply = activitiDemoService.getFlowViewData(sParam.getProcessId());
////                reply = activitiDemoService.getApplyStatus(instanceId);
//                ActivitiUtils.deleteDeployment("d1f8fb67-2a44-11eb-b3bd-005056ae1a17");
//                String s = ActivitiUtils.deploymentProcessDefinition("测试用例3", "process/flow.bpmn20.xml");
//                String process = ActivitiUtils.startProcess("flow", null);
//                InputStream inputStream = ActivitiUtils.viewImage(process, response);
//                String imageName = "image.svg";
//                FileUtils.copyInputStreamToFile(inputStream,new File("D:\\data\\home\\upload\\images\\" + imageName));
//                ////System.out.println();
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/activiti/deploymentProcess/create")
    @ResponseBody
    @ApiOperation(value = "部署流程")
    public ResponseBase deploymentProcess( HttpServletResponse response) {
        Reply reply = new Reply<>();
        try {
                String s = ActivitiUtils.deploymentProcessDefinition("知识提交流程", "process/knowledgeFlowPlus.bpmn20.xml");
                reply = Reply.ok(s);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/activiti/deploymentProcessIP/create")
    @ResponseBody
    @ApiOperation(value = "部署IP分配流程")
    public ResponseBase deploymentProcessIP( HttpServletResponse response) {
        Reply reply = new Reply<>();
        try {
            String s = ActivitiUtils.deploymentProcessDefinition("IP地址分配", "process/IPStatus.bpmn21.xml");
            reply = Reply.ok(s);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("流程模板报错",null);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("流程模板报错",null);
        }
        return setResultSuccess(reply);
    }
}
