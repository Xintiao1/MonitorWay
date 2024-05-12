package cn.mw.monitor.fingerprint.service;

import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author gui.quanwang
 * @className MwFingerprintManageService
 * @description 指纹库管理服务类
 * @date 2021/7/26
 */
public interface MwFingerprintManageService {

    /**
     * 获取指纹库版本信息（默认返回距离1970-01-01的毫秒数）
     *
     * @return 最新的版本信息
     */
    Reply getFingerPrintVersion();


    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    String getFingerprintFilePath();

    /**
     * 下载文件
     *
     * @param response
     */
    void downloadFile(HttpServletResponse response) throws Exception;
}
