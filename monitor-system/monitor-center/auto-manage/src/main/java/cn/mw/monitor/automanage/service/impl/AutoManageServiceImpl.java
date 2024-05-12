package cn.mw.monitor.automanage.service.impl;

import cn.mw.monitor.automanage.constant.Constant;
import cn.mw.monitor.automanage.entity.AutoManageEntity;
import cn.mw.monitor.automanage.param.AutoManageParam;
import cn.mw.monitor.automanage.param.NacosInstance;
import cn.mw.monitor.automanage.param.NacosResponse;
import cn.mw.monitor.automanage.service.AutoManageService;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author gui.quanwang
 * @className AutoManageServiceImpl
 * @description 自动化运维服务
 * @date 2022/4/2
 */
@Service
@Slf4j
public class AutoManageServiceImpl implements AutoManageService {

    @Value("${auto-manage.server.host}")
    private String SERVER_HOST;

    @Autowired
    private MWUserService userService;

    private final static Map<Integer, String> SERVER_MAP = new HashMap<>();

    private final static AtomicInteger ATOMIC_INTEGER = new AtomicInteger();

    private final static String SEPARATOR = ":";

    /**
     * 获取服务列表
     *
     * @return
     */
    @Override
    public Reply getServerList() {
        List<Map> serverList = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("serverName", Constant.SERVER_NAME);
        serverList.add(map);
        return Reply.ok(serverList);
    }

    /**
     * 获取自动化参数
     *
     * @param param 请求参数
     * @return
     */
    @Override
    public Reply getAutoManageList(AutoManageParam param) {
        try {
            List<AutoManageEntity> list = getServerInstanceList();
            cacheInstanceMap(list);
            int fromIndex = param.getPageSize() * (param.getPageNumber() - 1);
            int toIndex = param.getPageSize() * param.getPageNumber();
            if (fromIndex < 0 || fromIndex > list.size()) {
                fromIndex = 0;
                toIndex = param.getPageSize();
            }
            if (toIndex > list.size()) {
                toIndex = list.size();
            }
            PageInfo pageInfo = new PageInfo(list.subList(fromIndex, toIndex));
            pageInfo.setPageSize(param.getPageSize());
            pageInfo.setPageNum(param.getPageNumber());
            pageInfo.setTotal(list.size());
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("获取服务列表失败", e);
            return Reply.fail(ErrorConstant.AUTO_MANAGE_SELECT_BASE_CODE_320001,
                    ErrorConstant.AUTO_MANAGE_SELECT_BASE_MSG_320001);
        }
    }


    /**
     * 根据服务名称搜索子服务
     *
     * @param serverName 服务名称
     */
    @Override
    public Reply searchServerInstance(String serverName) {
        try {
            return Reply.ok();
        } catch (Exception e) {
            log.error("获取子服务列表失败", e);
            return Reply.fail("搜索实例化列表失败");
        }
    }

    /**
     * 更新实例状态
     *
     * @param id     实例ID
     * @param enable true:上线  false:下线
     */
    @Override
    public Reply updateServerInstance(int id, boolean enable) {
        try {
            //获取实例数据
            String ipPort = SERVER_MAP.get(id);
            if (StringUtils.isEmpty(ipPort) || !ipPort.contains(SEPARATOR)) {
                return Reply.ok();
            }
            String[] netInfo = ipPort.split(SEPARATOR);
            if (StringUtils.isEmpty(SERVER_HOST)) {
                SERVER_HOST = Constant.SERVER_HOST;
            }
            //先更新nacos数据
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            FormBody formBody = new FormBody.Builder()
                    .add("serviceName", Constant.SERVER_NAME)
                    .add("ip", netInfo[0])
                    .add("port", netInfo[1])
                    .add("enabled", String.valueOf(enable))
                    .build();
            Request request = new Request.Builder()
                    .url(SERVER_HOST + Constant.UPDATE_INSTANCE_INFO)
                    .put(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return Reply.ok();
            } else {
                return Reply.fail("更新失败！");
            }
        } catch (Exception e) {
            log.error("实例上下线失败", e);
            return Reply.fail("更新实例状态失败");
        }
    }

    /**
     * 缓存数据
     *
     * @param list
     */
    private void cacheInstanceMap(List<AutoManageEntity> list) {
        for (AutoManageEntity entity : list) {
            SERVER_MAP.put(entity.getId(), entity.getServerIp() + SEPARATOR + entity.getServerPort());
        }
    }

    /**
     * 获取NACOS服务实例化列表
     *
     * @return
     */
    private List<AutoManageEntity> getServerInstanceList() {
        List<AutoManageEntity> serverList = new ArrayList<>();
        try {
            if (StringUtils.isEmpty(SERVER_HOST)) {
                SERVER_HOST = Constant.SERVER_HOST;
            }
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            Request request = new Request.Builder()
                    .url(SERVER_HOST + Constant.GET_INSTANCE_LIST + "?serviceName=" + Constant.SERVER_NAME)
                    .method("GET", null)
                    .build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                NacosResponse nacosResponse = JSON.parseObject(response.body().string(), NacosResponse.class);
                if (CollectionUtils.isNotEmpty(nacosResponse.getHosts())) {
                    serverList = updateInstance(nacosResponse.getHosts());
                }
            }
        } catch (Exception e) {
            log.error("获取nacos服务实例化列表失败", e);
            serverList = new ArrayList<>();
        }
        return serverList;
    }

    /**
     * 更新实例
     *
     * @param instanceList
     */
    private List<AutoManageEntity> updateInstance(List<NacosInstance> instanceList) {
        //获取当前用户
        GlobalUserInfo userInfo = userService.getGlobalUser();
        List<AutoManageEntity> serverList = new ArrayList<>();
        //更新数据
        for (NacosInstance instance : instanceList) {
            AutoManageEntity serverInstance = new AutoManageEntity();
            serverInstance.setId(ATOMIC_INTEGER.incrementAndGet());
            serverInstance.setServerName(instance.getServiceName());
            serverInstance.setServerIp(instance.getIp());
            serverInstance.setServerPort(instance.getPort());
            serverInstance.setCreateTime(new Date());
            serverInstance.setDeleteFlag(false);
            serverInstance.setServerEnable(instance.getEnabled());
            serverInstance.setVersion(instance.getWeight());
            serverInstance.setUpdateTime(new Date());
            serverInstance.setUpdater(userInfo.getLoginName());
            serverInstance.setCreator(userInfo.getLoginName());
            serverList.add(serverInstance);
        }
        return serverList;
    }
}
