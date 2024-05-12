package cn.mw.module.security.service.impl;

import cn.mw.module.security.dto.*;
import cn.mw.module.security.service.EsSysLogRuleService;
import cn.mw.monitor.security.dao.EsSysLogRuleDao;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.weixinapi.DelFilter;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author qzg
 * @date 2022/1/6
 */
@Service
@Slf4j
public class EsSysLogRuleServiceImpl implements EsSysLogRuleService {
    @Resource
    private EsSysLogRuleDao esSysLogRuleDao;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private EsSysLogAuditServiceImpl esSysLogAuditServiceImpl;

    @Override
    public Reply getSystemLogRulesInfos(EsSysLogRuleDTO param) {
        try {
            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
            List<EsSysLogRuleDTO> list = esSysLogRuleDao.getSystemLogRulesInfos(param);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getSystemLogRulesInfos:{} cause:{}",e);
            return Reply.fail(500, "日志信息规则获取失败");
        }
    }

    @Override
    public Reply getRulesInfoById(EsSysLogRuleDTO param) {
        try {
            EsSysLogRuleDTO dto = esSysLogRuleDao.getSystemLogRulesInfoById(param);
            List<String> actions = Arrays.asList(dto.getAction().split(","));
            dto.setActions(actions);
            if (dto != null && dto.getId() != null) {
                //获取标签信息
                List<EsSysLogTagDTO> listTag = esSysLogRuleDao.getRuleTags(dto.getId());
                dto.setTagDTOList(listTag);
            }
            if (dto != null && dto.getRuleId() != null) {
                //获取规则信息
                List<MwRuleSelectParam> listRule = esSysLogRuleDao.getAlertRules(dto.getRuleId());
                List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
                if(listRule != null && listRule.size() > 0){
                    for (MwRuleSelectParam s : listRule){
                        if(s.getKey().equals("root")){
                            ruleSelectParams.add(s);
                        }
                    }
                    for(MwRuleSelectParam s : ruleSelectParams){
                        s.setConstituentElements(esSysLogAuditServiceImpl.getChild(s.getKey(),listRule));
                    }
                }
                dto.setMwRuleSelectListParam(ruleSelectParams);
            }
            dto.setRuleIds(esSysLogRuleDao.selectRules(dto.getRuleId()));
            dto.setActionUserIds(esSysLogRuleDao.selectActionUsersMapper(dto.getRuleId()));
            dto.setActionGroupIds(esSysLogRuleDao.selectActionGroupsMapper(dto.getRuleId()));
            return Reply.ok(dto);
        } catch (Exception e) {
            log.error("fail to getRulesInfoById:{} cause:{}",e);
            return Reply.fail(500, "根据id查询日志信息规则失败");
        }
    }

