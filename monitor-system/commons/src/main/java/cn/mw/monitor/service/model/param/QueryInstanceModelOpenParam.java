package cn.mw.monitor.service.model.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

/**
 * @ClassName
 * @Description 开放接口参数类
 * @Author gengjb
 * @Date 2023/4/11 8:34
 * @Version 1.0
 **/
@Data
public class QueryInstanceModelOpenParam{

    //公钥加密分页
    private String pageSize;

    //公钥加密起始页
    private String pageNumber;
}
