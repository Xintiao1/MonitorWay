package cn.mw.monitor.fingerprint.dao;

/**
 * @author gui.quanwang
 * @className MwFingerprintManageDao
 * @description 指纹库管理Dao层
 * @date 2021/7/26
 */
public interface MwFingerprintManageDao {

    /**
     * 获取指纹库版本信息（默认返回距离1970-01-01的毫秒数）
     *
     * @return 最新的版本信息
     */
    String getFingerPrintVersion();


    /**
     * 获取文件路径
     *
     * @return 文件路径
     */
    String getFingerprintFilePath();

    /**
     * 获取文件名称
     *
     * @return 文件名称
     */
    String getFingerprintFileName();
}
