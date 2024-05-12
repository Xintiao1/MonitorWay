package cn.mw.monitor.assets.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.assets.api.param.assets.AddUpdateAssetsIotParam;
import cn.mw.monitor.assets.dto.SoundParam;

/**
 * @author xhy
 * @date 2020/6/6 10:36
 */
public interface MwAssetsIotService {
    Reply aupdate(AddUpdateAssetsIotParam auParam);

    Reply selectList(AddUpdateAssetsIotParam param);

    Reply updateVoice(SoundParam soundParam);

    Reply selectThreshold(String assetsId);

    Reply selectIotTypeList();
}
