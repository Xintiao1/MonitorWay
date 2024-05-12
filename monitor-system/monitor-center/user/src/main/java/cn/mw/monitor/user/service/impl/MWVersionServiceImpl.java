package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.user.dao.MWVersionDao;
import cn.mw.monitor.user.model.MwDbVersionDTO;
import cn.mw.monitor.user.service.MWVersionService;
import cn.mwpaas.common.model.Reply;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class MWVersionServiceImpl implements MWVersionService {
    @Resource
    private MWVersionDao mwVersionDao;

    @Override
    public Reply selectVersion() {
        try {
            MwDbVersionDTO mwVersion = mwVersionDao.selectVersion();
            return Reply.ok(mwVersion);
        } catch (Exception e) {
            return Reply.fail(e.getMessage(),null);
        }
    }
}
