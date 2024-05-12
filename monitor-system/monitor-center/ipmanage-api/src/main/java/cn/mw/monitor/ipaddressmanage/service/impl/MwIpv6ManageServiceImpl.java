package cn.mw.monitor.ipaddressmanage.service.impl;

import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.ipaddressmanage.dao.MwIpv6ManageListDao;
import cn.mw.monitor.ipaddressmanage.param.*;
import cn.mw.monitor.ipaddressmanage.param.QueryIpAddressManageListParam;
import cn.mw.monitor.ipaddressmanage.util.ConverParam;
import cn.mw.monitor.ipaddressmanage.util.IPv6Judge;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.ipaddressmanage.dao.MwIpv6ManageTableDao;
import cn.mw.monitor.ipaddressmanage.paramv6.AddUpdateIpv6ManageParam;
import cn.mw.monitor.ipaddressmanage.paramv6.Ipv6ManageTable1Param;
import cn.mw.monitor.ipaddressmanage.paramv6.Ipv6ManageTableParam;
import cn.mw.monitor.ipaddressmanage.paramv6.QueryIpv6ManageParam;
import cn.mw.monitor.ipaddressmanage.service.MwIpv6ManageService;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.googlecode.ipv6.IPv6Network;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.mwpaas.common.model.Reply;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

@Service
@Slf4j
@Transactional
public class MwIpv6ManageServiceImpl implements MwIpv6ManageService {

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWCommonService mwCommonService;

    @Resource
    MwIpv6ManageTableDao mwIpv6ManageTableDao;

    @Resource
    MwIpv6ManageListDao mwIpv6ManageListDao;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    private static final Logger dbLogger = LoggerFactory.getLogger("MWDBLogger");


    @Override
    public Reply editorSelect(QueryIpv6ManageParam qParam) {
        Ipv6ManageTable1Param s = mwIpv6ManageTableDao.selectIpv6ById(qParam);

        //将用户，用户组，机构处理数组形式返回给前端
        List<String> orgidlist1 = mwIpv6ManageTableDao.selectOrg2(s.getOrgIdss());
        for (String o:orgidlist1){
            List<Integer> orgidItem = new ArrayList<>();
            String[] strs = o.split(",");
            for (String str : strs) {
                if(!str.trim().equals("")){
                    orgidItem.add(Integer.parseInt(str.trim()));
                }
            }
            s.getOrgIds().add(orgidItem);
        }
        return Reply.ok(s);
    }

