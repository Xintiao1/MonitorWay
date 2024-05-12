package cn.mw.monitor.user.service;

import cn.mw.monitor.user.model.MwUserGroupMapper;
import cn.mwpaas.common.model.Reply;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author swy
 * @since 2023-11-21
 */
public interface MwUserGroupMapperService extends IService<MwUserGroupMapper> {

    Reply sort(Integer currentId, Integer targetId);
}
