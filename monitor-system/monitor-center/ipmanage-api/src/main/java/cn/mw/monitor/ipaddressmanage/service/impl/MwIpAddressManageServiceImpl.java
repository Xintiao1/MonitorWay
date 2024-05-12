package cn.mw.monitor.ipaddressmanage.service.impl;


import cn.mw.monitor.api.common.IpV4Util;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.common.constant.AddressType;
import cn.mw.monitor.common.util.PageList;
import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManageListTableDao;
import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManagePowerTableDao;
import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManageTableDao;
import cn.mw.monitor.ipaddressmanage.dto.LinkLabel;
import cn.mw.monitor.ipaddressmanage.dto.MwIpAddresses1DTO;
import cn.mw.monitor.ipaddressmanage.param.*;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManageService;
import cn.mw.monitor.ipaddressmanage.util.ConverParam;
import cn.mw.monitor.ipaddressmanage.util.IPv6Judge;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.common.MWDateConstant;
import cn.mw.monitor.service.ipmanage.IpManageService;
import cn.mw.monitor.service.ipmanage.model.IpManageTree;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.scan.dto.IPInfoDTO;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.dto.UserDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.topology.model.DeviceInfo;
import cn.mw.monitor.user.dao.MWUserDao;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.WeiXinSendUtil;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.DateUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.fastjson.JSON;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.geoip.model.v20200101.DescribeIpv4LocationRequest;
import com.aliyuncs.geoip.model.v20200101.DescribeIpv4LocationResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static cn.mw.monitor.ipaddressmanage.ipenum.Constant.*;

/**
 * bkc
 */
@Service
@Slf4j
@Transactional
public class MwIpAddressManageServiceImpl implements MwIpAddressManageService, IpManageService, Runnable {

    private String accessKeyId1;
    private String accessKeySerect1;
    private AddUpdateIpAddressManageParam param;
    @Autowired
    LicenseManagementService licenseManagement;
    @Resource
    private MWUserDao mwUserDao;
    @Value("${WxNotice.appid}")
    private String appid;
    @Value("${WxNotice.secret}")
    private String secret;
    @Value("${WxNotice.templateid}")
    private String templateid;

    @Autowired
    private MwTangibleAssetsService mwTangibleAssetsService;

    private CountDownLatch latch;

    private ILoginCacheInfo iLoginCacheInfo1;

    private MwIpAddressManageTableDao mwIpAddressManageTableDao1;


    private static final Logger dbLogger = LoggerFactory.getLogger("MWDBLogger");

    private MWCommonService mwCommonService1;

    private MwIpAddressManageServiceImpl impl;


    private MwIpAddressManageListTableDao mwIpAddressManageListTableDao1;

    public MwIpAddressManageServiceImpl() {

    }


    public MwIpAddressManageServiceImpl(AddUpdateIpAddressManageParam param,
                                        CountDownLatch latch, ILoginCacheInfo iLoginCacheInfo1,
                                        MwIpAddressManageTableDao mwIpAddressManageTableDao1,
                                        MwIpAddressManageServiceImpl impl,
                                        MWCommonService mwCommonService1,
                                        MwIpAddressManageListTableDao mwIpAddressManageListTableDao1,
                                        String accessKeyId1,
                                        String accessKeySerect1) {
        this.param = param;
        this.latch = latch;
        this.iLoginCacheInfo1 = iLoginCacheInfo1;
        this.mwIpAddressManageTableDao1 = mwIpAddressManageTableDao1;
        this.impl = impl;
        this.mwCommonService1 = mwCommonService1;
        this.mwIpAddressManageListTableDao1 = mwIpAddressManageListTableDao1;
        this.accessKeyId = accessKeyId1;
        this.accessKeySerect = accessKeySerect1;

    }


    @Resource
    MwIpAddressManageTableDao mwIpAddressManageTableDao;

    @Resource
    MwIpAddressManageListTableDao mwIpAddressManageListTableDao;

    @Resource
    ILoginCacheInfo iLoginCacheInfo;
    @Resource
    MwIpAddressManagePowerTableDao ipAddressManagePowerTableDao;

    @Autowired
    private MWMessageService mwMessageService;

    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Value("${aliyuncs.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyuncs.accessKeySerect}")
    private String accessKeySerect;

    @Value("${basicUrl}")
    private String filePathLoc;

    /**
     * 导出 ip地址管理 table页数据
     */
    @Override
    public void batchExport(ExportIpAddressListParam uParam, HttpServletResponse response) throws IOException {
        ExcelWriter excelWriter = null;
        try {
            //需要导出的数据
            List<MwIpAddressManageListTable> s = null;
            if (uParam.getLinkId() != null) {
                Integer linkId = uParam.getLinkId();
                s = mwIpAddressManageListTableDao.selectSonList1(linkId);
            } else if (uParam.getIds() != null) {
                List<Integer> ids = uParam.getIds();
                s = mwIpAddressManageListTableDao.selectSonList2(ids);
            }
            Map<Integer, MwIpAddressManageListTable> idMap = new HashMap<>();
            List<Integer> ids = new ArrayList<>();
            for (MwIpAddressManageListTable  q:s) {
                if (q.getAccessPortName()!=null){
                    if(q.getAccessPortName().equals(IPInfoDTO.MORE_DATA)) {
                        idMap.put(q.getId(), q);
                        ids.add(q.getId());
                    }
                }
            }
            if(ids.size() > 0) {
                List<AddUpdatePortInfoParam> portInfoParams = mwIpAddressManageListTableDao.selectPortInfos(ids);
                for (AddUpdatePortInfoParam portInfoParam : portInfoParams) {
                    MwIpAddressManageListTable ipInfo = idMap.get(portInfoParam.getIpManageListId());
                    String PortName = ipInfo.getAccessPortName();
                    String AccessEquip = ipInfo.getAccessEquip();
                    if (ipInfo.getAccessPortName().contains(IPInfoDTO.MORE_DATA)) {
                        //有多个上联设备时,接入设备及端口显示设置
                        ipInfo.setAccessPortName(portInfoParam.getAccessPortName());
                        ipInfo.setAccessEquip( portInfoParam.getAccessEquip());
                    }else{
                        ipInfo.setAccessPortName(PortName + "/" + portInfoParam.getAccessPortName());
                        ipInfo.setAccessEquip(AccessEquip + "/" + portInfoParam.getAccessEquip());
                    }
                }
            }

            //将需要导出的数据分为50000一组(一个sheet最多只能放入65000左右条数据)
            List<List<MwIpAddressManageListTable>> li = getSubLists(s, 50000);

            //初始化导出字段
            Set<String> includeColumnFiledNames = new HashSet<>();
            System.err.println(uParam.getFields());
            if (uParam.getFields() != null && uParam.getFields().size() > 0) {
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
                includeColumnFiledNames.add("remarks");
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
            System.err.println("fileName: " + fileName);

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
        } catch (Exception e) {
            log.error("错误返回 :{}",e);
        } finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }

    }

    //将list集合数据按照指定大小分成好几个小的list
    public List<List<MwIpAddressManageListTable>> getSubLists(List<MwIpAddressManageListTable> allData, int size) {
        List<List<MwIpAddressManageListTable>> result = new ArrayList();
        for (int begin = 0; begin < allData.size(); begin = begin + size) {
            int end = (begin + size > allData.size() ? allData.size() : begin + size);
            result.add(allData.subList(begin, end));
        }
        return result;
    }

    /**
     * 批量删除 ip地址管理 table页数据
     */
    @Override
    public Reply batchDelete(AddUpdateIpAddressManageListParam uParam) {
        Integer count = mwIpAddressManageListTableDao.selectIpMangeHaveDistribution(uParam.getIds());
        if (count>0){
            return Reply.fail("地址段已分配，请先回收地址");
        }
        List<String> ipaddresses = ipAddressManagePowerTableDao.selectIPaddressByIds(uParam.getIds(),0);

        createIpDelete(ipaddresses);
        mwIpAddressManageListTableDao.deleteBatch(uParam);

        //删除对应的历史数据
        mwIpAddressManageListTableDao.deleteBatchList(uParam);

        Set<Integer> ids = new HashSet<>();
        ids.add(uParam.getLinkId());
        recursiveIp(ids);
        return Reply.ok("删除成功");
    }

    /**
     * 批量修改 ip地址管理 table页数据
     */
    @Override
    public Reply batchUpdate(AddUpdateIpAddressManageListParam uParam) {

        List<AddIpaddresStatusHis> addIpaddresStatusHiss = new ArrayList<>();
        List<AddUpdateIpAddressManageListParam> addUpdateIpAddressManageListParams = mwIpAddressManageListTableDao.getIPaddresssByIds(uParam.getIds());
        for (AddUpdateIpAddressManageListParam addUpdateIpAddressManageListParam:addUpdateIpAddressManageListParams) {
            AddIpaddresStatusHis addIpaddresStatusHis = new AddIpaddresStatusHis();
            addIpaddresStatusHis.setMac(uParam.getMac());
            addIpaddresStatusHis.setRemarks(uParam.getRemarks());
            addIpaddresStatusHis.setAssetsName(uParam.getAssetsName());
            addIpaddresStatusHis.setOldMac(addUpdateIpAddressManageListParam.getMac());
            addIpaddresStatusHis.setOldRemarks(addUpdateIpAddressManageListParam.getRemarks());
            addIpaddresStatusHis.setOldAssetsName(addUpdateIpAddressManageListParam.getAssetsName());
            addIpaddresStatusHis.setIpAddress(addUpdateIpAddressManageListParam.getIpAddress());
            addIpaddresStatusHis.setIsTem(uParam.isTem()==true?1:0);
            addIpaddresStatusHis.setOldIsTem(addUpdateIpAddressManageListParam.isTem()==true?1:0);
            addIpaddresStatusHis.setOldUseStatus(addUpdateIpAddressManageListParam.getIpState());
            addIpaddresStatusHis.setUseStatus(uParam.getIpState());
            addIpaddresStatusHis.setCreateDate(new Date());
            addIpaddresStatusHis.setCreator(iLoginCacheInfo.getLoginName());
            addIpaddresStatusHiss.add(addIpaddresStatusHis);
        }
        mwIpAddressManageListTableDao.insertIpaddresStatus(addIpaddresStatusHiss);

        mwIpAddressManageListTableDao.updateBatch1(uParam);
        //修改
        Set<Integer> ids = new HashSet<>();
        ids.add(uParam.getLinkId());
        recursiveIp(ids);
        return Reply.ok(uParam);
    }

    //初始化页子节点数量，递归的开始
    private void recursiveIp(Set<Integer> ids) {
        List<IpAddressManageTableParam> updateList = new ArrayList<>();
        for (Integer id : ids) {
            int online = 0;//在线数量
            int offline = 0;//离线数据
            int useCount = 0;//使用数量
            int notUserCount = 0;//未使用数量
            int reservedCount = 0;//预留数量
            List<AddUpdateIpAddressManageListParam> list = mwIpAddressManageListTableDao.selectListByLinkId(id);
            for (AddUpdateIpAddressManageListParam parm : list) {
                int isOnline = parm.getOnline() == null || parm.getOnline().equals("") ? 0 : parm.getOnline();
                if (isOnline == 0) {
                    offline++;
                } else if (isOnline == 1) {
                    online++;
                }
                int ipState = parm.getIpState() == null || parm.getIpState().equals("") ? 0 : parm.getIpState();
                if (ipState == 0) {
                    notUserCount++;
                } else if (ipState == 1) {
                    useCount++;
                } else if (ipState == 2) {
                    reservedCount++;
                }
            }
            IpAddressManageTableParam data = IpAddressManageTableParam.builder().id(id)
                    .offline(offline).online(online)
                    .useCount(useCount).notuseCount(notUserCount)
                    .reservedCount(reservedCount).build();
            updateList.add(data);
        }
        mwIpAddressManageListTableDao.updateBatch(updateList);
        Set<Integer> parentIds = mwIpAddressManageListTableDao.getIdsByIds(ids);
        if (parentIds != null && parentIds.size() > 0) {
            recursive(parentIds);
        }
    }

