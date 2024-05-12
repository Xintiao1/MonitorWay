package cn.mw.monitor.user.dto;

public class CaptchaDTO {
    private String id;
    private String captcha;

    public CaptchaDTO(String id, String captcha){
        this.id = id;
        this.captcha = captcha;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }
}
