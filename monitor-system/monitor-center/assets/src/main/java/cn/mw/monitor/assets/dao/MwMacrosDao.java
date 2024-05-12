package cn.mw.monitor.assets.dao;

import cn.mw.monitor.service.assets.param.MacrosDTO;

import java.util.List;

/**
 * @author syt
 * @Date 2020/12/22 14:14
 * @Version 1.0
 */
public interface MwMacrosDao {
    String selectChMacro(String macro);

    List<MacrosDTO> selectMacros();
}
