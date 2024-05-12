package cn.mw.monitor.service.license.service;

import java.util.List;

/**
 * @author syt
 * @Date 2021/9/26 14:42
 * @Version 1.0
 */
public interface CheckCountService {
    int selectTableCount(String tableName, boolean deleteFlag);

    int selectAssetsCount(List<Integer> assetsTypeId, List<Integer> monitorModes);
}
