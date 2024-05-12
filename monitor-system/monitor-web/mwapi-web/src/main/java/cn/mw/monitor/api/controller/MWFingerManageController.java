package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.fingerprint.service.MwFingerprintManageService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author gui.quanwang
 * @className MWFingerManageController
 * @description 指纹库管理接口类
 * @date 2021/7/26
 */
@RequestMapping("/mwapi")
@Controller
@Api(value = "指纹库管理", tags = "指纹库管理")
public class MWFingerManageController extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("control-" + MWFingerManageController.class.getName());

    @Autowired
    private MwFingerprintManageService mwFingerprintManageService;

    @MwPermit(moduleName = "prop_manage")
    @GetMapping("/fingerprint/version/browse")
    @ResponseBody
    @ApiOperation(value = "获取版本信息")
    public ResponseBase addOrUpdatePath(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwFingerprintManageService.getFingerPrintVersion();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }


    @GetMapping("/fingerprint/download")
    @ResponseBody
    @ApiOperation(value = "文件下载")
    public ResponseBase download(final HttpServletResponse response, final HttpServletRequest request) {
        Reply reply = new Reply();
        try {
            mwFingerprintManageService.downloadFile(response);
        } catch (Exception e) {
            return setResultFail("下载附件失败，error:" + e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }
}
