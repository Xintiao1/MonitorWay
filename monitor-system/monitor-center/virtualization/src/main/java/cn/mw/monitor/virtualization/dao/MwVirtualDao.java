package cn.mw.monitor.virtualization.dao;

import cn.mw.monitor.common.util.QueryHostParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.virtualization.dto.VirtualUser;
import cn.mw.monitor.virtualization.dto.VirtualUserPerm;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author qzg
 * @date 2021/6/2
 */
public interface MwVirtualDao {
    List<MwTangibleassetsTable> getAssetsIdByIp(QueryHostParam qParam);

    void deleteVirtualUserByDirector(@Param("typeIds") List<String> typeIds);

    void setVirtualUserByDirector(VirtualUser qParam);

    void setVirtualUserByOrg(VirtualUser qParam);

    void setVirtualUserByGroup(VirtualUser qParam);

    void editorVirtualUserByDirector(VirtualUser qParam);

    void editorVirtualUserByOrg(VirtualUser qParam);

    void editorVirtualUserByGroup(VirtualUser qParam);

    List<VirtualUser> getVirtualUser(VirtualUser qParam);

    List<VirtualUserPerm> selectVirtualUserList(String typeId);

    List<Map> getVirtualPowerList();

}
