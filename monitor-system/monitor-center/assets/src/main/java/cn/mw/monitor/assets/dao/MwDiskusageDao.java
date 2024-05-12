package cn.mw.monitor.assets.dao;

import cn.mw.monitor.assets.model.MwDiskusage;

import java.util.List;

public interface MwDiskusageDao {
    /**
     * 新增磁盘信息
     *
     * @param mwDiskusage 磁盘信息
     * @return
     */
    int insert(MwDiskusage mwDiskusage);

    int batchInsert(List<MwDiskusage> list);
}
