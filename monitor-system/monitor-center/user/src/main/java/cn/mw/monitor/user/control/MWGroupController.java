package cn.mw.monitor.user.control;

import cn.mw.monitor.api.param.user.QueryUserParam;
import cn.mw.monitor.user.dto.MwGroupDTO;
import cn.mw.monitor.user.service.impl.MWUserGroupServiceImpl;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.exception.CheckInsertGroupException;
import cn.mw.monitor.api.param.user.BindUserGroupParam;
import cn.mw.monitor.api.param.usergroup.AddUpdateGroupParam;
import cn.mw.monitor.api.param.usergroup.DeleteGroupParam;
import cn.mw.monitor.api.param.usergroup.QueryGroupParam;
import cn.mw.monitor.api.param.usergroup.UpdateGroupStateParam;
import cn.mw.monitor.service.user.api.MWGroupCommonService;
import cn.mw.monitor.user.service.MWGroupService;
import cn.mw.monitor.validator.group.Insert;
import cn.mw.monitor.validator.group.Update;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "用户组管理接口",tags = "用户组管理接口")
public class MWGroupController extends BaseApiService {

    @Autowired
    private MWGroupService mwGroupService;
    @Autowired
    private MWGroupCommonService mwGroupCommonService;

    @ApiOperation(value="绑定用户")
    @PostMapping("/group/popup/editor")
    @ResponseBody
    public ResponseBase bindUserGroud(@RequestBody BindUserGroupParam param,
                                      HttpServletRequest request,
                                      RedirectAttributesModelMap model) {
        try {
            List<Integer> groupIds = Arrays.asList(param.getGroupId());
            param.setGroupIds(groupIds);
            Reply reply = mwGroupService.bindUserGroup(param);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="用户组新增")
    @PostMapping("/group/create")
    @ResponseBody
    public ResponseBase addGroup(@Validated({Insert.class}) @RequestBody AddUpdateGroupParam param,
                                 HttpServletRequest request,
                                 RedirectAttributesModelMap model) {
        try {
            Reply reply = mwGroupService.insert(param);
            return setResultSuccess(reply);
        }catch (CheckInsertGroupException e) {
            return setResultFail(e.getMessage(),param);
        }catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="用户组删除")
    @PostMapping("/group/delete")
    @ResponseBody
    public ResponseBase deleteGroup(@RequestBody DeleteGroupParam param,
                                    HttpServletRequest request,
                                    RedirectAttributesModelMap model) {
        try {
            Reply reply = mwGroupService.delete(param.getGroupIds());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="用户组修改状态")
    @PostMapping("/group/perform")
    @ResponseBody
    public ResponseBase updateGroupState(@Validated @RequestBody UpdateGroupStateParam param,
                                         HttpServletRequest request,
                                         RedirectAttributesModelMap model) {
        try {
            Reply reply = mwGroupService.updateGroupState(param);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="用户组编辑")
    @PostMapping("/group/editor")
    @ResponseBody
    public ResponseBase updateGroup(@Validated({Update.class}) @RequestBody AddUpdateGroupParam param,
                                    HttpServletRequest request,
                                    RedirectAttributesModelMap model) {
        try {
            Reply reply = mwGroupService.update(param);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="分页查询用户组列表信息")
    @PostMapping("/group/browse")
    @ResponseBody
    public ResponseBase browseGroup(@RequestBody QueryGroupParam param,
                                    HttpServletRequest request,
                                    RedirectAttributesModelMap model) {
        try {
            Reply reply = mwGroupService.selectList(param);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="根据用户组ID获取用户组信息")
    @PostMapping("/group/popup/browse")
    @ResponseBody
    public ResponseBase browsePopupGroup(@RequestBody QueryGroupParam param,
                                         HttpServletRequest request,
                                         RedirectAttributesModelMap model) {
        try {
            Reply reply = mwGroupService.selectById(param.getGroupId());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="用户组下拉框查询")
    @PostMapping("/group/dropdown/browse")
    @ResponseBody
    public ResponseBase groupDropdownBrowse(HttpServletRequest request,
                                            RedirectAttributesModelMap model) {
        try {
            Reply reply = mwGroupService.selectDropdown();
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(),null);
        }
    }

    @ApiOperation(value="根据用户组ID获取用户组关联用户")
    @PostMapping("/group/GroupUser/browse")
    @ResponseBody
    public ResponseBase browseGroupUserById(@RequestBody QueryGroupParam param,
                                            HttpServletRequest request,
                                            RedirectAttributesModelMap model) {
        try {
            Reply reply = mwGroupCommonService.selectGroupUser(param.getGroupId());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="分页查询赛尔移动端用户组列表信息")
    @PostMapping("/cernet/group/browse")
    @ResponseBody
    public ResponseBase browseCernetGroup(@RequestBody QueryGroupParam param,
                                    HttpServletRequest request,
                                    RedirectAttributesModelMap model) {
        try {
            Reply reply = mwGroupService.getCernetGroup(param);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    /**
     * 获取用户模糊查询的数据
     *
     * @param qParam
     * @param request
     * @param model
     * @return
     */
    @PostMapping("/group/fuzzySearch/browse")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData(@RequestBody QueryGroupParam qParam,
                                              HttpServletRequest request,
                                              RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwGroupService.getFuzzySearchContent(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), "模糊查询所有字段资数据失败");
        }
        return setResultSuccess(reply);
    }



}
