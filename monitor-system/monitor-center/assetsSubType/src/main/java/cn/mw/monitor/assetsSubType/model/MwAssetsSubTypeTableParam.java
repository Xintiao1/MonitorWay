package cn.mw.monitor.assetsSubType.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;

@Data
@Component
public class MwAssetsSubTypeTableParam {

    @Value("${assetssubtype.base}")
    private String ids;


}
