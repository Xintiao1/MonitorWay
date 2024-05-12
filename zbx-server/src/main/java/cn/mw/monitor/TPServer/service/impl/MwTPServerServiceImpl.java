package cn.mw.monitor.TPServer.service.impl;

import cn.mw.monitor.TPServer.dao.MwTPServerTableDao;
import cn.mw.monitor.TPServer.dto.AddOrUpdateTPServerParam;
import cn.mw.monitor.TPServer.dto.MwTPServerDTO;
import cn.mw.monitor.TPServer.dto.QueryTPServerParam;
import cn.mw.monitor.TPServer.dto.TPServerDTO;
import cn.mw.monitor.TPServer.dto.TPServerDropdownDTO;
import cn.mw.monitor.TPServer.service.MwTPServerService;
import cn.mw.monitor.bean.DataPermission;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.service.engineManage.api.MwEngineCommonsService;
import cn.mw.monitor.service.tpserver.api.MwCommonsTPServer;
import cn.mw.monitor.service.tpserver.dto.MwTpServerCommonsDto;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.dto.DeleteDto;
import cn.mw.monitor.service.user.dto.InsertDto;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mw.monitor.util.MD5EncryptUtil;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.RSAUtils;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.HttpUtils;
import cn.mwpaas.common.utils.Md5Utils;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.codec.Charsets;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author syt
 * @Date 2020/10/30 16:01
 * @Version 1.0
 */
@Service
@Slf4j
@Transactional
public class MwTPServerServiceImpl implements MwTPServerService, MwCommonsTPServer {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/TPServer");

    @Resource
    private MwTPServerTableDao mwTPServerTableDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;
    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;
    @Autowired
    private MWCommonService mwCommonService;
    @Autowired
    private MwEngineCommonsService mwEngineCommonsService;

    @Autowired
    private MWOrgCommonService mwOrgCommonService;

    @Autowired
    private MWUserService mwUserService;

    private static final String ZABBIX_COOKIE_KEY="zbx_session";

    /**
     * 根据zabbix_server ID取zabbix_server信息
     *
     * @param id 自增序列ID
     * @return
     */
    @Override
    public Reply selectById(Integer id) {
        try {
            MwTPServerDTO mwTPServerDTO = mwTPServerTableDao.selectById(id);
            DataPermission dataPermission = mwCommonService.getDataPermissionDetail(DataType.MONITORING_SERVER, id + "");
            mwTPServerDTO.setDepartment(dataPermission.getDepartment());
            mwTPServerDTO.setPrincipal(dataPermission.getPrincipal());
            mwTPServerDTO.setGroup(dataPermission.getGroups());
            TPServerDTO TPServerDTO = CopyUtils.copy(TPServerDTO.class, mwTPServerDTO);

            String url = TPServerDTO.getMonitoringServerUrl();
            //获取第一个冒号的位置
            int index = url.indexOf(":");
            String protocol = url.substring(0,index).toUpperCase();
            index = url.indexOf(":",index+1);
            String port = url.substring(index +1, url.indexOf("/a"));
            TPServerDTO.setProtocol(protocol);
            TPServerDTO.setPort(port);
            TPServerDTO.setGroupIds(dataPermission.getGroupIds());
            TPServerDTO.setOrgIds(dataPermission.getOrgNodes());
            TPServerDTO.setPrincipal(dataPermission.getUserIds());
            logger.info("TPServer 根据自增id查询 TPServer信息成功 id={}", id);
            return Reply.ok(TPServerDTO);
        } catch (Exception e) {
            log.error("fail to selectById with id={}, cause:{}", id, e.getMessage());
            return Reply.fail(ErrorConstant.ZABBIX_SERVER_SELECT_CODE_311001, ErrorConstant.ZABBIX_SERVER_SELECT_MSG_311001);
        }
    }

