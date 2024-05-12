package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.AddAndUpdateModelFirmParam;
import cn.mw.monitor.model.param.AddAndUpdateModelMACParam;
import cn.mw.monitor.model.param.AddAndUpdateModelSpecificationParam;
import cn.mw.monitor.model.service.MWModelVendorService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author qzg
 * @date 2022/4/28
 */
@RequestMapping("/mwapi/modelVendor")
@Controller
@Slf4j
@Api(value = "资源中心", tags = "厂商规格型号关联接口")
public class MWModelVendorController extends BaseApiService {
    @Autowired
    private MWModelVendorService mwModelVendorService;

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/firm/create")
    @ResponseBody
    @ApiOperation(value = "厂商新建接口")
    public ResponseBase modelFirmAdd(@RequestBody AddAndUpdateModelFirmParam param) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.modelFirmAdd(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("modelFirmAdd{}", e);
            return setResultFail("厂商新建接口失败","");
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/firm/editor")
    @ResponseBody
    @ApiOperation(value = "厂商修改接口")
    public ResponseBase updateModelFirm(@RequestBody AddAndUpdateModelFirmParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.updateModelFirm(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("厂商/品牌修改失败:updateModelFirm{}", e);
            return setResultFail("厂商修改失败", "厂商/品牌修改失败");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/firm/checkByName")
    @ResponseBody
    @ApiOperation(value = "厂商名称重复性查询")
    public ResponseBase checkModelFirmByName(@RequestBody AddAndUpdateModelFirmParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.checkModelFirmByName(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("checkModelFirmByName{}", e);
            return setResultFail("厂商名称重复性查询失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/firm/browse")
    @ResponseBody
    @ApiOperation(value = "厂商查询接口")
    public ResponseBase queryModelFirmTree() {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.queryModelFirmTree();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("queryModelFirmTree{}", e);
            return setResultFail("厂商查询接口失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/firm/delete")
    @ResponseBody
    @ApiOperation(value = "厂商删除接口")
    public ResponseBase deleteModelFirm(@RequestBody AddAndUpdateModelFirmParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.deleteModelFirm(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("deleteModelFirm{}", e);
            return setResultFail("厂商删除接口失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/specification/create")
    @ResponseBody
    @ApiOperation(value = "厂商规格型号新增")
    public ResponseBase addBrandSpecification(@RequestBody AddAndUpdateModelSpecificationParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.addBrandSpecification(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("addBrandSpecification{}", e);
            return setResultFail("厂商规格型号新增失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/specification/checkByName")
    @ResponseBody
    @ApiOperation(value = "厂商规格型号重复性查询")
    public ResponseBase checkSpecification(@RequestBody AddAndUpdateModelSpecificationParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.checkSpecification(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("checkSpecification{}", e);
            return setResultFail("厂商规格型号重复性查询失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/specification/editor")
    @ResponseBody
    @ApiOperation(value = "厂商规格型号修改")
    public ResponseBase updateBrandSpecification(@RequestBody AddAndUpdateModelSpecificationParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.updateBrandSpecification(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("updateBrandSpecification{}", e);
            return setResultFail("厂商规格型号修改失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/specification/delete")
    @ResponseBody
    @ApiOperation(value = "厂商规格型号删除")
    public ResponseBase deleteBrandSpecification(@RequestBody AddAndUpdateModelSpecificationParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.deleteBrandSpecification(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("deleteBrandSpecification{}", e);
            return setResultFail("厂商规格型号删除失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/specification/browse")
    @ResponseBody
    @ApiOperation(value = "厂商规格型号查询")
    public ResponseBase queryBrandSpecification(@RequestBody AddAndUpdateModelSpecificationParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.queryBrandSpecification(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("queryBrandSpecification{}", e);
            return setResultFail("厂商规格型号查询失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/specification/browseByBrand")
    @ResponseBody
    @ApiOperation(value = "根据厂商查询规格型号")
    public ResponseBase querySpecificationByBrand(@RequestBody AddAndUpdateModelSpecificationParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.querySpecificationByBrand(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("querySpecificationByBrand{}", e);
            return setResultFail("根据厂商查询规格型号失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/mac/browse")
    @ResponseBody
    @ApiOperation(value = "MAC特性List查询")
    public ResponseBase queryMACInfoList(@RequestBody AddAndUpdateModelMACParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.queryMACInfoList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("queryMACInfoList{}", e);
            return setResultFail("MAC特性List查询失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/mac/create")
    @ResponseBody
    @ApiOperation(value = "MAC特性信息新增")
    public ResponseBase addMACInfo(@RequestBody AddAndUpdateModelMACParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.addMACInfo(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("addMACInfo{}", e);
            return setResultFail("MAC特性信息新增失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/mac/editor")
    @ResponseBody
    @ApiOperation(value = "MAC特性信息修改")
    public ResponseBase editorMACInfo(@RequestBody AddAndUpdateModelMACParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.editorMACInfo(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("editorMACInfo{}", e);
            return setResultFail("MAC特性信息修改失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/mac/delete")
    @ResponseBody
    @ApiOperation(value = "删除Mac特性数据")
    public ResponseBase deleteMACInfo(@RequestBody AddAndUpdateModelMACParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.deleteMACInfo(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("deleteMACInfo{}", e);
            return setResultFail("删除Mac特性数据失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/mac/check")
    @ResponseBody
    @ApiOperation(value = "mac特性数据校验")
    public ResponseBase checkMACInfo(@RequestBody AddAndUpdateModelMACParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.checkMACInfo(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("checkMACInfo{}", e);
            return setResultFail("mac特性数据校验失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/mac/getMacVendor")
    @ResponseBody
    @ApiOperation(value = "根据厂商简称查询所有全称")
    public ResponseBase getMacVendorByShortName(@RequestBody AddAndUpdateModelMACParam qParam) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.getMacVendorByShortName(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("getMacVendorByShortName{}", e);
            return setResultFail("根据厂商简称查询所有全称失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/firm/image-upload")
    @ResponseBody
    @ApiOperation(value = "厂商图片上传")
    public ResponseBase imageUpload(@RequestParam("file") MultipartFile multipartFile) {
        Reply reply = new Reply();
        try {
            reply = mwModelVendorService.imageUpload(multipartFile);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("imageUpload{}", e);
            return setResultFail("厂商图片上传失败","");
        }
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/specification/fuzzSearch")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledBySpecification() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelVendorService.fuzzSearchAllFiledBySpecification();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("fuzzSearchAllFiledBySpecification{}",e);
            return setResultFail("模糊查询所有字段资数据失败", "模糊查询所有字段资数据失败");
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "model_manage")
    @PostMapping("/mac/fuzzSearch")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledByMAC() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwModelVendorService.fuzzSearchAllFiledByMAC();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("fuzzSearchAllFiledByMAC{}",e);
            return setResultFail("模糊查询所有字段资数据失败", "模糊查询所有字段资数据失败");
        }

        return setResultSuccess(reply);
    }

}
