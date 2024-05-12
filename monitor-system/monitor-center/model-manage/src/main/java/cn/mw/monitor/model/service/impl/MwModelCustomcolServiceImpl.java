package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.customPage.api.param.QueryCustomMultiPageParam;
import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.customPage.api.param.UpdateCustomPageParam;
import cn.mw.monitor.customPage.dao.MwPagefieldTableDao;
import cn.mw.monitor.customPage.dao.MwPageselectTableDao;
import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.dto.MwCustomMultiColDTO;
import cn.mw.monitor.customPage.dto.MwCustomPageDTO;
import cn.mw.monitor.customPage.dto.MwMultiCustomPageDTO;
import cn.mw.monitor.customPage.model.MwCustomcolTable;
import cn.mw.monitor.customPage.model.MwMultiPageselectTable;
import cn.mw.monitor.customPage.model.MwPageselectTable;
import cn.mw.monitor.model.dao.MwModelCustomcolTableDao;
import cn.mw.monitor.model.dto.MwCustomcolByModelTable;
import cn.mw.monitor.model.dto.MwModelCustomColDTO;
import cn.mw.monitor.model.dto.UpdateCustomPageByModelParam;
import cn.mw.monitor.model.param.QueryCustomModelparam;
import cn.mw.monitor.model.service.MwModelCustomcolService;
import cn.mw.monitor.service.common.ServiceException;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class MwModelCustomcolServiceImpl implements MwModelCustomcolService {

    @Resource
    private MwModelCustomcolTableDao mwModelCustomcolTableDao;

    @Resource
    private MwPagefieldTableDao mwPagefieldTableDao;

    @Resource
    private MwPageselectTableDao mwPageselectTableDao;

    /**
     *
     */
    @Override
    public Reply selectById(QueryCustomModelparam queryCustomModelparam) {
        try {
            List<MwPageselectTable> psTable = mwPageselectTableDao.selectByPageId(queryCustomModelparam.getModelId());
//            List<MwCustomColDTO> colDTOS = mwPagefieldTableDao.selectByUserId(queryCustomPageParam);
            List<MwCustomColDTO> colDTOS = new ArrayList<>();
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
//                List<MwCustomColDTO> pageDTOs = mwPagefieldTableDao.selectResetById(queryCustomModelparam);
//                pageDTO.setMwCustomColDTOS(pageDTOs);
            }
            pageDTO.setMwPageselectTables(psTable);
            return Reply.ok(pageDTO);
        } catch (Exception e) {
            log.error("fail to selectById with QueryCustomPageParam={}, cause:{}", queryCustomModelparam, e.getMessage());
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
    public Reply update(List<MwCustomcolByModelTable> models) {
        try {
            models = models.stream().filter(data -> data.getCustomId()!=null).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(models)){
                mwModelCustomcolTableDao.updateBatch(models);
            }

            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("fail to update with auParam={}, cause:{}", models, e);
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
            mwModelCustomcolTableDao.insert(models);
            return Reply.ok("新增成功");
        } catch (Exception e) {
            log.error("fail to insert with auParam={}, cause:{}", models, e);
            return Reply.fail(ErrorConstant.CUSTOMCOLCODE_260102, ErrorConstant.CUSTOMCOL_MSG_260102);
        }
    }

    /**
     * 还原
     */

    @Override
    public Reply reset(UpdateCustomPageByModelParam uparam) {
        try {
            mwModelCustomcolTableDao.reset(uparam.getModels());
            return Reply.ok();
        } catch (Exception e) {
            log.error("fail to reset with auParam={}, cause:{}", uparam.getModels(), e);
            throw new ServiceException(Reply.fail(ErrorConstant.CUSTOMCOLCODE_260104, ErrorConstant.CUSTOMCOL_MSG_260104));
        }
    }
}
