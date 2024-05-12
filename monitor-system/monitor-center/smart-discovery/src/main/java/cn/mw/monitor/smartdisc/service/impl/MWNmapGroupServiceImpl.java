package cn.mw.monitor.smartdisc.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.smartDiscovery.api.MWNmapGroupService;
import cn.mw.monitor.smartdisc.dao.MWNmapGroupDao;
import cn.mw.monitor.smartdisc.model.*;
import cn.mwpaas.common.model.Reply;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MWNmapGroupServiceImpl implements MWNmapGroupService {
    @Resource
    private MWNmapGroupDao mwNmapGroupDao;


    @Override
    public Reply getDropDownFingerNodeGroup() {
        try {
            List<MWNmapFingerNodeGroup> mwNmapFingerNodeGroups = mwNmapGroupDao.selectFingerNodeGroup();
            return Reply.ok(mwNmapFingerNodeGroups);
        }catch (Exception e) {
            log.error("fail to getDropDownFingerNodeGroup", e);
            return Reply.fail(ErrorConstant.NMAP_316001, ErrorConstant.NMAP_MSG_316001);
        }
    }

    @Override
    public Reply getDropDownExceptionNodeGroup() {
        try {
            List<MWNmapExceptionNodeGroup> mwNmapExceptionNodeGroups = mwNmapGroupDao.selectExceptionNodeGroup();
            return Reply.ok(mwNmapExceptionNodeGroups);
        }catch (Exception e) {
            log.error("fail to getDropDownExceptionNodeGroup", e);
            return Reply.fail(ErrorConstant.NMAP_316002, ErrorConstant.NMAP_MSG_316002);
        }
    }

    @Override
    public Reply getDropDownPortGroup() {
        try {
            List<MWNmapPortGroup> mwNmapPortGroups = mwNmapGroupDao.selectPortGroup();
            return Reply.ok(mwNmapPortGroups);
        }catch (Exception e) {
            log.error("fail to getDropDownPortGroup", e);
            return Reply.fail(ErrorConstant.NMAP_316003, ErrorConstant.NMAP_MSG_316003);
        }
    }

    @Override
    public Reply getDropDownLiveNodeGroup() {
        try {
            List<MWNmapLiveNodeGroup> mwNmapLiveNodeGroups = mwNmapGroupDao.selectLiveNodeGroup();
            return Reply.ok(mwNmapLiveNodeGroups);
        }catch (Exception e) {
            log.error("fail to getDropDownLiveNodeGroup", e);
            return Reply.fail(ErrorConstant.NMAP_316004, ErrorConstant.NMAP_MSG_316004);
        }
    }

    @Override
    public Reply getDropDownNodeGroup() {
        try {
            List<MWNmapNodeGroup> mwNmapNodeGroups = mwNmapGroupDao.selectNodeGroup();
            return Reply.ok(mwNmapNodeGroups);
        }catch (Exception e) {
            log.error("fail to getDropDownNodeGroup", e);
            return Reply.fail(ErrorConstant.NMAP_316005, ErrorConstant.NMAP_MSG_316005);
        }
    }
}
