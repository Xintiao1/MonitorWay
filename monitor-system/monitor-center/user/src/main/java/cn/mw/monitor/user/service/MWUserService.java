package cn.mw.monitor.user.service;

import cn.mw.monitor.api.param.user.*;
import cn.mw.monitor.service.user.dto.SettingDTO;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.service.user.param.LoginParam;
import cn.mw.monitor.user.dto.MwUserDTO;
import cn.mw.monitor.service.user.dto.UserDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.dsig.TransformException;
import java.io.IOException;
import java.util.List;

public interface MWUserService {


    String getQyWeixinAccessToken(String appid, String appsecret) throws IOException;
    /**
     * 添加用户
     */
    Reply addUser(UserDTO userdto) throws Throwable;
    /**
     * 查询负责人
     * @param typeId
     * @param type
     */
    List<MwUserDTO> selectResponser(String typeId, String type);
    /**
     * 根据角色ID取用户信息
     *
     * @param roleId 角色ID
     * @return
     */
//    Reply selectByRoleId(Integer roleId);

    /**
     * 根据角色ID取用户信息
     */
    Reply selectById(Integer userId);

    /**
     * 根据角色ID取用户信息
     *
     * @param userId 用户ID
     * @return
     */
    Reply selectByUserId(Integer userId);

    /**
     * 根据登录名取用信息
     *
     * @param loginName 用户名
     * @return
     */
    Reply selectByLoginName(String loginName);

    /**
     * 根据用户微信openId取用信息
     *
     * @param openId 用户微信openId
     * @return
     */
    Reply selectByOpenid(String openId);

    /**
     * 查询机构及子机构下的用户信息信息
     *
     * @param
     * @return
     */
    Reply selectListByPerm(List<Integer> orgIds);

    /**
     * 分页查询用户信息
     *
     * @param qParam 用户信息
     * @return
     */
    Reply pageUser(QueryUserParam qParam);

    /**
     * 根据机构ID查询用户
     * @param orgId
     * @return
     */
    public Reply getUserByOrgId(Integer orgId);



    /**
     * 更新用户
     * @param userdto
     * @return
     */
    public Reply updateUser(UserDTO userdto) throws Throwable;

    /**
     * 更新用户OpenId
     * @param userdto
     * @return
     */
    public Reply updateUserOpenId(UserDTO userdto) throws Throwable;

    /**
     * 解锁用户
     * @param userdto
     * @return
     */
    public Reply unlock(UserDTO userdto) throws Throwable;

    /**
     * 用户登录
     * @param loginParam
     * @return
     */
    public Reply userlogin(LoginParam loginParam) throws Throwable;

    /**
     * 用户免密登录
     * @param loginParam
     * @return
     */
    public Reply userlogin(LoginParam loginParam,Boolean unPass) throws Throwable;
    /**
     * 用户登出
     * @param token
     * @return
     */
    public Reply userlogout(String token) throws Throwable;

    /**
     * 用户信息获取
     * @param token
     * @return
     */
    public Reply loginInfo(String token) throws Throwable;

    /**
     * 用户下拉框查询
     */
    Reply getDropDownUser();

    Reply delete(List<Integer> idList);

//    Reply updatePassword(UserDTO userdto) throws Throwable;

    Reply updateState(UpdateUserStateParam updateUserStateParam);

    Reply customColLoad(UserDTO userdto) throws Throwable;

    Reply insertSettings(SettingDTO settingDTO);

    Reply selectSettingsInfo();
    /**
     * 用户个人密码修改
     * @param userdto
     * @return
     */
    Reply updatePassword(UserDTO userdto);
    /**
     * 用户个人基本信息修改
     * @param userdto
     * @return
     */
    Reply updateUserInfo(UserDTO userdto);

    /**
     * 查询用户信息以及用户的机构信息
     * @param userdto
     * @return
     */
    Reply selectCurrUserInfo(UserDTO userdto);

    /*重置密码*/
    Reply resetPassword(UserDTO userDTO) throws Throwable;

    /**
     * 批量更新用户信息
     *
     * @param userDTO 用户数据
     * @return
     */
    Reply batchUpdateUsers(UserDTO userDTO);

    Reply selectAdUser(String loginName);

    Reply pageCernetUser(QueryUserParam qParam);

    Reply getUserList(QueryUserParam qParam);

    /**
     * 获取模糊查询内容
     * @param qParam 请求参数
     * @return 模糊查询列表数据
     */
    Reply getFuzzySearchContent(QueryUserParam qParam);

    /**
     * 获取当前登录用户的信息
     *
     * @return 用户信息
     */
    GlobalUserInfo getGlobalUser();


    /**
     * 根据用户ID获取用户的信息
     *
     * @return 用户信息
     */
    GlobalUserInfo getGlobalUser(Integer userId);

    /**
     * 获取所有的用户列表
     *
     * @param typeId   类别ID
     * @param dataType 类别
     * @return 获取所有的用户列表
     */
    List<cn.mw.monitor.service.assets.model.UserDTO> getAllUserList(int typeId, DataType dataType);

    /**
     * 获取所有的类别ID
     *
     * @param userInfo 用户信息
     * @param dataType 类别
     * @return 当前用户在该类别下的所有ID
     */
    List<String> getAllTypeIdList(GlobalUserInfo userInfo, DataType dataType);

    /**
     * 获取所有的类别ID
     *
     * @param userInfo 用户信息
     * @param dataType 类别
     * @return 当前用户在该类别下的所有ID
     */
    List<String> getTypeIdListByOrgIds(GlobalUserInfo userInfo, DataType dataType);

    /**
     * 获取所有的类别ID
     *
     * @param userInfo 用户信息
     * @param dataType 类别
     * @return 当前用户在该类别下的所有ID
     */
    List<String> getTypeIdListByGroupIds(GlobalUserInfo userInfo, DataType dataType);

    /**
     * 导出用户导入excel模板
     *
     * @param response 导出数据
     */
    void excelTemplateExport(HttpServletResponse response);

    /**
     * 导出用户导入excel模板
     *
     * @param response 导出数据
     * @param qParam 用户数据
     */
    void exportUserExcel(HttpServletResponse response,QueryUserParam qParam);

    /**
     * 用户批量导入
     *
     * @param file     excel文件数据
     * @param response 失败数据返回
     */
    void excelImport(MultipartFile file, HttpServletResponse response);

    /**
     * 校验用户导入数据
     *
     * @param userParam 用户导入数据
     * @return 用户导入数据
     * @throws TransformException 校验异常数据
     */
    RegisterParam transform(ExportUserParam userParam) throws TransformException;

    /**
     * 获取所有的负责人列表信息
     *
     * @param param 参数
     * @return
     */
    Reply getAuthUserList(ChangeUserParam param);

    /**
     * 更新负责人信息
     *
     * @param param 参数
     * @return
     */
    Reply changeUserAuth(ChangeUserParam param);
}
