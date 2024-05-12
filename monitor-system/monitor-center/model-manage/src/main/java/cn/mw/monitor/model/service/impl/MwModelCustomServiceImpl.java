package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.customPage.api.param.QueryCustomPageParam;
import cn.mw.monitor.customPage.dto.MwCustomColDTO;
import cn.mw.monitor.customPage.service.MwCustomcolService;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.dto.ModelType;
import cn.mw.monitor.model.util.ModelUtils;
import cn.mw.monitor.service.model.service.MwModelCustomService;
import cn.mw.monitor.service.model.param.QueryModelCustomPageParam;
import cn.mw.monitor.service.model.param.QueryModelGroupParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceParam;
import cn.mwpaas.common.model.Reply;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2021/2/20 9:04
 */
@Service
public class MwModelCustomServiceImpl implements MwModelCustomService {
    @Autowired
    private MwCustomcolService mwCustomcolService;
    @Resource
    private MwModelManageDao mwModelManageDao;
    @Value("${modelUrl}")
    private String modelUrl;

    @Override
    public Reply selectModelAllCustom(QueryModelCustomPageParam pageParam) {
        List<List<MwCustomColDTO>> lists = new ArrayList<>();
        if (pageParam.getPageId().size() > 0) {
            pageParam.getPageId().forEach(pageId -> {
                QueryCustomPageParam customPageParam = QueryCustomPageParam.builder().userId(pageParam.getUserId()).pageId(pageId).build();
                List<MwCustomColDTO> custom = mwCustomcolService.getCustom(customPageParam);
                lists.add(custom);
            });
        }
//        List<MwCustomColDTO> list = mwModelManageDao.selectModelPropertiesByModelId(param);
//        if (pageParam.getIsAssociaAssets()) {
//            MwCustomColDTO dto = new MwCustomColDTO();
//            dto.setLabel("资产ID");
//            dto.setProp("tangibleId");
//            dto.setId(0);
//            list.add(dto);
//        }

        return Reply.ok(lists);
    }

    @Override
    public Reply selectModelPropertiesAllCustom(QueryModelInstanceParam param) {
//        if (ModelUtils.modelTypeHashMap.get(param.getModelTypeId()).equals(ModelType.SON_MODEL)) {//子模型
//            //查询子模型的父模型id  一个子模型只有一个父模型
//            Integer fatherModelId = mwModelManageDao.getFatherModelIdBySonModelId(param.getModelId());
//            param.setFatherModelId(fatherModelId);
//        }

        List<String> pids = new ArrayList<>();
        if (!Strings.isNullOrEmpty(param.getPids())) {
            String[] str = param.getPids().split(",");
            for (String s : str) {
                if (!"".equals(s)) {
                    pids.add(s);
                }
            }
            param.setPidList(pids);
        }
        List<MwCustomColDTO> list = mwModelManageDao.selectModelPropertiesByModelId(param);

        QueryCustomPageParam customPageParam = QueryCustomPageParam.builder().userId(param.getUserId()).pageId(param.getPageId()).build();
        List<MwCustomColDTO> custom = mwCustomcolService.getCustom(customPageParam);
        custom.forEach(customDto -> {
            list.add(customDto);
        });

        return Reply.ok(list);
    }

    @Override
    public Reply selectModelAllIcon() {
        List<String> list = new ArrayList<>();
        File file = new File(modelUrl);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            list.add(files[i].getName());
        }
        return Reply.ok(list);
    }

    @Override
    public Reply selectModelGroupList(QueryModelGroupParam param) {
        List<Map<String, Object>> list = mwModelManageDao.selectModelGroupList(param);
        return Reply.ok(list);
    }

    @Override
    public Reply selectPropertiesList() {
        List<Map<String, Object>> list = mwModelManageDao.selectPropertiesList();
        return Reply.ok(list);
    }
}
