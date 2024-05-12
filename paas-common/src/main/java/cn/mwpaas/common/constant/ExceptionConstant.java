package cn.mwpaas.common.constant;

import cn.mwpaas.common.exception.BusinessException;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author phzhou
 * @ClassName ExceptionConstant
 * @CreateDate 2019/3/28
 * @Description
 */
public class ExceptionConstant {

    public static final Integer NO_PARAM_CODE = 9998;
    public static final String NO_PARAM_MESSAGE = "缺少参数%s";

    public static void verifyParam(Map<String, Object> map) {
        StringBuffer sb = new StringBuffer();
        map.forEach((key, value) -> {
            if (value == null) {
                sb.append(key).append("|");
            } else if (value instanceof String) {
                String str = String.valueOf(value);
                if (StringUtils.isBlank(str)) {
                    sb.append(key).append("|");
                }
            } else if (value instanceof List) {
                List list = (List) value;
                if (CollectionUtils.isEmpty(list)) {
                    sb.append(key).append("|");
                }
            }
        });
        if (sb.length() > 0) {
            throw new BusinessException(NO_PARAM_CODE, String.format(NO_PARAM_MESSAGE, sb));
        }
    }
}
