package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManageListTableDao;
import cn.mw.monitor.ipaddressmanage.dao.MwIpDictionaryTableDao;
import cn.mw.monitor.ipaddressmanage.dto.IpConfictDTO;
import cn.mw.monitor.ipaddressmanage.dto.LabelDTO;
import cn.mw.monitor.ipaddressmanage.dto.labelOb;
import cn.mw.monitor.ipaddressmanage.ipconflict.IpConflictManage;
import cn.mw.monitor.ipaddressmanage.model.IPConflictView;
import cn.mw.monitor.ipaddressmanage.param.*;
import cn.mw.monitor.ipaddressmanage.paramv6.AddUpdateIpv6ManageParam;
import cn.mw.monitor.ipaddressmanage.paramv6.QueryIpv6ManageParam;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManageScanService;
import cn.mw.monitor.ipaddressmanage.service.MwIpAddressManageService;
import cn.mw.monitor.ipaddressmanage.service.MwIpv6ManageService;
import cn.mw.monitor.service.license.service.LicenseManagementService;
import cn.mw.monitor.service.scan.dto.IPInfoDTO;
import cn.mw.monitor.service.user.api.MWMessageService;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWUser;
import cn.mw.monitor.service.user.model.ScanIpAddressManageQueueVO;
import cn.mw.monitor.weixin.util.AliyunApi;
import cn.mw.monitor.weixin.util.SendUnifiedInterFace;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api(value = "IP地址管理", tags = "IP地址管理")
public class MWIpAddressController extends BaseApiService {
    private AtomicBoolean isIpScanning = new AtomicBoolean(false);

    @Resource
    private MwIpAddressManageListTableDao mwIpAddressManageListTableDao;

    @Autowired
    private MwIpAddressManageService mwIpAddressManageService;

    @Autowired
    private MwIpAddressManageScanService mwIpAddressManageScanService;


    @Autowired
    private MwIpv6ManageService mwIpv6ManageService;

    @Autowired
    private SendUnifiedInterFace sendUnifiedInterFace;
    @Autowired
    private AliyunApi aliyunApi;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private LicenseManagementService licenseManagement;

    @Resource
    private MwIpDictionaryTableDao mwIpDictionaryTableDao;

    @Autowired
    private MWMessageService mwMessageService;

