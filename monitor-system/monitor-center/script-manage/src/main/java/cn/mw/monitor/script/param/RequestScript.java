package cn.mw.monitor.script.param;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author gui.quanwang
 * @className RequestScript
 * @description 请求类
 * @date 2022/4/14
 */
@Data
public class RequestScript  implements Serializable {

    private static final long serialVersionUID = 1L;

    private String hostip;

    private Integer key;

    private RequestRoot requestRoot;

    private String script;

    private List<String> result;

    private Integer line;

    private String loggerPath;

    private Boolean isOver;

    private Integer type=1;

}