    private void recursive(Set<Integer> ids) {
        List<IpAddressManageTableParam> updateList = new ArrayList<>();
        for (Integer id : ids) {
            int online = 0;//在线数量
            int offline = 0;//离线数据
            int useCount = 0;//使用数量
            int notUserCount = 0;//未使用数量
            int reservedCount = 0;//预留数量
            List<IpAddressManageTableParam> list = mwIpAddressManageTableDao.selectListById(id);
            for (IpAddressManageTableParam parm : list) {
                online += parm.getOnline();
                offline += parm.getOffline();
                useCount += parm.getUseCount();
                notUserCount += parm.getNotuseCount();
                reservedCount += parm.getReservedCount();
            }
            IpAddressManageTableParam data = IpAddressManageTableParam.builder().id(id)
                    .offline(offline).online(online)
                    .useCount(useCount).notuseCount(notUserCount)
                    .reservedCount(reservedCount).build();
            updateList.add(data);
        }
        mwIpAddressManageListTableDao.updateBatch(updateList);
        Set<Integer> parentIds = mwIpAddressManageListTableDao.getIdsByIds(ids);
        if (parentIds != null && parentIds.size() > 0) {
            recursive(parentIds);
        }
    }

    /**
     * 查询ip地址管理 图形数据
     */
    @Override
    public Reply selectPicture(IpAddressManageTableParam aParam) {
        aParam.setId(aParam.getId()<0?-aParam.getId():aParam.getId());
        IpAddressManageTableParam data1 = mwIpAddressManageTableDao.selectIpAddressById1(aParam);
        List<Integer> pictureValues = mwIpAddressManageTableDao.selectPicture(aParam.getId());
        IpAddressManagePictureParam data = new IpAddressManagePictureParam();
        data.setId(-data1.getId());
        data.setParentId(data1.getParentId());
        data.setLabel(data1.getLabel());
        data.setType("tgrouping");
        data.setLeaf(data1.isLeaf());
        data.setDescri(data1.getDescri());
        data.setMask(data1.getMask());
        data.setAddressDesc(data1.getAddressDesc());
        data.setCity(data1.getCity());
        data.setCountry(data1.getCountry());
        data.setRegion(data1.getRegion());
        data.setState(data1.getState());
        HashMap a = new HashMap<String, Integer>();
        a.put("key", "已使用");
        a.put("value", pictureValues.get(1));
        HashMap b = new HashMap<String, Integer>();
        b.put("key", "未使用");
        b.put("value", pictureValues.get(0));
        HashMap c = new HashMap<String, Integer>();
        c.put("key", "预留");
        c.put("value", pictureValues.get(9));
       /* HashMap c = new HashMap<String, Integer>();
        c.put("key", "预留");
        c.put("value", pictureValues.get(2));*/
        data.getPicture1().add(a);
        data.getPicture1().add(b);
        data.getPicture1().add(c);
        HashMap d = new HashMap<String, Integer>();
        d.put("key", "在线");
        d.put("value", pictureValues.get(3));
        HashMap e = new HashMap<String, Integer>();
        e.put("key", "离线");
        e.put("value", pictureValues.get(4));
        HashMap s = new HashMap<String, Integer>();
        s.put("key", "预留");
        s.put("value", pictureValues.get(9));
        data.getPicture2().add(d);
        data.getPicture2().add(e);
        data.getPicture2().add(s);
        HashMap f= new HashMap<String, Integer>();
        f.put("key", "已知");
        f.put("value", pictureValues.get(5));
        HashMap g= new HashMap<String, Integer>();
        g.put("key", "未知");
        g.put("value", pictureValues.get(6));
        HashMap t = new HashMap<String, Integer>();
        t.put("key", "预留");
        t.put("value", pictureValues.get(9));
        data.getPicture3().add(f);
        data.getPicture3().add(g);
        data.getPicture3().add(t);
        HashMap h= new HashMap<String, Integer>();
        h.put("key", "未分配");
        h.put("value",pictureValues.get(7));
        HashMap i= new HashMap<String, Integer>();
        i.put("key", "已分配");
        i.put("value", pictureValues.get(8));
        HashMap r = new HashMap<String, Integer>();
        r.put("key", "预留");
        r.put("value", pictureValues.get(9));
        data.getPicture4().add(h);
        data.getPicture4().add(i);
        data.getPicture4().add(r);
        data.setIpAddresses(data1.getIpAddresses());
        data.setPrincipal(data1.getPrincipal());
        data.setGroupIds(data1.getGroupIds());
        data.setOrgIds(data1.getOrgIds());
        return Reply.ok(data);
    }

