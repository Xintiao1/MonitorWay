package cn.mw.monitor.service.tpserver.api;

import cn.mw.monitor.service.tpserver.dto.MwTpServerCommonsDto;
import cn.mwpaas.common.model.Reply;

import java.util.List;

/**
 * @author syt
 * @Date 2020/12/12 20:43
 * @Version 1.0
 */
public interface MwCommonsTPServer {
    Reply selectByMainServer();

    //根据IP查询监控服务器ID
    List<MwTpServerCommonsDto> selectServerIdInfoByIp(List<String> ips);

}
