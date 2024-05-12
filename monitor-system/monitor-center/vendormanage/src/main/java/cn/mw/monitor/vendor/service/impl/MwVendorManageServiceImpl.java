package cn.mw.monitor.vendor.service.impl;

import cn.mw.monitor.service.vendor.model.VendorIconDTO;
import cn.mw.monitor.state.UploadCatalog;
import cn.mw.monitor.util.UploadUrlUtils;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.vendor.api.MWVendorDropDown;
import cn.mw.monitor.vendor.dao.MwVendorManageDao;
import cn.mw.monitor.vendor.dto.MwVendorManageTableDTO;
import cn.mw.monitor.vendor.model.MwBaseVendorIcon;
import cn.mw.monitor.vendor.param.AddOrUpdateVendorManageParam;
import cn.mw.monitor.vendor.param.QueryVendorManageParam;
import cn.mw.monitor.vendor.service.MwVendorManageService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author syt
 * @Date 2021/1/20 10:16
 * @Version 1.0
 */
@Service
@Slf4j
@Transactional
public class MwVendorManageServiceImpl implements MwVendorManageService, MWVendorDropDown {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/vendor");

    @Resource
    private MwVendorManageDao mwVendorManageDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    UploadUrlUtils uploadUrlUtils;

    @Override
    public Reply selectVendorDropdownList(String specification, boolean selectFlag) {
        try {
            if(!selectFlag) {
                List<MwBaseVendorIcon> list = (specification != null && StringUtils.isNotEmpty(specification)) ?
                        mwVendorManageDao.selectVendorDropdownList(specification)
                        : mwVendorManageDao.selectBVendorDropdownList();
                return Reply.ok(list);
            } else {
                return Reply.ok(mwVendorManageDao.selectDropdown());
            }
        } catch (Exception e) {
            log.error("fail to selectVendorDropdownList, cause:{}", e);
            return Reply.fail(ErrorConstant.VENDOR_MANAGE_DROPDOWN_CODE_312005, ErrorConstant.VENDOR_MANAGE_DROPDOWN_MSG_312005);
        }

    }

    @Override
    public Reply selectVModelDropdownList(String vendor) {
        try {
            return Reply.ok(mwVendorManageDao.selectVModelDropdownList(vendor));
        } catch (Exception e) {
            log.error("fail to selectVModelDropdownList with vendor={}, cause:{}", vendor, e);
            return Reply.fail(ErrorConstant.VENDOR_MANAGE_MODEL_DROPDOWN_CODE_312006, ErrorConstant.VENDOR_MANAGE_MODEL_DROPDOWN_MSG_312006);
        }
    }

    @Override
    public Reply selectById(Integer id) {
        try {
            return Reply.ok(mwVendorManageDao.selectById(id));
        } catch (Exception e) {
            log.error("fail to selectById with id={}, cause:{}", id, e);
            return Reply.fail(ErrorConstant.VENDOR_MANAGE_SELECT_CODE_312001, ErrorConstant.VENDOR_MANAGE_SELECT_MSG_312001);
        }
    }

    @Override
    public Reply selectList(QueryVendorManageParam qsParam) {
        try {
            PageHelper.startPage(qsParam.getPageNumber(), qsParam.getPageSize());
            Map criteria = PropertyUtils.describe(qsParam);
            List<MwVendorManageTableDTO> mwVendorManageTableDTOS = mwVendorManageDao.selectList(criteria);
            if(null!=mwVendorManageTableDTOS) {
                for (MwVendorManageTableDTO list : mwVendorManageTableDTOS) {
                    String module = null;
                    if (list.getVendorIconDTO() != null) {
                        if (list.getVendorIconDTO().getCustomFlag() == 1) {
                            module = "vendor-upload";
                        } else {
                            module = "Small";
                        }
                        String s = uploadUrlUtils.transferUrl(list.getVendorIconDTO().getCustomFlag(), list.getVendorIconDTO().getVendorSmallIcon(), module);
                        list.setVendorIconDTO(new VendorIconDTO(list.getVendorIconDTO().getId(),s, s, list.getVendorIconDTO().getCustomFlag()));
                    }else{
                        list.setVendorIconDTO(VendorIconDTO.builder().vendorLargeIcon("").vendorSmallIcon("").build());
                    }
                }
            }
            PageInfo pageInfo = new PageInfo<>(mwVendorManageTableDTOS);
            pageInfo.setList(mwVendorManageTableDTOS);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectList with qsParam={}, cause:{}", qsParam, e);
            return Reply.fail(ErrorConstant.VENDOR_MANAGE_SELECT_CODE_312001, ErrorConstant.VENDOR_MANAGE_SELECT_MSG_312001);
        }
    }

