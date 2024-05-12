package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.configmanage.entity.*;
import cn.mw.monitor.configmanage.service.MwConfigManageService;
import cn.mw.monitor.configmanage.service.MwPerfromService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "配置管理")
public class MWConfigManageController extends BaseApiService {

    @Autowired
    private MwConfigManageService mwConfigManageService;

    @Autowired
    private MwPerfromService mwPerfromService;


    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/addOrUpdatePath")
    @ResponseBody
    @ApiOperation(value = "添加或修改路径")
    public ResponseBase addOrUpdatePath(@RequestBody MwNcmPath qParam, HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.updateOrAddPath(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getPath")
    @ResponseBody
    @ApiOperation(value = "获取路径")
    public ResponseBase getPath(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getPath();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/configPerform")
    @ResponseBody
    @ApiOperation(value = "下发")
    public ResponseBase configPerform(@RequestBody QueryTangAssetsParam qParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.configPerform(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/batchPerform")
    @ResponseBody
    @ApiOperation(value = "批量下发")
    public ResponseBase batchPerform(@RequestBody MwDownloadParam qParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.batchPerform(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/configCompare")
    @ResponseBody
    @ApiOperation(value = "配置对比")
    public ResponseBase configCompare(@RequestBody List<MwNcmDownloadConfig> qParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.configCompare(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/deleteConfigs")
    @ResponseBody
    @ApiOperation(value = "删除部分配置")
    public ResponseBase deleteConfigs(@RequestBody List<MwNcmDownloadConfig> qParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.deleteConfigs(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/deletePerforms")
    @ResponseBody
    @ApiOperation(value = "删除部分执行结果")
    public ResponseBase deletePerforms(@RequestBody List<MwNcmDownloadConfig> qParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.deletePerforms(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/downloadConfig")
    @ResponseBody
    @ApiOperation(value = "下载具体配置")
    public void downloadConfig(@RequestBody MwNcmDownloadConfig qParam,
                               HttpServletRequest request, HttpServletResponse response, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            mwConfigManageService.getDownload(qParam, response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
        }
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/downloadPerform")
    @ResponseBody
    @ApiOperation(value = "下载执行结果")
    public void downloadPerform(@RequestBody MwNcmDownloadConfig qParam,
                                HttpServletRequest request, HttpServletResponse response, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            mwConfigManageService.getPerform(qParam, response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
        }
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/showConfig")
    @ResponseBody
    @ApiOperation(value = "查看具体配置")
    public ResponseBase shonConfig(@RequestBody MwNcmDownloadConfig qParam,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.selectDownload(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/showPerform")
    @ResponseBody
    @ApiOperation(value = "查看具体执行结果")
    public ResponseBase showPerform(@RequestBody MwNcmDownloadConfig qParam,
                                    HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.showPerform(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/showConfigs")
    @ResponseBody
    @ApiOperation(value = "查看配置")
    public ResponseBase shonConfigs(@RequestBody MwNcmDownloadConfig qParam,
                                    HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.selectDownloads(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/showPerforms")
    @ResponseBody
    @ApiOperation(value = "查看执行结果")
    public ResponseBase showPerforms(@RequestBody MwNcmDownloadConfig qParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.selectPerforms(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/batchDownload")
    @ResponseBody
    @ApiOperation(value = "批量下载配置")
    public ResponseBase batchDownload(@RequestBody MwDownloadParam qParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.batchDownload(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getConfig")
    @ResponseBody
    @ApiOperation(value = "下载配置")
    public ResponseBase getConfig(@RequestBody QueryTangAssetsParam qParam,
                                  HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.download(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/browse")
    @ResponseBody
    @ApiOperation(value = "配置管理查询")
    public ResponseBase queryList(@RequestBody QueryTangAssetsParam qParam,
                                  HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.selectList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/browselist")
    @ResponseBody
    @ApiOperation(value = "配置管理查询")
    public ResponseBase configManagebrowselist(@RequestBody QueryTangAssetsParam qParam,
                                  HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.configManagebrowselist(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/browselistAssets")
    @ResponseBody
    @ApiOperation(value = "查询选择列表资产")
    public ResponseBase browselistAssets(@RequestBody List<QueryTangAssetsParam> qParam,
                                               HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.browselistAssets(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/account-manage/selectAssets")
    @ResponseBody
    @ApiOperation(value = "查找资产")
    public ResponseBase selectAssets(@RequestBody QueryTangAssetsParam queryTangAssetsParam) {
        Reply reply = null;
        try {
            reply =mwConfigManageService.selectAssets(queryTangAssetsParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess("ok");
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/editorSelect")
    @ResponseBody
    @ApiOperation(value = "配置管理编辑前查询")
    public ResponseBase editorQuery(@RequestBody MwConfigMapper qParam,
                                    HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.editorSelect(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/editor")
    @ResponseBody
    @ApiOperation(value = "配置管理编辑")
    public ResponseBase editor(@RequestBody MwConfigMapper qParam,
                               HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.editor(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/batchEditor")
    @ResponseBody
    @ApiOperation(value = "配置管理批量编辑")
    public ResponseBase batchEditor(@RequestBody MwConfigMapper qParam,
                               HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.batchEditor(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/delete")
    @ResponseBody
    @ApiOperation(value = "配置管理删除")
    public ResponseBase batDelete(@RequestBody List<MwTangibleassetsDTO> qParam,
                                  HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.delete(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/changeToUtf")
    @ResponseBody
    @ApiOperation(value = "转化文件")
    public ResponseBase changetoutf(@RequestBody MwChageRequest qParam,
                                    HttpServletRequest request) {
        Reply reply = null;
        try {
            String kill = getIsoToUtf_8(qParam.getType(), qParam.getContext());
            reply = Reply.ok(kill);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/listChangeType")
    @ResponseBody
    @ApiOperation(value = "转化文件下拉框")
    public ResponseBase listchangetype(HttpServletRequest request) {
        Reply reply = null;
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> typezero = new HashMap<>();
            typezero.put("dropKey", 0);
            typezero.put("dropValue", "UTF-8");
            list.add(typezero);
            Map<String, Object> type = new HashMap<>();
            type.put("dropKey", 1);
            type.put("dropValue", "gbk转UTF-8");
            list.add(type);
            Map<String, Object> typetwo = new HashMap<>();
            typetwo.put("dropKey", 2);
            typetwo.put("dropValue", "ISO-8895-1转UTF-8");
            list.add(typetwo);
            Map<String, Object> typethree = new HashMap<>();
            typethree.put("dropKey", 3);
            typethree.put("dropValue", "Unicode转UTF-8");
            list.add(typethree);
            reply = Reply.ok(list);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/showChangedConfigs")
    @ResponseBody
    @ApiOperation(value = "查看变化的差异化配置数据")
    public ResponseBase showChangedConfigs(@RequestBody MwNcmDownloadConfig qParam) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.selectChangedConfigs(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getTreeGroup")
    @ResponseBody
    @ApiOperation(value = "获取配置管理树状结构")
    public ResponseBase<MwConfigManageTreeGroup> getTreeGroup(@RequestParam String TreeName) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getTreeGroup(TreeName);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }



    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/addTreeGroup")
    @ResponseBody
    @ApiOperation(value = "新增配置管理树状结构")
    public ResponseBase<MwConfigManageTreeGroup> addTreeGroup(@RequestBody MwConfigManageTreeGroup mwConfigManageTreeGroup) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.addTreeGroup(mwConfigManageTreeGroup);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/updateTreeGroup")
    @ResponseBody
    @ApiOperation(value = "修改配置管理树状结构")
    public ResponseBase<MwConfigManageTreeGroup> updateTreeGroup(@RequestBody MwConfigManageTreeGroup mwConfigManageTreeGroup) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.updateTreeGroup(mwConfigManageTreeGroup);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/deleteTreeGroup")
    @ResponseBody
    @ApiOperation(value = "删除配置管理树状结构")
    public ResponseBase<MwConfigManageTreeGroup> deleteTreeGroup(@RequestBody MwConfigManageTreeGroup mwConfigManageTreeGroup) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.deleteTreeGroup(mwConfigManageTreeGroup);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 增加规则管理数据
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/addRuleManage")
    @ResponseBody
    @ApiOperation(value = "新增配置管理规则管理")
    public ResponseBase<MwConfigManageRuleManage> addRuleManage(@RequestBody MwConfigManageRuleManage ruleManage) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.addRuleManage(ruleManage);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("新增配置管理规则管理失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 更新规则管理
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/updateRuleManage")
    @ResponseBody
    @ApiOperation(value = "修改配置管理规则管理")
    public ResponseBase<MwConfigManageRuleManage> updateRuleManage(@RequestBody MwConfigManageRuleManage ruleManage) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.updateRuleManage(ruleManage);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("修改配置管理规则管理失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getRuleList")
    @ResponseBody
    @ApiOperation(value = "查看规则列表")
    public ResponseBase<MwConfigManageRuleManage> getRuleList(@RequestBody MwConfigManageRuleManage qParam) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getRuleList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("查看规则列表失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查看规则管理详情
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getRuleDetail")
    @ResponseBody
    @ApiOperation(value = "查看规则管理详情")
    public ResponseBase<MwConfigManageRuleManage> getRuleDetail(@RequestBody MwConfigManageRuleManage ruleManage) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getRuleDetail(ruleManage);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("查看规则管理详情失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 删除配置管理规则管理
     *
     * @param ruleManage 规则管理数据
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/deleteRuleManage")
    @ResponseBody
    @ApiOperation(value = "删除配置管理规则管理")
    public ResponseBase<MwConfigManageRuleManage> deleteRuleManage(@RequestBody MwConfigManageRuleManage ruleManage) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.deleteRuleManage(ruleManage);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("删除配置管理规则管理失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 增加检测报告
     *
     * @param detectReport 检测报告数据
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/addDetectReport")
    @ResponseBody
    @ApiOperation(value = "新增配置管理检测报告")
    public ResponseBase<DetectReportDTO> addDetectReport(@RequestBody DetectReportDTO detectReport) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.addDetectReport(detectReport);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("新增配置管理检测报告失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 更新检测报告
     *
     * @param detectReport
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/updateDetectReport")
    @ResponseBody
    @ApiOperation(value = "修改配置管理检测报告")
    public ResponseBase<DetectReportDTO> updateDetectReport(@RequestBody DetectReportDTO detectReport) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.updateDetectReport(detectReport);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("修改配置管理检测报告失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 更新检测报告
     *
     * @param detectReport
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/updateDetectReportState")
    @ResponseBody
    @ApiOperation(value = "修改配置管理检测报告状态")
    public ResponseBase<DetectReportDTO> updateDetectReportState(@RequestBody DetectReportDTO detectReport) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.updateDetectReportState(detectReport);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("修改配置管理检测报告失败", e);
            return setResultFail("修改配置管理检测报告失败",null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 查看检测报告
     *
     * @param detectReport
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getReportList")
    @ResponseBody
    @ApiOperation(value = "查看检测报告")
    public ResponseBase<DetectReportDTO> getReportList(@RequestBody DetectReportDTO detectReport) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getReportList(detectReport);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("查看检测报告失败",null);
            }
        } catch (Throwable e) {
            log.error("查看检测报告失败", e);
            return setResultFail("查看检测报告失败",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查看检测报告
     *
     * @param detectReport
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getReportDetail")
    @ResponseBody
    @ApiOperation(value = "查看检测报告")
    public ResponseBase<DetectReportDTO> getReportDetail(@RequestBody DetectReportDTO detectReport) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getReportDetail(detectReport);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("查看检测报告失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 查看检测报告
     *
     * @param detectReport
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getReportDetectDetail")
    @ResponseBody
    @ApiOperation(value = "查看检测报告")
    public ResponseBase<DetectReportDTO> getReportDetectDetail(@RequestBody DetectReportDTO detectReport) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getReportDetectDetail(detectReport);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("查看检测报告失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 下载检测报告
     *
     * @param detectReport
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/downloadReportDetectDetail")
    @ResponseBody
    @ApiOperation(value = "下载检测报告")
    public void downloadReportDetectDetail(@RequestBody DetectReportDTO detectReport,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        try {
            mwConfigManageService.downloadDetectReport(detectReport, response);
        } catch (Throwable e) {
            log.error("下载检测报告失败", e);
        }
    }

    @MwPermit(moduleName = "prop_manage")
    @GetMapping("/configManage/downloadReportDetectDetail")
    @ResponseBody
    @ApiOperation(value = "下载检测报告")
    public void downloadReportDetectDetail(@RequestParam int id,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        try {
            DetectReportDTO detectReport = new DetectReportDTO();
            detectReport.setId(id);
            mwConfigManageService.downloadDetectReport(detectReport, response);
        } catch (Throwable e) {
            log.error("下载检测报告失败", e);
        }
    }


    /**
     * 删除配置管理检测报告
     *
     * @param detectReport
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/deleteDetectReport")
    @ResponseBody
    @ApiOperation(value = "删除配置管理检测报告")
    public ResponseBase<DetectReportDTO> deleteDetectReport(@RequestBody DetectReportDTO detectReport) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.deleteDetectReport(detectReport);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("删除配置管理检测报告失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 增加策略管理
     *
     * @param policyManageDTO 策略管理数据
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/addPolicyManage")
    @ResponseBody
    @ApiOperation(value = "新增配置管理策略管理")
    public ResponseBase<PolicyManageDTO> addPolicyManage(@RequestBody PolicyManageDTO policyManageDTO) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.addPolicyManage(policyManageDTO);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("新增配置管理策略管理失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 更新策略管理
     *
     * @param policyManageDTO
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/updatePolicyManage")
    @ResponseBody
    @ApiOperation(value = "修改配置管理策略管理")
    public ResponseBase<PolicyManageDTO> updatePolicyManage(@RequestBody PolicyManageDTO policyManageDTO) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.updatePolicyManage(policyManageDTO);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("修改配置管理策略管理失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }



    /**
     * 查看策略管理
     *
     * @param policyManageDTO
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getPolicyList")
    @ResponseBody
    @ApiOperation(value = "查看策略管理")
    public ResponseBase<PolicyManageDTO> getPolicyList(@RequestBody PolicyManageDTO policyManageDTO) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getPolicyList(policyManageDTO);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("查看策略管理失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查看策略管理
     *
     * @param policyManageDTO
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getPolicyDetail")
    @ResponseBody
    @ApiOperation(value = "查看策略管理")
    public ResponseBase<PolicyManageDTO> getPolicyDetail(@RequestBody PolicyManageDTO policyManageDTO) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getPolicyDetail(policyManageDTO);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("查看策略管理失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 删除配置管理策略管理
     *
     * @param policyManageDTO
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/deletePolicyManage")
    @ResponseBody
    @ApiOperation(value = "删除配置管理策略管理")
    public ResponseBase<PolicyManageDTO> deletePolicyManage(@RequestBody PolicyManageDTO policyManageDTO) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.deletePolicyManage(policyManageDTO);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("删除配置管理策略管理失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    /**
     * 获取策略关联报告和规则数据
     *
     * @param map 数据
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getPolicyRelationList")
    @ResponseBody
    @ApiOperation(value = "获取策略关联报告和规则数据")
    public ResponseBase getPolicyRelationList(@RequestBody HashMap map) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getPolicyRelationList(map);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("获取策略关联报告和规则数据失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查看策略管理
     *
     * @param map 数据
     * @return
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getTreeAndData")
    @ResponseBody
    @ApiOperation(value = "获取树及其数据")
    public ResponseBase getTreeAndData(@RequestBody HashMap map) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getTreeAndData(String.valueOf(map.get("type")));
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error("获取树及其数据失败", e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getFuzzList")
    @ResponseBody
    @ApiOperation(value = "获取配置管理模糊查询数据")
    public ResponseBase<MwConfigManageTreeGroup> getTreeGroup(@RequestBody HashMap map) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getFuzzList(String.valueOf(map.get("type")));
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/batchDownloadPerform")
    @ResponseBody
    @ApiOperation(value = "批量下载执行结果")
    public void batchDownloadPerform(@RequestBody HashMap map,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     RedirectAttributesModelMap model) {
        try {
            mwConfigManageService.batchDownloadPerform(map, response);
        } catch (Throwable e) {
            log.error("下载失败", e);
        }
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/fuzzSearchAllFiled/browse")
    @ResponseBody
    @ApiOperation(value = "获取配置管理资产列表模糊查询数据")
    public ResponseBase fuzzSearchAllFiledData(@RequestBody HashMap map) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwConfigManageService.fuzzSearchAllFiledData(String.valueOf(map.get("type")));
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/exportExcelTemplate")
    @ResponseBody
    @ApiOperation(value = "导出配置资产批量处理数据")
    public void exportExcelTemplate(HttpServletResponse response) {
        try {
            mwConfigManageService.excelTemplateExport(response);
        } catch (Throwable e) {
            log.error("导出配置资产批量处理数据失败", e);
        }
    }

    /**
     * 批量导入资产关联关系
     *
     * @param file     用户导入excel文件
     * @param response 返回数据
     */
    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/excelImport")
    @ResponseBody
    @ApiOperation(value = "导入配置资产，批量处理数据")
    public void excelImport(@RequestBody MultipartFile file, HttpServletResponse response) {
        try {
            mwConfigManageService.excelImport(file, response);
        } catch (Exception e) {
            log.error("数据导入失败", e);
        }
    }

    /**
     * 批量导入资产关联关系
     *
     * @param response 返回数据
     */
    @GetMapping("/configManage/excelImport/test")
    @ResponseBody
    public void excelImportTest(HttpServletResponse response) {
        try {
            File file = new File("D:\\exportAssetsTemplate.xlsx");
            mwConfigManageService.excelImport(file, response);
        } catch (Exception e) {
            log.error("数据导入失败", e);
        }
    }

    /*自动转化格式*/
    public String getIsoToUtf_8(Integer type, String str) {
        if (StringUtils.isBlank(str)) {
            return "";
        }
        String newStr = "";
        switch (type) {
            case 0:
                newStr = str;
                break;
            case 1:
                newStr =StringtoUTF("ISO-8859-1",str) ;
                break;
            case 2:
                newStr = StringtoUTF("gbk",str) ;
                break;
            case 3:
                newStr = unicodeToString(str);
                break;
            case 4:
                newStr = StringtoUTF("GB2312",str) ;
                break;
        }

        return newStr;
    }

    public String StringtoUTF(String charset, String content) {
        String newStr = "";
        try {
            newStr = new String(content.getBytes(charset), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.toString());
        }
        return newStr;
    }


    public  String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }


    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/createVariable")
    @ResponseBody
    @ApiOperation(value = "创建系统变量")
    public ResponseBase createVariable(@RequestBody MwScriptVariable qParam,
                               HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            List<MwScriptVariable> mwScriptVariables = new ArrayList<>();
            mwScriptVariables.add(qParam);
            reply = mwConfigManageService.createVariable(mwScriptVariables);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getVariable")
    @ResponseBody
    @ApiOperation(value = "获取数据变量")
    public ResponseBase getVariable(@RequestBody List<MwScriptVariable> qParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getVariable(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "prop_manage")
    @PostMapping("/configManage/getVariableByid")
    @ResponseBody
    @ApiOperation(value = "根据作业id获取数据变量")
    public ResponseBase getVariableByid(@RequestBody Integer integer,
                                    HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwConfigManageService.getVariableById(integer);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("配置管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail("配置管理报错",null);
        }
        return setResultSuccess(reply);
    }


}
