package cn.mw.monitor.fingerprint.service.impl;

import cn.mw.monitor.fingerprint.dao.MwFingerprintManageDao;
import cn.mw.monitor.fingerprint.service.MwFingerprintManageService;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gui.quanwang
 * @className MwFingerprintManageServiceImpl
 * @description 指纹库管理实现类
 * @date 2021/7/26
 */
@Service
@Transactional
@Slf4j
public class MwFingerprintManageServiceImpl implements MwFingerprintManageService {


    @Resource
    private MwFingerprintManageDao mwFingerprintManageDao;

    /**
     * 获取指纹库版本信息（默认返回距离1970-01-01的毫秒数）
     *
     * @return 最新的版本信息
     */
    @Override
    public Reply getFingerPrintVersion() {
        Map versionMap = new HashMap();
        String version = mwFingerprintManageDao.getFingerPrintVersion();
        versionMap.put("version", version);
        return Reply.ok(versionMap);
    }

    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    @Override
    public String getFingerprintFilePath() {
        return mwFingerprintManageDao.getFingerprintFilePath();
    }


    /**
     * 下载文件
     *
     * @param response
     */
    @Override
    public void downloadFile(HttpServletResponse response) throws Exception {
        String filePath = this.getFingerprintFilePath();
        String fileName = mwFingerprintManageDao.getFingerprintFileName();
        OutputStream os = null;
        InputStream is = null;

        try {
            //取得输出流
            os = response.getOutputStream();
            //清空输出流
            response.reset();
            response.setContentType("application/x-download;charset=GBK");
            response.setHeader("Content-Disposition", "attachment;filename=" + new String(fileName.getBytes("utf-8")));
            //读取流
            File f = new File(filePath);
            is = new FileInputStream(f);
            IOUtils.copy(is, response.getOutputStream());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
