package cn.mw.monitor.service.user.dto;

import cn.mw.monitor.state.DataType;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className DataAuthorityDTO
 * @description 数据权限实体类
 * @date 2022/4/1
 */
@Data
public class DataAuthorityDTO {

    /**
     * 对应数据的主键ID
     */
    private String id;

    /**
     * 所属类别
     */
    private DataType dataType;

    /**
     * 用户组ID列表
     */
    private List<Integer> groupIdList;

    /**
     * 负责人ID列表
     */
    private List<Integer> userIdList;

    /**
     * 机构ID列表
     */
    private List<Integer> orgIdList;

    /**
     * 机构节点node列表
     */
    private List<List<Integer>> orgNodeList;

    @Override
    public String toString() {
        return "DataAuthorityDTO{" +
                "id='" + id + '\'' +
                ", dataType=" + dataType +
                ", groupIdList=" + groupIdList +
                ", userIdList=" + userIdList +
                ", orgIdList=" + orgIdList +
                ", orgNodeList=" + orgNodeList +
                '}';
    }
}
