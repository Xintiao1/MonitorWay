package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.model.exception.ModelManagerException;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.MwModelManageService;
import cn.mw.monitor.model.service.impl.MwModelTransfer;
import cn.mw.monitor.service.model.param.AddAndUpdateModelPropertiesParam;
import cn.mw.monitor.service.model.service.MwModelManageCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.List;

/**
 * @author xhy
 * @date 2021/2/5 14:14
 */
@RequestMapping("/mwapi/modelmanage")
@Controller
@Slf4j
@Api(value = "模型管理接口", tags = "模型管理接口")
public class MwModelManageController extends BaseApiService {
    private static final Logger mwlogger = LoggerFactory.getLogger("MWDBLogger");

    @Autowired
    private MwModelManageService mwModelManageService;

    @Autowired
    private MwModelManageCommonService mwModelManageCommonService;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Value("${System.isFlag}")
    private Boolean isFlag;
    @Value("${fuzzyQuery.isShow}")
    private Boolean isShow;


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getUUIDByModel")
    @ResponseBody
    @ApiOperation(value = "创建一个UUID")
    public ResponseBase getUUIDByModel() {
        try {
            String UUID = UUIDUtils.getUUID();
            String index = "mw";
            String id = index + "_" + UUID;
            return setResultSuccess(id);
        } catch (Exception e) {
            log.error("getUUIDByModel{}", e);
            return setResultFail("创建UUID失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getMaxModelSort")
    @ResponseBody
    @ApiOperation(value = "获取最大modelSort")
    public ResponseBase getMaxModelSort(@RequestBody ModelParam modelParam) {
        try {
            int modelSort = mwModelManageService.getModelMaxSort(modelParam);
            return setResultSuccess(modelSort);
        } catch (Exception e) {
            log.error("getMaxModelSort{}", e);
            return setResultFail("getMaxModelSort失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getFuzzyQueryIsShow")
    @ResponseBody
    @ApiOperation(value = "模糊匹配数据是否显示")
    public ResponseBase getFuzzyQueryIsShow() {
        try {
            return setResultSuccess(isShow);
        } catch (Exception e) {
            log.error("getFuzzyQueryIsShow{}", e);
            return setResultFail("模糊匹配数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/popup/create")
    @ResponseBody
    @ApiOperation(value = "创建模型")
    public ResponseBase creatModel(@RequestBody AddAndUpdateModelParam addAndUpdateModelParam) {
        Reply reply;
        try {
            //添加操作操作日志
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.CREATE_MODEL.getName())
                    .objName(addAndUpdateModelParam.getModelName()).operateDes(OperationTypeEnum.CREATE_MODEL.getName() + ":" + addAndUpdateModelParam.getModelName()).type("model").build();
            mwlogger.info(JSON.toJSONString(builder));
            reply = mwModelManageService.creatModel(addAndUpdateModelParam);
            return setResultSuccess(reply);
        } catch (ModelManagerException e) {
            log.error("模型资源ID已经存在,请修改模型资源ID再提交", e);
            return setResultFail("创建模型", addAndUpdateModelParam);
        } catch (IOException e) {
            log.error("创建模型失败[es连接异常]{}", e);
            return setResultFail("创建模型失败[es连接异常]", addAndUpdateModelParam);
        } catch (Exception e) {
            log.error("创建模型失败{}", e);
            return setResultFail("创建模型失败", addAndUpdateModelParam);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/popup/editor")
    @ResponseBody
    @ApiOperation(value = "修改模型")
    public ResponseBase updateModel(@RequestBody AddAndUpdateModelParam addAndUpdateModelParam) {
        Reply reply;
        try {
            //添加操作操作日志
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.EDITOR_MODEL.getName())
                    .objName(addAndUpdateModelParam.getModelName()).operateDes(OperationTypeEnum.EDITOR_MODEL.getName() + ":" + addAndUpdateModelParam.getModelName()).type("model").build();
            mwlogger.info(JSON.toJSONString(builder));
            reply = mwModelManageService.updateModel(addAndUpdateModelParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateModel{}", e);
            return setResultFail("修改模型失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/delete")
    @ResponseBody
    @ApiOperation(value = "删除模型")
    public ResponseBase deleteModel(@RequestBody AddAndUpdateModelParam addAndUpdateModelParam) {
        Reply reply;
        try {
            //添加操作操作日志
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.DELETE_MODEL.getName())
                    .objName(addAndUpdateModelParam.getModelName()).operateDes(OperationTypeEnum.DELETE_MODEL.getName() + ":" + addAndUpdateModelParam.getModelName()).type("model").build();
            mwlogger.info(JSON.toJSONString(builder));
            reply = mwModelManageService.deleteModel(addAndUpdateModelParam, true);
            return setResultSuccess(reply);
        } catch (ModelManagerException e) {
            log.error("删除模型失败{}", e);
            return setResultFail("删除模型", addAndUpdateModelParam);
        } catch (IOException e) {
            log.error("删除模型失败[es服务连接异常]", e);
            return setResultSuccess("删除模型成功");
        } catch (Exception e) {
            if (e.getMessage().indexOf("Elasticsearch exception [type=index_not_found_exception") != -1) {
                log.error("删除模型失败[模型资源ID不存在]", e);
                return setResultSuccess("删除模型成功");
            }
            log.error("删除模型失败{}", e);
            return setResultFail("删除模型失败", addAndUpdateModelParam);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型")
    public ResponseBase selectModelList(@RequestBody ModelParam modelParam) {
        //点击模型分类子节点，应该把父Id也传过来，可以防止页面显示脏数据。
        Reply reply;
        try {
            reply = mwModelManageService.selectModelList(modelParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("selectModelList{}", e);
            return setResultFail("查询模型失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/parentModel/browse")
    @ResponseBody
    @ApiOperation(value = "父模型接口查询")
    public ResponseBase queryParentModelInfo() {
        Reply reply;
        try {
            reply = mwModelManageService.queryParentModelInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("queryParentModelInfo{}", e);
            return setResultFail("父模型接口查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/ordinaryModel/browse")
    @ResponseBody
    @ApiOperation(value = "普通模型接口查询")
    public ResponseBase queryOrdinaryModelInfo(@RequestBody AddAndUpdateModelGroupParam groupParam) {
        Reply reply;
        try {
            reply = mwModelManageService.queryOrdinaryModelInfo(groupParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("queryOrdinaryModelInfo{}", e);
            return setResultFail("普通模型接口查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/selectOrdinaryModel")
    @ResponseBody
    @ApiOperation(value = "数据关联使用 模型信息查询")
    public ResponseBase selectOrdinaryModel(@RequestBody RelationModelDataParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.selectOrdinaryModel(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("selectOrdinaryModel{}", e);
            return setResultFail("数据关联信息查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/properties/getPropertiesInfo")
    @ResponseBody
    @ApiOperation(value = "根据模型Index查询模型属性，外部关联时使用")
    public ResponseBase getPropertiesInfoByModelId(@RequestBody ModelParam modelParam) {
        Reply reply;
        try {
            reply = mwModelManageService.getPropertiesInfoByModelId(modelParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getPropertiesInfoByModelId{}", e);
            return setResultFail("模型Index查询模型属性失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/create")
    @ResponseBody
    @ApiOperation(value = "创建模型分类")
    public ResponseBase creatModelGroup(@RequestBody AddAndUpdateModelGroupParam groupParam) {
        Reply reply;
        try {
            //添加操作操作日志
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.CREATE_MODEL_GROUP.getName())
                    .objName(groupParam.getModelGroupName()).operateDes(OperationTypeEnum.CREATE_MODEL_GROUP.getName() + ":" + groupParam.getModelGroupName()).type("model").build();
            mwlogger.info(JSON.toJSONString(builder));
            reply = mwModelManageService.creatModelGroup(groupParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("creatModelGroup{}", e);
            return setResultFail("创建模型分类失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/editor")
    @ResponseBody
    @ApiOperation(value = "修改模型分类")
    public ResponseBase updateModelGroup(@RequestBody AddAndUpdateModelGroupParam groupParam) {
        Reply reply;
        try {
            //添加操作操作日志
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.EDITOR_MODEL_GROUP.getName())
                    .objName(groupParam.getModelGroupName()).operateDes(OperationTypeEnum.EDITOR_MODEL_GROUP.getName() + ":" + groupParam.getModelGroupName()).type("model").build();
            mwlogger.info(JSON.toJSONString(builder));
            reply = mwModelManageService.updateModelGroup(groupParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateModelGroup{}", e);
            return setResultFail("修改模型分类失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/popup/editor")
    @ResponseBody
    @ApiOperation(value = "模型分类修改查询")
    public ResponseBase queryModelGroupById(@RequestBody AddAndUpdateModelGroupParam groupParam) {
        Reply reply;
        try {
            reply = mwModelManageService.queryModelGroupById(groupParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("queryModelGroupById{}", e);
            return setResultFail("模型分类修改查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/getAll/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有模型分类")
    public ResponseBase getAllModelGroupInfo() {
        Reply reply;
        try {
            reply = mwModelManageCommonService.getAllModelGroupInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAllModelGroupInfo{}", e);
            return setResultFail("获取所有模型分类失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/delete")
    @ResponseBody
    @ApiOperation(value = "删除模型分类")
    public ResponseBase deleteModelGroup(@RequestBody DeleteModelGroupParam deleteModelGroupParam) {
        Reply reply;
        try {
            //添加操作操作日志
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.DELETE_MODEL_GROUP.getName())
                    .objName(deleteModelGroupParam.getModelGroupName()).operateDes(OperationTypeEnum.DELETE_MODEL_GROUP.getName() + ":" + deleteModelGroupParam.getModelGroupName()).type("model").build();
            mwlogger.info(JSON.toJSONString(builder));
            reply = mwModelManageService.deleteModelGroup(deleteModelGroupParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteModelGroup{}", e);
            return setResultFail("删除模型分类失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/group/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型分类,树结构,最多是两层结构")
    public ResponseBase selectModelTypeListTree(@RequestBody QueryModelTypeParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.selectModelTypeListTree(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("selectModelTypeListTree{}", e);
            return setResultFail("查询模型分类树结构失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/modelListInfo/browse")
    @ResponseBody
    @ApiOperation(value = "查询结构体模型导入数据")
    public ResponseBase queryModelListInfo() {
        Reply reply;
        try {
            reply = mwModelManageService.queryModelListInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("queryModelListInfo{}", e);
            return setResultFail("查询结构体模型导入数据失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/properties/create")
    @ResponseBody
    @ApiOperation(value = "创建模型属性")
    public ResponseBase creatModelProperties(@RequestBody @Validated AddAndUpdateModelPropertiesParam propertiesParam) {
        Reply reply;
        try {
            //添加操作操作日志
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.CREATE_PROPERTIES.getName())
                    .objName(propertiesParam.getModelName()).operateDes(OperationTypeEnum.CREATE_PROPERTIES.getName() + ":" + propertiesParam.getPropertiesName()).type("model").build();
            mwlogger.info(JSON.toJSONString(builder));
            reply = mwModelManageService.creatModelProperties(propertiesParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("creatModelProperties{}", e);
            return setResultFail("创建模型属性失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/properties/editor")
    @ResponseBody
    @ApiOperation(value = "修改模型属性")
    public ResponseBase updateModelProperties(@RequestBody AddAndUpdateModelPropertiesParam propertiesParam) {
        Reply reply;
        try {
            //添加操作操作日志
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.EDITOR_PROPERTIES.getName())
                    .objName(propertiesParam.getModelName()).operateDes(OperationTypeEnum.EDITOR_PROPERTIES.getName() + ":" + propertiesParam.getPropertiesName()).type("model").build();
            mwlogger.info(JSON.toJSONString(builder));
            reply = mwModelManageService.updateModelProperties(propertiesParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateModelProperties{}", e);
            return setResultFail("修改模型属性失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/properties/insertFields")
    @ResponseBody
    @ApiOperation(value = "修改所有模型属性，插入功能模块字段")
    public ResponseBase updateAllModelProperties() {
        Reply reply;
        try {
            reply = mwModelManageService.updateAllModelProperties();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateAllModelProperties{}", e);
            return setResultFail("修改所有模型属性失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/updateModelProperties/editor")
    @ResponseBody
    @ApiOperation(value = "根据模型分组插入机房机柜对应字段")
    public ResponseBase updateModelPropertiesByGroup(@RequestBody AddAndUpdateModelGroupParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.updateModelPropertiesByGroup(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("updateModelPropertiesByGroup{}", e);
            return setResultFail("修改机房机柜布局数据失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/properties/delete")
    @ResponseBody
    @ApiOperation(value = "删除模型属性")
    public ResponseBase deleteModelProperties(@RequestBody List<AddAndUpdateModelPropertiesParam> propertiesParamList) {
        Reply reply;
        try {
            //添加操作操作日志
            String modelName = "";
            String propertiesNames = "";
            if (propertiesParamList != null && propertiesParamList.size() > 0) {
                modelName = propertiesParamList.get(0).getModelName();
                for (AddAndUpdateModelPropertiesParam param : propertiesParamList) {
                    propertiesNames += param.getPropertiesName() + ",";
                }
            }
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.DELETE_PROPERTIES.getName())
                    .objName(modelName).operateDes(OperationTypeEnum.DELETE_PROPERTIES.getName() + ":" + propertiesNames).type("model").build();
            mwlogger.info(JSON.toJSONString(builder));
            reply = mwModelManageService.deleteModelProperties(propertiesParamList);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteModelProperties{}", e);
            return setResultFail("删除模型属性失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/propertiesGanged/browse")
    @ResponseBody
    @ApiOperation(value = "模型属性字段联动显示查询")
    public ResponseBase queryPropertiesGanged(@RequestBody QueryModelGangedFieldParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.queryPropertiesGanged(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("queryPropertiesGanged{}", e);
            return setResultFail("模型属性字段联动显示查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/propertiesFieldInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取上级联动字段属性")
    public ResponseBase getPropertiesFieldByGanged(@RequestBody AddAndUpdateModelPropertiesParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.getPropertiesFieldByGanged(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getPropertiesFieldByGanged{}", e);
            return setResultFail("获取上级联动字段属性失败", "");
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/propertiesShowStatus/editor")
    @ResponseBody
    @ApiOperation(value = "模型属性显示修改")
    public ResponseBase updateModelPropertiesShowStatus(@RequestBody EditorPropertiesNewParam propertiesParam) {
        Reply reply;
        try {
            if (propertiesParam.getPropertiesNameList() != null) {
                //添加操作操作日志
                SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName(OperationTypeEnum.EDITOR_PROPERTIES.getName())
                        .objName(propertiesParam.getModelName()).operateDes(OperationTypeEnum.EDITOR_PROPERTIES.getName() + ":" + JSON.toJSONString(propertiesParam.getPropertiesNameList())).type("model").build();
                mwlogger.info(JSON.toJSONString(builder));
            }
            reply = mwModelManageService.updateModelPropertiesShowStatus(propertiesParam);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("updateModelPropertiesShowStatus{}", e);
            return setResultFail("模型属性显示修改", propertiesParam);
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/propertiesByManage/browse")
    @ResponseBody
    @ApiOperation(value = "纳管属性查询")
    public ResponseBase getPropertiesByManage(@RequestBody ModelParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.getPropertiesByManage(param.getModelId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getPropertiesByManage{}", e);
            return setResultFail("纳管属性查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/propertiesByBaseShow/browse")
    @ResponseBody
    @ApiOperation(value = "纳管属性查询")
    public ResponseBase getPropertiesByBaseShow(@RequestBody ModelParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.getPropertiesByBaseShow(param.getShowType(), param.getModelId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getPropertiesByBaseShow{}", e);
            return setResultFail("纳管属性查询失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceFiledIdBySecond/browse")
    @ResponseBody
    @ApiOperation(value = "查询资产实例新增第二阶段属性id")
    public ResponseBase selectModelInstanceFiledBySecond(@RequestBody ModelParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.selectModelInstanceFiledBySecond(param.getModelId());
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelInstanceFiledBySecond{}", e);
            return setResultFail("查询资产实例属性id", param);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/properties/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型属性")
    public ResponseBase selectModelPropertiesList(@RequestBody ModelParam modelParam) {
        Reply reply;
        try {
            reply = mwModelManageService.selectModelPropertiesList(modelParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("selectModelPropertiesList{}", e);
            return setResultFail("查询模型属性失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/propertiesType/editor")
    @ResponseBody
    @ApiOperation(value = "修改模型属性分类")
    public ResponseBase editorModelPropertiesType(@RequestBody AddAndUpdateModelPropertiesParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.editorModelPropertiesType(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("editorModelPropertiesType{}", e);
            return setResultFail("修改模型属性分类失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/propertiesTypeList/browse")
    @ResponseBody
    @ApiOperation(value = "获取模型属性分类列表")
    public ResponseBase queryPropertiesTypeList(@RequestBody AddAndUpdateModelPropertiesParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.queryPropertiesTypeList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("queryPropertiesTypeList{}", e);
            return setResultFail("获取模型属性分类列表失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/relations/group/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型关系分组")
    public ResponseBase selectModelRelationsGroup(@RequestBody SelectRelationParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.selectModelRelationsGroup(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("selectModelRelationsGroup{}", e);
            return setResultFail("查询模型关系分组失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/propertiesSort/editor")
    @ResponseBody
    @ApiOperation(value = "修改属性排序")
    public ResponseBase editorPropertiesSort(@RequestBody List<ModelPropertiesSortParam> param) {
        Reply reply;
        try {
            reply = mwModelManageService.editorPropertiesSort(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("editorPropertiesSort{}", e);
            return setResultFail("修改属性排序", param);
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/popup/browse")
    @ResponseBody
    @ApiOperation(value = "查询可以添加关系的模型")
    public ResponseBase selectModelListByModelId(@RequestBody ModelParam modelParam) {
        Reply reply;
        try {
            reply = mwModelManageService.selectModelListByModelId(modelParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("selectModelListByModelId{}", e);
            return setResultFail("查询关系模型失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/relations/delete")
    @ResponseBody
    @ApiOperation(value = "删除模型关系")
    public ResponseBase deleteModelRelations(@RequestBody DeleteModelRelationParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.deleteModelRelations(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteModelRelations{}", e);
            return setResultFail("删除模型关系失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/relations/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型关系")
    public ResponseBase selectModelRelations(@RequestBody SelectRelationParam param) {
        Reply reply;
        try {
            reply = mwModelManageService.selectModelRelationsByModelId(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("selectModelRelations{}", e);
            return setResultFail("查询模型关系失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/checkModelByES/browse")
    @ResponseBody
    @ApiOperation(value = "校验ES数据库中的模型和MYSQL中的是否同步")
    public ResponseBase checkModelByES() {
        Reply reply;
        try {
            reply = mwModelManageService.checkModelByES();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("checkModelByES{}", e);
            return setResultFail("数据校验失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/syncModelToES/browse")
    @ResponseBody
    @ApiOperation(value = "数据库MYSQL中的模型同步到ES")
    public ResponseBase syncModelToES() {
        Reply reply;
        try {
            reply = mwModelManageService.syncModelToES();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("syncModelToES{}", e);
            return setResultFail("模型同步到ES失败", "");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/checkIsFlag/browse")
    @ResponseBody
    @ApiOperation(value = "判断模型管理是不是西藏邮储环境")
    public ResponseBase checkIsFlag() {
        try {
            return setResultSuccess(isFlag);
        } catch (Exception e) {
            log.error("checkIsFlag{}", e);
            return setResultFail("操作失败失败", "");
        }
    }

    @PostMapping("/modelAllInfo/clean")
    @ResponseBody
    @ApiOperation(value = "ES模型信息全部清除")
    public ResponseBase cleanAllModelInfo() {
        Reply reply;
        try {
            reply = mwModelManageService.cleanAllModelInfo();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("devOpsUpdatePropInfo :{}", e);
            return setResultFail("ES模型信息清除失败", "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/devOps/updatePropInfo")
    @ResponseBody
    @ApiOperation(value = "从mw_cmdbmd_properties, mw_cmdbmd_properties_value更模型prop_info字段")
    public ResponseBase devOpsUpdatePropInfo() {
        try {
            MwModelTransfer mwModelTransfer = (MwModelTransfer) SpringUtils.getBean("mwModelTransfer");
            mwModelTransfer.transfer();
            return setResultSuccess();
        } catch (Exception e) {
            log.error("devOpsUpdatePropInfo :{}", e);
            return setResultFail("更新模型prop_info字段失败", "");
        }
    }

    @PostMapping("/propInfoToPageField/editor")
    @ResponseBody
    @ApiOperation(value = "模型属性更新至pageField和coustcol表")
    public ResponseBase editorPropInfoToPageField() {
        Reply reply;
        try {
            reply = mwModelManageService.editorPropInfoToPageField();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Exception e) {
            log.error("editorPropInfoToPageField :{}", e);
            return setResultFail("模型字段自定义同步失败", "");
        }
        return setResultSuccess(reply);
    }
}
