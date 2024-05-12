package cn.mw.monitor.user.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class ADProperties {
    @Value("${user.ldap.connect.timeout}")
    private int timeout;
}
