package cn.mw.monitor.user.control;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.param.role.*;
import cn.mw.monitor.service.user.dto.MwRoleDTO;
import cn.mw.monitor.service.user.model.PageAuth;
import cn.mw.monitor.user.model.MwRole;
import cn.mw.monitor.user.service.MwModuleService;
import cn.mw.monitor.user.service.MwRoleService;
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
import java.util.List;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "角色权限管理接口",tags = "角色权限管理接口")
public class MWRoleController extends BaseApiService {

    @Autowired
    private MwRoleService mwRoleService;

    @Autowired
    private MwModuleService mwModuleService;

    @ApiOperation(value="重置模块权限映射信息")
    @PostMapping("/module-perm/reset")
    @ResponseBody
    public ResponseBase modulePermReset(HttpServletRequest request,
                                        RedirectAttributesModelMap model) {
        try {
            Reply reply = mwModuleService.modulePermReset();
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), null);
        }
    }

    @ApiOperation(value="重置角色模块权限映射信息")
    @PostMapping("/role-module-perm-mapper/reset")
    @ResponseBody
    public ResponseBase roleModulePermMapperReset(@RequestBody RoleModulePermResetParam param,
                                                  HttpServletRequest request,
                                                  RedirectAttributesModelMap model) {
        try {
            Reply reply = mwModuleService.roleModulePermMapperReset(param.getRoleId());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="获取角色模块权限映射信息")
    @PostMapping("/role-module-perm-mapper/browse")
    @ResponseBody
    public ResponseBase roleModulePermMapperBrowse(@RequestBody RoleModulePermResetParam param,
                                                   HttpServletRequest request,
                                                   RedirectAttributesModelMap model) {
        try {
            Reply reply = mwModuleService.roleModulePermMapperBrowse(param.getRoleId());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), param);
        }
    }

    @ApiOperation(value="角色新增")
    @PostMapping("/role/create")
    @ResponseBody
    public ResponseBase addRole(@Validated({Insert.class}) @RequestBody AddUpdateRoleParam auParam,
                                HttpServletRequest request,
                                RedirectAttributesModelMap model) {
        try {
            Reply reply = mwRoleService.insert(auParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), auParam);
        }
    }

    @ApiOperation(value="角色编辑")
    @PostMapping("/role/editor")
    @ResponseBody
    public ResponseBase updateRole(@Validated({Update.class}) @RequestBody AddUpdateRoleParam auParam,
                                HttpServletRequest request,
                                RedirectAttributesModelMap model) {
        try {
            Reply reply = mwRoleService.update(auParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), auParam);
        }
    }

    @ApiOperation(value="角色删除")
    @PostMapping("/role/delete")
    @ResponseBody
    public ResponseBase deleteRole(@RequestBody DeleteRoleParam dParam,
                                   HttpServletRequest request,
                                   RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwRoleService.delete(dParam.getRoleIdList());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), dParam);
        }
    }

    @ApiOperation(value="角色状态修改")
    @PostMapping("/role/perform")
    @ResponseBody
    public ResponseBase updateRoleState(@Validated @RequestBody UpdateRoleStateParam dParam,
                                        HttpServletRequest request,
                                        RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwRoleService.updateRoleState(dParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), dParam);
        }
    }

    @ApiOperation(value="分页查询角色列表信息")
    @PostMapping("/role/browse")
    @ResponseBody
    public ResponseBase browseRole(@RequestBody QueryRoleParam qParam,
                                   HttpServletRequest request,
                                   RedirectAttributesModelMap model) {
        try {
            Reply reply = mwRoleService.selectList(qParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    @ApiOperation(value="根据角色ID查询角色信息")
    @PostMapping(value = "/role/popup/browse")
    @ResponseBody
    public ResponseBase browseRolePopup(@RequestBody QueryRoleParam qParam,
                                        HttpServletRequest request,
                                        RedirectAttributesModelMap model) {
        try {
            Reply roleRreply = mwRoleService.selectByRoleId(qParam.getRoleId());
            Reply modulReply = mwModuleService.roleModulePermMapperBrowse(qParam.getRoleId());
            MwRole mwrole = (MwRole) roleRreply.getData();
            List<PageAuth> pageAuths = (List<PageAuth>) modulReply.getData();
            MwRoleDTO mwRoleDTO = CopyUtils.copy(MwRoleDTO.class, mwrole);
            mwRoleDTO.setPageAuth(pageAuths);
            return setResultSuccess(Reply.ok(mwRoleDTO));
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

    @ApiOperation(value="角色下拉框查询")
    @PostMapping("/role/dropdown/browse")
    @ResponseBody
    public ResponseBase roleDropdownBrowse(HttpServletRequest request,
                                           RedirectAttributesModelMap model) {
        try {
            Reply reply = mwRoleService.selectDorpdownList();
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), null);
        }
    }

    /**
     * 复制角色数据
     *
     * @param qParam  请求参数
     * @param request
     * @param model
     * @return
     */
    @ApiOperation(value = "角色状态复制")
    @PostMapping("/role/copy")
    @ResponseBody
    public ResponseBase copyRole(@Validated @RequestBody QueryRoleParam qParam,
                                 HttpServletRequest request,
                                 RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwRoleService.copyRole(qParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage(),e);
            return setResultFail(e.getMessage(), qParam);
        }
    }

   /*
   * 移至dev-admin包下
   *@ApiOperation(value="模块新增")
    @PostMapping("/role/module/create")
    @ResponseBody
    public ResponseBase addModule(@Validated({Insert.class}) AddUpdateModuleParam mParam, HttpServletRequest request,
                                             RedirectAttributesModelMap model) {
        try {
            Reply reply = mwRoleService.insertRoleModule(mParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), mParam);
        }
    }

    @ApiOperation(value="删除模块")
    @PostMapping("/role/module/delete")
    @ResponseBody
    public ResponseBase delModule(@Validated  DeleteModuleParam dParam, HttpServletRequest request,
                                  RedirectAttributesModelMap model) {
        try {
            Reply reply = mwRoleService.deleteRoleModule(dParam.getIds());
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), dParam);
        }
    }

    @ApiOperation(value="模块编辑")
    @PostMapping("/role/module/editor")
    @ResponseBody
    public ResponseBase updateModule(@Validated({Update.class}) @RequestBody AddUpdateModuleParam mParam,
                                   HttpServletRequest request,
                                   RedirectAttributesModelMap model) {
        try {
            Reply reply = mwRoleService.updateRoleModule(mParam);
            return setResultSuccess(reply);
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail(e.getMessage(), mParam);
        }
    }
*/
}
