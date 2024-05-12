package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MwModelSysLogDao;
import cn.mw.monitor.model.dto.SystemLogDTO;
import cn.mw.monitor.model.param.SystemLogParam;
import cn.mw.monitor.model.service.MwModelSysLogService;
import cn.mwpaas.common.model.Reply;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author qzg
 * @date 2021/11/11
 */
@Service
@Slf4j
public class MwModelSysLogServiceImpl implements MwModelSysLogService {
    @Resource
    MwModelSysLogDao mwModelSysLogDao;


    @Override
    public Reply getInstaceChangeHistory(SystemLogParam qParam) {
        try {
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            List<SystemLogDTO> list = mwModelSysLogDao.getInstaceChangeHistory(qParam);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to getInstaceChangeHistory param{}, case by {}", qParam, e);
            return Reply.fail(500, "模型日志查询失败");
        }
    }

    @Override
    @Transactional
    public Reply saveInstaceChangeHistory(SystemLogDTO qParam) {
        try {
            mwModelSysLogDao.saveInstaceChangeHistory(qParam);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to saveInstaceChangeHistory param{}, case by {}", qParam, e);
            return Reply.fail(500, "模型日志保存失败");
        }
    }

    @Override
    @Transactional
    public Reply batchSaveInstaceChangeHistory(List<SystemLogDTO> qParams) {
        try {
            mwModelSysLogDao.batchSaveInstaceChangeHistory(qParams);
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to saveInstaceChangeHistory param{}, case by {}", qParams, e);
            return Reply.fail(500, "模型日志保存失败");
        }
    }

    @Override
    public Reply getChangeHistoryVersion(String type) {
        try {
            Integer version = mwModelSysLogDao.getChangeHistoryVersion(type);
            return Reply.ok(version);
        } catch (Exception e) {
            log.error("fail to getChangeHistoryVersion param{}, case by {}", type, e);
            return Reply.fail(500, "获取日志版本失败");
        }
    }

    @Override
    @Transactional
    public Reply updateInstaceChangeHistory(String targetType, String ownType) {
        mwModelSysLogDao.updateInstaceChangeHistory(targetType,ownType);
        return Reply.ok();
    }

}
