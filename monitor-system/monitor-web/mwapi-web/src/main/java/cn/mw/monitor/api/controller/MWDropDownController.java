package cn.mw.monitor.api.controller;

import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.dropDown.service.MwDropDownService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "下拉框接口", tags ="下拉框接口")
public class MWDropDownController extends BaseApiService {

    @Autowired
    private MwDropDownService mwDropDownService;

    @ApiOperation(value = "根据下拉框code查询下拉框信息")
    @GetMapping("/dropdown/browse")
    @ResponseBody
    public ResponseBase browseCustomcol(@RequestParam("name") String name,
                                        HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwDropDownService.selectDropdownByCode(name);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), name);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "查询pageSelect的url")
    @GetMapping("/selectNum/browse")
    @ResponseBody
    public ResponseBase browseNumSelect(@RequestParam("type") String type) {
        Reply reply;
        try {
            reply = mwDropDownService.pageSelectNumUrl(type);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseSelect{}",e);
            return setResultFail(e.getMessage(), type);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "查询pageSelect的url")
    @GetMapping("/selectChar/browse")
    @ResponseBody
    public ResponseBase browseCharSelect(@RequestParam("type") String type) {
        Reply reply;
        try {
            reply = mwDropDownService.pageSelectCharUrl(type);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseSelect{}",e);
            return setResultFail(e.getMessage(), type);
        }
        return setResultSuccess(reply);
    }

    @ApiOperation(value = "查询pageSelect的 对应数据库表的每个字段值的下拉信息（去重）")
    @GetMapping("/selectDropdownTable/browse")
    @ResponseBody
    public ResponseBase selectDropdown(@RequestParam("fieldName") String fieldName, @RequestParam("tableName") String tableName) {
        Reply reply;
        try {
            reply = mwDropDownService.selectDropdown(fieldName, tableName);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("browseSelect{}",e);
            return setResultFail(e.getMessage(),fieldName + "---" + tableName);
        }
        return setResultSuccess(reply);
    }

}
