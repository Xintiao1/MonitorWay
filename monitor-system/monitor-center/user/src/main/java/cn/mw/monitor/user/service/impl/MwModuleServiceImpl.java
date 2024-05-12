package cn.mw.monitor.user.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.model.PageAuth;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.user.dao.*;
import cn.mw.monitor.user.model.MwModule;
import cn.mw.monitor.user.model.MwModulePermMapper;
import cn.mw.monitor.user.model.MwPermission;
import cn.mw.monitor.user.service.MwModuleService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service("mwModuleServiceImpl")
@Slf4j
public class MwModuleServiceImpl implements ApplicationRunner, MwModuleService {

    private static final String redisGroup = MwModuleServiceImpl.class.getSimpleName();

    @Value("${scheduling.enabled}")
    private boolean isTimer;

    @Value("${userDebug}")
    boolean userDebug;

    @Autowired
    private RedisTemplate<String, Object> redisObjectTemplate;

    @Resource
    private MwRoleModulePermMapperDao mwRoleModulePermMapperDao;

    @Resource
    private MwModulePermMapperDao mwModulePermMapperDao;

    @Resource
    private MwModuleDao mwModuleDao;

    @Resource
    private MwPermissionDao mwPermissionDao;

    /**
     * 重置模块权限映射信息
     */
    @Override
    public Reply modulePermReset() {
        try {
            mwModulePermMapperDao.clearMapper();
            List<MwModule> mlist = mwModuleDao.selectList();
            List<MwPermission> plist = mwPermissionDao.selectList();
            List<MwModulePermMapper> mpList = new ArrayList<>();
            for (MwModule mwModule : mlist) {
                for (MwPermission mwPermission : plist) {
                    mpList.add(MwModulePermMapper
                            .builder()
                            .moduleId(mwModule.getId())
                            .permId(mwPermission.getId())
                            .build()
                    );
                }
            }
            mwModulePermMapperDao.insert(mpList);
            return Reply.ok("重置成功！");
        } catch (Exception e) {
            log.error("fail to modulePermReset with cause:【{}】", e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.MODULE_100210, ErrorConstant.MODULE_MSG_100210));
        }
    }

    /**
     * 重置角色模块权限映射信息
     */
    @Override
    public Reply roleModulePermMapperReset(Integer roleId) {
        try {
            mwRoleModulePermMapperDao.updateEnableByRoleId(roleId);
            return Reply.ok("重置成功！");
        } catch (Throwable e){
            log.error("fail to roleModulePermMapperReset with roleId=【{}】, cause:【{}】",
                    roleId, e.getMessage());
            throw new ServiceException(
                    Reply.fail(ErrorConstant.MODULE_100211, ErrorConstant.MODULE_MSG_100211));
        }
    }

    /**
     * 获取角色模块权限映射信息
     */
    @Override
    public Reply roleModulePermMapperBrowse(Integer roleId) {
        try {
            List<PageAuth> pageAuths = mwRoleModulePermMapperDao.selectModulePermByRoleId(roleId);
            // 制作树状结构
            List<PageAuth> topRoleList = genModuleTree(pageAuths);
            return Reply.ok(topRoleList);
        } catch (Exception e) {
            log.error("fail to roleModulePermMapperBrowse with roleId=【{}】",
                    roleId, e);
            throw new ServiceException(
                    Reply.fail(ErrorConstant.ROLE_100212,ErrorConstant.ROLE_MSG_100212));
        }
    }

    /**
     * 获取模块信息
     */
    @Override
    public Reply getModuleInfo(Map criteria) {
        try {
            List<MwModule> list = mwModuleDao.selectModule(criteria);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getModuleInfo",e);
            throw new ServiceException(
                    Reply.fail(ErrorConstant.MODULE_100217,ErrorConstant.MODULE_MSG_100217));
        }
    }

    private List<PageAuth> genModuleTree(List<PageAuth> pageAuths){
        List<PageAuth> topRoleList = new ArrayList<>();
        List<PageAuth> childRoleList  = new ArrayList<>();

        // 如果深度为1则是标题栏
        pageAuths.forEach(pageAuth -> {
            if (pageAuth.getDeep() == 1) {
                topRoleList.add(pageAuth);
            } else {
                childRoleList.add(pageAuth);
            }
        });
        Set<Integer> pageIdSet = new HashSet<>(childRoleList.size());
        topRoleList.forEach(roleTop ->
                getChild(roleTop, childRoleList, pageIdSet));

        return topRoleList;
    }

    private void getChild(PageAuth pageAuth, List<PageAuth> childRoleList,
                          Set<Integer> orgIdSet) {
        List<PageAuth> childList = new ArrayList<>();
        childRoleList.stream()
                // 判断是否已循环过当前对象
                .filter(child -> !orgIdSet.contains(child.getPageId()))
                // 判断是否为父子关系
                .filter(child -> child.getPid().equals(pageAuth.getPageId()))
                // orgIdSet集合大小不超过mwOrgDTOList的大小
                .filter(child -> orgIdSet.size() <= childRoleList.size())
                .forEach(
                        // 放入pageIdSet,递归循环时可以跳过这个项目，提交循环效率
                        child -> {
                            orgIdSet.add(child.getPageId());
                            //获取当前类目的子类目
                            getChild(child, childRoleList, orgIdSet);
                            childList.add(child);
                        }
                );
        pageAuth.addChild(childList);
    }

    @Override
    public String getModulePermKey(String url) {
        //从url中分离出权限操作
        int lastSeq = url.lastIndexOf("/");
        String ops = url.substring(lastSeq + 1);
        String searchUrl = url.substring(0, lastSeq);

        MwModulePermMapper mwModulePermMapper = MwModulePermMapper.builder().moduleId(0).permId(0).build();
        mwModulePermMapper.setUrl(searchUrl);
        mwModulePermMapper.setPermName(ops);
        String searchKey = genRedisKey(mwModulePermMapper);
        log.info("searchKey:" +  searchKey);
        mwModulePermMapper = (MwModulePermMapper)redisObjectTemplate.opsForValue().get(searchKey);
        if(null != mwModulePermMapper){
            StringBuffer sb = new StringBuffer();
            sb.append(mwModulePermMapper.getModuleId()).append("-").append(ops);
            log.info("modulePermKey----->"+sb.toString());
            return sb.toString();
        }
        return null;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (isTimer) {
            return;
        }
      moduleToRedis();
    }

    private String genRedisKey(MwModulePermMapper mapper){
        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":").append(mapper.getUrl())
                .append("/").append(mapper.getPermName());
        return sb.toString();
    }

    public void moduleToRedis() {
        // 应用启动清理缓存
        Date starttime = new Date();
        StringBuffer info = new StringBuffer();
        info.append("redis-").append(redisGroup).append(" clean");
        log.info(info.toString() + " start");

        StringBuffer sb = new StringBuffer();
        sb.append(redisGroup).append(":*");
        Set<String> keys = redisObjectTemplate.keys(sb.toString());
        redisObjectTemplate.delete(keys);

        List<MwModulePermMapper> list = mwModulePermMapperDao.selectList();
        if (userDebug) {
            log.info("项目启动时刷进redis的模块权限信息--------->"+ JSONObject.toJSONString(list));
        }
        if(null != list ){
            for(MwModulePermMapper mwModulePermMapper : list){
                String url = mwModulePermMapper.getUrl();
                if(null == url){
                    continue;
                }
                String key = genRedisKey(mwModulePermMapper);
                //redis过期时间设置为10年
                redisObjectTemplate.opsForValue().set(key, mwModulePermMapper);
            }
        }

        Date endtime = new Date();
        Long diff = (endtime.getTime() - starttime.getTime()) / 1000;
        log.info(info.toString() + " end, used time:" + diff.longValue());
    }

}
