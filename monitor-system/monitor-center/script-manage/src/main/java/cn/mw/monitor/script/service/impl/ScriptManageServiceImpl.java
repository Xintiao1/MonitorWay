package cn.mw.monitor.script.service.impl;

import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.api.exception.CommonException;
import cn.mw.monitor.assets.utils.ExportExcel;
import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.common.util.SeverityUtils;
import cn.mw.monitor.script.dao.*;
import cn.mw.monitor.script.entity.*;
import cn.mw.monitor.script.enums.ScriptExecStatus;
import cn.mw.monitor.script.enums.ScriptType;
import cn.mw.monitor.script.param.*;
import cn.mw.monitor.script.service.ScriptManageService;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.configmanage.AutoManageSerice;
import cn.mw.monitor.service.configmanage.ConfigManageCommonService;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParamList;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.EncryptsUtil;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.DateUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author gui.quanwang
 * @className ScriptManageServiceImpl
 * @description 脚本管理服务实现类
 * @date 2022/4/8
 */
@Service
@Slf4j
public class ScriptManageServiceImpl implements ScriptManageService, AutoManageSerice {

    @Value("${script-manage.spider.url}")
    private String spiderUrl;

    @Value("${script-manage.spider.python.url}")
    private String spiderPythonUrl;
    @Autowired
    private ConfigManageCommonService mwConfigManageService;

    @Resource
    private ScriptManageDao scriptManageDao;

    @Resource
    private ScriptExecDao scriptExecDao;

    @Autowired
    private MWUserService userService;

    @Autowired
    private  MwModelViewCommonService mwModelViewCommonService;

    @Resource
    private ScriptAccountDao scriptAccountDao;

    @Autowired
    private MWCommonService mwCommonService;

    @Resource
    private HomeworkManageDao homeworkManageDao;

    @Resource
    private HomeworkRelationManageDao homeworkRelationManageDao;



    /**
     * 默认密码
     */
    private final static String DEFAULT_PASSWORD = "0*0*0*";

