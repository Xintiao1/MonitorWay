package cn.mw.monitor.user.service;

import cn.mw.monitor.user.dto.HashTypeDTO;

import java.util.Map;

/**
 * Created by dev on 2020/2/14.
 */
public interface  HashTypeService {
    Map<String, HashTypeDTO> selectMap();
}
