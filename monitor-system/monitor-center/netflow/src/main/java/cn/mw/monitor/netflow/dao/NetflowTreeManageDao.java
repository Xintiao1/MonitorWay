package cn.mw.monitor.netflow.dao;

import cn.mw.monitor.netflow.entity.NetflowTreeEntity;
import cn.mw.monitor.netflow.param.AssetsInfo;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author gui.quanwang
 * @className NetflowTreeManageDao
 * @description 流量监控树结构Dao
 * @date 2022/8/3
 */
public interface NetflowTreeManageDao extends BaseMapper<NetflowTreeEntity> {

    /**
     * 根据资产ID获取所有的IP列表
     * @param assetsId 资产ID
     * @return
     */
    List<String> getAssetsIpList(@Param(value = "assetsId") String assetsId);

    /**
     * 根据资产ID和接口索引，获取接口的IP地址
     * @param assetsId 资产ID
     * @param ifIndex 索引
     * @return
     */
    String getIpByAssetsIdAndIfIndex(@Param(value = "assetsId") String assetsId,
                                     @Param(value = "ifIndex") Integer ifIndex);

    /**
     * 根据资产ID获取所有的索引列表
     * @param assetsId 资产ID
     * @return
     */
    List<Integer> getIfIndexList(@Param(value = "assetsId") String assetsId);

    /**
     * 获取当前资产的所有接口列表
     *
     * @param assetsId      资产ID
     * @param interfaceName 接口名称
     * @param vlanFlag
     * @return
     */
    List<QueryAssetsInterfaceParam> getAllInterface(@Param("assetsId") String assetsId,
                                                    @Param("interfaceName") String interfaceName,
                                                    @Param("vlanFlag") Boolean vlanFlag);

    /**
     * 根据资产ID获取资产数据
     *
     * @param assetsId 资产ID
     * @return
     */
    AssetsInfo getAssetsById(@Param(value = "assetsId") String assetsId);
}