    /**
     * 查询ip地址管理 table页数据
     */
    @Override
    public Reply selectSnoList(QueryIpAddressManageListParam queryIpAddressManageParam) throws Exception {
        boolean tem = false;
        IpAddressManageTableParam qParam = new IpAddressManageTableParam();
        qParam.setId(queryIpAddressManageParam.getLinkId());
        if (queryIpAddressManageParam.getLinkId()<0) {
            qParam.setId(-queryIpAddressManageParam.getLinkId());
            tem = true;
            queryIpAddressManageParam.setIsTem(1);
            queryIpAddressManageParam.setLinkId(-queryIpAddressManageParam.getLinkId());
        }
        IpAddressManageTableParam    ipAddressManageTableParam   = mwIpAddressManageTableDao.selectIpAddressById1(qParam);
        updateIncludIpAddress(ipAddressManageTableParam.getIpAddresses());
        queryIpAddressManageParam.setInclude(ipAddressManageTableParam.getInclude());
        PageHelper.startPage(queryIpAddressManageParam.getPageNumber(), queryIpAddressManageParam.getPageSize());
        Map priCriteria = PropertyUtils.describe(queryIpAddressManageParam);
        if (queryIpAddressManageParam.getOrderName() != null && queryIpAddressManageParam.getOrderType() != null) {
            String s = ConverParam.HumpToLine(queryIpAddressManageParam.getOrderName());
                s = s.toLowerCase();
            priCriteria.put("orderName", s);
        }
        List<AddUpdateIpAddressManageListParam> list = mwIpAddressManageListTableDao.selectSonList(priCriteria);

        Map<Integer, AddUpdateIpAddressManageListParam> idMap = new HashMap<>();
        List<Integer> ids = new ArrayList<>();
;        for (AddUpdateIpAddressManageListParam q:list) {
            try {
                if(q.getAccessPortName().equals(IPInfoDTO.MORE_DATA)){
                    idMap.put(q.getId(), q);
                    ids.add(q.getId());
                }

                int port = Integer.parseInt(q.getAccessPort());
                if(port <= 0){
                    q.setAccessPort("");
                }
                MwTangibleassetsDTO mwTangibleassetsDTO =mwTangibleAssetsService.selectByIp(q.getIpAddress());
                if (mwTangibleassetsDTO!=null){
                    q.setAssetsName(mwTangibleassetsDTO.getAssetsName());
                    q.setAssetsId(mwTangibleassetsDTO.getAssetsId());
                }

            }catch (Exception e){
                q.setAccessPort("");
            }

            if (q.getAssetsDetail().size()>0){
                q.setAssetsTypeInOrOut(q.getAssetsDetail().get(0).getAssetsType());
            }

            try {
                //过滤mac显示
                if(q.getMac().equals(DeviceInfo.DEFAULT_MAC)){
                    q.setMac("");
                    q.setVendor("");
                }
            }catch (Exception e){
                q.setMac("");
                q.setVendor("");
            }
        }

        if(ids.size() > 0) {
            List<AddUpdatePortInfoParam> portInfoParams = mwIpAddressManageListTableDao.selectPortInfos(ids);
            for (AddUpdatePortInfoParam portInfoParam : portInfoParams) {
                AddUpdateIpAddressManageListParam ipInfo = idMap.get(portInfoParam.getIpManageListId());
                List<AddUpdatePortInfoParam> portInfoParamList = ipInfo.getPortInfos();
                if (null == portInfoParamList) {
                    portInfoParamList = new ArrayList<>();
                    ipInfo.setPortInfos(portInfoParamList);
                }else if(ipInfo.getAccessPortName().equals(IPInfoDTO.MORE_DATA)){
                    //有多个上联设备时,接入设备及端口显示设置
                    ipInfo.setAccessPortName(portInfoParam.getAccessPortName());
                    ipInfo.setAccessEquip(portInfoParam.getAccessEquip());
                    ipInfo.setAccessPort(portInfoParam.getAccessPort());
                }
                portInfoParamList.add(portInfoParam);
            }
        }

        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);
        mwMessageService.createMessage(queryIpAddressManageParam.getLinkId().toString(),4,0,null);
        return Reply.ok(pageInfo);
    }

    /**
     * 编辑前查询ip地址管理
     */
    @Override
    public Reply selectList1(QueryIpAddressManageParam qParam) {
        if (qParam.getId() == null) {
            return Reply.fail("id不能为空");
        }
        IpAddressManageTable1Param s = mwIpAddressManageTableDao.selectIpAddressById(qParam);

        //将用户，用户组，机构处理数组形式返回给前端
        List<String> orgidlist1 = mwIpAddressManageTableDao.selectOrg2(s.getOrgIdss());
        for (String o : orgidlist1) {
            List<Integer> orgidItem = new ArrayList<>();
            String[] strs = o.split(",");
            for (String str : strs) {
                if (!str.trim().equals("")) {
                    orgidItem.add(Integer.parseInt(str.trim()));
                }
            }
            s.getOrgIds().add(orgidItem);
        }
        //去空
        dealNull1(s);
        return Reply.ok(s);
    }

    public void dealNull1(IpAddressManageTable1Param s) {
        if (s != null) {
            s.getOrgIds().removeAll(Collections.singleton(null));
            s.getGroupIds().removeAll(Collections.singleton(null));
            s.getPrincipal().removeAll(Collections.singleton(null));
        }
    }

    /**
     * 查询ip地址管理
     */
    @Override
    public Reply selectList(QueryIpAddressManageParam qParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<IpAddressManageTableParam> list = new ArrayList<>();
        List<IpAddressManageTableParam> list1 = new ArrayList<>();
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
                if (qParam.getPageNumber() == null) {
                } else {
                    PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                }
                Map priCriteria = PropertyUtils.describe(qParam);
                list = mwIpAddressManageTableDao.selectPriIpAddress(priCriteria);
                for (IpAddressManageTableParam val :
                        list) {
                    IpAddressManageTableParam va2 = new IpAddressManageTableParam();
                    queryAddress(val, va2);
                    val.setLongitude(va2.getLongitude());
                    val.setLatitude(va2.getLatitude());
                }
                break;
            case PUBLIC:
                // List<String> orgNames = mwUserOrgMapperDao.getOrgNameByLoginName(loginName);
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
                    //  List<String> nodes = mwUserOrgMapperDao.getOrgNodesByLoginName(loginName);
                    //  orgIds = mwUserOrgMapperDao.getOrgIdByUserId(loginName);

                    orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);

                }
                if (null != orgIds && orgIds.size() > 0) {
                    qParam.setOrgIds(orgIds);
                }
                qParam.setIsAdmin(isAdmin);
                if (qParam.getPageNumber() == null) {
                } else {
                    PageHelper.startPage(qParam.getPageNumber(), qParam.getPageSize());
                }
                Map priCriteria1 = PropertyUtils.describe(qParam);
                list = mwIpAddressManageTableDao.selectPubIpAddress(priCriteria1);
                for (IpAddressManageTableParam val :
                        list) {
                    IpAddressManageTableParam va2 = new IpAddressManageTableParam();
                    queryAddress(val, va2);
                    val.setLongitude(va2.getLongitude());
                    val.setLatitude(va2.getLatitude());
                }
                break;
        }
        //去空
        dealNull(list);
        for (IpAddressManageTableParam var : list
        ) {
            var.setIPv4(true);
            if ("grouping".equals(var.getType())) {
                int i = mwIpAddressManageTableDao.checkIsLeaf(var.getId());
                if (i == 0) {
                    var.setLeaf(true);
                }
            }

        }

        PageInfo pageInfo = new PageInfo<>(list);
        pageInfo.setList(list);
        return Reply.ok(pageInfo);
    }

    public void queryAddress(IpAddressManageTableParam val, IpAddressManageTableParam va2) {
        //IpAddressManageTableParam val3=new IpAddressManageTableParam();
        if (val != null) {
            if (val.getLatitude() == null || val.getLongitude() == null) {
                IpAddressManageTableParam val2 = new IpAddressManageTableParam();
                val2.setId(val.getParentId());
                IpAddressManageTableParam table1Param = mwIpAddressManageTableDao.selectIpAddressById1(val2);
                queryAddress(table1Param, va2);
            } else {
                va2.setLatitude(val.getLatitude());
                va2.setLongitude(val.getLongitude());
            }
        }

    }

    public void dealNull(List<IpAddressManageTableParam> list) {
        if (list != null && list.size() > 0) {
            for (IpAddressManageTableParam a : list) {
                a.getOrgIds().removeAll(Collections.singleton(null));
                a.getGroupIds().removeAll(Collections.singleton(null));
                a.getPrincipal().removeAll(Collections.singleton(null));
            }
        }
    }

    /**
     * 删除ip地址管理
     */
    @Override
    @Transactional
    public Reply delete(AddUpdateIpAddressManageParam auParam) throws Exception {

        if (auParam.getIds() != null && auParam.getIds().size() > 0) {
            List<IpAddressManageTableParam> batUpdate = mwIpAddressManageTableDao.selectListByIds(auParam.getIds());
            if (batUpdate != null && batUpdate.size() > 0) {
                Boolean isdel = true;
                for (IpAddressManageTableParam updateData : batUpdate) {
                    isdel =isDistribution(updateData,true);
                }
                if (isdel){
                    for (IpAddressManageTableParam updateData : batUpdate) {
                        deleteCommon(updateData);
                    }
                }else {
                    return Reply.fail("地址已分配，请先回收地址");
                }
            }
        } else {
            IpAddressManageTableParam updateData = new IpAddressManageTableParam();
            BeanUtils.copyProperties(auParam, updateData);
            if (!deleteCommon(updateData)){
                return Reply.fail("地址已分配，请先回收地址");
            }
        }
        return Reply.ok("删除成功");
    }

    @Transactional
    public Boolean deleteCommon(IpAddressManageTableParam auParam) throws Exception {
        //将此节点下的所有数据关联数据
        List<IpAddressManageTableParam> subResult = mwIpAddressManageTableDao.selectSubIpAddress(auParam.getId());
        if (subResult != null && subResult.size() > 0) {
            for (IpAddressManageTableParam ipAddressManageTableParam : subResult) {
                deleteRecursive(ipAddressManageTableParam);
            }
        }
        if (auParam.getType().equals("iPaddresses")) {
            Integer count = mwIpAddressManageTableDao.selectIpMangeHaveDistribution(auParam.getId());
            if (count != 0) {
                return false;
            }
        }
        //删除ip地址管理的 负责人，用户组，机构 权限关系
        deleteMapperAndPerm(auParam);
        //删除公有ip地址
        mwIpAddressManageTableDao.deletePubIp(auParam.getId());

        if (auParam.getType().equals("iPaddresses")) {

            //删除 右侧旧table页子表数据历史数据
            mwIpAddressManageListTableDao.deleteHisByLinkId(auParam.getId());

            //创建ip删除记录
            createIpDeleteByLinkId(auParam.getId());

            //删除 右侧旧table页子表数据
            mwIpAddressManageListTableDao.deleteByLinkId(auParam.getId());
        }

        int parentId = auParam.getParentId();

        //删除ip地址管理
        mwIpAddressManageTableDao.delete(auParam.getId());

        //更新ip状态
        Set<Integer> linkIds = new HashSet<>();
        if (parentId == 0) {
        } else {
            linkIds.add(auParam.getParentId());
            recursive(linkIds);
        }
        return true;
    }

    private void createIpDeleteByLinkId(Integer id) {
        List<String> ipaddress = mwIpAddressManageTableDao.getListIPaddressByLinkId(id);
        createIpDelete(ipaddress);
    }

    private void createIpDelete(List<String> ipaddress) {
        if (ipaddress.size()>0){
            mwIpAddressManageTableDao.insertIpDelete(ipaddress);
        }
    }

    private boolean isDistribution(IpAddressManageTableParam ipAddressManageTableParam, Boolean isDistributionDel) {
        List<IpAddressManageTableParam> subResult = mwIpAddressManageTableDao.selectSubIpAddress(ipAddressManageTableParam.getId());
        if (subResult != null && subResult.size() > 0) {
            for (IpAddressManageTableParam ipAddressOrGroup : subResult) {
                if ("grouping".equals(ipAddressOrGroup.getType())) {
                    isDistributionDel = isDistribution(ipAddressOrGroup,isDistributionDel);
                    if (isDistributionDel==false){
                        return isDistributionDel;
                    }
                } else {
                    Integer count = mwIpAddressManageTableDao.selectIpMangeHaveDistribution(ipAddressOrGroup.getId());
                    if (count!=0){
                        isDistributionDel =false;
                        return isDistributionDel;
                    }
                }
            }
        }
        if (ipAddressManageTableParam.getType().equals("iPaddresses")) {
            Integer count = mwIpAddressManageTableDao.selectIpMangeHaveDistribution(ipAddressManageTableParam.getId());
            if (count != 0) {
                return false;
            }
        }
        return isDistributionDel;
    }

    @Transactional
    public void deleteRecursive(IpAddressManageTableParam auParam) throws Exception {
        //将此节点下的所有数据关联数据
        List<IpAddressManageTableParam> subResult = mwIpAddressManageTableDao.selectSubIpAddress(auParam.getId());
        if (subResult != null && subResult.size() > 0) {
            for (IpAddressManageTableParam ipAddressOrGroup : subResult) {
                if ("grouping".equals(ipAddressOrGroup.getType())) {
                    deleteRecursive(ipAddressOrGroup);
                } else {
                    //删除ip地址管理的 负责人，用户组，机构 权限关系
                    deleteMapperAndPerm(auParam);
                    //删除 右侧旧table页子表数据历史数据
                    mwIpAddressManageListTableDao.deleteHisByLinkId(ipAddressOrGroup.getId());

                    //创建ip删除记录
                    createIpDeleteByLinkId(ipAddressOrGroup.getId());
                    //删除 右侧旧table页子表数据
                    mwIpAddressManageListTableDao.deleteByLinkId(ipAddressOrGroup.getId());
                    //删除ip地址管理
                    mwIpAddressManageTableDao.delete(ipAddressOrGroup.getId());
                }
            }
        }

        //删除ip地址管理的 负责人，用户组，机构 权限关系
        deleteMapperAndPerm(auParam);

        if (auParam.getType().equals("iPaddresses")) {
            //删除 右侧旧table页子表数据历史数据
            mwIpAddressManageListTableDao.deleteHisByLinkId(auParam.getId());
            //创建ip删除记录
            createIpDeleteByLinkId(auParam.getId());
            //删除 右侧旧table页子表数据
            mwIpAddressManageListTableDao.deleteByLinkId(auParam.getId());
        }

        //删除ip地址管理
        mwIpAddressManageTableDao.delete(auParam.getId());

    }


    /**
     * 修改ip地址管理
     */
    @Override
    public Reply update(AddUpdateIpAddressManageParam auParam) throws Exception {
        //处理一下参数格式
        conver(auParam);

        //去空
        dealNull2(auParam);

        //修改ip地址管理 主信息
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        auParam.setModificationDate(new Date());
        mwIpAddressManageTableDao.update(auParam);
        //递归分组下的所有IP地址段 如果分组间隔扫描时间修改了，IP地址段也需要修改
        if (auParam.getTiming() != null) {
            List<Map<String, Integer>> idlist = new ArrayList<>();
            getIdAddresses(auParam.getId(), idlist);
            List<Map<String, Integer>> ip1 = idlist.stream().filter(stringIntegerMap -> stringIntegerMap.containsKey("ipAddresses")).collect(Collectors.toList());
            ip1.stream().forEach(stringIntegerMap -> {
                Integer ipAddresses = stringIntegerMap.get("ipAddresses");
                //update 分组下的所有IP地址段时间字段
                //AddUpdateIpAddressManageParam clone = (AddUpdateIpAddressManageParam)auParam.clone();
                AddUpdateIpAddressManageParam clone = new AddUpdateIpAddressManageParam();
                clone.setTiming(auParam.getTiming());
                clone.setId(ipAddresses);
                mwIpAddressManageTableDao.update(clone);
            });
        }


        //修改ip地址管理的 负责人，用户组，机构 权限关系（这里我们直接把旧的这些权限删除，重新添加）
        deleteMapperAndPerm(auParam);
        addMapperAndPerm(auParam);
        updateIncludIpAddress(auParam.getIpAddresses());

        //修改标签信息，先删除后添加
        if (auParam.getLabels() != null && auParam.getLabels().size() > 0) {
            mwIpAddressManageTableDao.deleteLabelLink(auParam.getId());
            insertLinkLabel(auParam);
        }

        /* 修改ip地址管理主表数据 我们就把table页子表数据重新添加
         * 1: 删除 右侧旧table页子表数据
         * 2：添加 右侧table页子表数据
         */
        //mwIpAddressManageListTableDao.deleteByLinkId(auParam.getId());
        //addIpAddressManageListTable(auParam);

        dealNull2(auParam);
        return Reply.ok(auParam);
    }

    private void updateIncludIpAddress(String auParam) { 
        if (auParam!=null&&!auParam.toString().trim().equals("")){
            String[] str = auParam.split("/");
            String startIp= IpV4Util.getBeginIpStr(str[0], str[1]);
            String endIp= IpV4Util.getEndIpStr(str[0], str[1]);

            if (startIp!=endIp){
                List<String> strings  = new ArrayList<>();
                strings.add(startIp);
                strings.add(endIp);
                mwIpAddressManageListTableDao.updateIpIncludecollect(0,strings);
            }
        }
    }

    //获取分组下的所有IP地址段
    public void getIdAddresses(Integer Id, List<Map<String, Integer>> idlist) {
        QueryIpAddressManageParam param = new QueryIpAddressManageParam();
        param.setParentId(Id);
        try {
            Reply reply = selectList(param);
            PageInfo data = (PageInfo) reply.getData();
            List<IpAddressManageTableParam> list = data.getList();
            for (IpAddressManageTableParam var :
                    list) {
                Integer id = var.getId();

                if ("ipAddresses".equalsIgnoreCase(var.getType())) {
                    HashMap<String, Integer> idMap = new HashMap<>();
                    idMap.put("ipAddresses", id);
                    idlist.add(idMap);
                }
                if ("grouping".equalsIgnoreCase(var.getType())) {
                    HashMap<String, Integer> idMap = new HashMap<>();
                    idMap.put("grouping", id);
                    idlist.add(idMap);
                    getIdAddresses(var.getId(), idlist);
                }
            }
        } catch (Exception e) {
            log.info("获取下级id失败");
        }

    }

    public void dealNull2(AddUpdateIpAddressManageParam a) {
        if (a != null) {
            a.getOrgIds().removeAll(Collections.singleton(null));
            a.getGroupIds().removeAll(Collections.singleton(null));
            a.getPrincipal().removeAll(Collections.singleton(null));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Reply insert(AddUpdateIpAddressManageParam param) throws Exception {
        AddUpdateIpAddressManageParam auParam = null;
        String lable = param.getIpAddresses();
        if(param.getInclude()==null){
            param.setInclude(0);
        }

        //若本次添加的数据名称字段为空，就取ip地址段的值作为名称
        if (param.getLabel() == null || param.getLabel().equals("")) {
            if (param.getIpAddresses() != null && !param.getIpAddresses().equals("")) {
                param.setLabel(param.getIpAddresses());
            }
        }

        if (param.getCreateIp()!=0&&param.getIpAddresses() != null && !param.getIpAddresses().equals("") && "iPaddresses".equalsIgnoreCase(param.getType())) {
            //对ip地址段的格式进行校验
            String[] ip = param.getIpAddresses().split("/");
            if (ip.length != 2) {
                return Reply.fail("请输入正确格式的ip地址段!");
            } else {
                String sip0 = ip[0];//ip
                Integer icird1 = null;
                try {
                    icird1 = Integer.parseInt(ip[1]);//段
                } catch (NumberFormatException e) {
                    return Reply.fail("ip地址段中段格式错误");
                }
                Boolean flag = IpV4Util.isIP(sip0);
                if (!flag) {
                    return Reply.fail("ip地址段中ip格式错误");
                }
                if (icird1 >= 0 && icird1 < 16) {
                    return Reply.fail("ip地址段中段格式暂不支持");
                } else if (icird1 > 32) {
                    return Reply.fail("ip地址段中段格式错误");
                }
            }

            //正式开始执行保方法
            Integer cirdI = Integer.parseInt(ip[1]);
            if (cirdI <= 32 && cirdI >= 24) {
                List<String> valList = new ArrayList<>();
                valList.add(param.getIpAddresses());
                List<String> resultIp = ipRemove(valList,param);
                if (resultIp.size() == 0) {
                    throw new Exception("ip地址段已存在，请选择其他地址段");
                }
                Integer count  = mwIpAddressManageTableDao.selectCount();
                ResponseBase responseBase = licenseManagement.getLicenseManagemengt("ip_manage",count , (int)Math.pow(2,32-cirdI));
                if (responseBase.getRtnCode() != 200) {
                    return Reply.fail(responseBase.getMsg(), responseBase.getData());
                }
                //添加地址段 24-32
                param.setIpAddresses(IPStaet(param.getIpAddresses()));
                auParam = insert1(param);
            } else if (cirdI < 24 && cirdI >= 16) {
                //添加地址段 [16,24)
                String ipFirst = ip[0];
                String cird = ip[1];
                String[] ipss = ipFirst.split("\\.");
                String s1 = ipss[0];
                String s2 = ipss[1];
                String s3 = ipss[2];
                String s4 = ipss[3];

                String s3_2 = MwIpAddressManageServiceImpl.toBinary(Integer.parseInt(s3), 8);

                String s31 = s3_2.substring(0, Integer.parseInt(cird) - 16);

                List<String> lists = ipInterval(24 - Integer.parseInt(cird));

                List<String> list_s3 = new ArrayList<>();
                for (String s : lists) {
                    list_s3.add(s31 + s);
                }

                //IP地址段
                List<String> list_09 = new ArrayList<>();
                for (String s : list_s3) {
                    list_09.add(s1 + "." + s2 + "." + String.valueOf(Integer.parseInt(s, 2)) + "." + s4 + "/24");
                }
                List<String> list_10 = ipRemove(list_09, param);
                AddUpdateIpAddressManageParam continuParam = (AddUpdateIpAddressManageParam) param.clone();
                if (param.getIPAllCreate()==1){
                    continuParam.setType("iPaddresses");
                    conver(continuParam);

                    dealNull2(continuParam);

                    //添加ip地址管理 主信息
                    continuParam.setCreator(iLoginCacheInfo.getLoginName());
                    continuParam.setCreateDate(new Date());
                    continuParam.setModifier(iLoginCacheInfo.getLoginName());
                    continuParam.setModificationDate(new Date());
                    mwIpAddressManageTableDao.insert(continuParam);

                    //添加负责人，用户组，机构 权限关系
                    addMapperAndPerm(continuParam);
                }

                if(list_10 != null && list_10.size() > 0) {
                    ExecutorService pool = Executors.newFixedThreadPool(3);
                    CountDownLatch latch1 = new CountDownLatch(list_10.size());
                    for (String s : list_10) {
                        Integer count  = mwIpAddressManageTableDao.selectCount();
                        ResponseBase responseBase = licenseManagement.getLicenseManagemengt("ip_manage",count , list_10.size()*256);
                        if (responseBase.getRtnCode() != 200) {
                            return Reply.fail(responseBase.getMsg(), responseBase.getData());
                        }
                        AddUpdateIpAddressManageParam addParam = (AddUpdateIpAddressManageParam) param.clone();
                        if (param.getIPAllCreate()==1){
                             addParam =continuParam;
                            addParam.setIpAddresses(IPStaet(s));
                        }else {
                            addParam.setType("iPaddresses");
                            addParam.setLabel(s);
                            addParam.setIpAddresses(IPStaet(s));
                        }
                        Future f = pool.submit(new MwIpAddressManageServiceImpl(addParam, latch1,
                                iLoginCacheInfo, mwIpAddressManageTableDao, this, mwCommonService,
                                mwIpAddressManageListTableDao, accessKeyId, accessKeySerect));
                    }
                    mainThreadOtherWord();
                    pool.shutdown();
                    latch1.await();
                    String username = iLoginCacheInfo.getLoginName();
                    if (param.getIPAllCreate()==1){
                        String splitIP[] = list_10.get(0).split("/");
                        String EndsplitIP[] = list_10.get(list_10.size()-1).split("/");
                        String startIP = getStart(splitIP[0]);
                        String endIP = EndIP(EndsplitIP[0]);
                        Integer Link =continuParam.getId();
                        mwIpAddressManageTableDao.updateIpInclude(Link,startIP,endIP);
                        mwIpAddressManageTableDao.updateIpIncludeTwo(Link,startIP,endIP);
                    }
                    SystemLogDTO sbuild = SystemLogDTO.builder().userName(username).modelName("IP地址管理")
                            .objName(param.getLabel()).operateDes("新增IPV4地址段").build();
                    dbLogger.info(JSON.toJSONString(sbuild));
                } else {
                    throw new Exception("ip地址段已存在，请选择其他地址段");
                }
            }
        } else {
            //添加包
            auParam = insert1(param);
        }
        return Reply.ok(auParam);
    }

    private String EndIP(String ipFirst) {
        String[] ipss = ipFirst.split("\\.");
        String s1 = ipss[0];
        String s2 = ipss[1];
        String s3 = ipss[2];
        String s4 = ipss[3];
        return s1 + "." + s2 + "." +s3 + "." + "255";
    }

    private String getStart(String ipFirst) {
        String[] ipss = ipFirst.split("\\.");
        String s1 = ipss[0];
        String s2 = ipss[1];
        String s3 = ipss[2];
        String s4 = ipss[3];
        return s1 + "." + s2 + "." +s3 + "." + "0";
    }

    /**
     * ip地址段去重
     */
    public List<String> ipRemove(List<String> listIp1, AddUpdateIpAddressManageParam param) {
        List<String> resultIp = new ArrayList<>();
        List<String> allIpAddresses = mwIpAddressManageTableDao.selectAllIpaddresses(param.getSignId());
        resultIp.addAll(listIp1);
        resultIp.removeAll(allIpAddresses);
        return resultIp;
    }

    /**
     * ip地址清单去重
     */
    public Set<String> ipListRemove(Set<String> listIp1, String ip,Integer signId) {
        Set<String> resultIp = new HashSet<>();
        List<String> allIp = mwIpAddressManageListTableDao.selectAllIpLIst(ip,signId);
        if (allIp.size() > 0) {
            Set<String> allIpset = new HashSet<>(allIp);
            for (String ips : listIp1
            ) {
                if (!allIpset.contains(ips)) {
                    resultIp.add(ips);
                }
            }
        } else {
            return listIp1;
        }

        return resultIp;
    }

    //ip地址段区间为16-23
    //多线程下 不支持spring原生事务 需要手动清除IP地址段
    public Set<String> ipAddressesRemove(Set<String> listIp1, List<String> allIp) {
        Set<String> resultIp = new HashSet<>();
        if (allIp.size() > 0) {
            Set<String> allIpset = new HashSet<>(allIp);
            for (String ips : listIp1
            ) {
                if (!allIpset.contains(ips)) {
                    resultIp.add(ips);
                }
            }
        } else {
            return listIp1;
        }

        return resultIp;
    }

    public void mainThreadOtherWord() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            log.error("错误返回 :{}",e);
        }
    }


    @Override
    public void run() {
        try {
            AddUpdateIpAddressManageParam auParam = param;

            if (param.getIPAllCreate()==1){

            }else {
                //处理一下数格式
                conver(auParam);

                dealNull2(auParam);

                //添加ip地址管理 主信息
                auParam.setCreator(iLoginCacheInfo1.getLoginName());
                auParam.setCreateDate(new Date());
                auParam.setModifier(iLoginCacheInfo1.getLoginName());
                auParam.setModificationDate(new Date());

                mwIpAddressManageTableDao1.insert(auParam);



                //添加负责人，用户组，机构 权限关系
                addMapperAndPerm1(auParam);
            }


            /*添加右侧table页子表数据
             * 1: 根据auParam 获取所有的ip地址集合(去重)
             * 2：根据ip地址集合  添加ip地址管理的 table页数据（子表数据）
             */
            if (auParam.getType().equals("iPaddresses")) {
                try {
                    addIpAddressManageListTable1(auParam);
                } catch (Exception e) {
                    log.error("错误返回 :{}",e);
                }
            }
        } catch (Exception e) {
            log.error("错误返回 :{}",e);
        } finally {
            latch.countDown();
        }
    }


    public static String toBinary(int num, int digits) {
        int value = 1 << digits | num;
        String bs = Integer.toBinaryString(value);
        return bs.substring(1);
    }

    private static List<String> ipInterval(int num) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 2 << (num - 1); i++) {
            String binaryString = MwIpAddressManageServiceImpl.toBinary(i, num);
            list.add(binaryString);
        }
        return list;
    }

    /**
     * 新增ip地址管理
     *
     * @param auParam
     * @return
     */
    @Override
    public AddUpdateIpAddressManageParam insert1(AddUpdateIpAddressManageParam auParam) throws Exception {
        //处理一下参数格式
        conver(auParam);

        dealNull2(auParam);

        //添加ip地址管理 主信息
        auParam.setCreator(iLoginCacheInfo.getLoginName());
        auParam.setCreateDate(new Date());
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        auParam.setModificationDate(new Date());
        mwIpAddressManageTableDao.insert(auParam);

        //添加负责人，用户组，机构 权限关系
        addMapperAndPerm(auParam);

        //添加标签信息
        //insertLinkLabel(auParam);

        /*添加右侧table页子表数据
         * 1: 根据auParam 获取所有的ip地址集合(去重)
         * 2：根据ip地址集合  添加ip地址管理的 table页数据（子表数据）
         */
        if (auParam.getType().equals("iPaddresses")&&auParam.getCreateIp()==1) {
            addIpAddressManageListTable(auParam);
        }


        //当添加数据时，sessio中的用户，和这条数据中的负责人选择的都是admin时这种情况
        //返回参数负责人会出现重复数据（数据库里存储的数据是正常）所以在这里去重
        auParam.setPrincipal(removeDuplicate(auParam.getPrincipal()));
        dealNull2(auParam);
        //return Reply.ok(auParam);
        return auParam;
    }

    @Override
    public Reply queryLocAdress(QueryLocAddressParam param) throws Exception {
        Reply reply = new Reply();
        List<String> resultList = new ArrayList<>();
        Document parse = getXml();
        if (AddressType.REGION.equals(param.getType())) {
            getRegion(resultList, parse, param);
        } else if (AddressType.CITY.equals(param.getType())) {
            getCity(resultList, parse, param);
        } else if (AddressType.STATE.equals(param.getType())) {
            getState(resultList, parse, param);
        } else if (AddressType.COUNTRY.equals(param.getType())) {
            getCountry(resultList, parse);
        } else {
            return Reply.fail("参数中缺少类型字段");
        }
        reply.setData(resultList);
        return reply;
    }

    @Override
    public Reply selectGitList(QueryIpAddressManageParam queryIpAddressManageParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        //公有IP私有IP 分别查询
        List<QueryGitParam> gitList = new ArrayList<>();
        if ("PUB".equalsIgnoreCase(queryIpAddressManageParam.getType())) {
            List<AddPubIpAddressParam> pubList = mwIpAddressManageTableDao.selectPubIp();
            for (AddPubIpAddressParam var : pubList
            ) {
                QueryGitParam var1 = new QueryGitParam();
                var1.setLng(var.getLongitude());
                var1.setLat(var.getLatitude());
                var1.setArea(var.getCountry());
                StringBuilder sb = new StringBuilder();
                sb.append(var.getCountry()).append(var.getState()).append(var.getCity()).append(var.getRegion());
                var1.setInfo(sb.toString());
                var1.setType("PUB");
                gitList.add(var1);
            }
        } else {
            List<IpAddressManageTableParam> priList = mwIpAddressManageTableDao.selectPriIp();
            for (IpAddressManageTableParam var : priList
            ) {
                QueryGitParam var1 = new QueryGitParam();
                var1.setLng(var.getLongitude());
                var1.setLat(var.getLatitude());
                var1.setArea(var.getCountry());
                StringBuilder sb = new StringBuilder();
                sb.append(var.getCountry()).append(var.getState()).append(var.getCity()).append(var.getRegion()).append(var.getAddressDesc());
                var1.setInfo(sb.toString());
                var1.setType("PRI");
                gitList.add(var1);
            }

        }

        return Reply.ok(gitList);
    }

    @Override
    public PageInfo selectKillIP(Map<String, Object> map, QueryIpAddressManageParam qParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        String loginName = iLoginCacheInfo.getLoginName();

        qParam.setIp((List<String>) map.get("IpFour"));
        qParam.setIpSix((List<String>) map.get("IpSix"));
        List<Map<String, String>> maps = new ArrayList<>();

        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            qParam.setGroupIds(groupIds);
        }
        // List<String> orgNames = mwUserOrgMapperDao.getOrgNameByLoginName(loginName);
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

            if (null != orgIds && orgIds.size() > 0) {
                qParam.setOrgIds(orgIds);
            }
            qParam.setIsAdmin(isAdmin);

            Map priCriteria1 = PropertyUtils.describe(qParam);
