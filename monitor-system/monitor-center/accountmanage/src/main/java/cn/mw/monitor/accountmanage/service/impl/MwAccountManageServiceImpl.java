package cn.mw.monitor.accountmanage.service.impl;

import cn.mw.monitor.accountmanage.dao.MwAccountManageTableDao;
import cn.mw.monitor.accountmanage.entity.AddAccountManageParam;
import cn.mw.monitor.accountmanage.entity.MwAccountManageTable;
import cn.mw.monitor.accountmanage.entity.MwQueryAccountManageTable;
import cn.mw.monitor.accountmanage.entity.QueryAccountManageParam;
import cn.mw.monitor.accountmanage.service.MwAccountManageService;
import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
@Transactional
public class MwAccountManageServiceImpl implements MwAccountManageService {

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWCommonService mwCommonService;

    @Resource
    MwAccountManageTableDao mwAccountManageTableDao;

    @Autowired
    private MWUserService userService;

    private final static String DEFAULT_PASSWORD = "0*0*0";

    /**
     * 获取单个账户信息
     *
     * @param param 账户信息
     * @return
     * @throws Exception
     */
    @Override
    public Reply selectList1(QueryAccountManageParam param) throws Exception {
        try {
            MwQueryAccountManageTable s = mwAccountManageTableDao.selectOne(param);
            //获取数据权限
            DataPermission dataPermission = mwCommonService.getDataPermission(param);
            s.setPassword(DEFAULT_PASSWORD);
            s.setPrincipal(dataPermission.getUserIds());
            s.setOrgIdss(dataPermission.getOrgIds());
            s.setOrgIds(dataPermission.getOrgNodes());
            s.setGroupIds(dataPermission.getGroupIds());
            return Reply.ok(s);
        } catch (Exception e) {
            log.error("获取账户数据信息失败", e);
            return Reply.fail("获取账户数据信息失败");
        }
    }

    @Override
    public Reply selectList(QueryAccountManageParam qParam) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            List<String> idList = userService.getAllTypeIdList(userInfo, qParam.getBaseDataType());