    private  final  static  String IN_BAND_IP="inBandIp";
    /**
     * 增加脚本数据
     *
     * @param param 脚本数据
     * @return
     */
    @Override
    public Reply addScript(ScriptManageParam param) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            if (param.getScriptTreeId() == null || param.getScriptTreeId() <= 0) {
                return Reply.fail("请选择脚本所在树节点");
            }
            ScriptType scriptType = ScriptType.getScriptType(param.getScriptType());
            if (scriptType == null) {
                return Reply.fail("请选择脚本类型");
            }
            //增加脚本信息
            ScriptManageEntity script = new ScriptManageEntity();
            script.setId(getMaxScriptId());
            script.setScriptName(param.getScriptName());
            script.setScriptTreeId(param.getScriptTreeId());
            script.setScriptType(scriptType.getTypeName());
            script.setScriptContent(param.getScriptContent());
            script.setScriptDesc(param.getScriptDesc());
            script.setScriptVersion("1.0");
            script.setCreateTime(new Date());
            script.setUpdateTime(new Date());
            script.setDeleteFlag(false);
            script.setCreator(userInfo.getLoginName());
            script.setUpdater(userInfo.getLoginName());
            scriptManageDao.insert(script);
            return Reply.ok();
        } catch (Exception e) {
            log.error("增加脚本失败", e);
            return Reply.fail("增加脚本失败");
        }
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxScriptId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<ScriptManageEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        ScriptManageEntity maxEntity = scriptManageDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;
    }

    /**
     * 更新脚本数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply updateScript(ScriptManageParam param) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            //获取脚本数据
            ScriptManageEntity script = scriptManageDao.selectById(param.getId());
            if (script == null || script.getDeleteFlag()) {
                return Reply.fail("更新脚本失败");
            }
            if (param.getScriptTreeId() == null || param.getScriptTreeId() <= 0) {
                return Reply.fail("请选择脚本所在树节点");
            }
            ScriptType scriptType = ScriptType.getScriptType(param.getScriptType());
            if (scriptType == null) {
                return Reply.fail("请选择脚本类型");
            }
            script.setScriptName(param.getScriptName());
            script.setScriptTreeId(param.getScriptTreeId());
            script.setScriptType(scriptType.getTypeName());
            script.setScriptContent(param.getScriptContent());
            script.setScriptDesc(param.getScriptDesc());
            script.setUpdateTime(new Date());
            script.setUpdater(userInfo.getLoginName());
            scriptManageDao.updateById(script);
            return Reply.ok();
        } catch (Exception e) {
            log.error("更新脚本数据失败", e);
            return Reply.fail("更新脚本数据失败");
        }
    }

    /**
     * 删除脚本数据
     *
     * @param param 脚本数据
     * @return
     */
    @Override
    public Reply deleteScript(ScriptManageParam param) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            for (int id : param.getIds()) {
                //获取脚本数据
                ScriptManageEntity script = scriptManageDao.selectById(id);
                if (script == null || script.getDeleteFlag()) {
                    return Reply.fail("删除脚本失败");
                }
                script.setDeleteFlag(true);
                script.setUpdateTime(new Date());
                script.setUpdater(userInfo.getLoginName());
                scriptManageDao.updateById(script);
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("删除脚本失败", e);
            return Reply.fail("删除脚本失败");
        }
    }

    /**
     * 获取脚本列表
     *
     * @param scriptManageParam 脚本参数
     * @return
     */
    @Override
    public Reply getScriptList(ScriptManageParam scriptManageParam) {
        try {
            PageHelper.startPage(scriptManageParam.getPageNumber(), scriptManageParam.getPageSize());
            QueryWrapper<ScriptManageEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            if (scriptManageParam.getScriptTreeId() != null && scriptManageParam.getScriptTreeId() > 0) {
                wrapper.eq("script_tree_id", scriptManageParam.getScriptTreeId());
            }
            if (StringUtils.isNotEmpty(scriptManageParam.getSearchAll())) {
                wrapper.and(qw -> qw.like("script_name", scriptManageParam.getSearchAll())
                        .or()
                        .like("creator", scriptManageParam.getSearchAll()));
            } else {
                if (StringUtils.isNotEmpty(scriptManageParam.getScriptName())) {
                    wrapper.like("script_name", scriptManageParam.getScriptName());
                }
                if (StringUtils.isNotEmpty(scriptManageParam.getCreator())) {
                    wrapper.like("creator", scriptManageParam.getCreator());
                }
                if (StringUtils.isNotEmpty(scriptManageParam.getScriptType())) {
                    ScriptType scriptType = ScriptType.getScriptType(scriptManageParam.getScriptType());
                    if (scriptType == null) {
                        return Reply.fail("请选择脚本类型");
                    }
                    wrapper.eq("script_type", scriptType.getTypeName());
                }
            }
            if (scriptManageParam.getCreateDateStart() != null && scriptManageParam.getCreateDateEnd() != null) {
                wrapper.between("create_time", scriptManageParam.getCreateDateStart(), scriptManageParam.getCreateDateEnd());
            }
            wrapper.orderByDesc("create_time");
            List<ScriptManageEntity> list = scriptManageDao.selectList(wrapper);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("获取脚本列表", e);
            return Reply.fail("获取脚本列表失败");
        }
    }

    /**
     * 获取脚本信息
     *
     * @param scriptManageParam 脚本参数
     * @return
     */
    @Override
    public Reply getScriptInfo(ScriptManageParam scriptManageParam) {
        try {
            ScriptManageEntity script = scriptManageDao.selectById(scriptManageParam.getId());
            if (script == null || script.getDeleteFlag()) {
                return Reply.fail("获取失败");
            }
            return Reply.ok(script);
        } catch (Exception e) {
            log.error("获取脚本信息失败", e);
            return Reply.fail("获取脚本信息失败");
        }
    }

    /**
     * 执行脚本
     *
     * @param scriptManageParam 脚本参数
     * @return
     */
    @Override
    public synchronized Reply execScript(ScriptManageParam scriptManageParam) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            String checkResult = checkScriptParam(scriptManageParam);
            if (StringUtils.isNotEmpty(checkResult)) {
                return Reply.fail(checkResult);
            }
            //执行脚本类别
            ScriptType scriptType = ScriptType.getScriptType(scriptManageParam.getScriptType());
            //获取默认账号
            ScriptAccountEntity defaultAccount = scriptAccountDao.selectById(scriptManageParam.getDefaultAccountId());
            //获取默认账号执行类别
            ScriptType defaultAccountType = ScriptType.getScriptType(defaultAccount.getSystemType());
            //获取执行ID
            int maxExecId = getMaxExecId();
            //执行历史
            ScriptExecEntity scriptExec;

            //执行账号
            ScriptAccountEntity account;
            //执行账号类别
            ScriptType accountType;
            //执行IP列表
            List<String> ipList = new ArrayList<>();
            //执行账户列表
            List<SpiderRequestInfo.RequestAccount> accountList = new ArrayList<>();
            Map<String,List<SpiderRequestInfo.RequestAccount>> ipAccount = new HashMap<>();
            Map<String,List<String>> ansibleIP = new HashMap<>();
            //上个资产选择ansible
            String beforeIp=spiderPythonUrl.split(",")[0];
            Boolean ifDevice = false;
            for (TransAssets transAssets : scriptManageParam.getTransAssetsList()) {

                if (transAssets.getAccountId() == null) {
                    account = defaultAccount;
                } else {
                    account = scriptAccountDao.selectById(transAssets.getAccountId());
                }
                if (account.getSystemType().equals(ScriptType.DEVICE.getTypeName())){
                    ifDevice=true;
                }
                accountType = ScriptType.getScriptType(account.getSystemType());
                scriptExec = new ScriptExecEntity();
                Map<String, String> kill = mwConfigManageService.getAsstetByid(transAssets.getAssetsId());
                scriptExec.setId(getMaxScriptExecId());
                scriptExec.setExecId(maxExecId);
                scriptExec.setScriptId(scriptManageParam.getId());
                scriptExec.setScriptName(scriptManageParam.getScriptName());
                scriptExec.setAssetsId(transAssets.getAssetsId());
                scriptExec.setAssetsIP(kill.get("assetsName"));
                scriptExec.setAssetsPort("");
                scriptExec.setCostTime(0);
                scriptExec.setMaxOverTime(scriptManageParam.getMaxOverTime());
                scriptExec.setReturnContent("");
                scriptExec.setExecStatus(ScriptExecStatus.INIT.getStatus());
                scriptExec.setCreator(userInfo.getLoginName());
                scriptExec.setExecType(1);
                scriptExec.setMissionType(1);
                scriptExec.setCreateTime(new Date());
                scriptExec.setDeleteFlag(false);
                scriptExec.setAccountId(account.getId());
                scriptExec.setDefaultAccountId(scriptManageParam.getDefaultAccountId());
                scriptExec.setScriptParam(scriptManageParam.getScriptParam());
                scriptExec.setIsSensitive(scriptExec.getIsSensitive());
                scriptExec.setScriptContent(scriptManageParam.getScriptContent());
                scriptExec.setHomeworkFlag(false);
                scriptExec.setSqlDatabase(scriptManageParam.getSqlDatabase());
                scriptExec.setSqlText(scriptManageParam.getSqlText());
                scriptExec.setOrderAddress(scriptManageParam.getOrderAddress());
                scriptExec.setScriptType(scriptType.getTypeName());
                scriptExecDao.insert(scriptExec);
                //构造请求数据
                SpiderRequestInfo.RequestAccount requestAccount = new SpiderRequestInfo.RequestAccount();
                if (ScriptType.SQL == scriptType) {
                    ScriptAccountEntity parentAccount = scriptAccountDao.selectById(defaultAccount.getPid());
                    accountType = ScriptType.getScriptType(parentAccount.getSystemType());
                    requestAccount.setSqlDatabase(scriptManageParam.getSqlDatabase());
                    requestAccount.setSqlUsername(account.getAccount());
                    requestAccount.setSqlPassword(EncryptsUtil.decrypt(account.getPassword()));
                    requestAccount.setSqlText(scriptManageParam.getSqlText());
                    requestAccount.setSqlProt(Integer.parseInt(account.getPort()));
                    requestAccount.setUsername(parentAccount.getAccount());
                    requestAccount.setIp(kill.get("assetsName"));
                    requestAccount.setPassword(EncryptsUtil.decrypt(parentAccount.getPassword()));
                    requestAccount.setAnsible_connection(accountType == ScriptType.CMD ? "winrm" : "");
                    requestAccount.setProt(Integer.parseInt(parentAccount.getPort()));
                    requestAccount.setType(accountType.getAccountTypeId());
                } else {
                    requestAccount.setUsername(account.getAccount());
                    requestAccount.setIp(kill.get("assetsName"));
                    requestAccount.setPassword(EncryptsUtil.decrypt(account.getPassword()));
                    requestAccount.setAnsible_connection(accountType == ScriptType.CMD ? "winrm" : "");
                    requestAccount.setProt(Integer.parseInt(account.getPort()));
                    requestAccount.setType(accountType.getAccountTypeId());
                }
                if (transAssets.getAnsibleIpaddress()==null||transAssets.getAnsibleIpaddress().equals("")){
                    transAssets.setAnsibleIpaddress(beforeIp);
                }
                //以上一个资产选择IP为主
                beforeIp = transAssets.getAnsibleIpaddress();
                if (ipAccount.get(transAssets.getAnsibleIpaddress())==null){
                    accountList = new ArrayList<>();
                    ipList = new ArrayList<>();
                    accountList.add(requestAccount);
                    ipList.add(kill.get("assetsName"));
                    ipAccount.put(transAssets.getAnsibleIpaddress(),accountList);
                    ansibleIP.put(transAssets.getAnsibleIpaddress(),ipList);
                }else {
                    accountList = ipAccount.get(transAssets.getAnsibleIpaddress());
                    ipList = ansibleIP.get(transAssets.getAnsibleIpaddress());
                    accountList.add(requestAccount);
                    ipList.add(kill.get("assetsName"));
                    ipAccount.put(transAssets.getAnsibleIpaddress(),accountList);
                    ansibleIP.put(transAssets.getAnsibleIpaddress(),ipList);
                }


            }

            //下发脚本执行命令
            SpiderRequestInfo requestInfo = new SpiderRequestInfo();
            switch (scriptType) {
                case SHELL:
                case CMD:
                case PERL:
                case PYTHON:
                case POWERSHELL:
                    requestInfo.setMindType(0);
                    requestInfo.setOrdeAddress("");
                    break;
                case SQL:
                    requestInfo.setMindType(3);
                    requestInfo.setOrdeAddress(scriptManageParam.getOrderAddress());
                    ScriptAccountEntity parentAccount = scriptAccountDao.selectById(defaultAccount.getPid());
                    defaultAccount = parentAccount;
                    defaultAccountType = ScriptType.getScriptType(parentAccount.getSystemType());
                    break;
                default:
                    break;
            }

            if (ifDevice){
                requestInfo.setMindType(3);
            }
            requestInfo.setAppkey(maxExecId);

            requestInfo.setPassword(EncryptsUtil.decrypt(defaultAccount.getPassword()));
            requestInfo.setProt(Integer.parseInt(defaultAccount.getPort()));
            requestInfo.setType(defaultAccountType.getAccountTypeId());
            requestInfo.setUsername(defaultAccount.getAccount());
            requestInfo.setNeglect(false);
            requestInfo.setScript(scriptManageParam.getScriptContent());
            requestInfo.setScriptType(ScriptType.getScriptType(scriptManageParam.getScriptType()).getTypeId());

            for (String ip:ipAccount.keySet()) {
                requestInfo.setRoot(ipAccount.get(ip));
                requestInfo.setIpList(ansibleIP.get(ip));
                sendRequestToSpider(Arrays.asList(requestInfo),ip);
            }
            return Reply.ok(maxExecId);
        } catch (Exception e) {
            log.error("执行脚本失败", e);
            return Reply.fail("执行脚本失败");
        }
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxScriptExecId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<ScriptExecEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        ScriptExecEntity maxEntity = scriptExecDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;
    }

    /**
     * 校验脚本执行参数是否正确
     *
     * @param scriptManageParam 脚本执行参数
     * @return
     */
    private String checkScriptParam(ScriptManageParam scriptManageParam) {
        ScriptType scriptType = ScriptType.getScriptType(scriptManageParam.getScriptType());
        if (scriptType == null) {
            return "请选择脚本类别";
        }
        if (scriptManageParam.getDefaultAccountId() == null || scriptManageParam.getDefaultAccountId() <= 0) {
            return "请选择执行账户";
        }
        if (CollectionUtils.isEmpty(scriptManageParam.getTransAssetsList())) {
            return "请选择目标服务器";
        }
        if (StringUtils.isEmpty(scriptManageParam.getScriptContent())) {
            return "请填写脚本执行内容";
        }
        if (StringUtils.isEmpty(scriptManageParam.getScriptName())) {
            return "请填写执行脚本名称";
        }
        if (ScriptType.SQL == scriptType) {
            if (StringUtils.isEmpty(scriptManageParam.getSqlDatabase())) {
                return "请填写数据库名称";
            }
            if (StringUtils.isEmpty(scriptManageParam.getSqlText())) {
                return "请填写mysql前置标识";
            }
            if (StringUtils.isEmpty(scriptManageParam.getOrderAddress())) {
                return "请填写目的地址";
            }
        }
        return null;
    }

    /**
     * 将执行发送至spider
     *
     * @param entity
     */
    @Deprecated
    private void sendScriptToSpider(ScriptManageParam scriptManageParam, ScriptExecEntity entity, ScriptAccountEntity account) {
        try {
            ScriptType scriptType = ScriptType.getScriptType(scriptManageParam.getScriptType());
            ScriptType accountType = ScriptType.getScriptType(account.getSystemType());
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json;charset=UTF-8,text/plain");
            RequestScript requestScript = new RequestScript();
            RequestRoot requestRoot = new RequestRoot();
            switch (scriptType) {
                case SHELL:
                case CMD:
                case PERL:
                case PYTHON:
                case POWERSHELL:
                    requestRoot.setAdmin(account.getAccount());
                    requestRoot.setProt(Integer.parseInt(account.getPort()));
                    requestRoot.setPwd(EncryptsUtil.decrypt(account.getPassword()));
                    requestRoot.setType(accountType.getTypeId());
                    break;
                case SQL:
                    requestRoot.setMysqlPwd(EncryptsUtil.decrypt(account.getPassword()));
                    requestRoot.setMysqlRoot(account.getAccount());
                    ScriptAccountEntity parentAccount = scriptAccountDao.selectById(account.getPid());
                    accountType = ScriptType.getScriptType(parentAccount.getSystemType());
                    requestRoot.setAdmin(parentAccount.getAccount());
                    requestRoot.setProt(Integer.parseInt(account.getPort()));
                    requestRoot.setPwd(EncryptsUtil.decrypt(parentAccount.getPassword()));
                    requestRoot.setType(accountType.getTypeId());
                    break;
                default:
                    break;
            }
            requestScript.setRequestRoot(requestRoot);
            requestScript.setHostip(entity.getAssetsIP());
            requestScript.setKey(entity.getId());
            requestScript.setScript(scriptManageParam.getScriptContent().replaceAll("\n", "\r\n")
                    .replaceAll("\"", "\\\\$0"));
            requestScript.setType(scriptType.getTypeId());

            RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(requestScript));
            Request request = new Request.Builder()
                    .url(spiderUrl + "/mwapi/baseSpider/runScript")
                    .method("POST", body)
                    .addHeader("Accept", "application/json, text/plain, */*")
                    .addHeader("Accept-Encoding", "gzip, deflate, br")
                    .addHeader("Accept-Language", "zh-CN,zh;q=0.9")
                    .addHeader("Connection", "keep-alive")
                    .addHeader("Content-Type", "application/json;charset=UTF-8")
                    .addHeader("Cookie", "sidebarStatus=1; sid=mw65417b4880e045d698a0c2b2a42625cb")
                    .addHeader("Host", "secdevwechat.monitorway.net")
                    .addHeader("Origin", "https://secdevwechat.monitorway.net")
                    .addHeader("Referer", "https://secdevwechat.monitorway.net/")
                    .addHeader("Sec-Fetch-Dest", "empty")
                    .addHeader("Sec-Fetch-Mode", "cors")
                    .addHeader("Sec-Fetch-Site", "same-origin")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.61 Safari/537.36")
                    .addHeader("Content-Type", "text/plain")
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                entity.setExecStatus(ScriptExecStatus.EXECUTING.getStatus());
            } else {
                entity.setExecStatus(ScriptExecStatus.FAIL.getStatus());
                entity.setEndTime(new Date());
                entity.setReturnContent("请求接口失败" + JSON.toJSONString(response));
            }
        } catch (Exception e) {
            log.error("发送执行脚本命令失败", e);
            entity.setExecStatus(ScriptExecStatus.FAIL.getStatus());
            entity.setEndTime(new Date());
            entity.setReturnContent("请求接口失败");
        }
        scriptExecDao.updateById(entity);
    }

    /**
     * 更新执行结果
     *
     * @param map 结果数据
     * @return
     */
    @Override
    @Deprecated
    public Reply updateExecScript(HashMap map) {
        log.error(this.getClass().getSimpleName() + "更新执行结果>>" + JSON.toJSONString(map));
        if (map.containsKey("key")) {
            int id = (int) map.get("key");
            ScriptExecEntity scriptExec = scriptExecDao.selectById(id);
            if (ScriptExecStatus.EXECUTING.getStatus() == scriptExec.getExecStatus()) {
                scriptExec.setReturnContent((String) map.get("error"));
                long time = System.currentTimeMillis() - scriptExec.getCreateTime().getTime();
                scriptExec.setCostTime((int) time);
                scriptExec.setExecStatus((boolean) map.get("isover") ? ScriptExecStatus.FINISHED.getStatus() : ScriptExecStatus.EXECUTING.getStatus());
                scriptExec.setExecStatus((boolean) map.get("isError") ? ScriptExecStatus.FAIL.getStatus() : scriptExec.getExecStatus());
                if (ScriptExecStatus.EXECUTING.getStatus() != scriptExec.getExecStatus()) {
                    scriptExec.setEndTime(new Date());
                }
                scriptExecDao.updateById(scriptExec);
            }
        }
        return Reply.ok();
    }

    /**
     * 获取执行列表数据
     *
     * @param scriptManageParam
     * @return
     */
    @Override
    public Reply getExecList(ScriptManageParam scriptManageParam) {
        try {
            Map resultMap = new HashMap();
            QueryWrapper<ScriptExecEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            wrapper.eq("exec_id", scriptManageParam.getExecId());
            List<ScriptExecEntity> execList = scriptExecDao.selectList(wrapper);
            int costTime = 0;
            for (ScriptExecEntity entity : execList) {
                if (entity.getHomeworkFlag()) {
                    costTime = costTime < entity.getCostTime() ? entity.getCostTime() : costTime;
                } else {
                    costTime += entity.getCostTime();
                }
            }
            resultMap.put("execList", execList);
            resultMap.put("costTime", costTime);
            return Reply.ok(resultMap);
        } catch (Exception e) {
            log.error("获取执行列表数据失败", e);
            return Reply.fail("获取执行列表数据失败");
        }
    }

    /**
     * 获取执行详情数据
     *
     * @param scriptManageParam
     * @return
     */
    @Override
    public Reply getExecDetail(ScriptManageParam scriptManageParam) {
        try {
            ScriptExecEntity exec = scriptExecDao.selectById(scriptManageParam.getId());
            if (ScriptExecStatus.EXECUTING.getStatus() == exec.getExecStatus()) {
                int costTime = (int) (System.currentTimeMillis() - exec.getCreateTime().getTime());
                exec.setCostTime(costTime);
            }
            return Reply.ok(exec);
        } catch (Exception e) {
            log.error("获取执行详情数据失败", e);
            return Reply.fail("获取执行详情数据失败");
        }
    }

    /**
     * 获取历史脚本执行列表数据
     *
     * @param scriptManageParam
     * @return
     */
    @Override
    public Reply getExecHistoryList(ScriptManageParam scriptManageParam) {
        try {
            PageHelper.startPage(scriptManageParam.getPageNumber(), scriptManageParam.getPageSize());
            QueryWrapper<ScriptExecEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            wrapper.eq("is_homework", false);
            if (StringUtils.isNotEmpty(scriptManageParam.getSearchAll())) {
                wrapper.and(qw -> qw.like("script_name", scriptManageParam.getSearchAll())
                        .or()
                        .like("creator", scriptManageParam.getSearchAll())
                        .or()
                        .like("assets_ip", scriptManageParam.getSearchAll()));
            } else {
                if (StringUtils.isNotEmpty(scriptManageParam.getScriptName())) {
                    wrapper.like("script_name", scriptManageParam.getScriptName());
                }
                if (StringUtils.isNotEmpty(scriptManageParam.getCreator())) {
                    wrapper.like("creator", scriptManageParam.getCreator());
                }
                if (StringUtils.isNotEmpty(scriptManageParam.getAssetsIP())) {
                    wrapper.like("assets_ip", scriptManageParam.getAssetsIP());
                }
                if (scriptManageParam.getId() != null && scriptManageParam.getId() > 0) {
                    wrapper.eq("id", scriptManageParam.getId());
                }
                if (scriptManageParam.getExecStatus() != null && scriptManageParam.getExecStatus() > 0) {
                    wrapper.eq("exec_status", scriptManageParam.getExecStatus());
                }
            }
            if (scriptManageParam.getCreateDateStart() != null && scriptManageParam.getCreateDateEnd() != null) {
                wrapper.between("create_time", scriptManageParam.getCreateDateStart(), scriptManageParam.getCreateDateEnd());
            }
            wrapper.orderByDesc("create_time");
//            wrapper.eq("script_id", scriptManageParam.getId());
//            wrapper.groupBy("exec_id");
            List<ScriptExecEntity> execList = scriptExecDao.selectList(wrapper);
              for (ScriptExecEntity entity : execList) {
                if (entity.getMissionType() == 2) {
                    FileTransParam fileTransParam = JSON.parseObject(entity.getTransFileParam(), FileTransParam.class);
                    entity.setFileTransParam(fileTransParam);
                }
            }
            PageInfo pageInfo = new PageInfo<>(execList);
            pageInfo.setList(execList);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("获取历史脚本执行列表数据失败", e);
            return Reply.fail("获取历史脚本执行列表数据失败");
        }
    }

    /**
     * 获取模糊查询数据
     *
     * @param type 类别
     * @return
     */
    @Override
    public Reply getFuzzList(String type) {
        List<Map<String, String>> maps = new ArrayList<>();
        try {
            switch (type) {
                case "script-manage":
                    maps = scriptManageDao.fuzzSearchScriptData();
                    break;
                case "exec-history":
                    maps = scriptExecDao.fuzzSearchScriptExecData();
                    break;
                case "account-manage":
                    maps = scriptAccountDao.fuzzSearchAccountData();
                    break;
                default:
                    break;
            }
            Map<String, List> listMap = new HashMap<>();
            for (Map<String, String> map : maps) {
                if (listMap.get(map.get("type")) == null) {
                    List<String> strings = new ArrayList<>();
                    strings.add(map.get("keyName"));
                    listMap.put(map.get("type"), strings);
                } else {
                    List<String> strings = listMap.get(map.get("type"));
                    strings.add(map.get("keyName"));
                    listMap.put(map.get("type"), strings);
                }
            }
            return Reply.ok(listMap);
        } catch (Exception e) {
            log.error("获取模糊查询数据失败", e);
            return Reply.fail("获取模糊查询数据失败");
        }
    }

    /**
     * 增加账户
     *
     * @param param
     * @return
     */
    @Override
    public Reply addAccount(ScriptAccountParam param) {
        try {
            ScriptAccountEntity account = new ScriptAccountEntity();
            GlobalUserInfo userInfo = userService.getGlobalUser();
            account.setId(getMaxScriptAccountId());
            account.setAccount(param.getAccount());
            account.setPassword(EncryptsUtil.encrypt(param.getPassword()));
            account.setAccountAlias(param.getAccountAlias());
            account.setPort(param.getPort());
            account.setAccountDesc(param.getAccountDesc());
            account.setSystemType(param.getSystemType());
            account.setPid(param.getPid());
            account.setCreator(userInfo.getLoginName());
            account.setCreateDate(new Date());
            account.setModifier(userInfo.getLoginName());
            account.setModificationDate(new Date());
            account.setDeleteFlag(false);
            scriptAccountDao.insert(account);
            param.setId(account.getId());
            mwCommonService.addMapperAndPerm(param);
            return Reply.ok();
        } catch (Exception e) {
            log.error("增加账户数据失败", e);
            return Reply.fail("增加账户数据失败");
        }
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxScriptAccountId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<ScriptAccountEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        ScriptAccountEntity maxEntity = scriptAccountDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;
    }

    /**
     * 更新账户信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply updateAccount(ScriptAccountParam param) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            ScriptAccountEntity account = scriptAccountDao.selectById(param.getId());
            if (account == null || account.getDeleteFlag()) {
                return Reply.fail("更新账户数据失败,账户不存在或已删除");
            }
            account.setAccount(param.getAccount());
            if (!DEFAULT_PASSWORD.equals(param.getPassword())) {
                account.setPassword(EncryptsUtil.encrypt(param.getPassword()));
            }
            account.setAccountAlias(param.getAccountAlias());
            account.setPort(param.getPort());
            account.setAccountDesc(param.getAccountDesc());
            account.setSystemType(param.getSystemType());
            account.setPid(param.getPid());
            account.setModifier(userInfo.getLoginName());
            account.setModificationDate(new Date());
            scriptAccountDao.updateById(account);
            mwCommonService.updateMapperAndPerm(param);
            return Reply.ok();
        } catch (Exception e) {
            log.error("更新账户数据失败", e);
            return Reply.fail("更新账户数据失败");
        }
    }

    /**
     * 删除账户信息
     *
     * @param param
     * @return
     */
    @Override
    public Reply deleteAccount(ScriptAccountParam param) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            ScriptAccountEntity account;
            for (int id : param.getIds()) {
                param.setId(id);
                mwCommonService.deleteMapperAndPerm(param);
                account = scriptAccountDao.selectById(id);
                account.setModifier(userInfo.getLoginName());
                account.setModificationDate(new Date());
                account.setDeleteFlag(true);
                scriptAccountDao.deleteById(account.getId());
                scriptAccountDao.deteAssetsId(account.getId());
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("更新账户数据失败", e);
            return Reply.fail("更新账户数据失败");
        }
    }

    /**
     * 获取单个账户数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply browseAccount(ScriptAccountParam param) {
        try {
            ScriptAccountEntity account = scriptAccountDao.selectById(param.getId());
            account.setPassword(DEFAULT_PASSWORD);
            DataPermission dataPermission = mwCommonService.getDataPermission(param);
            account.setOrgIds(dataPermission.getOrgNodes());
            account.setPrincipal(dataPermission.getUserIds());
            account.setGroupIds(dataPermission.getGroupIds());
            account.setOrgIdss(dataPermission.getOrgIds());
            return Reply.ok(account);
        } catch (Exception e) {
            log.error("获取账户数据失败", e);
            return Reply.fail("获取账户数据失败");
        }
    }

    /**
     * 获取账户列表数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getAccountList(ScriptAccountParam param) {
        try {
            // TODO: 2022/12/13 数据权限
            QueryWrapper<ScriptAccountEntity> wrapper = new QueryWrapper<>();
            if (param.getPid() == null) {
                PageHelper.startPage(param.getPageNumber(), param.getPageSize());
                List<ScriptAccountParam> accountList = scriptAccountDao.getAccountList(param);
                PageInfo pageInfo = new PageInfo<>(accountList);
                pageInfo.setList(accountList);
                return Reply.ok(pageInfo);
            } else {
                wrapper.eq("delete_flag", false);
                wrapper.eq("pid", 0);
                List<ScriptAccountEntity> accountList = scriptAccountDao.selectList(wrapper);
                return Reply.ok(accountList);
            }
        } catch (Exception e) {
            log.error("获取账户数据失败", e);
            return Reply.fail("获取账户数据失败");
        }
    }

    /**
     * 获取账户下拉列表数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply getAccountDropList(ScriptAccountParam param) {
        try {
            ScriptType scriptType = ScriptType.getScriptType(param.getSystemType());
            QueryWrapper<ScriptAccountEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            if (param.getAccountAlias()!=null){
                wrapper.like("account_alias", param.getAccountAlias());
            }

            if (ScriptType.SQL.getTypeName().equals(param.getSystemType())) {
                wrapper.eq("system_type", ScriptType.SQL.getTypeName());
            } else {
                wrapper.eq("pid", 0);
                if (scriptType != null) {
                    wrapper.eq("system_type", scriptType.getTypeName());
                    wrapper.or().eq("system_type", "device");
                }
            }
            List<ScriptAccountEntity> accountList = scriptAccountDao.selectList(wrapper);
            return Reply.ok(accountList);
        } catch (Exception e) {
            log.error("获取账户下拉列表数据失败", e);
            return Reply.fail("获取账户下拉列表数据失败");
        }
    }

    /**
     * 分发文件
     *
     * @param param 分发数据
     * @return
     */
    @Override
    public Reply distributeFile(FileTransParam param) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            if (param.getDefaultAccountId() == null || param.getDefaultAccountId() <= 0) {
                return Reply.fail("请选择执行账户");
            }
            if (CollectionUtils.isEmpty(param.getTransAssetsList())) {
                return Reply.fail("请选择目标服务器");
            }
