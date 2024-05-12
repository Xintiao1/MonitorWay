package cn.mw.monitor.shiro;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.servlet.KaptchaExtend;
import com.google.code.kaptcha.util.Config;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;


@Component
public class KaptchaExtendPlus  extends KaptchaExtend implements InitializingBean {
    /*------------------------------配置属性----------------------------------------------------*/
    @Value("${kaptcha.border}")
    private String kaptchaBorder;

    @Value("${kaptcha.color}")
    private String kaptchaColor;

    @Value("${kaptcha.space}")
    private String kaptchaSpace;

    @Value("${kaptcha.string}")
    private String kaptchaString;

    @Value("${kaptcha.noiseImpl}")
    private String kaptchaNoiseImpl;



    /*------------------------------调用属性----------------------------------------------------*/
    private Properties props = new Properties();
    private Producer kaptchaProducer = null;
    private String sessionKeyValue = null;
    private String sessionKeyDateValue = null;


    public KaptchaExtendPlus() {

    }

    public void captcha(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Cache-Control", "no-store, no-cache");
        resp.setContentType("image/jpeg");
        String capText = this.kaptchaProducer.createText();
        req.getSession().setAttribute(this.sessionKeyValue, capText);
        req.getSession().setAttribute(this.sessionKeyDateValue, new Date());
        BufferedImage bi = this.kaptchaProducer.createImage(capText);
        ServletOutputStream out = resp.getOutputStream();
        ImageIO.write(bi, "jpg", out);
        req.getSession().setAttribute(this.sessionKeyValue, capText);
        req.getSession().setAttribute(this.sessionKeyDateValue, new Date());
    }

    public String getGeneratedKey(HttpServletRequest req) {
        HttpSession session = req.getSession();
        return (String)session.getAttribute("KAPTCHA_SESSION_KEY");
    }



    @Override
    public void afterPropertiesSet() throws Exception {
        ImageIO.setUseCache(false);
        this.props.put("kaptcha.border", kaptchaBorder);
        this.props.put("kaptcha.textproducer.font.color", kaptchaColor);
        this.props.put("kaptcha.textproducer.char.space", kaptchaSpace);
        this.props.put("kaptcha.textproducer.char.string", kaptchaString);
        this.props.put("kaptcha.noise.impl", kaptchaNoiseImpl);

        Config config = new Config(this.props);
        this.kaptchaProducer = config.getProducerImpl();
        this.sessionKeyValue = config.getSessionKey();
        this.sessionKeyDateValue = config.getSessionDate();
    }
}
