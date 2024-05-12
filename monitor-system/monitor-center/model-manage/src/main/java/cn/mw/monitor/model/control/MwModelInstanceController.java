package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.model.dto.ModelFileDTO;
import cn.mw.monitor.model.exception.ModelManagerException;
import cn.mw.monitor.model.param.*;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.service.activitiAndMoudle.ActivitiSever;
import cn.mw.monitor.service.activitiAndMoudle.ModelServer;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.model.service.MwModelManageCommonService;
import cn.mw.monitor.service.systemLog.param.SystemLogParam;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import cn.mw.monitor.service.model.param.MwSyncZabbixAssetsParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xhy
 * @date 2021/2/25 9:07
 */
@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "模型实列接口", tags = "模型实列接口")
public class MwModelInstanceController extends BaseApiService {
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private MwModelCommonService mwModelCommonService;
    @Autowired
    private ActivitiSever activitiServer;
    @Autowired
    private LicenseManagementService licenseManagement;
    @Autowired
    private ModelServer modelSever;
    @Value("${System.isFlag}")
    private Boolean isFlag;
    //文件上传路径
    @Value("${file.url}")
    private String filePath;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;
    @Autowired
    private MwModelManageCommonService mwModelManageCommonService;
    //logo上传目录
    static final String MODULE = "file-upload";


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/create")
    @ResponseBody
    @ApiOperation(value = "创建模型实例")
    public ResponseBase creatModelInstance(@Validated @RequestBody AddAndUpdateModelInstanceParam instanceParam) {
        Integer type;
        String message = "实例新增成功";
        Reply reply = null;

        //设置为资源管理模式,则不能从资源中心添加
        if (!modelAssetEnable) {
            return setResultFail(ErrorConstant.TANGASSETS_MSG_210122, instanceParam);
        }

        try {
            Object object = (Object) instanceParam;
            Map<String,Object> valueMap = new HashedMap();
            if(CollectionUtils.isNotEmpty(instanceParam.getPropertiesList())) {
               valueMap = instanceParam.getPropertiesList().stream().filter(s-> !Strings.isNullOrEmpty(s.getPropertiesIndexId()) && !Strings.isNullOrEmpty(s.getPropertiesValue()))
                       .collect(Collectors.toMap(s -> s.getPropertiesIndexId(), s -> (Object)s.getPropertiesValue(), (
                        value1, value2) -> {
                    return value2;
                }));
            }
            Map map = new HashedMap();
            if(instanceParam.getCreateType()==0){
                //调用流程审批
                map = activitiServer.OperMoudleContainActiviti(instanceParam.getModelId().toString(), 0, object);
            }else{
                //调用工单处理
                map = activitiServer.OperMoudleContainActivitiTwo(instanceParam.getModelId().toString(), 0, object,instanceParam.getWorkflowMoudleId(),valueMap);
            }
            //返回类型为0，表示不走流程 否则进入流程审批环节
            if (map != null && map.get("type") != null && map.get("message") != null) {
                type = (Integer) map.get("type");
                message = map.get("message").toString();
                if (type == 0) {
                    reply = modelSever.creatModelInstance(object, 0);
                    return setResultSuccess(reply);
                } else if (type == 3) {
                    return setActiviti(message, "");
                } else {
                    return setResultFail(message, "");
                }
            }
            return setResultFail("新增实例失败", "");
        } catch (ModelManagerException e) {
            log.error("creatModelInstance{}", e);
            return setResultFail(e.getMessage(), "");
        }
        catch (Exception e) {
            log.error("creatModelInstance{}", e);
            return setResultFail("创建模型实例失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/batchInstance/create")
    @ResponseBody
    @ApiOperation(value = "批量创建模型实例")
    public ResponseBase batchCreatModelInstance(@Validated @RequestBody BatchAddModelInstanceParam instanceParam) {
        Integer type;
        String message = "实例新增成功";
        Reply reply = null;

        //设置为资源管理模式,则不能从资源中心添加
        if (!modelAssetEnable) {
            return setResultFail(ErrorConstant.TANGASSETS_MSG_210122, instanceParam);
        }
        try {
            Object object = (Object) instanceParam;
            //调用流程审批
            Map map = activitiServer.OperMoudleContainActiviti(instanceParam.getModelId().toString(), 0, object);
            //返回类型为0，表示不走流程 否则进入流程审批环节
            if (map != null && map.get("type") != null && map.get("message") != null) {
                type = (Integer) map.get("type");
                message = map.get("message").toString();
                if (type == 0) {
                    reply = modelSever.batchCreatModelInstance(object, 0);
                    return setResultSuccess(reply);
                } else if (type == 3) {
                    return setActiviti(message, "");
                } else {
                    return setResultFail(message, "");
                }
            }
            return setResultFail("新增实例失败", "");
        } catch (Exception e) {
            log.error("creatModelInstance{}", e);
            return setResultFail("批量创建模型实例失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/editor")
    @ResponseBody
    @ApiOperation(value = "修改模型实例")
    public ResponseBase updateModelInstance(@RequestBody AddAndUpdateModelInstanceParam instanceParam) {
        Integer type;
        String message = "实例修改成功";
        try {
            Object object = (Object) instanceParam;
            //调用流程审批
            Map map = activitiServer.OperMoudleContainActiviti(instanceParam.getModelId().toString(), 2, object);
            //返回类型为0，表示不走流程 否则进入流程审批环节
            if (map != null && map.get("type") != null && map.get("message") != null) {
                type = (Integer) map.get("type");
                message = map.get("message").toString();
                if (type == 0) {
                    modelSever.updateModelInstance(object, 0);
                    return setResultSuccess(message);
                } else if (type == 3) {
                    return setActiviti(message, "");
                } else {
                    return setResultFail(message, "");
                }
            }
            return setResultFail("实例修改成功", "");
        } catch (Exception e) {
            log.error("updateModelInstance{}", e);
            return setResultFail("修改模型实例失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/syncZabbixName/editor")
    @ResponseBody
    @ApiOperation(value = "同步zabbix资产可见名称")
    public ResponseBase updateSyncZabbixName(@RequestBody MwSyncZabbixAssetsParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.updateSyncZabbixName(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("updateSyncZabbixName{}", e);
            return setResultFail("同步zabbix资产可见名称失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/delete")
    @ResponseBody
    @ApiOperation(value = "删除模型实例")
    public ResponseBase deleteModelInstance(@RequestBody List<DeleteModelInstanceParam> instanceParam) {
        Integer type;
        String message = "删除模型实例";
        try {
            if (instanceParam != null && instanceParam.size() > 0) {
                for (DeleteModelInstanceParam deleteParam : instanceParam) {
                    Object object = (Object) deleteParam;
                    //调用流程审批
                    Map map = activitiServer.OperMoudleContainActiviti(deleteParam.getModelId().toString(), 1, object);
                    //返回类型为0，表示不走流程 否则进入流程审批环节
                    if (map != null && map.get("type") != null && map.get("message") != null) {
                        type = (Integer) map.get("type");
                        message = map.get("message").toString();
                        if (type == 0) {
                            modelSever.deleteModelInstance(object, 0);
                            setResultSuccess(message);
                        } else if (type == 3) {
                            setActiviti(message, "");
                        } else {
                            setResultFail(message, "");
                        }
                    }
                }
            }
            return setResultSuccess(message);
        } catch (Exception e) {
            log.error("deleteModelInstance{}", e);
            return setResultFail("删除模型实例失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/look")
    @ResponseBody
    @ApiOperation(value = "查看模型实例详情")
    public ResponseBase lookModelInstance(@RequestBody AddAndUpdateModelInstanceParam instanceParam) {
        Reply reply;
        try {
            reply = mwModelInstanceService.lookModelInstance(instanceParam);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("lookModelInstance{}", e);
            return setResultFail("查看模型实例详情失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceByAction/look")
    @ResponseBody
    @ApiOperation(value = "流程审批节点查看模型实例详情")
    public ResponseBase lookModelInstanceByAction(@RequestBody Object instanceParam) {
        AddAndUpdateModelInstanceParam param = JSONObject.parseObject(((JSONObject) instanceParam).get("instanceParam").toString(), AddAndUpdateModelInstanceParam.class);
        Reply reply;
        try {
            reply = mwModelInstanceService.lookModelInstanceByAction(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("lookModelInstanceByAction{}", e);
            return setResultFail("查看流程审批详情失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceByActionDelete/look")
    @ResponseBody
    @ApiOperation(value = "流程审批节点查看模型实例详情")
    public ResponseBase lookModelInstanceByActionDelete(@RequestBody Object instanceParam) {
        DeleteModelInstanceParam param = JSONObject.parseObject(((JSONObject) instanceParam).get("instanceParam").toString(), DeleteModelInstanceParam.class);
        Reply reply;
        try {
            reply = mwModelInstanceService.lookModelInstanceByActionDelete(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("lookModelInstanceByActionDelete{}", e);
            return setResultFail("操作失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceInfoById/browse")
    @ResponseBody
    @ApiOperation(value = "根据id获取模型实例数据")
    public ResponseBase getInstanceInfoById(@RequestBody QueryModelInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.getInstanceInfoById(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getInstanceInfoById{}", e);
            return setResultFail("获取模型实例数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceStructInfo/browse")
    @ResponseBody
    @ApiOperation(value = "获取模型实例结构体数据")
    public ResponseBase getInstanceStructInfo(@RequestBody QueryModelInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.getInstanceStructInfo(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getInstanceStructInfo{}", e);
            return setResultFail("获取模型实例结构体数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getModelPropertyType/browse")
    @ResponseBody
    @ApiOperation(value = "获取模型属性字段和类型")
    public ResponseBase getModelPropertyTypeById(@RequestBody QueryModelInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.getModelPropertiesById(param.getModelId());
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getModelPropertyTypeById{}", e);
            return setResultFail("获取模型属性字段和类型失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型实例")
    public ResponseBase selectModelInstance(@RequestBody QueryInstanceModelParam param) {
        Reply reply = null;
        try {
            //特殊部署环境中(西藏邮储)，不需要模型列表，全部展示实例列表
            param.setIsFlag(isFlag);
            if (isFlag) {
                reply = mwModelInstanceService.selectModelInstance(param);
            } else {
                if (!Strings.isNullOrEmpty(param.getType())) {
                    //左侧树点击的是模型时，展示的为实例列表信息
                    if ("model".equals(param.getType())) {
                        reply = mwModelInstanceService.selectModelInstance(param);
                    } else {
                        //左侧树点击的是模型分类时，展示的为模型列表信息，ModelId为模型分组Id
                        reply = mwModelInstanceService.queryModelListInfo(param);
                    }
                }
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelInstance{}", e);
            return setResultFail("查询模型实例失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/fieldUniqueCheck")
    @ResponseBody
    @ApiOperation(value = "实例字段唯一性校验")
    public ResponseBase modelInstanceFieldUnique(@RequestBody QueryModelInstanceParam param) {
        Reply reply = null;
        try {
            reply = mwModelInstanceService.modelInstanceFieldUnique(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("modelInstanceFieldUnique{}", e);
            return setResultFail("实例字段唯一性校验失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceByFuzzyQuery/browse")
    @ResponseBody
    @ApiOperation(value = "模型实例模糊查询下拉信息提示")
    public ResponseBase getInstanceInfoByFuzzyQuery(@RequestBody QueryModelInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.getInstanceInfoByFuzzyQuery(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelInstance{}", e);
            return setResultFail("模型实例模糊查询下拉信息提示失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceFiled/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型实例显示字段")
    public ResponseBase selectModelInstanceFiled(@RequestBody QueryCustomModelparam param) {
        Reply reply;
        try {
            //列表字段查询
            reply = mwModelInstanceService.selectModelInstanceFiled(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelInstanceFiled{}", e);
            return setResultFail("查询模型实例显示字段失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceTree/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型实例树结构")
    public ResponseBase selectModelInstanceTree() {
        Reply reply;
        try {
            reply = mwModelInstanceService.selectModelInstanceTree();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelInstanceTree{}", e);
            return setResultFail("查询模型实例树结构失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/shiftInstance/check")
    @ResponseBody
    @ApiOperation(value = "模型实例转移判断")
    public ResponseBase shiftInstanceCheck(@RequestBody List<AddAndUpdateModelInstanceParam> param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.shiftInstanceCheck(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("shiftInstanceCheck{}", e);
            return setResultFail("查询模型实例转移判断失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/properties/popup/browse")
    @ResponseBody
    @ApiOperation(value = "查询添加模型实例需要添加的属性")
    public ResponseBase selectInstanceProperties(@RequestBody QueryModelInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.selectInstanceProperties(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectInstanceProperties{}", e);
            return setResultFail("查询模型实例属性失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/properties/browse")
    @ResponseBody
    @ApiOperation(value = "查询模型实例的属性值")
    public ResponseBase selectModelInstanceProperties(@RequestBody QueryModelInstanceParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.selectModelInstanceProperties(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelInstanceProperties{}", e);
            return setResultFail("查询模型实例的属性值失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/insertFiled/browse")
    @ResponseBody
    @ApiOperation(value = "新增模型实例显示的字段值")
    public ResponseBase selectModelInstanceFiledByInsert(@RequestBody QueryCustomModelparam queryCustomModelparam) {
        Reply reply;
        try {
            reply = mwModelInstanceService.selectModelInstanceFiledByInsert(queryCustomModelparam);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectModelInstanceFiledByInsert{}", e);
            return setResultFail("新增模型实例显示的字段值失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/image-upload")
    @ResponseBody
    @ApiOperation(value = "模型实例图片上传")
    public ResponseBase imageUpload(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) {
        Reply reply = new Reply();
        try {
            Integer instanceId = 0;
            if (request.getParameter("instanceId") != null && !Strings.isNullOrEmpty(request.getParameter("instanceId").toString())) {
                instanceId = Integer.valueOf(request.getParameter("instanceId"));
            }
            reply = mwModelInstanceService.imageUpload(multipartFile, instanceId);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("imageUpload{}", e);
            return setResultFail("模型实例图片上传失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/changeHistory")
    @ResponseBody
    @ApiOperation(value = "模型实例变更历史")
    public ResponseBase instaceChangeHistory(@RequestBody SystemLogParam qParam) {
        Reply reply = new Reply();
        try {
            qParam.setType("instance_" + qParam.getInstanceId());
            reply = mwModelInstanceService.instaceChangeHistory(qParam);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("instaceChangeHistory{}", e);
            return setResultFail("模型实例变更历史失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/getTimeOutInfo")
    @ResponseBody
    @ApiOperation(value = "过期实例提醒")
    public ResponseBase getTimeOutInfo() {
        Reply reply = new Reply();
        try {
            reply = mwModelInstanceService.getTimeOutInfo();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getTimeOutInfo{}", e);
            return setResultFail("过期实例提醒失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/getSelectDataInfo")
    @ResponseBody
    @ApiOperation(value = "获取关联下拉数据")
    public ResponseBase getSelectDataInfo(@RequestBody List<QueryRelationInstanceInfo> param) {
        Reply reply = new Reply();
        try {
            reply = mwModelInstanceService.getSelectDataInfo(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getSelectDataInfo{}", e);
            return setResultFail("获取关联下拉数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/download")
    @ResponseBody
    @ApiOperation(value = "文件下载")
    public ResponseBase download(@RequestBody ModelFileDTO dto, HttpServletRequest request, HttpServletResponse response) {
        Reply reply = new Reply();
        OutputStream os = null;
        InputStream is = null;
        String fileName = dto.getFileName();
        try {
            //取得输出流
            os = response.getOutputStream();
            //清空输出流
            response.reset();
            response.setContentType("application/x-download;charset=GBK");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("utf-8")));
            //读取流
            File f = new File(filePath + File.separator + fileName);
            is = new FileInputStream(f);
            if (is == null) {
                log.error("下载附件失败，请检查文件" + fileName + "是否存在");
                return setResultFail("下载附件失败，请检查文件" + fileName + "是否存在", fileName);
            }
            IOUtils.copy(is, response.getOutputStream());
        } catch (IOException e) {
            return setResultFail("下载附件失败，error:" + "", fileName);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                log.error("FileInputStream close fail{}", e);
                return setResultFail("文件下载失败", "");
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.error("OutputStream close fail{}", e);
                return setResultFail("文件下载失败", "");
            }
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/getRoomLayout")
    @ResponseBody
    @ApiOperation(value = "获取机房机柜布局数据")
    public ResponseBase getRoomLayout(@RequestBody QueryInstanceModelParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelInstanceService.getRoomAndCabinetLayout(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("获取机房机柜布局数据：getRoomLayout{}", e);
            return setResultFail("获取机房机柜布局数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/updateRoomLayout")
    @ResponseBody
    @ApiOperation(value = "修改机房布局数据")
    public ResponseBase updateRoomLayout(@RequestBody QueryBatchSelectDataParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelInstanceService.updateRoomLayout(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("updateRoomLayout{}", e);
            return setResultFail("修改机房布局数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/updateCabinetLayout")
    @ResponseBody
    @ApiOperation(value = "修改机柜布局数据")
    public ResponseBase updateCabinetLayout(@RequestBody QueryCabinetLayoutListParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelInstanceService.updateCabinetLayout(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("updateCabinetLayout{}", e);
            return setResultFail("修改机柜布局数据失败", "");
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instance/getAllCabinetInfo")
    @ResponseBody
    @ApiOperation(value = "获取机房下每个机柜信息")
    public ResponseBase getAllCabinetInfoByRoom(@RequestBody QueryInstanceModelParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelInstanceService.getAllCabinetInfoByRoom(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getAllCabinetInfoByRoom{}", e);
            return setResultFail("获取机房下每个机柜信息失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceRelation/getModelRelationInfo")
    @ResponseBody
    @ApiOperation(value = "实例关系拓扑:根据模型id获取所有模型关系关联数据")
    public ResponseBase getModelRelationInfo(@RequestBody QueryInstanceRelationToPoParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelInstanceService.getModelRelationInfo(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getModelRelationInfo{}", e);
            return setResultFail("获取模型关系关联数据失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceRelation/getInstanceListByModelId")
    @ResponseBody
    @ApiOperation(value = "实例关系拓扑:根据模型id获取所有实例列表数据")
    public ResponseBase getInstanceListByModelId(@RequestBody QueryInstanceModelParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelInstanceService.getInstanceListByModelId(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getInstanceListByModelId{}", e);
            return setResultFail("根据模型id获取所有实例列表数据失败", "");
        }
    }



    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getMonitorMode/browse")
    @ResponseBody
    @ApiOperation(value = "获取监控方式")
    public ResponseBase getMonitorModeInfo() {
        Reply reply = new Reply();
        try {
            reply = mwModelManageCommonService.getMonitorModeInfo();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getMonitorModeInfo{}", e);
            return setResultFail("获取监控方式失败", "");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/batchUpdatePower/editor")
    @ResponseBody
    @ApiOperation(value = "批量修改权限")
    public ResponseBase batchUpdatePower(@RequestBody BatchUpdatePowerParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.batchUpdatePower(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("batchUpdatePower{}", e);
            return setResultFail("", param);
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/batchsUpdateSQLPower/editor")
    @ResponseBody
    @ApiOperation(value = "从es中获取权限插入数据库中")
    public ResponseBase batchUpdatePowerByEs() {
        Reply reply;
        try {
            reply = mwModelInstanceService.batchUpdatePowerByEs();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("batchUpdatePowerByEs{}", e);
            return setResultFail("",e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/batchUpdatePollingEngine/editor")
    @ResponseBody
    @ApiOperation(value = "修改轮询引擎")
    public ResponseBase batchUpdatePollingEngine(@RequestBody UpdatePollingEngineParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.updatePollingEngine(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("batchUpdatePowerByEs{}", e);
            return setResultFail("",e);
        }
    }

	@MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceRelation/browse")
    @ResponseBody
    @ApiOperation(value = "查询能够选择的实例关系")
    public ResponseBase instanceRelationBrowse(@RequestBody QueryInstanceRelationToPoParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.instanceRelationBrowse(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("instanceRelationBrowse", e);
            return setResultFail("查看实例关系失败" ,e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/esDataRefresh/create")
    @ResponseBody
    @ApiOperation(value = "刷新es数据")
    public ResponseBase esDataRefresh(@RequestBody ModelParam modelParams) {
        try {
            mwModelInstanceService.esDataRefresh(modelParams);
            return setResultSuccess("刷新es数据成功");
        } catch (Exception e) {
            log.error("esDataRefresh", e);
            return setResultFail("刷新es数据失败" ,e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getInstanceIdByLinkRelation/browse")
    @ResponseBody
    @ApiOperation(value = "通过topo关系获取关联数据的实例Id")
    public ResponseBase getInstanceIdByLinkRelation(@RequestBody QueryInstanceTopoInfoParam topoInfoParam) {
        Reply reply;
        try {
            reply = mwModelInstanceService.getInstanceIdByLinkRelation(topoInfoParam);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getInstanceIdByLinkRelation", e);
            return setResultFail("通过topo关系获取关联数据的实例Id" ,e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/setModelAreaDataToEs/editor")
    @ResponseBody
    @ApiOperation(value = "es设置区域字段")
    public ResponseBase setModelAreaDataToEs() {
        try {
            mwModelInstanceService.setModelAreaDataToEs();
            return setResultSuccess("es设置区域字段成功");
        } catch (Exception e) {
            log.error("setModelAreaDataToEs", e);
            return setResultFail("es设置区域字段失败" ,e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/syncLinkRelation/editor")
    @ResponseBody
    @ApiOperation(value = "资产通过关联关系同步告警区域")
    public ResponseBase syncAllInstanceLinkRelation() {
        try {
            mwModelInstanceService.syncAllInstanceLinkRelation();
            return setResultSuccess("es设置区域字段成功");
        } catch (Exception e) {
            log.error("setModelAreaDataToEs", e);
            return setResultFail("es设置区域字段失败" ,e);
        }
    }


    @MwPermit(moduleName = "model_manage")
    @PostMapping("/allModelMonitorItem/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有的监控项信息")
    public ResponseBase selectAllModelMonitorItem() {
        Reply reply;
        try {
            reply = mwModelInstanceService.selectAllModelMonitorItem();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("selectAllModelMonitorItem", e);
            return setResultFail("获取所有的监控项信息失败" ,e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/configPower/create")
    @ResponseBody
    @ApiOperation(value = "IP地址回显权限数据配置")
    public ResponseBase settingConfigPowerByIp(@RequestBody SettingConfigPowerParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.settingConfigPowerByIp(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("settingConfigPowerByIp", e);
            return setResultFail("IP地址回显权限数据配置失败" ,e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/configPower/browse")
    @ResponseBody
    @ApiOperation(value = "IP地址回显权限数据配置")
    public ResponseBase getSettingConfigPowerByIp() {
        Reply reply;
        try {
            reply = mwModelInstanceService.getSettingConfigPowerByIp();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getSettingConfigPowerByIp", e);
            return setResultFail("获取IP地址回显权限数据失败" ,e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getModelInfoParam/browse")
    @ResponseBody
    @ApiOperation(value = "根据Id查询模型信息")
    public ResponseBase getModelInfoParamById(@RequestBody MwModelInfoParam param) {
        Reply reply;
        try {
            reply = mwModelInstanceService.getModelInfoParamById(param.getModelId());
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getModelInfoParamById", e);
            return setResultFail("根据Id查询模型信息失败" ,e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/instanceName/browse")
    @ResponseBody
    @ApiOperation(value = "根据模型id查询资产名称")
    public ResponseBase getInstanceNameByModel() {
        Reply reply;
        try {
            reply = mwModelCommonService.getInstanceNameByModelId();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getSettingConfigPowerByIp", e);
            return setResultFail("获取IP地址回显权限数据失败" ,e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getCabinetRelationAsset/browse")
    @ResponseBody
    @ApiOperation(value = "获取所有机柜下属设备")
    public ResponseBase getCabinetRelationDevice() {
        Reply reply;
        try {
            reply = mwModelCommonService.getCabinetRelationDevice();
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getCabinetRelationDevice", e);
            return setResultFail("获取所有机柜下属设备失败" ,e);
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/getInterfaceInfosByAssetsId/browse")
    @ResponseBody
    @ApiOperation(value = "获取资产接口信息")
    public ResponseBase getInterfaceInfosByAssetsId(@RequestBody MwModelAssetsInterfaceParam param) {
        Reply reply;
        try {
            reply = mwModelCommonService.getInterfaceInfosByAssetsIds(param);
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getInterfaceInfosByAssetsId", e);
            return setResultFail("获取资产接口信息失败" ,e);
        }
    }
}