//            if (CollectionUtils.isEmpty(param.getFileNameList())||param.getFileNameList().size()>1) {
//                return Reply.fail("上传文件为空或者步骤中上传多个文件");
//            }
            //执行IP列表
            List<String> ipList = new ArrayList<>();
            //执行账户列表
            List<SpiderRequestInfo.RequestAccount> accountList = new ArrayList<>();
            ScriptAccountEntity defaultAccount = scriptAccountDao.selectById(param.getDefaultAccountId());
            ScriptType defaultAccountType = ScriptType.getScriptType(defaultAccount.getSystemType());

            //获取最大执行ID
            int maxExecId = getMaxExecId();

            //文件下发
            ScriptAccountEntity account;
            ScriptType accountType;
            MwTangibleassetsTable assets;
            for (TransAssets transAssets : param.getTransAssetsList()) {
                ScriptExecEntity scriptExec = new ScriptExecEntity();
                if (transAssets.getAccountId() == null) {
                    account = defaultAccount;
                } else {
                    account = scriptAccountDao.selectById(transAssets.getAccountId());
                }
                accountType = ScriptType.getScriptType(account.getSystemType());
              /*  Reply reply = mwModelViewCommonService.selectById(transAssets.getAssetsId());*/
                Map<String, String> kill = mwConfigManageService.getAsstetByid(transAssets.getAssetsId());
        /*        if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                    assets = (MwTangibleassetsTable) reply.getData();
                    if (null != assets) {*/
                        scriptExec.setId(getMaxScriptExecId());
                        scriptExec.setExecId(maxExecId);
                        scriptExec.setScriptId(0);
                        scriptExec.setScriptName(param.getScriptName());
                        scriptExec.setAssetsId(kill.get("assetsName"));
                        scriptExec.setAssetsIP(kill.get("assetsName"));
                        scriptExec.setAssetsPort("");
                        scriptExec.setCostTime(0);
                        scriptExec.setMaxOverTime(param.getMaxOverTime());
                        scriptExec.setReturnContent("");
                        scriptExec.setExecStatus(ScriptExecStatus.INIT.getStatus());
                        scriptExec.setCreator(userInfo.getLoginName());
                        scriptExec.setExecType(1);
                        scriptExec.setMissionType(2);
                        scriptExec.setCreateTime(new Date());
                        scriptExec.setDeleteFlag(false);
                        scriptExec.setAccountId(account.getId());
                        scriptExec.setScriptParam("");
                        scriptExec.setIsSensitive(scriptExec.getIsSensitive());
                        scriptExec.setScriptContent("");
                        scriptExec.setHomeworkFlag(false);
                        scriptExec.setTransFileParam(JSON.toJSONString(param));
                        scriptExec.setDefaultAccountId(defaultAccount.getId());
                        scriptExecDao.insert(scriptExec);
                        //增加到列表中
                        ipList.add(kill.get("assetsName"));
                        SpiderRequestInfo.RequestAccount requestAccount = new SpiderRequestInfo.RequestAccount();
                        requestAccount.setUsername(account.getAccount());
                        requestAccount.setIp(kill.get("assetsName"));
                        requestAccount.setPassword(EncryptsUtil.decrypt(account.getPassword()));
                        requestAccount.setAnsible_connection(accountType == ScriptType.CMD ? "winrm" : "");
                        requestAccount.setProt(Integer.parseInt(account.getPort()));
                        requestAccount.setType(accountType.getAccountTypeId());
                        accountList.add(requestAccount);
                  /*  }
                }*/
            }
            SpiderRequestInfo requestInfo = new SpiderRequestInfo();
            requestInfo.setAppkey(maxExecId);
            requestInfo.setIpList(ipList);
            requestInfo.setMindType(1);
            requestInfo.setOrdeAddress(param.getTargetFilePath());
            requestInfo.setPassword(EncryptsUtil.decrypt(defaultAccount.getPassword()));
            requestInfo.setProt(Integer.parseInt(defaultAccount.getPort()));
            requestInfo.setRoot(accountList);
            StringBuilder str = new StringBuilder();
            for (FileTransParam.FilePathInfo fileInfo : param.getFileNameList()) {
                str.append(fileInfo.getFilePath()).append(",");
            }
            requestInfo.setScript(str.substring(0, str.length() - 1));
            requestInfo.setScriptType(ScriptType.SHELL.getTypeId());
            requestInfo.setType(defaultAccountType.getTypeId());
            requestInfo.setUsername(defaultAccount.getAccount());
            requestInfo.setNeglect(false);

            //发送分发文件至spider
            sendRequestToSpider(Arrays.asList(requestInfo),spiderPythonUrl.split(",")[0]);
            return Reply.ok(maxExecId);
        } catch (Exception e) {
            log.error("分发文件失败", e);
            return Reply.fail("分发文件失败");
        }
    }

    /**
     * 增加作业
     *
     * @param param 作业信息
     * @return
     */
    @Override
    public synchronized Reply addHomework(HomeworkParam param) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            if (CollectionUtils.isEmpty(param.getStepList())) {
                return Reply.fail("请选择执行步骤");
            }
            //获取最大作业版本ID
            int versionId = getMaxVersionId();
            //增加作业信息
            HomeworkEntity homework = new HomeworkEntity();
            homework.setId(getMaxHomeworkId());
            homework.setHomeworkName(param.getHomeworkName());
            homework.setHomeworkTreeId(param.getHomeworkTreeId());
            homework.setHomeworkDesc(param.getHomeworkDesc());
            homework.setCreateTime(new Date());
            homework.setUpdateTime(new Date());
            homework.setCreator(userInfo.getLoginName());
            homework.setUpdater(userInfo.getLoginName());
            homework.setDeleteFlag(false);
            homework.setHomeworkVersionId(versionId);
            homework.setVariableIds(param.getVariableIds().toString());
            homeworkManageDao.insert(homework);

            //保存对应的执行记录
            for (HomeworkParam.HomeworkChildParam childParam : param.getStepList()) {
                switch (childParam.getStepType()) {
                    case 1:
                        if (childParam.getScriptParam().getDefaultAccountId() == null
                                || childParam.getScriptParam().getDefaultAccountId() <= 0) {
                            return Reply.fail("请选择执行账户");
                        }
                        if (CollectionUtils.isEmpty(childParam.getScriptParam().getTransAssetsList())) {
                            log.error("保存作业内容失败,无资产数据" + JSON.toJSONString(childParam));
                            continue;
                        }
                        break;
                    case 2:
                        if (childParam.getFileTransParam().getDefaultAccountId() == null
                                || childParam.getFileTransParam().getDefaultAccountId() <= 0) {
                            return Reply.fail("请选择执行账户");
                        }
                        if (CollectionUtils.isEmpty(childParam.getFileTransParam().getTransAssetsList())) {
                            log.error("保存作业内容失败,无资产数据" + JSON.toJSONString(childParam));
                            continue;
                        }
                        break;
                    default:
                        break;
                }
                saveHomeworkExecItem(userInfo, homework, childParam, versionId);
            }
            return Reply.ok();
        } catch (Exception e) {
            log.error("保存作业内容失败", e);
            return Reply.fail("保存作业内容失败");
        }
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxHomeworkId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<HomeworkEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        HomeworkEntity maxEntity = homeworkManageDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;
    }

    /**
     * 保存作业----执行项(脚本信息,文件分发)
     *
     * @param userInfo   用户信息
     * @param homework   作业信息
     * @param childParam 脚本信息
     * @param versionId  版本ID
     */
    private void saveHomeworkExecItem(GlobalUserInfo userInfo, HomeworkEntity homework,
                                      HomeworkParam.HomeworkChildParam childParam, int versionId) {
        //获取默认的全局账户
        ScriptAccountEntity defaultAccount = new ScriptAccountEntity();
        String stepName = "";
        boolean ignoreError = false;
        List<TransAssets> transAssetsList = new ArrayList<>();
        switch (childParam.getStepType()) {
            case 1:
                defaultAccount = scriptAccountDao.selectById(childParam.getScriptParam().getDefaultAccountId());
                stepName = childParam.getScriptParam().getStepName();
                ignoreError = childParam.getScriptParam().getIgnoreError();
                transAssetsList = childParam.getScriptParam().getTransAssetsList();
                break;
            case 2:
                defaultAccount = scriptAccountDao.selectById(childParam.getFileTransParam().getDefaultAccountId());
                stepName = childParam.getFileTransParam().getStepName();
                ignoreError = childParam.getFileTransParam().getIgnoreError();
                transAssetsList = childParam.getFileTransParam().getTransAssetsList();
                break;
            default:
                break;
        }
        ScriptExecEntity scriptExec;
        //获取最大执行ID
        int maxExecId = getMaxExecId();
        //创建关联关系,并保存数据
        HomeworkRelationEntity relation = new HomeworkRelationEntity();
        relation.setId(getMaxHomeworkRelId());
        relation.setExecId(maxExecId);
        relation.setHomeworkId(homework.getId());
        relation.setHomeworkSort(childParam.getIndex());
        relation.setHomeworkVersionId(versionId);
        homeworkRelationManageDao.insert(relation);
        //批量保存资产数据对应的关系
        for (TransAssets transAssets : transAssetsList) {
            ScriptAccountEntity account;
            scriptExec = new ScriptExecEntity();
            if (transAssets.getAccountId() == null) {
                account = defaultAccount;
            } else {
                account = scriptAccountDao.selectById(transAssets.getAccountId());
            }
            Map<String, String> kill = mwConfigManageService.getAsstetByid(transAssets.getAssetsId());

            if (null != kill) {
                scriptExec.setId(getMaxScriptExecId());
                scriptExec.setExecId(maxExecId);
                scriptExec.setScriptId(0);
                scriptExec.setScriptName(stepName);
                scriptExec.setAssetsId(transAssets.getAssetsId());
                scriptExec.setAssetsIP(kill.get("assetsName"));
                scriptExec.setAssetsPort("");
                scriptExec.setCostTime(0);
                scriptExec.setReturnContent("");
                scriptExec.setExecStatus(ScriptExecStatus.INIT.getStatus());
                scriptExec.setCreator(userInfo.getLoginName());
                scriptExec.setExecType(1);
                scriptExec.setMissionType(childParam.getStepType());
                scriptExec.setCreateTime(new Date());
                scriptExec.setDeleteFlag(false);
                scriptExec.setAccountId(account.getId());
                /*scriptExec.setIsVarible(childParam.getScriptParam().getIsVarible());*/
                switch (childParam.getStepType()) {
                    case 1:
                        scriptExec.setScriptParam(childParam.getScriptParam().getScriptParam());
                        scriptExec.setIsSensitive(childParam.getScriptParam().getIsSensitive());
                        scriptExec.setScriptContent(childParam.getScriptParam().getScriptContent());
                        scriptExec.setMaxOverTime(childParam.getScriptParam().getMaxOverTime());
                        scriptExec.setDefaultAccountId(childParam.getScriptParam().getDefaultAccountId());
                        scriptExec.setScriptType(childParam.getScriptParam().getScriptType());
                        scriptExec.setSqlDatabase(childParam.getScriptParam().getSqlDatabase());
                        scriptExec.setSqlText(childParam.getScriptParam().getSqlText());
                        scriptExec.setOrderAddress(childParam.getScriptParam().getOrderAddress());
                        break;
                    case 2:
                        scriptExec.setMaxOverTime(childParam.getFileTransParam().getMaxOverTime());
                        scriptExec.setTransFileParam(JSON.toJSONString(childParam.getFileTransParam()));
                        scriptExec.setDefaultAccountId(childParam.getFileTransParam().getDefaultAccountId());
                        break;
                    default:
                        break;
                }
                scriptExec.setHomeworkFlag(true);
                scriptExec.setIgnoreError(ignoreError);
                scriptExec.setHomeworkSort(childParam.getIndex());
                scriptExec.setHomeworkVersionId(versionId);

                scriptExecDao.insert(scriptExec);
            }

        }
    }

    /**
     * 删除作业
     *
     * @param param 作业信息
     * @return
     */
    @Override
    public Reply deleteHomework(HomeworkParam param) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            for (Integer id : param.getIds()) {
                HomeworkEntity homework = homeworkManageDao.selectById(id);
                if (homework == null || homework.getDeleteFlag()) {
                    return Reply.fail("删除作业失败,作业不存在");
                }
                homework.setDeleteFlag(true);
                homework.setUpdateTime(new Date());
                homework.setUpdater(userInfo.getLoginName());
                homeworkManageDao.updateById(homework);
            }
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("删除作业失败", e);
            return Reply.fail("删除作业失败");
        }
    }

    /**
     * 修改作业
     *
     * @param param 作业信息
     * @return
     */
    @Override
    public Reply updateHomework(HomeworkParam param) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            HomeworkEntity homework = homeworkManageDao.selectById(param.getId());
            if (homework == null || homework.getDeleteFlag()) {
                return Reply.fail("更新作业失败,作业不存在");
            }

            //获取最大作业版本ID
            int versionId = getMaxVersionId();

            homework.setUpdateTime(new Date());
            homework.setUpdater(userInfo.getLoginName());
            homework.setHomeworkName(param.getHomeworkName());
            homework.setHomeworkTreeId(param.getHomeworkTreeId());
            homework.setHomeworkDesc(param.getHomeworkDesc());
            homework.setHomeworkVersionId(versionId);
            //更新作业数据
            homeworkManageDao.updateById(homework);
            //更新执行计划序列数据
            for (HomeworkParam.HomeworkChildParam childParam : param.getStepList()) {
                saveHomeworkExecItem(userInfo, homework, childParam, versionId);
            }
            return Reply.ok("更新成功");
        } catch (Exception e) {
            log.error("更新作业失败", e);
            return Reply.fail("更新作业失败");
        }
    }

    /**
     * 查询作业
     *
     * @param param 作业信息
     * @return
     */
    @Override
    public Reply browseHomework(HomeworkParam param) {
        try {
            HomeworkEntity homework = homeworkManageDao.selectById(param.getId());
            if (homework == null || homework.getDeleteFlag()) {
                return Reply.fail("查询作业失败,作业不存在");
            }
            List<HomeworkParam.HomeworkChildParam> stepList = new ArrayList<>();
            //获取具体的执行列表内容(颗粒度到作业步骤)
            List<ScriptExecEntity> execEntityList = scriptExecDao.getHomeworkStepList(homework.getHomeworkVersionId());
            for (ScriptExecEntity entity : execEntityList) {
                stepList.add(getChildDetail(entity.getExecId()));
            }
            homework.setStepList(stepList);
            return Reply.ok(homework);
        } catch (Exception e) {
            log.error("查询作业失败", e);
            return Reply.fail("查询作业失败");
        }
    }

    /**
     * 查询作业列表
     *
     * @param param 作业信息
     * @return
     */
    @Override
    public Reply browseHomeworkList(HomeworkParam param) {
        try {
            PageHelper.startPage(param.getPageNumber(), param.getPageSize());
            QueryWrapper<HomeworkEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("delete_flag", false);
            if (param.getHomeworkTreeId() != null && param.getHomeworkTreeId() > 0) {
                wrapper.eq("homework_tree_id", param.getHomeworkTreeId());
            }
            if (StringUtils.isNotEmpty(param.getSearchAll())) {
                wrapper.and(qw -> qw.like("homework_name", param.getSearchAll())
                        .or()
                        .like("creator", param.getSearchAll())
                        .or()
                        .like("updater", param.getSearchAll()));
            } else {
                if (StringUtils.isNotEmpty(param.getHomeworkName())) {
                    wrapper.like("homework_name", param.getHomeworkName());
                }
                if (StringUtils.isNotEmpty(param.getCreator())) {
                    wrapper.like("creator", param.getCreator());
                }
                if (StringUtils.isNotEmpty(param.getUpdater())) {
                    wrapper.like("updater", param.getUpdater());
                }
            }
            wrapper.orderByDesc("create_time");
            List<HomeworkEntity> list = homeworkManageDao.selectList(wrapper);
            PageInfo pageInfo = new PageInfo<>(list);
            pageInfo.setList(list);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("查询作业失败", e);
            return Reply.fail("查询作业失败");
        }
    }

    public TimeTaskRresult downloadConfig(String id) throws Exception {
        TimeTaskRresult result = new TimeTaskRresult();
        HomeworkParam param = new HomeworkParam();
        param.setId(Integer.valueOf(id));
        performHomework(param);

        return result.setSuccess(true).setResultType(1);
    }

    //自动化执行超时处理
    public TimeTaskRresult gameOverTask() throws Exception {
        TimeTaskRresult result = new TimeTaskRresult();
        scriptExecDao.updateTimeOver();

        return result.setSuccess(true).setResultType(1);
    }

    /**
     * 执行作业
     *
     * @param param 作业信息
     * @return
     */
    @Override
    public Reply performHomework(HomeworkParam param) throws Exception {
            GlobalUserInfo userInfo = new GlobalUserInfo();
            try {
                 userInfo = userService.getGlobalUser();
            }catch (Exception e){
               userInfo.setLoginName("admin");
            }

            //获取作业信息
            HomeworkEntity homework = homeworkManageDao.selectById(param.getId());
            if (homework == null || homework.getDeleteFlag()) {
                return Reply.fail("查询作业失败,作业不存在");
            }
            //获取最大作业版本ID
            int versionId = getMaxVersionId();
            int oldVersionId = homework.getHomeworkVersionId();

            List<SpiderRequestInfo> requestList = new ArrayList<>();

            //执行作业----总
            //更新作业数据--step1
            homework.setUpdateTime(new Date());
            homework.setUpdater(userInfo.getLoginName());
            homework.setHomeworkVersionId(versionId);
            homeworkManageDao.updateById(homework);

            //创建作业执行记录
            ScriptExecEntity homeworkExec = new ScriptExecEntity();
            homeworkExec.setId(getMaxScriptExecId());
            homeworkExec.setExecId(getMaxExecId());
            homeworkExec.setScriptName(homework.getHomeworkName());
            homeworkExec.setAssetsIP("无");
            homeworkExec.setAssetsPort("");
            homeworkExec.setCostTime(0);
            homeworkExec.setExecStatus(ScriptExecStatus.EXECUTING.getStatus());
            homeworkExec.setCreator(userInfo.getLoginName());
            homeworkExec.setExecType(1);
            homeworkExec.setMissionType(3);
            homeworkExec.setCreateTime(new Date());
            homeworkExec.setDeleteFlag(false);
            homeworkExec.setHomeworkFlag(false);
            homeworkExec.setHomeworkVersionId(versionId);
            homeworkExec.setHomeworkId(param.getId());
            scriptExecDao.insert(homeworkExec);

            //获取作业关联列表---step2
            QueryWrapper<HomeworkRelationEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("homework_version_id", oldVersionId);
            wrapper.orderByAsc("homework_sort");
            List<HomeworkRelationEntity> relationList = homeworkRelationManageDao.selectList(wrapper);
            if (CollectionUtils.isEmpty(relationList)) {
                return Reply.fail("执行作业失败");
            }
            for (HomeworkRelationEntity relation : relationList) {
                //获取最大执行ID
                int maxExecId = getMaxExecId();
                //创建关联关系,并保存数据
                HomeworkRelationEntity homeworkRelationEntity = new HomeworkRelationEntity();
                homeworkRelationEntity.setId(getMaxHomeworkRelId());
                homeworkRelationEntity.setExecId(maxExecId);
                homeworkRelationEntity.setHomeworkId(homework.getId());
                homeworkRelationEntity.setHomeworkSort(relation.getHomeworkSort());
                homeworkRelationEntity.setHomeworkVersionId(versionId);
                homeworkRelationManageDao.insert(homeworkRelationEntity);

                //获取作业执行记录----step3
                QueryWrapper<ScriptExecEntity> execWrapper = new QueryWrapper<>();
                execWrapper.eq("homework_version_id", oldVersionId);
                execWrapper.eq("delete_flag", false);
                execWrapper.eq("is_homework", true);
                execWrapper.eq("exec_id", relation.getExecId());
                List<ScriptExecEntity> execItemList = scriptExecDao.selectList(execWrapper);

                //下发作业
                //执行IP列表
                List<String> ipList = new ArrayList<>();
                //执行账户列表
                List<SpiderRequestInfo.RequestAccount> accountList = new ArrayList<>();
                ScriptAccountEntity account;
                ScriptType accountType;
                int mindType = 0;
                //默认账户
                int defaultAccountId = 0;
                String targetFilePath = "";
                String sourceFilePath = "";
                ScriptType scriptType = null;
                boolean ignoreFlase = false;
                String script = "";
                String sqlOrderAddress = "";
                for (ScriptExecEntity execItem : execItemList) {
                    execItem.setId(getMaxScriptExecId());
                    execItem.setExecId(maxExecId);
                    execItem.setCostTime(0);
                    execItem.setReturnContent("");
                    execItem.setExecStatus(ScriptExecStatus.INIT.getStatus());
                    execItem.setCreator(userInfo.getLoginName());
                    execItem.setCreateTime(new Date());
                    execItem.setHomeworkVersionId(versionId);
                    execItem.setHomeworkFlag(true);
                    execItem.setHomeworkId(param.getId());
                    scriptExecDao.insert(execItem);

                    //增加到列表中
                    ipList.add(execItem.getAssetsIP());
                    account = scriptAccountDao.selectById(execItem.getAccountId());
                    accountType = ScriptType.getScriptType(account.getSystemType());
                    Boolean mineType = false;
                    SpiderRequestInfo.RequestAccount requestAccount = new SpiderRequestInfo.RequestAccount();
                    if (ScriptType.SQL.getTypeName().equals(execItem.getScriptType())) {
                        ScriptAccountEntity parentAccount = scriptAccountDao.selectById(account.getPid());
                        accountType = ScriptType.getScriptType(parentAccount.getSystemType());
                        requestAccount.setSqlDatabase(execItem.getSqlDatabase());
                        requestAccount.setSqlUsername(account.getAccount());
                        requestAccount.setSqlPassword(EncryptsUtil.decrypt(account.getPassword()));
                        requestAccount.setSqlText(execItem.getSqlText());
                        requestAccount.setSqlProt(Integer.parseInt(account.getPort()));
                        requestAccount.setUsername(parentAccount.getAccount());
                        requestAccount.setIp(execItem.getAssetsIP());
                        requestAccount.setPassword(EncryptsUtil.decrypt(parentAccount.getPassword()));
                        requestAccount.setAnsible_connection(accountType == ScriptType.CMD ? "winrm" : "");
                        requestAccount.setProt(Integer.parseInt(parentAccount.getPort()));
                        requestAccount.setType(accountType.getAccountTypeId());
                    }
                    else {
                        requestAccount.setUsername(account.getAccount());
                        requestAccount.setIp(execItem.getAssetsIP());
                        requestAccount.setPassword(EncryptsUtil.decrypt(account.getPassword()));
                        requestAccount.setAnsible_connection(accountType == ScriptType.CMD ? "winrm" : "");
                        requestAccount.setProt(Integer.parseInt(account.getPort()));
                        requestAccount.setType(accountType.getAccountTypeId());
                    }
                    accountList.add(requestAccount);

                    mindType = execItem.getMissionType();
                   /* if (account.getAccountAlias().contains("device")){
                        mindType = 3;
                    }*/
                    if (account.getSystemType().equals("device")){
                        mindType = 3;
                    }
                    defaultAccountId = execItem.getDefaultAccountId();
                    ignoreFlase = execItem.getIgnoreError();
                    script = execItem.getScriptContent();
                    sqlOrderAddress = execItem.getOrderAddress();
                    if (mindType == 2 && StringUtils.isEmpty(targetFilePath)) {
                        FileTransParam fileTransParam = JSON.parseObject(execItem.getTransFileParam(), FileTransParam.class);
                        targetFilePath = fileTransParam.getTargetFilePath();
                        StringBuilder str = new StringBuilder();
                        for (FileTransParam.FilePathInfo fileInfo : fileTransParam.getFileNameList()) {
                            str.append(fileInfo.getFilePath()).append(",");
                        }
                        sourceFilePath = str.substring(0, str.length() - 1);
                    }
                    if (choeseMinType(mindType)&& scriptType == null) {
                        scriptType = ScriptType.getScriptType(execItem.getScriptType());
                    }
                }

                ScriptAccountEntity defaultAccount = scriptAccountDao.selectById(defaultAccountId);
                ScriptType defaultAccountType = ScriptType.getScriptType(defaultAccount.getSystemType());

                SpiderRequestInfo requestInfo = new SpiderRequestInfo();
                requestInfo.setAppkey(maxExecId);
                requestInfo.setIpList(ipList);
                requestInfo.setPassword(EncryptsUtil.decrypt(defaultAccount.getPassword()));
                requestInfo.setProt(Integer.parseInt(defaultAccount.getPort()));
                requestInfo.setRoot(accountList);
                requestInfo.setType(defaultAccountType.getTypeId());
                requestInfo.setUsername(defaultAccount.getAccount());
                requestInfo.setNeglect(ignoreFlase);
                switch (mindType) {
                    //脚本执行
                    case 1:
                        if (ScriptType.SQL == scriptType) {
                            requestInfo.setMindType(2);
                            requestInfo.setOrdeAddress(sqlOrderAddress);
                        } else {
                            requestInfo.setMindType(0);
                            requestInfo.setOrdeAddress("");
                        }
                        requestInfo.setScript(script);
                        requestInfo.setScriptType(scriptType.getTypeId());
                        break;
                    case 3:
                        requestInfo.setMindType(3);
                        requestInfo.setOrdeAddress("");
                        requestInfo.setScript(script);
                        requestInfo.setScriptType(scriptType.getTypeId());
                        break;
                    //文件分发
                    case 2:
                        requestInfo.setMindType(1);
                        requestInfo.setOrdeAddress(targetFilePath);
                        requestInfo.setScript(sourceFilePath);
                        requestInfo.setScriptType(ScriptType.SHELL.getTypeId());
                        break;
                    default:
                        break;
                }
                requestList.add(requestInfo);
            }
            //发送分发文件至spider
            sendRequestToSpider(requestList,spiderPythonUrl.split(",")[0]);
            return Reply.ok(homework.getHomeworkVersionId());

    }



    private boolean choeseMinType(Integer mindType) {
        if (mindType.equals(1)||mindType.equals(3)){
            return true;
        }
        return false;
    }

    /**
     * 查询作业步骤详情
     *
     * @param param 作业信息
     * @return
     */
    @Override
    public Reply browseHomeworkStep(HomeworkParam param) {
        try {
            //获取步骤信息
            QueryWrapper<ScriptExecEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("exec_id", param.getExecId());
            ScriptExecEntity execEntity = scriptExecDao.selectList(wrapper).get(0);
            if (execEntity == null) {
                return Reply.fail("查询作业步骤详情失败");
            }
            HomeworkParam.HomeworkChildParam childStep = new HomeworkParam.HomeworkChildParam();
            childStep.setStepType(execEntity.getMissionType());
            //绑定资产数据
            List<TransAssets> assetsList = scriptExecDao.getTransAssetsList(param.getExecId());
            //获取详细信息(脚本执行 , 文件分发)
            switch (execEntity.getMissionType()) {
                //脚本执行
                case 1:
                    ScriptManageParam scriptInfo = new ScriptManageParam();
                    scriptInfo.setScriptName(execEntity.getScriptName());
                    scriptInfo.setScriptType(execEntity.getScriptType());
                    scriptInfo.setScriptContent(execEntity.getScriptContent());
                    scriptInfo.setScriptParam(execEntity.getScriptParam());
                    scriptInfo.setIsSensitive(execEntity.getIsSensitive());
                    scriptInfo.setMaxOverTime(execEntity.getMaxOverTime());
                    scriptInfo.setStepName(execEntity.getScriptName());
                    scriptInfo.setIgnoreError(execEntity.getIgnoreError());
                    scriptInfo.setDefaultAccountId(execEntity.getAccountId());
                    scriptInfo.setTransAssetsList(assetsList);
                    childStep.setScriptParam(scriptInfo);
                    break;
                //文件分发
                case 2:
                    FileTransParam fileInfo = JSON.parseObject(execEntity.getTransFileParam(), FileTransParam.class);
                    fileInfo.setStepName(execEntity.getScriptName());
                    fileInfo.setIgnoreError(execEntity.getIgnoreError());
                    fileInfo.setDefaultAccountId(execEntity.getAccountId());
                    fileInfo.setTransAssetsList(assetsList);
                    childStep.setFileTransParam(fileInfo);
                    break;
                default:
                    log.error("查询作业步骤详情失败" + JSON.toJSONString(execEntity));
                    break;
            }
            return Reply.ok(childStep);
        } catch (Exception e) {
            log.error("查询作业步骤详情失败", e);
            return Reply.fail("查询作业步骤详情失败");
        }
    }

    /**
     * 获取步骤详情
     *
     * @param execId
     * @return
     */
    private HomeworkParam.HomeworkChildParam getChildDetail(int execId) {
        //获取步骤信息
        QueryWrapper<ScriptExecEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("exec_id", execId);
        ScriptExecEntity execEntity = scriptExecDao.selectList(wrapper).get(0);
        if (execEntity == null) {
            throw new CommonException("查询作业步骤详情失败");
        }
        HomeworkParam.HomeworkChildParam childStep = new HomeworkParam.HomeworkChildParam();
        childStep.setStepType(execEntity.getMissionType());
        childStep.setStepName(execEntity.getScriptName());
        //绑定资产数据
        List<TransAssets> assetsList = scriptExecDao.getTransAssetsList(execId);
        //获取详细信息(脚本执行 , 文件分发)
        switch (execEntity.getMissionType()) {
            //脚本执行
            case 1:
                ScriptManageParam scriptInfo = new ScriptManageParam();
                scriptInfo.setScriptName(execEntity.getScriptName());
                scriptInfo.setScriptType(execEntity.getScriptType());
                scriptInfo.setScriptContent(execEntity.getScriptContent());
                scriptInfo.setScriptParam(execEntity.getScriptParam());
                scriptInfo.setIsSensitive(execEntity.getIsSensitive());
                scriptInfo.setMaxOverTime(execEntity.getMaxOverTime());
                scriptInfo.setStepName(execEntity.getScriptName());
                scriptInfo.setIgnoreError(execEntity.getIgnoreError());
                scriptInfo.setDefaultAccountId(execEntity.getDefaultAccountId());
                scriptInfo.setTransAssetsList(assetsList);
                scriptInfo.setSqlDatabase(execEntity.getSqlDatabase());
                scriptInfo.setSqlText(execEntity.getSqlText());
                scriptInfo.setOrderAddress(execEntity.getOrderAddress());
                childStep.setScriptParam(scriptInfo);
                childStep.setFileTransParam(null);
                scriptInfo.setIsVarible(execEntity.getIsVarible());
                break;
            //文件分发
            case 2:
                FileTransParam fileInfo = JSON.parseObject(execEntity.getTransFileParam(), FileTransParam.class);
                fileInfo.setStepName(execEntity.getScriptName());
                fileInfo.setIgnoreError(execEntity.getIgnoreError());
                fileInfo.setDefaultAccountId(execEntity.getDefaultAccountId());
                fileInfo.setTransAssetsList(assetsList);
                fileInfo.setIsVarible(execEntity.getIsVarible());
                childStep.setFileTransParam(fileInfo);
                childStep.setScriptParam(null);
                break;
            default:
                log.error("查询作业步骤详情失败" + JSON.toJSONString(execEntity));
                break;
        }
        return childStep;
    }

    /**
     * 查询作业步骤详情
     *
     * @param param 作业信息
     * @return
     */
    @Override
    public Reply getExecResult(HomeworkParam param) {
        try {
            if (param.getHomeworkVersionId() == null || param.getHomeworkVersionId() <= 0) {
                return Reply.fail("请填写正常的执行编号");
            }
            Map resultMap = new HashMap();
            //获取作业执行记录
            QueryWrapper<ScriptExecEntity> homeworkExecWrapper = new QueryWrapper<>();
            homeworkExecWrapper.eq("homework_version_id", param.getHomeworkVersionId());
            homeworkExecWrapper.eq("is_homework", false);
            ScriptExecEntity homeworkExecInfo = scriptExecDao.selectList(homeworkExecWrapper).get(0);
            if (homeworkExecInfo == null) {
                return Reply.fail("请填写正常的执行编号");
            }
            //结果列表
            List<ScriptExecEntity> resultList = new ArrayList<>();
            //获取作业信息
            QueryWrapper<HomeworkRelationEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("homework_version_id", param.getHomeworkVersionId());
            wrapper.orderByAsc("homework_sort");
            List<HomeworkRelationEntity> relationList = homeworkRelationManageDao.selectList(wrapper);
            if (CollectionUtils.isEmpty(relationList)) {
                return Reply.fail("查询作业步骤详情失败");
            }
            for (HomeworkRelationEntity relation : relationList) {
                //获取结果
                List<ScriptExecEntity> execResultList = scriptExecDao.getHomeworkStepResultList(param.getHomeworkVersionId(), relation.getExecId());
                ScriptExecEntity execResult = new ScriptExecEntity();
                int costTime = 0;
                ScriptExecStatus execStatus = ScriptExecStatus.FINISHED;
                String stepName = "";
                int index = 0;
                Date createTime = new Date();
                Date endTime = new Date();
                //判断执行结果
                for (ScriptExecEntity execEntity : execResultList) {
                    costTime = costTime < execEntity.getCostTime() ? execEntity.getCostTime() : costTime;
                    stepName = execEntity.getStepName();
                    index = execEntity.getHomeworkSort();
                    createTime = execEntity.getCreateTime();
                    if (execEntity.getEndTime() != null && execEntity.getEndTime().before(endTime)) {
                        endTime = execEntity.getEndTime();
                    }
                    switch (ScriptExecStatus.getStatus(execEntity.getExecStatus())) {
                        case EXECUTING:
                            if (execStatus != ScriptExecStatus.FAIL) {
                                execStatus = ScriptExecStatus.EXECUTING;
                            }
                            break;
                        case FINISHED:
                            if (execStatus != ScriptExecStatus.EXECUTING && execStatus != ScriptExecStatus.FAIL) {
                                execStatus = ScriptExecStatus.FINISHED;
                            }
                            break;
                        case FAIL:
                            if (execEntity.getIgnoreError()) {
                                if (execStatus != ScriptExecStatus.EXECUTING) {
                                    execStatus = ScriptExecStatus.FINISHED;
                                }
                            } else {
                                execStatus = ScriptExecStatus.FAIL;
                            }
                            break;
                        default:
                            break;
                    }
                }
                execResult.setExecStatus(execStatus.getStatus());
                execResult.setCostTime(costTime);
                execResult.setStepName(stepName);
                execResult.setExecId(relation.getExecId());
                execResult.setHomeworkSort(index);
                execResult.setCreateTime(createTime);
                execResult.setEndTime(endTime);
                resultList.add(execResult);
            }
            boolean isExecuting = false;
            int executingTime = 0;
            for (ScriptExecEntity scriptExecEntity : resultList) {
                if (isExecuting) {
                    scriptExecEntity.setExecStatus(ScriptExecStatus.INIT.getStatus());
                    scriptExecEntity.setEndTime(null);
                } else {
                    if (scriptExecEntity.getExecStatus() == ScriptExecStatus.EXECUTING.getStatus()) {
                        executingTime = (int) (System.currentTimeMillis() - scriptExecEntity.getCreateTime().getTime());
                        isExecuting = true;
                        scriptExecEntity.setCostTime(executingTime);
                    }
                }
            }
            resultMap.put("resultList", resultList);
            resultMap.put("homeworkStatus", homeworkExecInfo.getExecStatus());
            resultMap.put("homeworkCostTime", homeworkExecInfo.getCostTime() + executingTime);
            //查询进度
            return Reply.ok(resultMap);
        } catch (Exception e) {
            log.error("查询作业步骤详情失败", e);
            return Reply.fail("查询作业步骤详情失败");
        }
    }

    /**
     * 更新作业执行结果
     *
     * @param map 结果数据
     * @return
     */
    @Override
    public Reply updateHomeworkExecScript(HashMap map) {
        try {
            log.error(this.getClass().getSimpleName() + "更新作业执行结果>>" + JSON.toJSONString(map)+"msg>>"+map.get("msg").toString());
            if (map.containsKey("key") && map.containsKey("data")) {
                int execId = (int) map.get("key");
                String ip = (String) map.get("data");
                int success = (int) map.get("success");
                Date createTime;
                long createDateTime = 0L;
                String createDateTimeString = (String) map.get("dateTime");
                String[] timeArray = createDateTimeString.split("\\.");
                createDateTime = Long.parseLong(timeArray[0]) * 1000 + Long.parseLong(timeArray[1].substring(0, 3));

                QueryWrapper<ScriptExecEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("delete_flag", false);
                wrapper.eq("exec_id", execId);
                wrapper.eq("assets_ip", ip);
                wrapper.eq("exec_status",0);
                List<ScriptExecEntity> scriptExeclist = scriptExecDao.selectList(wrapper);
                ScriptExecEntity scriptExec = scriptExeclist.get(0);

                if (true) {
                    if (createDateTime <= 0) {
                        createTime = scriptExec.getCreateTime();
                    } else {
                        createTime = new Date(createDateTime);
                        if (createTime.after(new Date())) {
                            createTime = scriptExec.getCreateTime();
                        }
                    }
                    scriptExec.setReturnContent((String) map.get("msg"));
                    long time = System.currentTimeMillis() - createTime.getTime();
                    scriptExec.setCostTime((int) time);
                    scriptExec.setCreateTime(createTime);
                    scriptExec.setExecStatus(success == 1 ? ScriptExecStatus.FINISHED.getStatus() : scriptExec.getIgnoreError() ? ScriptExecStatus.FINISHED.getStatus() : ScriptExecStatus.FAIL.getStatus());
                    scriptExec.setEndTime(new Date());
                    scriptExecDao.updateById(scriptExec);
                }

                //如果是作业下发,需要判断是否作业已经执行结束
                if (scriptExec.getHomeworkFlag()) {
                    //判断当前任务是否完全结束
                    int unFinishedCount = scriptExecDao.countUnFinishedExec(scriptExec.getHomeworkVersionId());
                    if (unFinishedCount == 0) {
                        int errorCount = scriptExecDao.countErrorExec(scriptExec.getHomeworkVersionId());
                        int costTime = scriptExecDao.countCostTime(scriptExec.getHomeworkVersionId());
                        UpdateWrapper<ScriptExecEntity> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("delete_flag", false)
                                .eq("homework_version_id", scriptExec.getHomeworkVersionId())
                                .eq("is_homework", false)
                                .eq("exec_status", ScriptExecStatus.EXECUTING.getStatus());
                        updateWrapper.set("cost_time", costTime);
                        updateWrapper.set("end_time", new Date());
                        if (errorCount == 0) {
                            updateWrapper.set("exec_status", ScriptExecStatus.FINISHED.getStatus());
                        } else {
                            updateWrapper.set("exec_status", ScriptExecStatus.FAIL.getStatus());
                        }
                        scriptExecDao.update(null, updateWrapper);
                    }
                }
            }
        } catch (Exception e) {
            log.error("更新作业执行结果失败", e);
        }
        return Reply.ok();
    }

    /**
     * 执行作业
     *
     * @param param 作业信息
     * @return
     */
    @Override
    @Transactional
    public synchronized Reply rePerformHomework(HomeworkParam param) {
        try {
            GlobalUserInfo userInfo = userService.getGlobalUser();
            //获取作业信息
//            HomeworkEntity homework = homeworkManageDao.selectById(param.getId());
//            if (homework == null || homework.getDeleteFlag()) {
//                return Reply.fail("查询作业失败,作业不存在");
//            }
            //获取最大作业版本ID
            int versionId = getMaxVersionId();
            int oldVersionId = param.getHomeworkVersionId();

            List<SpiderRequestInfo> requestList = new ArrayList<>();

            //执行作业----总
            //更新作业数据--step1
//            homework.setUpdateTime(new Date());
//            homework.setUpdater(userInfo.getLoginName());
//            homework.setHomeworkVersionId(versionId);
//            homeworkManageDao.updateById(homework);

            //获取作业关联列表---step2
            QueryWrapper<HomeworkRelationEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("homework_version_id", oldVersionId);
            wrapper.orderByAsc("homework_sort");
            List<HomeworkRelationEntity> relationList = homeworkRelationManageDao.selectList(wrapper);
            if (CollectionUtils.isEmpty(relationList)) {
                return Reply.fail("执行作业失败");
            }

            //获取执行记录
            QueryWrapper<ScriptExecEntity> homeworkExecInfo = new QueryWrapper<>();
            homeworkExecInfo.eq("homework_version_id", oldVersionId);
            homeworkExecInfo.eq("is_homework", false);
            ScriptExecEntity execEntity = scriptExecDao.selectList(homeworkExecInfo).get(0);

            //创建作业执行记录
            ScriptExecEntity homeworkExec = new ScriptExecEntity();
            homeworkExec.setId(getMaxScriptExecId());
            homeworkExec.setExecId(getMaxExecId());
            homeworkExec.setScriptName(execEntity.getScriptName());
            homeworkExec.setAssetsIP("无");
            homeworkExec.setAssetsPort("");
            homeworkExec.setCostTime(0);
            homeworkExec.setExecStatus(ScriptExecStatus.EXECUTING.getStatus());
            homeworkExec.setCreator(userInfo.getLoginName());
            homeworkExec.setExecType(1);
            homeworkExec.setMissionType(3);
            homeworkExec.setCreateTime(new Date());
            homeworkExec.setDeleteFlag(false);
            homeworkExec.setHomeworkFlag(false);
            homeworkExec.setHomeworkVersionId(versionId);
            scriptExecDao.insert(homeworkExec);

            for (HomeworkRelationEntity relation : relationList) {
                //获取最大执行ID
                int maxExecId = getMaxExecId();
                //创建关联关系,并保存数据
                HomeworkRelationEntity homeworkRelationEntity = new HomeworkRelationEntity();
                homeworkRelationEntity.setId(getMaxHomeworkRelId());
                homeworkRelationEntity.setExecId(maxExecId);
                homeworkRelationEntity.setHomeworkId(relation.getHomeworkId());
                homeworkRelationEntity.setHomeworkSort(relation.getHomeworkSort());
                homeworkRelationEntity.setHomeworkVersionId(versionId);
                homeworkRelationManageDao.insert(homeworkRelationEntity);

                //获取作业执行记录----step3
                QueryWrapper<ScriptExecEntity> execWrapper = new QueryWrapper<>();
                execWrapper.eq("homework_version_id", oldVersionId);
                execWrapper.eq("delete_flag", false);
                execWrapper.eq("is_homework", true);
                execWrapper.eq("exec_id", relation.getExecId());
                List<ScriptExecEntity> execItemList = scriptExecDao.selectList(execWrapper);

                //下发作业
                //执行IP列表
                List<String> ipList = new ArrayList<>();
                //执行账户列表
                List<SpiderRequestInfo.RequestAccount> accountList = new ArrayList<>();
                ScriptAccountEntity account;
                ScriptType accountType;
                int mindType = 0;
                //默认账户
                int defaultAccountId = 0;
                String targetFilePath = "";
                String sourceFilePath = "";
                ScriptType scriptType = null;
                boolean ignoreFlase = false;
                String script = "";
                for (ScriptExecEntity execItem : execItemList) {
                    execItem.setId(getMaxScriptExecId());
                    execItem.setExecId(maxExecId);
                    execItem.setCostTime(0);
                    execItem.setReturnContent("");
                    execItem.setExecStatus(ScriptExecStatus.INIT.getStatus());
                    execItem.setCreator(userInfo.getLoginName());
                    execItem.setCreateTime(new Date());
                    execItem.setHomeworkVersionId(versionId);
                    execItem.setHomeworkFlag(true);
                    scriptExecDao.insert(execItem);

                    //增加到列表中
                    ipList.add(execItem.getAssetsIP());
                    account = scriptAccountDao.selectById(execItem.getAccountId());
                    accountType = ScriptType.getScriptType(account.getSystemType());
                    SpiderRequestInfo.RequestAccount requestAccount = new SpiderRequestInfo.RequestAccount();
                    requestAccount.setUsername(account.getAccount());
                    requestAccount.setIp(execItem.getAssetsIP());
                    requestAccount.setPassword(EncryptsUtil.decrypt(account.getPassword()));
                    requestAccount.setAnsible_connection(accountType == ScriptType.CMD ? "winrm" : "");
                    requestAccount.setProt(Integer.parseInt(account.getPort()));
                    requestAccount.setType(accountType.getAccountTypeId());
                    accountList.add(requestAccount);

                    mindType = execItem.getMissionType();
                    defaultAccountId = execItem.getDefaultAccountId();
                    ignoreFlase = execItem.getIgnoreError();
                    script = execItem.getScriptContent();
                    if (mindType == 2 && StringUtils.isEmpty(targetFilePath)) {
                        FileTransParam fileTransParam = JSON.parseObject(execItem.getTransFileParam(), FileTransParam.class);
                        targetFilePath = fileTransParam.getTargetFilePath();
                        StringBuilder str = new StringBuilder();
                        for (FileTransParam.FilePathInfo fileInfo : fileTransParam.getFileNameList()) {
                            str.append(fileInfo.getFilePath()).append(",");
                        }
                        sourceFilePath = str.substring(0, str.length() - 1);
                    }
                    if (mindType == 1 && scriptType == null) {
                        scriptType = ScriptType.getScriptType(execItem.getScriptType());
                    }
                }

                ScriptAccountEntity defaultAccount = scriptAccountDao.selectById(defaultAccountId);
                ScriptType defaultAccountType = ScriptType.getScriptType(defaultAccount.getSystemType());

                SpiderRequestInfo requestInfo = new SpiderRequestInfo();
                requestInfo.setAppkey(maxExecId);
                requestInfo.setIpList(ipList);
                requestInfo.setPassword(EncryptsUtil.decrypt(defaultAccount.getPassword()));
                requestInfo.setProt(Integer.parseInt(defaultAccount.getPort()));
                requestInfo.setRoot(accountList);
                requestInfo.setType(defaultAccountType.getTypeId());
                requestInfo.setUsername(defaultAccount.getAccount());
                requestInfo.setNeglect(ignoreFlase);
                switch (mindType) {
                    //脚本执行
                    case 1:
                        requestInfo.setMindType(0);
                        requestInfo.setOrdeAddress("");
                        requestInfo.setScript(script);
                        requestInfo.setScriptType(scriptType.getTypeId());
                        break;
                    //文件分发
                    case 2:
                        requestInfo.setMindType(1);
                        requestInfo.setOrdeAddress(targetFilePath);
                        requestInfo.setScript(sourceFilePath);
                        requestInfo.setScriptType(ScriptType.SHELL.getTypeId());
                        break;
                    default:
                        break;
                }
                requestList.add(requestInfo);
            }
            //发送分发文件至spider
            sendRequestToSpider(requestList,spiderPythonUrl.split(",")[0]);
            return Reply.ok(versionId);
        } catch (Exception e) {
            log.error("执行失败", e);
            return Reply.fail("执行失败");
        }
    }

    @Override
    public void syncAccount(JSONArray jsonArray, GlobalUserInfo userInfo, SynAccount param) {
        for (int i = 0; i < jsonArray.size(); i++) {
            try {
                Map<String, String> map = new HashMap<>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                map = (Map<String, String>) jsonObject.get("accountAndKey");
                for (String s : map.keySet()) {
                    if (!map.get(s).equals("")) {
                        log.info("进入了");
                        Integer id = scriptAccountDao.selectPwdAnd(s, EncryptsUtil.encrypt(map.get(s)),null);
                        if (id == null) {
                            log.info("查询结果");
                            id = addJSONAccount(jsonObject, s, map, userInfo,param);
                            log.info("得到结果");
                        }

                        ScriptOutAsssets scriptOutAsssets = new ScriptOutAsssets();
                        scriptOutAsssets.setHostname(jsonObject.getString("name"));
                        scriptOutAsssets.setIp(jsonObject.getString("ip"));
                        scriptOutAsssets.setAccountId(id);
                        log.info("新增数据账号");
                        scriptAccountDao.insertScriptOut(scriptOutAsssets);
                        log.info("新增数据账号成功");
                    }
                    log.info("不存在堡垒机保存密码" + s);
                }
            } catch (Exception e) {
                log.info("这个资产存在问题:第" + i);
                log.error(e.toString());
            }
        }
    }

    @Override
    public void removAssets(List<Integer> ids) {
        scriptAccountDao.removAssets(ids);
    }

    @Override
    public Reply accountCreate(CreateAssets param) {
        //查询账号是不是唯一值

            Integer count = scriptAccountDao.selectCountAssets(param.getAccountId(), param.getIPAddress());
            if (count > 0) {
                return Reply.fail("无法创建相同资产的相同账号哦");
            } else {
                ScriptOutAsssets scriptOutAsssets = new ScriptOutAsssets();
                scriptOutAsssets.setAccountId(param.getAccountId());
                scriptOutAsssets.setIp(param.getIPAddress());
                scriptOutAsssets.setHostname(param.getAssetName());
                scriptAccountDao.insertScriptOut(scriptOutAsssets);
            }

        return Reply.ok("创建成功");
    }

    @Override
    public Reply editorAccount(CreateAssets param) {
        Integer count = scriptAccountDao.selectCountAssets(param.getAccountId(), param.getIPAddress());
        if (count > 1) {
            return Reply.fail("无法创建相同资产的相同账号哦");
        } else {
            ScriptOutAsssets scriptOutAsssets = new ScriptOutAsssets();
            scriptOutAsssets.setId(param.getId());
            scriptOutAsssets.setAccountId(param.getAccountId());
            scriptOutAsssets.setIp(param.getIPAddress());
            scriptOutAsssets.setHostname(param.getAssetName());
            scriptAccountDao.updateAssets(scriptOutAsssets);
        }
        return Reply.ok("创建成功");
    }

    @Override
    public Reply getAllAlertBrowse(MwHomeworkAlert mwHomeworkAlert) {
        PageHelper.startPage(mwHomeworkAlert.getPageNumber(), mwHomeworkAlert.getPageSize());
        List<MwHomeworkAlert>  mwHomeworkAlerts= scriptAccountDao.getAllAlertBrowse(mwHomeworkAlert);
        PageInfo pageInfo = new PageInfo<>(mwHomeworkAlerts);
        pageInfo.setList(mwHomeworkAlerts);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply alertCreate(MwHomeworkAlert mwHomeworkAlert) {
        scriptAccountDao.alertCreate(mwHomeworkAlert);
        return Reply.ok();
    }

    @Override
    public Reply alertEditor(MwHomeworkAlert mwHomeworkAlert) {
        scriptAccountDao.alertEditor(mwHomeworkAlert);
        return Reply.ok();
    }

    @Override
    public Reply alertDelete(List<MwHomeworkAlert> mwHomeworkAlert) {
        List<Integer> id = new ArrayList<>();
        for (MwHomeworkAlert m:mwHomeworkAlert) {
            id.add(m.getId());
        }
        scriptAccountDao.removeAlert(id);
        return Reply.ok();
    }

    @Override
    public Reply alertHomework(String alertExeHomework) {
        String []idString  = alertExeHomework.replace("[","").replace("]","").split(",");
        List<String> ids = Arrays.asList(idString);
        QueryWrapper<HomeworkEntity> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);
        List<HomeworkEntity> list = homeworkManageDao.selectList(wrapper);
        return Reply.ok(list);
    }

    @Override
    public Reply homeworkList(ListParamString listParamString) {
        List<MwHomeworkAlertMapper> mwHomeworkAlertMappers = scriptAccountDao.getListMwHomeWorkMapper(listParamString.id);
        List<Integer> versionid =  new ArrayList<>();
        for (MwHomeworkAlertMapper mwHomeworkAlertMapper:mwHomeworkAlertMappers) {
            versionid.add(mwHomeworkAlertMapper.getVersionId());
        }
        List<ScriptExecEntity> execList = new ArrayList<>();
        if (versionid.size()>0){
            PageHelper.startPage(listParamString.getPageNumber(), listParamString.getPageSize());
            QueryWrapper<ScriptExecEntity> wrapper = new QueryWrapper<>();
            wrapper.in("homework_version_id", versionid);
            wrapper.eq("mission_type",3);
            execList = scriptExecDao.selectList(wrapper);
            for (ScriptExecEntity entity : execList) {
                if (entity.getMissionType() == 2) {
                    FileTransParam fileTransParam = JSON.parseObject(entity.getTransFileParam(), FileTransParam.class);
                    entity.setFileTransParam(fileTransParam);
                }
            }
        }

        PageInfo pageInfo = new PageInfo<>(execList);
        pageInfo.setList(execList);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply HisBrowse(HomeworkHis param) {
        PageHelper.startPage(param.getPageNumber(), param.getPageSize());
        QueryWrapper<ScriptExecEntity> queryWrapper = new QueryWrapper<ScriptExecEntity>().eq("homework_id",param.getHomeworkId()).eq("delete_flag", false)
                .eq("homework_sort",param.getFatherId()).orderByDesc("create_time");
        if (param.getChildId()!=null&&!param.getChildId().equals("")){
            queryWrapper.eq("assets_id",param.getChildId());
        }


        List<ScriptExecEntity> scriptExecEntities = scriptExecDao.selectIsNotDelete(param);
        PageInfo pageInfo = new PageInfo<>(scriptExecEntities);
        pageInfo.setList(scriptExecEntities);
        return Reply.ok(pageInfo);
    }

    @Override
    public Reply HisBrowseCheck(HomeworkHis param) {
        List<ScriptExecEntity> scriptExecEntities = scriptExecDao.selectList(new QueryWrapper<ScriptExecEntity>().eq("homework_id",param.getHomeworkId()).groupBy("assets_ip"));
        List<FatherOption> fatherOptions = new ArrayList<>();
        for (ScriptExecEntity scriptExecEntity:scriptExecEntities) {
           boolean child = false;

            if (!scriptExecEntity.getAssetsIP().equals("无")){
              for (FatherOption fatherOption : fatherOptions){
                  if (fatherOption.getFatherId().equals(scriptExecEntity.getHomeworkSort().toString())){
                      child = true;
                  }
              }
                List<ChildrenOption> childrenOptions = new ArrayList<>();
                ChildrenOption childrenOption = new ChildrenOption();
                childrenOption.setChildId(scriptExecEntity.getAssetsId());
                childrenOption.setName(scriptExecEntity.getAssetsIP());
                FatherOption fatherOption = new FatherOption();
                if (child){
                    for (FatherOption fatherOptionsign : fatherOptions){
                        if (fatherOptionsign.getFatherId().equals(scriptExecEntity.getHomeworkSort().toString())){
                            childrenOptions = fatherOptionsign.getChildrenOptions();
                            fatherOption= fatherOptionsign;
                            childrenOptions.add(childrenOption);
                            fatherOption.setChildrenOptions(childrenOptions);
                        }
                    }
                }else {
                    fatherOption.setFatherId(scriptExecEntity.getHomeworkSort().toString()).setName(scriptExecEntity.getScriptName());
                    childrenOptions.add(childrenOption);
                    fatherOption.setChildrenOptions(childrenOptions);
                    fatherOptions.add(fatherOption);
                }

            }

        }

        return Reply.ok(fatherOptions);
    }

    @Override
    public Reply downById(HomeworkHis param, HttpServletResponse response) {

        OutputStream os = null;

        ScriptExecEntity scriptExecEntities = scriptExecDao.selectById(param.getId());

        QueryModelInstanceByPropertyIndexParamList queryModelInstanceByPropertyIndexParamList = new QueryModelInstanceByPropertyIndexParamList();
        List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
        QueryModelInstanceByPropertyIndexParam queryModelInstanceByPropertyIndexParam = new QueryModelInstanceByPropertyIndexParam();
        queryModelInstanceByPropertyIndexParam.setPropertiesIndexId(IN_BAND_IP);
        queryModelInstanceByPropertyIndexParam.setPropertiesValue(scriptExecEntities.getAssetsIP());
        paramLists.add(queryModelInstanceByPropertyIndexParam);
        queryModelInstanceByPropertyIndexParamList.setParamLists(paramLists);
        List<MwTangibleassetsDTO> mwTangibleassetsDTOS = new ArrayList<>();
        try {
            mwTangibleassetsDTOS=mwModelViewCommonService.findModelAssetsByRelationIds(queryModelInstanceByPropertyIndexParamList);
        }catch (Exception e){
            log.info("没对应资产");
        }
        String assetsName = "";
        if (mwTangibleassetsDTOS.size()>0){
            assetsName = mwTangibleassetsDTOS.get(0).getInstanceName()+"_";
        }else {
            assetsName = "";
        }
        Long time = new Date().getTime();
        String newFileName = assetsName+scriptExecEntities.getAssetsIP()+"_"+time;
        response.setContentType("application/force-download");
        response.setHeader("Content-Disposition", "attachment;fileName=" + newFileName);

        String str = scriptExecEntities.getReturnContent();
        try {
            os = response.getOutputStream();
            os.write(str.getBytes("UTF-8"));
        } catch (Exception e) {
            log.error("下载执行结果失败",e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.error("下载执行结果失败",e);
            }
        }
        return null;
    }

    @Override
    public void downZip(HomeworkHis param, HttpServletResponse response) {

        try {
            response.setContentType("application/octet-stream");
            response.setHeader("Accept-Ranges", "bytes");

            String fileName = URLEncoder.encode("资产配置备份_" + DateUtils.nowDate() + ".zip", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            QueryWrapper<ScriptExecEntity> queryWrapper = new QueryWrapper<ScriptExecEntity>().eq("homework_id",param.getHomeworkId())
                    .eq("homework_sort",param.getFatherId()).orderByDesc("assets_ip");
            if (param.getChildId()!=null&&!param.getChildId().equals("")){
                queryWrapper.eq("assets_id",param.getChildId());
            }
            if(param.getCreateStarttime()!=null){
                queryWrapper.gt("create_time", SeverityUtils.getDate((param.getCreateStarttime().getTime()-86400000l)/1000));
                queryWrapper.le("create_time",SeverityUtils.getDate((param.getCreateEndtime().getTime()+86400000l)/1000));
            }
            List<ScriptExecEntity> scriptExecEntities = scriptExecDao.selectList(queryWrapper);
            if (scriptExecEntities.size()>0){
                String beyound = "";
                ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
                String assetsName ="";

                for (ScriptExecEntity scriptExecEntity:scriptExecEntities) {
                    if (!beyound.equals(scriptExecEntity.getAssetsIP())){
                        QueryModelInstanceByPropertyIndexParamList queryModelInstanceByPropertyIndexParamList = new QueryModelInstanceByPropertyIndexParamList();
                        List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
                        QueryModelInstanceByPropertyIndexParam queryModelInstanceByPropertyIndexParam = new QueryModelInstanceByPropertyIndexParam();
                        queryModelInstanceByPropertyIndexParam.setPropertiesIndexId(IN_BAND_IP);
                        queryModelInstanceByPropertyIndexParam.setPropertiesValue(scriptExecEntity.getAssetsIP());
                        paramLists.add(queryModelInstanceByPropertyIndexParam);
                        queryModelInstanceByPropertyIndexParamList.setParamLists(paramLists);
                        List<MwTangibleassetsDTO> mwTangibleassetsDTOS = new ArrayList<>();
                        try {
                            mwTangibleassetsDTOS=mwModelViewCommonService.findModelAssetsByRelationIds(queryModelInstanceByPropertyIndexParamList);
                        }catch (Exception e){
                            log.info("没对应资产");
                        }
                        beyound= scriptExecEntity.getAssetsIP();
                        if (mwTangibleassetsDTOS.size()>0){
                            assetsName = mwTangibleassetsDTOS.get(0).getInstanceName();
                        }else {
                            assetsName = scriptExecEntity.getAssetsIP();
                        }
                        ZipEntry zipEntry = new ZipEntry(assetsName+"/");
                        zos.putNextEntry(zipEntry);
                        zos.closeEntry();
                    }
                    String loText = assetsName+"_"+scriptExecEntity.getCreateTime().getTime()+"_"+UUID.randomUUID().toString()+".config";
                    ZipEntry zipFile = new ZipEntry(assetsName+"/"+loText);
                    zos.putNextEntry(zipFile);
                    zos.write(scriptExecEntity.getReturnContent().getBytes());
                    zos.closeEntry();
                }
                zos.close();
            }else {
                updateResponse(response, "报告下载失败");
            }

        } catch (IOException e) {
            try {
                updateResponse(response, "报告下载失败");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        }

    }

    @Override
    public Reply getConText(ScriptManageParam param) {
        QueryWrapper queryWrapper = new QueryWrapper();
        if (param.getScriptName()!=null){
            queryWrapper.like("script_name",param.getScriptName());
        }

        List<ScriptManageEntity>  scripts = scriptManageDao.selectList(queryWrapper);
        return Reply.ok(scripts);
    }

    @Override
    public void exeportModel( HttpServletResponse response) throws IOException {
        List<String> lableName = Arrays.asList("账户名称","账户别名","密码","端口","描述","系统类别(linux,window,device)","资产名称","ip地址");
        ExportExcel.exportExcel("导入模板", "导入模板", lableName, null, null, "yyyy-MM-dd HH:mm:ss", response);
    }

    @Override
    public void exeportAccount(MultipartFile file, HttpServletResponse response, GlobalUserInfo userInfo) {
        try{
            byte[] byteArr = file.getBytes();
            InputStream inputStream = new ByteArrayInputStream(byteArr);
            HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
            //加载
            List<Map<String, Object>> maps = ExeclAll(workbook);
            for (Map <String,Object> map:maps) {
                Integer id = scriptAccountDao.selectPwdAnd(map.get("admin").toString(), EncryptsUtil.encrypt(map.get("password").toString()),map.get("port").toString());
                if (id == null) {
                    log.info("查询结果");
           /*         id = addJSONAccount(jsonObject, s, map, userInfo,param);*/
                    id = addMapAccount(map,userInfo);
                    log.info("得到结果");
                }

                ScriptOutAsssets scriptOutAsssets = new ScriptOutAsssets();
                scriptOutAsssets.setHostname(map.get("assetsName").toString());
                scriptOutAsssets.setIp(map.get("ipaddress").toString());
                scriptOutAsssets.setAccountId(id);
                log.info("新增数据账号");
                scriptAccountDao.insertScriptOut(scriptOutAsssets);
                log.info("新增数据账号成功");
            }
        }catch (Exception e){
             log.info("得到结果:",e);
        }

    }

    @Override
    public Reply deleteExecHistoryList(List<ScriptManageParam> param) {
        List<Integer> ids = new ArrayList<>();
        param.stream().forEach(e->{
            ids.add(e.getId());
        });
        if (ids.size()>0){
            scriptExecDao.deletebyIds(ids);
        }
        return null;
    }

    private Integer addMapAccount(Map<String, Object> map, GlobalUserInfo userInfo) {
        ScriptAccountEntity account = new ScriptAccountEntity();
        account.setId(getMaxScriptAccountId());
        account.setAccount(map.get("admin").toString());
        account.setPassword(EncryptsUtil.encrypt(map.get("password").toString()));
        if (map.get("adminAli").toString().equals("device")){
            account.setAccountAlias(map.get("adminAli").toString()+"-device");
        }else {
            account.setAccountAlias(map.get("adminAli").toString());
        }

        account.setPort(map.get("port").toString());
        account.setAccountDesc(map.get("desc").toString());
        if (map.get("sysytem").toString().equals("Windows")) {
            account.setSystemType("batchfile");
        } else {
            account.setSystemType("sh");
        }
        account.setCreator(userInfo.getLoginName());
        account.setCreateDate(new Date());
        account.setModifier(userInfo.getLoginName());
        account.setModificationDate(new Date());
        account.setDeleteFlag(false);
        log.info("新增数据");
        scriptAccountDao.insert(account);
        /*  mwCommonService.addMapperAndPerm(param);*/
        log.info("新增成功");
        return account.getId();
    }

    private List<Map<String, Object>> ExeclAll(HSSFWorkbook workbook) {
        List<Map<String, Object>> maps = new ArrayList<>();

        //获取第一个工作表

        HSSFSheet hs = workbook.getSheetAt(0);

        //获取Sheet的第一个行号和最后一个行号
        int last = hs.getLastRowNum();
        int first = hs.getFirstRowNum();
        //遍历获取单元格里的信息

        for (int i = first + 1; i < 100000; i++) {
            HSSFRow row = hs.getRow(i);
            if (row == null) {
                i = 1000001;
                continue;
            }
            boolean saveMap = true;
            int firstCellNum = 0;//获取所在行的第一个行号
            int lastCellNum = row.getLastCellNum();//获取所在行的最后一个行号
            Map<String, Object> map = new HashMap<>();
            for (int j = firstCellNum; j < firstCellNum + 8; j++) {
                HSSFCell cell = row.getCell(j);
                if (lastCellNum - firstCellNum < 1) {
                    saveMap = false;
                } else {
                    String value = " ";
                    try {
                        try {
                            cell.setCellType(CellType.STRING);
                            value = cell.getRichStringCellValue().getString();
                        } catch (Exception e) {

                        }
                        if (j == firstCellNum) {
                            map.put("admin", value);
                        } else if (j == firstCellNum + 1) {
                            map.put("adminAli", value);
                        }
                        else if (j == firstCellNum + 2) {
                            map.put("password", value);
                        }
                        else if (j == firstCellNum + 3) {
                            map.put("port", value);
                        }
                        else if (j == firstCellNum + 4) {
                            map.put("desc", value);
                        }
                        else if (j == firstCellNum + 5) {
                            map.put("sysytem", value);
                        }
                        else if (j == firstCellNum + 6) {
                            map.put("assetsName", value);
                        }
                        else if (j == firstCellNum + 7) {
                            map.put("ipaddress", value);
                        }
                    }catch (Exception e) {
                        j = 5;
                        saveMap = false;
                    }
                }
            }
            if (saveMap) {
                maps.add(map);
            }
        }


        return maps;

    }

    /**
     * 更新response，返回JSON格式数据
     * @param response
     * @param s
     * @throws IOException
     */
    private void updateResponse(HttpServletResponse response, String s) throws IOException {
        Reply reply = Reply.fail(s);
        ResponseBase responseBase = new ResponseBase(Constants.HTTP_RES_CODE_200, s, reply);
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().println(JSON.toJSONString(responseBase));
    }


    private Integer addJSONAccount(JSONObject jsonObject, String s, Map<String, String> map, GlobalUserInfo userInfo, SynAccount param) {
        ScriptAccountEntity account = new ScriptAccountEntity();
        account.setId(getMaxScriptAccountId());
        account.setAccount(s);
        account.setPassword(EncryptsUtil.encrypt(map.get(s)));
        Integer res = (int)(Math.random()*10000);
        account.setAccountAlias(s+res.toString());
        account.setPort("22");
        account.setAccountDesc("");
        if (jsonObject.getString("sysType").equals("Windows")) {
            account.setSystemType("batchfile");
        } else {
            account.setSystemType("sh");
        }
        account.setCreator(userInfo.getLoginName());
        account.setCreateDate(new Date());
        account.setModifier(userInfo.getLoginName());
        account.setModificationDate(new Date());
        account.setDeleteFlag(false);
        log.info("新增数据");
        scriptAccountDao.insert(account);
        param.setId(account.getId());
      /*  mwCommonService.addMapperAndPerm(param);*/
        log.info("新增成功");
        return account.getId();
    }

    /**
     * 获取ID最大值
     *
     * @return
     */
    private synchronized Integer getMaxHomeworkRelId() {
        //获取最大执行ID
        int maxId = 1;
        QueryWrapper<HomeworkRelationEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( id ) AS id");
        HomeworkRelationEntity maxEntity = homeworkRelationManageDao.selectOne(wrapper);
        if (maxEntity != null) {
            maxId += maxEntity.getId();
        }
        return maxId;
    }

    /**
     * 下发指令到各个服务器
     *
     * @param spiderRequestInfo 指令参数
     */
    private void sendRequestToSpider(List<SpiderRequestInfo> spiderRequestInfo,String ip) {
        try {
            OkHttpClient client = new OkHttpClient().newBuilder().build();
            MediaType mediaType = MediaType.parse("application/json");
            Map map = new HashMap();
            map.put("sendMinds", spiderRequestInfo);
            RequestBody body = RequestBody.create(mediaType, JSON.toJSONString(map));
            log.error("JSON INFO IS " + JSON.toJSONString(map));
            Request request = new Request.Builder()
                    .url(ip + "/admin/textMind")
                    .method("POST", body)
                    .build();
            Response response = client.newCall(request).execute();
            log.error("response is " + JSON.toJSONString(response));

            if (response.isSuccessful()) {
                for (SpiderRequestInfo info : spiderRequestInfo) {
                    UpdateWrapper<ScriptExecEntity> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("delete_flag", false).eq("exec_id",
                            info.getAppkey()).eq("exec_status", ScriptExecStatus.INIT.getStatus());
                    updateWrapper.set("exec_status", ScriptExecStatus.EXECUTING.getStatus());
                    scriptExecDao.update(null, updateWrapper);
                }
            } else {
                int execId = 0;
                for (SpiderRequestInfo info : spiderRequestInfo) {
                    execId = info.getAppkey();
                    UpdateWrapper<ScriptExecEntity> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("delete_flag", false).eq("exec_id",
                            info.getAppkey()).eq("exec_status", ScriptExecStatus.INIT.getStatus());
                    updateWrapper.set("exec_status", ScriptExecStatus.FAIL.getStatus())
                            .set("return_content", "请求接口失败" + JSON.toJSONString(response));
                    scriptExecDao.update(null, updateWrapper);
                }
                //更新作业执行状态
                QueryWrapper<ScriptExecEntity> wrapper = new QueryWrapper<>();
                wrapper.eq("exec_id", execId);
                ScriptExecEntity execEntity = scriptExecDao.selectList(wrapper).get(0);
                UpdateWrapper<ScriptExecEntity> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("delete_flag", false)
                        .eq("homework_version_id", execEntity.getHomeworkVersionId())
                        .eq("is_homework", false)
                        .eq("exec_status", ScriptExecStatus.EXECUTING.getStatus());
                updateWrapper.set("exec_status", ScriptExecStatus.FAIL.getStatus())
                        .set("return_content", "请求接口失败" + JSON.toJSONString(response));
                scriptExecDao.update(null, updateWrapper);
            }
        } catch (Exception e) {
            log.error("下发文件到各个服务器失败", e);
        }
    }

    /**
     * 获取最大版本ID
     *
     * @return 版本ID
     */
    private synchronized int getMaxVersionId() {
        //获取最大作业版本ID
        int versionId = 1;
        QueryWrapper<ScriptExecEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( homework_version_id )  as homeworkVersionId ");
        ScriptExecEntity maxRelation = scriptExecDao.selectOne(wrapper);
        if (maxRelation != null) {
            versionId += maxRelation.getHomeworkVersionId();
        }
        return versionId;
    }

    /**
     * 获取最大执行ID
     *
     * @return 执行ID
     */
    private synchronized int getMaxExecId() {
        //获取最大执行ID
        int maxExecId = 1;
        QueryWrapper<ScriptExecEntity> wrapper = new QueryWrapper<>();
        wrapper.select(" max( exec_id ) as execId");
        ScriptExecEntity maxExec = scriptExecDao.selectOne(wrapper);
        if (maxExec != null) {
            maxExecId += maxExec.getExecId();
        }
        return maxExecId;
    }

    @Async
    @Override
    public String triggerAuto(Map<String,String> map) throws Exception {
        String title = map.get("告警标题");
        Integer excType = 1;

        if (title==null||title.trim().equals("")){
            title = map.get("恢复标题");
            excType = 0;
        }
        log.info("告警标题："+title);
        MwHomeworkAlert mwHomeworkAlert = new MwHomeworkAlert();
        mwHomeworkAlert.setAlertTitle(title);
        List<MwHomeworkAlert> homeworkAlert = scriptAccountDao.getAllAlertBrowse(mwHomeworkAlert);
        for (MwHomeworkAlert h:homeworkAlert) {
            if (excType==h.getAlertType()){
                execHomeWotk(h);
            }
        }
        return "";
    }

    //执行脚本
    private void execHomeWotk(MwHomeworkAlert h) throws Exception {
        List<String> exeid = Arrays.asList(h.getAlertExeHomework().replace("[","").replace("]","").split(","));
        List<MwHomeworkAlertMapper> mwHomeworkAlertMappers = new ArrayList<>();
        for (String id:exeid) {
            HomeworkParam homeworkParam = new HomeworkParam();
            homeworkParam.setId(Integer.valueOf(id));
            Reply reply = performHomework(homeworkParam);
            Integer hoemworkversionid = (Integer) reply.getData();
            scriptAccountDao.addAlertNum(h.getId());
            MwHomeworkAlertMapper mwHomeworkAlertMapper = new MwHomeworkAlertMapper();
            mwHomeworkAlertMapper.setVersionId(hoemworkversionid);
            mwHomeworkAlertMapper.setHomeworkAlertId(h.getId());
            mwHomeworkAlertMappers.add(mwHomeworkAlertMapper);
        }
        if (mwHomeworkAlertMappers.size()>0){
            scriptAccountDao.addMwHomeWorkAlertMapper(mwHomeworkAlertMappers);
        }

    }

    //选择告警类型
    private Integer choiceType(String warring) {


        return 0;
    }
}
