package cn.mw.monitor.assets.service;

import cn.mw.monitor.assets.param.MWTreeCustomClassifyParam;
import cn.mwpaas.common.model.Reply;

/**
 * @ClassName MWTreeCustomClassifyService
 * @Author gengjb
 * @Date 2021/9/9 11:36
 * @Version 1.0
 **/
public interface MWTreeCustomClassifyService {

    /**
     * 创建树状结构自定义数据
     * @param classifyParam 自定义分类数据
     * @return
     */
    Reply createCustomClassify(MWTreeCustomClassifyParam classifyParam);


    /**
     * 删除树状结构自定义数据
     * @param classifyParam 自定义分类数据
     * @return
     */
    Reply deleteCustomClassify(MWTreeCustomClassifyParam classifyParam);

    /**
     * 修改树状结构自定义数据
     * @param classifyParam 自定义分类数据
     * @return
     */
    Reply updateCustomClassify(MWTreeCustomClassifyParam classifyParam);

    /**
     * 查询树状结构自定义数据
     * @param classifyParam 自定义分类数据
     * @return
     */
    Reply selectCustomClassify(MWTreeCustomClassifyParam classifyParam);

    /**
     * 根据自定义层级查询资产数据
     * @param classifyParam 自定义分类数据
     * @return
     */
    Reply selectCustomClassifyAssets(MWTreeCustomClassifyParam classifyParam);
}
