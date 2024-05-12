package cn.mw.monitor.model.util.virtual;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vijava.com.vmware.vim25.mo.ServerConnection;
import vijava.com.vmware.vim25.mo.ServiceInstance;

import java.net.URL;

/**
 * @author qzg
 * @version 1.1
 * @description 操作vcenter的连接和断开，以及定义公共应用类
 * @date 2017年2月8日14:35:38
 */
@Slf4j
@Service
public class ConnectedVimServiceBase {
    public ServiceInstance si = null;

    /**
     * @description 链接vcenter
     * @date 2022年8月23
     * @version 1.0
     * @author qzg
     */
    public void connect(String url, String userName, String passWord) {
        try {
            if (url.indexOf("/sdk") != -1) {
                si = new ServiceInstance(new URL(url), userName, passWord, true);
            }else{
                si = new ServiceInstance(new URL("https://" + url + "/sdk"), userName, passWord, true);
            }
        } catch (Exception e) {
            log.error("连接失败",e);
        }
    }

    /**
     * @description 断开vcenter链接
     * @date 2022年8月23
     * @version 1.0
     * @author qzg
     */
    public void disconnect() {
        try {
            si.getServerConnection().logout();
        } catch (Exception e) {
        }
    }

    public ServiceInstance loginVCenter(String ip, String userName, String passWord) {
        try {
            si = new ServiceInstance(new URL("https://" + ip + "/sdk"), userName, passWord, true);
        } catch (Exception e) {
            log.error("fail to connect");
        } finally {
            try {
                disconnect();
            } catch (Exception e) {
                log.error("fail to disconnect");
            }
        }
        return si;
    }

    /**
     * @description 获取链接URL
     * @date 2022年8月23
     * @version 1.0
     * @author qzg
     */
    public URL getUrl() {
        ServerConnection serverConnection = si.getServerConnection();
        URL url = null;
        if (serverConnection != null) {
            url = serverConnection.getUrl();
        } else {
            return null;
        }
        return url;
    }
}

