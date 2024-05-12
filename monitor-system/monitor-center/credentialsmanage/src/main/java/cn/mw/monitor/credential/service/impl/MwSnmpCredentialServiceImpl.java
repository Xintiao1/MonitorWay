package cn.mw.monitor.credential.service.impl;


import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.credential.api.param.QueryCredentialParam;
import cn.mw.monitor.credential.dao.MwSnmpCredentialDao;
import cn.mw.monitor.credential.model.MwSnmpCredential;
import cn.mw.monitor.credential.model.MwSnmpPortCredential;
import cn.mw.monitor.credential.service.MwSnmpCredentialService;
import cn.mw.monitor.credential.util.MapResultHandler;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.util.MWUtils;
import cn.mwpaas.common.model.Reply;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * (MwSnmpCredential)表服务实现类
 *
 * @author makejava
 * @since 2021-05-31 14:14:37
 */
@Service("mwSnmpCredentialService")
@Slf4j
@Transactional
public class MwSnmpCredentialServiceImpl implements MwSnmpCredentialService {
    @Resource
    private MwSnmpCredentialDao mwSnmpCredentialDao;

    @Autowired
    ILoginCacheInfo loginCacheInfo;

    /**
     * 系统凭据下拉框查询
     *
     * @return 实例对象
     */
    @Override
    public Reply selectCredDropDown() {
        MapResultHandler<String, String> credResultHandler = new MapResultHandler<>();
        try {
            mwSnmpCredentialDao.selectCredDropDown(credResultHandler);
            Map<String, String> mappedResults = credResultHandler.getMappedResults();
            return Reply.ok(mappedResults);
        }catch (Exception e) {
            log.error(" fail to select snmp commName credential dropdown list ", e);
            return Reply.fail(ErrorConstant.CRED_317005, ErrorConstant.CRED_MSG_317005);
        }
    }

    @Override
    public Reply selectCredById(Integer credId) {

        try {
            MwSnmpCredential snmpCred = mwSnmpCredentialDao.selectByPrimaryKey(credId);
            return Reply.ok(snmpCred);
        }catch (Exception e) {
            log.error("fail to select snmp comm_name cred by id :",e);
            return Reply.fail(ErrorConstant.CRED_MSG_317006,ErrorConstant.CRED_317006);
        }
    }

    public boolean isRoleTopId () {
        String loginName = loginCacheInfo.getLoginName();
        String roleId = loginCacheInfo.getRoleId(loginName);
        return MWUtils.ROLE_TOP_ID.equals(roleId);
    }
    /**
     * 查询系统和凭据列表
     * @return 对象列表
     */
    @Override
    public Reply pageCredential(QueryCredentialParam param,boolean flag) {
        try {
            PageHelper.startPage(param.getPageNumber(),param.getPageSize());
            Map<String, Object> describe = PropertyUtils.describe(param);
            List<MwSnmpCredential> mscs = new ArrayList<>();
            if (flag) {
                mscs = mwSnmpCredentialDao.select(describe,null);
            }else {
                mscs = mwSnmpCredentialDao.select(describe,loginCacheInfo.getLoginName());
            }
            PageInfo<MwSnmpCredential> pageInfo = new PageInfo<>(mscs);
            return Reply.ok(pageInfo);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(" fail to browse snmp commName credential list ", e);
            return Reply.fail(ErrorConstant.CRED_317004, ErrorConstant.CRED_MSG_317004);
        }
    }

    /**
     * 新增系统凭据
     */
    @Override
    public Reply insert(MwSnmpCredential msp) {
        try {
            String creator = loginCacheInfo.getLoginName();
            msp.setCreator(creator);
            mwSnmpCredentialDao.insert(msp);
        }catch (Exception e) {
            log.error(" fail to insert snmp commName credential ",e);
            return Reply.fail(ErrorConstant.CRED_MSG_317001,ErrorConstant.CRED_MSG_317001);
        }
        return Reply.ok("新增凭据成功!");
    }

    /**
     * 修改数据
     *
     * @param msp 实例对象
     * @return 实例对象
     */
    @Override
    public Reply update(MwSnmpCredential msp) {
        try {
            mwSnmpCredentialDao.updateByPrimaryKeySelective(msp);
        }catch (Exception e) {
            log.error(" fail to update snmp commName credential ",e);
            return Reply.fail(ErrorConstant.CRED_MSG_317002,ErrorConstant.CRED_MSG_317002);
        }
        return Reply.ok("修改凭据成功!");
    }

    /**
     * 通过主键删除数据
     *
     * @param ids 主键集合
     * @return 是否成功
     */
    @Override
    public Reply deleteById(List<Integer> ids) {
        try {
            ids.forEach(
                    id->{
                        mwSnmpCredentialDao.deleteByPrimaryKey(id);
                    }
            );
        }catch (Exception e) {
            log.error("fail to delete snmp commName credential with ids={}",ids,e);
            return Reply.fail(ErrorConstant.CRED_MSG_317003,ErrorConstant.CRED_MSG_317003);
        }
        return Reply.ok("删除凭据成功!");
    }
}