    @Override
    public Reply update(AddOrUpdateVendorManageParam uParam) {
        QueryVendorManageParam param = new QueryVendorManageParam();
        param.setBrand(uParam.getBrand());
        param.setSpecification(uParam.getSpecification());
        try {
            Map criteria = PropertyUtils.describe(param);
            List<MwVendorManageTableDTO> list = mwVendorManageDao.selectList(criteria);
            if (list.size() > 0) {
                for (MwVendorManageTableDTO vendor : list) {
                    if (!vendor.getId().equals(uParam.getId())) {
                        return Reply.fail(ErrorConstant.VENDOR_MANAGE_UPDATE_CODE_312002, "厂商与规格型号已存在，修改失败！");
                    }
                }
            }
        } catch (Exception e) {
            log.error("fail to update with AddOrUpdateVendorManageParam={}, cause:{}", uParam, e);
            return Reply.fail(ErrorConstant.VENDOR_MANAGE_SELECT_CODE_312001, ErrorConstant.VENDOR_MANAGE_SELECT_MSG_312001);
        }
        uParam.setModifier(iLoginCacheInfo.getLoginName());
        mwVendorManageDao.update(uParam);
        return Reply.ok("更新成功");
    }

    @Override
    public Reply delete(List<Integer> ids,List<Integer> vendorIds) {
        mwVendorManageDao.delete(ids);
        //需要删除关联表，不然下拉框还是可以查询出数据
        if(!CollectionUtils.isEmpty(vendorIds)){
            mwVendorManageDao.deleteVendorIcon(vendorIds);
        }
        return Reply.ok("删除成功");
    }

    @Override
    public Reply insert(AddOrUpdateVendorManageParam aParam) throws Exception {
        QueryVendorManageParam param = new QueryVendorManageParam();
        param.setBrand(aParam.getBrand());
        param.setSpecification(aParam.getSpecification());
        if(null==aParam.getCustomBrand())
            aParam.setCustomBrand(false);
        //是否自定义厂商
        if(!aParam.getCustomBrand()) {
            try {
                Map criteria = PropertyUtils.describe(param);
                List<MwVendorManageTableDTO> list = mwVendorManageDao.selectList(criteria);
                if (list.size() > 0) {
                    return Reply.fail(ErrorConstant.VENDOR_MANAGE_INSERT_CODE_312003, "厂商与规格型号已存在，新增失败！");
                }
            } catch (Exception e) {
                log.error("fail to insert with AddOrUpdateVendorManageParam={}, cause:{}", aParam, e);
                return Reply.fail(ErrorConstant.VENDOR_MANAGE_SELECT_CODE_312001, ErrorConstant.VENDOR_MANAGE_SELECT_MSG_312001);
            }
            aParam.setModifier(iLoginCacheInfo.getLoginName());
            aParam.setCreator(iLoginCacheInfo.getLoginName());
            mwVendorManageDao.insert(aParam);
            return Reply.ok("新增成功");
        }else{
            //设置用户图片上传标志
            aParam.setCustomFlag(UploadCatalog.UPLOAD.getCode());
            int vcount=mwVendorManageDao.selectCountVendorByName(aParam.getBaseVendor());
            if(vcount>0){
                return Reply.fail("该厂商已存在");
            }
            if(null!=aParam.getMac()&& !"".equals(aParam.getMac())){
                try {
                    aParam.setModifier(iLoginCacheInfo.getLoginName());
                    aParam.setCreator(iLoginCacheInfo.getLoginName());
                    mwVendorManageDao.insertBaseVendor(aParam);
                    int vendorId=mwVendorManageDao.selectVendorIdBYName(aParam.getBaseVendor());
                    aParam.setVendorId(vendorId);
                    mwVendorManageDao.insertMacVendor(aParam);
                    mwVendorManageDao.insert(aParam);

                    return Reply.ok("新增成功");
                } catch (Exception e) {
                    log.error("fail to insert with AddOrUpdateVendorManage Param={}, cause:{}", aParam, e);
                    return Reply.fail(e.getMessage());
                }
            }else{
                //没有填写mac信息给默认值999999
                try {
                    aParam.setModifier(iLoginCacheInfo.getLoginName());
                    aParam.setCreator(iLoginCacheInfo.getLoginName());
                    aParam.setMac("999999");
                    mwVendorManageDao.insertBaseVendor(aParam);
                    int vendorId=mwVendorManageDao.selectVendorIdBYName(aParam.getBaseVendor());
                    aParam.setVendorId(vendorId);
                    mwVendorManageDao.insertMacVendor(aParam);
                    mwVendorManageDao.insert(aParam);
                    return Reply.ok("新增成功");
                } catch (Exception e) {
                    log.error(e.toString());
                    return Reply.fail(e.getMessage());
                }
            }


        }

    }

}
