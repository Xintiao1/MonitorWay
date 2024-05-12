package cn.mw.monitor.netflow.dao;

import cn.mw.monitor.netflow.entity.IpGroupNFAExpandEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author gui.quanwang
 * @className IpGroupExpandManageDao
 * @description Ip地址组拓展信息管理Dao(NFA)
 * @date 2022/8/24
 */
public interface IpGroupNFAExpandManageDao extends BaseMapper<IpGroupNFAExpandEntity> {

    /**
     * 通过IP地址组ID，获取IP地址列表
     *
     * @param ipGroupId IP地址组ID
     * @return
     */
    List<String> getIpGroupList(@Param(value = "ipGroupId") int ipGroupId);

}
