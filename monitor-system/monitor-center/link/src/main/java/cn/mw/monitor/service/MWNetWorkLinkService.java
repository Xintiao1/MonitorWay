package cn.mw.monitor.service;

import cn.mw.monitor.link.dto.MwLinkTreeDto;
import cn.mw.monitor.service.link.param.AddAndUpdateParam;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.link.dto.NetWorkLinkDto;
import cn.mw.monitor.link.param.*;

import java.util.List;

/**
 * @author xhy
 * @date 2020/7/20 11:22
 */
public interface MWNetWorkLinkService {
    Reply selectList(LinkDropDownParam param);

    Reply insertNetWorkLink(AddAndUpdateParam addAndUpdateParam);

    Reply getAssetsList(DropDownParam dropDownParam);

    Reply getIpAddressList(DropDownParam dropDownParam);

    Reply editorNetWorkLink(AddAndUpdateParam addAndUpdateParam);

    Reply deleteNetWorkLink(DeleteLinkParam dParam);

    Reply selectLink(String linkId);

    Reply enableActive(String enable, String linkId);

    void deleteMappers(List<String> linkIds);

    List<NetWorkLinkDto> getNetWorkLinkDtos(LinkDropDownParam param);

    Reply getLinkList();

    Reply getBandwidth(DropDownParam param);

    Reply fuzzSearchAllFiledData(String value);

    List<NetWorkLinkDto>  seleAllLink();

    /**
     * 查询线路树状结构数据
     * @return
     */
    Reply selectLinkTree();

    /**
     * 创建线路目录
     * @param mwLinkTreeDto 目录数据
     * @return
     */
    Reply createLinkContents(MwLinkTreeDto mwLinkTreeDto);

    /**
     * 修改线路目录
     * @param mwLinkTreeDtos 目录数据
     * @return
     */
    Reply updateLinkContents(List<MwLinkTreeDto> mwLinkTreeDtos);

    /**
     * 删除线路目录
     * @param mwLinkTreeDto 目录数据
     * @return
     */
    Reply deleteLinkContents(MwLinkTreeDto mwLinkTreeDto);

    /**
     * 查询线路下拉数据
     * @return
     */
    Reply selectLinkDropDown();

    /**
     * 拖动目录数据
     * @param mwLinkTreeDto 拖动的目录信息
     * @return
     */
    Reply dragLinkContents(MwLinkTreeDto mwLinkTreeDto);

    /**
     * 获取线路目录下拉数据
     * @return
     */
    Reply getLinkTreeDropDown();

    /**
     * 获取线路类型趋势图
     * @param linkDto
     * @return
     */
    Reply getLinkTrendData(NetWorkLinkDto linkDto);

    /**
     * 查询状态下拉
     * @return
     */
    Reply getLinkStatusDropDown();

    /**
     * 获取目录分类明细
     * @return
     */
    Reply getLinkDirectoryDetail();
}
