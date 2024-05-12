package cn.mw.monitor.model.param;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/7/28
 */
@Data
public class ModelHttpParam {
    private String url;
    private String username;
    private String password;
    private String cookie;
    private String token;
    //请求发送参数
    private String paramData;

}
