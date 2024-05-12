package cn.mw.monitor.user.control;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.param.org.*;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.validator.group.Insert;
import cn.mw.monitor.validator.group.Update;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "机构管理接口",tags = "机构管理接口")
public class MWOrgController extends BaseApiService {

    @Autowired
    private MWOrgService mwOrgService;

    @ApiOperation(value="绑定用户")
    @PostMapping("/org/popup/editor")
    @ResponseBody
    public ResponseBase bindUserOrg(@RequestBody BindUserOrgParam param,
                                    HttpServletRequest request,
                                    RedirectAttributesModelMap model) {
        try {
            List<Integer> orgIds = Arrays.asList(param.getOrgId());
            param.setOrgIds(orgIds);
            Reply reply = mwOrgService.bindUserOrg(param);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="机构新增")
    @PostMapping("/org/create")
    @ResponseBody
    public ResponseBase addOrg(@Validated({Insert.class}) @RequestBody AddUpdateOrgParam param,
                               HttpServletRequest request,
                               RedirectAttributesModelMap model) {
        try {
            Reply reply = mwOrgService.insert(param);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="机构删除")
    @PostMapping("/org/delete")
    @ResponseBody
    public ResponseBase deleteOrg(@Validated @RequestBody DeleteOrgParam param,
                                  HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        try {
            Reply reply = mwOrgService.delete(param.getOrgIds());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="机构状态修改")
    @PostMapping("/org/perform")
    @ResponseBody
    public ResponseBase updateOrgState(@Validated @RequestBody UpdateOrgStateParam bParam,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        try {
            Reply reply = mwOrgService.updateOrgState(bParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), bParam);
        }
    }

    @ApiOperation(value="机构修改")
    @PostMapping("/org/editor")
    @ResponseBody
    public ResponseBase updateOrg(@Validated({Update.class}) @RequestBody AddUpdateOrgParam uParam,
                                  HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        try {
            Reply reply = mwOrgService.update(uParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), uParam);
        }
    }

    @ApiOperation(value="机构列表查询")
    @PostMapping("/org/browse")
    @ResponseBody
    public ResponseBase browseOrg(@RequestBody QueryOrgParam qParam,
                                  HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        try {
            Reply reply = mwOrgService.selectList(qParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    @ApiOperation(value="根据机构ID取机构信息")
    @PostMapping(value = "/org/popup/browse")
    @ResponseBody
    public ResponseBase browseOrgPopup(@RequestBody QueryOrgParam qParam,
                                       HttpServletRequest request,
                                       RedirectAttributesModelMap model) {
        try {
            Reply reply = mwOrgService.selectByOrgId(qParam.getOrgId());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    @ApiOperation(value="机构下拉框查询")
    @PostMapping("/org/dropdown/browse")
    @ResponseBody
    public ResponseBase orgDropdownBrowse(@RequestBody QueryOrgForDropDown qParam,
                                          HttpServletRequest request,
                                          RedirectAttributesModelMap model){
        try{
            Reply reply = mwOrgService.selectDorpdownList(qParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    @ApiOperation(value="根据权限查询机构信息")
    @PostMapping(value = "/org/perm/search/browse")
    @ResponseBody
    public ResponseBase getListFilterByPerm(@RequestBody QueryOrgParam qParam,
                                            HttpServletRequest request,
                                            RedirectAttributesModelMap model) {
        try {
            Reply reply = mwOrgService.selectListFilterByPerm(qParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    @ApiOperation(value="赛尔移动端机构列表查询")
    @PostMapping("/cernet/org/browse")
    @ResponseBody
    public ResponseBase browseCernetOrg(@RequestBody QueryOrgParam qParam,
                                  HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        try {
            Reply reply = mwOrgService.getOrgList(qParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    @ApiOperation(value="机构列表查询")
    @PostMapping("/org/batch/browse")
    @ResponseBody
    public ResponseBase batchBrowseOrg(@RequestBody QueryOrgParam qParam,
                                  HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        try {
            Reply reply = mwOrgService.batchQueryOrg(qParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    /**
     * 获取机构的经纬度
     * @param qParam
     * @return
     */
    @ApiOperation(value="查询机构经纬度下拉")
    @PostMapping("/org/longitude/dropDown")
    @ResponseBody
    public ResponseBase getOrgLongitudeDropDown(@RequestBody QueryOrgParam qParam) {
        try {
            Reply reply = mwOrgService.getOrgLongitudeDropDown(qParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }
}
