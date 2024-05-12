package cn.mw.monitor.logManage.service;

import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.logManage.dto.MwVectorChannelDTO;
import cn.mw.monitor.logManage.param.VectorChannelParam;
import cn.mw.monitor.logManage.param.VectorChannelSearchParam;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface MwVectorChannelService {

    ResponseBase selectList(VectorChannelSearchParam searchParam);

    ResponseBase addVectorChannel(VectorChannelParam param);

    ResponseBase updateVectorChannel(VectorChannelParam param);

    ResponseBase deleteVectorChannel(List<Integer> ids);
}
