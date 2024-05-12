package cn.mw.monitor.util;

import cn.mw.monitor.state.UploadCatalog;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UploadUrlUtils implements InitializingBean {
    @Value("${file.url}")
    public   String uploadUrl;
    @Value("${basicUrl}")
    public   String basicUrl;

    /**
     * @param flag 不同上传地址
     * @param imgUrl 图片url 如 1.png
     * @param module 不同类型图片目录
     * @return
     */
    public  String transferUrl(int flag,String imgUrl,String module){

        switch (UploadCatalog.getByValue(flag)){
            case BASIC:
                return basicUrl+"/"+module+"/"+imgUrl;
            case UPLOAD:
                return uploadUrl+"/"+module+"/"+imgUrl;
            default:
                return null;

        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