//                list = mwIpAddressManageTableDao.selectPubIpAddress(priCriteria1);

            List<Map<String, String>> mapsFour = (List<Map<String, String>>) map.get("killIp");
            if (qParam.getIsIPv4()){
                List<String> finalList = mwIpAddressManageTableDao.selectIpKillPremit(priCriteria1);
                maps.addAll(mapsFour.stream().filter(e -> finalList.contains(e.get("ip"))).collect(Collectors.toList()));
            }else {
                List<String> mapsSix = (List<String>) map.get("IpSix");
                List<String> finalListSix = mwIpAddressManageTableDao.selectIpSixKillPremit(priCriteria1);
                List<String> strings = new ArrayList<>();
                for (String address:finalListSix) {
                    strings.addAll(mapsSix.stream().filter(e -> IPv6Judge.getInstance().IpContain(address,IPv6Judge.getInstance().getFullIPv6(e))).collect(Collectors.toList()));

                }
                maps.addAll(mapsFour.stream().filter(e -> strings.contains(e.get("ip"))).collect(Collectors.toList()));
            }
        } else {
            List<Map<String, String>> killIP = (List<Map<String, String>>) map.get("killIp");
            if (qParam.getIsIPv4()){
                List<String> IpFour = (List<String>) map.get("IpFour");
                maps.addAll(killIP.stream().filter(e -> IpFour.contains(e.get("ip"))).collect(Collectors.toList()));
            }else {
                List<String> IpSix = (List<String>) map.get("IpSix");
                maps.addAll(killIP.stream().filter(e -> IpSix.contains(e.get("ip"))).collect(Collectors.toList()));
            }

//            maps.addAll((List<Map<String, String>>) map.get("killIp"));

        }
        List<Map<String, String>> mapsRes = new ArrayList<>();

        List<MwIpConnection> mwIpConnections = new ArrayList<>();
        for (int i = 0; i <maps.size() ; i++) {
            Map<String,String> finalMap =maps.get(i);
            MwIpConnection mwIpConnectionOne = new MwIpConnection();
            List<MwIpConnection> mwIpConnection= mwIpAddressManageTableDao.selectIpConnection(finalMap.get("ip"));
            if (mwIpConnection.size()>0){
                if (finalMap.get("status").equals("2")){
                    mwIpConnectionOne.setIpAddress(finalMap.get("ip"));
                    mwIpConnectionOne.setOper("绿盟操作者");
                    String orgName= mwIpAddressManageTableDao.getOrgNameByIp(finalMap.get("ip")).toString()==null? mwIpAddressManageTableDao.getOrgNameByIpv6(finalMap.get("ip")).toString():mwIpAddressManageTableDao.getOrgNameByIp(finalMap.get("ip")).toString();
                    mwIpConnectionOne.setOrgName(orgName);
                    mwIpConnectionOne.setOperType(1);
                    mwIpConnectionOne.setOperStatus(1);
                    mwIpConnectionOne.setOperPlatform("绿盟");
                    mwIpConnectionOne.setOperTime(new Date());
                    mwIpConnections.add(mwIpConnectionOne);
                    mwIpAddressManageTableDao.updateIpConnection(mwIpConnection);
                    finalMap.put("status","正常");
                    mapsRes.add(finalMap);
                }else {
                    Date date = new Date();
                    mwIpConnectionOne=mwIpConnection.get(0);
                    finalMap.put("timeout", String.valueOf((date.getTime()-mwIpConnectionOne.getOperTime().getTime())/6000));
                    finalMap.put("orgName", mwIpConnectionOne.getOrgName());
                    finalMap.put("status","断网");
                    finalMap.put("extend","展开");
                    mapsRes.add(finalMap);
                }
            }else {
                if (finalMap.get("status").equals("2")){
                    finalMap.put("status","正常");
                    mapsRes.add(finalMap);
                }else {
                    mwIpConnectionOne.setIpAddress(finalMap.get("ip"));
                    mwIpConnectionOne.setOper("绿盟操作者");
                    String orgName= mwIpAddressManageTableDao.getOrgNameByIp(finalMap.get("ip")).toString()==null? mwIpAddressManageTableDao.getOrgNameByIpv6(finalMap.get("ip")).toString():mwIpAddressManageTableDao.getOrgNameByIp(finalMap.get("ip")).toString();
                    mwIpConnectionOne.setOrgName(orgName);
                    mwIpConnectionOne.setOperType(0);
                    mwIpConnectionOne.setOperStatus(0);
                    mwIpConnectionOne.setOperPlatform("绿盟");
                    mwIpConnectionOne.setOperTime(new Date());
                    mwIpConnections.add(mwIpConnectionOne);
                    Date date = new Date();
                    finalMap.put("timeout", String.valueOf((date.getTime()-mwIpConnectionOne.getOperTime().getTime())/6000));
                    finalMap.put("orgName", mwIpConnectionOne.getOrgName());
                    finalMap.put("status","断网");
                    finalMap.put("extend","展开");
                    mapsRes.add(finalMap);
                }
            }

        }

        if (mwIpConnections.size()>0){
            mwIpAddressManageTableDao.insertIpConnection(mwIpConnections);
        }
        if (!(qParam.getSearchIp()==null||qParam.getSearchIp().equals(""))){
            mapsRes= mapsRes.stream().filter(e->e.get("ip").equals(qParam.getSearchIp())).collect(Collectors.toList());
        }
        PageList pageList = new PageList();
        List<Map<String, String>> set = pageList.getList(mapsRes, qParam.getPageNumber(), qParam.getPageSize());

        PageInfo pageInfo = new PageInfo<>(set);
        pageInfo.setPageNum(qParam.getPageNumber());
        pageInfo.setHasNextPage(mapsRes.size() > qParam.getPageNumber() * qParam.getPageSize());
        pageInfo.setIsLastPage(mapsRes.size() <= qParam.getPageNumber() * qParam.getPageSize());
        pageInfo.setSize(mapsRes.size());
        return pageInfo;
    }

    @Override
    public Map<String, String> serchIpPermit(AddDisConnection qParamOne, Boolean ipType) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        QueryIpAddressManageParam qParam = new QueryIpAddressManageParam();
        String loginName = iLoginCacheInfo.getLoginName();
        UserDTO userDTO = mwUserDao.selectByLoginName(loginName);
        List<String> ip = new ArrayList<>();
        ip.add(qParamOne.getIp());
        Map<String, String> maps = new HashMap<>();

        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            qParam.setGroupIds(groupIds);
        }
        // List<String> orgNames = mwUserOrgMapperDao.getOrgNameByLoginName(loginName);
        List<String> orgNames = mwOrgCommonService.getOrgNamesByNodes(loginName);
        List<String> finalList = new ArrayList<>();
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

            if (null != orgIds && orgIds.size() > 0) {
                qParam.setOrgIds(orgIds);
            }
            qParam.setIsAdmin(isAdmin);