            Map criteria = PropertyUtils.describe(qParam);
            criteria.put("isSystem", userInfo.isSystemUser());
            criteria.put("listSet", Joiner.on(",").join(idList));

            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            List<MwAccountManageTable> list = mwAccountManageTableDao.selectList(criteria);

            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("获取列表失败", e);
            return Reply.fail(500, "账号管理查询失败");
        }
    }

    @Override
    public Reply delete(List<Integer> auParam) throws Exception {
        try {
            //删除账号管理的 负责人，用户组，机构 权限关系
            if (auParam != null && auParam.size() > 0) {
                //批量删除数据权限
                AddAccountManageParam account = new AddAccountManageParam();
                List<String> deleteIdList = new ArrayList<>();
                for (Integer id : auParam) {
                    deleteIdList.add(id + "");
                }
                account.setDeleteIdList(deleteIdList);
                mwCommonService.deleteMapperAndPerm(account);
            }
            //删除账号管理
            mwAccountManageTableDao.deleteBatch(auParam);
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("删除失败", e);
            return Reply.fail("删除失败");
        }
    }

    @Override
    public Reply update(AddAccountManageParam auParam) throws Exception {
        Reply checkReply = checkParam(auParam, true);
        if (checkReply != null) {
            return checkReply;
        }

        GlobalUserInfo userInfo = userService.getGlobalUser();

        //修改账号管理 主信息
        auParam.setModifier(userInfo.getLoginName());
        auParam.setModificationDate(new Date());

        QueryAccountManageParam a = new QueryAccountManageParam();
        a.setId(auParam.getId());
        MwQueryAccountManageTable s = mwAccountManageTableDao.selectOne(a);

        if (s.getPassword() == null || s.getPassword().equals("")) {
            if (auParam.getPassword() != null && !auParam.getPassword().equals("") && (!DEFAULT_PASSWORD.equals(auParam.getPassword()))) {
                auParam.setPassword(EncryptsUtil.encrypt(auParam.getPassword()));
            } else {
                auParam.setPassword(null);
            }
        } else {
            if (auParam.getPassword().equals(DEFAULT_PASSWORD)) {
                auParam.setPassword(null);
            } else {
                auParam.setPassword(EncryptsUtil.encrypt(auParam.getPassword()));
            }
            if (EncryptsUtil.decrypt(s.getPassword()).equals(auParam.getPassword())) {
                auParam.setPassword(null);
            }
        }

        if (s.getEnablePassword() == null || s.getEnablePassword().equals("")) {
            if (auParam.getEnablePassword() != null && !auParam.getEnablePassword().equals("")) {
                auParam.setEnablePassword(EncryptsUtil.encrypt(auParam.getEnablePassword()));
            }
        } else {
            //解决重复提交多次加密问题
            if (!s.getEnablePassword().equals(auParam.getEnablePassword())) {
                if (EncryptsUtil.decrypt(s.getEnablePassword()).equals(auParam.getEnablePassword())) {
                    auParam.setEnablePassword(null);
                } else {
                    auParam.setEnablePassword(EncryptsUtil.encrypt(auParam.getEnablePassword()));
                }
            }
        }
        mwAccountManageTableDao.update(auParam);

        //修改ip地址管理的 负责人，用户组，机构 权限关系
        mwCommonService.updateMapperAndPerm(auParam);

        return Reply.ok(auParam);
    }

    @Override
    public Reply insert(AddAccountManageParam auParam) throws Exception {
        Reply checkReply = checkParam(auParam, false);
        if (checkReply != null) {
            return checkReply;
        }

        GlobalUserInfo userInfo = userService.getGlobalUser();

        //添加ip地址管理 主信息
        auParam.setCreator(userInfo.getLoginName());
        auParam.setCreateDate(new Date());
        auParam.setModifier(userInfo.getLoginName());
        auParam.setModificationDate(new Date());

        String pass = auParam.getPassword();
        if (pass != null && !pass.equals("")) {
            String str = EncryptsUtil.encrypt(pass);
            auParam.setPassword(str);
        }

        String enablePas = auParam.getEnablePassword();
        if (enablePas != null && !enablePas.equals("")) {
            auParam.setEnablePassword(EncryptsUtil.encrypt(enablePas));
        }
        mwAccountManageTableDao.insert(auParam);

        //添加数据权限
        mwCommonService.addMapperAndPerm(auParam);
        return Reply.ok(auParam);
    }

    @Override
    public Reply selectDrop() throws Exception {
        List<HashMap<String, String>> hashMaps = mwAccountManageTableDao.selectDrop();
        return Reply.ok(hashMaps);
    }



    /**
     * 校验账户数据
     *
     * @param auParam  账户数据
     * @param isUpdate 是否为更新方式
     * @return
     */
    private Reply checkParam(AddAccountManageParam auParam, boolean isUpdate) {
        if (StringUtils.isEmpty(auParam.getAccount())) {
            return Reply.fail("账户名称不能为空");
        }
        if (StringUtils.isEmpty(auParam.getUsername())) {
            return Reply.fail("用户名不能为空");
        }
        if (StringUtils.isEmpty(auParam.getPassword())) {
            return Reply.fail("密码不能为空");
        }
        if (StringUtils.isEmpty(auParam.getProtocol())) {
            return Reply.fail("连接协议不能为空");
        }
        if (StringUtils.isEmpty(auParam.getPort())) {
            return Reply.fail("端口不能为空");
        }
        if (isUpdate) {
            QueryAccountManageParam param = new QueryAccountManageParam();
            param.setId(auParam.getId());
            MwQueryAccountManageTable s = mwAccountManageTableDao.selectOne(param);
            //修改账号名称再判断
            if (s != null && !s.getAccount().equals(auParam.getAccount())) {
                if (mwAccountManageTableDao.checkAccountNameRepeat(auParam.getAccount())) {
                    return Reply.fail("账户名称不能重复");
                }
            }
        } else {
            if (mwAccountManageTableDao.checkAccountNameRepeat(auParam.getAccount())) {
                return Reply.fail("账户名称不能重复");
            }
        }
        return null;
    }
}
