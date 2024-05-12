package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.MwModelRelationsService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author qzg
 * @date 2022/2/21
 */
@RequestMapping("/mwapi/modelRelation")
@Controller
@Slf4j
@Api(value = "模型关系接口", tags = "模型关系接口")
public class MwModelRelationsController extends BaseApiService {
    @Autowired
    private MwModelRelationsService mwModelRelationsService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/selectAllModel")
    @ResponseBody
    @ApiOperation(value = "获取所有关联模型数据")
    public ResponseBase selectAllModelByRelationsExludeOwn(@RequestBody AddAndUpdateModelRelationParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.selectAllModelByRelationsExludeOwn(param.getOwnModelId());
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectAllModelByRelations{}", e);
            return setResultFail("获取所有关联模型数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/create")
    @ResponseBody
    @ApiOperation(value = "创建模型关系")
    public ResponseBase creatModelRelations(@RequestBody AddAndUpdateModelRelationParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.creatModelRelations(param);
            if(PaasConstant.RES_SUCCESS == reply.getRes()){
                return setResultSuccess(reply);
            }else{
                return setResultWarn(reply);
            }

        } catch (Exception e) {
            log.error("creatModelRelations{}", e);
            return setResultFail("创建模型关系失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation(value = "模型关系展示")
    public ResponseBase showModelRelations(@RequestBody AddAndUpdateModelRelationParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.showModelRelations(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("creatModelRelations{}", e);
            return setResultFail("模型关系展示失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/popup/browse")
    @ResponseBody
    @ApiOperation(value = "模型关系修改查询")
    public ResponseBase selectModelRelationsByModelId(@RequestBody AddAndUpdateModelRelationParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.selectModelRelationsByModelId(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelRelationsByModelId{}", e);
            return setResultFail("模型关系修改查询失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/editor")
    @ResponseBody
    @ApiOperation(value = "模型关系修改")
    public ResponseBase editorModelRelationsByModelId(@RequestBody AddAndUpdateModelRelationParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.editorModelRelationsByModelId(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("editorModelRelationsByModelId{}", e);
            return setResultFail("模型关系修改失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除模型关系")
    public ResponseBase deleteModelRelations(@RequestBody AddAndUpdateModelRelationParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.deleteModelRelations(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("deleteModelRelations{}", e);
            return setResultFail("删除模型关系失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/create")
    @ResponseBody
    @ApiOperation(value = "创建模型关系分组")
    public ResponseBase creatModelRelationsGroup(@RequestBody AddAndUpdateModelRelationGroupParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.creatModelRelationsGroup(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("creatModelRelationsGroup{}", e);
            return setResultFail("创建模型关系分组失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/popup/editor")
    @ResponseBody
    @ApiOperation(value = "修改模型前查询关系分组")
    public ResponseBase modelRelationsGroupByUpdate(@RequestBody AddAndUpdateModelRelationGroupParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.modelRelationsGroupByUpdate(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("modelRelationsGroupByUpdate{}", e);
            return setResultFail("修改模型前查询关系分组失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/editor")
    @ResponseBody
    @ApiOperation(value = "修改模型关系分组")
    public ResponseBase updateModelRelationsGroup(@RequestBody AddAndUpdateModelRelationGroupParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.updateModelRelationsGroup(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("updateModelRelationsGroup{}", e);
            return setResultFail("修改模型关系分组失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/delete")
    @ResponseBody
    @ApiOperation(value = "删除模型关系分组")
    public ResponseBase deleteModelRelationsGroup(@RequestBody DeleteModelRelationGroupParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.deleteModelRelationsGroup(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("deleteModelRelationsGroup{}", e);
            return setResultFail("删除模型关系分组失败","");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/browse")
    @ResponseBody
    @ApiOperation(value = "查询可以添加关系分组")
    public ResponseBase selectGroupList(@RequestBody QueryGroupListParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.selectGroupList(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectGroupList{}", e);
            return setResultFail("查询可添加关系分组", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/relationsGroup/browse")
    @ResponseBody
    @ApiOperation(value = "模型关系下拉数据查询")
    public ResponseBase queryModelRelationGroupBySelect(@RequestBody ModelRelationGroupsParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.queryModelRelationGroupBySelect(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("queryModelRelationGroupBySelect{}", e);
            return setResultFail("模型关系下拉数据查询失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/relationsToPo/insert")
    @ResponseBody
    @ApiOperation(value = "模型实例拓扑新增数据")
    public ResponseBase addInstanceToPo(@RequestBody QueryInstanceRelationToPoParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.addInstanceToPo(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("addInstanceToPo{}", e);
            return setResultFail("模型实例拓扑新增数据失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/relationsToPo/editor")
    @ResponseBody
    @ApiOperation(value = "模型实例拓扑数据查询")
    public ResponseBase viewInstanceToPo(@RequestBody QueryInstanceRelationToPoParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.viewInstanceToPo(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("viewInstanceToPo {}", e);
            return setResultFail("模型实例拓扑数据查询失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/relationsToPo/getRelationNum")
    @ResponseBody
    @ApiOperation(value = "拓扑实例新增数量查询")
    public ResponseBase queryRelationNumInstanceToPo(@RequestBody QueryInstanceRelationToPoParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.queryRelationNumInstanceToPo(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("queryRelationNumInstanceToPo{}", e);
            return setResultFail("拓扑实例新增数量查询失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/relationsToPo/delete")
    @ResponseBody
    @ApiOperation(value = "拓扑实例删除")
    public ResponseBase deleteInstanceToPo(@RequestBody QueryInstanceRelationToPoParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.deleteInstanceToPo(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("deleteInstanceToPo{}", e);
            return setResultFail("拓扑实例删除失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/hideRelationsToPo/save")
    @ResponseBody
    @ApiOperation(value = "拓扑模型隐藏删除")
    public ResponseBase hideModelToPo(@RequestBody QueryInstanceRelationToPoParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.hideModelToPo(param);
            if (reply.getRes() != 0) {
                return setResultFail(reply.getMsg(),"");
            }
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error("hideModelToPo{}", e);
            return setResultFail("拓扑模型删除失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getTOPOModel/browse")
    @ResponseBody
    @ApiOperation(value = "获取已关联的拓扑模型")
    public ResponseBase getTOPOModelByRelation(@RequestBody QueryInstanceRelationToPoParam param) {
        Reply reply;
        try {
            reply = mwModelRelationsService.getTOPOModel(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getTOPOModelByRelation{}", e);
            return setResultFail("获取已关联的拓扑模型失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceView/create")
    @ResponseBody
    @ApiOperation(value = "创建实例视图")
    public ResponseBase createIntanceView(@RequestBody @Validated  AddMwInstanceViewParam param) {
        Reply reply = mwModelRelationsService.insertInstanceView(param);
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceView/editor")
    @ResponseBody
    @ApiOperation(value = "更新实例视图")
    public ResponseBase updIntanceView(@RequestBody @Validated  UpdMwInstanceViewParam param) {
        Reply reply = mwModelRelationsService.updateInstanceView(param);
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceView/delete")
    @ResponseBody
    @ApiOperation(value = "删除实例视图")
    public ResponseBase delIntanceView(@RequestBody @Validated  DelMwInstanceViewParam param) {
        Reply reply = mwModelRelationsService.deleteInstanceViewById(param.getId());
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceView/select")
    @ResponseBody
    @ApiOperation(value = "查看实例视图")
    public ResponseBase findIntanceView(@RequestBody SelMwInstanceViewParam param) {
        Reply reply = mwModelRelationsService.findInstanceViewById(param.getId());
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceView/browse")
    @ResponseBody
    @ApiOperation(value = "查看实例视图列表")
    public ResponseBase findIntanceViewList(@RequestBody SelMwInstanceViewParam param) {
        Reply reply = mwModelRelationsService.findAllInstanceView(param);
        return setResultSuccess(reply);
    }
}
