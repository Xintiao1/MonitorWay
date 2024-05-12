package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.engineManage.api.param.engineManage.AddOrUpdateEngineManageParam;
import cn.mw.monitor.engineManage.api.param.engineManage.DeleteEngineManageParam;
import cn.mw.monitor.engineManage.api.param.engineManage.QueryEngineDropdown;
import cn.mw.monitor.engineManage.api.param.engineManage.QueryEngineManageParam;
import cn.mw.monitor.engineManage.service.MwEngineManageService;
import cn.mw.monitor.service.assets.param.AssetsSearchTermFuzzyParam;
import cn.mw.monitor.service.license.service.CheckCountService;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;

/**
 * @author baochengbin
 * @date 2020/3/17
 */

@RequestMapping("/mwapi")
@Controller
@Slf4j
public class MWEngineManageController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWEngineManageController.class.getName());

    @Autowired
    MwEngineManageService mwEngineMangeService;

    @Autowired
    LicenseManagementService licenseManagement;

    @Autowired
    CheckCountService checkCountService;

    /**
     * 新增引擎
     */
    @MwPermit(moduleName = "polling_manage")
    @PostMapping("/engineManage/create")
    @ResponseBody
    public ResponseBase addEngineMange(@RequestBody AddOrUpdateEngineManageParam aParam,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            //许可校验
            //数量获取
            int count = checkCountService.selectTableCount("mw_enginemanage_table", true);
            ResponseBase responseBase = licenseManagement.getLicenseManagemengt("polling_manage", count, 1);
            if (responseBase.getRtnCode() != 200) {
                return  setResultFail(responseBase.getMsg(), responseBase.getData());
            }

            // 验证内容正确性
            reply = mwEngineMangeService.insert(aParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), aParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 删除引擎
     */
    @MwPermit(moduleName = "polling_manage")
    @PostMapping("/engineManage/delete")
    @ResponseBody
    public ResponseBase deleteEnigneManage(@RequestBody DeleteEngineManageParam dParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwEngineMangeService.delete(dParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改引擎
     */
    @MwPermit(moduleName = "polling_manage")
    @PostMapping("/engineManage/editor")
    @ResponseBody
    public ResponseBase updateEngineMange(@RequestBody AddOrUpdateEngineManageParam auParam,
                                          HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwEngineMangeService.update(auParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), auParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 分页查询引擎
     */
    @MwPermit(moduleName = "polling_manage")
    @PostMapping("/engineManage/browse")
    @ResponseBody
    public ResponseBase browseEngineMange(@RequestBody QueryEngineManageParam qParam,
                                          HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwEngineMangeService.selectList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), qParam);
        }

        return setResultSuccess(reply);
    }

    /**
     * 引擎下拉框查询
     */
    @MwPermit(moduleName = "polling_manage")
    @GetMapping("/engineManage/dropdown/browse")
    @ResponseBody
    public ResponseBase engineManageDropdownBrowse(@Param("monitorServerId") Integer monitorServerId, @Param("selectFlag") boolean selectFlag,
                                                   @Param("addLocalhost") boolean addLocalhost ,HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            int msId = (null == monitorServerId?-1:monitorServerId);
            reply = mwEngineMangeService.selectDropdownList(msId, selectFlag, addLocalhost);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }

        return setResultSuccess(reply);
    }

    /**
     * 引擎下拉框批量查询
     */
    @MwPermit(moduleName = "polling_manage")
    @PostMapping("/engineManage/dropdownBatch/browse")
    @ResponseBody
    public ResponseBase engineManageDropdownBatch(@RequestBody QueryEngineDropdown param,
                                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwEngineMangeService.selectDropdownBatchList(param.getMonitorServerIds());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }

        return setResultSuccess(reply);
    }

    /**
     * 引擎子页面查询
     */
    @MwPermit(moduleName = "polling_manage")
    @PostMapping("/engineManage/popup/browse")
    @ResponseBody
    public ResponseBase engineManagePopupBrowse(@RequestBody QueryEngineManageParam qParam,
                                                HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwEngineMangeService.selectById(qParam.getId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "polling_manage")
    @PostMapping("/engineManage/fuzzSearchAllFiled/browse")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData(@RequestBody AssetsSearchTermFuzzyParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwEngineMangeService.fuzzSearchAllFiledData(param.getValue());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), "模糊查询所有字段资数据失败");
        }

        return setResultSuccess(reply);
    }
}
