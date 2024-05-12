package cn.mw.monitor.manager;

import cn.mw.monitor.manager.dto.MwAssetsIdsDTO;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;

import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/10/26 10:23
 */
public interface MwAssetsManager {
     /**
      * 获得当前用户的全部的hostIds，获得全部的资产assets
      * @param userId
      * @return
      */
     Map<String, Object> getAssetsByUserId(Integer userId);

     List<MwAssetsIdsDTO> getAssetsIds(Boolean deleteFlag);

     /**
      * 获得全部的hostIds，获得全部的资产assets
      * @param mwCommonAssetsDto
      * @return
      */
     Map<String, Object> getAssetsByUserId(MwCommonAssetsDto mwCommonAssetsDto);


     /**
      * 获得全部的hostIds，获得全部的资产assets
      * @param mwCommonAssetsDto
      * @return
      */
     List<MwTangibleassetsTable> getAllAssetsByUserId(MwCommonAssetsDto mwCommonAssetsDto);

     Map<Integer, List<String>> getAssetsByServerId(List<MwTangibleassetsTable> mwTangibleassetsDTOS);

     /**
      * 全部的资产数据根据monitorServerId进行分类
      * @param map
      * @return
      */
     Map<Integer,List<String>> getAssetsByServerId(Map<String, Object> map);


     List<String> getLogHostList(MwCommonAssetsDto mwCommonAssetsDto);

     List<String> getLogHostList1(MwCommonAssetsDto mwCommonAssetsDto);


     MwAssetsIdsDTO selectAssetsByIp(String linkTargetIp);

     /**
      * 根据告警规则查询所有的资产
      * @param mwCommonAssetsDto
      * @return
      */
     List<String> getAssetsByAction(MwCommonAssetsDto mwCommonAssetsDto);

     List<MwTangibleassetsDTO> getAssetsByAssetsTypeId(Integer assetsTypeId);

     List<MwTangibleassetsTable> getAssetsTable(QueryTangAssetsParam qParam);

     MwTangibleassetsDTO getAssetsAndOrgs(String assetsId);

     Boolean checkNowItems(int monitorServerId, String hostId);

     //根据对应字段查询所需要的字段数据信息
     List<Map<String,Object>> getAssetsFieldData(List<String> fields);
}
