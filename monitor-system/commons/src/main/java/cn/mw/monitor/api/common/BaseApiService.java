package cn.mw.monitor.api.common;


/**
 * 封装为restfull风格
 */
public class BaseApiService {

    private  static String SYSTEM_ALTER = "系统内部报错，请联系管理员!";
    // 失败
    public ResponseBase setResultFail(String msg, Object data) {
        return new ResponseBase(Constants.HTTP_RES_CODE_500, msg, data);
    }

    // 失败
    public ResponseBase setActiviti(String msg, Object data) {
        return new ResponseBase(Constants.HTTP_RES_CODE_202, msg, data);
    }

    // 请求验证码
    public ResponseBase setResultGetCode(String msg, Object data) {
        return new ResponseBase(Constants.HTTP_RES_CODE_401, msg, data);
    }


    // 请求验证码
    public ResponseBase setResultPermit(String msg, Object data) {
        return new ResponseBase(Constants.HTTP_RES_CODE_301, msg, data);
    }
    // 警告
    public ResponseBase setResultWarn(Object data) {
        return new ResponseBase(Constants.HTTP_RES_CODE_300, null, data);
    }

    // 成功
    public ResponseBase setResultSuccess(Object data) {
        return new ResponseBase(Constants.HTTP_RES_CODE_200, Constants.HTTP_RES_CODE_200_VALUE, data);
    }

    // 成功不带参数
    public ResponseBase setResultSuccess() {
        return new ResponseBase(Constants.HTTP_RES_CODE_200, Constants.HTTP_RES_CODE_200_VALUE, null);
    }

    // 成功但需要返回提示信息
    public ResponseBase setResultTooltip(Object data) {
        return new ResponseBase(Constants.HTTP_RES_CODE_201, Constants.HTTP_RES_CODE_201_VALUE, data);
    }

    // 通用封装
    public ResponseBase setResult(int code, String msg, Object data) {
        return new ResponseBase(code, msg, data);
    }

    // 通用封装
    public ResponseBase setResult(int code, String msg, Object data, String[] params) {
        int i = 1;
        for (String param : params) {
            StringBuilder sb = new StringBuilder();
            sb.append("#\\{").append(i).append("\\}");
            msg = msg.replaceAll(sb.toString(), param);
            i++;
        }
        return new ResponseBase(code, msg, data);
    }
}
