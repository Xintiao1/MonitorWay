package cn.mw.monitor.service.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Created by zy.quaee on 2021/6/4 11:40.
 **/
@Data
@Builder
public class SettingCaptchaDTO {
    /**
     * 验证码开关
     */
    private boolean enableCaptcha;

    private SettingDTO settingDTO;

}
