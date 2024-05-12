package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedHostGroupDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName
 * @Description 外部资产信息
 * @Author gengjb
 * @Date 2023/5/19 23:51
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedExternalAssetsInfo implements MwVisualizedModule {

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Override
    public int[] getType() {
        return new int[]{71};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            Map<String, List<MwVisualizedHostGroupDto>> hostMap = new HashMap<>();
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            if(StringUtils.isNotBlank(moduleParam.getBusinStatusTitle())){
                List<MwVisualizedHostGroupDto> visualizedHostGroupDtos = visualizedManageDao.selectHostAndGroupCache(moduleParam.getServerName(),moduleParam.getBusinStatusTitle());
                hostMap.put(moduleParam.getBusinStatusTitle(),visualizedHostGroupDtos);
                return hostMap;
            }
            List<MwVisualizedHostGroupDto> visualizedHostGroupDtos = visualizedManageDao.selectHostAndGroupCache(moduleParam.getServerName(),null);
            if(CollectionUtils.isEmpty(visualizedHostGroupDtos)){return null;}
            Map<String, List<MwVisualizedHostGroupDto>> listMap = visualizedHostGroupDtos.stream().collect(Collectors.groupingBy(item -> item.getHostGroupName()));
            if(listMap == null || listMap.isEmpty() || StringUtils.isBlank(moduleParam.getServerName())){return listMap;}
            //主机组需要按照_分割，取后面的值
            for (String hostGroupName : listMap.keySet()) {
                List<MwVisualizedHostGroupDto> hostGroupDtos = listMap.get(hostGroupName);
                if(StringUtils.isBlank(hostGroupName) || !hostGroupName.contains("_")){
                    hostMap.put(hostGroupName,hostGroupDtos);
                    continue;
                }
                String[] groupNames = hostGroupName.split("_",2);
                if(groupNames.length > 1){
                    hostMap.put(groupNames[1],hostGroupDtos);
                    continue;
                }
                hostMap.put(hostGroupName,hostGroupDtos);
            }
            return hostMap;
        }catch (Throwable e){
            log.error("MwVisualizedExternalAssetsInfo{} getData::",e);
            return null;
        }
    }
}
