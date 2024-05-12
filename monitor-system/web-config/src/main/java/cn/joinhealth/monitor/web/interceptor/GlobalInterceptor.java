package cn.joinhealth.monitor.web.interceptor;



import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by yeshengqi on 2019/5/13.
 */
@Data
public class GlobalInterceptor extends HandlerInterceptorAdapter {

    private List<String> accessControlAllowOrigins;

    private static Logger logger = LoggerFactory.getLogger(GlobalInterceptor.class);


    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {


        String refer = request.getHeader("referer");
        StringBuffer urlsb = request.getRequestURL();
        logger.info("请求url地址：{}", urlsb + ";refer:" + refer);

        String allowOrigin = null;

        if(null != refer){
            allowOrigin = refer;
        }else{
            allowOrigin = urlsb.toString();
        }

        if (null != allowOrigin) {
            for (String origin : accessControlAllowOrigins) {
                if(allowOrigin.indexOf(origin) >= 0){
                    response.setHeader("Access-Control-Allow-Origin",allowOrigin);
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
                    response.setHeader("Access-Control-Allow-Headers", "Authorization,Origin, X-Requested-With, Content-Type, Accept,Access-Token");
                    break;
                }
            }
        }

        return true;
    }

}
