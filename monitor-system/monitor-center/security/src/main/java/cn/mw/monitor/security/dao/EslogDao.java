package cn.mw.monitor.security.dao;

import java.util.List;

/**
 * @author xhy
 * @date 2020/9/8 14:14
 */
public interface EslogDao {
    List<String> getOrgNameByIp(String ip);

    String getAssetsType(String hostIp);

    List<String> getAsstsHostName(String typeName);
}
