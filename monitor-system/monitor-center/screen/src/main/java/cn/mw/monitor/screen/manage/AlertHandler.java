package cn.mw.monitor.screen.manage;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.screen.dao.MWNewScreenManageDao;
import cn.mw.monitor.screen.dto.MWNewScreenAssetsFilterDto;
import cn.mw.monitor.screen.dto.MWTangibleassetsDto;
import cn.mw.monitor.service.alert.dto.AlertEnum;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.model.OrgDTO;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.weixinapi.DelFilter;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 首页告警处理
 * @Author gengjb
 * @Date 2023/6/26 15:41
 * @Version 1.0
 **/
@Slf4j
@Component
public class AlertHandler {

    private String modelDataId = "ALERT";

    @Autowired
    private MWNewScreenManageDao newScreenManageDao;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWOrgService orgService;

    //获取用户过滤信息
    public List<String> handler(){
        try {
            Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
            MWNewScreenAssetsFilterDto mwNewScreenAssetsFilterDto = newScreenManageDao.selectNewScreenAssetsFilterData(modelDataId,userId,0);
            List<MwRuleSelectParam> mwRuleSelectParams = newScreenManageDao.selectMwAlertRuleSelect("NEW_HOME_" + mwNewScreenAssetsFilterDto.getId());
            //如果用户有自定义过滤规则,需要以用户的规则定义过滤
            return customRuleFilter(mwNewScreenAssetsFilterDto,userId,mwRuleSelectParams);
        }catch (Throwable e){
            log.error("Screen AlertHandler{} handler()");
            return null;
        }
    }

    /**
     * 自定义规则过滤
     */
    private List<String> customRuleFilter(MWNewScreenAssetsFilterDto mwNewScreenAssetsFilterDto,Integer userId,List<MwRuleSelectParam> mwRuleSelectParams){
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setAssetsTypeId(mwNewScreenAssetsFilterDto.getAssetsTypeId());
        assetsParam.setAssetsTypeSubId(mwNewScreenAssetsFilterDto.getAssetsTypeSubId());
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setSkipDataPermission(true);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(userId);
        //根据类型查询资产数据
        List<MwTangibleassetsTable> mwTangibleassetsTables = mwAssetsManager.getAssetsTable(assetsParam);
        if(CollectionUtils.isEmpty(mwTangibleassetsTables)){return null;}
        if(CollectionUtils.isEmpty(mwRuleSelectParams)){return mwTangibleassetsTables.stream().map(MwTangibleassetsTable::getAssetsId).collect(Collectors.toList());}
        List<MWTangibleassetsDto> mwTangibleassetsDtos = new ArrayList<>();
        List<MWOrgDTO> mworgDtos = orgService.getAllOrgList();
        for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
            MWTangibleassetsDto dto = new MWTangibleassetsDto();
            dto.setAssetsId(mwTangibleassetsTable.getAssetsId());
            dto.setAssetsName(mwTangibleassetsTable.getAssetsName());
            dto.setId(mwTangibleassetsTable.getId());
            dto.setMonitorServerId(mwTangibleassetsTable.getMonitorServerId());
            dto.setIsKeyDevices(mwTangibleassetsTable.getIsKeyDevices());
            List<String> assetsOrgs = getAssetsOrgs(mwTangibleassetsTable, mworgDtos);
            dto.setOrgNames(assetsOrgs);
            mwTangibleassetsDtos.add(dto);
        }
        List<MWTangibleassetsDto> tangibleassetsDtos = handleAssetsLabelFilter(mwTangibleassetsDtos, mwRuleSelectParams);
        if(CollectionUtils.isEmpty(tangibleassetsDtos)){return null;}
        return tangibleassetsDtos.stream().map(MWTangibleassetsDto::getAssetsId).collect(Collectors.toList());

    }


    private  List<MWTangibleassetsDto> handleAssetsLabelFilter(List<MWTangibleassetsDto> mwTangibleassetsDtos, List<MwRuleSelectParam> mwRuleSelectParams) {
        if (CollectionUtils.isEmpty(mwRuleSelectParams)){return mwTangibleassetsDtos;}
        List<MWTangibleassetsDto> tangibleassetsDtos = new ArrayList<>();
        //查询到每个资产的标签
        for (MWTangibleassetsDto mwTangibleassetsDto : mwTangibleassetsDtos) {
            HashMap<String, Object> map = new HashMap<>();
            MessageContext context = new MessageContext();
            context.setKey(map);
            map.put(AlertEnum.ASSETS.toString(), mwTangibleassetsDto.getAssetsName());
            map.put(AlertEnum.ASSETSTYPE.toString(), mwTangibleassetsDto.getTypeName());
            map.put(AlertEnum.KEYDEVICES.toString(),String.valueOf(mwTangibleassetsDto.getIsKeyDevices()));
            map.put(AlertEnum.ORG.toString(), mwTangibleassetsDto.getOrgNames());
            if (mwRuleSelectParams.size() > 2) {
                log.info("ruleSelectParams star");
                List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
                for (MwRuleSelectParam s : mwRuleSelectParams) {
                    if (s.getParentKey().equals("root")) {
                        ruleSelectParams.add(s);
                    }
                }
                for (MwRuleSelectParam s : ruleSelectParams) {
                    s.setConstituentElements(getChild(s.getKey(), mwRuleSelectParams));
                }
                Boolean aBoolean = DelFilter.delFilter(ruleSelectParams, context, mwRuleSelectParams);
                if(aBoolean){
                    tangibleassetsDtos.add(mwTangibleassetsDto);
                }
            }
        }
        return tangibleassetsDtos;
    }

    private static List<MwRuleSelectParam> getChild(String key, List<MwRuleSelectParam> rootList){
        List<MwRuleSelectParam> childList = new ArrayList<>();
        for(MwRuleSelectParam s : rootList){
            if(s.getParentKey().equals(key)){
                childList.add(s);
            }
        }
        for(MwRuleSelectParam s : childList){
            s.setConstituentElements(getChild(s.getKey(),rootList));
        }
        if(childList.size() == 0){
            return null;
        }
        return childList;
    }

    private List<String> getAssetsOrgs(MwTangibleassetsTable tangTable,List<MWOrgDTO> mworgDtos){
        List<String> orgNames = new ArrayList<>();
        List<List<Integer>> modelViewOrgIds = tangTable.getModelViewOrgIds();
        if(CollectionUtils.isNotEmpty(modelViewOrgIds) && CollectionUtils.isNotEmpty(mworgDtos)){
            List<Integer> orgIds = new ArrayList<>();
            for(List<Integer> mo : modelViewOrgIds){
                orgIds.addAll(mo);
            }
            List<OrgDTO> orgDTOS = orgIds.parallelStream().map(mwOrgDTO ->{
                MWOrgDTO dto = mworgDtos.stream().filter(u -> u.getOrgId().equals(mwOrgDTO)).findFirst().orElse(null);
                OrgDTO reOrgDto = new OrgDTO();
                if(null != dto){
                    BeanUtils.copyProperties(dto,reOrgDto);
                }
                return reOrgDto;
            }).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(orgDTOS)){
                orgNames = orgDTOS.stream().filter(item-> StringUtils.isNotBlank(item.getOrgName())).map(OrgDTO::getOrgName).collect(Collectors.toList());
            }
        }
        return orgNames;
    }
}
