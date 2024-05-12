package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MWModelVendorDao;
import cn.mw.monitor.model.param.AddAndUpdateModelFirmParam;
import cn.mw.monitor.model.param.AddAndUpdateModelMACParam;
import cn.mw.monitor.model.param.AddAndUpdateModelSpecificationParam;
import cn.mw.monitor.model.service.MWModelVendorService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author qzg
 * @date 2022/4/28
 */
@Service
@Slf4j
public class MWModelVendorServiceImpl implements MWModelVendorService {
    @Resource
    MWModelVendorDao mwModelVendorDao;
    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Value("${file.url}")
    private String imgPath;
    //厂商图标上传目录
    static final String MODULE = "vendor-upload";

    /**
     * 厂商/品牌 新增
     *
     * @param param
     * @return
     */
    @Override
    @Transactional
    public Reply modelFirmAdd(AddAndUpdateModelFirmParam param) {
        try {
            //插入之前判断名称是否重复
            Integer check = mwModelVendorDao.checkFirmByName(param);
            if (check != null && check == 0) {
                if(!Strings.isNullOrEmpty(param.getVendorSmallIcon()) || !Strings.isNullOrEmpty(param.getVendorLargeIcon()) ){
                    param.setCustomFlag(1);
                }
                mwModelVendorDao.modelFirmAdd(param);
            } else {
                return Reply.fail(500, "厂商/品牌名称已存在，请重新输入");
            }
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to modelFirmAdd param{}, case by {}", param, e);
            return Reply.fail(500, "厂商/品牌新增失败");
        }
    }

    /**
     * 厂商/品牌修改
     *
     * @param param
     * @return
     */
    @Override
    @Transactional
    public Reply updateModelFirm(AddAndUpdateModelFirmParam param) {
        try {
            if(!Strings.isNullOrEmpty(param.getVendorSmallIcon()) || !Strings.isNullOrEmpty(param.getVendorLargeIcon()) ){
                param.setCustomFlag(1);
            }
            mwModelVendorDao.updateModelFirm(param);
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to updataModelFirm param{}, case by {}", param, e);
            return Reply.fail(500, "厂商/品牌修改失败");
        }
    }

    /**
     * 厂商名称重复性查询
     *
     * @param param
     * @return
     */
    @Override
    public Reply checkModelFirmByName(AddAndUpdateModelFirmParam param) {
        try {
            Integer check = mwModelVendorDao.checkFirmByName(param);
            return Reply.ok(check);
        } catch (Throwable e) {
            log.error("fail to checkModelFirmByName param{}, case by {}", param, e);
            return Reply.fail(500, "厂商名称重复性查询失败");
        }
    }

    /**
     * 获取厂商树结构
     *
     * @return
     */
    @Override
    public Reply queryModelFirmTree() {
        try {
            List<AddAndUpdateModelFirmParam> list = mwModelVendorDao.queryModelFirmTree();
            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to queryModelFirmTree param{}, case by {}", e);
            return Reply.fail(500, "获取厂商树结构");
        }
    }

    /**
     * 删除厂商
     * 厂商删除时，地下规格型号一起删除
     *
     * @param param
     * @return
     */
    @Override
    @Transactional
    public Reply deleteModelFirm(AddAndUpdateModelFirmParam param) {
        try {
            //删除厂商
            mwModelVendorDao.deleteModelFirm(param.getId());
            //删除规格型号
            mwModelVendorDao.deleteSpecificationByBrand(param.getId());
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to deleteModelFirm param{}, case by {}", e);
            return Reply.fail(500, "删除厂商数据失败");
        }
    }

    /**
     * 新增厂商的规格型号
     *
     * @param qParam
     * @return
     */
    @Override
    @Transactional
    public Reply addBrandSpecification(AddAndUpdateModelSpecificationParam qParam) {
        try {
            qParam.setCreator(iLoginCacheInfo.getLoginName());
            qParam.setModifier(iLoginCacheInfo.getLoginName());
            //新增之前查重
            Integer check = mwModelVendorDao.checkSpecification(qParam);
            if (check != null && check == 0) {
                mwModelVendorDao.addBrandSpecification(qParam);
            } else {
                return Reply.fail(500, "厂商规格型号重复，重新输入");
            }
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to addBrandSpecification param{}, case by {}", qParam, e);
            return Reply.fail(500, "新增厂商的规格型号失败");
        }
    }

    /**
     * 修改厂商的规格型号
     *
     * @param qParam
     * @return
     */
    @Override
    @Transactional
    public Reply updateBrandSpecification(AddAndUpdateModelSpecificationParam qParam) {
        try {
            qParam.setModifier(iLoginCacheInfo.getLoginName());
            mwModelVendorDao.updateBrandSpecification(qParam);
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to updataModelFirm param{}, case by {}", qParam, e);
            return Reply.fail(500, "修改厂商的规格型号失败");
        }
    }