    @Override
    public Reply selectList(QueryTPServerParam qsParam) {
        try {
            GlobalUserInfo userInfo = mwUserService.getGlobalUser();
            List<String> typeIdList = mwUserService.getAllTypeIdList(userInfo, DataType.MONITORING_SERVER);
            PageHelper.startPage(qsParam.getPageNumber(), qsParam.getPageSize());
            List<MwTPServerDTO> mwTPServers = new ArrayList();
            Map pubCriteria = PropertyUtils.describe(qsParam);
            pubCriteria.put("list", Joiner.on(",").join(typeIdList));
            pubCriteria.put("systemUser", userInfo.isSystemUser());
            mwTPServers = mwTPServerTableDao.selectPubList(pubCriteria);

            List<String> idList = new ArrayList<>();
            Map<String, DataPermission> dataPermissionMap = new HashMap<>();
            for (MwTPServerDTO server : mwTPServers) {
                idList.add(server.getId() + "");
            }
            List<DataPermission> dataPermissionList = mwCommonService.getDataAuthByIds(DataType.MONITORING_SERVER, idList);
            for (DataPermission permission : dataPermissionList) {
                dataPermissionMap.put(permission.getId(), permission);
            }
            for (MwTPServerDTO server : mwTPServers) {
                DataPermission permission = dataPermissionMap.get(server.getId() + "");
                if (permission != null){
                    server.setDepartment(permission.getDepartment());
                    server.setPrincipal(permission.getPrincipal());
                    server.setGroup(permission.getGroups());
                }
            }

            for (MwTPServerDTO mwTPServerDTO : (List<MwTPServerDTO>) mwTPServers) {
                mwTPServerDTO.setMonitoringServerPassword("******");
            }
            PageInfo pageInfo = new PageInfo<>(mwTPServers);
            pageInfo.setList(mwTPServers);
            logger.info("分页查询TPServer信息成功[]{}[]", mwTPServers);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("fail to selectList with qsParam={}, cause:{}", qsParam, e.getMessage());
            return Reply.fail(ErrorConstant.ZABBIX_SERVER_SELECT_CODE_311001, ErrorConstant.ZABBIX_SERVER_SELECT_MSG_311001);
        } finally {
            log.info("remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }

    @Override
    public Reply update(AddOrUpdateTPServerParam auParam) {
//        if (Strings.isNullOrEmpty(auParam.getMonitoringServerUrl())) {
//            StringBuffer sUrl = new StringBuffer();
//            sUrl.append(auParam.getProtocol()).append("://")
//                    .append(auParam.getMonitoringServerIp())
//                    .append(":")
//                    .append(auParam.getPort())
//                    .append("/api_jsonrpc.php");
//            auParam.setMonitoringServerUrl(sUrl.toString());
//        }
        if (null != auParam.getMonitoringServerIp() && !"".equals(auParam.getMonitoringServerIp())) {
            List<TPServerDropdownDTO> checkList = mwTPServerTableDao.check(auParam.getMonitoringServerIp());
            if (checkList != null && checkList.size() > 0) {
                if (checkList.get(0).getId() != auParam.getId()) {
                    return Reply.fail(ErrorConstant.ZABBIX_SERVER_INSERT_CODE_311003, "监控服务器不可重复添加！");
                }
            }
        }

        if (auParam.getMainServer()) {//当被设置成主监控器服务器时做校验
            List<TPServerDropdownDTO> list = mwTPServerTableDao.selectByMainServerIsTrue();

            if (list != null && list.size() > 0) {
                if (list.get(0).getId() != auParam.getId()) {
                    return Reply.fail(ErrorConstant.ZABBIX_SERVER_INSERT_CODE_311003, "已有监控服务器被设置成主监控服务器，请重新设置");
                }
            }
        }
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        mwTPServerTableDao.update(auParam);
        //删除负责人
        deleteMapperAndPerm(auParam.getId());
        //添加负责人
        addMapperAndPerm(auParam);

        return Reply.ok("更新成功！");

    }

    @Override
    public Reply delete(List<Integer> ids) throws Throwable {
        //先判断有没有资产关联TPServer
        List<String> list = mwTPServerTableDao.selectAssetsByMonitorServer(ids);
        //判断有没有引擎关联TPServer
        List<String> list1 = mwTPServerTableDao.selectEngineByMonitorServer(ids);
        if (list.size() > 0 || list1.size() > 0) {
            throw new Throwable("所删除的监控服务器中有关联资产或者引擎，无法删除");
        }
        //删除TPServer数据
        mwTPServerTableDao.delete(ids);
//        mwEngineCommonsService.deleteEngineByMonitorServerIds(ids);
        ids.forEach(
                id -> {
                    //删除负责人
                    deleteMapperAndPerm(id);
                });

        return Reply.ok("删除成功");
    }

    @Override
    public Reply insert(AddOrUpdateTPServerParam auParam) {
//        if (Strings.isNullOrEmpty(auParam.getMonitoringServerUrl())) {
//            StringBuffer sUrl = new StringBuffer();
//            sUrl.append(auParam.getProtocol()).append("://")
//                    .append(auParam.getMonitoringServerIp())
//                    .append(":")
//                    .append(auParam.getPort())
//                    .append("/api_jsonrpc.php");
//            auParam.setMonitoringServerUrl(sUrl.toString());
//        }
        if (null != auParam.getMonitoringServerIp() && !"".equals(auParam.getMonitoringServerIp())) {
            List<TPServerDropdownDTO> checkList = mwTPServerTableDao.check(auParam.getMonitoringServerIp());
            if (checkList != null && checkList.size() > 0) {
                return Reply.fail(ErrorConstant.ZABBIX_SERVER_INSERT_CODE_311003, "监控服务器不可重复添加！");
            }
        }

        if (auParam.getMainServer()) {//当被设置成主监控器服务器时做校验
            List<TPServerDropdownDTO> list = mwTPServerTableDao.selectByMainServerIsTrue();
            if (list != null && list.size() > 0) {
                return Reply.fail(ErrorConstant.ZABBIX_SERVER_INSERT_CODE_311003, "已有监控服务器被设置成主监控服务器，请重新设置");
            }
        }
        auParam.setCreator(iLoginCacheInfo.getLoginName());
        auParam.setModifier(iLoginCacheInfo.getLoginName());
        auParam.setDeleteFlag(false);
        mwTPServerTableDao.insert(auParam);
        //添加负责人
        addMapperAndPerm(auParam);

        return Reply.ok("新增成功！");
    }

    @Override
    public Reply selectDropdownListByType(boolean selectFlag) {
        try {
            logger.info("根据监控服务类型查询监控服务器，获取下拉列表成功");
            return Reply.ok(selectFlag ? mwTPServerTableDao.selectDropdownList() : mwTPServerTableDao.selectDropdownListByType());

        } catch (Exception e) {
            log.error("fail to selectDropdownListByType", e);
            return Reply.fail(ErrorConstant.ZABBIX_SERVER_SELECT_CODE_311001, ErrorConstant.ZABBIX_SERVER_SELECT_MSG_311001);
        }
    }

    /**
     * 删除负责人
     *
     * @param id
     */
    private void deleteMapperAndPerm(Integer id) {
        DeleteDto deleteDto = DeleteDto.builder().typeId(id.toString()).type(DataType.MONITORING_SERVER.getName()).build();
        mwCommonService.deleteMapperAndPerm(deleteDto);
    }

    /**
     * 添加负责人
     *
     * @param uParam
     */
    private void addMapperAndPerm(AddOrUpdateTPServerParam uParam) {
        InsertDto insertDto = InsertDto.builder()
                .groupIds(uParam.getGroupIds())
                .userIds(uParam.getPrincipal())
                .orgIds(uParam.getOrgIds())
                .typeId(String.valueOf(uParam.getId()))
                .type(DataType.MONITORING_SERVER.getName())
                .desc(DataType.MONITORING_SERVER.getDesc()).build();
        //添加负责人
        mwCommonService.addMapperAndPerm(insertDto);
    }

    @Override
    public Reply selectByMainServer() {
        int monitorServerId = 0;
        List<TPServerDropdownDTO> tpServerDropdownDTOS = mwTPServerTableDao.selectByMainServerIsTrue();
        if (tpServerDropdownDTOS.size() > 0) {//如果有主就使用主的监控服务器id
            monitorServerId = tpServerDropdownDTOS.get(0).getId();
        } else {
            //随机选取一个从监控服务器id
            List<TPServerDropdownDTO> tpServerS = mwTPServerTableDao.selectDropdownListByType();
            if (tpServerS.size() > 0) {
                monitorServerId = tpServerS.get(0).getId();
            }
        }
        return Reply.ok(monitorServerId);
    }

    /**
     * 监控服务器模糊搜索所有字段联想
     *
     * @param value
     * @return
     */
    @Override
    public Reply fuzzSearchAllFiledData(String value) {
        //根据值模糊查询数据
        List<Map<String, String>> fuzzSeachAllFileds = mwTPServerTableDao.fuzzSearchAllFiled(value);
        Set<String> fuzzSeachData = new HashSet<>();
        if (!CollectionUtils.isEmpty(fuzzSeachAllFileds)) {
            for (Map<String, String> fuzzSeachAllFiled : fuzzSeachAllFileds) {
                String monitoring_server_name = fuzzSeachAllFiled.get("monitoring_server_name");
                String monitoring_server_ip = fuzzSeachAllFiled.get("monitoring_server_ip");
                String monitoring_server_type = fuzzSeachAllFiled.get("monitoring_server_type");
                String monitoring_server_version = fuzzSeachAllFiled.get("monitoring_server_version");
                String modifier = fuzzSeachAllFiled.get("modifier");
                String creator = fuzzSeachAllFiled.get("creator");
                if (StringUtils.isNotBlank(monitoring_server_name) && monitoring_server_name.contains(value)) {
                    fuzzSeachData.add(monitoring_server_name);
                }
                if (StringUtils.isNotBlank(monitoring_server_ip) && monitoring_server_ip.contains(value)) {
                    fuzzSeachData.add(monitoring_server_ip);
                }
                if (StringUtils.isNotBlank(monitoring_server_type) && monitoring_server_type.contains(value)) {
                    fuzzSeachData.add(monitoring_server_type);
                }
                if (StringUtils.isNotBlank(monitoring_server_version) && monitoring_server_version.contains(value)) {
                    fuzzSeachData.add(monitoring_server_version);
                }
                if (StringUtils.isNotBlank(creator) && creator.contains(value)) {
                    fuzzSeachData.add(creator);
                }
                if (StringUtils.isNotBlank(modifier) && modifier.contains(value)) {
                    fuzzSeachData.add(modifier);
                }
            }
        }
        Map<String, Set<String>> fuzzyQuery = new HashMap<>();
        fuzzyQuery.put("fuzzyQuery", fuzzSeachData);
        return Reply.ok(fuzzyQuery);
    }

    @Override
    public Reply getZabbixTemplateSession(String hostIp) {
        Map params = new HashMap();
        // 读取主监控服务器
        params.put("mainServer", true);
        params.put("monitoringServerType", "Zabbix");
        List<MwTPServerDTO> mwTPServers = mwTPServerTableDao.selectPubList(params);
        if (CollectionUtils.isEmpty(mwTPServers)){
            return Reply.fail("zabbix service is null!");
        }
        String userName=mwTPServers.get(0).getMonitoringServerUser();
        String passWord=RSAUtils.decryptData(mwTPServers.get(0).getMonitoringServerPassword(), RSAUtils.RSA_PRIVATE_KEY);
        String url=String.format(Locale.ENGLISH,"http://%s/index.php?request=templates.php&name=%s&password=%s&autologin=1&enter=登录",hostIp,userName,passWord);
        return Reply.ok(getZabbixSessionId(url));
    }

    public static String getZabbixSessionId(String url){
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            URI uri = builder.build();

            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                Header[] headers= response.getHeaders("set-Cookie");
                for (Header header:headers) {
                    if (header.getValue()!=null&&header.getValue().indexOf(ZABBIX_COOKIE_KEY)==0){
                        return header.getValue();
                    }
                }
            }
        } catch (Exception e) {
            log.error("get Zabbix session error!",e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public List<MwTpServerCommonsDto> selectServerIdInfoByIp(List<String> ips) {
        return mwTPServerTableDao.selectTpServerByIps(ips);
    }
}
