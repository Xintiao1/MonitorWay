package cn.mw.monitor.dev.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.api.exception.CheckDeleteModuleException;
import cn.mw.monitor.api.param.role.AddUpdateModuleParam;
import cn.mw.monitor.dev.service.MWModuleService;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.PageAuth;
import cn.mw.monitor.user.dao.*;
import cn.mw.monitor.user.model.MwModule;
import cn.mw.monitor.user.model.MwModulePermMapper;
import cn.mw.monitor.user.model.MwPermission;
import cn.mw.monitor.user.service.impl.MwModuleServiceImpl;
import cn.mw.monitor.util.ErrorMsgUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
@Slf4j
@Transactional
public class MWModuleServiceImpl implements MWModuleService {
    public static final Integer ROOT_ROLE = 0;
    private static final String redisGroup = MwModuleServiceImpl.class.getSimpleName();

    @Resource
    MwRoleDao mwRoleDao;

    @Resource
    MwUserRoleMapperDao mwUserRoleMapperDao;

    @Resource
    private MwPermissionDao mwPermissionDao;

    @Resource
    MwRoleModulePermMapperDao mwRoleModulePermMapperDao;

    @Resource
    MwModuleDao mwModuleDao;

    @Autowired
    private RedisTemplate<String, Object> redisObjectTemplate;

    @Resource
    private MwModulePermMapperDao mwModulePermMapperDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Override
    public Reply insertRoleModule(AddUpdateModuleParam mParam) {
        try {
            MwModule mwModule = CopyUtils.copy(MwModule.class,mParam);
            //新增模块信息
            int count = mwModuleDao.selectMaxModuleId()+1;
            mwModule.setId(count);
            //判断是否不为一级模块
            if (null != mwModule.getPid() && mwModule.getPid() != ROOT_ROLE) {
                mwModuleDao.updateIsNoteById(mwModule.getPid(), false);
                Map<String, Object> map = mwModuleDao.selectDeepNodesById(mwModule.getPid());
                mwModule.setDeep((Integer) map.get("deep") + 1);
                mwModule.setNodes(map.get("nodes").toString() + ","+ count);
            } else {
                mwModule.setDeep(1);
                mwModule.setPid(ROOT_ROLE);
                mwModule.setNodes(count+"");
            }
            //编辑模块内部信息
            mwModule.setEnable("1");
            mwModule.setVersion(0);
            mwModule.setIsNode(true);
            mwModule.setDeleteFlag(false);
            mwModuleDao.insert(mwModule);

            //新增模块后 同时更新模块权限信息
            mwModulePermMapperDao.clearMapper();
            List<MwModule> mlist = mwModuleDao.selectList();
            List<MwPermission> plist = mwPermissionDao.selectList();
            List<MwModulePermMapper> mpList = new ArrayList<>();
            for (MwModule mwModule1 : mlist) {
                for (MwPermission mwPermission : plist) {
                    mpList.add(MwModulePermMapper
                            .builder()
                            .moduleId(mwModule1.getId())
                            .permId(mwPermission.getId())
                            .build()
                    );
                }
            }
            mwModulePermMapperDao.insert(mpList);

            synchronized (this) {
                List<MwModulePermMapper> list = mwModulePermMapperDao.selectList();
                if(null != list ){
                    for(MwModulePermMapper mwModulePermMapper : list){
                        String url = mwModulePermMapper.getUrl();
                        if(null == url){
                            continue;
                        }
                        String key = genRedisKey(mwModulePermMapper);
                        redisObjectTemplate.opsForValue().set(key, mwModulePermMapper);
                    }
                }
            }
            return Reply.ok("新增成功！");
        } catch (Exception e) {
            log.error("fail to insertRoleModele with AddUpdateModuleParam=【{}】, cause:【{}】",
                    mParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.MODULE_100213, ErrorConstant.MODULE_MSG_100213));
        }
    }

    private String genRedisKey(MwModulePermMapper mapper){
        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":").append(mapper.getUrl())
                .append("/").append(mapper.getPermName());
        return sb.toString();
    }

    @Override
    public Reply deleteRoleModule(List<Integer> ids) {
        try {
            Map<String, StringBuffer> maps = new HashMap<>();
            ids.forEach(
                    id -> {
                        MwModule mwModule = mwModuleDao.selectModuleById(id);
                        String name = mwModule.getModuleName();
                        int count = mwModuleDao.countModuleByPid(id);
                        if (count > 0) {
                            if (maps.get(ErrorConstant.MODULE_MSG_100214) == null) {
                                maps.put(ErrorConstant.MODULE_MSG_100214, new StringBuffer());
                            }
                            maps.get(ErrorConstant.MODULE_MSG_100214).append("、").append(name);
                        }
                    }
            );

            String msg = ErrorMsgUtils.getErrorMsg(maps);
            if (StringUtils.isNotEmpty(msg)) {
                throw new CheckDeleteModuleException(ErrorConstant.MODULE_MSG_100215, msg);
            }
            mwModuleDao.deleteModuleByIds(ids);
            mwModuleDao.deleteModulePermByIds(ids);
            synchronized (this) {
                List<MwModulePermMapper> list = mwModulePermMapperDao.selectList();
                if(null != list ){
                    for(MwModulePermMapper mwModulePermMapper : list){
                        String url = mwModulePermMapper.getUrl();
                        if(null == url){
                            continue;
                        }
                        String key = genRedisKey(mwModulePermMapper);
                        redisObjectTemplate.opsForValue().set(key, mwModulePermMapper);
                    }
                }
            }
            return Reply.ok("删除成功！");
        } catch (Exception e) {
            log.error("fail to deleteModule with moduleIds=【{}】, cause:【{}】",
                    ids, e.getMessage());
            if (e instanceof CheckDeleteModuleException) {
                throw e;
            } else {
                throw new ServiceException(
                        Reply.fail(ErrorConstant.MODULE_100215, ErrorConstant.MODULE_MSG_100215));
            }
        }
    }

    @Override
    public Reply updateRoleModule(AddUpdateModuleParam mParam) {
        try {
            MwModule mwModule = CopyUtils.copy(MwModule.class,mParam);
            mwModuleDao.updateModule(mwModule);
            return Reply.ok("更新成功！");
        }catch (Exception e) {
            log.error("fail to updateModule with AddUpdateModuleParam=【{}】, cause:【{}】",
                    mParam, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.MODULE_100216, ErrorConstant.MODULE_MSG_100216));
        }
    }

    @Override
    public Reply selectRoleModule(Integer id) {
        try {
            MwModule mwModule = mwModuleDao.selectModuleById(id);
            return Reply.ok(mwModule);
        }catch (Exception e) {
            throw new ServiceException(
                    Reply.fail(ErrorConstant.MODULE_100217, ErrorConstant.MODULE_MSG_100217));
        }
    }

}
