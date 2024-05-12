package cn.mw.monitor.license.service.impl;

import cn.mw.monitor.license.dao.MwCheckLicenseDao;
import cn.mw.monitor.service.license.service.CheckCountService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author syt
 * @Date 2021/9/26 14:44
 * @Version 1.0
 */
@Service
public class CheckCountServiceImpl implements CheckCountService {

    @Resource
    MwCheckLicenseDao mwCheckLicenseDao;

    @Override
    public int selectTableCount(String tableName, boolean deleteFlag) {
        return mwCheckLicenseDao.selectTableCount(tableName, deleteFlag);
    }

    @Override
    public int selectAssetsCount(List<Integer> assetsTypeId, List<Integer> monitorModes) {
        return mwCheckLicenseDao.selectAssetsCount(assetsTypeId, monitorModes);
    }
}
