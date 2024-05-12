package cn.mw.monitor.server.service;

import cn.mw.monitor.TPServer.model.TPServerTypeEnum;
import cn.mw.monitor.server.serverdto.ApplicationDTO;

import java.util.List;

/**
 * @author gui.quanwang
 * @className ResultResolver
 * @description 结果解析接口
 * @date 2023/2/13
 */
public interface ResultResolver {

    /**
     * 解析数据
     *
     * @param serverType 服务器类别
     * @param data       原始数据
     * @return
     */
    List<ApplicationDTO> analysisResult(TPServerTypeEnum serverType, String data);

}