//                list = mwIpAddressManageTableDao.selectPubIpAddress(priCriteria1);

            if (ipType) {
                qParam.setIp(ip);
                Map priCriteria1 = PropertyUtils.describe(qParam);
                finalList.addAll(mwIpAddressManageTableDao.selectIpKillPremit(priCriteria1));

            } else {
                qParam.setIpSix(ip);
                Map priCriteria1 = PropertyUtils.describe(qParam);
                List<String> finalListSix = mwIpAddressManageTableDao.selectIpSixKillPremit(priCriteria1);
                finalList.addAll(finalListSix.stream().filter(e -> IPv6Judge.getInstance().IpContain(e,IPv6Judge.getInstance().getFullIPv6(qParamOne.getIp()))).collect(Collectors.toList()));
            }
        } else {
            finalList.addAll(ip);
        }
        if (finalList.size() > 0) {
            maps.put("permit", "true");
        } else {
            maps.put("permit", "false");
        }

        if (userDTO.getPhoneNumber()==null||userDTO.getPhoneNumber().trim().equals("")){
            maps.put("havePhone","false");
        }
        else {
            maps.put("havePhone","true");
            maps.put("phone", userDTO.getPhoneNumber());
        }

        return maps;
    }

    @Override
    public Map<String, String> sendSms() {
        String loginName = iLoginCacheInfo.getLoginName();
        UserDTO userDTO = mwUserDao.selectByLoginName(loginName);
        Map<String, String> maps = new HashMap<>();
        if (userDTO.getPhoneNumber()==null||userDTO.getPhoneNumber().trim().equals("")){
            maps.put("havePhone","false");
        }
        else {
            maps.put("havePhone","true");
            maps.put("phone", userDTO.getPhoneNumber());
        }
        return maps;
    }

    @Override
    public PageInfo selectKillIPHistory(Map<String, Object> map, QueryIpAddressManageParam qParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        String loginName = iLoginCacheInfo.getLoginName();

        qParam.setIp((List<String>) map.get("IpFour"));
        qParam.setIpSix((List<String>) map.get("IpSix"));
        List<Map<String, String>> maps = new ArrayList<>();

        List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);
        if (null != groupIds && groupIds.size() > 0) {
            qParam.setGroupIds(groupIds);
        }
        // List<String> orgNames = mwUserOrgMapperDao.getOrgNameByLoginName(loginName);
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

            if (null != orgIds && orgIds.size() > 0) {
                qParam.setOrgIds(orgIds);
            }
            qParam.setIsAdmin(isAdmin);

            Map priCriteria1 = PropertyUtils.describe(qParam);