    /**
     * 删除厂商的规格型号
     *
     * @param qParam
     * @return
     */
    @Override
    @Transactional
    public Reply deleteBrandSpecification(AddAndUpdateModelSpecificationParam qParam) {
        try {
            mwModelVendorDao.deleteBrandSpecification(qParam.getIds());
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to deleteBrandSpecification param{}, case by {}", qParam, e);
            return Reply.fail(500, "删除厂商的规格型号失败");
        }
    }

    /**
     * 查询厂商的规格型号类别数据
     *
     * @param qParam
     * @return
     */
    @Override
    public Reply queryBrandSpecification(AddAndUpdateModelSpecificationParam qParam) {
        try {
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            List<AddAndUpdateModelSpecificationParam> list = mwModelVendorDao.queryBrandSpecification(qParam);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Throwable e) {
            log.error("fail to queryBrandSpecification param{}, case by {}", qParam, e);
            return Reply.fail(500, "查询厂商的规格型号类别数据失败");
        }
    }

    @Override
    public Reply querySpecificationByBrand(AddAndUpdateModelSpecificationParam qParam) {
        try {
            List<AddAndUpdateModelSpecificationParam> list = mwModelVendorDao.queryBrandSpecification(qParam);
            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to querySpecificationByBrand param{}, case by {}", qParam, e);
            return Reply.fail(500, "根据厂商查询规格型号数据失败");
        }
    }

    /**
     * 厂商规格型号重复性查询
     *
     * @param qParam
     * @return
     */
    @Override
    public Reply checkSpecification(AddAndUpdateModelSpecificationParam qParam) {
        try {
            Integer check = mwModelVendorDao.checkSpecification(qParam);
            return Reply.ok(check);
        } catch (Throwable e) {
            log.error("fail to checkSpecification param{}, case by {}", qParam, e);
            return Reply.fail(500, "厂商规格型号重复性查询失败");
        }
    }

    /**
     * MAC特性List查询
     *
     * @param qParam
     * @return
     */
    @Override
    public Reply queryMACInfoList(AddAndUpdateModelMACParam qParam) {
        try {
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            List<AddAndUpdateModelMACParam> list = mwModelVendorDao.queryMACInfoList(qParam);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Throwable e) {
            log.error("fail to queryMACInfoList param{}, case by {}", qParam, e);
            return Reply.fail(500, "获取MAC特性列表失败");
        }
    }

    /**
     * MAC特性信息新增
     *
     * @param qParam
     * @return
     */
    @Override
    @Transactional
    public Reply addMACInfo(AddAndUpdateModelMACParam qParam) {
        try {
            mwModelVendorDao.addMACInfo(qParam);
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to addMACInfo param{}, case by {}", qParam, e);
            return Reply.fail(500, "MAC特性信息新增失败");
        }
    }

    /**
     * MAC特性信息修改
     *
     * @param qParam
     * @return
     */
    @Override
    @Transactional
    public Reply editorMACInfo(AddAndUpdateModelMACParam qParam) {
        try {
            mwModelVendorDao.editorMACInfo(qParam);
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to editorMACInfo param{}, case by {}", qParam, e);
            return Reply.fail(500, "MAC特性信息修改失败");
        }
    }

    /**
     * MAC特性信息删除
     *
     * @param qParam
     * @return
     */
    @Override
    @Transactional
    public Reply deleteMACInfo(AddAndUpdateModelMACParam qParam) {
        try {
            mwModelVendorDao.deleteMACInfo(qParam.getMacList());
            return Reply.ok();
        } catch (Throwable e) {
            log.error("fail to deleteMACInfo param{}, case by {}", qParam, e);
            return Reply.fail(500, "MAC特性信息删除失败");
        }
    }

    /**
     * MAC特性信息唯一性校验
     *
     * @param qParam
     * @return
     */
    @Override
    public Reply checkMACInfo(AddAndUpdateModelMACParam qParam) {
        try {
            Integer num = mwModelVendorDao.checkMACInfo(qParam);
            return Reply.ok(num);
        } catch (Throwable e) {
            log.error("fail to checkMACInfo param{}, case by {}", qParam, e);
            return Reply.fail(500, "MAC特性信息唯一性校验失败");
        }
    }


    /**
     * 根据厂商简称查询所有全称
     *
     * @param qParam
     * @return
     */
    @Override
    public Reply getMacVendorByShortName(AddAndUpdateModelMACParam qParam) {
        try {
            List<String> list =  mwModelVendorDao.getMacVendorByShortName(qParam);
            return Reply.ok(list);
        } catch (Throwable e) {
            log.error("fail to getMacVendorByShortName param{}, case by {}", qParam, e);
            return Reply.fail(500, "获取厂商全称失败");
        }
    }

