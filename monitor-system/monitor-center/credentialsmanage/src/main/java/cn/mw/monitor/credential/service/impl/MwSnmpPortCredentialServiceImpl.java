package cn.mw.monitor.credential.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.credential.api.param.QueryCredentialParam;
import cn.mw.monitor.credential.common.SNMPVersionType;
import cn.mw.monitor.credential.dao.MwSnmpCredentialDao;
import cn.mw.monitor.credential.dao.MwSnmpPortCredentialDao;
import cn.mw.monitor.credential.dto.MwSNMPCredDTO;
import cn.mw.monitor.credential.model.MwSnmpCredential;
import cn.mw.monitor.credential.model.MwSnmpPortCredential;
import cn.mw.monitor.credential.model.MwSysCredential;
import cn.mw.monitor.credential.service.MwSnmpPortCredentialService;
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
 * (MwSnmpPortCredential)表服务实现类
 *
 * @author makejava
 * @since 2021-05-31 14:15:28
 */
@Service("mwSnmpPortCredentialService")
@Slf4j
@Transactional
public class MwSnmpPortCredentialServiceImpl implements MwSnmpPortCredentialService {
    @Resource
    private MwSnmpPortCredentialDao mwSnmpPortCredentialDao;

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
    public Reply selectCredDropDown(QueryCredentialParam param,boolean flag) {
        MwSNMPCredDTO mscd = null;
        Integer moduleId = param.getModuleId();
        try {
            if (SNMPVersionType.SNMPv1v2.name().equals(param.getSnmpVersion())) {
                List<String> ports = new ArrayList<>();
                List<String> commNames = new ArrayList<>();

                if (flag) {
                    ports = mwSnmpPortCredentialDao.selectSNMPCred(null,moduleId);
                    commNames = mwSnmpCredentialDao.selectSNMPCommCred(null,moduleId);
                } else {
                    ports = mwSnmpPortCredentialDao.selectSNMPCred(loginCacheInfo.getLoginName(),moduleId);
                    commNames = mwSnmpCredentialDao.selectSNMPCommCred(loginCacheInfo.getLoginName(),moduleId);
                }
                mscd = MwSNMPCredDTO.builder()
                        .portList(ports)
                        .commNameList(commNames).build();
            }
            return Reply.ok(mscd);
        }catch (Exception e) {
            log.error(" fail to select snmp credential dropdown list ", e);
            return Reply.fail(ErrorConstant.CRED_317005, ErrorConstant.CRED_MSG_317005);
        }
    }

    @Override
    public Reply selectCredById(Integer credId) {

        try {
            MwSnmpPortCredential snmpCred = mwSnmpPortCredentialDao.selectByPrimaryKey(credId);
            return Reply.ok(snmpCred);
        }catch (Exception e) {
            log.error("fail to select snmp port cred by id :",e);
            return Reply.fail(ErrorConstant.CRED_MSG_317006,ErrorConstant.CRED_317006);
        }
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
            List<MwSnmpPortCredential> mscs = new ArrayList<>();
            try {
                if (flag) {
                    mscs = mwSnmpPortCredentialDao.select(describe,null);
                } else {
                    mscs = mwSnmpPortCredentialDao.select(describe,loginCacheInfo.getLoginName());
                }
            }catch (Exception e) {
                log.error("fail to browse snmp port credential list:",e);
                return Reply.fail(ErrorConstant.CRED_317004, ErrorConstant.CRED_MSG_317004);
            }
            PageInfo<MwSnmpPortCredential> pageInfo = new PageInfo<>(mscs);
            return Reply.ok(pageInfo);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error(" fail to browse snmp port credential list ", e);
            return Reply.fail(ErrorConstant.CRED_317004, ErrorConstant.CRED_MSG_317004);
        }
    }

    /**
     * 新增系统凭据
     */
    @Override
    public Reply insert(MwSnmpPortCredential msp) {
        try {
            String creator = loginCacheInfo.getLoginName();
            msp.setCreator(creator);
            mwSnmpPortCredentialDao.insert(msp);
        }catch (Exception e) {
            log.error(" fail to insert snmp port credential ",e);
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
    public Reply update(MwSnmpPortCredential msp) {
        try {
            mwSnmpPortCredentialDao.updateByPrimaryKeySelective(msp);
        }catch (Exception e) {
            log.error(" fail to update snmp port credential ",e);
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
                        mwSnmpPortCredentialDao.deleteByPrimaryKey(id);
                    }
            );
        }catch (Exception e) {
            log.error("fail to delete snmp port credential with ids={}",ids,e);
            return Reply.fail(ErrorConstant.CRED_MSG_317003,ErrorConstant.CRED_MSG_317003);
        }
        return Reply.ok("删除凭据成功!");
    }
}
