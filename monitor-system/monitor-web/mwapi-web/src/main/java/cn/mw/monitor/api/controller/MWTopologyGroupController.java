package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.service.scan.param.TopoMetaInfoParam;
import cn.mw.monitor.service.topo.api.MwTopoGroupService;
import cn.mw.monitor.service.topo.model.TopoGroupView;
import cn.mw.monitor.service.topo.param.*;
import cn.mw.monitor.snmp.service.MWTopologyService;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequestMapping("/mwapi")
@Controller
@Api(value = "拓扑分组管理", tags = "拓扑分组管理")
@Slf4j(topic = "MWTopoLogger")
public class MWTopologyGroupController extends BaseApiService {

    @Autowired
    private MwTopoGroupService mwTopoGroupService;

    @Autowired
    private MWTopologyService mwTopologyService;

    @PostMapping("/topology/group/create")
    @ResponseBody
    @ApiOperation(value = "添加分组", tags = "拓扑信息保存")
    public ResponseBase addGroup(@Validated  @RequestBody TopoGroupAddParam topoGroupAddParam) {
        Reply reply = null;
        try {
            reply = mwTopoGroupService.addTopoGroup(topoGroupAddParam);
        }catch (Throwable e){
            log.error("addGroup", e);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology/group/drag/create")
    @ResponseBody
    @ApiOperation(value = "拖动分组", tags = "拖动分组")
    public ResponseBase saveGroup(@RequestBody TopoGroupDragParam topoGroupDragParam) {
        Reply reply = null;
        try {
            reply = mwTopoGroupService.dragTopoGroup(topoGroupDragParam);
        }catch (Throwable e){
            log.error("saveGroup", e);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology/group/delete")
    @ResponseBody
    @ApiOperation(value = "删除拓扑分组", tags = "删除拓扑分组")
    public ResponseBase delGroup(@Validated  @RequestBody TopoGroupDelParam param) {
        Reply reply = null;
        try {
            reply = mwTopoGroupService.deleteTopoGroup(param);
        }catch (Throwable e){
            log.error("delGroup", e);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology/group/browse")
    @ResponseBody
    @ApiOperation(value = "查看拓扑分组列表", tags = "查看拓扑分组列表")
    public ResponseBase listGroup() {
        Reply reply = null;
        try {
            reply = mwTopoGroupService.getTopoGroupView(true);
        }catch (Throwable e){
            log.error("listGroup", e);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology/label/group/browse")
    @ResponseBody
    @ApiOperation(value = "查看拓扑标签分组列表", tags = "查看拓扑标签分组列表")
    public ResponseBase listLabel() {
        Reply reply = null;
        try {
            reply = mwTopoGroupService.getTopoLabelGroupView(true);
        }catch (Throwable e){
            log.error("listLabel", e);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology/department/group/browse")
    @ResponseBody
    @ApiOperation(value = "查看拓扑机构分组列表", tags = "查看拓扑机构分组列表")
    public ResponseBase listDept() {
        Reply reply = null;
        try {
            reply = mwTopoGroupService.getTopoDeptGroupView(true);
        }catch (Throwable e){
            log.error("listLabel", e);
        }
        return setResultSuccess(reply.getData());
    }

    @PostMapping("/topology/copy/create")
    @ResponseBody
    @ApiOperation(value = "拓扑复制", tags = "拓扑复制")
    public ResponseBase copyTopo(@Validated @RequestBody TopoCopyParam param) {
        Reply reply = null;
        try {
            mwTopologyService.copyTopo(param.getId());
            switch (param.getType()) {
                case 1:
                    reply = mwTopoGroupService.getTopoGroupView(true);
                    break;
                case 2:
                    reply = mwTopoGroupService.getTopoLabelGroupView(true);
                    break;
                case 3:
                    reply = mwTopoGroupService.getTopoDeptGroupView(true);
                    break;
                default:
                    reply = mwTopoGroupService.getTopoGroupView(true);
                    break;
            }
        } catch (Throwable e) {
            log.error("copyTopo", e);
        }
        return setResultSuccess(reply.getData());
    }
}
