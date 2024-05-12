package cn.mw.monitor.model.param;

import lombok.Data;

/**
 * @author qzg
 * @date 2023/7/11
 */
@Data
public class MwModelEditorDataBaseConfigParam {
    //配置名称
    private String configName;
    //服务器IP
    private String configServerIp;
    //服务器端口
    private String configPort;
    //服务器实例
    private String configInstance;
    //数据库驱动
    private String configDriver;
}
