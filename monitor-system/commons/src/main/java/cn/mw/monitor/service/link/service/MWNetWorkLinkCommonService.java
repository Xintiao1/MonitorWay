package cn.mw.monitor.service.link.service;

import cn.mw.monitor.service.link.dto.MwLinkInterfaceDto;
import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import cn.mw.monitor.service.link.param.MwLinkCommonParam;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @ClassName
 * @Description 线路模块公共接口
 * @Author gengjb
 * @Date 2023/2/14 10:32
 * @Version 1.0
 **/
public interface MWNetWorkLinkCommonService {

    /**
     * 根据传入的IP和AssetsId查询符合条件的线路
     * @return
     */
    List<AddAndUpdateParam> getLinkByAssetsIdAndIp(AddAndUpdateParam param);

    /**
     * 查询所有线路
     * @return
     */
    List<AddAndUpdateParam> getAllLinkInfo();

    /**
     * 根据线路ID获取线路接口信息
     * @param linkIds
     * @return
     */
    List<MwLinkInterfaceDto> getLinkInterfaceInfo(List<String> linkIds);

    /**
     * 获取线路信息
     * @param linkCommonParam
     * @return
     */
    Reply getLinkInfo(MwLinkCommonParam linkCommonParam);


    /**
     * 获取目录下拉接口
     * @return
     */
    Reply getLinkDirectoryDropDown();

    
}