    /**
     * 创建日志规则列表数据
     * @param param
     * @return
     */
    @Override
    @Transactional
    public Reply createSysLogRulesInfo(EsSysLogRuleDTO param) {
        if(Strings.isNullOrEmpty( param.getRuleName())){
            return Reply.fail(500, "日志信息规则名称不可为空");
        }
        String loginName = iLoginCacheInfo.getLoginName();
        try {
            //先插入规则插件信息，获取规则id
            String uuid = UUIDUtils.getUUID();
            List<MwRuleSelectParam> paramList = new ArrayList<>();
            for (MwRuleSelectParam s : param.getMwRuleSelectListParam()) {
                MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
                ruleSelectDto.setCondition(s.getCondition());
                ruleSelectDto.setDeep(s.getDeep());
                ruleSelectDto.setKey(s.getKey());
                ruleSelectDto.setName(s.getName());
                ruleSelectDto.setParentKey(s.getParentKey());
                ruleSelectDto.setRelation(s.getRelation());
                ruleSelectDto.setValue(s.getValue());
                ruleSelectDto.setUuid(uuid);
                paramList.add(ruleSelectDto);
                s.setUuid(uuid);
                if (s.getConstituentElements() != null && s.getConstituentElements().size() > 0) {
                    List<MwRuleSelectParam> temps = delMwRuleSelectList(s);
                    paramList.addAll(temps);
                }
            }
            esSysLogRuleDao.insertMwAlertRuleSelect(paramList);
            //在插入日志规则设置信息
            param.setRuleId(uuid);
            param.setCreator(loginName);
            param.setUpdater(loginName);
            StringBuilder action = new StringBuilder();
            if(param.getActions() != null && param.getActions().size()>0){
                for (String s : param.getActions()){
                    action.append(s).append(",");
                }
            }
            param.setAction(action.toString());
            esSysLogRuleDao.insertSysLogRule(param);
            //插入规则标签对应信息
            EsSysLogRuleTagMapperDTO mapperDTO = new EsSysLogRuleTagMapperDTO();
            if(CollectionUtils.isNotEmpty(param.getTagIds())){
                mapperDTO.setRuleMapperId(param.getId());
                mapperDTO.setTagIds(param.getTagIds());
                esSysLogRuleDao.insertSysLogTagMapper(mapperDTO);
            }
            List<SysLogRuleMapper> logRuleMappers = new ArrayList<>();
            List<String> ruleIds = param.getRuleIds();
            if(CollectionUtils.isNotEmpty(ruleIds)){
                ruleIds.forEach(ruleId -> {
                            SysLogRuleMapper actionRuleMapper = SysLogRuleMapper.builder().ActionId(uuid).ruleId(ruleId).build();
                            logRuleMappers.add(actionRuleMapper);
                        }
                );
                esSysLogRuleDao.insertActionRuleMapper(logRuleMappers);
            }
            //用户
            List<SysLogUserMapper> actionUserMappers = new ArrayList<>();
            List<Integer> userIds = param.getActionUserIds();
            if (CollectionUtils.isNotEmpty(userIds)) {
                userIds.forEach(userid -> {
                    SysLogUserMapper actionUserMapper = SysLogUserMapper.builder().actionId(uuid).userId(userid).build();
                    actionUserMappers.add(actionUserMapper);
                });
                esSysLogRuleDao.insertActionUsersMapper(actionUserMappers);
            }
            //用户组
            List<SysLogGroupMapper> actionGroupMappers = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(param.getActionGroupIds())){
                List<Integer> groupIds = param.getActionGroupIds();
                groupIds.forEach(groupId ->{
                    SysLogGroupMapper actionGroupMapper =  SysLogGroupMapper.builder().actionId(uuid).groupId(groupId).build();
                    actionGroupMappers.add(actionGroupMapper);
                });
                esSysLogRuleDao.insertActionGroupsMapper(actionGroupMappers);
            }
        } catch (Exception e) {
            log.error("fail to createSysLogRulesInfo:{} cause:{}",e);
            return Reply.fail(500, "日志信息规则创建失败");
        }
        return Reply.ok();
    }

    /**
     * 修改日志规则 数据
     *
     * @param param
     * @return
     */
    @Override
    @Transactional
    public Reply editoSysLogRulesInfo(EsSysLogRuleDTO param) {
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            String uuid = UUIDUtils.getUUID();
            List<MwRuleSelectParam> paramList = new ArrayList<>();
            for (MwRuleSelectParam s : param.getMwRuleSelectListParam()) {
                MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
                ruleSelectDto.setCondition(s.getCondition());
                ruleSelectDto.setDeep(s.getDeep());
                ruleSelectDto.setKey(s.getKey());
                ruleSelectDto.setName(s.getName());
                ruleSelectDto.setParentKey(s.getParentKey());
                ruleSelectDto.setRelation(s.getRelation());
                ruleSelectDto.setValue(s.getValue());
                ruleSelectDto.setUuid(uuid);
                paramList.add(ruleSelectDto);
                s.setUuid(uuid);
                if (s.getConstituentElements() != null && s.getConstituentElements().size() > 0) {
                    List<MwRuleSelectParam> temps = delMwRuleSelectList(s);
                    paramList.addAll(temps);
                }
            }
            //先删除 规则
            esSysLogRuleDao.deleteMwAlertRuleSelect(param.getRuleId());
            //删除 标签
            esSysLogRuleDao.deleteTagInfo(param.getId());
            //修改日志规则列表数据
            param.setUpdater(loginName);
            param.setRuleId(uuid);
            StringBuilder action = new StringBuilder();
            if(param.getActions() != null && param.getActions().size()>0){
                for (String s : param.getActions()){
                    action.append(s).append(",");
                }
            }
            param.setAction(action.toString());
            esSysLogRuleDao.updateSysLogRulesInfo(param);
            //新增 规则
            esSysLogRuleDao.insertMwAlertRuleSelect(paramList);
            //新增 标签
            //插入规则标签对应信息
            if(CollectionUtils.isNotEmpty(param.getTagIds())){
                EsSysLogRuleTagMapperDTO mapperDTO = new EsSysLogRuleTagMapperDTO();
                mapperDTO.setRuleMapperId(param.getId());
                mapperDTO.setTagIds(param.getTagIds());
                esSysLogRuleDao.insertSysLogTagMapper(mapperDTO);
            }
            //先删除
            esSysLogRuleDao.deleteActionRulesMapper(param.getRuleId());
            List<SysLogRuleMapper> logRuleMappers = new ArrayList<>();
            List<String> ruleIds = param.getRuleIds();
            if(CollectionUtils.isNotEmpty(ruleIds)){
                ruleIds.forEach(ruleId -> {
                            SysLogRuleMapper actionRuleMapper = SysLogRuleMapper.builder().ActionId(uuid).ruleId(ruleId).build();
                            logRuleMappers.add(actionRuleMapper);
                        }
                );
                esSysLogRuleDao.insertActionRuleMapper(logRuleMappers);
            }
            //用户
            esSysLogRuleDao.deleteActionUsersMapper(param.getRuleId());
            List<SysLogUserMapper> actionUserMappers = new ArrayList<>();
            List<Integer> userIds = param.getActionUserIds();
            if (CollectionUtils.isNotEmpty(userIds)) {
                userIds.forEach(userid -> {
                    SysLogUserMapper actionUserMapper = SysLogUserMapper.builder().actionId(uuid).userId(userid).build();
                    actionUserMappers.add(actionUserMapper);
                });
                esSysLogRuleDao.insertActionUsersMapper(actionUserMappers);
            }
            //用户组
            esSysLogRuleDao.deleteActionGroupsMapper(param.getRuleId());
            List<SysLogGroupMapper> actionGroupMappers = new ArrayList<>();
            if(CollectionUtils.isNotEmpty(param.getActionGroupIds())){
                List<Integer> groupIds = param.getActionGroupIds();
                groupIds.forEach(groupId ->{
                    SysLogGroupMapper actionGroupMapper =  SysLogGroupMapper.builder().actionId(uuid).groupId(groupId).build();
                    actionGroupMappers.add(actionGroupMapper);
                });
                esSysLogRuleDao.insertActionGroupsMapper(actionGroupMappers);
            }

            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to editoSysLogRulesInfo:{} cause:{}",e);
            return Reply.fail(500, "获取标签信息失败");
        }

    }

    @Override
    public Reply updateSysLogRulesState(EsSysLogRuleDTO param) {
        try {
            esSysLogRuleDao.updateSysLogRulesState(param);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to updateSysLogRulesState:{} cause:{}",e);
            return Reply.fail(500, "修改日志规则状态失败");
        }
    }

    /**
     * 删除日志规则信
     * @param param
     * @return
     */
    @Override
    @Transactional
    public Reply deleteSysLogRulesInfo(EsSysLogRuleDTO param) {
        try {
            esSysLogRuleDao.deleteSysLogRulesInfo(param.getIds());
            esSysLogRuleDao.deleteActionGroupsMapper(param.getRuleId());
            esSysLogRuleDao.deleteActionUsersMapper(param.getRuleId());
            esSysLogRuleDao.deleteActionRulesMapper(param.getRuleId());
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to deleteSysLogRulesInfo:{} cause:{}",e);
            return Reply.fail(500, "删除日志规则信息失败");
        }
    }


    @Override
    @Transactional
    public Reply createTagInfo(EsSysLogTagDTO param) {
        try {
            esSysLogRuleDao.createTagInfo(param);
        } catch (Exception e) {
            log.error("fail to createTagInfo:{} cause:{}",e);
            return Reply.fail(500, "新增标签信息失败");
        }
        return Reply.ok();
    }

    @Override
    @Transactional
    public Reply deleteSysLogTagInfo(EsSysLogTagDTO param) {
        try {
            esSysLogRuleDao.deleteSysLogTagInfo(param.getIds());
        } catch (Exception e) {
            log.error("fail to deleteSysLogTagInfo:{} cause:{}",e);
            return Reply.fail(500, "删除标签信息失败");
        }
        return Reply.ok();
    }

    @Override
    public Reply getSysLogTagInfo() {
        try {
            List<EsSysLogTagDTO> listTag = esSysLogRuleDao.getSysLogTagInfo();
            return Reply.ok(listTag);
        } catch (Exception e) {
            log.error("fail to getSysLogTagInfo:{} cause:{}",e);
            return Reply.fail(500, "获取标签信息失败");
        }
    }

    @Override
    public Reply fuzzSearchAllFiledData() {
        //根据值模糊查询数据
        List<Map<String, Object>> fuzzSeachAllFileds = esSysLogRuleDao.fuzzSearchAllFiled();
        Set<String> fuzzSeachData = new HashSet<>();
        if (!cn.mwpaas.common.utils.CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
            for (Map<String, Object> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                fuzzSeachAllFiled.forEach((k, v) -> {
                    String value = "";
                    if (v != null) {
                        value = String.valueOf(v);
                    }
                    fuzzSeachData.add(value);
                });
            }
        }
        fuzzSeachData.stream().sorted(Comparator.reverseOrder());
        Map<String, Set<String>> fuzzyQuery = new HashMap<>();
        fuzzyQuery.put("fuzzyQuery", fuzzSeachData);
        return Reply.ok(fuzzyQuery);
    }


    public List<MwRuleSelectParam> delMwRuleSelectList(MwRuleSelectParam param) {
        List<MwRuleSelectParam> paramList = new ArrayList<>();
        for (MwRuleSelectParam s : param.getConstituentElements()) {
            MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
            ruleSelectDto.setCondition(s.getCondition());
            ruleSelectDto.setDeep(s.getDeep());
            ruleSelectDto.setKey(s.getKey());
            ruleSelectDto.setName(s.getName());
            ruleSelectDto.setParentKey(s.getParentKey());
            ruleSelectDto.setRelation(s.getRelation());
            ruleSelectDto.setValue(s.getValue());
            ruleSelectDto.setUuid(param.getUuid());
            paramList.add(ruleSelectDto);
            s.setUuid(param.getUuid());
            if (s.getConstituentElements() != null && s.getConstituentElements().size() > 0) {
                List<MwRuleSelectParam> temps = delMwRuleSelectList(s);
                paramList.addAll(temps);
            }
        }
        return paramList;

    }
}
