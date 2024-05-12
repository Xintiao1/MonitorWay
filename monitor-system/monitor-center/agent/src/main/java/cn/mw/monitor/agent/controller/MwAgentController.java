package cn.mw.monitor.agent.controller;

import cn.mw.monitor.agent.api.AgentManage;
import cn.mw.monitor.agent.model.AgentView;
import cn.mw.monitor.agent.param.AgentQueryParam;
import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Agent管理
 *
 * @author lijubo
 * @date 2023/03/03
 */

@RequestMapping("/mwapi/agent")
@Controller
@Slf4j
@Api(value = "Agent管理", tags = "Agent管理")
public class MwAgentController extends BaseApiService {

    private AgentManage agentManage;

    public MwAgentController(AgentManage agentManage){
        this.agentManage = agentManage;
    }

    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation(value = "Agent信息查看")
    public ResponseBase addModelAssetsByScanSuccess(@RequestBody AgentQueryParam param) {
        List<AgentView> agentViewList = agentManage.getAgentViews();
        return setResultSuccess(agentViewList);
    }
}
