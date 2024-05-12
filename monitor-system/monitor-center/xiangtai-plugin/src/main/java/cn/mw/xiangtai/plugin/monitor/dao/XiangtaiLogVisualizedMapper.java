package cn.mw.xiangtai.plugin.monitor.dao;

import cn.mw.xiangtai.plugin.domain.dto.AttackTypeCodeMappingDto;
import cn.mw.xiangtai.plugin.domain.dto.XIangtaiMapAreaDto;
import cn.mw.xiangtai.plugin.domain.dto.XiangtaiAssetsInfoDto;
import cn.mw.xiangtai.plugin.domain.dto.XiangtaiDeviceDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author gengjb
 * @description 祥泰日志可视化
 * @date 2023/10/19 9:49
 */
@Mapper
public interface XiangtaiLogVisualizedMapper {

    List<AttackTypeCodeMappingDto> selectAttackTypeMapping(@Param("codes") List<String> codes);

    /**
     * 查询祥泰资产信息
     * @return
     */
    XiangtaiAssetsInfoDto selectXiangtaiAssetsInfo();

    /**
     * 查询祥泰IP与设备映射信息
     * @return
     */
    List<XiangtaiDeviceDto> selectXiangtaiDeviceMappingInfo();

    /**
     * 查询IP地址的区域信息
     * @param areas
     * @return
     */
    List<XIangtaiMapAreaDto> selectIpAddressAreaInfo(@Param("areas") List<String> areas);
}
