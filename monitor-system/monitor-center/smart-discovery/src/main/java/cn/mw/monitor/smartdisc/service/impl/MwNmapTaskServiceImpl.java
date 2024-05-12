package cn.mw.monitor.smartdisc.service.impl;

import cn.mw.monitor.api.common.IpV4Util;
import cn.mw.monitor.api.exception.CheckInsertNmapTaskException;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.service.smartDiscovery.param.QueryNmapTaskParam;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.common.util.CopyUtils;
import cn.mw.monitor.service.smartDiscovery.api.MWNmapTaskService;
import cn.mw.monitor.service.smartDiscovery.param.AddUpdateNmapTaskParam;
import cn.mw.monitor.service.smartDiscovery.param.QueryNmapResultParam;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.service.user.model.MWOrg;
import cn.mw.monitor.smartdisc.common.NmapXMLUtil;
import cn.mw.monitor.smartdisc.dao.MWNmapGroupDao;
import cn.mw.monitor.smartdisc.dao.MWNmapTaskDao;
import cn.mw.monitor.smartdisc.model.*;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.models.auth.In;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class MwNmapTaskServiceImpl implements MWNmapTaskService {

    @Resource
    private MWNmapTaskDao mwNmapTaskDao;

    @Resource
    private MWNmapGroupDao mwNmapGroupDao;

    @Autowired
    private ILoginCacheInfo loginCacheInfo;

    @Override
    public Reply insert(AddUpdateNmapTaskParam param) {
        try {
            MWNmapExploreTask mwNmapExploreTask = CopyUtils.copy(MWNmapExploreTask.class,param);
            int count = mwNmapTaskDao.selectTaskByName(mwNmapExploreTask.getTaskName());
            if (count > 0 ) {
                throw new CheckInsertNmapTaskException(ErrorConstant.NMAP_315007, ErrorConstant.NMAP_MSG_315007);
            }
            mwNmapExploreTask.setDetectTimes(1);
            String creator = loginCacheInfo.getLoginName();
            mwNmapExploreTask.setCreator(creator);
            mwNmapExploreTask.setModifier(creator);
            if (StringUtils.isNotBlank(mwNmapExploreTask.getPortGroupKey())) {
                MWNmapPortGroup portGroup = mwNmapGroupDao.selectPortGroupById(Integer.parseInt(mwNmapExploreTask.getPortGroupKey()));
                mwNmapExploreTask.setPortRange("TCP:"+portGroup.getTcpPortGroup()+" "+"UDP:"+portGroup.getUdpPortGroup());
            }else {
                mwNmapExploreTask.setPortRange("TCP:"+mwNmapExploreTask.getTcpPortGroup()+" "+"UDP:"+mwNmapExploreTask.getUdpPortGroup());
            }

            if (StringUtils.isNotBlank(mwNmapExploreTask.getNodeGroupKey())) {
                mwNmapExploreTask.setDetectTargetInput(mwNmapExploreTask.getNodeGroupKey());
            }
            //验证ipv4输入格式
            if (StringUtils.isNotBlank(mwNmapExploreTask.getDetectTargetInput())) {
                String[] ips = mwNmapExploreTask.getDetectTargetInput().split(",");
                for (String ip : ips) {
                    //IP地址验证
                    if (ip.indexOf("/") == -1 && ip.indexOf("-") == -1) {
                        boolean validIpv4Addr = IpV4Util.isValidIpv4Addr(ip);
                        if (!validIpv4Addr) {
                            throw new CheckInsertNmapTaskException(ErrorConstant.NMAP_315008, ErrorConstant.NMAP_MSG_315008);
                        }
                    }
                    //IP地址段格式验证
                    if (ip.indexOf("/") != -1) {
                        String[] ipAdress = ip.split("/");
                        if (ipAdress.length != 2) {
                            return Reply.fail("请输入正确格式的ip地址段!");
                        } else {
                            String sip0 = ipAdress[0];//ip
                            Integer icird1 = null;
                            try {
                                icird1 = Integer.parseInt(ipAdress[1]);//段
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
                    }
                }
            }

            //存为节点组
            if (mwNmapExploreTask.getIsSavedNode()) {
                mwNmapGroupDao.insertNodeGroup(mwNmapExploreTask.getNodeName(),mwNmapExploreTask.getDetectTargetInput());
            }
            //存为端口组
            if (mwNmapExploreTask.getIsSavedPort()) {
                mwNmapGroupDao.insertPortGroup(mwNmapExploreTask.getPortName(),mwNmapExploreTask.getTcpPortGroup(),mwNmapExploreTask.getUdpPortGroup());
            }

            //存为指纹探测节点组
            if (mwNmapExploreTask.getIsFingerScan()) {
                mwNmapGroupDao.insertFingerDetectGroup(mwNmapExploreTask.getDetectTargetInput());
            }

            //存为IP存活探测节点组
            if (mwNmapExploreTask.getIpLiveDetect()) {
                mwNmapGroupDao.insertIpLiveDetectGroup(mwNmapExploreTask.getLiveDetectNodeName(),mwNmapExploreTask.getDetectTargetInput());
            }
            //存为例外IP组
            if (mwNmapExploreTask.getIsSavedExceptionIPGroup()) {
                mwNmapGroupDao.insertExceptionIPGroup(mwNmapExploreTask.getExceptionIPName(),mwNmapExploreTask.getExceptionIPInput());
            }
            mwNmapTaskDao.insertNmapTask(mwNmapExploreTask);
            MWNmapExploreTask task = mwNmapTaskDao.selectNmapTaskByName(mwNmapExploreTask.getTaskName());
            String customStartTime = task.getCustomStartTime();
            if ("1".equals(task.getRunWay())) {
                startNmapDetect(task);
            }else if ("3".equals(task.getRunWay())) {
                timerTask(customStartTime,task);
            }
            }catch (Exception e) {
                if (e instanceof CheckInsertNmapTaskException) {
                    throw new CheckInsertNmapTaskException(e.hashCode(),e.toString());
                } else {
                    throw new ServiceException(
                            Reply.fail(ErrorConstant.NMAP_315001, ErrorConstant.NMAP_MSG_315001));
                }
        }
        return Reply.ok("新增成功！");
    }

    /*指定时间开启扫描*/
    public void timerTask(String customStartTime,MWNmapExploreTask task) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date parse = sdf.parse(customStartTime);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @SneakyThrows
            @Override
            public void run() {
                startNmapDetect(task);
            }
        },parse);
    }

    @Override
    public Reply selectResult(QueryNmapResultParam param) {
        try {
//            PageHelper.startPage(param.getPageNumber(),param.getPageSize());
//            List<MWNmapIpService> ipServices = mwNmapTaskDao.selectIpService(param.getTaskId());
            List<MWNmapIpServiceList> ipServicesAll = new ArrayList<>();
            List<Integer> ipIdList = mwNmapTaskDao.selectIpByTaskId(param.getTaskId());

            for (Integer ipId : ipIdList) {
                MWNmapIpServiceList list = new MWNmapIpServiceList();
                List<MWNmapIpService> clilds = new ArrayList<>();
                List<MWNmapIpService> ipServices = new ArrayList<>();
                ipServices  =  mwNmapTaskDao.selectServiceByIpId(ipId);
                for (int i = 0; i < ipServices.size(); i++) {
                    if (i == 0 ) {
                        MWNmapIpService service = ipServices.get(i);
                        list.setIp(service.getIp());
                        list.setIpId(service.getIpId());
                        list.setIpType(service.getIpType());
                        list.setOsType(service.getOsType());
                        list.setAgreement(service.getAgreement());
                        list.setExtraInfo(service.getExtraInfo());
                        list.setPort(service.getPort());
                        list.setProduct(service.getProduct());
                        list.setHostName(service.getHostName());
                        list.setReason(service.getReason());
                        list.setReasonTTL(service.getReasonTTL());
                        list.setState(service.getState());
                        list.setServiceName(service.getServiceName());

                    }else {
                        clilds.add(ipServices.get(i));
                    }
                    list.setIpChilds(clilds);
                }
                ipServicesAll.add(list);
            }
            /* for (Integer ipId :ipIdList) {
                List<MWNmapIpService> ipServices = new ArrayList<>();
                ipServices  =  mwNmapTaskDao.selectServiceByIpId(ipId);
                parent.add(ipServices);
                ipServicesAll.add(ipServices);
            }*/
//            PageInfo<?> pageInfo = new PageInfo<>(ipServicesAll);
            log.info("ACCESS_LOG[]user[]自动化发现[]根据任务ID获取任务扫描结果[]{}", param);
            return Reply.ok(ipServicesAll);
        } catch (Exception e) {
            log.error("fail to selectResult with taskId={}", e);
            return Reply.fail(ErrorConstant.NMAP_315004, ErrorConstant.NMAP_MSG_315004);
        }
    }

    @Override
    public Reply pageUser(QueryNmapTaskParam qParam) {

        try {

            PageHelper.startPage(qParam.getPageNumber(),qParam.getPageSize());
            Map describe = PropertyUtils.describe(qParam);
            List<MWNmapExplore> explores = mwNmapTaskDao.selectList(describe);
            PageInfo<?> pageInfo = new PageInfo<>(explores);

            log.info("ACCESS_LOG[]nmap[]nmap扫描[]分页查询扫描任务信息[]{}[]", qParam);
            return Reply.ok(pageInfo);

        } catch (Exception e) {
            log.error("fail to pageUser with param={}", qParam, e);
            return Reply.fail(ErrorConstant.NMAP_315002, ErrorConstant.NMAP_MSG_315002);
        }
    }

    @Override
    public Reply selectNmapTaskDetails(QueryNmapResultParam param) {
        try {
            MWNmapExploreTask mwNmapExploreTask = mwNmapTaskDao.selectTaskById(param.getTaskId());

            log.info("ACCESS_LOG[]user[]自动化发现[]查询NMAP任务详情[]{}", param);
            return Reply.ok(mwNmapExploreTask);
        } catch (Exception e) {
            log.error("fail to selectTaskDetails with taskId={}", e);
            return Reply.fail(ErrorConstant.NMAP_315003, ErrorConstant.NMAP_MSG_315003);
        }
    }

    @Override
    public Reply updateNmapTask(AddUpdateNmapTaskParam param) {

        try {
            MWNmapExploreTask task = CopyUtils.copy(MWNmapExploreTask.class,param);
            if (StringUtils.isNotBlank(task.getPortGroupKey())) {
                MWNmapPortGroup portGroup = mwNmapGroupDao.selectPortGroupById(Integer.parseInt(task.getPortGroupKey()));
                task.setPortRange("TCP:"+portGroup.getTcpPortGroup()+" "+"UDP:"+portGroup.getUdpPortGroup());
            }else {
                task.setPortRange("TCP:"+task.getTcpPortGroup()+" "+"UDP:"+task.getUdpPortGroup());
            }
            if (StringUtils.isNotBlank(task.getNodeGroupKey())) {
                task.setDetectTargetInput(task.getNodeGroupKey());
            }
            //验证ipv4输入格式
            if (StringUtils.isNotBlank(task.getDetectTargetInput())) {
                String[] ips = task.getDetectTargetInput().split(",");
                for (String ip : ips) {
                    //IP地址验证
                    if (ip.indexOf("/") == -1 && ip.indexOf("-") == -1) {
                        boolean validIpv4Addr = IpV4Util.isValidIpv4Addr(ip);
                        if (!validIpv4Addr) {
                            throw new CheckInsertNmapTaskException(ErrorConstant.NMAP_315008, ErrorConstant.NMAP_MSG_315008);
                        }
                    }
                    //IP地址段格式验证
                    if (ip.indexOf("/") != -1) {
                        String[] ipAdress = ip.split("/");
                        if (ipAdress.length != 2) {
                            return Reply.fail("请输入正确格式的ip地址段!");
                        } else {
                            String sip0 = ipAdress[0];//ip
                            Integer icird1 = null;
                            try {
                                icird1 = Integer.parseInt(ipAdress[1]);//段
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
                    }
                }
            }
            task.setModifier(loginCacheInfo.getLoginName());
            mwNmapTaskDao.updateTask(task);

        }catch (Exception e) {
                if (e instanceof CheckInsertNmapTaskException) {
                    throw e;
                } else {
                    throw new ServiceException(
                            Reply.fail(ErrorConstant.NMAP_315005, ErrorConstant.NMAP_MSG_315005));
                }
        }

        return Reply.ok("更新成功！");
    }

    @Override
    public Reply runNmapTask(QueryNmapResultParam param) {

        try {
            MWNmapExploreTask task = mwNmapTaskDao.selectTaskById(param.getTaskId());
            if (task.getDetectTimes() >= 1) {
                mwNmapTaskDao.deleteExceptTask(task.getId());
            }
            startNmapDetect(task);
            task.setDetectTimes(task.getDetectTimes()+1);
            mwNmapTaskDao.updateTask(task);
        }catch (Exception e) {
            log.error("fail to update runNmapTask with taskName={}", param.getTaskName(), e);
            Reply.fail(ErrorConstant.NMAP_315006, ErrorConstant.NMAP_MSG_315006);
            throw new ServiceException(
                    Reply.fail(ErrorConstant.NMAP_315010, ErrorConstant.NMAP_MSG_315010));
        }

        return Reply.ok("重启扫描成功！");

    }

    @Override
    public Reply delete(List<Integer> idList) {

        try {
            mwNmapTaskDao.delete(idList);
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("fail to deleteOrg with ids={}", idList, e);
            return Reply.fail(ErrorConstant.NMAP_315009, ErrorConstant.NMAP_MSG_315009);

        }
    }

    public void startNmapDetect(MWNmapExploreTask task) throws Exception {
        StringBuilder commandSu = new StringBuilder();
        StringBuilder commandSt = new StringBuilder();
        StringBuilder command = new StringBuilder();
        LocalDateTime taskStartTime = LocalDateTime.now();
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String ipInput = task.getDetectTargetInput();
        String[] ipStr = ipInput.split(",");

        //将探测目标与例外IP进行比较去重
        List<String> exceptionIPList = new ArrayList<>();
        List<String> detectRemove = new ArrayList<>();
        if (StringUtils.isNotBlank(task.getExceptionIPInput())) {
            String[] split = task.getExceptionIPInput().split(",");
            exceptionIPList = Arrays.asList(split);
            detectRemove = getListRemoveExceptionIp(ipStr);
            detectRemove.removeAll(exceptionIPList);
        }else {
            detectRemove = getListRemoveExceptionIp(ipStr);
        }

        //存活性检测
        if (task.getIpLiveDetect()) {
            command.append(" "+"-sP");
        }

        //指纹扫描
        if (task.getIsFingerScan()) {
            command.append(" "+"-sV");
        }

        //探测速度
        if (task.getDetectSpeed() == 1) {
            command.append(" "+"-T4");
        }else {
            command.append(" "+"-T2");
        }

        if (StringUtils.isNotBlank(task.getPortGroupKey())) {
            MWNmapPortGroup portGroup = mwNmapGroupDao.selectPortGroupById(Integer.parseInt(task.getPortGroupKey()));
            task.setUdpPortGroup(portGroup.getUdpPortGroup());
            task.setTcpPortGroup(portGroup.getTcpPortGroup());
        }
        //临时保存命令信息
        String commandTemp = command.toString();

        //UDP端口扫描
        if (StringUtils.isNotBlank(task.getUdpPortGroup())) {
            command.setLength(0);
            command = new StringBuilder(commandTemp);
            command.append(" "+"-sU -p"+" "+task.getUdpPortGroup());
            for (String ip :detectRemove) {
                String  temp = command+"";
                LocalDateTime startTime = LocalDateTime.now();
                commandSu.append(command).append(" "+ip);
                List<Map<String, Object>> portList = NmapXMLUtil.getResultFromNmap(ip,commandSu);
                insertAll(portList,task,startTime);
                commandSu.setLength(0);
                command.setLength(0);
                command = new StringBuilder(temp);
            }
        }

        //TCP端口扫描
        if (StringUtils.isNotBlank(task.getTcpPortGroup())) {
            command.setLength(0);
            command = new StringBuilder(commandTemp);
            command.append(" "+"-sT -p"+" "+task.getTcpPortGroup());
            for (String ip :detectRemove) {
                String  temp = command+"";
                LocalDateTime startTime = LocalDateTime.now();
                commandSt.append(command).append(" "+ip);
                List<Map<String, Object>> portList = NmapXMLUtil.getResultFromNmap(ip,commandSt);
                insertAll(portList,task,startTime);
                commandSt.setLength(0);
                command.setLength(0);
                command = new StringBuilder(temp);
            }
        }
        //不指定端口 默认扫描最常用10000端口 --top-ports 10000
        if(StringUtils.isBlank(task.getUdpPortGroup()) && StringUtils.isBlank(task.getTcpPortGroup())) {
            command.setLength(0);
            command = new StringBuilder(commandTemp);
            command.append(" "+"--top-ports 10000");
            for (String ip :detectRemove) {
                String  temp = command+"";
                LocalDateTime startTime = LocalDateTime.now();
                commandSt.append(command).append(" "+ip);
                List<Map<String, Object>> portList = NmapXMLUtil.getResultFromNmap(ip,commandSt);
                insertAll(portList,task,startTime);
                commandSt.setLength(0);
                command.setLength(0);
                command = new StringBuilder(temp);
            }
        }




        String startEndTime = dt.format(taskStartTime)+"-"+dt.format(LocalDateTime.now());
        task.setStartEndTime(startEndTime.trim());
        mwNmapTaskDao.updateTask(task);
        //保存非存活IP
        if (task.getIsSavedNonLiveData()) {
            List<String> nonLiveIP = new ArrayList<>();
            nonLiveIP.addAll(detectRemove);
            List<String> liveIp = mwNmapTaskDao.selectLiveIPByTaskId(task.getId());
            nonLiveIP.removeAll(liveIp);
            StringBuilder strIp = new StringBuilder();
            for (String str : nonLiveIP) {
                strIp.append(str).append(";");
            }
            mwNmapTaskDao.insertNonLiveIP(task.getId(),strIp.toString());
        }
    }

    //根据IPV4地址段获取IP
    public List<String> isIPAdress(String ipAddresses) throws Exception {
        //根据auParam 获取所有的ip地址集合(去重)
        List<String> list = new ArrayList<>();
        //获取  ip地址地址段  内所有ip
        if(ipAddresses!=null&&!ipAddresses.equals("")){
            String[] str = ipAddresses.split("/");
            list = IpV4Util.parseIpMaskRangeInclude(str[0],str[1],0,256);
        }
        return list;
    }

    public void insertAll(List<Map<String, Object>> portList,MWNmapExploreTask task,LocalDateTime startTime) {
        LocalDateTime taskStartTime = LocalDateTime.now();
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Object ipId = mwNmapTaskDao.selectMaxId();
        if (ipId == null) {
            ipId = 1;
        }else {
            ipId = (int)ipId +1 ;
        }

        Object o = mwNmapTaskDao.selectMaxServiceId();
        int serviceId = 0;
        if (o != null) {
            serviceId = (int) o;
        }
        List<Integer> serviceIds = new ArrayList<>();
        if (portList != null && portList.size() > 0) {
            MWNmapIp nmapIp = new MWNmapIp();
            int count = 0;
            List<String> osTypeList = new ArrayList<>();
            for (Map<String, Object> map : portList) {
                serviceId++;
                serviceIds.add(serviceId);
                count++;

                MWNmapService service = new MWNmapService();
                service.setServiceName(((String) map.get("serviceName")).trim()!=null?((String) map.get("serviceName")).trim():"");
                service.setPort(((String) map.get("port")).trim()!=null?((String) map.get("port")).trim():"");
                service.setState(((String) map.get("state")).trim()!=null?((String) map.get("state")).trim():"");
                service.setAgreement(((String) map.get("agreement")).trim()!=null?((String) map.get("agreement")).trim():"");
                service.setExtraInfo(((String) map.get("extraInfo")).trim()!=null?((String) map.get("extraInfo")).trim():"");
                service.setProduct(((String) map.get("product")).trim()!=null?((String) map.get("product")).trim():"");
                service.setReason(((String) map.get("reason")).trim()!=null?((String) map.get("reason")).trim():"");
                service.setReasonTTL(((String) map.get("reasonTTL")).trim()!=null?((String) map.get("reasonTTL")).trim():"");
                mwNmapTaskDao.insertService(service);

                nmapIp.setIp(((String) map.get("ip")).trim());
//                nmapIp.setIpType(((String) map.get("ipType")).trim());
                if ("ipv4 ".equals(map.get("ipType"))) {
                    nmapIp.setIpType("IPv4");
                }else if ("ipv6 ".equals(map.get("ipType"))){
                    nmapIp.setIpType("IPv6");
                }
                nmapIp.setHostName(((String) map.get("hostName")).trim()!=null?((String) map.get("hostName")).trim():"");
                osTypeList.add(map.get("osType").toString()!=null?((String) map.get("osType")).trim():"");
            }
            int maxLength = Integer.MIN_VALUE;
            List<String> maxList = new ArrayList<>();
            for (String s : osTypeList) {
                if (s.length() > maxLength) {
                    maxLength = s.length();
                    maxList.clear();
                    maxList.add(s);
                } else if (s.length() == maxLength) {
                    maxList.add(s);
                }
            }
            String osType = maxList.get(0);
            nmapIp.setId((Integer) ipId);
            nmapIp.setOsType(osType.trim());
            nmapIp.setServiceCount(count);

            nmapIp.setStartTime(dt.format(startTime));
            nmapIp.setEndTime(dt.format(LocalDateTime.now()));
            mwNmapTaskDao.insertIp(nmapIp);

            int taskId = mwNmapTaskDao.selectTaskIdByName(task.getTaskName());
            mwNmapTaskDao.insertTaskIp(taskId, (Integer) ipId);

            mwNmapTaskDao.insertIpService((Integer) ipId,serviceIds);
        }
    }

    /*把IP地址范围分成IP*/
    public List<String> getIp(String ip) {
        List<String> ipList = new ArrayList<>();
        String[] split = ip.split("-");
        String from = split[0]; //起始Ip
        String to = split[1];   //结束Ip

        String share = from.substring(0, from.lastIndexOf(".") + 1);
        int start = Integer.parseInt(from.substring(from.lastIndexOf(".") + 1, from.length()));
        int end = Integer.parseInt(to.substring(to.lastIndexOf(".") + 1, to.length()));

        for (int i = start;i<=end;i++) {
            String ipStr = share + i;
            ipList.add(ipStr);
        }
        return ipList;
    }

    /*
    * 将探测目标与例外IP进行去重处理
    * */
    public List<String> getListRemoveExceptionIp(String[] ipStr) throws Exception {

        List<String> detectAllIP = new ArrayList<>();
        for (String ipDetect : ipStr) {
            List<String> ipDetectList = new ArrayList<>();
            if (ipDetect.indexOf("-") != -1) {
                List<String> ipList = new ArrayList<>();
                ipList = getIp(ipDetect);
                detectAllIP.addAll(ipList);
            }else if (ipDetect.indexOf("/") != -1) {
                List<String> ipAdress = new ArrayList<>();
                ipAdress = isIPAdress(ipDetect);
                detectAllIP.addAll(ipAdress);
            }else {
                ipDetectList.add(ipDetect);
                detectAllIP.addAll(ipDetectList);
            }
        }
        //探测目标IP去重
        List<String> removeList = new ArrayList<>();
        Set<String> set = new HashSet<>();
        for (String ip :detectAllIP) {
            if (set.add(ip)) {
                removeList.add(ip);
            }
        }
        detectAllIP.clear();
        detectAllIP.addAll(removeList);

        return detectAllIP;
    }
}