//                list = mwIpAddressManageTableDao.selectPubIpAddress(priCriteria1);

            List<Map<String, String>> mapsFour = (List<Map<String, String>>) map.get("killIp");
            List<String> finalList = mwIpAddressManageTableDao.selectIpKillPremit(priCriteria1);
            List<String> finalListSix = mwIpAddressManageTableDao.selectIpSixKillPremit(priCriteria1);
            maps.addAll(mapsFour.stream().filter(e -> finalList.contains(e.get("ip"))).collect(Collectors.toList()));
            List<String> mapsSix = (List<String>) map.get("IpSix");
            List<String> strings = new ArrayList<>();
            for (String address:finalListSix) {
                strings.addAll(mapsSix.stream().filter(e -> IPv6Judge.getInstance().IpContain(address,IPv6Judge.getInstance().getFullIPv6(e))).collect(Collectors.toList()));

            }
            maps.addAll(mapsFour.stream().filter(e -> strings.contains(e.get("ip"))).collect(Collectors.toList()));
        } else {
            maps.addAll((List<Map<String, String>>) map.get("killIp"));
        }


        PageHelper.startPage(qParam.getPageNumber() , qParam.getPageSize());
        List<MwIpConnection> mwIpConnections = mwIpAddressManageTableDao.getMwIPConnection(maps);
        if (!(qParam.getSearchIp()==null||qParam.getSearchIp().equals(""))){
            if (!isAdmin) {
                mwIpConnections= mwIpConnections.stream().filter(e->e.getIpAddress().equals(qParam.getSearchIp())&&e.getOper().equals(loginName)).collect(Collectors.toList());
            }else {
                mwIpConnections= mwIpConnections.stream().filter(e->e.getIpAddress().equals(qParam.getSearchIp())).collect(Collectors.toList());
            }

        }else {
            if (!isAdmin) {
                mwIpConnections= mwIpConnections.stream().filter(e->e.getOper().equals(loginName)).collect(Collectors.toList());
            }
        }
        PageInfo pageInfo = new PageInfo<>(mwIpConnections);
        return pageInfo;
    }

    @Override
    public Reply updateIPConnection(AddDisConnection qParam, String type) {
        String loginName = iLoginCacheInfo.getLoginName();
        List<MwIpConnection> connections = new ArrayList<>();
        MwIpConnection mwIpConnectionOne = new MwIpConnection();

        mwIpConnectionOne.setIpAddress(qParam.getIp());
        mwIpConnectionOne.setOper(loginName);
        String orgName= mwIpAddressManageTableDao.getOrgNameByIp(qParam.getIp()).toString()==null? mwIpAddressManageTableDao.getOrgNameByIpv6(qParam.getIp()).toString():mwIpAddressManageTableDao.getOrgNameByIp(qParam.getIp()).toString();
        mwIpConnectionOne.setOrgName(orgName);
        mwIpConnectionOne.setOperPlatform("H5猫维");
        mwIpConnectionOne.setOperTime(new Date());
        mwIpConnectionOne.setOperType(type.equals("add")?0:1);
        mwIpConnectionOne.setOperStatus(type.equals("add")?0:1);

        if (!type.equals("add")){
            mwIpConnectionOne.setOperType(1);
            mwIpConnectionOne.setOperStatus(1);
            List<MwIpConnection> mwIpConnection= mwIpAddressManageTableDao.selectIpConnection(qParam.getIp());
            if (mwIpConnection!=null){
                mwIpAddressManageTableDao.updateIpConnection(mwIpConnection);
            }
        }

        connections.add(mwIpConnectionOne);
        if (connections.size()>0){
            mwIpAddressManageTableDao.insertIpConnection(connections);
        }
        return Reply.ok();
    }

    @Override
    public Reply getIpaddressList() {
        List<Map<String,String>> q = mwIpAddressManageTableDao.getIpaddressList();
        return Reply.ok(q);
    }

    @Override
    public Reply ipaddressStatusHisBrow(AddIpaddresStatusHis uParam) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        PageHelper.startPage(uParam.getPageNumber(), uParam.getPageSize());
        Map priCriteria = PropertyUtils.describe(uParam);
        List<AddIpaddresStatusHis> addIpaddresStatusHis =  mwIpAddressManageTableDao.selectIpaddressStatusHis(priCriteria);
        PageInfo pageInfo = new PageInfo<>(addIpaddresStatusHis);
        pageInfo.setList(addIpaddresStatusHis);
        return Reply.ok(pageInfo);
    }


    @Override
    public String sendWx(String IP,String Test) {
        String loginName = iLoginCacheInfo.getLoginName();
        String touser = mwUserGroupCommonService.getUserIdIdByLoginName(loginName);
        List<String> tousers = mwUserGroupCommonService.getWxOpenId(loginName);
        List<String> orgName = mwIpAddressManageTableDao.selectOrgName(IP);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HashMap<String, String> wxmap =new HashMap<>();
        wxmap.put("first","微信告警推送");
        wxmap.put("keyword1",orgName.toString()+IP+Test+"发送");
        wxmap.put("keyword2","一键断网");
        wxmap.put("keyword3",orgName.toString());
        wxmap.put("keyword4",df.format(new Date()));
        wxmap.put("keyword5",orgName.toString()+loginName+"操作了一键断网");
        wxmap.put("remark","请您正常查看");
        if (tousers.size()>0){
            sendwxMsagger(wxmap,tousers);
            log.info("weixin:{} touser",touser);
            return "断网成功，微信已推送";
        }
        return "断网成功，微信未绑定，推送失败";
    }

    @Override
    public void sendwxMsagger(HashMap<String, String> wxmap , List<String> tousers ){
        WxMpInMemoryConfigStorage wxStorage = new WxMpInMemoryConfigStorage();
        wxStorage.setAppId(appid);
        wxStorage.setSecret(secret);
        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(wxStorage);

        //2,推送消息
        String token  = WeiXinSendUtil.getAccessToken(appid,secret);
        for (String s:tousers){
            wxmap.put("appid",appid);
            wxmap.put("secret",secret);
            wxmap.put("templateid",templateid);
            try {
                WeiXinSendUtil.send(s,wxmap,token);
            } catch (Exception e) {
                log.info("微信推送失败：" + e.getMessage());
            }
        }
    }

    @Override
    public void createAnble() {

        List<AddUpdateIpAddressManageListParam> q = mwIpAddressManageTableDao.selectEndAnbleIpAddress();
        List<AddUpdateIpAddressManageListParam> w = new ArrayList<>();
        List<String> strings = new ArrayList<>();
        for (AddUpdateIpAddressManageListParam s:q) {
            String [] sb = s.getIpAddress().split("[.]");
            String ipAddress = sb[0]+"."+sb[1]+"."+sb[2]+"."+"0";
            s.setIpAddress(ipAddress);
            w.add(s);
            strings.add(ipAddress);
        }
        for (AddUpdateIpAddressManageListParam b:q) {
            String [] sq = b.getIpAddress().split("[.]");
            String ipAddress  = sq[0]+"."+sq[1]+"."+sq[2]+"."+"255";
            b.setIpAddress(ipAddress);
            w.add(b);
            strings.add(ipAddress);
        }

        if (q.size()>0){
            mwIpAddressManageListTableDao.updateIpIncludecollect(1,strings);
            mwIpAddressManageListTableDao.insertBatch(w);
        }
    }


    public DescribeIpv4LocationResponse queryPubIp(String ip) {
        if (accessKeyId == null) {
            accessKeyId = accessKeyId1;
        }
        if (accessKeySerect == null) {
            accessKeySerect = accessKeySerect1;
        }
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou"
                , accessKeyId
                , accessKeySerect);
        IAcsClient client = new DefaultAcsClient(profile);
        DescribeIpv4LocationRequest request = new DescribeIpv4LocationRequest();
        request.setIp(ip);
        try {
            DescribeIpv4LocationResponse response = client.getAcsResponse(request);
            return response;
        } catch (Exception e) {
            log.error("错误返回 :{}",e);
        }

        return null;
    }

    public Document getXml() throws Exception {
        File file = new File(filePathLoc + File.separator + "LocList.xml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document parse = documentBuilder.parse(file);
        return parse;
    }

    public void getCountry(List<String> valList, Document parse) {
        NodeList moduleKey = parse.getElementsByTagName("CountryRegion");
        for (int i = 0; i < moduleKey.getLength(); i++) {
            Node item = moduleKey.item(i);
            NamedNodeMap attributes = item.getAttributes();
            Node item1 = attributes.item(1);
            String nodeValue = item1.getNodeValue();
            valList.add(nodeValue);
        }
    }

    public void getState(List<String> valList, Document parse, QueryLocAddressParam param) {
        NodeList moduleKey = parse.getElementsByTagName("State");
        for (int i = 0; i < moduleKey.getLength(); i++) {
            Node item = moduleKey.item(i);
            NamedNodeMap attributes = item.getAttributes();
            Node states = attributes.item(1);
            Node parentNode = item.getParentNode();
            NamedNodeMap attributes1 = parentNode.getAttributes();
            Node countrys = attributes1.item(1);
            String countrysValue = countrys.getNodeValue();
            if (states != null) {
                String stateValue = states.getNodeValue();
                if (countrysValue.equals(param.getCountry())) {
                    valList.add(stateValue);
                }
            }
        }
    }

    public void getCity(List<String> valList, Document parse, QueryLocAddressParam param) {
        NodeList moduleKey = parse.getElementsByTagName("City");
        for (int i = 0; i < moduleKey.getLength(); i++) {
            Node item = moduleKey.item(i);
            NamedNodeMap attributes = item.getAttributes();
            Node city = attributes.item(1);
            Node parentNode = item.getParentNode();
            NamedNodeMap attributes1 = parentNode.getAttributes();
            Node state = attributes1.item(1);
            Node parentNode1 = parentNode.getParentNode();
            NamedNodeMap attributes2 = parentNode1.getAttributes();
            Node country = attributes2.item(1);
            if (country != null && state != null) {
                if (country.getNodeValue().equals(param.getCountry()) && state.getNodeValue().equals(param.getState())) {
                    valList.add(city.getNodeValue());
                }
                //国外的城市 没有省 直接是城市
            } else if (country != null && state == null) {
                if (country.getNodeValue().equals(param.getCountry())) {
                    valList.add(city.getNodeValue());
                }
            }
        }
    }

    public void getRegion(List<String> valList, Document parse, QueryLocAddressParam param) {
        NodeList moduleKey = parse.getElementsByTagName("Region");
        for (int i = 0; i < moduleKey.getLength(); i++) {
            Node item = moduleKey.item(i);
            NamedNodeMap attributes = item.getAttributes();
            Node region = attributes.item(1);
            Node parentNode = item.getParentNode();
            NamedNodeMap attributes1 = parentNode.getAttributes();
            Node city = attributes1.item(1);
            Node parentNode1 = parentNode.getParentNode();
            NamedNodeMap attributes2 = parentNode1.getAttributes();
            Node state = attributes2.item(1);
            Node parentNode2 = parentNode1.getParentNode();
            NamedNodeMap attributes3 = parentNode2.getAttributes();
            Node country = attributes3.item(1);
            if (country != null && state != null && city != null) {
                if (country.getNodeValue().equals(param.getCountry()) && state.getNodeValue().equals(param.getState()) && city.getNodeValue().equals(param.getCity())) {
                    valList.add(region.getNodeValue());
                }
            }
        }
    }


    private void insertLinkLabel(AddUpdateIpAddressManageParam auParam) {
        if (null != auParam.getLabels() && auParam.getLabels().size() > 0) {
            //下拉框和文本框放在同一个字段上保存
            List<LinkLabel> linkLabels = auParam.getLabels();
            for (LinkLabel linkLabel : linkLabels) {
                if (null != linkLabel.getValue()) {
                    if (linkLabel.getInputFormat() == 2) {
                        String value = linkLabel.getValue();
                        Date dateValue = MWUtils.strToDate(value, MWDateConstant.NORM_DATE);
                        linkLabel.setDateTagboard(dateValue);
                    } else if (linkLabel.getInputFormat() == 3) {
                        linkLabel.setTagboard(String.valueOf(linkLabel.getDropKey()));
                    } else {
                        linkLabel.setTagboard(linkLabel.getValue());
                    }
                }
                linkLabel.setLinkId(auParam.getId());
            }
            mwIpAddressManageTableDao.insertLabelLink(linkLabels);
        }
    }

    public List removeDuplicate(List list) {
        List listTemp = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            if (!listTemp.contains(list.get(i))) {
                listTemp.add(list.get(i));
            }
        }
        return listTemp;
    }

    //添加ip地址管理  右侧table页子表数据
    private void addIpAddressManageListTable(AddUpdateIpAddressManageParam auParam) throws Exception {
        //根据auParam 获取所有的ip地址集合(去重)
        auParam.setInclude(0);
        HashSet<String> ipList = getIpAddressList(auParam);
        //IP地址清单去重 掩码 为24-32区间
        String dimIp = "";
        if (!ipList.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            String var = (String) ipList.toArray()[0];
            String[] split = var.split("\\.");
            dimIp = stringBuilder.append(split[0]).append(".").append(split[1]).append(".").append(split[2]).toString();
        }

        //
        Set<String> ipLists = ipListRemove(ipList, dimIp,auParam.getSignId());


        //根据ip地址集合  添加ip地址管理的 table页数据（子表数据）
        List<AddUpdateIpAddressManageListParam> listSontable = new ArrayList<>();
        Iterator<String> it = ipLists.iterator();
        while (it.hasNext()) {
            String ip = it.next();
            AddUpdateIpAddressManageListParam entity = new AddUpdateIpAddressManageListParam();
            entity.setIpAddress(ip);
            entity.setLinkId(auParam.getId());
            entity.setCreator(iLoginCacheInfo.getLoginName());
            entity.setModifier(iLoginCacheInfo.getLoginName());
            entity.setInterval(auParam.getTiming());
            entity.setSignId(auParam.getSignId());
            listSontable.add(entity);
        }
        //再开一个线程跑
        new Thread(() -> {
            Iterator<String> its = ipLists.iterator();

            List<AddPubIpAddressParam> pubList = new ArrayList<>();
            while (its.hasNext()) {
                String ip = its.next();
//                DescribeIpv4LocationResponse ipv4Location = queryPubIp(ip);
                AddPubIpAddressParam param = new AddPubIpAddressParam();
//                String country = ipv4Location.getCountry();
//                if ("局域网".equals(country)) {
//                    continue;
//                }
                param.setLinkId(auParam.getId());
                param.setIpAddress(ip);
                param.setIpType("IPV4");
                param.setCountry(auParam.getCountry());
                param.setState(auParam.getState());
                param.setCity(auParam.getCity());
                param.setRegion(auParam.getRegion());
                param.setLongitude(auParam.getLongitude());
                param.setLatitude(auParam.getLatitude());
                //mwIpAddressManageTableDao.addPubIP(param);
                pubList.add(param);
            }
            ArrayList<AddPubIpAddressParam> collect = pubList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparing(f -> f.getLatitude() + f.getLongitude()))
            ), ArrayList::new));
            mwIpAddressManageTableDao.addPubIP(collect);
        }).start();

        //初始化ip_state 并保存
        if (listSontable.size() > 0) {
            for (AddUpdateIpAddressManageListParam param : listSontable) {
                param.setOnline(0);
                param.setIpState(0);
            }
            mwIpAddressManageListTableDao.insertBatch(listSontable);
            try {
                auParam.setInclude(1);
                HashSet<String> ipAddressList = getIpAddressList(auParam);
                List<String> strings = new ArrayList<>(ipAddressList);
                mwIpAddressManageListTableDao.updateIpIncludecollect(1,strings);
            } catch (Exception e) {
                log.error("错误返回 :{}",e);
            }
        } else {
            throw new Exception("该IP地址已存在,请勿重复添加！");
        }

    }



    private void addIpAddressManageListTable1(AddUpdateIpAddressManageParam auParam) throws Exception {
        //根据auParam 获取所有的ip地址集合(去重)
        auParam.setInclude(0);
        HashSet<String> ipList = getIpAddressList(auParam);

        //IP地址去重 16-24区间
        String dimIp = "";
        if (!ipList.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            String var = (String) ipList.toArray()[0];
            String[] split = var.split("\\.");
            dimIp = stringBuilder.append(split[0]).append(".").append(split[1]).append(".").append(split[2]).toString();
        }
        List<String> allIp = mwIpAddressManageListTableDao1.selectAllIpLIst(dimIp,auParam.getSignId());

        Set<String> ipLists = ipAddressesRemove(ipList, allIp);
        if (ipLists.size() == 0) {
            //删除IP地址段
            mwIpAddressManageTableDao1.deleteByIpaddresses(auParam.getIpAddresses());
        }
        //根据ip地址集合  添加ip地址管理的 table页数据（子表数据）
        List<AddUpdateIpAddressManageListParam> listSontable = new ArrayList<>();
        Iterator<String> it = ipLists.iterator();
        while (it.hasNext()) {
            String ip = it.next();
            AddUpdateIpAddressManageListParam entity = new AddUpdateIpAddressManageListParam();
            entity.setIpAddress(ip);
            entity.setLinkId(auParam.getId());
            entity.setCreator(iLoginCacheInfo1.getLoginName());
            entity.setModifier(iLoginCacheInfo1.getLoginName());
            entity.setInterval(auParam.getTiming());
            listSontable.add(entity);
        }
        //再开一个线程跑
        new Thread(() -> {
            Iterator<String> its = ipLists.iterator();

            List<AddPubIpAddressParam> pubList = new ArrayList<>();
            while (its.hasNext()) {
                String ip = its.next();
//                DescribeIpv4LocationResponse ipv4Location = queryPubIp(ip);
                AddPubIpAddressParam param = new AddPubIpAddressParam();
//                String country = ipv4Location.getCountry();
//                if ("局域网".equals(country)) {
//                    continue;
//                }
                param.setLinkId(auParam.getId());
                param.setIpAddress(ip);
                param.setIpType("IPV4");
                param.setCountry(auParam.getCountry());
                param.setState(auParam.getState());
                param.setCity(auParam.getCity());
                param.setRegion(auParam.getRegion());
                param.setLongitude(auParam.getLongitude());
                param.setLatitude(auParam.getLatitude());
                pubList.add(param);
            }
            ArrayList<AddPubIpAddressParam> collect = pubList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparing(f -> f.getLatitude() + f.getLongitude()))
            ), ArrayList::new));
            mwIpAddressManageTableDao1.addPubIP(collect);
        }).start();

        //初始化ip_state 并保存
        if (listSontable.size() > 0) {
            listSontable.forEach(data -> {
                data.setIpState(0);
                data.setOnline(0);
            });
            mwIpAddressManageListTableDao1.insertBatch(listSontable);
            try {
                auParam.setInclude(1);
                HashSet<String> ipAddressList = getIpAddressList(auParam);
                List<String> strings = new ArrayList<>(ipAddressList);
                mwIpAddressManageTableDao1.updateIpIncludecollect(strings);
            } catch (Exception e) {
                log.error("错误返回 :{}",e);
            }
        } else {
            throw new Exception("ip地址清单已存在");
        }

    }

    //根据auParam 获取所有的ip地址集合(去重)
    private HashSet<String> getIpAddressList(AddUpdateIpAddressManageParam auParam) throws Exception {
        HashSet<String> list = new HashSet<>();
        List<String> ips = null;
        //获取  ip地址地址段  内所有ip
        String ipAddresses = auParam.getIpAddresses();
        if (ipAddresses != null && !ipAddresses.equals("")) {
            String[] str = ipAddresses.split("/");
            if (auParam.getInclude() == null) {
                ips = IpV4Util.parseIpMaskRangeInclude(str[0], str[1], 0, 256);
            } else if (auParam.getInclude() == 1) {
                ips = IpV4Util.parseIpMaskRangeNotInclude(str[0], str[1], 0, 256);
            } else {
                ips = IpV4Util.parseIpMaskRangeInclude(str[0], str[1], 0, 256);
            }
            list.addAll(ips);
        }

        return list;
    }

    /*private void addDetailRule(AddUpdateIpAddressManageParam auParam) throws Exception{
        //添加ip地址管理的  ip范围
        if (null != auParam.getIpRange() && auParam.getIpRange().size() > 0) {
            auParam.getIpRange().forEach(mwIpRang1DTO -> mwIpRang1DTO.setLinkId(auParam.getId()));
            mwIpAddressManageTableDao.createIpRang(auParam.getIpRange());
        }

        //添加ip地址管理的  地址段
        if (null != auParam.getIpsubnets() && auParam.getIpsubnets().size() > 0) {
            auParam.getIpsubnets().forEach(ipAddress1DTO -> ipAddress1DTO.setLinkId(auParam.getId()));
            mwIpAddressManageTableDao.createIpAddresses(auParam.getIpsubnets());
        }

       *//* //添加ip地址管理的  地址清单信息
        if (null != auParam.getIpAddressList1DTO() && auParam.getIpAddressList1DTO().size() > 0) {
            auParam.getIpAddressList1DTO().forEach(ipAddressList1DTO -> ipAddressList1DTO.setId(auParam.getId()));
            mwIpAddressManageTableDao.createIpAddresslist(auParam.getIpAddressList1DTO());
        }*//*

    }*/

    /**
     * 添加负责人，用户组，机构 权限关系
     *
     * @param auParam
     */
    private void addMapperAndPerm(AddUpdateIpAddressManageParam auParam) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(auParam.getGroupIds())  //用户组
                .userIds(auParam.getPrincipal())  //责任人
                .orgIds(auParam.getOrgIds())      //机构
                .typeId(String.valueOf(auParam.getId())) //数据主键
                .type(DataType.IP.getName())        //IP
                .desc(DataType.IP.getDesc()).build(); //IP地址管理
        ArrayList<Integer> idList = new ArrayList<>();
        //递归获取所有分组下的IP地址段的id
        getId(auParam.getId(), idList);

        mwCommonService.extendPerm(insertDto, idList);
    }

    public void getId(Integer Id, List<Integer> idlist) {
        QueryIpAddressManageParam param = new QueryIpAddressManageParam();
        param.setParentId(Id);
        try {
            Reply reply = selectList(param);
            PageInfo data = (PageInfo) reply.getData();
            List<IpAddressManageTableParam> list = data.getList();
            for (IpAddressManageTableParam var :
                    list) {
                Integer id = var.getId();
                idlist.add(id);
                if ("grouping".equalsIgnoreCase(var.getType())) {
                    getId(var.getId(), idlist);
                }


            }
        } catch (Exception e) {
            log.info("获取下级id失败");
        }

    }

    private void addMapperAndPerm1(AddUpdateIpAddressManageParam auParam) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(auParam.getGroupIds())  //用户组
                .userIds(auParam.getPrincipal())  //责任人
                .orgIds(auParam.getOrgIds())      //机构
                .typeId(String.valueOf(auParam.getId())) //数据主键
                .type(DataType.IP.getName())        //IP
                .desc(DataType.IP.getDesc()).build(); //IP地址管理
        mwCommonService1.addMapperAndPerm(insertDto);
    }

    /**
     * 删除负责人，用户组，机构 权限关系
     *
     * @param auParam
     */
    private void deleteMapperAndPerm(AddUpdateIpAddressManageParam auParam) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeId(String.valueOf(auParam.getId()))
                .type(DataType.IP.getName())
                .build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    private void deleteMapperAndPerm(IpAddressManageTableParam auParam) {
        DeleteDto deleteDto = DeleteDto.builder()
                .typeId(String.valueOf(auParam.getId()))
                .type(DataType.IP.getName())
                .build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }


    private void conver(AddUpdateIpAddressManageParam auParam) {
        if (auParam.getType().equals("iPaddresses")) {
            auParam.setLeaf(1);
        } else {
            auParam.setLeaf(0);
        }

        //ip地址段设置
        if (null != auParam.getIpAddresses() && !auParam.getIpAddresses().equals("")) {
            MwIpAddresses1DTO a = new MwIpAddresses1DTO();
            String str = auParam.getIpAddresses().trim();
            String[] strings = str.split("/");
            a.setIp(strings[0]);
            a.setCidr(strings[1]);

            //设置子网掩码
            String mask = getMaskByCidr(Integer.parseInt(strings[1]));
            auParam.setMask(mask);

            a.setIpAddresses(str);

            List<MwIpAddresses1DTO> li = new ArrayList<MwIpAddresses1DTO>();
            li.add(a);
            auParam.setIpsubnets(li);

        }

    }

    /**
     * @describe 根据cidr计子网掩码
     */
    private String getMaskByCidr(int cidr) {
        if (cidr > 0 && cidr <= 32) {
            char[] chars = new char[32];
            for (int i = 0; i < chars.length; i++) {
                if (i < cidr) {
                    chars[i] = '1';
                } else {
                    chars[i] = '0';
                }
            }
            String s1 = "";
            String s2 = "";
            String s3 = "";
            String s4 = "";
            for (int i = 0; i < chars.length; i++) {
                if (8 * 0 <= i && i < 8 * 1) {
                    s1 += chars[i];
                } else if (8 * 1 <= i && i < 8 * 2) {
                    s2 += chars[i];
                } else if (8 * 2 <= i && i < 8 * 3) {
                    s3 += chars[i];
                } else if (8 * 3 <= i && i < 8 * 4) {
                    s4 += chars[i];
                }
            }
            String mask = Integer.parseInt(s1, 2) + "." + Integer.parseInt(s2, 2)
                    + "." + Integer.parseInt(s3, 2) + "." + Integer.parseInt(s4, 2);
            return mask;
        } else {
            return "请输入合理的ip地址段cidr";
        }
    }

    private String IPStaet(String network){

        String arr[] = network.split("/");
        String ip = arr[0];
        String mask = arr[1];
        int inetMask = Integer.parseInt(mask);

        //子网掩码为1占了几个字节
        int num1 = inetMask / 8;
        //子网掩码的补位位数
        int num2 = inetMask%8;
        int array[] = new int[4];
        for (int i = 0; i < num1; i++) {
            array[i] = 255;
        }
        for (int i = num1; i < 4; i++) {
            array[i] = 0;
        }
        for (int i = 0; i < num2; num2--) {
            array[num1] += Math.pow(2, 8-num2);
        }
        String netMask =  array[0] + "." + array[1] + "." + array[2] + "." + array[3];


        String lowAddr = "";
        int ipArray[] = new int[4];
        int netMaskArray[] = new int[4];
        if(4 != ip.split("\\.").length || "" == netMask){

        }
        String ipinfo = ip;
        for (int i = 0; i <4; i++) {
            ipArray[i] = Integer.parseInt(ipinfo.split("\\.")[i]);
            String ipAddr= ipinfo.replaceAll("\n", "");
            ipArray[i] = Integer.parseInt(ipAddr.split("\\.")[i]);
            netMaskArray[i] = Integer.parseInt(netMask.split("\\.")[i]);
            if(ipArray[i] > 255 || ipArray[i] < 0 || netMaskArray[i] > 255 || netMaskArray[i] < 0){

            }
            ipArray[i] = ipArray[i]&netMaskArray[i];
        }
        //构造最小地址
        for (int i = 0; i < 4; i++){
            if(i == 3){
                ipArray[i] = ipArray[i] ;
            }
            if ("" == lowAddr){
                lowAddr +=ipArray[i];
            } else{
                lowAddr += "." + ipArray[i];
            }
        }

            return  lowAddr+"/"+mask;
    }

    @Override
    public PageInfo<IpManageTree> countFenOrHui(Date StartTime, Date EndTime, Integer pageNumber, Integer pageSize) {
        List<IpManageTree> ipManageTrees = mwIpAddressManageListTableDao.getAllManage(0);
        List<IpManageTree> iprelationTree = groupIpManage(ipManageTrees,0);
/*
        List<IpManageTree> ipTreeManage = getTreeManage(StartTime,  EndTime,  pageNumber,  pageSize,iprelationTree);
*/

        PageHelper.startPage(pageNumber, pageSize);
        List<IpManageTree> ipManageTreeList = new ArrayList<>();
        ipManageTreeList = mwIpAddressManageListTableDao.selectCountIpOper(StartTime,EndTime,iprelationTree);
        List<IpManageTree> ipManageTreesCha = getChaIpList(StartTime,EndTime,iprelationTree);
        for (IpManageTree ipManageTree:ipManageTreeList) {
            for (IpManageTree ipManageTreegroup:iprelationTree) {
                if (ipManageTree.getId().equals(ipManageTreegroup.getId())){
                    ipManageTree.setParentIds(ipManageTreegroup.getParentIds());
                    ipManageTree.setParentId(ipManageTreegroup.getParentId());
                    ipManageTree.setLabels(ipManageTreegroup.getLabels());
                    ipManageTree.setLabel(ipManageTreegroup.getLabel());
                }
            }
            for (IpManageTree ipManageTreegroup:ipManageTreesCha) {
                if (ipManageTree.getId().equals(ipManageTreegroup.getId()) ){
                    ipManageTree.setCountCha(ipManageTree.getCountCha()+ipManageTreegroup.getCountCha());
                }
            }
        }
        PageInfo<IpManageTree> pageInfo = new PageInfo<>(ipManageTreeList);
        pageInfo.setList(ipManageTreeList);
        return pageInfo;
    }



    @Override
    public List<IpManageTree> getTreeFrist(Date StartTime, Date EndTime, Integer pageNumber, Integer pageSize,Integer id) {
        List<IpManageTree> ipManageTrees = mwIpAddressManageListTableDao.getAllManage(0);
        List<IpManageTree> iprelationTree = groupIpManage(ipManageTrees,0);
        List<IpManageTree> ipTreeManage = getTreeManage(StartTime,  EndTime,  pageNumber,  pageSize,iprelationTree,id);
        return ipTreeManage;
    }

    private List<IpManageTree> getTreeManage(Date StartTime, Date EndTime, Integer pageNumber, Integer pageSize,  List<IpManageTree> iprelationTree,Integer id) {
        List<IpManageTree> ipManageTreeList = new ArrayList<>();
        List<IpManageTree> ipManageTrees = new ArrayList<>();
        ipManageTreeList = mwIpAddressManageListTableDao.selectCountIpOper(StartTime,EndTime,iprelationTree);
        List<IpManageTree> ipManageTreesCha = getChaIpList(StartTime,EndTime,iprelationTree);
        for (IpManageTree ipManageTree:ipManageTreeList) {
            for (IpManageTree ipManageTreegroup:iprelationTree) {
                if (ipManageTree.getId().equals(ipManageTreegroup.getId()) ){
                    ipManageTree.setParentIds(ipManageTreegroup.getParentIds());
                    ipManageTree.setParentId(ipManageTreegroup.getParentId());
                    ipManageTree.setLabels(ipManageTreegroup.getLabels());
                    ipManageTree.setLabel(ipManageTreegroup.getLabel());
                }
            }
            for (IpManageTree ipManageTreegroup:ipManageTreesCha) {
                if (ipManageTree.getId().equals(ipManageTreegroup.getId()) ){
                    ipManageTree.setCountCha(ipManageTreegroup.getCountCha());
                }
            }
        }

        Integer index =  0 ;
        for (IpManageTree ipManageTree:ipManageTreeList) {
            boolean have = false;
            for (IpManageTree ipmanage:ipManageTrees) {
                if (ipManageTree.getParentIds().size()>index+1) {
                    if (ipManageTree.getParentIds().get(index + 1).equals(ipmanage.getId())) {
                        ipmanage = changeNum(ipmanage, ipManageTree);
                        have = true;
                    }
                }
            }
            if (!have){
                if (id==0?true:ipManageTree.getParentIds().contains(id)){
                    if (index==0&&id!=0){
                        index = ipManageTree.getParentIds().indexOf(id);
                    }
                    IpManageTree ipManage = new IpManageTree();
                    if (ipManageTree.getParentIds().size()>=index+2){
                        ipManage.setLabel(ipManageTree.getLabels().get(index));
                        ipManage.setId(ipManageTree.getParentIds().get(index+1));
                    }else {
                        ipManage.setLabel(ipManageTree.getLabels().get(index)+"(地址段非分组)");
                        ipManage.setId(0);
                    }
                    ipManage = changeNum(ipManage,ipManageTree);
                    ipManageTrees.add(ipManage);
                }
            }

        }
        return  ipManageTrees;
    }

    private List<IpManageTree> getChaIpList(Date startTime, Date endTime, List<IpManageTree> iprelationTree) {
        List<IpManageTree> ipManageTreeList = new ArrayList<>();
        List<Map<String, Object>> mapListGroup = ipAddressManagePowerTableDao.selectIPdisOperHis(-1,0, null,null,null,null,"3",null,null,null,null, DateUtils.formatDateTime(startTime), DateUtils.formatDateTime(endTime),null);
        List<String> strings = new ArrayList<>();
        for (Map map:mapListGroup) {
            List<String> stringsIp = Arrays.asList(map.get(IPADDRESS_RELATION).toString().replace("[","").replace("]","").split(","));
           strings.addAll(stringsIp);
        }
        if (strings.size()>0){
            ipManageTreeList = ipAddressManagePowerTableDao.selectParenIds(strings);
        }
        for (IpManageTree ipManageTree:ipManageTreeList) {
            for (String s:strings) {
               if (ipManageTree.getLabel().equals(s)){
                   ipManageTree.setCountCha(ipManageTree.getCountCha()+1);
               }
            }
        }
        return ipManageTreeList;
    }

    private IpManageTree changeNum(IpManageTree ipManage, IpManageTree ipManageTree) {
        if (ipManageTree.getStatusPrecent()==null){
            ipManageTree.setStatusPrecent(new Double("0.0"));
        }
        if (ipManageTree.getStatusPrecent()>=80){
            ipManage.setGtEight(ipManage.getGtEight()+1);
        }
        if (ipManageTree.getStatusPrecent()<=50){
            ipManage.setLtFri(ipManage.getLtFri()+1);
        }
        if (ipManageTree.getStatusPrecent()>50 && ipManageTree.getStatusPrecent()<80 ){
            ipManage.setGtFri(ipManage.getGtFri()+1);
        }
        return ipManage;
    }

    private List<IpManageTree> groupIpManage(List<IpManageTree> ipManageTrees,Integer parent) {
        List<IpManageTree> iprelationTreesIpaddressListNew = new ArrayList<>();
        List<IpManageTree> iprelationTreesGroup = new ArrayList<>();
        List<IpManageTree> iprelationTreesIpaddressList = new ArrayList<>();
        for (IpManageTree ipManageTree:ipManageTrees) {
           if (ipManageTree.getType().equals(IP_GROUPING)){
               iprelationTreesGroup.add(ipManageTree);
           }else {
               iprelationTreesIpaddressList.add(ipManageTree);
           }
        }
        iprelationTreesIpaddressListNew = addParentIds(iprelationTreesGroup,iprelationTreesIpaddressList);


        return iprelationTreesIpaddressListNew;
    }

    private List<IpManageTree> addParentIds(List<IpManageTree> iprelationTreesGroup, List<IpManageTree> iprelationTreesIpaddressList) {
        List<IpManageTree> iprelationTreesIpaddressListNew = new ArrayList<>();
        for (IpManageTree ipManageTree:iprelationTreesIpaddressList) {
            if (ipManageTree.getParentId()==0){
                iprelationTreesIpaddressListNew.add(ipManageTree);
            }else {
                List<IpManageTree> ipManageTrees = new ArrayList<>();
                ipManageTrees=iprelationTreesGroup;
                iprelationTreesIpaddressListNew.add(groupParent(ipManageTrees,ipManageTree,ipManageTree.parentId));
            }
        }
        return iprelationTreesIpaddressListNew;
    }

    private IpManageTree groupParent(List<IpManageTree> iprelationTreesGroup, IpManageTree ipManageTree,Integer parentId) {
        List<IpManageTree> IPGroup = new ArrayList<>();
        IPGroup = iprelationTreesGroup;
        for (IpManageTree newipManageTree:IPGroup) {
            if (newipManageTree.getId().equals(parentId)){
                if (newipManageTree.getParentId()!=0){
                    IpManageTree ipManageTree1 = new IpManageTree();
                    ipManageTree1= newipManageTree;
                    ipManageTree1=groupParent(IPGroup,ipManageTree1,ipManageTree1.getParentId());
                    ipManageTree.getLabels().addAll(ipManageTree1.getLabels());
                    ipManageTree.getParentIds().addAll(ipManageTree1.getParentIds());
                    ipManageTree.getLabels().add(ipManageTree.getLabel());
                    ipManageTree.getParentIds().add(ipManageTree.getParentId());
                }else {
                    ipManageTree.getLabels().add(newipManageTree.getLabel());
                    ipManageTree.getParentIds().add(newipManageTree.getParentId());
                    ipManageTree.getLabels().add(ipManageTree.getLabel());
                    ipManageTree.getParentIds().add(ipManageTree.getParentId());
                }
                newipManageTree.setLabels(new ArrayList<>());
                newipManageTree.setParentIds(new ArrayList<>());
            }
        }
        return ipManageTree;
    }
}
