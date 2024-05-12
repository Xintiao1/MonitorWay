package cn.mw.xiangtai.plugin.service.impl.detailimpl;

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
 * @description 攻击类型topN
 * @date 2023/10/17 9:49
 */
@Service
@Slf4j
public class XiangtaiAttackTypeTopNModule implements XiangtaiVisualizedModule {

    @Autowired
    private XiangtaiVisualizedService visualizedService;

    @Autowired
    private XiangtaiLogVisualizedMapper logVisualizedMapper;

    @Override
    public int[] getType() {
        return new int[]{109};
    }

    @Override
    public Object getData(XiangtaiVisualizedParam visualizedParam) {
        try {
            List<AttackTypeDTO> typeDTOS = visualizedService.attackTypeTopN(visualizedParam.getTopN());
            handlerAttackTypeCodeMapping(typeDTOS);
            return typeDTOS;
        }catch (Throwable e){
            log.error("XiangtaiAttackTypeTopNModule{} getData() ERROR::",e);
            return null;
        }
    }

    /**
     * 处理攻击类型的编码映射关系
     * @param typeDTOS
     */
    private void handlerAttackTypeCodeMapping(List<AttackTypeDTO> typeDTOS){
        if(CollectionUtils.isEmpty(typeDTOS)){return;}
        List<String> codes = typeDTOS.stream().map(AttackTypeDTO::getType).collect(Collectors.toList());
        log.info("logVisualizedMapper::"+logVisualizedMapper);
        List<AttackTypeCodeMappingDto> attackTypeCodeMappingDtos = logVisualizedMapper.selectAttackTypeMapping(codes);
        if(CollectionUtils.isEmpty(attackTypeCodeMappingDtos)){return;}
        Map<String, String> codeMap = attackTypeCodeMappingDtos.stream().collect(Collectors.toMap(AttackTypeCodeMappingDto::getAttackCode, AttackTypeCodeMappingDto::getAttackName));
        for (AttackTypeDTO typeDTO : typeDTOS) {
            String name = codeMap.get(typeDTO.getType());
            if(StringUtils.isNotBlank(name)){
                typeDTO.setType(name);
            }
        }
    }
}
