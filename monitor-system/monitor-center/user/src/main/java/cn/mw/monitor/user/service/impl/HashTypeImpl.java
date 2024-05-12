package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.user.dao.HashTypeDao;
import cn.mw.monitor.user.dto.HashTypeDTO;
import cn.mw.monitor.user.model.HashType;
import cn.mw.monitor.user.service.HashTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dev on 2020/2/14.
 */
@Service
@Slf4j
public class HashTypeImpl implements HashTypeService {

    @Resource
    private HashTypeDao hashTypeDao;

    @Override
    public Map<String, HashTypeDTO> selectMap() {
        List<HashType> list = hashTypeDao.selectList();
        Map<String, HashTypeDTO> retmap = new HashMap();
        try {
            for (HashType hashType : list) {
                HashTypeDTO hashTypeDTO = CopyUtils.copy(HashTypeDTO.class, hashType);
                retmap.put(hashType.getId().toString(), hashTypeDTO);
            }
        }catch(Exception e){
            log.error("fail to HashTypeImpl.selectMap , cause:{}",e.getMessage());
            return null;
        }
        return retmap;
    }
}
