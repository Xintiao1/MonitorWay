package cn.mw.monitor.server.dao;

import cn.mw.monitor.server.serverdto.AssetsDTO;
import cn.mw.monitor.server.serverdto.TagsDto;
import cn.mw.monitor.service.server.param.QueryAssetsAvailableParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xhy
 * @date 2020/4/29 16:29
 */
public interface ServerAssetsDao {

    String getTypeName(@Param("hostid") String hostid, @Param("monitorServerId") Integer monitorServerId);

    List<TagsDto> getTagsByhostId(@Param("hostid") String hostid);

    List<String> getOrgNameByAssetsId(@Param("assetsId") String assetsId, @Param("type") String type);

    List<String> getGroupNameByAssetsId(@Param("assetsId") String assetsId, @Param("type") String type);

    List<String> getUserNameByAssetsId(@Param("assetsId") String assetsId, @Param("type") String type);

    List<AssetsDTO> selectTangibleAssetsByIp(@Param("ip") String ip, @Param("id") String id);

    List<AssetsDTO> selectOutbandAssetsByIp(@Param("ip") String ip, @Param("id") String id);

    List<String> getOrgNameByTypeId(String id);

    List<String> getGroupNameByTypeId(String id);

    List<String> getUserNameByTypeId(String id);

    List<String> selectErrorAvailableById(QueryAssetsAvailableParam param);

    List<String> selectAllAvailableById(QueryAssetsAvailableParam param);

    List<String> selectNoneAvailableById(QueryAssetsAvailableParam param);
}