    @Autowired
    private MWUserCommonService mwUserCommonService;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private IpConflictManage ipConflictManage;

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6/editorBrowse")
    @ResponseBody
    @ApiOperation(value = "IPv6地址管理编辑查询")
    public ResponseBase ipv6QueryOne(@RequestBody QueryIpv6ManageParam qParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwIpv6ManageService.editorSelect(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6/browse")
    @ResponseBody
    @ApiOperation(value = "IPv6地址管理查询")
    public ResponseBase ipv6QueryList(@RequestBody QueryIpAddressManageParam qParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwIpv6ManageService.selectList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6/delete")
    @ResponseBody
    @ApiOperation(value = "IPv6地址管理删除")
    public ResponseBase ipv6Delete(@RequestBody AddUpdateIpv6ManageParam param) {
        Reply reply;
        try {
            reply = mwIpv6ManageService.delete(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6/editor")
    @ResponseBody
    @ApiOperation(value = "IPv6地址管理修改")
    public ResponseBase ipv6Update(@RequestBody @Valid AddUpdateIpv6ManageParam param) {
        Reply reply;
        try {
            reply = mwIpv6ManageService.update(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @ApiOperation(value = "新增IPv6地址管理")
    @PostMapping("/ipv6/create")
    @ResponseBody
    public ResponseBase ipv6Add(@RequestBody @Valid AddUpdateIpv6ManageParam param,
                                HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpv6ManageService.insert(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6List/export")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页导出")
    public void ipv6ListBatchExport(@RequestBody ExportIpAddressListParam uParam,
                                    HttpServletRequest request, RedirectAttributesModelMap model,
                                    HttpServletResponse response) {
        try {
            mwIpv6ManageService.batchExport(uParam, response);
        } catch (Throwable e) {
            log.error(e.toString());
        }
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6List/batchDelete")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页批量删除")
    public ResponseBase ipv6ListBatchDelete(@RequestBody AddUpdateIpAddressManageListParam uParam,
                                            HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpv6ManageService.batchDelete(uParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6List/batchUpdate")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页批量修改")
    public ResponseBase ipv6ListBatchUpdate(@RequestBody AddUpdateIpAddressManageListParam uParam,
                                            HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpv6ManageService.batchUpdate(uParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6List/addList")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页新增")
    public ResponseBase ipv6ListaddList(@RequestBody AddUpdateIpAddressManageListParam uParam,
                                            HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpv6ManageService.ipv6ListaddList(uParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @ApiOperation(value = "新增IPv6地址管理")
    @PostMapping("/ipv6list/browse")
    @ResponseBody
    public ResponseBase ipv6List(@RequestBody QueryIpAddressManageListParam param,
                                 HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpv6ManageService.selectSonList(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6Picture/browse")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页上方图形数据查询")
    public ResponseBase queryIPv6Picture(@RequestBody QueryIpv6ManageParam qParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpv6ManageService.selectPicture(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipv6List/getHisList")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页历史查询")
    public ResponseBase getIpv6HisList(@RequestBody AddUpdateIpAddressManageListParam parm,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpv6ManageService.getHisList(parm);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManageList/getHisList")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页历史查询")
    public ResponseBase getHisList(@RequestBody AddUpdateIpAddressManageListParam parm,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManageScanService.getHisList(parm);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("Pv6地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManageList/batchScan")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页批量扫描")
    public void batchScan(@RequestBody ScanIpAddressManageListParam parm,
                          HttpServletRequest request, RedirectAttributesModelMap model) {

        //获取当前用户的用户信息
        boolean isScaned = isIpScanning.compareAndSet(false, true);
        boolean crun = false;
        ScanIpAddressManageListParam paramTwo = new ScanIpAddressManageListParam();
        Integer userId = iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId();
        MWUser userInfo = new MWUser();
        //测试
        if (false) {
            /*  mwMessageService.createMessage("有任务正在执行",1,0);*/
            /*加入数据库执行任务 //并得知是否需要持续运行*/
            //怎加队列
            userInfo = (MWUser) mwUserCommonService.selectByUserId(userId).getData();
            mwIpAddressManageScanService.addScanQueue(JSONObject.toJSON(parm).toString(), userId, parm.getLinkId());
            mwMessageService.createMessage("当前扫描任务已经进入等待", 1, 0, userInfo);
            try {
                mwMessageService.createMessage(parm.getLinkId().toString(), 0, 0, userInfo);
            }catch (Exception e){

            }
            return;
        }
        else {
            if (request != null) {
                mwIpAddressManageScanService.addScanQueue(JSONObject.toJSON(parm).toString(), userId, parm.getLinkId());
                userInfo = (MWUser) mwUserCommonService.selectByUserId(userId).getData();
            } else {
                ScanIpAddressManageQueueVO scanIpAddressManageQueue = mwIpAddressManageScanService.selectqueue();
                if (scanIpAddressManageQueue != null) {
                    crun = true;
                    try{
                        parm = JSONObject.parseObject(scanIpAddressManageQueue.getParam(), ScanIpAddressManageListParam.class);
                    }catch (Exception e){
                        //System.out.println(e.toString());
                    }
                    userInfo = (MWUser) mwUserCommonService.selectByUserId(scanIpAddressManageQueue.getUserId()).getData();
                } else {
                        isIpScanning.compareAndSet(true, false);
                    return;
                }
            }
        }

        ScanIpAddressManageListParam finalParm = parm;
        try {
            Boolean isAll = parm.getIsAll();
            MWUser finalUserInfo = userInfo;
            boolean finalCrun = crun;
            mwMessageService.createMessage(parm.getLinkId().toString(), 0, 0, userInfo);
                if (isAll) {
                List<AddUpdateIpAddressManageListParam> list = mwIpAddressManageListTableDao.selectListByLinkId(parm.getLinkId());
                ExecutorService executor = Executors.newFixedThreadPool(1);
                Future<Integer> future = executor.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() {
                        mwIpAddressManageScanService.batchScanIp(list, finalParm.getLinkId(), finalUserInfo);
                        mwIpAddressManageScanService.updateAssetsType(finalParm.getLinkId());
                        isIpScanning.compareAndSet(true, false);
                        mwIpAddressManageListTableDao.updateScanTime(0,finalParm.getLinkId(),null);
                        ScanIpAddressManageQueueVO scanIpAddressManageQueue = mwIpAddressManageScanService.selectqueue();
                        if (scanIpAddressManageQueue != null) {
                            mwIpAddressManageScanService.deleteQueue(finalParm.getLinkId(), finalUserInfo.getUserId());
                            batchScan(paramTwo, null, model);
                        }
                        return 0;
                    }
                });
            } else {
                ExecutorService executor = Executors.newFixedThreadPool(1);
                Future<Integer> future = executor.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() {
                        mwIpAddressManageScanService.batchScanIp(finalParm.getUParam(), finalParm.getLinkId(), finalUserInfo);
                        List<AddUpdateIpAddressManageListParam> uParam = finalParm.getUParam();
                        List<Integer> ids = new ArrayList<>();
                        for (AddUpdateIpAddressManageListParam u:uParam) {
                            ids.add(u.getId());
                        }
                        if (uParam.size() > 0) {
                            AddUpdateIpAddressManageListParam addUpdateIpAddressManageListParam = uParam.get(0);
                            Integer linkIds = addUpdateIpAddressManageListParam.getLinkId();
                            mwIpAddressManageScanService.updateAssetsType(linkIds);
                        }
                        mwIpAddressManageListTableDao.updateScanTime(1,null,ids);
                        isIpScanning.compareAndSet(true, false);
                        ScanIpAddressManageQueueVO scanIpAddressManageQueue = mwIpAddressManageScanService.selectqueue();
                        if (scanIpAddressManageQueue != null) {
                            mwIpAddressManageScanService.deleteQueue(finalParm.getLinkId(), finalUserInfo.getUserId());
                            batchScan(paramTwo, null, model);

                        }
                        return 0;
                    }
                });
            }
        } catch (Throwable e) {
            ScanIpAddressManageQueueVO scanIpAddressManageQueue = mwIpAddressManageScanService.selectqueue();
            if (scanIpAddressManageQueue != null) {
                mwIpAddressManageScanService.deleteQueue(finalParm.getLinkId(), userInfo.getUserId());
                batchScan(paramTwo, null, model);
            }
            log.error("batchScan", e);

        }

    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManageList/canleBatchScan")
    @ResponseBody
    @ApiOperation(value = "取消扫描")
    public ResponseBase canleBatchScan(@RequestBody ScanIpAddressManageListParam parm,
                                       HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManageScanService.canleBatchScan(parm.getLinkId());
            mwIpAddressManageScanService.deleteQueue(parm.getLinkId(), iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        } finally {
            isIpScanning.compareAndSet(true, false);
        }
        return setResultSuccess(reply);
    }



    @PostMapping("/ipAddressManageList/batchScanList")
    @ResponseBody
    @ApiOperation(value = "IP地址批量扫描")
    public ResponseBase batchScanList(@RequestBody ScanIpAddressManageListParam parms,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            List<Integer> integers = parms.getLinkIds();
            for (Integer integer:integers) {
                parms.setLinkId(integer);
                parms.setIsAll(true);
                batchScan(parms,request,model);
            }

        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("Pv6地址管理报错",null);
        }
        return setResultSuccess("成功");
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManageList/export")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页导出")
    public void ipSonListBatchExport(@RequestBody ExportIpAddressListParam uParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model,
                                     HttpServletResponse response) {
        try {
            mwIpAddressManageService.batchExport(uParam, response);
        } catch (Throwable e) {
            log.error(e.toString());
        }
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManageList/batchDelete")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页批量删除")
    public ResponseBase ipSonListBatchDelete(@RequestBody AddUpdateIpAddressManageListParam uParam,
                                             HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManageService.batchDelete(uParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManageList/batchUpdate")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页批量修改")
    public ResponseBase ipSonListBatchUpdate(@RequestBody AddUpdateIpAddressManageListParam uParam,
                                             HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManageService.batchUpdate(uParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipaddressStatusHis/brow")
    @ResponseBody
    @ApiOperation(value = "ip端口历史查找")
    public ResponseBase ipaddressStatusHisBrow(@RequestBody AddIpaddresStatusHis uParam,
                                             HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManageService.ipaddressStatusHisBrow(uParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManagePicture/browse")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页上方图形数据查询")
    public ResponseBase queryPicture(@RequestBody IpAddressManageTableParam qParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManageService.selectPicture(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

/*    @MwPermit(moduleName = "ip_manage")*/
    @PostMapping("/ipAddressManageList/browse")
    @ResponseBody
    @ApiOperation(value = "IP地址管理table页数据查询")
    public ResponseBase querySonList(@RequestBody QueryIpAddressManageListParam qParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManageService.selectSnoList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManage/editorBrowse")
    @ResponseBody
    @ApiOperation(value = "IP地址管理编辑查询")
    public ResponseBase queryList1(@RequestBody QueryIpAddressManageParam qParam,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwIpAddressManageService.selectList1(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    //    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManage/browse")
    @ResponseBody
    @ApiOperation(value = "IP地址管理查询")
    public ResponseBase queryList(@RequestBody QueryIpAddressManageParam qParam,
                                  HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwIpAddressManageService.selectList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManage/delete")
    @ResponseBody
    @ApiOperation(value = "IP地址管理删除")
    public ResponseBase delete(@RequestBody AddUpdateIpAddressManageParam addUpdateIpAddressManageParam) {
        Reply reply;
        try {
            reply = mwIpAddressManageService.delete(addUpdateIpAddressManageParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }



    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManage/TreeeDelete")
    @ResponseBody
    @ApiOperation(value = "树状结构manage删除")
    public ResponseBase delete(@RequestBody List<AddUpdateIpAddressManageParam> addUpdateIpAddressManageParam) {
        Reply reply = null;
        try {
            AddUpdateIpAddressManageParam addUpdateIpAddressManageParam1 = new AddUpdateIpAddressManageParam();
            AddUpdateIpv6ManageParam addUpdateIpv6ManageParam = new AddUpdateIpv6ManageParam();
            Set<Integer> ipv4 = new HashSet<>();
            Set<Integer> ipv6 = new HashSet<>();
            addUpdateIpAddressManageParam.stream().forEach(e->{
                if (e.getIPv4()==1){
                    ipv6.add(e.getId());
                }else {
                    ipv4.add(e.getId());
                }
            });
            if (ipv4.size()>0){
                addUpdateIpAddressManageParam1.setIds(ipv4);
                reply = mwIpAddressManageService.delete(addUpdateIpAddressManageParam1);
                if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                    return setResultFail(reply.getMsg(), reply.getData());
                }
            }

            if (ipv6.size()>0){
                addUpdateIpv6ManageParam.setIds(ipv6);
                reply =  mwIpv6ManageService.delete(addUpdateIpv6ManageParam);;
                if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                    return setResultFail(reply.getMsg(), reply.getData());
                }
            }

        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManage/editor")
    @ResponseBody
    @ApiOperation(value = "IP地址管理修改")
    public ResponseBase update(@RequestBody @Valid AddUpdateIpAddressManageParam addUpdateIpAddressManageParam) {
        Reply reply;
        try {
            reply = mwIpAddressManageService.update(addUpdateIpAddressManageParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @ApiOperation(value = "新增IP地址管理")
    @PostMapping("/ipAddressManage/create")
    @ResponseBody
    public ResponseBase add(@RequestBody @Valid AddUpdateIpAddressManageParam param,
                            HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManageService.insert(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    /**
     * 下拉框查询
     */
    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManage/dropdown/browse")
    @ResponseBody
    public ResponseBase typeDropdownBrowse(@RequestBody IpDictionaryTableParam qsParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            List<IpDictionaryTableParam> result = mwIpDictionaryTableDao.selectListByType(qsParam);
            reply = Reply.ok(result);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }

        return setResultSuccess(reply);
    }

    /**
     * 标签下拉查询
     */
    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManage/labelDropdown/browse")
    @ResponseBody
    public ResponseBase labelDropdownBrowse(HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            List<LabelDTO> result = mwIpDictionaryTableDao.selectLabelList();
            reply = Reply.ok(result);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }

        return setResultSuccess(reply);
    }

    //使用状态下拉框
    @MwPermit(moduleName = "ip_manage")
    @GetMapping("/ipAddressManageList/ipstate/browse")
    @ResponseBody
    public ResponseBase labelIpStateBrowse() {
        Reply reply;
        try {
            List<labelOb> result = mwIpDictionaryTableDao.selectLabeipState();
            reply = Reply.ok(result);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "ip_manage")
    @ApiOperation(value = "获取地理位置接口")
    @PostMapping("/ipLocAddress/browse")
    @ResponseBody
    public ResponseBase getAddress(@RequestBody QueryLocAddressParam param,
                                   HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            reply = mwIpAddressManageService.queryLocAdress(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
    }
        return setResultSuccess(reply);
    }

    @PostMapping("/gitList/browse")
    @ResponseBody
    @ApiOperation(value = "git地图查询")
    public ResponseBase queryGitList(@RequestBody QueryIpAddressManageParam qParam,
                                     HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply = null;
        try {
            reply = mwIpAddressManageService.selectGitList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    //    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManage/hfivebrow/perform")
    @ResponseBody
    @ApiOperation(value = "Ip断网地址查询", tags = "Ip断网地址查询")
    public ResponseBase hfivebrow(HttpServletRequest request, @RequestBody QueryIpAddressManageParam qParam) {
        Reply reply = null;
        try {
            Map<String, Object> res = new HashMap<>();
            Map<String, Object> map = sendUnifiedInterFace.sreachIndex(null);
            PageInfo pageInfo = mwIpAddressManageService.selectKillIP(map, qParam);
            res.put("Page", pageInfo);
            res.put("select", map.get("select"));
            reply = Reply.ok(res);
//         reply = mwIpAddressManageService.selectList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    //    @MwPermit(moduleName = "ip_manage")
    @PostMapping("/ipAddressManage/hfivebrowhistory/perform")
    @ResponseBody
    @ApiOperation(value = "获取历史数据", tags = "获取历史数据")
    public ResponseBase hfivebrowhistory(HttpServletRequest request, @RequestBody QueryIpAddressManageParam qParam) {
        Reply reply = null;
        try {
            Map<String, Object> res = new HashMap<>();
            Map<String, Object> map = sendUnifiedInterFace.sreachIndex(null);

            PageInfo pageInfo = mwIpAddressManageService.selectKillIPHistory(map, qParam);
            res.put("Page", pageInfo);
            res.put("select", map.get("select"));
            reply = Reply.ok(res);
//         reply = mwIpAddressManageService.selectList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/ipAddressManage/getIpaddressList/brow")
    @ResponseBody
    @ApiOperation(value = "查询所有地址段接口", tags = "查询所有地址段接口")
    public ResponseBase getIpaddressList() {
        Reply reply = null;
        try {
            reply = mwIpAddressManageService.getIpaddressList();
//         reply = mwIpAddressManageService.selectList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("IP地址管理报错",null);
            }
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/ipAddressManage/addDisConnetion/perform")
    @ResponseBody
    @ApiOperation(value = "增加断网ip", tags = "增加断网ip")
    public ResponseBase addDisConnetion(HttpServletRequest request, @RequestBody AddDisConnection qParam) {


        Reply reply = null;
        String code = qParam.getCode();
        try {
            if (!sendUnifiedInterFace.IsIPAddress(qParam.getIp(), qParam.getDaemon())) {
                return setResultPermit("数据类型非IP地址格式！！", true);
            }
            Boolean ipType = sendUnifiedInterFace.isIPv4Address(qParam.getIp());
            Map<String, String> pageInfo = mwIpAddressManageService.serchIpPermit(qParam, ipType);
            if (pageInfo.get("permit").equals("false")) {
                return setResultPermit("对不起，当前ip你没有数据查看和更改权限", true);
            }
            if (pageInfo.get("havePhone").equals("true") && (code != null && !code.equals(""))) {
                String codeTrue = redisTemplate.opsForValue().get("code" + pageInfo.get("phone")).toString();
                if (codeTrue.equals(code)) {
                    Map<String, String> res = sendUnifiedInterFace.addIndex(qParam.getIp(), qParam.getDaemon(), qParam.getMask(), qParam.getDst(), qParam.getDescription());

                } else {
                    return setResultGetCode("手机验证码错误", true);
                }
            } else if (pageInfo.get("havePhone").equals("true") && (code == null || code.equals(""))) {
                return setResultGetCode("发送验证码", true);
            } else {
                return setResultPermit("用户手机未在猫维系统注册", true);
            }
            mwIpAddressManageService.updateIPConnection(qParam, "add");
            reply = Reply.ok(mwIpAddressManageService.sendWx(qParam.getIp(), "新增断网"));
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/ipAddressManage/DisConnetion/perform")
    @ResponseBody
    @ApiOperation(value = "断网", tags = "断网")
    public ResponseBase DisConnetion(HttpServletRequest request, @RequestBody AddDisConnection qParam) {
        Reply reply = null;
        String code = qParam.getCode();
        try {
            Boolean ipType = sendUnifiedInterFace.isIPv4Address(qParam.getIp());
            Map<String, String> pageInfo = mwIpAddressManageService.serchIpPermit(qParam, ipType);
            if (pageInfo.get("permit").equals("false")) {
                return setResultPermit("对不起，当前ip你没有数据查看和更改权限", true);
            }
            if (pageInfo.get("havePhone").equals("true") && (code != null && !code.equals(""))) {
                String codeTrue = redisTemplate.opsForValue().get("code" + pageInfo.get("phone")).toString();
                if (codeTrue.equals(code)) {
                    Map<String, String> res = sendUnifiedInterFace.addIndex(qParam.getIp(), qParam.getDaemon(), qParam.getMask(), qParam.getDst(), qParam.getDescription());

                } else {
                    return setResultGetCode("手机验证码错误", true);
                }
            } else if (pageInfo.get("havePhone").equals("true") && (code == null || code.equals(""))) {
                return setResultGetCode("发送验证码", true);
            } else {
                return setResultPermit("用户手机未在猫维系统注册", true);
            }
            mwIpAddressManageService.updateIPConnection(qParam, "add");
            reply = Reply.ok(mwIpAddressManageService.sendWx(qParam.getIp(), "断网"));
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }


    @PostMapping("/ipAddressManage/connetion/perform")
    @ResponseBody
    @ApiOperation(value = "恢复网络", tags = "恢复网络")
    public ResponseBase connetion(HttpServletRequest request, @RequestBody AddDisConnection qParam) {
        Reply reply = null;
        String code = qParam.getCode();
        try {
            Boolean ipType = sendUnifiedInterFace.isIPv4Address(qParam.getIp());
            Map<String, String> pageInfo = mwIpAddressManageService.serchIpPermit(qParam, ipType);
            if (pageInfo.get("permit").equals("false")) {
                return setResultPermit("对不起，当前ip你没有数据查看和更改权限", true);
            }
            if (pageInfo.get("havePhone").equals("true") && (code != null && !code.equals(""))) {
                String codeTrue = redisTemplate.opsForValue().get("code" + pageInfo.get("phone")).toString();
                if (codeTrue.equals(code)) {
                    Map<String, String> res = sendUnifiedInterFace.disenable(qParam.getIndex());
                } else {
                    return setResultGetCode("验证码错误请重新发送", true);
                }
            } else if (pageInfo.get("havePhone").equals("true") && (code == null || code.equals(""))) {
                return setResultGetCode("发送验证码", true);
            } else {
                return setResultPermit("用户手机未在猫维系统注册", true);
            }
            mwIpAddressManageService.updateIPConnection(qParam, "delete");
            reply = Reply.ok(mwIpAddressManageService.sendWx(qParam.getIp(), "恢复"));
        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);

        }
        return setResultSuccess(reply);
    }


    @PostMapping("/ipAddressManage/sendSms")
    @ResponseBody
    @ApiOperation(value = "发送手机验证码", tags = "发送手机验证码")
    public ResponseBase sendSms(HttpServletRequest request) {
        Reply reply = null;
        try {
            Map<String, String> pageInfo = mwIpAddressManageService.sendSms();
            if (pageInfo.get("havePhone").equals("true")) {
                String Phone = pageInfo.get("phone");
                Integer key = (int) ((Math.random() * 9 + 1) * 1000);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(aliyunApi.getDateName(), key.toString());
                aliyunApi.send(Phone, jsonObject.toString());
                redisTemplate.opsForValue().set("code" + Phone, key, 180, TimeUnit.SECONDS);
            } else {
                return setResultPermit("当前手机未在系统内注册", true);
            }


        } catch (Throwable e) {
            log.error(e.toString());
            return setResultFail("IP地址管理报错",null);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/ipAddressManage/ipconflict/editor")
    @ResponseBody
    @ApiOperation(value = "查看ip冲突详情", tags = "查看ip冲突详情")
    public ResponseBase ipConflictView(@RequestBody IpConflictParam ipConflictParam) {

        IPConflictView ipConflictView = new IPConflictView();
        try {
            IPInfoDTO ipInfoDTO = ipConflictManage.getIpConflict(ipConflictParam.getIp());
            if(null != ipInfoDTO){
                ipConflictView.extractFrom(ipInfoDTO);
            }
        }catch (Exception e){
            log.error("ipConflictView" ,e);
        }

        return setResultSuccess(Reply.ok(ipConflictView));
    }

    @PostMapping("/ipAddressManage/ipconflict/browse")
    @ResponseBody
    @ApiOperation(value = "查看ip冲突列表", tags = "查看ip冲突列表")
    public ResponseBase ipConflictList(HttpServletRequest request) {
        List<IPConflictView> ipConflictViewList = new ArrayList<>();
        IpConfictDTO dto = new IpConfictDTO();
        try {
            List<IPInfoDTO> ipInfoDTOList = ipConflictManage.getCurrentIpConflictList();
            if (null != ipInfoDTOList) {
                for (IPInfoDTO ipInfoDTO : ipInfoDTOList) {
                    IPConflictView ipConflictView = new IPConflictView();
                    ipConflictView.extractFrom(ipInfoDTO);
                    ipConflictViewList.add(ipConflictView);
                }
            }
        }catch (Exception e){
            log.error("ipConflictList" ,e);
        }

        dto.setItemNameRankList(ipConflictViewList);
        dto.initTitle();

        return setResultSuccess(Reply.ok(dto));
    }



}
