package cn.mw.xiangtai.plugin.service.impl;

import cn.mw.xiangtai.plugin.domain.entity.SyslogHeartbeatEntity;
import cn.mw.xiangtai.plugin.mapper.SyslogHeartbeatMapper;
import cn.mw.xiangtai.plugin.service.SyslogHeartbeatService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional("mclickhouseTransactionManager")
public class SyslogHeartbeatServiceImpl extends ServiceImpl<SyslogHeartbeatMapper, SyslogHeartbeatEntity> implements SyslogHeartbeatService {
}
