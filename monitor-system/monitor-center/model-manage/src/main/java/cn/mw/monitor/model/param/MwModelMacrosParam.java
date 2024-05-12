package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/4/20 17:15
 */
@Data
@ApiModel
public class MwModelMacrosParam extends MwModelRelationInfoParam{
    private String USERNAME;
    private String PASSWORD;
    private String HOST;
    private String PORT;
    private String URL;
    private String DSN;
    private String SCHEME;
    private String PATH;
    private String INSTANCE;
    private String SYSNR;
    private String CLIENT;
    private String TOKENS;
}
