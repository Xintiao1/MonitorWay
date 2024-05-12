package cn.huaxing.service;

import cn.huaxing.param.HuaxingVisualizedParam;
import cn.mwpaas.common.model.Reply;

/**
 * @author gengjb
 * @description 可视化插件接口
 * @date 2023/8/28 10:40
 */
public interface HuaxingVisualizedService {

    Reply getHuaxingDataBaseInfo(HuaxingVisualizedParam visualizedParam);

}
