package cn.mw.xiangtai.plugin.service.impl.detailimpl;

import cn.mw.xiangtai.plugin.domain.dto.AttackDataDTO;
import cn.mw.xiangtai.plugin.domain.dto.AttackTypeCodeMappingDto;
import cn.mw.xiangtai.plugin.domain.dto.AttackTypeDTO;
import cn.mw.xiangtai.plugin.domain.param.XiangtaiVisualizedParam;
import cn.mw.xiangtai.plugin.monitor.dao.XiangtaiLogVisualizedMapper;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedModule;
import cn.mw.xiangtai.plugin.service.XiangtaiVisualizedService;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author gengjb
 * @description 攻击数据分析
 * @date 2023/10/17 9:59
 */
@Service
@Slf4j
public class XiangtaiAttackDataAnalysisModule implements XiangtaiVisualizedModule {

    @Autowired
    private XiangtaiVisualizedService visualizedService;

    @Autowired
    private XiangtaiLogVisualizedMapper logVisualizedMapper;

    @Override
    public int[] getType() {
        return new int[]{112};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            List<AttackDataDTO> attackDataDTOS = visualizedService.attackDataAnalysis();
            handlerAttackTypeCodeMapping(attackDataDTOS);
            return attackDataDTOS;
        }catch (Throwable e){
            log.error("XiangtaiAttackDataAnalysisModule{} getData() ERROR::",e);
            return null;
        }
    }


    /**
     * 处理攻击类型的编码映射关系
     * @param dataDTOS
     */
    private void handlerAttackTypeCodeMapping(List<AttackDataDTO> dataDTOS){
        if(CollectionUtils.isEmpty(dataDTOS)){return;}
        List<String> codes = dataDTOS.stream().map(AttackDataDTO::getName).collect(Collectors.toList());
        List<AttackTypeCodeMappingDto> attackTypeCodeMappingDtos = logVisualizedMapper.selectAttackTypeMapping(codes);
        if(CollectionUtils.isEmpty(attackTypeCodeMappingDtos)){return;}
        Map<String, String> codeMap = attackTypeCodeMappingDtos.stream().collect(Collectors.toMap(AttackTypeCodeMappingDto::getAttackCode, AttackTypeCodeMappingDto::getAttackName));
        for (AttackDataDTO dto : dataDTOS) {
            String name = codeMap.get(dto.getName());
            if(StringUtils.isNotBlank(name)){
                dto.setName(name);
            }
        }
    }
}
