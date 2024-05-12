package cn.mw.monitor.screen.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class MapAlertConfig {
    @Value("${screen.linkErrorColor}")
    private String linkErrorColor;

    @Value("${screen.linkNormalColor}")
    private String linkNormalColor;

    @Value("${screen.normalKey}")
    private String normalKey;

    @Value("${screen.alertKey}")
    private String alertKey;
}
