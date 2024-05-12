package cn.mw.zbx;

import cn.mw.monitor.TPServer.dao.MwTPServerTableDao;
import cn.mw.monitor.TPServer.dto.MwTPServerDTO;
import cn.mw.monitor.TPServer.dto.QueryTPServerParam;
import cn.mw.monitor.TPServer.model.TPServerTypeEnum;
import cn.mw.monitor.api.common.SpringUtils;
import cn.mw.monitor.util.RSAUtils;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MWTPServerProxy implements InvocationHandler {

    private static boolean debug = false;

    public static final char VERSION_SEP = '_';

    public static final char DATA_SEP = '.';

    private static Object lock = new Object();

    private static Map<Integer, MWTPServerAPI> zabbixMap;

    private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        synchronized (lock) {
            if (null == zabbixMap) {
                doRefresh();
            }
        }
        int serverId = Integer.parseInt(args[0].toString());
        LocalDateTime start = LocalDateTime.now();
        if(debug){
            log.info("MWTPServerProxy invoke start:" + serverId + ";start:" + start.toLocalTime());
        }
        //qzg修改，如果获取不到值，重新调用doRefresh()
        //-------------------
        MWTPServerAPI mwtpServerAPI = null;

        mwtpServerAPI = zabbixMap.get(serverId);
        //多线程访问时,在锁范围内再加上一次判断,保证其他的线程刷新后,自己不用再去执行刷新
        if (zabbixMap.get(serverId) == null) {
            synchronized (lock) {
                if(zabbixMap.get(serverId) == null) {
                    doRefresh();
                }
            }
            mwtpServerAPI = zabbixMap.get(serverId);
        }

        if(null == mwtpServerAPI){
            return null;
        }

        Object obj = method.invoke(mwtpServerAPI, args);
        LocalDateTime end = LocalDateTime.now();
        //-------------------
        if(debug) {
            Duration duration = Duration.between(start, end);
            log.info("MWTPServerProxy invoke end:" + Thread.currentThread().getName()
                    + ";duration:" + duration.toMillis()
                    + ";end:" + end.toLocalTime());
        }
        return obj;
    }

    public static void refresh() throws Exception{
        synchronized(lock){
            doRefresh();
        }

        zabbixMap.forEach((key, value) ->{
            log.info(value.message());
        });
    }

    private static void doRefresh() throws Exception {
        log.info("MWTPServerProxy init zabbixMap:" + Thread.currentThread().getName());
        MwTPServerTableDao mwTPServerTableDao = (MwTPServerTableDao)SpringUtils.getBean("mwTPServerTableDao");
        QueryTPServerParam qsParam = new QueryTPServerParam();
        qsParam.setMonitoringServerType("Zabbix");
        PageHelper.startPage(0, Integer.MAX_VALUE);
        Map pubCriteria = PropertyUtils.describe(qsParam);
        List<MwTPServerDTO> mwTPServers = mwTPServerTableDao.selectPubList(pubCriteria);

        //初始化map
        zabbixMap = new ConcurrentHashMap<>();
        for(MwTPServerDTO mwTPServerDTO : mwTPServers) {
            String monitoringServerVersion = mwTPServerDTO.getMonitoringServerVersion().replace(DATA_SEP, VERSION_SEP);
            String enumKey = mwTPServerDTO.getMonitoringServerType() + monitoringServerVersion;
            String passWdReal = mwTPServerDTO.getMonitoringServerPassword();
            if(mwTPServerDTO.getEncryptedFlag()) {
                passWdReal = RSAUtils.decryptData(mwTPServerDTO.getMonitoringServerPassword(), RSAUtils.RSA_PRIVATE_KEY);
            }
            TPServerTypeEnum tpServerTypeEnum = TPServerTypeEnum.valueOf(enumKey);
            try {
                switch (tpServerTypeEnum) {
                    case Zabbix6_0:
                        MWZabbixApiV6 mwZabbixApiV6 = new MWZabbixApiV6();
                        mwZabbixApiV6.setServerId(mwTPServerDTO.getId());
                        mwZabbixApiV6.setZabbixUrl(mwTPServerDTO.getMonitoringServerUrl());
                        mwZabbixApiV6.setZabbixUser(mwTPServerDTO.getMonitoringServerUser());
                        mwZabbixApiV6.setZabbixPassword(passWdReal);
                        mwZabbixApiV6.setDebug(debug);
                        mwZabbixApiV6.setServerType(TPServerTypeEnum.Zabbix6_0);
                        mwZabbixApiV6.init();
                        zabbixMap.put(mwTPServerDTO.getId(), mwZabbixApiV6);
                        break;
                    case Zabbix5_0:
                        MWZabbixApiV5 mwZabbixApiV5 = new MWZabbixApiV5();
                        mwZabbixApiV5.setServerId(mwTPServerDTO.getId());
                        mwZabbixApiV5.setZabbixUrl(mwTPServerDTO.getMonitoringServerUrl());
                        mwZabbixApiV5.setZabbixUser(mwTPServerDTO.getMonitoringServerUser());
                        mwZabbixApiV5.setZabbixPassword(passWdReal);
                        mwZabbixApiV5.setDebug(debug);
                        mwZabbixApiV5.setServerType(TPServerTypeEnum.Zabbix5_0);
                        mwZabbixApiV5.init();
                        zabbixMap.put(mwTPServerDTO.getId(), mwZabbixApiV5);
                        break;
                    case Zabbix4_0:
                        MWZabbixApiV4 mwZabbixApiV4 = new MWZabbixApiV4();
                        mwZabbixApiV4.setServerId(mwTPServerDTO.getId());
                        mwZabbixApiV4.setZabbixUrl(mwTPServerDTO.getMonitoringServerUrl());
                        mwZabbixApiV4.setZabbixUser(mwTPServerDTO.getMonitoringServerUser());
                        mwZabbixApiV4.setZabbixPassword(passWdReal);
                        mwZabbixApiV4.setDebug(debug);
                        mwZabbixApiV4.setServerType(TPServerTypeEnum.Zabbix4_0);
                        mwZabbixApiV4.init();
                        zabbixMap.put(mwTPServerDTO.getId(), mwZabbixApiV4);
                        break;
                    default:
                        log.error("MWTPServerProxy error server:" + mwTPServerDTO.getMonitoringServerName()
                                + ";id:" + mwTPServerDTO.getId());
                }
            }catch (Exception e){
                log.error("doRefresh", e);
            }
        }
    }

    public static List<MWTPServerAPI> getMWTPServerAPIList(){
        List<MWTPServerAPI> list = new ArrayList<>();
        try {
            if (null == zabbixMap) {
                refresh();
            }
            zabbixMap.forEach((key, value) -> {
                list.add(value);
            });
        }catch (Exception e){
            log.error("getMWTPServerAPIList{}", e);
        }
        return list;
    }
}
