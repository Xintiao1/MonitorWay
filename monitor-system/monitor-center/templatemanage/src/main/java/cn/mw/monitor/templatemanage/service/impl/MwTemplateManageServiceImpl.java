package cn.mw.monitor.templatemanage.service.impl;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.templatemanage.dao.MwTemplateManageDao;
import cn.mw.monitor.templatemanage.entity.*;
import cn.mw.monitor.templatemanage.service.MwTemplateManageService;
import cn.mw.monitor.state.DataType;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional
public class MwTemplateManageServiceImpl implements MwTemplateManageService {

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWCommonService mwCommonService;

    @Resource
    MwTemplateManageDao mwTemplateManageDao;

    @Override
    public Reply specification(ParamEntity brand) throws Exception {
        List<String> results = mwTemplateManageDao.selectSpecification(brand.getBrand());
        return Reply.ok(results);
    }

    @Override
    public Reply brand(ParamEntity specification) throws Exception {
        List<String> results = mwTemplateManageDao.selectBrand(specification.getSpecification());
        return Reply.ok(results);
    }

    @Override
    public Reply selectList1(QueryTemplateManageParam param) {
        MwQueryTemplateManageTable s = mwTemplateManageDao.selectOne(param);

        //将用户，用户组，机构处理数组形式返回给前端

        return Reply.ok(s);
    }

    @Override
    public Reply selectList(QueryTemplateManageParam qParam) {
        try {
            List<MwTemplateManageTable> list = new ArrayList<>();
            PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
            Map pubCriteria = PropertyUtils.describe(qParam);
            list = mwTemplateManageDao.selectList(pubCriteria);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.info("fail to selectList QueryTemplateManageParam:{} cause:{}", qParam, e.getMessage());
            return Reply.fail(500, "模板管理查询失败");
        }
    }

    @Override
    public Reply delete(List<Integer> auParam) throws Exception {
        //删除模板管理
        mwTemplateManageDao.deleteBatch(auParam);

        return Reply.ok("删除成功");
    }

    @Override
    public Reply update(AddTemplateManageParam auParam) throws Exception {
        //校验是否重复
        if (StringUtils.isEmpty(auParam.getTemplate())) {
            return Reply.fail("请填写模板名称");
        }
        QueryTemplateManageParam param = new QueryTemplateManageParam();
        param.setId(auParam.getId());
        MwQueryTemplateManageTable s = mwTemplateManageDao.selectOne(param);
        //修改模板名称再判断
        if (s!= null && !s.getTemplate().equals(auParam.getTemplate())) {
            if (mwTemplateManageDao.countTemplateName(auParam.getTemplate()) >= 1) {
                return Reply.fail("模板名称重复");
            }
        }

        //修改模板管理 主信息
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        auParam.setModificationDate(new Date());
        mwTemplateManageDao.update(auParam);

        return Reply.ok(auParam);
    }

    @Override
    public Reply insert(AddTemplateManageParam auParam) throws Exception {
        //校验是否重复
        if (StringUtils.isEmpty(auParam.getTemplate())) {
            return Reply.fail("请填写模板名称");
        }
        if (mwTemplateManageDao.countTemplateName(auParam.getTemplate()) >= 1) {
            return Reply.fail("模板名称重复");
        }

        //添加模板管理 主信息
        auParam.setCreator(iLoginCacheInfo.getLoginName());
        auParam.setCreateDate(new Date());
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        auParam.setModificationDate(new Date());
        mwTemplateManageDao.insert(auParam);

        return Reply.ok(auParam);
    }

    @Override
    public Reply selectListDropDown() {
        try {
            List<DropDownParam> lists = mwTemplateManageDao.getDropdown();
            return Reply.ok(lists);
        } catch (Exception e) {
            return Reply.fail("下拉框查询", 500);
        }
    }

    /**
     * 添加负责人，用户组，机构 权限关系
     *
     * @param auParam
     */
    private void addMapperAndPerm(AddTemplateManageParam auParam) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(auParam.getGroupIds())  //用户组
                .userIds(auParam.getPrincipal())  //责任人
                .orgIds(auParam.getOrgIds())      //机构
                .typeId(String.valueOf(auParam.getId())) //数据主键
                .type(DataType.TEMPLATE.getName())        //ACCOUNT
                .desc(DataType.TEMPLATE.getDesc()).build(); //账号管理
        mwCommonService.addMapperAndPerm(insertDto);
    }

    /**
     * 删除负责人，用户组，机构 权限关系
     *
     * @param auParam
     */
    private void deleteMapperAndPerm(AddTemplateManageParam auParam) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeId(String.valueOf(auParam.getId()))
                .type(DataType.TEMPLATE.getName())
                .build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }
}
