package cn.mw.monitor.model.service;

import cn.mw.monitor.model.param.AddAndUpdateModelFirmParam;
import cn.mw.monitor.model.param.AddAndUpdateModelMACParam;
import cn.mw.monitor.model.param.AddAndUpdateModelSpecificationParam;
import cn.mwpaas.common.model.Reply;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author qzg
 * @date 2022/4/28
 */
public interface MWModelVendorService {
    Reply modelFirmAdd(AddAndUpdateModelFirmParam param);

    Reply updateModelFirm(AddAndUpdateModelFirmParam param);

    Reply checkModelFirmByName(AddAndUpdateModelFirmParam param);

    Reply queryModelFirmTree();

    Reply deleteModelFirm(AddAndUpdateModelFirmParam param);

    Reply addBrandSpecification(AddAndUpdateModelSpecificationParam qParam);

    Reply updateBrandSpecification(AddAndUpdateModelSpecificationParam qParam);

    Reply deleteBrandSpecification(AddAndUpdateModelSpecificationParam qParam);

    Reply queryBrandSpecification(AddAndUpdateModelSpecificationParam qParam);

    Reply querySpecificationByBrand(AddAndUpdateModelSpecificationParam qParam);

    Reply checkSpecification(AddAndUpdateModelSpecificationParam qParam);

    Reply queryMACInfoList(AddAndUpdateModelMACParam qParam);

    Reply addMACInfo(AddAndUpdateModelMACParam qParam);

    Reply editorMACInfo(AddAndUpdateModelMACParam qParam);

    Reply deleteMACInfo(AddAndUpdateModelMACParam qParam);

    Reply checkMACInfo(AddAndUpdateModelMACParam qParam);

    Reply getMacVendorByShortName(AddAndUpdateModelMACParam qParam);

    Reply imageUpload(MultipartFile multipartFile);

    Reply fuzzSearchAllFiledBySpecification();

    Reply fuzzSearchAllFiledByMAC();
}
