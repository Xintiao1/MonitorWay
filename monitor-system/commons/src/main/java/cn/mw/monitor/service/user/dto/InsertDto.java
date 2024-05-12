package cn.mw.monitor.service.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/6/1 9:46
 */
@Data
@Builder
public class InsertDto {
    /**
     * 用户组ID列表
     */
    private List<Integer> groupIds;

    /**
     * 负责人ID列表
     */
    private List<Integer> userIds;

    /**
     * 机构ID列表
     */
    private List<List<Integer>> orgIds;

    /**
     * 关联的主键ID
     */
    private String typeId;

    /**
     * 类别ID {@link cn.mw.monitor.state.DataType}
     */
    private String type;

    /**
     * 类别描述 {@link cn.mw.monitor.state.DataType}
     */
    private String desc;
}
