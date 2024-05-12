package cn.mw.monitor.customPage.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.customPage.api.param.QueryCustomMultiPageParam;
import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.customPage.api.param.UpdateCustomPageParam;
import cn.mw.monitor.customPage.dao.MwCustomcolTableDao;
import cn.mw.monitor.customPage.dao.MwPagefieldTableDao;
import cn.mw.monitor.customPage.dao.MwPageselectTableDao;
import cn.mw.monitor.customPage.dto.*;
import cn.mw.monitor.customPage.model.MwCustomcolTable;
import cn.mw.monitor.customPage.model.MwMultiPageselectTable;
import cn.mw.monitor.customPage.model.MwPageselectTable;
import cn.mw.monitor.customPage.service.MwCustomcolService;
import cn.mw.monitor.service.common.ServiceException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class MwCustomcolServiceImpl implements MwCustomcolService {

    @Resource
    private MwCustomcolTableDao mwCustomcolTableDao;

    @Resource
    private MwPagefieldTableDao mwPagefieldTableDao;

    @Resource
    private MwPageselectTableDao mwPageselectTableDao;

    /**
     *
     */
    @Override
    public Reply selectById(QueryCustomPageParam queryCustomPageParam) {
        try {
            List<MwPageselectTable> psTable = mwPageselectTableDao.selectByPageId(queryCustomPageParam.getPageId());
            List<MwCustomColDTO> colDTOS = mwPagefieldTableDao.selectByUserId(queryCustomPageParam);
            MwCustomPageDTO pageDTO = new MwCustomPageDTO();
            boolean flag = false;
            for (MwCustomColDTO mwCustomColDTO : colDTOS) {
                if (mwCustomColDTO.getDeleteFlag() != null && mwCustomColDTO.getDeleteFlag() == 1) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                pageDTO.setMwCustomColDTOS(colDTOS);
            } else {
                List<MwCustomColDTO> pageDTOs = mwPagefieldTableDao.selectResetById(queryCustomPageParam);
                pageDTO.setMwCustomColDTOS(pageDTOs);
            }
            pageDTO.setMwPageselectTables(psTable);
            return Reply.ok(pageDTO);
        } catch (Exception e) {
            log.error("fail to selectById with QueryCustomPageParam={}, cause:{}", queryCustomPageParam, e.getMessage());
            throw new ServiceException(Reply.fail(ErrorConstant.CUSTOMCOLCODE_260101, ErrorConstant.CUSTOMCOL_MSG_260101));
        }
    }

    @Override
    public List<MwCustomColDTO> getCustom(QueryCustomPageParam queryCustomPageParam) {
        List<MwCustomColDTO> colDTOS = mwPagefieldTableDao.selectByUserId(queryCustomPageParam);
        boolean flag = false;
        for (MwCustomColDTO mwCustomColDTO : colDTOS) {
            if (mwCustomColDTO.getDeleteFlag() != null && mwCustomColDTO.getDeleteFlag() == 1) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            return colDTOS;
        } else {
            List<MwCustomColDTO> pageDTOs = mwPagefieldTableDao.selectResetById(queryCustomPageParam);
            return pageDTOs;
        }
    }

    /**
     *
     */
    @Override
    public Reply update(List<UpdateCustomColDTO> models) {
        try {
            mwCustomcolTableDao.updateBatch(models);
            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("fail to updatecustomcol with auParam=" + JSON.toJSONString(models), e);
            throw new ServiceException(Reply.fail(ErrorConstant.CUSTOMCOLCODE_260103, ErrorConstant.CUSTOMCOL_MSG_260103));
        }
    }

    /**
     *
     */
    @Override
    public Reply selectByMultiPageId(QueryCustomMultiPageParam param) {
        try {
            List<MwCustomMultiColDTO> colDTOS = mwPagefieldTableDao.selectByMutilPageId(param);
            List<MwMultiPageselectTable> psTable = mwPageselectTableDao.selectByMultiPageId(param.getPageIds());
            MwMultiCustomPageDTO mwMultiCustomPageDTO = new MwMultiCustomPageDTO();
            Map<String, List<MwCustomColDTO>> colmap = new HashMap<String, List<MwCustomColDTO>>();
            if (null != colDTOS && colDTOS.size() > 0) {
                colDTOS.forEach(value -> {
                    String key = "p" + value.getPageId().toString();
                    colmap.put(key, value.getPagelist());
                });
                mwMultiCustomPageDTO.setMwCustomColDTOS(colmap);
            }

            Map<String, List<MwPageselectTable>> selmap = new HashMap<String, List<MwPageselectTable>>();
            if (null != psTable && psTable.size() > 0) {
                psTable.forEach(value -> {
                    String key = "p" + value.getPageId().toString();
                    selmap.put(key, value.getPagelist());
                });
                mwMultiCustomPageDTO.setMwPageselectTables(selmap);
            }

            //logger.info("customcol_LOG[]customcol[]自定义列信息管理[]根据USERID取列信息[]{}",queryCustomPageParam);
            return Reply.ok(mwMultiCustomPageDTO);
        } catch (Exception e) {
            //log.error("fail to selectById with id={}, cause:{}", queryCustomPageParam.getUserId(),e.getMessage());
            return Reply.fail(ErrorConstant.CUSTOMCOLCODE_260101, ErrorConstant.CUSTOMCOL_MSG_260101);
        }
    }

    /**
     *
     */
    @Override
    @Deprecated
    public Reply insert(List<MwCustomcolTable> models) {
        try {
            mwCustomcolTableDao.insert(models);
            return Reply.ok("新增成功");
        } catch (Exception e) {
            log.error("fail to insertcustomcol with auParam={}, cause:{}", models, e.getMessage());
            return Reply.fail(ErrorConstant.CUSTOMCOLCODE_260102, ErrorConstant.CUSTOMCOL_MSG_260102);
        }
    }

    /**
     * 还原
     */

    @Override
    public Reply reset(UpdateCustomPageParam uparam) {
        try {
            mwCustomcolTableDao.reset(uparam.getModels());
            QueryCustomPageParam queryCustomPageParam = new QueryCustomPageParam();
            queryCustomPageParam.setPageId(uparam.getPageId());
            queryCustomPageParam.setUserId(uparam.getUserId());
            List<MwCustomColDTO> pageDTOs = mwPagefieldTableDao.selectResetById(queryCustomPageParam);
            MwCustomPageDTO pageDTO = new MwCustomPageDTO();
            pageDTO.setMwCustomColDTOS(pageDTOs);
            return Reply.ok(pageDTO);

        } catch (Exception e) {
            log.error("fail to updatecustomcol with auParam={}, cause:{}", uparam.getModels(), e.getMessage());
            throw new ServiceException(Reply.fail(ErrorConstant.CUSTOMCOLCODE_260104, ErrorConstant.CUSTOMCOL_MSG_260104));
        }
    }
}
