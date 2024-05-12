package cn.mw.monitor.zbx.dao;

import cn.mw.monitor.zbx.dto.TableAssetsDto;

/**
 * @author xhy
 * @date 2020/5/8 13:57
 */
public interface MWAssetsDao {

    TableAssetsDto getTableDetail(String hostId);
}