    @Override
    public Reply selectList(QueryIpAddressManageParam qParam) throws Exception{
        List<Ipv6ManageTableParam> list = new ArrayList<>();

        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        String perm = iLoginCacheInfo.getRoleInfo().getDataPerm();
        DataPermission dataPermission = DataPermission.valueOf(perm);
        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            qParam.setGroupIds(groupIds);
        }
        switch (dataPermission) {
            case PRIVATE:
                qParam.setUserId(userId);
                if(qParam.getPageNumber()==null){
                }else{
                    PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                }
                Map priCriteria = PropertyUtils.describe(qParam);
                list = mwIpv6ManageTableDao.selectPriIpAddress(priCriteria);
                for (Ipv6ManageTableParam val:
                        list) {
                    Ipv6ManageTableParam va2=new Ipv6ManageTableParam();
                    queryAddress(val,va2);
                    val.setLongitude(va2.getLongitude());
                    val.setLatitude(va2.getLatitude());
                }
                break;
            case PUBLIC:
                List<String> orgNames = mwOrgCommonService.getOrgNamesByNodes(loginName);

                List<Integer> orgIds = new ArrayList<>();
                Boolean isAdmin = false;
                for (String orgName : orgNames) {
                    if (orgName.equals(MWUtils.ORG_NAME_TOP)) {
                        isAdmin = true;
                        break;
                    }
                }
                if (!isAdmin) {
                    orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);
                }
                if (null != orgIds && orgIds.size() > 0) {
                    qParam.setOrgIds(orgIds);
                }
                qParam.setIsAdmin(isAdmin);
                if(qParam.getPageNumber()==null){
                }else{
                    PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                }
                Map priCriteria1 = PropertyUtils.describe(qParam);
                list = mwIpv6ManageTableDao.selectPubIpAddress(priCriteria1);
                for (Ipv6ManageTableParam val:
                        list) {
                    Ipv6ManageTableParam va2=new Ipv6ManageTableParam();
                    queryAddress(val,va2);
                    val.setLongitude(va2.getLongitude());
                    val.setLatitude(va2.getLatitude());
                }
                break;
        }


        for (Ipv6ManageTableParam var:list
        ) {
            var.setIPv4(false);
            if("grouping".equals(var.getType())){
                int i = mwIpv6ManageTableDao.checkIsLeaf(var.getId());
                if(i==0){
                    var.setLeaf(true);
                }
            }

        }


        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);
        return Reply.ok(pageInfo);
        //return null;
    }

    public void queryAddress(Ipv6ManageTableParam val,Ipv6ManageTableParam va2){
        //IpAddressManageTableParam val3=new IpAddressManageTableParam();
        if(val!=null) {
            if (val.getLatitude() == null || val.getLongitude() == null) {
                Ipv6ManageTableParam table1Param = mwIpv6ManageTableDao.selectPictureIpv6ById1(val.getParentId());
                queryAddress(table1Param, va2);
            } else {
                va2.setLatitude(val.getLatitude());
                va2.setLongitude(val.getLongitude());
            }
        }

    }

    @Override
    public Reply delete(AddUpdateIpv6ManageParam auParam) throws Exception {
        if(auParam.getIds()!=null && auParam.getIds().size()>0){
            List<Ipv6ManageTableParam> subResult = mwIpv6ManageTableDao.selectListByIds(auParam.getIds());
            Integer batUpdate = mwIpv6ManageTableDao.selectListByIdsHaveOper(auParam.getIds());
            if (batUpdate>0){
                return Reply.fail("地址已分配，请先回收地址");
            }
            if(subResult!=null&&subResult.size()>0){
                for (Ipv6ManageTableParam node : subResult) {
                    deleteCommon(node);
                }
            }
        }else{
            Ipv6ManageTableParam param=new Ipv6ManageTableParam();
            BeanUtils.copyProperties(auParam,param);
            deleteCommon(param);
        }



        return Reply.ok("删除成功");
    }

    public void deleteCommon(Ipv6ManageTableParam auParam) throws Exception {
        List<Ipv6ManageTableParam> subResult = mwIpv6ManageTableDao.selectSubIpAddress(auParam.getId());
        if(subResult!=null&&subResult.size()>0){
            for (Ipv6ManageTableParam node : subResult) {
                if("grouping".equals(node.getType())) {
                    deleteRecursive(node);
                }else {
                    mwIpv6ManageTableDao.deleteList(node.getId());
                    //删除 右侧旧table页子表数据 TODO
                    mwIpv6ManageListDao.deleteHisByLinkId(node.getId());
                }
            }
        }

        //删除ip地址管理的 负责人，用户组，机构 权限关系
        deleteMapperAndPerm(auParam);

        //需要删除的 可能是包结构或者ip地址段结构
        if(auParam.getType().equals("iPaddresses")){
            mwIpv6ManageTableDao.deleteList(auParam.getId());
            //删除 右侧旧table页子表数据 TODO
            mwIpv6ManageListDao.deleteHisByLinkId(auParam.getId());
        }


        //删除ip地址管理
        mwIpv6ManageTableDao.delete(auParam.getId());
    }

    public void deleteRecursive(Ipv6ManageTableParam auParam) throws Exception {
        //将此节点下的所有数据关联数据
        List<Ipv6ManageTableParam> subResult = mwIpv6ManageTableDao.selectSubIpAddress(auParam.getId());
        if(subResult!=null&&subResult.size()>0){
            for (Ipv6ManageTableParam node : subResult) {
                if("grouping".equals(node.getType())){
                    deleteRecursive(node);
                }else {
                    //删除ipv6地址管理的 负责人，用户组，机构 权限关系
                    deleteMapperAndPerm(auParam);

                    //删除 右侧旧table页子表数据历史数据 TODO

                    mwIpv6ManageTableDao.deleteList(node.getId());
                    mwIpv6ManageListDao.deleteHisByLinkId(node.getId());

                    //删除 右侧旧table页子表数据 TODO

                    //删除ip地址管理
                    mwIpv6ManageTableDao.delete(node.getId());
                }
            }
        }

        //删除ip地址管理的 负责人，用户组，机构 权限关系  能到这一步的都是包结构
        deleteMapperAndPerm(auParam);


        //删除ip地址管理
        mwIpv6ManageTableDao.delete(auParam.getId());

    }

    @Override
    public Reply update(AddUpdateIpv6ManageParam auParam) throws Exception {

        //修改ip地址管理 主信息
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        auParam.setModificationDate(new Date());
        mwIpv6ManageTableDao.update(auParam);

        //修改ip地址管理的 负责人，用户组，机构
        deleteMapperAndPerm(auParam);
        addMapperAndPerm(auParam);
        SystemLogDTO sbuild = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("IP地址管理")
                .objName(auParam.getIpAddresses()).operateDes("修改IPV6编辑").build();
        dbLogger.info(JSON.toJSONString(sbuild));
        return Reply.ok(auParam);
    }

    @Override
    public Reply insert(AddUpdateIpv6ManageParam auParam) throws Exception {
        //若本次添加的数据名称字段为空，就取ip地址段的值作为名称
        if(auParam.getLabel() == null || auParam.getLabel().equals("")){
            if(auParam.getIpAddresses()!=null&&!auParam.getIpAddresses().equals("")){
                auParam.setLabel(auParam.getIpAddresses());
            }
        }
        if(auParam.getIpAddresses()!=null&&!auParam.getIpAddresses().equals("")&&"iPaddresses".equalsIgnoreCase(auParam.getType())){
            String ipAddresses = auParam.getIpAddresses();
            try {

                //判断ipv6格式
                IPv6Network.fromString(ipAddresses);
                //对ipv6地址段去重
                List<String> valList = new ArrayList<>();
                valList.add(ipAddresses);
                List<String> resultIp = ipRemove(valList);
                if(resultIp.size()==0){
                    return Reply.fail("ip地址段已存在，请选择其他地址段");
                }
                List<String> ipRand = IPv6Judge.getInstance().Ipv6toBigInteger(auParam.getIpAddresses());
                List<Ipv6ManageTable1Param>  count =  mwIpv6ManageTableDao.countIPv6(ipRand.get(0),ipRand.get(1),auParam.getSignId());
                Integer max = count.size();
                Integer kill = count.size();
                for (Ipv6ManageTable1Param s:count) {
                    BigInteger  Min= new BigInteger(s.getIpRandStart());
                    BigInteger Max = new BigInteger(s.getIpRandEnd());
                    BigInteger  ipMin= new BigInteger(ipRand.get(0));
                    BigInteger ipMax = new BigInteger(ipRand.get(1));
                    if (ipMin.compareTo(Min)==1&&ipMin.compareTo(Max)==-1){
                        max--;
                    }
                    if (ipMax.compareTo(Min)==1&&ipMax.compareTo(Max)==-1){
                        max--;
                    }
                }
                if (max!=kill){
                    return Reply.fail("地址存在重复地址段");
                }
                insert1(auParam);
            }catch (Exception e){
                return Reply.fail("IPV6格式错误");
            }
        }else{
            //分组
            insert1(auParam);
        }
        SystemLogDTO sbuild = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("IP地址管理")
                .objName(auParam.getIpAddresses()).operateDes("新增IPV6").build();
        return Reply.ok(auParam);
    }

    @Override
    public Reply selectSonList(QueryIpAddressManageListParam qParam) throws Exception {
        PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
        if (qParam.getLinkId()<0){
            qParam.setLinkId(-qParam.getLinkId());
            qParam.setIsTem(1);
        }
        Map priCriteria = PropertyUtils.describe(qParam);
        if(qParam.getOrderName()!=null&&qParam.getOrderType()!=null){
            String s = ConverParam.HumpToLine(qParam.getOrderName());
            priCriteria.put("orderName",s);
        }
        List<AddUpdateIpAddressManageListParam> list = mwIpv6ManageListDao.selectSonList(priCriteria);
        for (AddUpdateIpAddressManageListParam q:list) {
            if (q.getAssetsDetail().size()>0){
                q.setAssetsTypeInOrOut(q.getAssetsDetail().get(0).getAssetsType());
            }
        }
        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply selectPicture(QueryIpv6ManageParam qParam) throws Exception {
        Ipv6ManageTableParam data1 = mwIpv6ManageTableDao.selectPictureIpv6ById1(qParam.getId());
        List<Integer> pictureValues = mwIpv6ManageTableDao.selectPicture(qParam.getId());
        IpAddressManagePictureParam data = new IpAddressManagePictureParam();
        if (data1 != null) {
            data.setId(data1.getId());
            data.setParentId(data1.getParentId());
            data.setLabel(data1.getLabel());
            data.setType(data1.getLabel());
            data.setLeaf(data1.isLeaf());
            data.setDescri(data1.getDescri());
            data.setMask(data1.getMask());
            data.setIpAddresses(data1.getIpAddresses());
            data.setPrincipal(data1.getPrincipal());
            data.setGroupIds(data1.getGroupIds());
            data.setOrgIds(data1.getOrgIds());
        }
        HashMap a= new HashMap<String, Integer>();
        a.put("key", "已使用");
        a.put("value", pictureValues.get(1));
        HashMap b= new HashMap<String, Integer>();
        b.put("key","未使用");
        b.put("value", pictureValues.get(0));
        HashMap c= new HashMap<String, Integer>();
        c.put("key", "预留");
        c.put("value", pictureValues.get(2));
        data.getPicture1().add(a);
        data.getPicture1().add(b);
        data.getPicture1().add(c);
        HashMap d= new HashMap<String, Integer>();
        d.put("key", "在线");
        d.put("value", pictureValues.get(3));
        HashMap e= new HashMap<String, Integer>();
        e.put("key", "离线");
        e.put("value", pictureValues.get(4));
        data.getPicture2().add(d);
        data.getPicture2().add(e);
        HashMap f= new HashMap<String, Integer>();
        f.put("key", "已知");
        f.put("value", pictureValues.get(5));
        HashMap g= new HashMap<String, Integer>();
        g.put("key", "未知");
        g.put("value", pictureValues.get(6));
        data.getPicture3().add(f);
        data.getPicture3().add(g);
        HashMap h= new HashMap<String, Integer>();
        h.put("key", "未分配");
        h.put("value", pictureValues.get(7));
        HashMap i= new HashMap<String, Integer>();
        i.put("key", "已分配");
        i.put("value", pictureValues.get(8));
        data.getPicture4().add(h);
        data.getPicture4().add(i);
        return Reply.ok(data);
    }

    @Override
    public void batchExport(ExportIpAddressListParam uParam, HttpServletResponse response) throws IOException {
        ExcelWriter excelWriter = null;
        try {
            //需要导出的数据
            List<MwIpAddressManageListTable> s = null;
            if (uParam.getLinkId() != null) {
                Integer linkId = uParam.getLinkId();
                s = mwIpv6ManageListDao.selectSonList1(linkId);
            } else if (uParam.getIds() != null) {
                List<Integer> ids = uParam.getIds();
                s = mwIpv6ManageListDao.selectSonList2(ids);
            }

            //将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
            //MwIpAddressManageServiceImpl mwIpAddressManageService = new MwIpAddressManageServiceImpl();
            List<List<MwIpAddressManageListTable>> li = getSubLists(s, 50000);

            //初始化导出字段
            Set<String> includeColumnFiledNames = new HashSet<>();
            System.err.println(uParam.getFields());
            if (uParam.getFields() != null && uParam.getFields().size()>0) {
                includeColumnFiledNames = uParam.getFields();
            } else {
                includeColumnFiledNames.add("ipAddress");
                includeColumnFiledNames.add("ipState");
                includeColumnFiledNames.add("online");
                includeColumnFiledNames.add("mac");
                includeColumnFiledNames.add("vendor");
                //includeColumnFiledNames.add("accessPort");
                includeColumnFiledNames.add("accessPortName");
                includeColumnFiledNames.add("accessEquip");
                includeColumnFiledNames.add("assetsName");
                includeColumnFiledNames.add("assetsType");
                includeColumnFiledNames.add("updateDate");
            }

            //设置回复头一些信息
            String fileName = null; //导出文件名
            if (uParam.getName() != null && !uParam.getName().equals("")) {
                fileName = uParam.getName();
            } else {
                fileName = System.currentTimeMillis() + "";
            }
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            System.err.println("fileName: "+fileName);

            //创建easyExcel写出对象
            excelWriter = EasyExcel.write(
                    response.getOutputStream(), MwIpAddressManageListTable.class).build();

            //计算sheet分页
            Integer sheetNum = s.size() % 50000 == 0 ? s.size() / 50000 : s.size() / 50000 + 1;
            for (int i = 0; i < sheetNum; i++) {
                WriteSheet sheet = EasyExcel.writerSheet(i, "sheet" + i)
                        .includeColumnFiledNames(includeColumnFiledNames)
                        .build();
                excelWriter.write(li.get(i), sheet);
            }
            System.err.println("导出成功");
        }catch(Exception e){
            log.error("错误返回 :{}",e);
        }finally {
            if(excelWriter!=null){
                excelWriter.finish();
            }
        }
    }

    public List<List<MwIpAddressManageListTable>> getSubLists(List<MwIpAddressManageListTable> allData, int size) {
        List<List<MwIpAddressManageListTable>> result = new ArrayList();
        for (int begin = 0; begin < allData.size(); begin = begin + size) {
            int end = (begin + size > allData.size() ? allData.size() : begin + size);
            result.add(allData.subList(begin, end));
        }
        return result;
    }



    @Override
    public Reply batchDelete(AddUpdateIpAddressManageListParam uParam) {
        mwIpv6ManageListDao.deleteBatch(uParam);
        List<Integer> ids = uParam.getIds();
        for (Integer var:ids
        ) {
            mwIpv6ManageListDao.deleteHisById(var);
        }
        //历史信息没有删除
        return Reply.ok();
    }

    @Override
    public Reply batchUpdate(AddUpdateIpAddressManageListParam uParam) {
        mwIpv6ManageListDao.updateBatch(uParam);
        return Reply.ok();
    }

    @Override
    public Reply getHisList(AddUpdateIpAddressManageListParam parm) throws Exception {
        PageHelper.startPage(parm.getPageNumber(), parm.getPageSize());
        Map priCriteria = PropertyUtils.describe(parm);
        List<AddUpdateIpAddressManageListHisParam> list = mwIpv6ManageListDao.getHisList(priCriteria);
        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply ipv6ListaddList(AddUpdateIpAddressManageListParam uParam) {
        uParam.setCreateDate(new Date());
        uParam.setModificationDate(new Date());
        uParam.setCreator(iLoginCacheInfo.getLoginName());
        uParam.setModifier(iLoginCacheInfo.getLoginName());
        mwIpv6ManageListDao.insertIpv6(uParam);
        return Reply.ok("成功");
    }

    public List<String> ipRemove(List<String> listIp1){
        List<String> resultIp = new ArrayList<>();
        List<String> allIpAddresses = mwIpv6ManageTableDao.selectAllIpaddresses();
        resultIp.addAll(listIp1);
        resultIp.removeAll(allIpAddresses);
        return resultIp;
    }


    public void insert1(AddUpdateIpv6ManageParam auParam) throws Exception {
        conver(auParam);
        List<String> ipRand = IPv6Judge.getInstance().Ipv6toBigInteger(auParam.getIpAddresses());
        //添加ipv6地址管理 主信息
        auParam.setCreator(iLoginCacheInfo.getLoginName());
        auParam.setCreateDate(new Date());
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        auParam.setModificationDate(new Date());
        if (ipRand.size()==2){
            auParam.setIpRandStart(ipRand.get(0));
            auParam.setIpRandEnd(ipRand.get(1));
        }
        mwIpv6ManageTableDao.insert(auParam);

        //添加用户，用户组，机构权限
        addMapperAndPerm(auParam);
    }

    private void conver(AddUpdateIpv6ManageParam auParam){
        //设置是否为叶子节点
        if(auParam.getType().equals("iPaddresses")){
            auParam.setLeaf(1);
        }else{
            auParam.setLeaf(0);
        }

        //ipv6没有子网掩码概念 故不设置
    }

    /**
     * 删除负责人，用户组，机构 权限关系
     * @param auParam
     */
    private void deleteMapperAndPerm(AddUpdateIpv6ManageParam auParam) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeId(String.valueOf(auParam.getId()))
                .type(DataType.IPV6.getName())
                .build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    private void deleteMapperAndPerm(Ipv6ManageTableParam auParam) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeId(String.valueOf(auParam.getId()))
                .type(DataType.IPV6.getName())
                .build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    /**
     * 添加负责人，用户组，机构 权限关系
     * @param auParam
     */
    private void addMapperAndPerm(AddUpdateIpv6ManageParam auParam) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(auParam.getGroupIds())  //用户组
                .userIds(auParam.getPrincipal())  //责任人
                .orgIds(auParam.getOrgIds())      //机构
                .typeId(String.valueOf(auParam.getId())) //数据主键
                .type(DataType.IPV6.getName())        //IP
                .desc(DataType.IPV6.getDesc()).build(); //IP地址管理
        mwCommonService.addMapperAndPerm(insertDto);
    }

}