    @Override
    public Reply imageUpload(MultipartFile multipartFile) {

        if (multipartFile.isEmpty()) {
            return Reply.fail("文件为空");
        }
        //获取文件名
        String fileName = multipartFile.getOriginalFilename();
        //文件重命名，防止重复
        fileName = System.currentTimeMillis() + fileName;
        //设置放到数据库字段的值
        String fileNameInTable = fileName;
        File file = new File(new File(imgPath).getAbsolutePath() + File.separator + MODULE + File.separator + fileName);
        //检测是否存在目录
        String path = new File(imgPath).getAbsolutePath() + File.separator + MODULE + File.separator;
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            multipartFile.transferTo(file);
        } catch (Exception e) {
            log.error("transferTo fail {}",e);
            return Reply.fail(e.getMessage(), multipartFile);
        }
        //修改文件权限
        Runtime runtime = Runtime.getRuntime();
        String command = "chmod 644 " + file.getAbsolutePath();
        try {
            Process process = runtime.exec(command);
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0) {
                log.info("change file permission failed");
            }
        } catch (IOException | InterruptedException e) {
            log.error("fail to imageUpload param{}, case by {}", e);
        }
        return Reply.ok(fileNameInTable);
    }

    /**
     * 规格型号字段联想
     *
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledBySpecification() {
        try {
            //根据值模糊查询数据
            List<Map<String, String>> fuzzSeachAllFileds = mwModelVendorDao.fuzzSearchAllFiledBySpecification();
            Set<String> fuzzSeachData = new HashSet<>();
            if (!CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
                for (Map<String, String> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                    String specification = fuzzSeachAllFiled.get("specification");
                    String description = fuzzSeachAllFiled.get("description");
                    String creator = fuzzSeachAllFiled.get("creator");
                    String modifier = fuzzSeachAllFiled.get("modifier");
                    if (StringUtils.isNotBlank(specification)) {
                        fuzzSeachData.add(specification);
                    }
                    if (StringUtils.isNotBlank(description)) {
                        fuzzSeachData.add(description);
                    }
                    if (StringUtils.isNotBlank(creator)) {
                        fuzzSeachData.add(creator);
                    }
                    if (StringUtils.isNotBlank(modifier)) {
                        fuzzSeachData.add(modifier);
                    }
                }
            }
            Map<String, Set<String>> fuzzyQuery = new HashMap<>();
            fuzzyQuery.put("fuzzyQuery", fuzzSeachData);
            return Reply.ok(fuzzyQuery);
        } catch (Throwable e) {
            log.error("fail to fuzzSearchAllFiledBySpecification param{}, case by {}", e);
            return Reply.fail(500, "规格型号字段联想失败");
        }
    }

    /**
     * MAC特性字段联想
     *
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledByMAC() {
        try {
            //根据值模糊查询数据
            List<Map<String, String>> fuzzSeachAllFileds = mwModelVendorDao.fuzzSearchAllFiledByMAC();
            Set<String> fuzzSeachData = new HashSet<>();
            if (!CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
                for (Map<String, String> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                    if(fuzzSeachAllFiled!=null){
                        String mac = fuzzSeachAllFiled.get("mac")!=null?fuzzSeachAllFiled.get("mac"):"";
                        String vendor = fuzzSeachAllFiled.get("vendor")!=null?fuzzSeachAllFiled.get("vendor"):"";
                        String shortName = fuzzSeachAllFiled.get("short_name")!=null?fuzzSeachAllFiled.get("short_name"):"";
                        String country = fuzzSeachAllFiled.get("country")!=null?fuzzSeachAllFiled.get("country"):"";
                        String address = fuzzSeachAllFiled.get("address")!=null?fuzzSeachAllFiled.get("address"):"";
                        if (StringUtils.isNotBlank(mac)) {
                            fuzzSeachData.add(mac);
                        }
                        if (StringUtils.isNotBlank(vendor)) {
                            fuzzSeachData.add(vendor);
                        }
                        if (StringUtils.isNotBlank(shortName)) {
                            fuzzSeachData.add(shortName);
                        }
                        if (StringUtils.isNotBlank(country)) {
                            fuzzSeachData.add(country);
                        }
                        if (StringUtils.isNotBlank(address)) {
                            fuzzSeachData.add(address);
                        }
                    }
                }
            }
            Map<String, Set<String>> fuzzyQuery = new HashMap<>();
            fuzzyQuery.put("fuzzyQuery", fuzzSeachData);
            return Reply.ok(fuzzyQuery);
        } catch (Throwable e) {
            log.error("fail to fuzzSearchAllFiledByMAC param{}, case by {}", e);
            return Reply.fail(500, "MAC特性字段联想失败");
        }
    }

}
